package com.auca.school_management_system;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.auca.school_management_system.entity.Classroom;
import com.auca.school_management_system.entity.Exam;
import com.auca.school_management_system.entity.ExamStatus;
import com.auca.school_management_system.entity.Grade;
import com.auca.school_management_system.entity.Student;
import com.auca.school_management_system.entity.Subject;
import com.auca.school_management_system.entity.Teacher;
import com.auca.school_management_system.repository.ClassroomRepository;
import com.auca.school_management_system.repository.ExamRepository;
import com.auca.school_management_system.repository.GradeRepository;
import com.auca.school_management_system.repository.StudentRepository;
import com.auca.school_management_system.repository.SubjectRepository;
import com.auca.school_management_system.repository.TeacherRepository;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:school-management-system-api-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class SchoolManagementSystemApiTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClassroomRepository classroomRepository;

	@Autowired
	private ExamRepository examRepository;

	@Autowired
	private GradeRepository gradeRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private TeacherRepository teacherRepository;

	@Test
	void derivedAndJpqlQueryEndpointsReturnExpectedRecords() throws Exception {
		Classroom occupied = new Classroom();
		occupied.setRoomNumber(1);
		occupied.setName("Room 1");
		classroomRepository.save(occupied);

		Classroom free = new Classroom();
		free.setRoomNumber(4);
		free.setName("Room 4");
		classroomRepository.save(free);

		Subject mathematics = new Subject();
		mathematics.setName("Mathematics");
		subjectRepository.save(mathematics);

		Subject unused = new Subject();
		unused.setName("Kinyarwanda");
		subjectRepository.save(unused);

		Teacher teacher = new Teacher();
		teacher.setFirstName("Alice");
		teacher.setLastName("Teacher");
		teacher.setEmail("alice@example.com");
		teacher.setClassroom(occupied);
		teacher.getSubjects().add(mathematics);
		teacherRepository.save(teacher);

		Student student = new Student();
		student.setFirstName("Aline");
		student.setLastName("Iradukunda");
		student.setRegNumber("S4-2026-018");
		student.setGuardianPhone("+250700000001");
		studentRepository.save(student);

		Exam exam = new Exam();
		exam.setExamDate(LocalDate.now());
		exam.setStatus(ExamStatus.PLANNED);
		exam.setTitle("Mathematics");
		exam.setStudent(student);
		exam.setTeacher(teacher);
		examRepository.save(exam);

		mockMvc.perform(get("/api/students/by-last-name").param("lastName", "iradukunda"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].regNumber").value("S4-2026-018"));

		mockMvc.perform(get("/api/exams/by-status").param("status", "PLANNED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(get("/api/teachers/by-subject").param("name", "mathematics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(get("/api/classrooms/free"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].roomNumber").value(4));

		mockMvc.perform(get("/api/subjects/unused"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name").value("Kinyarwanda"));

		mockMvc.perform(get("/api/students/of-teacher/{teacherId}", teacher.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void safeSaveForcesPlannedAndRejectsPastDates() throws Exception {
		Student student = new Student();
		student.setFirstName("Rafiki");
		student.setLastName("Iradukunda");
		student.setRegNumber("S4-2026-019");
		student.setGuardianPhone("+250700000002");
		studentRepository.save(student);

		Classroom classroom = new Classroom();
		classroom.setRoomNumber(2);
		classroom.setName("Room 2");
		classroomRepository.save(classroom);

		Teacher teacher = new Teacher();
		teacher.setFirstName("Bob");
		teacher.setLastName("Teacher");
		teacher.setEmail("bob@example.com");
		teacher.setClassroom(classroom);
		teacherRepository.save(teacher);

		String futureRequest = """
				{
				  "examDate": "%s",
				  "status": "MARKED",
				  "title": "Safe save test",
				  "student": {"id": "%s"},
				  "teacher": {"id": "%s"}
				}
				""".formatted(LocalDate.now().plusDays(1), student.getId(), teacher.getId());

		mockMvc.perform(post("/api/exams/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(futureRequest))
				.andExpect(status().isCreated());

		org.assertj.core.api.Assertions.assertThat(examRepository.findAll())
				.singleElement()
				.extracting(Exam::getStatus)
				.isEqualTo(ExamStatus.PLANNED);

		String pastRequest = futureRequest.replace(
				LocalDate.now().plusDays(1).toString(),
				LocalDate.now().minusDays(1).toString());
		mockMvc.perform(post("/api/exams/save")
						.contentType(MediaType.APPLICATION_JSON)
						.content(pastRequest))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Exam date cannot be in the past"));
	}

	@Test
	void statusTeacherClassroomGradeReportsAndPageEndpointsWork() throws Exception {
		Classroom occupied = saveClassroom(1);
		Classroom secondRoom = saveClassroom(2);
		saveClassroom(4);
		Subject mathematics = saveSubject("Mathematics");
		Subject english = saveSubject("English");
		Teacher teacher = saveTeacher("Alice", "alice@example.com", occupied, mathematics);
		Teacher anotherTeacher = saveTeacher("Bob", "bob@example.com", secondRoom);
		Student student = saveStudent("S4-2026-020");

		LocalDate fixtureDate = LocalDate.of(2026, 10, 20);
		Exam marked = saveExam(student, teacher, fixtureDate, ExamStatus.MARKED);
		Exam planned = saveExam(student, teacher, fixtureDate, ExamStatus.PLANNED);
		Exam written = saveExam(student, teacher, fixtureDate, ExamStatus.WRITTEN);

		mockMvc.perform(put("/api/teachers/{teacherId}/subjects/{subjectId}",
						teacher.getId(), english.getId()))
				.andExpect(status().isOk())
				.andExpect(content().string("Subject added to the teacher"));
		mockMvc.perform(put("/api/teachers/{teacherId}/subjects/{subjectId}",
						teacher.getId(), english.getId()))
				.andExpect(status().isConflict());

		mockMvc.perform(patch("/api/teachers/{teacherId}/classroom", teacher.getId())
						.param("roomNumber", "4"))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/teachers/{teacherId}/classroom", anotherTeacher.getId())
						.param("roomNumber", "4"))
				.andExpect(status().isConflict());

		mockMvc.perform(post("/api/grades/save")
						.param("examId", marked.getId().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"subjectName":"Mathematics","comment":"Good work","marks":78}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().string("Grade saved successfully"));
		Grade savedGrade = gradeRepository.findAll().get(0);
		mockMvc.perform(get("/api/grades/student/{regNumber}", student.getRegNumber()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
		mockMvc.perform(delete("/api/exams/delete/{id}", marked.getId()))
				.andExpect(status().isConflict());

		mockMvc.perform(patch("/api/exams/{id}/status", planned.getId())
						.param("status", "WRITTEN"))
				.andExpect(status().isOk());
		mockMvc.perform(patch("/api/exams/{id}/status", planned.getId())
						.param("status", "PLANNED"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Cannot change status from WRITTEN to PLANNED"));

		mockMvc.perform(get("/api/exams/stats/by-status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
		mockMvc.perform(get("/api/exams/page").param("page", "0").param("size", "2")
						.param("sort", "examDate,desc"))
				.andExpect(status().isOk());

		mockMvc.perform(patch("/api/exams/cancel-day")
						.param("teacherId", teacher.getId().toString())
						.param("date", fixtureDate.toString()))
				.andExpect(status().isOk())
				.andExpect(content().string("2 exams cancelled"));
		org.assertj.core.api.Assertions.assertThat(examRepository.findById(marked.getId()))
				.get()
				.extracting(Exam::getStatus)
				.isEqualTo(ExamStatus.MARKED);
		org.assertj.core.api.Assertions.assertThat(examRepository.findById(written.getId()))
				.get()
				.extracting(Exam::getStatus)
				.isEqualTo(ExamStatus.VOIDED);

		mockMvc.perform(delete("/api/grades/delete/{id}", savedGrade.getId()))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/exams/delete/{id}", marked.getId()))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/grades/save")
						.param("examId", written.getId().toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"subjectName":"English","comment":"Not marked","marks":50}
								"""))
				.andExpect(status().isBadRequest());
	}

	private Classroom saveClassroom(int roomNumber) {
		Classroom classroom = new Classroom();
		classroom.setRoomNumber(roomNumber);
		classroom.setName("Room " + roomNumber);
		return classroomRepository.save(classroom);
	}

	private Subject saveSubject(String name) {
		Subject subject = new Subject();
		subject.setName(name);
		return subjectRepository.save(subject);
	}

	private Teacher saveTeacher(String name, String email, Classroom classroom, Subject... subjects) {
		Teacher teacher = new Teacher();
		teacher.setFirstName(name);
		teacher.setLastName("Teacher");
		teacher.setEmail(email);
		teacher.setClassroom(classroom);
		teacher.getSubjects().addAll(java.util.List.of(subjects));
		return teacherRepository.save(teacher);
	}

	private Student saveStudent(String regNumber) {
		Student student = new Student();
		student.setFirstName("Test");
		student.setLastName("Student");
		student.setRegNumber(regNumber);
		student.setGuardianPhone("+250700000003");
		return studentRepository.save(student);
	}

	private Exam saveExam(Student student, Teacher teacher, LocalDate date, ExamStatus status) {
		Exam exam = new Exam();
		exam.setStudent(student);
		exam.setTeacher(teacher);
		exam.setExamDate(date);
		exam.setStatus(status);
		exam.setTitle(status + " test");
		return examRepository.save(exam);
	}
}
