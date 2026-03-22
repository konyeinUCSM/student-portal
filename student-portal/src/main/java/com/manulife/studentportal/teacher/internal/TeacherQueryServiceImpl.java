package com.manulife.studentportal.teacher.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.teacher.TeacherInfo;
import com.manulife.studentportal.teacher.TeacherQueryService;
import com.manulife.studentportal.teacher.TeacherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherQueryServiceImpl implements TeacherQueryService {

    private final TeacherRepository teacherRepository;

    @Override
    public Optional<TeacherInfo> findById(Long id) {
        return teacherRepository.findById(id)
                .map(teacher -> new TeacherInfo(
                        teacher.getId(),
                        teacher.getName(),
                        teacher.getClassIds(),
                        teacher.getSubjectIds()));
    }

    @Override
    public boolean isTeacherAssignedToClass(Long teacherId, Long classId) {
        return teacherRepository.findById(teacherId)
                .map(teacher -> teacher.getClassIds().contains(classId))
                .orElse(false);
    }
}
