package com.manulife.studentportal.service.impl;

import com.manulife.studentportal.dto.request.CreateSubjectRequest;
import com.manulife.studentportal.dto.request.UpdateSubjectRequest;
import com.manulife.studentportal.dto.response.SubjectResponse;
import com.manulife.studentportal.entity.Subject;
import com.manulife.studentportal.entity.Teacher;
import com.manulife.studentportal.exception.DuplicateResourceException;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.SubjectMapper;
import com.manulife.studentportal.repository.SubjectRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.SubjectService;
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
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectMapper subjectMapper;
    private final SecurityService securityService;

    @Override
    public SubjectResponse create(CreateSubjectRequest request) {
        log.info("Creating subject with name: {}", request.getName());

        // Validate name uniqueness
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

            Set<Subject> assignedSubjects = teacher.getSubjects();
            List<SubjectResponse> subjectResponses = assignedSubjects.stream()
                    .map(subjectMapper::toResponse)
                    .collect(Collectors.toList());

            // Create a page from the list (simple pagination)
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), subjectResponses.size());
            List<SubjectResponse> pageContent = subjectResponses.subList(start, end);

            return new PageImpl<>(pageContent, pageable, subjectResponses.size());
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

        subject.setDeleted(true);

        log.info("Subject soft deleted successfully with id: {}", id);
    }
}
