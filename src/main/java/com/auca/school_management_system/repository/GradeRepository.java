package com.auca.school_management_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auca.school_management_system.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

    @Query("""
            select g from Grade g
            where g.exam.student.regNumber = :regNumber
            order by g.gradedDate desc
            """)
    List<Grade> findByStudentRegNumber(@Param("regNumber") String regNumber);

    boolean existsByExamId(UUID examId);
}
