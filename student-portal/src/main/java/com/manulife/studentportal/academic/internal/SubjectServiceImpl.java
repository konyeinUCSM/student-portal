package com.manulife.studentportal.academic.internal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.academic.ExamRepository;
import com.manulife.studentportal.academic.Subject;
import com.manulife.studentportal.academic.SubjectInfo;
import com.manulife.studentportal.academic.SubjectMapper;
import com.manulife.studentportal.academic.SubjectRepository;
import com.manulife.studentportal.academic.SubjectService;
import com.manulife.studentportal.academic.web.CreateSubjectRequest;
import com.manulife.studentportal.academic.web.SubjectResponse;
import com.manulife.studentportal.academic.web.UpdateSubjectRequest;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.InvalidOperationException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.teacher.Teacher;
import com.manulife.studentportal.teacher.TeacherRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectMapper subjectMapper;
    private final SecurityService securityService;

    @Override
    public SubjectResponse create(CreateSubjectRequest request) {
        log.info("Creating subject with name: {}", request.getName());

        if (subjectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Subject with name " + request.getName() + " already exists");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .build();

        subject = subjectRepository.save(subject);
        log.info("Subject created successfully with id: {}", subject.getId());

        return subjectMapper.toResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        log.debug("Fetching subject with id: {}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return subjectMapper.toResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectInfo getSubjectInfoById(Long id) {
        log.debug("Fetching subject info by id: {}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return new SubjectInfo(subject.getId(), subject.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectResponse> getAll(Pageable pageable) {
        log.debug("Fetching all subjects with pagination: {}", pageable);

        if (securityService.isAdmin()) {
            // ADMIN gets all subjects
            Page<Subject> subjects = subjectRepository.findAll(pageable);
            return subjects.map(subjectMapper::toResponse);
        } else if (securityService.isTeacher()) {
            // TEACHER gets only assigned subjects
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            List<Subject> subjects = subjectRepository.findAllById(teacher.getSubjectIds());
            Page<Subject> subjectPage = new PageImpl<>(subjects, pageable, subjects.size());
            return subjectPage.map(subjectMapper::toResponse);
        }

        return Page.empty(pageable);
    }

    @Override
    public SubjectResponse update(Long id, UpdateSubjectRequest request) {
        log.info("Updating subject with id: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        // Validate name uniqueness (excluding current subject)
        if (!subject.getName().equals(request.getName()) &&
            subjectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Subject with name " + request.getName() + " already exists");
        }

        subject.setName(request.getName());
        subject = subjectRepository.save(subject);

        log.info("Subject updated successfully with id: {}", id);
        return subjectMapper.toResponse(subject);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting subject with id: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (examRepository.existsBySubjectId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete subject with id " + id + ": it is still used in active exams. Delete the exams first.");
        }

        subject.softDelete(securityService.getCurrentUsername());

        log.info("Subject soft deleted successfully with id: {}", id);
    }
}
