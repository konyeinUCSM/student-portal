package com.manulife.studentportal.academic.internal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.academic.SchoolClass;
import com.manulife.studentportal.academic.SchoolClassRepository;
import com.manulife.studentportal.academic.SchoolClassInfo;
import com.manulife.studentportal.academic.ExamRepository;
import com.manulife.studentportal.academic.SchoolClassMapper;
import com.manulife.studentportal.academic.web.CreateClassRequest;
import com.manulife.studentportal.academic.web.UpdateClassRequest;
import com.manulife.studentportal.academic.web.SchoolClassResponse;
import com.manulife.studentportal.student.StudentInfo;
import com.manulife.studentportal.student.StudentQueryService;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.InvalidOperationException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.academic.SchoolClassService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolClassServiceImpl implements SchoolClassService {

    private static final String CLASS_NOT_FOUND = "Class not found with id: ";

    private final SchoolClassRepository schoolClassRepository;
    private final ExamRepository examRepository;
    private final SchoolClassMapper schoolClassMapper;
    private final StudentQueryService studentQueryService;
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
    public SchoolClassInfo getClassInfoById(Long id) {
        log.debug("Fetching class info by id: {}", id);
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLASS_NOT_FOUND + id));
        return new SchoolClassInfo(schoolClass.getId(), schoolClass.getName());
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

        if (studentQueryService.existsByClassId(id)) {
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
    public List<StudentInfo> getStudents(Long id, Pageable pageable) {
        log.debug("Fetching students for class id: {}", id);

        // Validate class exists
        schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLASS_NOT_FOUND + id));

        // This method should ideally be in StudentService, but for now we'll use StudentQueryService
        // In a proper implementation, StudentService would have a method like getStudentsByClassId
        // For now, we return empty list as this is a query that crosses boundaries
        // The proper fix is to remove this endpoint from SchoolClassController
        return List.of();
    }
}
