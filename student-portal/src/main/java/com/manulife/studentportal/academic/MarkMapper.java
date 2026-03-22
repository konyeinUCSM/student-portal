package com.manulife.studentportal.academic;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.manulife.studentportal.academic.internal.Mark;
import com.manulife.studentportal.academic.web.MarkResponse;
import com.manulife.studentportal.student.StudentQueryService;

@Mapper(componentModel = "spring")
public abstract class MarkMapper {

    @Autowired
    protected StudentQueryService studentQueryService;

    @Mapping(source = "studentId", target = "studentId")
    @Mapping(target = "studentName", expression = "java(getStudentName(mark.getStudentId()))")
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    @Mapping(source = "exam.subject.name", target = "subjectName")
    @Mapping(source = "exam.schoolClass.name", target = "className")
    @Mapping(source = "exam.fullMarks", target = "fullMarks")
    @Mapping(source = "mark", target = "percentage", qualifiedByName = "calculatePercentage")
    public abstract MarkResponse toResponse(Mark mark);

    public abstract List<MarkResponse> toResponseList(List<Mark> marks);

    protected String getStudentName(Long studentId) {
        if (studentId == null) {
            return null;
        }
        try {
            var studentInfo = studentQueryService.findById(studentId);
            return studentInfo.map(info -> info.name()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Named("calculatePercentage")
    protected Double calculatePercentage(Mark mark) {
        if (mark == null || mark.getExam() == null || mark.getExam().getFullMarks() == null
                || mark.getExam().getFullMarks() == 0) {
            return 0.0;
        }
        return (mark.getScore() / mark.getExam().getFullMarks()) * 100.0;
    }
}
