package com.auca.school_management_system.service;

import com.auca.school_management_system.entity.Exam;
import com.auca.school_management_system.entity.ExamStatus;
import com.auca.school_management_system.repository.ExamRepository;
import com.auca.school_management_system.repository.GradeRepository;
import com.auca.school_management_system.repository.StudentRepository;
import com.auca.school_management_system.repository.TeacherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GradeRepository gradeRepository;

    public ExamService(
            ExamRepository examRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            GradeRepository gradeRepository) {
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional
    public String save(Exam exam) {
        if (exam.getExamDate() == null) {
            return "Exam date is required";
        }
        if (exam.getExamDate().isBefore(LocalDate.now())) {
            return "Exam date cannot be in the past";
        }
        if (exam.getStudent() == null || exam.getStudent().getId() == null
                || !studentRepository.existsById(exam.getStudent().getId())) {
            return "The student with that id does not exist";
        }
        if (exam.getTeacher() == null || exam.getTeacher().getId() == null
                || !teacherRepository.existsById(exam.getTeacher().getId())) {
            return "The teacher with that id does not exist";
        }
        if (examRepository.existsByTeacherIdAndExamDateAndStatusNot(
                exam.getTeacher().getId(), exam.getExamDate(), ExamStatus.VOIDED)) {
            return "Teacher already supervises an exam on that date";
        }
        exam.setStudent(studentRepository.findById(exam.getStudent().getId()).orElseThrow());
        exam.setTeacher(teacherRepository.findById(exam.getTeacher().getId()).orElseThrow());
        exam.setStatus(ExamStatus.PLANNED);
        examRepository.save(exam);
        return "Exam saved successfully";
    }

    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    public Optional<Exam> findById(UUID id) {
        return examRepository.findById(id);
    }

    @Transactional
    public Optional<Exam> update(UUID id, Exam updatedExam) {
        Optional<Exam> existingExam = examRepository.findById(id);

        if (existingExam.isPresent()) {
            Exam exam = existingExam.get();
            exam.setExamDate(updatedExam.getExamDate());
            exam.setTitle(updatedExam.getTitle());
            if (updatedExam.getStudent() != null && updatedExam.getStudent().getId() != null) {
                studentRepository.findById(updatedExam.getStudent().getId()).ifPresent(exam::setStudent);
            }
            if (updatedExam.getTeacher() != null && updatedExam.getTeacher().getId() != null) {
                teacherRepository.findById(updatedExam.getTeacher().getId()).ifPresent(exam::setTeacher);
            }

            return Optional.of(examRepository.save(exam));
        }

        return Optional.empty();
    }

    @Transactional
    public String delete(UUID id) {
        Optional<Exam> exam = examRepository.findById(id);
        if (exam.isEmpty()) {
            return "The exam with that id does not exist";
        }
        if (gradeRepository.existsByExamId(id)) {
            return "That exam has grades, delete them first";
        }
        examRepository.delete(exam.get());
        return "Exam deleted successfully";
    }

    public List<Exam> findByStatus(ExamStatus status) {
        return examRepository.findByStatusOrderByExamDateAsc(status);
    }

    public List<Exam> findBetween(LocalDate start, LocalDate end) {
        return examRepository.findByExamDateBetweenOrderByExamDateAsc(start, end);
    }

    @Transactional
    public String updateStatus(UUID id, ExamStatus newStatus) {
        Optional<Exam> result = examRepository.findById(id);
        if (result.isEmpty()) {
            return "The exam with that id does not exist";
        }
        Exam exam = result.get();
        ExamStatus currentStatus = exam.getStatus();
        boolean allowed = currentStatus == ExamStatus.PLANNED
                && (newStatus == ExamStatus.WRITTEN || newStatus == ExamStatus.VOIDED)
                || currentStatus == ExamStatus.WRITTEN
                && (newStatus == ExamStatus.MARKED || newStatus == ExamStatus.VOIDED);
        if (!allowed) {
            return "Cannot change status from " + currentStatus + " to " + newStatus;
        }
        exam.setStatus(newStatus);
        examRepository.save(exam);
        return "Exam status updated successfully";
    }

    public List<Object[]> countByStatus() {
        return examRepository.countExamsByStatus();
    }

    @Transactional
    public String cancelDay(UUID teacherId, LocalDate date) {
        int cancelled = examRepository.cancelByTeacherAndDate(
                teacherId, date, ExamStatus.VOIDED, ExamStatus.MARKED);
        return cancelled + " exams cancelled";
    }

    public Page<Exam> findPage(Pageable pageable) {
        return examRepository.findAll(pageable);
    }
}
