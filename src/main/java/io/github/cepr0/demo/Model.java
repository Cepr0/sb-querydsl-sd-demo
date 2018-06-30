package io.github.cepr0.demo;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Entity
@Table(name = "models")
public class Model {
	@Id
	@GeneratedValue
	private UUID id;
	@NotNull private Integer number;
	@NotBlank private String name;

	Model(Integer number) {
		this.number = number;
		this.name = "Model" + number;
	}

	@PrePersist
	private void prePersist() {
		id = UUID.randomUUID();
	}
}
