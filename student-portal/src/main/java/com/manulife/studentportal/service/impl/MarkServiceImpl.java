package com.manulife.studentportal.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.request.BatchMarkRequest;
import com.manulife.studentportal.dto.request.CreateMarkRequest;
import com.manulife.studentportal.dto.request.UpdateMarkRequest;
import com.manulife.studentportal.dto.response.MarkResponse;
import com.manulife.studentportal.entity.Exam;
import com.manulife.studentportal.entity.Mark;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.entity.Teacher;
import com.manulife.studentportal.exception.BusinessLogicException;
import com.manulife.studentportal.exception.DuplicateResourceException;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.MarkMapper;
import com.manulife.studentportal.repository.ExamRepository;
import com.manulife.studentportal.repository.MarkRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.MarkService;
import com.manulife.studentportal.util.GradeCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkServiceImpl implements MarkService {

    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final MarkMapper markMapper;
    private final SecurityService securityService;

    @Override
    public MarkResponse create(CreateMarkRequest request) {
        log.info("Creating mark for studentId: {}, examId: {}", request.getStudentId(), request.getExamId());

        // Validate student exists
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        // Validate exam exists
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + request.getExamId()));

        // Validate student belongs to exam's class
        if (!student.getSchoolClass().getId().equals(exam.getSchoolClass().getId())) {
            throw new BusinessLogicException("Student does not belong to the exam's class");
        }

        // Validate score <= exam.fullMarks
        if (request.getScore() > exam.getFullMarks()) {
            throw new BusinessLogicException("Score cannot exceed full marks (" + exam.getFullMarks() + ")");
        }

        // If TEACHER: validate assigned to exam's class AND subject
        if (securityService.isTeacher()) {
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            Long examClassId = exam.getSchoolClass().getId();
            Long examSubjectId = exam.getSubject().getId();

            boolean assignedToClass = teacher.getClasses().stream()
                    .anyMatch(c -> c.getId().equals(examClassId));
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(examSubjectId));

            if (!assignedToClass || !assignedToSubject) {
                throw new AccessDeniedException("You can only enter marks for classes and subjects you are assigned to");
            }
        }

        // Check no duplicate (studentId + examId)
        if (markRepository.existsByStudentIdAndExamId(request.getStudentId(), request.getExamId())) {
            throw new DuplicateResourceException("Mark already exists for this student and exam");
        }

        // Calculate grade
        String grade = GradeCalculator.calculateGrade(request.getScore(), exam.getFullMarks());

        // Create mark
        Mark mark = Mark.builder()
                .student(student)
                .exam(exam)
                .score(request.getScore())
                .grade(grade)
                .remarks(request.getRemarks())
                .build();

        mark = markRepository.save(mark);
        log.info("Mark created successfully: studentId={}, examId={}, score={}, grade={}",
                request.getStudentId(), request.getExamId(), request.getScore(), grade);

        return markMapper.toResponse(mark);
    }

    @Override
    public void createBatch(BatchMarkRequest request) {
        log.info("Creating batch marks for examId: {}, count: {}", request.getExamId(), request.getMarks().size());

        // Validate exam exists
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + request.getExamId()));

        // If TEACHER: validate assigned to exam's class AND subject
        if (securityService.isTeacher()) {
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            Long examClassId = exam.getSchoolClass().getId();
            Long examSubjectId = exam.getSubject().getId();

            boolean assignedToClass = teacher.getClasses().stream()
                    .anyMatch(c -> c.getId().equals(examClassId));
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(examSubjectId));

            if (!assignedToClass || !assignedToSubject) {
                throw new AccessDeniedException("You can only enter marks for classes and subjects you are assigned to");
            }
        }

        List<Mark> marksToSave = new ArrayList<>();

        for (BatchMarkRequest.BatchMarkEntry entry : request.getMarks()) {
            // Validate student exists
            Student student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + entry.getStudentId()));

            // Validate student belongs to exam's class
            if (!student.getSchoolClass().getId().equals(exam.getSchoolClass().getId())) {
                throw new BusinessLogicException("Student " + entry.getStudentId() + " does not belong to the exam's class");
            }

            // Validate score <= exam.fullMarks
            if (entry.getScore() > exam.getFullMarks()) {
                throw new BusinessLogicException("Score for student " + entry.getStudentId() + " cannot exceed full marks (" + exam.getFullMarks() + ")");
            }

            // Check no duplicate
            if (markRepository.existsByStudentIdAndExamId(entry.getStudentId(), request.getExamId())) {
                throw new DuplicateResourceException("Mark already exists for student " + entry.getStudentId() + " and this exam");
            }

            // Calculate grade
            String grade = GradeCalculator.calculateGrade(entry.getScore(), exam.getFullMarks());

            // Create mark
            Mark mark = Mark.builder()
                    .student(student)
                    .exam(exam)
                    .score(entry.getScore())
                    .grade(grade)
                    .remarks(entry.getRemarks())
                    .build();

            marksToSave.add(mark);
        }

        // Save all marks
        markRepository.saveAll(marksToSave);
        log.info("Batch marks created successfully: examId={}, count={}", request.getExamId(), marksToSave.size());
    }

    @Override
    @Transactional(readOnly = true)
    public MarkResponse getById(Long id) {
        log.debug("Fetching mark with id: {}", id);

        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with id: " + id));

        if (securityService.isAdmin()) {
            // ADMIN sees all
            return markMapper.toResponse(mark);
        } else if (securityService.isTeacher()) {
            // TEACHER must be assigned to mark's exam class and subject
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            Long markClassId = mark.getExam().getSchoolClass().getId();
            Long markSubjectId = mark.getExam().getSubject().getId();

            boolean assignedToClass = teacher.getClasses().stream()
                    .anyMatch(c -> c.getId().equals(markClassId));
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(markSubjectId));

            if (!assignedToClass || !assignedToSubject) {
                throw new AccessDeniedException("You can only view marks for classes and subjects you are assigned to");
            }

            return markMapper.toResponse(mark);
        } else if (securityService.isStudent()) {
            // STUDENT must own the mark
            Long studentId = securityService.getCurrentProfileId();
            if (!mark.getStudent().getId().equals(studentId)) {
                throw new AccessDeniedException("You can only view your own marks");
            }

            return markMapper.toResponse(mark);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MarkResponse> getAll(Pageable pageable, Long examId, Long studentId) {
        log.debug("Fetching all marks with pagination: {}, examId: {}, studentId: {}", pageable, examId, studentId);

        String role = securityService.getCurrentRole();

        if ("ADMIN".equals(role)) {
            // ADMIN gets all marks, with optional filters
            Page<Mark> marks;
            if (examId != null) {
                marks = markRepository.findByExamId(examId, pageable);
            } else if (studentId != null) {
                marks = markRepository.findByStudentId(studentId, pageable);
            } else {
                marks = markRepository.findAll(pageable);
            }
            return marks.map(markMapper::toResponse);
        } else if ("TEACHER".equals(role)) {
            // TEACHER gets marks for exams in assigned classes/subjects
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            List<Long> assignedClassIds = teacher.getClasses().stream()
                    .map(SchoolClass::getId)
                    .collect(Collectors.toList());

            if (assignedClassIds.isEmpty()) {
                return Page.empty(pageable);
            }

            // Get exams for assigned classes
            Page<Exam> assignedExams = examRepository.findBySchoolClassIdIn(assignedClassIds, Pageable.unpaged());
            List<Long> assignedExamIds = assignedExams.getContent().stream()
                    .map(Exam::getId)
                    .collect(Collectors.toList());

            if (assignedExamIds.isEmpty()) {
                return Page.empty(pageable);
            }

            // Apply filters if provided
            Page<Mark> marks;
            if (examId != null) {
                // Check if exam is in assigned list
                if (!assignedExamIds.contains(examId)) {
                    throw new AccessDeniedException("You can only view marks for exams in your assigned classes");
                }
                marks = markRepository.findByExamId(examId, pageable);
            } else if (studentId != null) {
                // Get marks for student, filtered by assigned exams
                marks = markRepository.findByStudentId(studentId, pageable);
                // Filter out marks not in assigned exams
                List<Mark> filteredMarks = marks.getContent().stream()
                        .filter(mark -> assignedExamIds.contains(mark.getExam().getId()))
                        .collect(Collectors.toList());
                marks = new org.springframework.data.domain.PageImpl<>(filteredMarks, pageable, filteredMarks.size());
            } else {
                marks = markRepository.findByExamIdIn(assignedExamIds, pageable);
            }

            return marks.map(markMapper::toResponse);
        }

        return Page.empty(pageable);
    }

    @Override
    public MarkResponse update(Long id, UpdateMarkRequest request) {
        log.info("Updating mark with id: {}", id);

        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with id: " + id));

        // TEACHER must be assigned to mark's exam class and subject
        String role = securityService.getCurrentRole();
        if ("TEACHER".equals(role)) {
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            Long markClassId = mark.getExam().getSchoolClass().getId();
            Long markSubjectId = mark.getExam().getSubject().getId();

            boolean assignedToClass = teacher.getClasses().stream()
                    .anyMatch(c -> c.getId().equals(markClassId));
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(markSubjectId));

            if (!assignedToClass || !assignedToSubject) {
                throw new AccessDeniedException("You can only update marks for classes and subjects you are assigned to");
            }
        }

        // Validate score <= exam.fullMarks
        if (request.getScore() > mark.getExam().getFullMarks()) {
            throw new BusinessLogicException("Score cannot exceed full marks (" + mark.getExam().getFullMarks() + ")");
        }

        // Update score and recalculate grade
        mark.setScore(request.getScore());
        String grade = GradeCalculator.calculateGrade(request.getScore(), mark.getExam().getFullMarks());
        mark.setGrade(grade);

        // Update remarks
        if (request.getRemarks() != null) {
            mark.setRemarks(request.getRemarks());
        }

        mark = markRepository.save(mark);
        log.info("Mark updated successfully with id: {}, newScore: {}, newGrade: {}", id, request.getScore(), grade);

        return markMapper.toResponse(mark);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting mark with id: {}", id);

        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mark not found with id: " + id));

        // Soft delete
        mark.setDeleted(true);

        log.info("Mark soft deleted successfully with id: {}", id);
    }
}
