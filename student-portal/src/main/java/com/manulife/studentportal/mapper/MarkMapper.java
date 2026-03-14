package com.manulife.studentportal.mapper;

import com.manulife.studentportal.dto.response.MarkResponse;
import com.manulife.studentportal.entity.Mark;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarkMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    @Mapping(source = "exam.subject.name", target = "subjectName")
    @Mapping(source = "exam.schoolClass.name", target = "className")
    @Mapping(source = "exam.fullMarks", target = "fullMarks")
    @Mapping(source = "mark", target = "percentage", qualifiedByName = "calculatePercentage")
    MarkResponse toResponse(Mark mark);

    List<MarkResponse> toResponseList(List<Mark> marks);

    @Named("calculatePercentage")
    default Double calculatePercentage(Mark mark) {
        if (mark == null || mark.getExam() == null || mark.getExam().getFullMarks() == null || mark.getExam().getFullMarks() == 0) {
            return 0.0;
        }
        return (mark.getScore() / mark.getExam().getFullMarks()) * 100.0;
    }
}
