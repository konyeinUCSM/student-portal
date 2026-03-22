package com.manulife.studentportal.dashboard.internal;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.academic.web.GradeSummaryResponse;
import com.manulife.studentportal.academic.web.MarkResponse;
import com.manulife.studentportal.academic.MarkService;
import com.manulife.studentportal.shared.security.SecurityService;
import com.manulife.studentportal.dashboard.StudentPortalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentPortalServiceImpl implements StudentPortalService {

    private final MarkService markService;
    private final SecurityService securityService;

    @Override
    public Page<MarkResponse> getMyMarks(Pageable pageable) {
        log.debug("Fetching marks for current student");

        // Get student ID from JWT (NEVER from URL params)
        Long studentId = securityService.getCurrentProfileId();

        return markService.getMarksByStudent(studentId, pageable);
    }

    @Override
    public List<MarkResponse> getMarksByExam(Long examId) {
        log.debug("Fetching marks for current student and examId: {}", examId);

        // Get student ID from JWT
        Long studentId = securityService.getCurrentProfileId();

        return markService.getMarksByStudentAndExam(studentId, examId);
    }

    @Override
    public GradeSummaryResponse getGradeSummary() {
        log.debug("Calculating grade summary for current student");

        Long studentId = securityService.getCurrentProfileId();

        return markService.getGradeSummary(studentId);
    }
}
