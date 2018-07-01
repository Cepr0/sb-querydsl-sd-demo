### Example of making a 'REST query language' with Querydsl and Spring Data

With [Querydsl](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#core.extensions.querydsl) and [Web support](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#core.web) 
Spring Data extensions we can easy implement a 'REST query language' to filter the data of the specific entity.

To do this we can just extend our repo from `QuerydslPredicateExecutor`, add `Predicate` with annotation `@QuerydslPredicate`
as argument to our REST controller method and use it in `findAll` method of the repository.     

Then we can request our entity data with base filtering, paging and sorting support, like following for example:

    GET /people?name=John&size=10&page=2,sort=name,desc

where `name` is a property of our entity `Person`, `size` is a page size, `page` is a number of current page
and `sort` is a parameter that tells to sort data by `name` in descending order.

#### Repository

```java
public interface PersonRepo extends JpaRepository<Person, Long>, QuerydslPredicateExecutor<Person> {
} 
```

#### Controller

```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/people")
public class PersonController {

    private final PersonRepo personRepo;

    @GetMapping
    public ResponseEntity getFiltered(
            @QuerydslPredicate(root = Person.class) Predicate predicate,
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
}
```

#### Complex filtering

To implement more complex filter we have to extend our repo from the `QuerydslBinderCustomizer` 
then use its `customize` method (we can  do this right in the repo):

```java
public interface PersonRepo extends
        JpaRepository<Person, Long>,
        QuerydslPredicateExecutor<Person>,
        QuerydslBinderCustomizer<QPerson> {

    @SuppressWarnings("NullableProblems")
    @Override
    default void customize(QuerydslBindings bindings, QPerson person) {

        // Exclude id from filtering
        bindings.excluding(person.id);

        // Make case-insensitive 'like' filter for all string properties 
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);

        // Make a kind of 'between' filter for Person.age property
        bindings.bind(person.age).all((path, value) -> {
            Iterator<? extends Integer> it = value.iterator();
            Integer from = it.next();
            if (value.size() >= 2) {
                Integer to = it.next();
                return Optional.of(path.between(from, to));
            } else {
                return Optional.of(path.goe(from));
            }
        });

        // Make a kind of 'between' filter for Person.dob property 
        bindings.bind(person.dob).all((path, value) -> {
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
```

Here we excluded unnecessary `id` property from filtering, 
made 'like with ignore case' filter for all `String` properties of our entity 
and implemented two `between` filters for `age` property and for `dob` property (date of birth).

(see details here: https://stackoverflow.com/a/35158320)       

Now we can filter our data as following:

    GET /people?name=jo

find all people which name contains 'jo', or 'Jo'

    GET /people?age=18&age=30

find all people which age is between 18 and 30

    GET /people?dob=2000-01-01&dob=2000-12-31
             
find all people which date of birth is between 2000-01-01 and 2000-12-31

Note that in case to use date in the query we have to specified its pattern with `@DateTimeFormat` annotation in the entity:

```java
@Data
@Entity
@Table(name = "people")
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
```

To make `customize` method works we have to add parameter `bindings` to `@QuerydslPredicate` of our controller method:
```java
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
```
#### Setting up Querydsl

To add Querydsl to the project we have to add its dependencies and plugins to our pom.xml:

```xml
<dependencies>
    <!-- ... -->
    
    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-jpa</artifactId>
    </dependency>

    <dependency>
        <groupId>com.querydsl</groupId>
        <artifactId>querydsl-apt</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- ... -->
        
        <plugin>
            <groupId>com.mysema.maven</groupId>
            <artifactId>apt-maven-plugin</artifactId>
            <version>1.1.3</version>
            <executions>
                <execution>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>target/generated-sources/annotations</outputDirectory>
                        <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Then we have to **compile the project** to generate Q-classes of our entities. 

#### Additional info

Postman API-docs of this demo can be found [here](https://documenter.getpostman.com/view/788154/RWEnmvWX).

Relatet Stackoverflow post is [here](https://stackoverflow.com/q/51127468/5380322).