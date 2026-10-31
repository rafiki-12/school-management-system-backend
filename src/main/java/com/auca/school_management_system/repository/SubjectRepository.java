package com.auca.school_management_system.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.auca.school_management_system.entity.Subject;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    @Query("select s from Subject s where s.teachers is empty")
    List<Subject> findUnusedSubjects();
}
