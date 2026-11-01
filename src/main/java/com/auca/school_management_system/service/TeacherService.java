package com.auca.school_management_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auca.school_management_system.entity.Classroom;
import com.auca.school_management_system.entity.Subject;
import com.auca.school_management_system.entity.Teacher;
import com.auca.school_management_system.repository.ClassroomRepository;
import com.auca.school_management_system.repository.SubjectRepository;
import com.auca.school_management_system.repository.TeacherRepository;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;

    public TeacherService(
            TeacherRepository teacherRepository,
            ClassroomRepository classroomRepository,
            SubjectRepository subjectRepository) {
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public String save(Teacher teacher) {
        String relationError = resolveRelations(teacher);
        if (relationError != null) {
            return relationError;
        }
        teacherRepository.save(teacher);
        return "Teacher saved successfully";
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    public Optional<Teacher> findById(UUID id) {
        return teacherRepository.findById(id);
    }

    @Transactional
    public Optional<Teacher> update(UUID id, Teacher updatedTeacher) {
        return teacherRepository.findById(id).flatMap(teacher -> {
            String relationError = resolveRelations(updatedTeacher);
            if (relationError != null) {
                return Optional.empty();
            }
            teacher.setFirstName(updatedTeacher.getFirstName());
            teacher.setLastName(updatedTeacher.getLastName());
            teacher.setEmail(updatedTeacher.getEmail());
            teacher.setClassroom(updatedTeacher.getClassroom());
            teacher.setSubjects(updatedTeacher.getSubjects());
            return Optional.of(teacherRepository.save(teacher));
        });
    }

    @Transactional
    public String delete(UUID id) {
        if (!teacherRepository.existsById(id)) {
            return "The teacher with that id does not exist";
        }
        teacherRepository.deleteById(id);
        return "Teacher deleted successfully";
    }

    public List<Teacher> findBySubject(String name) {
        return teacherRepository.findBySubjectNameIgnoreCase(name);
    }

    @Transactional
    public String addSubject(UUID teacherId, UUID subjectId) {
        Optional<Teacher> teacherResult = teacherRepository.findById(teacherId);
        if (teacherResult.isEmpty()) {
            return "The teacher with that id does not exist";
        }
        Optional<Subject> subjectResult = subjectRepository.findById(subjectId);
        if (subjectResult.isEmpty()) {
            return "That subject does not exist";
        }

        Teacher teacher = teacherResult.get();
        Subject subject = subjectResult.get();
        if (teacher.getSubjects().contains(subject)) {
            return "The teacher already has that subject";
        }
        teacher.getSubjects().add(subject);
        teacherRepository.save(teacher);
        return "Subject added to the teacher";
    }

    @Transactional
    public String changeClassroom(UUID teacherId, int roomNumber) {
        Optional<Teacher> teacherResult = teacherRepository.findById(teacherId);
        if (teacherResult.isEmpty()) {
            return "The teacher with that id does not exist";
        }

        Optional<Classroom> classroomResult = classroomRepository.findByRoomNumber(roomNumber);
        if (classroomResult.isEmpty()) {
            return "The classroom with that roomNumber does not exist";
        }
        Classroom classroom = classroomResult.get();

        Teacher teacher = teacherResult.get();
        if (teacher.getClassroom() != null
                && teacher.getClassroom().getId().equals(classroom.getId())) {
            return "Classroom changed successfully";
        }
        if (teacherRepository.existsByClassroom(classroom)) {
            return "That classroom is taken by another teacher";
        }

        teacher.setClassroom(classroom);
        teacherRepository.save(teacher);
        return "Classroom changed successfully";
    }

    private String resolveRelations(Teacher teacher) {
        if (teacher.getClassroom() != null) {
            if (teacher.getClassroom().getId() == null) {
                return "The classroom with that id does not exist";
            }
            Optional<Classroom> classroom = classroomRepository.findById(teacher.getClassroom().getId());
            if (classroom.isEmpty()) {
                return "The classroom with that id does not exist";
            }
            teacher.setClassroom(classroom.get());
        }

        List<Subject> subjects = new ArrayList<>();
        if (teacher.getSubjects() != null) {
            for (Subject requestedSubject : teacher.getSubjects()) {
                if (requestedSubject.getId() == null) {
                    return "That subject does not exist";
                }
                Optional<Subject> subject = subjectRepository.findById(requestedSubject.getId());
                if (subject.isEmpty()) {
                    return "That subject does not exist";
                }
                subjects.add(subject.get());
            }
        }
        teacher.setSubjects(subjects);
        return null;
    }
}
