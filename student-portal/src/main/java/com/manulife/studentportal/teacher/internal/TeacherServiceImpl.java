package com.manulife.studentportal.teacher.internal;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.academic.SchoolClassService;
import com.manulife.studentportal.academic.SubjectService;
import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.teacher.Teacher;
import com.manulife.studentportal.teacher.TeacherRepository;
import com.manulife.studentportal.teacher.TeacherService;
import com.manulife.studentportal.teacher.web.AssignClassesRequest;
import com.manulife.studentportal.teacher.web.AssignSubjectsRequest;
import com.manulife.studentportal.teacher.web.CreateTeacherRequest;
import com.manulife.studentportal.teacher.web.TeacherResponse;
import com.manulife.studentportal.teacher.web.UpdateTeacherRequest;
import com.manulife.studentportal.user.Role;
import com.manulife.studentportal.user.User;
import com.manulife.studentportal.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final SchoolClassService schoolClassService;
    private final SubjectService subjectService;
    private final TeacherMapper teacherMapper;
    private final SecurityService securityService;

    @Override
    public TeacherResponse create(CreateTeacherRequest request) {
        log.info("Creating teacher profile for userId: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getRole() != Role.TEACHER) {
            throw new BusinessLogicException("User with id " + request.getUserId() + " does not have TEACHER role");
        }

        if (teacherRepository.countByStaffIdAllRecords(request.getStaffId()) > 0) {
            throw new DuplicateResourceException("Teacher with staffId " + request.getStaffId() + " already exists");
        }

        if (teacherRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new DuplicateResourceException("Teacher profile already exists for userId: " + request.getUserId());
        }

        Teacher teacher = Teacher.builder()
                .userId(user.getId())
                .name(request.getName())
                .staffId(request.getStaffId())
                .phone(request.getPhone())
                .classIds(new HashSet<>())
                .subjectIds(new HashSet<>())
                .build();

        teacher = teacherRepository.save(teacher);
        log.info("Teacher created successfully with id: {} for userId: {}", teacher.getId(), request.getUserId());

        return teacherMapper.toResponse(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getById(Long id) {
        log.debug("Fetching teacher with id: {}", id);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return teacherMapper.toResponse(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherResponse> getAll(Pageable pageable) {
        log.debug("Fetching all teachers with pagination: {}", pageable);
        Page<Teacher> teachers = teacherRepository.findAll(pageable);
        return teachers.map(teacherMapper::toResponse);
    }

    @Override
    public TeacherResponse update(Long id, UpdateTeacherRequest request) {
        log.info("Updating teacher with id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (request.getName() != null) {
            teacher.setName(request.getName());
        }
        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }

        teacher = teacherRepository.save(teacher);
        log.info("Teacher updated successfully with id: {}", id);

        return teacherMapper.toResponse(teacher);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting teacher with id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        teacher.softDelete(securityService.getCurrentUsername());

        log.info("Teacher soft deleted successfully with id: {}", id);
    }

    @Override
    public void assignClasses(Long id, AssignClassesRequest request) {
        log.info("Assigning classes to teacher with id: {}, classIds: {}", id, request.getClassIds());

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        // Validate all class IDs exist
        for (Long classId : request.getClassIds()) {
            try {
                schoolClassService.getClassInfoById(classId);
            } catch (Exception e) {
                throw new ResourceNotFoundException("Class not found with id: " + classId);
            }
        }

        teacher.setClassIds(new HashSet<>(request.getClassIds()));
        teacherRepository.save(teacher);

        log.info("Classes assigned successfully to teacher id: {}", id);
    }

    @Override
    public void assignSubjects(Long id, AssignSubjectsRequest request) {
        log.info("Assigning subjects to teacher with id: {}, subjectIds: {}", id, request.getSubjectIds());

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        // Validate all subject IDs exist
        for (Long subjectId : request.getSubjectIds()) {
            try {
                subjectService.getSubjectInfoById(subjectId);
            } catch (Exception e) {
                throw new ResourceNotFoundException("Subject not found with id: " + subjectId);
            }
        }

        teacher.setSubjectIds(new HashSet<>(request.getSubjectIds()));
        teacherRepository.save(teacher);

        log.info("Subjects assigned successfully to teacher id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.manulife.studentportal.academic.SchoolClassInfo> getClasses(Long id) {
        log.debug("Fetching assigned classes for teacher id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        return teacher.getClassIds().stream()
                .map(schoolClassService::getClassInfoById)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.manulife.studentportal.academic.SubjectInfo> getSubjects(Long id) {
        log.debug("Fetching assigned subjects for teacher id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        return teacher.getSubjectIds().stream()
                .map(subjectService::getSubjectInfoById)
                .collect(Collectors.toList());
    }
}
