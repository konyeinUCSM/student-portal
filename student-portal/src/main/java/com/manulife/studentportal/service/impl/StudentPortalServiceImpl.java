package com.manulife.studentportal.service.impl;

import com.manulife.studentportal.dto.response.GradeSummaryResponse;
import com.manulife.studentportal.dto.response.MarkResponse;
import com.manulife.studentportal.dto.response.SubjectGradeResponse;
import com.manulife.studentportal.entity.Exam;
import com.manulife.studentportal.entity.Mark;
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.MarkMapper;
import com.manulife.studentportal.repository.ExamRepository;
import com.manulife.studentportal.repository.MarkRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.StudentPortalService;
import com.manulife.studentportal.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentPortalServiceImpl implements StudentPortalService {

    private final StudentRepository studentRepository;
    private final MarkRepository markRepository;
    private final ExamRepository examRepository;
    private final MarkMapper markMapper;
    private final SecurityService securityService;

    @Override
    public Page<MarkResponse> getMyMarks(Pageable pageable) {
        log.debug("Fetching marks for current student");

        // Get student ID from JWT (NEVER from URL params)
        Long studentId = securityService.getCurrentProfileId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Page<Mark> marks = markRepository.findByStudentId(studentId, pageable);
        return marks.map(markMapper::toResponse);
    }

    @Override
    public List<MarkResponse> getMarksByExam(Long examId) {
        log.debug("Fetching marks for current student and examId: {}", examId);

        // Get student ID from JWT
        Long studentId = securityService.getCurrentProfileId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Validate exam exists
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        // Validate exam belongs to student's class
        if (!exam.getSchoolClass().getId().equals(student.getSchoolClass().getId())) {
            throw new AccessDeniedException("You can only view exams for your class");
        }

        // Get mark for this student and exam
        List<Mark> marks = markRepository.findByStudentIdAndExamId(studentId, examId)
                .map(List::of)
                .orElse(List.of());

        return marks.stream()
                .map(markMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GradeSummaryResponse getGradeSummary() {
        log.debug("Calculating grade summary for current student");

        // Get student ID from JWT
        Long studentId = securityService.getCurrentProfileId();

        // 1. Get student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // 2. Get all exams for student's class
        Page<Exam> examsPage = examRepository.findBySchoolClassId(student.getSchoolClass().getId(), Pageable.unpaged());
        List<Exam> allExams = examsPage.getContent();

        // 3. Get all marks for this student
        List<Mark> allMarks = markRepository.findByStudentId(studentId);

        // 4. Group marks by subject
        Map<String, List<Mark>> marksBySubject = new HashMap<>();
        for (Mark mark : allMarks) {
            String subjectName = mark.getExam().getSubject().getName();
            marksBySubject.computeIfAbsent(subjectName, k -> new ArrayList<>()).add(mark);
        }

        // 5. Calculate average for each subject
        List<SubjectGradeResponse> subjectGrades = new ArrayList<>();
        double totalPercentageSum = 0.0;
        int subjectCount = 0;

        for (Map.Entry<String, List<Mark>> entry : marksBySubject.entrySet()) {
            String subjectName = entry.getKey();
            List<Mark> subjectMarks = entry.getValue();

            // Calculate average percentage for this subject
            double subjectPercentageSum = 0.0;
            for (Mark mark : subjectMarks) {
                double percentage = GradeCalculator.calculatePercentage(mark.getScore(), mark.getExam().getFullMarks());
                subjectPercentageSum += percentage;
            }

            double averagePercentage = subjectPercentageSum / subjectMarks.size();
            String grade = GradeCalculator.gradeFromPercentage(averagePercentage);

            SubjectGradeResponse subjectGrade = SubjectGradeResponse.builder()
                    .subjectName(subjectName)
                    .averagePercentage(Math.round(averagePercentage * 100.0) / 100.0) // 2 decimal places
                    .grade(grade)
                    .examsCount(subjectMarks.size())
                    .build();

            subjectGrades.add(subjectGrade);

            totalPercentageSum += averagePercentage;
            subjectCount++;
        }

        // 6. Calculate overall percentage and grade
        double overallPercentage = 0.0;
        String overallGrade = "F";

        if (subjectCount > 0) {
            overallPercentage = totalPercentageSum / subjectCount;
            overallPercentage = Math.round(overallPercentage * 100.0) / 100.0; // 2 decimal places
            overallGrade = GradeCalculator.gradeFromPercentage(overallPercentage);
        }

        // 7. Build and return response
        return GradeSummaryResponse.builder()
                .studentName(student.getName())
                .className(student.getSchoolClass().getName())
                .subjectGrades(subjectGrades)
                .overallPercentage(overallPercentage)
                .overallGrade(overallGrade)
                .build();
    }
}
