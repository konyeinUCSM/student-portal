package com.manulife.studentportal.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manulife.studentportal.dto.request.CreateExamRequest;
import com.manulife.studentportal.dto.request.UpdateExamRequest;
import com.manulife.studentportal.dto.response.ExamResponse;
import com.manulife.studentportal.entity.Exam;
import com.manulife.studentportal.entity.SchoolClass;
import com.manulife.studentportal.entity.Student;
import com.manulife.studentportal.entity.Subject;
import com.manulife.studentportal.entity.Teacher;
import com.manulife.studentportal.exception.DuplicateResourceException;
import com.manulife.studentportal.exception.ResourceNotFoundException;
import com.manulife.studentportal.mapper.ExamMapper;
import com.manulife.studentportal.repository.ExamRepository;
import com.manulife.studentportal.repository.SchoolClassRepository;
import com.manulife.studentportal.repository.StudentRepository;
import com.manulife.studentportal.repository.SubjectRepository;
import com.manulife.studentportal.repository.TeacherRepository;
import com.manulife.studentportal.security.SecurityService;
import com.manulife.studentportal.service.ExamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ExamMapper examMapper;
    private final SecurityService securityService;

    @Override
    public ExamResponse create(CreateExamRequest request) {
        log.info("Creating exam with name: {}", request.getName());

        String role = securityService.getCurrentRole();

        // Validate class exists
        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));

        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        // If TEACHER: validate teacher is assigned to BOTH the class AND the subject
        if (securityService.isTeacher()) {
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            boolean assignedToClass = teacher.getClasses().stream()
                    .anyMatch(c -> c.getId().equals(request.getClassId()));
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(request.getSubjectId()));

            if (!assignedToClass || !assignedToSubject) {
                throw new AccessDeniedException("You can only create exams for classes and subjects you are assigned to");
            }
        }

        // Check unique constraint (name + classId + subjectId)
        if (examRepository.existsByNameAndSchoolClassIdAndSubjectId(
                request.getName(), request.getClassId(), request.getSubjectId())) {
            throw new DuplicateResourceException(
                    "Exam with name '" + request.getName() + "' already exists for this class and subject");
        }

        // Create exam
        Exam exam = Exam.builder()
                .name(request.getName())
                .examDate(request.getExamDate())
                .fullMarks(request.getFullMarks())
                .passMarks(request.getPassMarks())
                .schoolClass(schoolClass)
                .subject(subject)
                .build();

        exam = examRepository.save(exam);
        log.info("Exam created successfully with id: {}", exam.getId());

        return examMapper.toResponse(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponse getById(Long id) {
        log.debug("Fetching exam with id: {}", id);

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        if (securityService.isAdmin()) {
            // ADMIN sees all
            return examMapper.toResponse(exam);
        } else if (securityService.isTeacher()) {
            // TEACHER must be assigned to exam's class
            Long teacherId = securityService.getCurrentProfileId();
            if (!teacherRepository.existsByIdAndClasses_Id(teacherId, exam.getSchoolClass().getId())) {
                throw new AccessDeniedException("You can only view exams for classes you are assigned to");
            }
            return examMapper.toResponse(exam);
        } else if (securityService.isStudent()) {
            // STUDENT must be enrolled in exam's class
            Long studentId = securityService.getCurrentProfileId();
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

            if (!student.getSchoolClass().getId().equals(exam.getSchoolClass().getId())) {
                throw new AccessDeniedException("You can only view exams for your class");
            }
            return examMapper.toResponse(exam);
        }

        throw new AccessDeniedException("Access denied");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponse> getAll(Pageable pageable) {
        log.debug("Fetching all exams with pagination: {}", pageable);

        if (securityService.isAdmin()) {
            // ADMIN gets all exams
            Page<Exam> exams = examRepository.findAll(pageable);
            return exams.map(examMapper::toResponse);
        } else if (securityService.isTeacher()) {
            // TEACHER gets exams for assigned classes
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            List<Long> assignedClassIds = teacher.getClasses().stream()
                    .map(SchoolClass::getId)
                    .collect(Collectors.toList());

            if (assignedClassIds.isEmpty()) {
                return Page.empty(pageable);
            }

            Page<Exam> exams = examRepository.findBySchoolClassIdIn(assignedClassIds, pageable);
            return exams.map(examMapper::toResponse);
        } else if (securityService.isStudent()) {
            // STUDENT gets exams for their class
            Long studentId = securityService.getCurrentProfileId();
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

            Page<Exam> exams = examRepository.findBySchoolClassId(student.getSchoolClass().getId(), pageable);
            return exams.map(examMapper::toResponse);
        }

        return Page.empty(pageable);
    }

    @Override
    public ExamResponse update(Long id, UpdateExamRequest request) {
        log.info("Updating exam with id: {}", id);

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        String role = securityService.getCurrentRole();

        // ADMIN can update any. TEACHER can only update exams they could create (assigned class + subject)
        if ("TEACHER".equals(role)) {
            Long teacherId = securityService.getCurrentProfileId();
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + teacherId));

            Long examClassId = exam.getSchoolClass().getId();
            Long examSubjectId = exam.getSubject().getId();

            boolean assignedToClass = teacher.getClasses().stream()
                    .anyMatch(c -> c.getId().equals(examClassId));
            boolean assignedToSubject = teacher.getSubjects().stream()
                    .anyMatch(s -> s.getId().equals(examSubjectId));

            if (!assignedToClass || !assignedToSubject) {
                throw new AccessDeniedException("You can only update exams for classes and subjects you are assigned to");
            }
        }

        // Update only non-null fields
        if (request.getName() != null) {
            // Check unique constraint if name is being changed
            if (!exam.getName().equals(request.getName()) &&
                examRepository.existsByNameAndSchoolClassIdAndSubjectId(
                        request.getName(), exam.getSchoolClass().getId(), exam.getSubject().getId())) {
                throw new DuplicateResourceException(
                        "Exam with name '" + request.getName() + "' already exists for this class and subject");
            }
            exam.setName(request.getName());
        }
        if (request.getExamDate() != null) {
            exam.setExamDate(request.getExamDate());
        }
        if (request.getFullMarks() != null) {
            exam.setFullMarks(request.getFullMarks());
        }
        if (request.getPassMarks() != null) {
            exam.setPassMarks(request.getPassMarks());
        }

        exam = examRepository.save(exam);
        log.info("Exam updated successfully with id: {}", id);

        return examMapper.toResponse(exam);
    }

    @Override
    public void delete(Long id) {
        log.info("Soft deleting exam with id: {}", id);

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        // Soft delete
        exam.setDeleted(true);

        log.info("Exam soft deleted successfully with id: {}", id);
    }
}
