package io.github.cepr0.demo;

import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;

import static io.github.cepr0.demo.ModelController.ModelDto.selfLinkOf;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/models")
public class ModelController {

	private final ModelRepo modelRepo;

	public ModelController(ModelRepo modelRepo) {
		this.modelRepo = modelRepo;
	}

	@GetMapping
	public ResponseEntity getFiltered(
			@QuerydslPredicate(root = Model.class, bindings = ModelRepo.class) Predicate predicate,
			Pageable pageable,
			PagedResourcesAssembler<Model> assembler
	) {
		return ResponseEntity.ok(assembler.toResource(modelRepo.findAll(predicate, pageable), ModelDto::new));
	}

	@Transactional
	@PostMapping
	public ResponseEntity create(@RequestBody @Valid ModelRequest modelRequest) {
		Model model = modelRepo.save(modelRequest.toModel());
		return ResponseEntity
				.created(URI.create(selfLinkOf(model.getId()).getHref()))
				.body(new ModelDto(model));
	}

	@GetMapping("/{id}")
	public ResponseEntity get(@PathVariable("id") UUID id) {
		return modelRepo.findById(id)
				.map(ModelDto::new)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@Data
	static class ModelRequest {
		@NotNull private Integer number;
		@NotBlank private String name;

		Model toModel() {
			return new Model(number, name);
		}
	}

	@Getter
	@EqualsAndHashCode(callSuper = false)
	@Relation(value = "model", collectionRelation = "models")
	static class ModelDto extends ResourceSupport {
		private final Integer number;
		private final String name;

		ModelDto(Model m) {
			this.number = m.getNumber();
			this.name = m.getName();
			add(selfLinkOf(m.getId()));
		}

		static Link selfLinkOf(UUID id) {
			return linkTo(methodOn(ModelController.class).get(id)).withSelfRel();
		}
	}
}
