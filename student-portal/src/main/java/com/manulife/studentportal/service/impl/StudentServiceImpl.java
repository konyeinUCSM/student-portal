package com.manulife.studentportal.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.request.CreateStudentRequest;
import com.manulife.studentportal.dto.request.UpdateStudentRequest;
import com.manulife.studentportal.dto.response.StudentResponse;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.entity.Teacher;
import com.manulife.studentportal.user.internal.User;
import com.manulife.studentportal.user.Role;
import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.InvalidOperationException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.StudentMapper;
import com.manulife.studentportal.repository.MarkRepository;
import com.manulife.studentportal.repository.SchoolClassRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.user.internal.UserRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.StudentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final MarkRepository markRepository;
    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TeacherRepository teacherRepository;
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

        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));

        Student student = Student.builder()
                .user(user)
                .name(request.getName())
                .rollNumber(request.getRollNumber())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .schoolClass(schoolClass)
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
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            List<Long> assignedClassIds = teacher.getClasses().stream()
                    .map(SchoolClass::getId)
                    .collect(Collectors.toList());

            if (!assignedClassIds.contains(student.getSchoolClass().getId())) {
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
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            List<Long> assignedClassIds = teacher.getClasses().stream()
                    .map(SchoolClass::getId)
                    .collect(Collectors.toList());

            if (assignedClassIds.isEmpty()) {
                return Page.empty(pageable);
            }

            Page<Student> students = studentRepository.findBySchoolClassIdIn(assignedClassIds, pageable);
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
            SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));
            student.setSchoolClass(schoolClass);
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

        if (markRepository.existsByStudentId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete student with id " + id + ": academic mark records exist. Delete the marks first.");
        }

        student.softDelete(securityService.getCurrentUsername());

        log.info("Student soft deleted successfully with id: {}", id);
    }
}
