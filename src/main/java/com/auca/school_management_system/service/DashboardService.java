package com.auca.school_management_system.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.auca.school_management_system.entity.ExamStatus;
import com.auca.school_management_system.repository.ExamRepository;
import com.auca.school_management_system.repository.GradeRepository;
import com.auca.school_management_system.repository.StudentRepository;
import com.auca.school_management_system.repository.TeacherRepository;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomService classroomService;
    private final ExamRepository examRepository;
    private final GradeRepository gradeRepository;

    public DashboardService(
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            ClassroomService classroomService,
            ExamRepository examRepository,
            GradeRepository gradeRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.classroomService = classroomService;
        this.examRepository = examRepository;
        this.gradeRepository = gradeRepository;
    }

    public Map<String, Long> summary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("students", studentRepository.count());
        summary.put("teachers", teacherRepository.count());
        summary.put("freeClassrooms", (long) classroomService.findFreeClassrooms().size());
        summary.put("examsToday", examRepository.countByExamDate(LocalDate.now()));
        summary.put("planned", examRepository.countByStatus(ExamStatus.PLANNED));
        summary.put("grades", gradeRepository.count());
        return summary;
    }
}
