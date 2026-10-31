package com.auca.school_management_system.repository;

import java.util.UUID;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auca.school_management_system.entity.Exam;
import com.auca.school_management_system.entity.ExamStatus;
import org.springframework.transaction.annotation.Transactional;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    List<Exam> findByStatusOrderByExamDateAsc(ExamStatus status);

    List<Exam> findByExamDateBetweenOrderByExamDateAsc(LocalDate start, LocalDate end);

    boolean existsByTeacherIdAndExamDateAndStatusNot(
            UUID teacherId, LocalDate examDate, ExamStatus status);

    long countByExamDate(LocalDate examDate);

    long countByStatus(ExamStatus status);

    @Query("select e.status, count(e) from Exam e group by e.status order by e.status")
    List<Object[]> countExamsByStatus();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update Exam e
            set e.status = :newStatus
            where e.teacher.id = :teacherId
              and e.examDate = :examDate
              and e.status <> :excludedStatus
            """)
    int cancelByTeacherAndDate(
            @Param("teacherId") UUID teacherId,
            @Param("examDate") LocalDate examDate,
            @Param("newStatus") ExamStatus newStatus,
            @Param("excludedStatus") ExamStatus excludedStatus);
}
