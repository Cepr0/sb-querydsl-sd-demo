package io.github.cepr0.demo;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
public interface ModelRepo extends
		JpaRepository<Model, UUID>,
		QuerydslPredicateExecutor<Model>,
		QuerydslBinderCustomizer<QModel> {

	@Override
	default void customize(QuerydslBindings bindings, QModel model) {

		// Exclude id from filtering
		bindings.excluding(model.id);

		// Make all string case-insensitive
		bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);

		// Make a kind of 'between' filter (see https://stackoverflow.com/a/35158320)
		bindings.bind(model.number).all((path, value) -> {
			Iterator<? extends Integer> it = value.iterator();
			Integer from = it.next();
			if (value.size() >= 2) {
				Integer to = it.next();
				return Optional.of(path.between(from, to));
			} else {
				return Optional.of(path.goe(from));
			}
		});
	}
}
