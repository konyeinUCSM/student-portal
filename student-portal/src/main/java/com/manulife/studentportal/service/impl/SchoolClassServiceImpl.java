package com.manulife.studentportal.service.impl;

import com.manulife.studentportal.dto.request.CreateClassRequest;
import com.manulife.studentportal.dto.request.UpdateClassRequest;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.StudentResponse;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.entity.Teacher;
import com.manulife.studentportal.exception.DuplicateResourceException;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.SchoolClassMapper;
import com.manulife.studentportal.mapper.StudentMapper;
import com.manulife.studentportal.repository.SchoolClassRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.SchoolClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolClassServiceImpl implements SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassMapper schoolClassMapper;
    private final StudentMapper studentMapper;
    private final SecurityService securityService;

    @Override
    public SchoolClassResponse create(CreateClassRequest request) {
        log.info("Creating class with name: {}", request.getName());

        // Validate name uniqueness
        if (schoolClassRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Class with name " + request.getName() + " already exists");
        }

        SchoolClass schoolClass = SchoolClass.builder()
                .name(request.getName())
                .build();

        schoolClass = schoolClassRepository.save(schoolClass);
        log.info("Class created successfully with id: {}", schoolClass.getId());

        return schoolClassMapper.toResponse(schoolClass);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassResponse getById(Long id) {
        log.debug("Fetching class with id: {}", id);
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));
        return schoolClassMapper.toResponse(schoolClass);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolClassResponse> getAll(Pageable pageable) {
        log.debug("Fetching all classes with pagination: {}", pageable);

        String role = securityService.getCurrentRole();

        if ("ADMIN".equals(role)) {
            // ADMIN gets all classes
            Page<SchoolClass> classes = schoolClassRepository.findAll(pageable);
            return classes.map(schoolClassMapper::toResponse);
        } else if ("TEACHER".equals(role)) {
            // TEACHER gets only assigned classes
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            Set<SchoolClass> assignedClasses = teacher.getClasses();
            List<SchoolClassResponse> classResponses = assignedClasses.stream()
                    .map(schoolClassMapper::toResponse)
                    .collect(Collectors.toList());

            // Create a page from the list (simple pagination)
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), classResponses.size());
            List<SchoolClassResponse> pageContent = classResponses.subList(start, end);

            return new PageImpl<>(pageContent, pageable, classResponses.size());
        }

        return Page.empty(pageable);
    }

    @Override
    public SchoolClassResponse update(Long id, UpdateClassRequest request) {
        log.info("Updating class with id: {}", id);

        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        // Validate name uniqueness (excluding current class)
        if (!schoolClass.getName().equals(request.getName()) &&
            schoolClassRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Class with name " + request.getName() + " already exists");
        }

        schoolClass.setName(request.getName());
        schoolClass = schoolClassRepository.save(schoolClass);

        log.info("Class updated successfully with id: {}", id);
        return schoolClassMapper.toResponse(schoolClass);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting class with id: {}", id);

        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        schoolClass.setDeleted(true);

        log.info("Class soft deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudents(Long id, Pageable pageable) {
        log.debug("Fetching students for class id: {}", id);

        // Verify class exists
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        // Get all students in this class
        List<Student> students = studentRepository.findBySchoolClassId(id, pageable);

        return students.stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
