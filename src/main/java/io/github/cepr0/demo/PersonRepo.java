package io.github.cepr0.demo;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Optional;

public interface PersonRepo extends
		JpaRepository<Person, Long>,
		QuerydslPredicateExecutor<Person>,
		QuerydslBinderCustomizer<QPerson> {

	@SuppressWarnings("NullableProblems")
	@Override
	default void customize(QuerydslBindings bindings, QPerson model) {

		// Exclude id from filtering
		bindings.excluding(model.id);

		// Make all string case-insensitive
		bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);

		// Make a kind of 'between' filter for Person.age property
		// (see https://stackoverflow.com/a/35158320)
		bindings.bind(model.age).all((path, value) -> {
			Iterator<? extends Integer> it = value.iterator();
			Integer from = it.next();
			if (value.size() >= 2) {
				Integer to = it.next();
				return Optional.of(path.between(from, to));
			} else {
				return Optional.of(path.goe(from));
			}
		});

		bindings.bind(model.dob).all((path, value) -> {
			Iterator<? extends LocalDate> it = value.iterator();
			LocalDate from = it.next();
			if (value.size() >= 2) {
				LocalDate to = it.next();
				return Optional.of(path.between(from, to));
			} else {
				return Optional.of(path.goe(from));
			}
		});
	}
}
