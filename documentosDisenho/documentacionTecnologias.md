# Movienatic — Tecnologías y estructura del proyecto

Documento de referencia con los términos y librerías del proyecto, explicados con ejemplos del propio código, y el mapa de carpetas.

---

## 1. Spring Framework

Framework de Java cuyo pilar es el **contenedor de IoC** (Inversión de Control): en lugar de que tú crees los objetos, los crea y gestiona Spring (los llama *beans*). Tú declaras las dependencias y Spring te las inyecta.

### IoC e inyección por constructor

Spring **nunca** instancia con `new` tus clases de negocio. Busca las clases anotadas y las construye pasándole sus dependencias por el constructor:

```java
@Service
public class TmdbService {
    public TmdbService(@Value("${tmdb.api.base-url}") String baseUrl, ...) { ... }
}

@Service
public class MovieImportService {
    private final TmdbService tmdbService;            // inyectado por Spring
    private final MovieRepository movieRepository;

    public MovieImportService(TmdbService tmdbService, MovieRepository movieRepository, ...) {
        this.tmdbService = tmdbService;               // no hay "new TmdbService()"
        this.movieRepository = movieRepository;
    }
}
```

- **Inversión de Control**: la clase *depende* de una abstracción (interfaz `MovieRepository`) y Spring le da la implementación concreta.
- **Inyección por constructor** (no por setter ni por campo): es la recomendada porque deja el objeto completo e inmutable desde el constructor, y facilita los tests.
- Spring además usa **inyección por campo** con `@Autowired`, pero el constructor se prefiere (y es lo que usa este proyecto).

### Módulos de Spring que usa el proyecto

| Módulo | Para qué |
|---|---|
| Spring Beans / Core | Contenedor de IoC (`@Service`, `@Component`, `@Repository`) |
| Spring Data JPA | Acceso a BD por repositorios |
| Spring Web / WebMVC | Servidor embebido Tomcat y controladores REST |
| Spring Transaction | Transacciones (`@Transactional`) |
| Spring Test | Tests (starter de test) |

---

## 2. Spring Boot

Extensión de Spring que elimina la configuración manual.

### `@SpringBootApplication`

```java
@SpringBootApplication
public class MovienaticApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovienaticApplication.class, args);
    }
}
```

Hace tres cosas a la vez:
1. **Component scanning**: escanea el paquete `com.EnSe.Movienatic` y subpaquetes buscando `@Component`, `@Service`, `@Repository`, `@RestController`. Por eso las clases deben estar **bajo ese paquete** (estructura de carpetas coherente con el paquete).
2. **Auto-configuración**: deduce la configuración (BD, Tomcat, Jackson, JPA).
3. **Configuración de beans**: habilita el escaneo de `@Configuration`.

### Starters (build.gradle)

```gradle
implementation 'org.springframework.boot:spring-boot-starter-webservices'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-validation'    // @NotBlank, @Email, @Valid
runtimeOnly 'org.postgresql:postgresql'
```

Un *starter* es un paquete de dependencias ya preseleccionadas — `spring-boot-starter-data-jpa` ya trae Hibernate, JDBC y transacciones. No hace falta declararlas una a una.

### `application.properties` y `@Value`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/movienaticdb
spring.jpa.hibernate.ddl-auto=update
tmdb.api.base-url=https://api.themoviedb.org/3
tmdb.api.token=eyJhbGciOiJIUzI1NiJ9...
```

```java
public TmdbService(@Value("${tmdb.api.token}") String token) { ... }
```

`@Value("${clave}")` inyecta el valor de la property. (En proyectos grandes se usa mejor `@ConfigurationProperties` para agrupar.) También se pueden inyectar con variables de entorno: `@Value("${tmdb.api.token}")` lee la variable de entorno `TMDB_API_TOKEN` si se define con ese formato en Spring Boot.

### Servidor embebido

`bootRun` arranca Tomcat embebido en `http://localhost:8080` sin desplegar un servidor externo.

---

## 3. Anotaciones de Spring en el código

### Stereotypes (declaran un bean)

| Anotación | Significado | Ejemplo en el proyecto |
|---|---|---|
| `@Component` | Bean genérico | `DataInitializer`, `AppConfig` |
| `@Service` | Lógica de negocio | `TmdbService`, `MovieImportService`, `MovieService` |
| `@Repository` | Acceso a datos | `MovieRepository` (interfaz JPA) |
| `@RestController` | Bean web (API REST) | `MovieController`, `AuthController` |

Las tres primeras son "aliases" de `@Component` con semántica propia; todas hacen que Spring escanee y cree el bean.

### `@RestController` y `@RequestMapping`

```java
@RestController
@RequestMapping("/movies")          // prefijo de URL
public class MovieController {

    private final MovieService movieService;
    public MovieController(MovieService movieService) { this.movieService = movieService; }

    @GetMapping("/{id}")            // → GET /movies/{id}
    public MovieDto getMovieById(@PathVariable Long id) {
        MovieDto movie = movieService.getMovieById(id);
        if (movie == null) {
            throw new MovieNotFoundException(HttpStatus.NOT_FOUND, "Película no encontrada: " + id);
        }
        return movie;                // el return se serializa a JSON automáticamente
    }
}
```

- `@RestController` = `@Controller` + `@ResponseBody`: el objeto retornado se serializa a **JSON** con Jackson, en vez de buscar una plantilla HTML.
- `@RequestMapping("/movies")` a nivel de clase define el prefijo. Ojo: el argumento de `@RestController("x")` es el **nombre del bean**, no una URL.
- `@PathVariable` captura el segmento de la URL: `GET /movies/5` → `id = 5`.
- Otros verbos: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`.

### `CommandLineRunner` (ejecutar al arrancar)

```java
@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        int imported = movieImportService.importPopularMovies(10);
        log.info("Importadas {} películas nuevas desde TMDB", imported);
    }
}
```

Spring, tras construir el contexto, recorre todos los beans y llama a `run()` de los que implementan `CommandLineRunner` o `ApplicationRunner`. (El primero recibe los args de la línea de comandos.)

### `RestClient` (cliente HTTP)

```java
this.restClient = RestClient.builder()
        .baseUrl("https://api.themoviedb.org/3")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .defaultHeader(HttpHeaders.ACCEPT, "application/json")
        .build();

public TmdbResponse fetchDiscoveredMovies(int page) {
    return restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/discover/movie")
                    .queryParam("language", "es-ES")
                    .build())
            .retrieve()                       // si hay 4xx/5xx lanza excepción
            .body(TmdbResponse.class);         // deserializa el JSON al record
}
```

Se configura una vez en el constructor y se reutiliza. `.retrieve()` devuelve un `ResponseSpec` para convertir la respuesta.

### `@Transactional`

```java
@Transactional
public int importPopularMovies(int pages) {
    ...
    movieRepository.saveAll(newMovies);   // no visible hasta el commit
    return imported;
}
```

- Marca el método como una transacción: todo el bloque se confirma (commit) si termina bien; si lanza excepción, se deshace (rollback).
- **`@Transactional(readOnly = true)`** (en `MovieService`) indica solo lectura: es más rápido y permite optimizaciones. Se usa en `getMovieById` porque accede a colecciones lazy.
- Las colecciones `LAZY` (p. ej. `movie.getReviews()`) solo se cargan dentro de una transacción activa; fuera de ella saltarían `LazyInitializationException`.

---

## 4. Spring Data JPA

Capa que hace el acceso a BD casi declarativo: escribes una **interfaz** y Spring genera la implementación.

```java
public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTitle(String title);          // derived query
    Optional<Movie> findByTitle(String title);    // derived query

    @Query("select m.title from Movie m")         // JPQL explícito
    List<String> findAllTitles();
}
```

- `JpaRepository<Movie, Long>`: `Movie` = entidad, `Long` = tipo de su `@Id`.
- Heredando `JpaRepository` obtienes CRUD y paginación gratis: `save`, `findById`, `findAll`, `count`, `delete`, `saveAll`, `findAll(Pageable)`, ...
- **Derived queries**: el nombre del método codifica la consulta. `findByTitle` → `WHERE title = ?`, `existsByTitle` → `SELECT EXISTS(...)`, `findByTitleContainingIgnoreCase` → `LIKE ?`, etc.
- **`@Query` con JPQL**: JPQL consulta la *entidad* (no la tabla); Hibernate lo traduce a SQL.

### Efficient findAllTitles (evitar N+1)

```java
Set<String> existingTitles = new HashSet<>(movieRepository.findAllTitles());
...
if (!existingTitles.add(dto.title())) { continue; }  // si ya existe, salta
```

Trae todos los títulos con **una** consulta y compara en memoria. La alternativa (consultar `existsByTitle` por película) genera N+1 consultas.

---

## 5. JPA (Jakarta Persistence) y Hibernate

- **JPA** es el estándar de mapeo objeto-relacional (ORM). Describe *cómo* se mapea una clase a una tabla.
- **Hibernate** es la implementación concreta (el "proveedor") que se usa en este proyecto. Traduce entidades a SQL.

### Anotaciones de mapeo

```java
@Entity
@Table(name = "movies")                       // tabla
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // autoincremento
    private Long id;

    @Column(nullable = false)                 // NOT NULL
    private String title;

    @Column(nullable = false, length = 2000)  // varchar(2000)
    private String description;

    @ElementCollection                                        // tabla aparte
    @CollectionTable(name = "movie_casting", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "position")
    private List<CastMember> casting = new ArrayList<>();
}
```

Relaciones:

```java
// Muchos a uno (N:1): una review pertenece a una película
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "movie_id", nullable = false)
private Movie movie;

// Uno a muchos (1:N)
@OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
private List<Review> reviews;

// Muchos a muchos (N:M)
@ManyToMany
@JoinTable(name = "user_favorite_movies",
           joinColumns = @JoinColumn(name = "user_id"),
           inverseJoinColumns = @JoinColumn(name = "movie_id"))
private Set<Movie> favoriteMovies = new HashSet<>();
```

- `LAZY` vs `EAGER`: `LAZY` carga bajo demanda (dentro de transacción); `EAGER` carga siempre. Se prefiere `LAZY` para no hacer queries innecesarios.
- `CascadeType.ALL`: propagar operaciones (guardar/eliminar) a la colección relacionada.
- `@JoinTable`: tabla puente de las relaciones N:M.

### `@Embeddable`

```java
@Embeddable
public class CastMember {
    @Column(name = "actor")          private String name;
    @Column(name = "character_name") private String character;
}
```

Un valor compuesto sin tabla propia; se guarda dentro de la tabla de la entidad que lo contiene (en este caso, la tabla `movie_casting`).

### `ddl-auto` (sincronizar esquema)

```properties
spring.jpa.hibernate.ddl-auto=update
```

| Valor | Qué hace |
|---|---|
| `none` | No toca el esquema |
| `validate` | Solo comprueba que coincida |
| `update` | Crea tablas/columnas que falten |
| `create` | Borra y recrea todo |
| `create-drop` | Como `create` + borra al cerrar |

`update` es solo para desarrollo: no altera columnas existentes ni elimina columnas sobrantes. En producción se usa `validate`/`none` y migraciones (Flyway/Liquibase).

---

## 6. Jackson (JSON)

Librería que convierte **Java ↔ JSON**. Spring la usa automáticamente.

### Deserializar (JSON → record)

```java
public record TmdbMovieDto(
        Long id,
        String title,
        String overview,
        @JsonProperty("release_date") String releaseDate,   // JSON "release_date"
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("genre_ids") List<Integer> genreIds) {
}
```

- El JSON de TMDB viene en `snake_case` (`release_date`), Java usa `camelCase` (`releaseDate`); `@JsonProperty` los enlaza. Sin él, el campo quedaría en `null`.
- Los **records** se deserializan bien con Jackson sin configuración extra.

### Serializar (Java → JSON)

Al retornar `MovieDto` desde `MovieController`, Jackson genera el JSON:

```json
{
  "title": "Avatar",
  "genre": "Ciencia ficción, Acción",
  "casting": [{"name": "Sam Worthington", "character": "Jake Sully"}],
  "reviews": []
}
```

Los nombres de los campos del record son las claves del JSON.

---

## 7. Lombok

Genera código repetitivo en **compilación** (getters, setters, constructores, `toString`, etc.) con anotaciones. Declarado en `build.gradle`:

```gradle
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

`compileOnly` = solo en tiempo de compilación (el código generado ya está en el `.class`).

### Uso en el proyecto

```java
@Getter                    // genera getTitle(), getGenre(), etc.
@Setter                    // genera setTitle(), etc.
@NoArgsConstructor        // constructor vacío (lo exige JPA)
@AllArgsConstructor    // constructor con todos los campos
@Entity
@Table(name = "movies")
public class Movie {
    @Column(nullable = false)
    private String title;     // sin getters/setters escritos a mano
}
```

En entidades JPA se usa `@Getter/@Setter/@NoArgsConstructor` (no `@Data`), porque `@Data` genera `equals`/`hashCode`/`toString` que pueden provocar `LazyInitializationException` o recursion con colecciones y relaciones.

---

## 8. Records de Java

Los DTOs usan `record`, una forma compacta de clase inmutable:

```java
public record ReviewDto(Long id, String content, Integer rating, Integer likes, Integer dislikes, Long timestamp) { }
```

Genera automáticamente constructor, getters (`content()`), `equals`, `hashCode` y `toString`. Son inmutables y perfecto para DTOs (datos que solo viajan).

> Nota: los records exponen los accesores **sin** `get` (p. ej. `dto.title()`, no `dto.getTitle()`).

---

## 9. API TMDB

API REST externa de películas. Endpoints que usa el proyecto:

| Endpoint | Para qué | Método en el código |
|---|---|---|
| `GET /discover/movie` | Listado de películas (traducidas a `es-ES`) | `fetchDiscoveredMovies(page)` |
| `GET /genre/movie/list` | Ids de género → nombres | `fetchGenreNames()` |
| `GET /movie/{id}/credits` | Reparto (actor + personaje) | `fetchCredits(movieId)` |

Configuración en `application.properties` (`tmdb.api.*`) y autenticación vía header `Authorization: Bearer <token>`.

**Importante**: los endpoints de *listado* (como `discover`) no incluyen el casting; hay que pedir `/credits` por película.

---

## 10. PostgreSQL

Base de datos relacional usada (`movienaticdb`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/movienaticdb
spring.datasource.username=adminatic
spring.datasource.password=...
spring.datasource.driver-class-name=org.postgresql.Driver
```

El driver va como `runtimeOnly` en `build.gradle`.

---

## 11. Gradle

Herramienta de build (equivale a Maven, pero con scripts más potentes).

```bash
./gradlew bootRun        # arranca la app
./gradlew compileJava    # compila
./gradlew test           # tests
./gradlew build          # compila + test + empaqueta
```

`./gradlew` usa el wrapper: descarga y usa la versión de Gradle definida en `gradle/wrapper/gradle-wrapper.properties`, sin necesidad de tener Gradle instalado.

---

## 12. Validación de datos (Bean Validation / Hibernate Validator)

Comprueba los datos **antes** de que lleguen a la lógica de negocio. La especificación es Jakarta Bean Validation (`jakarta.validation`) y su implementación de referencia es **Hibernate Validator**.

### Dependencia

```gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

Desde Spring Boot 2.3 **ya no va incluida** en `spring-boot-starter-web`: sin esta línea, `@NotBlank`/`@Email`/`@Valid` no compilan.

### Anotaciones usadas en el proyecto

| Anotación | Comprueba | Ejemplo en el proyecto |
|---|---|---|
| `@NotBlank` | no nulo, no vacío y sin solo espacios | `username`, `password` en `RegisterDto` |
| `@Email` | formato de email válido | `email` en `RegisterDto`, `email` en `User` |

Otras comunes: `@NotNull`, `@NotEmpty`, `@Size(min, max)`, `@Min`, `@Max`, `@Pattern`.

**Nota:** `@Email` deja pasar `null` y la cadena vacía (solo valida el formato si hay contenido), así que si el campo es obligatorio hay que combinarlo con `@NotBlank`.

### Dónde se ponen

**1. En el DTO de entrada** (lo habitual):

```java
// dto/RegisterDto.java
public record RegisterDto(@NotBlank String username, @NotBlank @Email String email,
        @NotBlank String password) {}
```

**2. En la entidad** (red de seguridad interna): Hibernate Validator comprueba las anotaciones de un `@Entity` en el `save()`:

```java
// model/User.java
@Column(nullable = false, unique = true)
@Email
private String email;      // si llegara mal → ConstraintViolationException (500)
```

La validación del DTO evita que la petición incorrecta llegue siquiera al service; la de la entidad protege frente a errores internos (por ejemplo, una inserción hecha a mano desde otro punto del código).

### Activación: `@Valid`

Las anotaciones del DTO **solo se ejecutan si el controller lo pide**:

```java
// controller/AuthController.java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto) { ... }
```

- `@Valid` delante de `@RequestBody` dispara la validación de todos los campos del DTO.
- Si falla, Spring lanza `MethodArgumentNotValidException`, que recoge el `GlobalExceptionHandler` (sección 13) y responde **400**.
- **Sin `@Valid` las anotaciones no hacen nada** (aunque compilen).

Ejemplo de respuesta con un email mal escrito:

```json
{"status": 400, "error": "Bad Request",
 "message": "email: debe ser una dirección de correo electrónico con formato correcto"}
```

---

## 13. Excepciones y gestión centralizada de errores

### Dos formas de lanzar un error

**1. `ResponseStatusException` (de Spring)** — lleva código HTTP y mensaje en la propia excepción:

```java
public class MovieNotFoundException extends ResponseStatusException {
    public MovieNotFoundException(HttpStatus status, String message) {
        super(status, message);        // → 404 + "Película no encontrada: 5"
    }
}
```

Spring la convierte solo en la respuesta HTTP correspondiente, sin intervención del controller.

**2. Excepciones propias `RuntimeException`** — para errores de negocio (duplicados, credenciales...):

```java
public class DuplicatedUserException extends RuntimeException {
    public DuplicatedUserException(String username, String email) {
        super("El usuario '" + username + "' o el email '" + email + "' ya están registrados");
        this.username = username;
        this.email = email;
    }
}
```

- Heredar de `RuntimeException` hace que no haga falta `throws` ni `try/catch` en los controllers.
- `super(mensaje)` es **imprescindible**: sin él, `e.getMessage()` devuelve `null`.

### `GlobalExceptionHandler` (`@RestControllerAdvice`)

Clase única, en el paquete `exception/`, que captura todas las excepciones y las convierte en JSON con la misma forma. Los controllers no necesitan `try/catch`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatedUserException.class)
    public ResponseEntity<Map<String, Object>> usuarioDuplicado(DuplicatedUserException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());          // 409
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> credencialesIncorrectas(BadCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());      // 401
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, mensaje);              // 400 (fallos de @Valid)
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
```

- `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`: aplica a **todos** los controllers del proyecto, no solo a uno.
- `@ExceptionHandler(Tipo.class)` registra qué hacer con cada excepción. El propio Spring lanza `MethodArgumentNotValidException` cuando falla un `@Valid`; aquí solo se traduce a JSON.

### Mapa de excepciones → código HTTP

| Excepción | Status | Cuándo ocurre |
|---|---|---|
| `MethodArgumentNotValidException` | **400** Bad Request | falla `@Valid` en el DTO (sección 12) |
| `BadCredentialsException` | **401** Unauthorized | login con credenciales incorrectas |
| `MovieNotFoundException` / `UserNotFoundException` | **404** Not Found | recurso inexistente |
| `DuplicatedMovieException` / `DuplicatedUserException` | **409** Conflict | registro o importación duplicada |

Ejemplo real de respuesta (registro duplicado):

```json
{"status": 409, "error": "Conflict",
 "message": "El usuario 'pepe' o el email 'pepe@correo.es' ya están registrados"}
```

### Por qué centralizada y no `try/catch` en cada controller

- **Un solo punto** para todos los errores → respuesta JSON uniforme que el frontend puede manejar siempre igual.
- El controller queda limpio: solo lanza la excepción (`throw new ...`).
- Si alguna excepción no está gestionada, Spring responde 500 y el error aparece en el log (se ve al depurar, no se cuela en la API).

### Nota de seguridad (login)

`BadCredentialsException` usa el **mismo mensaje** para "el usuario no existe" y "la contraseña es incorrecta". Si se distinguieran, un atacante podría averiguar qué emails o nombres de usuario existen en la BD (enumeración de usuarios).

---

## 14. Estructura de carpetas

```
Movienatic/
├── build.gradle                     → dependencias y configuración de build
├── settings.gradle
├── gradlew / gradlew.bat            → wrapper de Gradle
├── gradle/wrapper/...               → versión fija de Gradle
├── documentosDisenho/               → documentación y diseño (este archivo, modelo UML .mdj)
└── src/
    ├── main/
    │   ├── java/com/EnSe/Movienatic/
    │   │   ├── MovienaticApplication.java   → punto de entrada (@SpringBootApplication)
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java     → importa TMDB al arrancar (CommandLineRunner)
    │   │   │   └── AppConfig.java           → configuraciones de Spring (por definir)
    │   │   ├── controller/                  → API REST (@RestController)
    │   │   │   ├── MovieController.java      → GET /movies/{id}
    │   │   │   └── AuthController.java       → (por completar) /auth
    │   │   ├── dto/                         → records que mapean el JSON (TMDB) y la salida (MovieDto)
    │   │   │   ├── TmdbResponse.java
    │   │   │   ├── TmdbMovieDto.java
    │   │   │   ├── TmdbGenreResponse.java
    │   │   │   ├── TmdbGenreDto.java
    │   │   │   ├── TmdbCreditsResponse.java
    │   │   │   ├── TmdbCastDto.java
    │   │   │   ├── MovieDto.java            → salida de la API
    │   │   │   ├── ReviewDto.java
    │   │   │   ├── RegisterDto.java         → entrada de /auth/register (@NotBlank, @Email)
    │   │   │   ├── LoginDto.java            → entrada de /auth/login
    │   │   │   └── UserDto.java             → datos de usuario (salida)
    │   │   ├── exception/                   → excepciones propias + gestión centralizada
    │   │   │   ├── GlobalExceptionHandler.java  → @RestControllerAdvice (todas las respuestas de error)
    │   │   │   ├── MovieNotFoundException.java  → 404 (ResponseStatusException)
    │   │   │   ├── UserNotFoundException.java
    │   │   │   ├── BadCredentialsException.java → 401 en el login
    │   │   │   ├── DuplicatedMovieException.java
    │   │   │   └── DuplicatedUserException.java → 409 en el registro
    │   │   ├── model/                       → entidades JPA (+ tipos embebidos)
    │   │   │   ├── Movie.java
    │   │   │   ├── CastMember.java          → @Embeddable (actor + personaje)
    │   │   │   ├── Review.java
    │   │   │   ├── User.java
    │   │   │   └── MovieList.java
    │   │   ├── repository/                  → interfaces de Spring Data JPA
    │   │   │   ├── MovieRepository.java     → JpaRepository<Movie, Long>
    │   │   │   └── UserRepository.java      → JpaRepository<User, Long>
    │   │   └── service/                     → lógica de negocio (@Service)
    │   │       ├── TmdbService.java         → cliente HTTP de la API TMDB
    │   │       ├── MovieImportService.java  → mapea y guarda en BD (sin duplicados)
    │   │       ├── MovieService.java        → consulta de películas (devuelve DTOs)
    │   │       └── UserService.java         → lógica de usuarios (por completar)
    │   └── resources/
    │       └── application.properties       → configuración (BD, TMDB, Hibernate)
    ├── test/java/com/EnSe/Movienatic/       → tests (MovienaticApplicationTests)
    └── ...
```

### Qué significa cada paquete

- **`config`**: beans de configuración / lógica de arranque (`DataInitializer`, `AppConfig`).
- **`controller`**: expone la API REST (convierte HTTP ↔ DTO). No habla con la BD directamente.
- **`dto`** (Data Transfer Object): objetos "de transporte". Los `Tmdb*` reflejan el JSON externo; los `Movie*`/`Review*` son la salida para el cliente.
- **`model`**: entidades con `@Entity` que representan las tablas (`movies`, `reviews`, `users`, `movie_lists`).
- **`repository`**: interfaces de Spring Data JPA; puente entre la app y la BD.
- **`service`**: lógica de negocio. Se inyectan los repositorios y otros servicios; transforma entidad ↔ DTO.
- **`exception`**: excepciones propias para errores concretos (película no encontrada, usuario duplicado...) y el `GlobalExceptionHandler` que las traduce a respuestas HTTP con formato uniforme.
- **`resources/application.properties`**: configuración central.

### Separación en capas (flujo de una petición)

```
Cliente HTTP
    ↓
Controller      (MovieController)          → valida URL, llama al service
    ↓
Service         (MovieService)             → lógica de negocio, entidad ↔ DTO
    ↓
Repository      (MovieRepository)          → interfaces JPA
    ↓
Hibernate / JPA                           → traduce a SQL
    ↓
PostgreSQL
```

---

## 15. Flujo de la importación (TMDB → BD)

```
DataInitializer.run()          (CommandLineRunner, al arrancar)
    ↓
MovieImportService.importPopularMovies(10)   (@Transactional)
    ↓
TmdbService.fetchGenreNames()               → /genre/movie/list   → Map<id, nombre>
    ↓
TmdbService.fetchDiscoveredMovies(page)     → /discover/movie     → TmdbResponse
    ↓
  por cada TmdbMovieDto:
    ├── ¿el título ya está en el Set?  → saltar (no duplicar)
    └── TmdbService.fetchCredits(id)    → /movie/{id}/credits → TmdbCreditsResponse
              ↓
        mapear a entidad Movie (toEntity)  +  List<CastMember>
              ↓
MovieRepository.saveAll(nuevas)          → INSERT en PostgreSQL
              ↓
commit (al terminar el método @Transactional)
```

Pasos clave:

1. **Sin duplicados**: los títulos existentes se cargan en un `Set` con una sola consulta (`findAllTitles`) y se comparan en memoria (evita el patrón N+1).
2. **Inserción en lote**: `saveAll` en vez de `save` por película.
3. **Ratings sin importar**: `averageRating` y `numberOfRatings` se dejan a `null`; los únicos valores posibles serán los de los usuarios de la app.
4. **Transacción**: todo el import se confirma al terminar el método; si algo falla, no queda nada a medias.

---

## 16. Comandos útiles

```bash
./gradlew bootRun                                   # ejecutar
./gradlew compileJava                               # compilar
curl http://localhost:8080/movies/1                 # probar endpoint
```