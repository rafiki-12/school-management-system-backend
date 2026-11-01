package com.auca.school_management_system.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.auca.school_management_system.entity.Subject;
import com.auca.school_management_system.repository.SubjectRepository;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public String save(Subject subject) {
        subjectRepository.save(subject);
        return "Subject saved successfully";
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Optional<Subject> findById(UUID id) {
        return subjectRepository.findById(id);
    }

    public Optional<Subject> update(UUID id, Subject updatedSubject) {
        return subjectRepository.findById(id).map(subject -> {
            subject.setName(updatedSubject.getName());
            return subjectRepository.save(subject);
        });
    }

    public String delete(UUID id) {
        if (!subjectRepository.existsById(id)) {
            return "The subject with that id does not exist";
        }
        subjectRepository.deleteById(id);
        return "Subject deleted successfully";
    }

    public List<Subject> findUnused() {
        return subjectRepository.findUnusedSubjects();
    }
}
