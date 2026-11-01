package com.auca.school_management_system.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.auca.school_management_system.entity.Student;
import com.auca.school_management_system.repository.StudentRepository;
import com.auca.school_management_system.repository.TeacherRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public StudentService(StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public String save(Student student) {
        studentRepository.save(student);
        return "Student saved successfully";
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(UUID id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> update(UUID id, Student updatedStudent) {
        return studentRepository.findById(id).map(student -> {
            student.setFirstName(updatedStudent.getFirstName());
            student.setLastName(updatedStudent.getLastName());
            student.setRegNumber(updatedStudent.getRegNumber());
            student.setGuardianPhone(updatedStudent.getGuardianPhone());
            return studentRepository.save(student);
        });
    }

    public String delete(UUID id) {
        if (!studentRepository.existsById(id)) {
            return "The student with that id does not exist";
        }

        studentRepository.deleteById(id);
        return "Student deleted successfully";
    }

    public List<Student> findByLastName(String lastName) {
        return studentRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public Optional<List<Student>> findByTeacher(UUID teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            return Optional.empty();
        }
        return Optional.of(studentRepository.findDistinctByExamTeacherId(teacherId));
    }
}
