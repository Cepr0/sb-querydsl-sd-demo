package io.github.cepr0.demo;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

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
			Pageable pageable
	) {
		return ResponseEntity.ok(modelRepo.findAll(predicate, pageable));
	}

	@Transactional
	@PostMapping
	public ResponseEntity create(@RequestBody @Valid Model model) {
		modelRepo.save(model);
		return ResponseEntity
				.created(linkTo(methodOn(ModelController.class).get(model.getId())).toUri())
				.body(model);
	}

	@GetMapping("/{id}")
	public ResponseEntity get(@PathVariable("id") UUID id) {
		return modelRepo.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
