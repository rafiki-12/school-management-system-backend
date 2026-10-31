package com.auca.school_management_system.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.auca.school_management_system.entity.Classroom;
import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {

    Optional<Classroom> findByRoomNumber(int roomNumber);

    @Query("""
            select c from Classroom c
            where not exists (select t from Teacher t where t.classroom = c)
            order by c.roomNumber asc
            """)
    List<Classroom> findFreeClassrooms();
}
