package com.auca.school_management_system.repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auca.school_management_system.entity.Classroom;
import com.auca.school_management_system.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    @Query("""
            select distinct t from Teacher t
            join t.subjects s
            where lower(s.name) = lower(:name)
            """)
    List<Teacher> findBySubjectNameIgnoreCase(@Param("name") String name);

    boolean existsByClassroom(Classroom classroom);
}
