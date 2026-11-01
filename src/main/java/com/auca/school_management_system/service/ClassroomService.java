package com.auca.school_management_system.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.auca.school_management_system.entity.Classroom;
import com.auca.school_management_system.repository.ClassroomRepository;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ClassroomService(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public String save(Classroom classroom) {
        classroomRepository.save(classroom);
        return "Classroom saved successfully";
    }

    public List<Classroom> findAll() {
        return classroomRepository.findAll();
    }

    public Optional<Classroom> findById(UUID id) {
        return classroomRepository.findById(id);
    }

    public Optional<Classroom> update(UUID id, Classroom updatedClassroom) {
        return classroomRepository.findById(id).map(classroom -> {
            classroom.setRoomNumber(updatedClassroom.getRoomNumber());
            classroom.setName(updatedClassroom.getName());
            return classroomRepository.save(classroom);
        });
    }

    public String delete(UUID id) {
        if (!classroomRepository.existsById(id)) {
            return "The classroom with that id does not exist";
        }
        classroomRepository.deleteById(id);
        return "Classroom deleted successfully";
    }

    public Optional<Classroom> findByRoomNumber(int roomNumber) {
        return classroomRepository.findByRoomNumber(roomNumber);
    }

    public List<Classroom> findFreeClassrooms() {
        return classroomRepository.findFreeClassrooms();
    }
}
