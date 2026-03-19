package com.manulife.studentportal.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.request.AssignClassesRequest;
import com.manulife.studentportal.dto.request.AssignSubjectsRequest;
import com.manulife.studentportal.dto.request.CreateTeacherRequest;
import com.manulife.studentportal.dto.request.UpdateTeacherRequest;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.dto.response.TeacherResponse;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Subject;
import com.manulife.studentportal.entity.Teacher;
import com.manulife.studentportal.entity.User;
import com.manulife.studentportal.enums.Role;
import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.SchoolClassMapper;
import com.manulife.studentportal.mapper.SubjectMapper;
import com.manulife.studentportal.mapper.TeacherMapper;
import com.manulife.studentportal.repository.SchoolClassRepository;
import com.manulife.studentportal.repository.SubjectRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.repository.UserRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.TeacherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherMapper teacherMapper;
    private final SchoolClassMapper schoolClassMapper;
    private final SubjectMapper subjectMapper;
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
                .user(user)
                .name(request.getName())
                .staffId(request.getStaffId())
                .phone(request.getPhone())
                .classes(new HashSet<>())
                .subjects(new HashSet<>())
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

        List<SchoolClass> classes = schoolClassRepository.findAllById(request.getClassIds());

        if (classes.size() != request.getClassIds().size()) {
            Set<Long> foundIds = classes.stream().map(SchoolClass::getId).collect(Collectors.toSet());
            Set<Long> notFoundIds = request.getClassIds().stream()
                    .filter(classId -> !foundIds.contains(classId))
                    .collect(Collectors.toSet());
            throw new ResourceNotFoundException("Classes not found with ids: " + notFoundIds);
        }

        teacher.setClasses(new HashSet<>(classes));
        teacherRepository.save(teacher);

        log.info("Classes assigned successfully to teacher id: {}", id);
    }

    @Override
    public void assignSubjects(Long id, AssignSubjectsRequest request) {
        log.info("Assigning subjects to teacher with id: {}, subjectIds: {}", id, request.getSubjectIds());

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());

        if (subjects.size() != request.getSubjectIds().size()) {
            Set<Long> foundIds = subjects.stream().map(Subject::getId).collect(Collectors.toSet());
            Set<Long> notFoundIds = request.getSubjectIds().stream()
                    .filter(subjectId -> !foundIds.contains(subjectId))
                    .collect(Collectors.toSet());
            throw new ResourceNotFoundException("Subjects not found with ids: " + notFoundIds);
        }

        teacher.setSubjects(new HashSet<>(subjects));
        teacherRepository.save(teacher);

        log.info("Subjects assigned successfully to teacher id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassResponse> getClasses(Long id) {
        log.debug("Fetching assigned classes for teacher id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        return teacher.getClasses().stream()
                .map(schoolClassMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjects(Long id) {
        log.debug("Fetching assigned subjects for teacher id: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        return teacher.getSubjects().stream()
                .map(subjectMapper::toResponse)
                .collect(Collectors.toList());
    }
}
