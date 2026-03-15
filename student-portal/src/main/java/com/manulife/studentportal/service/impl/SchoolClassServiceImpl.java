package com.manulife.studentportal.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.request.CreateClassRequest;
import com.manulife.studentportal.dto.request.UpdateClassRequest;
import com.manulife.studentportal.dto.response.SchoolClassResponse;
import com.manulife.studentportal.dto.response.StudentResponse;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.exception.DuplicateResourceException;
import com.manulife.studentportal.exception.InvalidOperationException;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.SchoolClassMapper;
import com.manulife.studentportal.mapper.StudentMapper;
import com.manulife.studentportal.repository.ExamRepository;
import com.manulife.studentportal.repository.SchoolClassRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.SchoolClassService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolClassServiceImpl implements SchoolClassService {

    private static final String CLASS_NOT_FOUND = "Class not found with id: ";

    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException(CLASS_NOT_FOUND + id));
        return schoolClassMapper.toResponse(schoolClass);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolClassResponse> getAll(Pageable pageable) {
        log.debug("Fetching all classes with pagination: {}", pageable);

        if (securityService.isAdmin()) {
            // ADMIN gets all classes
            Page<SchoolClass> classes = schoolClassRepository.findAll(pageable);
            return classes.map(schoolClassMapper::toResponse);
        } else if (securityService.isTeacher()) {
            // TEACHER gets only assigned classes — paginated at DB level
            Long teacherId = securityService.getCurrentProfileId();
            return schoolClassRepository.findByTeacherId(teacherId, pageable)
                    .map(schoolClassMapper::toResponse);
        }

        return Page.empty(pageable);
    }

    @Override
    public SchoolClassResponse update(Long id, UpdateClassRequest request) {
        log.info("Updating class with id: {}", id);

        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLASS_NOT_FOUND + id));

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
                .orElseThrow(() -> new ResourceNotFoundException(CLASS_NOT_FOUND + id));

        if (studentRepository.existsBySchoolClassId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete class with id " + id + ": it still has active students enrolled. Reassign or remove students first.");
        }

        if (examRepository.existsBySchoolClassId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete class with id " + id + ": it still has active exams. Delete the exams first.");
        }

        schoolClass.softDelete(securityService.getCurrentUsername());

        log.info("Class soft deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudents(Long id, Pageable pageable) {
        log.debug("Fetching students for class id: {}", id);

        // Get all students in this class
        List<Student> students = studentRepository.findBySchoolClassId(id, pageable);

        return students.stream()
                .map(studentMapper::toResponse)
                .toList();
    }
}
