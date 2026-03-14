package com.manulife.studentportal.service;

import com.manulife.studentportal.dto.response.GradeSummaryResponse;
import com.manulife.studentportal.dto.response.MarkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentPortalService {

    Page<MarkResponse> getMyMarks(Pageable pageable);

    List<MarkResponse> getMarksByExam(Long examId);

    GradeSummaryResponse getGradeSummary();
}
