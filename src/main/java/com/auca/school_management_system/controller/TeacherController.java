package com.auca.school_management_system.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auca.school_management_system.entity.Teacher;
import com.auca.school_management_system.service.TeacherService;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Teacher teacher) {
        String message = teacherService.save(teacher);
        if (message.equals("Teacher saved successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @GetMapping("/all")
    public List<Teacher> findAll() {
        return teacherService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        var result = teacherService.findById(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The teacher with that id does not exist");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Teacher teacher) {
        var result = teacherService.update(id, teacher);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The teacher with that id does not exist");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = teacherService.delete(id);
        if (message.equals("The teacher with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @GetMapping("/by-subject")
    public List<Teacher> findBySubject(@RequestParam String name) {
        return teacherService.findBySubject(name);
    }

    @PutMapping("/{teacherId}/subjects/{subjectId}")
    public ResponseEntity<String> addSubject(
            @PathVariable UUID teacherId,
            @PathVariable UUID subjectId) {
        String message = teacherService.addSubject(teacherId, subjectId);
        if (message.equals("The teacher with that id does not exist")
                || message.equals("That subject does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("The teacher already has that subject")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else if (message.equals("Subject added to the teacher")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
    }

    @PatchMapping("/{teacherId}/classroom")
    public ResponseEntity<String> changeClassroom(
            @PathVariable UUID teacherId,
            @RequestParam int roomNumber) {
        String message = teacherService.changeClassroom(teacherId, roomNumber);
        if (message.equals("The teacher with that id does not exist")
                || message.equals("The classroom with that roomNumber does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("That classroom is taken by another teacher")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else if (message.equals("Classroom changed successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
    }
}
