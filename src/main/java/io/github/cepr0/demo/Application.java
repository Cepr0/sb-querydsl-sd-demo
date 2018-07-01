package io.github.cepr0.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@RequiredArgsConstructor
@SpringBootApplication
public class Application {

	private final PersonRepo personRepo;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@EventListener
	public void onReady(ApplicationReadyEvent e) {
		personRepo.saveAll(
				range(0, 10)
				.mapToObj(i -> new Person("Person" + i, LocalDate.of(1970 + 5 * i, i + 1, i + 1)))
				.collect(toList())
		);
	}
}
