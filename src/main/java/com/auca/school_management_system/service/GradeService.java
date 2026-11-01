package com.auca.school_management_system.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auca.school_management_system.entity.Exam;
import com.auca.school_management_system.entity.ExamStatus;
import com.auca.school_management_system.entity.Grade;
import com.auca.school_management_system.repository.ExamRepository;
import com.auca.school_management_system.repository.GradeRepository;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final ExamRepository examRepository;

    public GradeService(GradeRepository gradeRepository, ExamRepository examRepository) {
        this.gradeRepository = gradeRepository;
        this.examRepository = examRepository;
    }

    @Transactional
    public String save(UUID examId, Grade grade) {
        Optional<Exam> examResult = examRepository.findById(examId);
        if (examResult.isEmpty()) {
            return "The exam with that id does not exist";
        }
        Exam exam = examResult.get();
        if (exam.getStatus() != ExamStatus.MARKED) {
            return "Grades are only for MARKED exams";
        }
        grade.setExam(exam);
        grade.setGradedDate(LocalDate.now());
        gradeRepository.save(grade);
        return "Grade saved successfully";
    }

    public List<Grade> findByStudent(String regNumber) {
        return gradeRepository.findByStudentRegNumber(regNumber);
    }

    @Transactional
    public String delete(UUID id) {
        if (!gradeRepository.existsById(id)) {
            return "The grade with that id does not exist";
        }
        gradeRepository.deleteById(id);
        return "Grade deleted successfully";
    }
}
