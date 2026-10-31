package com.auca.school_management_system.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auca.school_management_system.entity.Student;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findByLastNameIgnoreCaseOrderByFirstNameAsc(String lastName);

    @Query("select distinct e.student from Exam e where e.teacher.id = :teacherId")
    List<Student> findDistinctByExamTeacherId(@Param("teacherId") UUID teacherId);
}
