package io.github.cepr0.demo;

import lombok.Data;
import org.hibernate.annotations.Formula;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Entity
@Table(name = "models")
public class Person {

	@Id
	@GeneratedValue
	private Long id;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@Column(nullable = false)
	private LocalDate dob;

	@Column(nullable = false, length = 32)
	private String name;

	@Formula("timestampdiff('year', dob, now())")
	private Integer age;

	public Person(String name, LocalDate dob) {
		this.name = name;
		this.dob = dob;
		this.age = Long.valueOf(ChronoUnit.YEARS.between(dob, LocalDate.now())).intValue();
	}
}
