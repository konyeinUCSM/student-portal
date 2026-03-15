package com.manulife.studentportal.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.manulife.studentportal.dto.response.GradeSummaryResponse;
import com.manulife.studentportal.dto.response.MarkResponse;

public interface StudentPortalService {

    Page<MarkResponse> getMyMarks(Pageable pageable);

    List<MarkResponse> getMarksByExam(Long examId);

    GradeSummaryResponse getGradeSummary();
}
