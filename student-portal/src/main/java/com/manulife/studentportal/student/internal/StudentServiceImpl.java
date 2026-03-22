package com.manulife.studentportal.student.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.student.Student;
import com.manulife.studentportal.student.StudentMapper;
import com.manulife.studentportal.student.StudentRepository;
import com.manulife.studentportal.student.StudentService;
import com.manulife.studentportal.student.web.CreateStudentRequest;
import com.manulife.studentportal.student.web.StudentResponse;
import com.manulife.studentportal.student.web.UpdateStudentRequest;
import com.manulife.studentportal.teacher.TeacherQueryService;
import com.manulife.studentportal.user.Role;
import com.manulife.studentportal.user.User;
import com.manulife.studentportal.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final TeacherQueryService teacherQueryService;
    private final StudentMapper studentMapper;
    private final SecurityService securityService;

    @Override
    public StudentResponse create(CreateStudentRequest request) {
        log.info("Creating student profile for userId: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getRole() != Role.STUDENT) {
            throw new BusinessLogicException("User with id " + request.getUserId() + " does not have STUDENT role");
        }

        if (studentRepository.countByRollNumberAllRecords(request.getRollNumber()) > 0) {
            throw new DuplicateResourceException("Student with roll number " + request.getRollNumber() + " already exists");
        }

        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("Student profile already exists for userId: " + request.getUserId());
        }

        // Note: classId validation removed to avoid cyclic dependency with academic module
        // Class existence will be validated by database queries when marks are assigned

        Student student = Student.builder()
                .userId(user.getId())
                .name(request.getName())
                .rollNumber(request.getRollNumber())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .classId(request.getClassId())
                .build();

        student = studentRepository.save(student);
        log.info("Student created successfully with id: {} for userId: {}", student.getId(), request.getUserId());

        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        log.debug("Fetching student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (securityService.isAdmin()) {
            // ADMIN can view any student
            return studentMapper.toResponse(student);
        } else if (securityService.isTeacher()) {
            // TEACHER can only view students in their assigned classes
            Long teacherId = securityService.getCurrentProfileId();
            boolean isAssigned = teacherQueryService.isTeacherAssignedToClass(teacherId, student.getClassId());

            if (!isAssigned) {
                throw new AccessDeniedException("You do not have permission to view this student");
            }

            return studentMapper.toResponse(student);
        } else if (securityService.isStudent()) {
            // STUDENT can only view their own profile
            Long studentProfileId = securityService.getCurrentProfileId();
            if (!id.equals(studentProfileId)) {
                throw new AccessDeniedException("You can only view your own profile");
            }

            return studentMapper.toResponse(student);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> getAll(Pageable pageable) {
        log.debug("Fetching all students with pagination: {}", pageable);

        if (securityService.isAdmin()) {
            // ADMIN gets all students
            Page<Student> students = studentRepository.findAll(pageable);
            return students.map(studentMapper::toResponse);
        } else if (securityService.isTeacher()) {
            // TEACHER gets students in their assigned classes only
            Long teacherId = securityService.getCurrentProfileId();
            var teacherInfo = teacherQueryService.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            if (teacherInfo.classIds().isEmpty()) {
                return Page.empty(pageable);
            }

            Page<Student> students = studentRepository.findByClassIdIn(teacherInfo.classIds(), pageable);
            return students.map(studentMapper::toResponse);
        }

        return Page.empty(pageable);
    }

    @Override
    public StudentResponse update(Long id, UpdateStudentRequest request) {
        log.info("Updating student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Update only non-null fields
        if (request.getName() != null) {
            student.setName(request.getName());
        }
        if (request.getPhone() != null) {
            student.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getClassId() != null) {
            // Note: classId validation removed to avoid cyclic dependency with academic module
            student.setClassId(request.getClassId());
        }

        student = studentRepository.save(student);
        log.info("Student updated successfully with id: {}", id);

        return studentMapper.toResponse(student);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Note: Mark existence check removed to avoid cyclic dependency with academic module
        // If marks exist, they should be handled by cascade delete or separate cleanup

        student.softDelete(securityService.getCurrentUsername());

        log.info("Student soft deleted successfully with id: {}", id);
    }
}
