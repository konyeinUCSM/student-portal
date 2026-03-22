package com.manulife.studentportal.academic.internal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.academic.Exam;
import com.manulife.studentportal.academic.ExamRepository;
import com.manulife.studentportal.academic.MarkRepository;
import com.manulife.studentportal.academic.MarkMapper;
import com.manulife.studentportal.academic.GradeCalculator;
import com.manulife.studentportal.academic.SchoolClassService;
import com.manulife.studentportal.academic.web.BatchMarkRequest;
import com.manulife.studentportal.academic.web.CreateMarkRequest;
import com.manulife.studentportal.academic.web.UpdateMarkRequest;
import com.manulife.studentportal.academic.web.MarkResponse;
import com.manulife.studentportal.student.StudentQueryService;
import com.manulife.studentportal.shared.exception.BusinessLogicException;
import com.manulife.studentportal.shared.exception.DuplicateResourceException;
import com.manulife.studentportal.shared.exception.ResourceNotFoundException;
import com.manulife.studentportal.teacher.TeacherRepository;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.academic.MarkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarkServiceImpl implements MarkService {

    private static final String MARK_NOT_FOUND = "Mark not found with id: ";

    private final MarkRepository markRepository;
    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final StudentQueryService studentQueryService;
    private final MarkMapper markMapper;
    private final SecurityService securityService;
    private final SchoolClassService schoolClassService;

    @Override
    public MarkResponse create(CreateMarkRequest request) {
        log.info("Creating mark for studentId: {}, examId: {}", request.getStudentId(), request.getExamId());

        // Validate student exists
        var studentInfo = studentQueryService.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + request.getExamId()));

        if (!studentInfo.classId().equals(exam.getSchoolClass().getId())) {
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
                .studentId(request.getStudentId())
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
            // Validate student exists
            var studentInfo = studentQueryService.findById(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + entry.getStudentId()));

            if (!studentInfo.classId().equals(exam.getSchoolClass().getId())) {
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
                    .studentId(entry.getStudentId())
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
            if (!mark.getStudentId().equals(securityService.getCurrentProfileId())) {
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

    @Override
    @Transactional(readOnly = true)
    public Page<MarkResponse> getMarksByStudent(Long studentId, Pageable pageable) {
        log.debug("Fetching marks for studentId: {}", studentId);
        Page<Mark> marks = markRepository.findByStudentId(studentId, pageable);
        return marks.map(markMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkResponse> getMarksByStudentAndExam(Long studentId, Long examId) {
        log.debug("Fetching marks for studentId: {} and examId: {}", studentId, examId);

        // Validate student exists
        var studentInfo = studentQueryService.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        // Validate exam belongs to student's class
        if (!exam.getSchoolClass().getId().equals(studentInfo.classId())) {
            throw new AccessDeniedException("Exam does not belong to student's class");
        }

        List<Mark> marks = markRepository.findByStudentIdAndExamId(studentId, examId)
                .map(List::of)
                .orElse(List.of());

        return marks.stream()
                .map(markMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public com.manulife.studentportal.academic.web.GradeSummaryResponse getGradeSummary(Long studentId) {
        log.debug("Calculating grade summary for studentId: {}", studentId);

        var studentInfo = studentQueryService.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Mark> allMarks = markRepository.findByStudentId(studentId);

        java.util.Map<String, List<Mark>> marksBySubject = new java.util.HashMap<>();
        for (Mark mark : allMarks) {
            String subjectName = mark.getExam().getSubject().getName();
            marksBySubject.computeIfAbsent(subjectName, k -> new ArrayList<>()).add(mark);
        }

        List<com.manulife.studentportal.academic.web.SubjectGradeResponse> subjectGrades = new ArrayList<>();
        double totalPercentageSum = 0.0;
        int subjectCount = 0;

        for (java.util.Map.Entry<String, List<Mark>> entry : marksBySubject.entrySet()) {
            String subjectName = entry.getKey();
            List<Mark> subjectMarks = entry.getValue();

            double subjectPercentageSum = 0.0;
            for (Mark mark : subjectMarks) {
                double percentage = GradeCalculator.calculatePercentage(mark.getScore(), mark.getExam().getFullMarks());
                subjectPercentageSum += percentage;
            }

            double averagePercentage = subjectPercentageSum / subjectMarks.size();
            String grade = GradeCalculator.gradeFromPercentage(averagePercentage);

            com.manulife.studentportal.academic.web.SubjectGradeResponse subjectGrade =
                    com.manulife.studentportal.academic.web.SubjectGradeResponse.builder()
                    .subjectName(subjectName)
                    .averagePercentage(Math.round(averagePercentage * 100.0) / 100.0)
                    .grade(grade)
                    .examsCount(subjectMarks.size())
                    .build();

            subjectGrades.add(subjectGrade);

            totalPercentageSum += averagePercentage;
            subjectCount++;
        }

        double overallPercentage = 0.0;
        String overallGrade = "F";

        if (subjectCount > 0) {
            overallPercentage = totalPercentageSum / subjectCount;
            overallPercentage = Math.round(overallPercentage * 100.0) / 100.0;
            overallGrade = GradeCalculator.gradeFromPercentage(overallPercentage);
        }

        com.manulife.studentportal.academic.SchoolClassInfo classInfo =
                schoolClassService.getClassInfoById(studentInfo.classId());

        return com.manulife.studentportal.academic.web.GradeSummaryResponse.builder()
                .studentName(studentInfo.name())
                .className(classInfo.name())
                .subjectGrades(subjectGrades)
                .overallPercentage(overallPercentage)
                .overallGrade(overallGrade)
                .build();
    }
}
