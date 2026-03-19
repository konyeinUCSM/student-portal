package com.manulife.studentportal.service.impl;

import java.util.ArrayList;
import java.util.List;

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
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
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

    private static final String MARK_NOT_FOUND = "Mark not found with id: ";

    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final MarkMapper markMapper;
    private final SecurityService securityService;

    @Override
    public MarkResponse create(CreateMarkRequest request) {
        log.info("Creating mark for studentId: {}, examId: {}", request.getStudentId(), request.getExamId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + request.getExamId()));

        if (!student.getSchoolClass().getId().equals(exam.getSchoolClass().getId())) {
            throw new BusinessLogicException("Student does not belong to the exam's class");
        }

        if (request.getScore() > exam.getFullMarks()) {
            throw new BusinessLogicException("Score cannot exceed full marks (" + exam.getFullMarks() + ")");
        }

        if (securityService.isTeacher()) {
            validateTeacherExamAccess(exam.getSchoolClass().getId(), exam.getSubject().getId());
        }

        if (markRepository.existsByStudentIdAndExamId(request.getStudentId(), request.getExamId())) {
            throw new DuplicateResourceException("Mark already exists for this student and exam");
        }

        String grade = GradeCalculator.calculateGrade(request.getScore(), exam.getFullMarks());

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

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + request.getExamId()));

        if (securityService.isTeacher()) {
            validateTeacherExamAccess(exam.getSchoolClass().getId(), exam.getSubject().getId());
        }

        List<Mark> marksToSave = new ArrayList<>();

        for (BatchMarkRequest.BatchMarkEntry entry : request.getMarks()) {
            Student student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + entry.getStudentId()));

            if (!student.getSchoolClass().getId().equals(exam.getSchoolClass().getId())) {
                throw new BusinessLogicException("Student " + entry.getStudentId() + " does not belong to the exam's class");
            }

            if (entry.getScore() > exam.getFullMarks()) {
                throw new BusinessLogicException("Score for student " + entry.getStudentId() + " cannot exceed full marks (" + exam.getFullMarks() + ")");
            }

            if (markRepository.existsByStudentIdAndExamId(entry.getStudentId(), request.getExamId())) {
                throw new DuplicateResourceException("Mark already exists for student " + entry.getStudentId() + " and this exam");
            }

            String grade = GradeCalculator.calculateGrade(entry.getScore(), exam.getFullMarks());

            marksToSave.add(Mark.builder()
                    .student(student)
                    .exam(exam)
                    .score(entry.getScore())
                    .grade(grade)
                    .remarks(entry.getRemarks())
                    .build());
        }

        markRepository.saveAll(marksToSave);
        log.info("Batch marks created successfully: examId={}, count={}", request.getExamId(), marksToSave.size());
    }

    @Override
    @Transactional(readOnly = true)
    public MarkResponse getById(Long id) {
        log.debug("Fetching mark with id: {}", id);

        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARK_NOT_FOUND + id));

        if (securityService.isAdmin()) {
            return markMapper.toResponse(mark);
        } else if (securityService.isTeacher()) {
            validateTeacherExamAccess(mark.getExam().getSchoolClass().getId(), mark.getExam().getSubject().getId());
            return markMapper.toResponse(mark);
        } else if (securityService.isStudent()) {
            if (!mark.getStudent().getId().equals(securityService.getCurrentProfileId())) {
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

        if (securityService.isAdmin()) {
            return getAdminMarks(examId, studentId, pageable);
        }
        if (securityService.isTeacher()) {
            return getTeacherMarks(examId, studentId, pageable);
        }
        return Page.empty(pageable);
    }

    private Page<MarkResponse> getAdminMarks(Long examId, Long studentId, Pageable pageable) {
        Page<Mark> marks;
        if (examId != null) {
            marks = markRepository.findByExamId(examId, pageable);
        } else if (studentId != null) {
            marks = markRepository.findByStudentId(studentId, pageable);
        } else {
            marks = markRepository.findAll(pageable);
        }
        return marks.map(markMapper::toResponse);
    }

    private Page<MarkResponse> getTeacherMarks(Long examId, Long studentId, Pageable pageable) {
        Long teacherId = securityService.getCurrentProfileId();
        List<Long> assignedClassIds = teacherRepository.findClassIdsByTeacherId(teacherId);
        if (assignedClassIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> assignedExamIds = examRepository.findIdsBySchoolClassIdIn(assignedClassIds);
        if (assignedExamIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Mark> marks;
        if (examId != null) {
            if (!assignedExamIds.contains(examId)) {
                throw new AccessDeniedException("You can only view marks for exams in your assigned classes");
            }
            marks = markRepository.findByExamId(examId, pageable);
        } else if (studentId != null) {
            marks = markRepository.findByStudentIdAndExamIdIn(studentId, assignedExamIds, pageable);
        } else {
            marks = markRepository.findByExamIdIn(assignedExamIds, pageable);
        }
        return marks.map(markMapper::toResponse);
    }

    @Override
    public MarkResponse update(Long id, UpdateMarkRequest request) {
        log.info("Updating mark with id: {}", id);

        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARK_NOT_FOUND + id));

        if (securityService.isTeacher()) {
            validateTeacherExamAccess(mark.getExam().getSchoolClass().getId(), mark.getExam().getSubject().getId());
        }

        if (request.getScore() > mark.getExam().getFullMarks()) {
            throw new BusinessLogicException("Score cannot exceed full marks (" + mark.getExam().getFullMarks() + ")");
        }

        mark.setScore(request.getScore());
        mark.setGrade(GradeCalculator.calculateGrade(request.getScore(), mark.getExam().getFullMarks()));

        if (request.getRemarks() != null) {
            mark.setRemarks(request.getRemarks());
        }

        mark = markRepository.save(mark);
        log.info("Mark updated successfully with id: {}, newScore: {}, newGrade: {}", id, mark.getScore(), mark.getGrade());

        return markMapper.toResponse(mark);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting mark with id: {}", id);

        Mark mark = markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MARK_NOT_FOUND + id));

        mark.softDelete(securityService.getCurrentUsername());

        log.info("Mark soft deleted successfully with id: {}", id);
    }

    private void validateTeacherExamAccess(Long examClassId, Long examSubjectId) {
        if (!securityService.isTeacherAssignedToClass(examClassId) ||
                !securityService.isTeacherAssignedToSubject(examSubjectId)) {
            throw new AccessDeniedException("You can only access marks for classes and subjects you are assigned to");
        }
    }
}
