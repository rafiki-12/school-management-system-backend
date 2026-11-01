package com.auca.school_management_system.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import com.auca.school_management_system.entity.Exam;
import com.auca.school_management_system.entity.ExamStatus;
import com.auca.school_management_system.service.ExamService;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Exam exam) {
        String message = examService.save(exam);
        if (message.equals("Exam saved successfully")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } else if (message.equals("Teacher already supervises an exam on that date")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else if (message.equals("The student with that id does not exist")
                || message.equals("The teacher with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
    }

    @GetMapping("/all")
    public List<Exam> findAll() {
        return examService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        var result = examService.findById(id);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The exam with that id does not exist");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Exam exam) {
        var result = examService.update(id, exam);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The exam with that id does not exist");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = examService.delete(id);
        if (message.equals("The exam with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("That exam has grades, delete them first")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.ok(message);
        }
    }

    @GetMapping("/by-status")
    public List<Exam> findByStatus(@RequestParam ExamStatus status) {
        return examService.findByStatus(status);
    }

    @GetMapping("/between")
    public List<Exam> findBetween(@RequestParam String start, @RequestParam String end) {
        return examService.findBetween(LocalDate.parse(start), LocalDate.parse(end));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable UUID id,
            @RequestParam ExamStatus status) {
        String message = examService.updateStatus(id, status);
        if (message.equals("The exam with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("Exam status updated successfully")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
    }

    @GetMapping("/stats/by-status")
    public List<Object[]> statsByStatus() {
        return examService.countByStatus();
    }

    @PatchMapping("/cancel-day")
    public String cancelDay(@RequestParam UUID teacherId, @RequestParam LocalDate date) {
        return examService.cancelDay(teacherId, date);
    }

    @GetMapping("/page")
    public Page<Exam> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "examDate,desc") String sort) {
        String[] parts = sort.split(",", 2);
        Sort.Direction direction = parts.length == 1
                ? Sort.Direction.ASC
                : Sort.Direction.fromString(parts[1]);
        return examService.findPage(PageRequest.of(page, size, Sort.by(direction, parts[0])));
    }
}
