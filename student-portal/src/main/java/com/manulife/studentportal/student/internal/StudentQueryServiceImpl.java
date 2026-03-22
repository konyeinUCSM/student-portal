package com.manulife.studentportal.student.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.student.StudentInfo;
import com.manulife.studentportal.student.StudentQueryService;
import com.manulife.studentportal.student.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryServiceImpl implements StudentQueryService {

    private final StudentRepository studentRepository;

    @Override
    public Optional<StudentInfo> findById(Long id) {
        return studentRepository.findById(id)
                .map(student -> new StudentInfo(student.getId(), student.getName(), student.getClassId()));
    }

    @Override
    public boolean existsByClassId(Long classId) {
        return studentRepository.existsByClassId(classId);
    }
}
