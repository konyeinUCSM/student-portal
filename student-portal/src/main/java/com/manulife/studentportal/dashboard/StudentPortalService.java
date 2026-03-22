package com.manulife.studentportal.dashboard;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.academic.web.GradeSummaryResponse;
import com.manulife.studentportal.academic.web.MarkResponse;

public interface StudentPortalService {

    Page<MarkResponse> getMyMarks(Pageable pageable);

    List<MarkResponse> getMarksByExam(Long examId);

    GradeSummaryResponse getGradeSummary();
}
