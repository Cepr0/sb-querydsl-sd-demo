package io.github.cepr0.demo;

import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;

import static io.github.cepr0.demo.PersonController.PersonDto.selfLinkOf;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/people")
public class PersonController {

	private final PersonRepo personRepo;

	@GetMapping
	public ResponseEntity getFiltered(
			@QuerydslPredicate(root = Person.class, bindings = PersonRepo.class) Predicate predicate,
			Pageable pageable,
			PagedResourcesAssembler<Person> assembler
	) {
		return ResponseEntity.ok(
				assembler.toResource(
						personRepo.findAll(predicate, pageable),
						PersonDto::new
				)
		);
	}

	@PostMapping
	public ResponseEntity create(@RequestBody @Valid PersonController.PersonRequest personRequest) {
		Person person = personRepo.save(personRequest.toPerson());
		return ResponseEntity
				.created(URI.create(selfLinkOf(person.getId()).getHref()))
				.body(new PersonDto(person));
	}

	@GetMapping("/{id}")
	public ResponseEntity get(@PathVariable("id") Long id) {
		return personRepo.findById(id)
				.map(PersonDto::new)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PatchMapping("/{id}")
	public ResponseEntity update(
			@PathVariable("id") Long id,
			@RequestBody @Valid PersonController.PersonRequest req
	) {
		return personRepo.findById(id)
				.map(person -> {
					person.setName(req.getName());
					person.setDob(req.getDob());
					return person;
				})
				.map(personRepo::save)
				.map(person -> ResponseEntity.ok(new PersonDto(person)))
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity delete(@PathVariable("id") Long id) {
		try {
			personRepo.deleteById(id);
			return ResponseEntity.noContent().build();
		} catch (EmptyResultDataAccessException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@Data
	static class PersonRequest {
		@NotBlank private String name;
		@NotNull private LocalDate dob;

		Person toPerson() {
			return new Person(name, dob);
		}
	}

	@Getter
	@EqualsAndHashCode(callSuper = false)
	@Relation(value = "person", collectionRelation = "people")
	static class PersonDto extends ResourceSupport {
		private final String name;
		private LocalDate dob;
		private final Integer age;

		PersonDto(Person m) {
			this.name = m.getName();
			this.dob = m.getDob();
			this.age = m.getAge();
			add(selfLinkOf(m.getId()));
		}

		static Link selfLinkOf(Long id) {
			return linkTo(methodOn(PersonController.class).get(id)).withSelfRel();
		}
	}
}
