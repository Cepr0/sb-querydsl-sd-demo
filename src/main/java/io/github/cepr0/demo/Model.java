package io.github.cepr0.demo;

import lombok.Data;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "models")
public class Model {
	@Id @GeneratedValue private UUID id;
	private Integer number;
	private String name;

	Model(Integer number) {
		this.number = number;
		this.name = "Model" + number;
	}

	public Model(Integer number, String name) {
		this.number = number;
		this.name = name;
	}

	@PrePersist
	private void prePersist() {
		id = UUID.randomUUID();
	}
}
