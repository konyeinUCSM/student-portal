package com.manulife.studentportal.academic.internal;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.academic.AcademicQueryService;
import com.manulife.studentportal.academic.ExamRepository;
import com.manulife.studentportal.academic.MarkRepository;
import com.manulife.studentportal.academic.SchoolClassInfo;
import com.manulife.studentportal.academic.SchoolClassRepository;
import com.manulife.studentportal.academic.SubjectInfo;
import com.manulife.studentportal.academic.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicQueryServiceImpl implements AcademicQueryService {

    private final MarkRepository markRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;

    @Override
    public boolean existsMarkForStudent(Long studentId) {
        return markRepository.existsByStudentId(studentId);
    }

    @Override
    public long countClasses() {
        return schoolClassRepository.count();
    }

    @Override
    public long countSubjects() {
        return subjectRepository.count();
    }

    @Override
    public long countExams() {
        return examRepository.count();
    }

    @Override
    public Optional<SchoolClassInfo> findClassById(Long id) {
        return schoolClassRepository.findById(id)
                .map(schoolClass -> new SchoolClassInfo(schoolClass.getId(), schoolClass.getName()));
    }

    @Override
    public Optional<SubjectInfo> findSubjectById(Long id) {
        return subjectRepository.findById(id)
                .map(subject -> new SubjectInfo(subject.getId(), subject.getName()));
    }

    @Override
    public List<SchoolClassInfo> findClassesByIds(Set<Long> classIds) {
        return schoolClassRepository.findAllById(classIds).stream()
                .map(schoolClass -> new SchoolClassInfo(schoolClass.getId(), schoolClass.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectInfo> findSubjectsByIds(Set<Long> subjectIds) {
        return subjectRepository.findAllById(subjectIds).stream()
                .map(subject -> new SubjectInfo(subject.getId(), subject.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsClassById(Long classId) {
        return schoolClassRepository.existsById(classId);
    }

    @Override
    public boolean existsSubjectById(Long subjectId) {
        return subjectRepository.existsById(subjectId);
    }
}
