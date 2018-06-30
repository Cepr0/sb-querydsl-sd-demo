package io.github.cepr0.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@EnableJpaRepositories(considerNestedRepositories = true)
@SpringBootApplication
public class Application {

	private final ModelRepo modelRepo;

	public Application(ModelRepo modelRepo) {
		this.modelRepo = modelRepo;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@EventListener
	public void onReady(ApplicationReadyEvent e) {
		modelRepo.saveAll(range(0, 10).mapToObj(Model::new).collect(toList()));
	}
}
