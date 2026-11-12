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
│   │   │   ├── MovienaticApplication.java   → punto de entrada (@SpringBootApplication)
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java     → importa películas TMDB al arrancar (CommandLineRunner)
│   │   │   │   ├── DemoDataGenerator.java   → datos de prueba (perfil "demo")
│   │   │   │   ├── AppConfig.java           → configuraciones de Spring (vacío, por definir)
│   │   │   │   ├── OpenApiConfig.java       → configuración de Swagger/OpenAPI con Bearer Auth
│   │   │   │   └── SecurityConfig.java      → configuración de Spring Security (JWT, CORS, roles)
│   │   │   ├── controller/                  → API REST (@RestController)
│   │   │   │   ├── AuthController.java       → POST /auth/login, /register, /refresh, /logout
│   │   │   │   ├── MovieController.java       → GET /movies, /movies/{id}, /detail, /casting, /review
│   │   │   │   ├── MovieListController.java   → gestión de listas de películas (por completar)
│   │   │   │   ├── ReviewController.java      → POST/PUT/DELETE /users/{id}/reviews/{reviewId}, like/dislike
│   │   │   │   └── UserController.java        → GET /users, /users/{id}, /following, /followers, /pending, /favourites
│   │   │   ├── dto/                         → records que mapean el JSON (TMDB) y la salida (MovieDto)
│   │   │   │   ├── AuthResponse.java          → token de acceso y refresh tras login/refresh
│   │   │   │   ├── GenreDto.java              → id y nombre de género
│   │   │   │   ├── LoginDto.java              → entrada de /auth/login (email, password)
│   │   │   │   ├── MovieDto.java              → salida de películas con @JsonView (Summary, Detail, Casting, Review)
│   │   │   │   ├── MovieFilter.java           → filtros de búsqueda (título, género, rating, año)
│   │   │   │   ├── MoviePageResponse.java   → wrapper paginado para /movies con @JsonView
│   │   │   │   ├── RegisterDto.java           → entrada de /auth/register (@NotBlank, @Email)
│   │   │   │   ├── ReviewDto.java             → datos de una reseña
│   │   │   │   ├── ReviewRequest.java         → entrada para crear/editar reseña
│   │   │   │   ├── UpdateReviewRequest.java   → entrada para actualizar reseña
│   │   │   │   ├── FavouriteRequest.java      → entrada para añadir/quitar favorita
│   │   │   │   ├── FollowRequest.java         → entrada para seguir/dejar de seguir
│   │   │   │   ├── PendingRequest.java        → entrada para añadir/quitar pendiente
│   │   │   │   ├── TmdbCastDto.java
│   │   │   │   ├── TmdbCreditsResponse.java
│   │   │   │   ├── TmdbGenreDto.java
│   │   │   │   ├── TmdbGenreResponse.java
│   │   │   │   ├── TmdbMovieDto.java
│   │   │   │   ├── TmdbResponse.java
│   │   │   │   ├── UserBasicDetailsDto.java   → listado público de usuarios (id, username, photoURL)
│   │   │   │   ├── UserDetailDto.java         → detalle individual de usuario con contadores
│   │   │   │   └── UserDto.java               → (legacy, ya no se usa en endpoints principales)
│   │   │   ├── exception/                   → excepciones propias + gestión centralizada
│   │   │   │   ├── GlobalExceptionHandler.java  → @RestControllerAdvice (todas las respuestas de error)
│   │   │   │   ├── BadCredentialsException.java → 401 en el login
│   │   │   │   ├── DuplicatedMovieException.java → 409 en importación
│   │   │   │   ├── DuplicatedUserException.java → 409 en registro
│   │   │   │   ├── FavoriteMovieNotFoundException.java → 404
│   │   │   │   ├── FollowingUserNotFoundException.java → 404
│   │   │   │   ├── ListNotFoundException.java → 404
│   │   │   │   ├── MovieNotFoundException.java → 404
│   │   │   │   ├── PendingMovieNotFoundException.java → 404
│   │   │   │   ├── ReviewAlreadyExistsException.java → 409 (reseña duplicada)
│   │   │   │   ├── ReviewNotFoundException.java → 404
│   │   │   │   ├── ReviewNotOwnedException.java → 403 (no es propietaria)
│   │   │   │   └── UserNotFoundException.java → 404
│   │   │   ├── model/                       → entidades JPA (+ tipos embebidos)
│   │   │   │   ├── CastMember.java          → @Embeddable (actor + personaje)
│   │   │   │   ├── Genre.java               → entidad género (normalización many-to-many)
│   │   │   │   ├── Movie.java
│   │   │   │   ├── MovieList.java
│   │   │   │   ├── RefreshToken.java        → entidad para refresh tokens (rotación, revocación)
│   │   │   │   ├── Review.java
│   │   │   │   ├── ReviewLike.java
│   │   │   │   └── User.java
│   │   │   ├── repository/                  → interfaces de Spring Data JPA
│   │   │   │   ├── GenreRepository.java     → JpaRepository<Genre, Long>
│   │   │   │   ├── MovieRepository.java     → JpaRepository<Movie, Long> + findByGenreId/Name
│   │   │   │   ├── RefreshTokenRepository.java → JpaRepository<RefreshToken, Long> + findByToken
│   │   │   │   ├── ReviewLikeRepository.java
│   │   │   │   ├── ReviewRepository.java
│   │   │   │   └── UserRepository.java      → JpaRepository<User, Long> + findByUsername/Email
│   │   │   ├── security/                    → seguridad JWT
│   │   │   │   ├── config/
│   │   │   │   │   └── SecurityConfig.java  → filter chain, CORS, permitAll, estado stateless
│   │   │   │   ├── filter/
│   │   │   │   │   └── JwtAuthenticationFilter.java → lee header Authorization, valida JWT
│   │   │   │   └── service/
│   │   │   │       ├── JwtService.java        → genera, firma y valida JWT
│   │   │   │       └── MyUserDetailsService.java → implementa UserDetailsService (busca por email)
│   │   │   └── service/                     → lógica de negocio (@Service)
│   │   │       ├── MovieImportService.java  → importa películas de TMDB (sin duplicados)
│   │   │       ├── MovieService.java        → consulta de películas (devuelve DTOs, filtros)
│   │   │       ├── TmdbService.java         → cliente HTTP de la API TMDB (RestClient)
│   │   │       └── UserService.java         → lógica de usuarios (reviews, favoritos, pendientes, follows)
│   │   └── resources/
│   │       └── application.properties       → configuración (BD, TMDB, JWT, Hibernate)
│   ├── test/java/com/EnSe/Movienatic/       → tests (MovienaticApplicationTests)
│   └── ...
```

### Qué significa cada paquete

| Ubicación | ¿Qué hace? |
|-----------|------------|
| **`config`** | beans de configuración / lógica de arranque (`DataInitializer`, `DemoDataGenerator`, `AppConfig`, `OpenApiConfig`, `SecurityConfig`). |
| **`controller`** | expone la API REST (convierte HTTP ↔ DTO). No habla con la BD directamente. |
| **`dto`** (Data Transfer Object): objetos de transporte. Los `Tmdb*` reflejan el JSON externo; los `Movie*`/`Review*`/`User*` son la salida para el cliente; los `*Request` son la entrada. |
| **`model`**: entidades con `@Entity` que representan las tablas (`movies`, `reviews`, `users`, `genres`, `movie_genres`, `refresh_tokens`, `movie_lists`, etc.). |
| **`repository`**: interfaces de Spring Data JPA; puente entre la app y la BD. |
| **`security`**: todo lo relacionado con autenticación y autorización JWT (`SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`, `MyUserDetailsService`). |
| **`service`**: lógica de negocio. Se inyectan los repositorios y otros servicios; transforma entidad ↔ DTO. |
| **`exception`**: excepciones propias para errores concretos y el `GlobalExceptionHandler` que las traduce a respuestas HTTP con formato uniforme. |
| **`resources/application.properties`**: configuración central. |

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

## 16. Seguridad: autenticación y autorización con JWT

El proyecto usa **Spring Security** + **JSON Web Tokens (JWT)** para proteger los endpoints de la API REST. Spring Security no guarda sesiones en el servidor (*stateless*); el cliente envía un token JWT en cada petición y el backend valida su firma.

### 16.1 Dependencias necesarias

En `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

### 16.2 Propiedades de configuración

En `application.properties`:

```properties
# Clave secreta para firmar los JWT (debe ser larga y secreta; en producción usa variable de entorno)
jwt.secret=TuClaveSecretaMuyLargaYSeguraParaFirmarJWT1234567890

# Tiempo de vida del access token en milisegundos (por defecto 15 minutos)
jwt.access-expiration=900000

# Tiempo de vida del refresh token en milisegundos (por defecto 7 días)
jwt.refresh-expiration=604800000
```

> ⚠️ Nunca subas `jwt.secret` al repositorio de Git. Úsala como variable de entorno en producción (`@Value("${jwt.secret}")` lee también variables de entorno si se define así).

### 16.3 `MyUserDetailsService` (puente entre tu entidad `User` y Spring Security)

Spring Security necesita un objeto `UserDetails` (su propia interfaz) para autenticar. El proyecto no usa la clase `User` de Spring, sino la entidad propia del dominio:

```java
package com.EnSe.Movienatic.security.service;

import com.EnSe.Movienatic.model.User;              // TU entidad de BD
import com.EnSe.Movienatic.repository.UserRepository; // TU repositorio JPA
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring pasa el email como "username"; se busca en la entidad propia
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())      // el "username" de Spring será el email
                .password(user.getPassword())   // hash BCrypt
                .roles("USER")                  // rol por defecto
                .build();
    }
}
```

- `loadUserByUsername` se invoca al hacer **login** y también en **cada petición con JWT** (desde `JwtAuthenticationFilter`).
- El campo `password` de tu entidad `User` debe guardar el **hash BCrypt**, nunca texto plano.

### 16.4 `JwtService` (firmar y validar tokens)

```java
package com.EnSe.Movienatic.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration:900000}")
    private long accessExpiration;

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
```

> Advertencia de IntelliJ: `signWith(Key, SignatureAlgorithm)` está deprecada desde JJWT 0.12.0. Sigue funcionando; en versiones futuras migra a `SigningAlgorithm`.

### 16.5 `JwtAuthenticationFilter` (filtro que lee el token en cada petición)

Este filtro se ejecuta en **todas** las peticiones HTTP. Si hay un header `Authorization: Bearer <token>`, lo valida y establece al usuario en el contexto de seguridad:

```java
package com.EnSe.Movienatic.security.filter;

import com.EnSe.Movienatic.security.service.JwtService;
import com.EnSe.Movienatic.security.service.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### 16.6 `SecurityConfig` (configuración de Spring Security)

```java
package com.EnSe.Movienatic.security.config;

import com.EnSe.Movienatic.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (sin token)
                .requestMatchers(
                    "/auth/**",            // login, register, refresh, logout
                    "/movies", "/movies/**", // catálogo público
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/webjars/**"
                ).permitAll()
                // Todo lo demás requiere token válido
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // tu frontend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Puntos clave:

- **`STATELESS`**: no se usa `HttpSession` del servidor; el estado viaja en el JWT.
- **`permitAll()`** en `/auth/**`, `/movies/**` y Swagger para que sean accesibles sin token.
- **`anyRequest().authenticated()`**: todo lo demás exige token válido.
- El filtro JWT se inserta **antes** de `UsernamePasswordAuthenticationFilter`.

### 16.7 DTOs de autenticación

```java
package com.EnSe.Movienatic.dto;

public record LoginDto(String email, String password) { }

public record AuthResponse(String accessToken, String refreshToken, String type, String email) {
    public AuthResponse(String accessToken, String email) {
        this(accessToken, null, "Bearer", email);
    }

    public AuthResponse(String accessToken, String refreshToken, String email) {
        this(accessToken, refreshToken, "Bearer", email);
    }
}

public record RefreshRequest(String refreshToken) { }
```

### 16.8 `AuthController` (endpoints de autenticación)

```java
package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.AuthResponse;
import com.EnSe.Movienatic.dto.LoginDto;
import com.EnSe.Movienatic.dto.RefreshRequest;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.repository.RefreshTokenRepository;
import com.EnSe.Movienatic.repository.UserRepository;
import com.EnSe.Movienatic.security.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginDto loginDto) {
        // 1. Valida email + contraseña (usa MyUserDetailsService + BCrypt internamente)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        // 2. Recupera tu entidad User de la base de datos
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3. Genera el JWT
        String token = jwtService.generateAccessToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build()
        );

        // 4. Crea y guarda el refresh token en BD (para rotación y revocación)
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setUser(user);
        rt.setExpiryDate(new Date(System.currentTimeMillis() + 604800000L)); // 7 días
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);

        return ResponseEntity.ok(new AuthResponse(token, refreshToken, user.getEmail()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token no válido"));

        if (rt.isRevoked() || rt.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Refresh token expirado o revocado");
        }

        User user = rt.getUser();

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .build()
        );

        // Rotar refresh token
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        String newRefreshToken = UUID.randomUUID().toString();
        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefreshToken);
        newRt.setUser(user);
        newRt.setExpiryDate(new Date(System.currentTimeMillis() + 604800000L));
        newRt.setRevoked(false);
        refreshTokenRepository.save(newRt);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, user.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
        return ResponseEntity.ok("Sesión cerrada");
    }
}
```

### 16.9 Entidad `RefreshToken` y repositorio

```java
package com.EnSe.Movienatic.model;

import java.util.Date;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private String tokenType;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Date expiresAt;
}
```

```java
package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
```

### 16.10 Flujo de autenticación completo

```
1. Frontend → POST /auth/login  { email, password }
2. Backend  → authenticationManager.authenticate()  → llama a MyUserDetailsService.loadUserByUsername(email)
3. Backend  → compara contraseña BCrypt
4. Backend  → genera accessToken (JWT, 15 min) y refreshToken (BD, 7 días)
5. Frontend → guarda accessToken (memoria/localStorage) y refreshToken
6. Frontend → en cada petición protegida envía: Authorization: Bearer <accessToken>
7. Backend  → JwtAuthenticationFilter valida el token y pone al usuario en SecurityContext
8. Si accessToken expira (401/403) → frontend llama a POST /auth/refresh con refreshToken
9. Backend  → valida refreshToken en BD, rota (invalida el viejo, crea uno nuevo) y devuelve accessToken nuevo
```

### 16.11 Mapa de excepciones de seguridad

| Situación | Código HTTP | Origen |
|-----------|-------------|--------|
| Token expirado | 403 / 401 | `JwtAuthenticationFilter` / `ExpiredJwtException` |
| Token malformado / sin firma válida | 403 / 401 | `JwtAuthenticationFilter` |
| Credenciales incorrectas en login | 401 | `BadCredentialsException` → `GlobalExceptionHandler` |
| Refresh token expirado o revocado | 401 | `AuthController` → `RuntimeException` |
| Usuario no encontrado | 404 | `UserNotFoundException` → `GlobalExceptionHandler` |

> **Nota importante:** Si el access token expira, el filtro JWT lanza `ExpiredJwtException` **antes** de llegar a tu controlador. Ese error **no** lo captura `GlobalExceptionHandler`, porque ocurre en el filtro de Spring Security. Para evitar que el usuario tenga que hacer login manualmente, el frontend debe interceptar el 401/403 y llamar a `/auth/refresh`.

### 16.12 Cómo probar la seguridad

#### Con `curl`

```bash
# 1. Registro (público)
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","email":"ana@mail.com","password":"123456"}'

# 2. Login (público) → devuelve accessToken y refreshToken
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@mail.com","password":"123456"}'

# 3. Endpoint protegido (requiere token)
curl -X GET http://localhost:8080/users/1/pending \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 4. Refresh (cuando el access token expire)
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"uuid-del-refresh-token"}'

# 5. Logout (invalida el refresh token)
curl -X POST http://localhost:8080/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"uuid-del-refresh-token"}'
```

#### Con Swagger UI

1. Abre `http://localhost:8080/swagger-ui/index.html`.
2. Ejecuta `POST /auth/login` para obtener el token.
3. Haz clic en **Authorize** (candado 🔒), pega el token con formato `Bearer <token>` y pulsa **Authorize**.
4. Ahora puedes ejecutar cualquier endpoint protegido (`GET /users/{id}/pending`, etc.) y Swagger enviará el token automáticamente.

---

## 18. Comandos útiles

```bash
./gradlew bootRun                                   # ejecutar
./gradlew compileJava                               # compilar
curl http://localhost:8080/movies/1                 # probar endpoint
```



## 17. Filtros de búsqueda de películas (`Specification`)

El endpoint `GET /movies` permite filtrar películas de forma dinámica y paginada usando **Spring Data JPA `Specification`**. Esto evita crear un método de repositorio por cada combinación de filtros posibles.

### 17.1 DTO de filtros (`MovieFilter`)

Se define un record que agrupa los criterios opcionales de búsqueda:

```java
package com.EnSe.Movienatic.dto;

import java.util.List;

public record MovieFilter(
        String title,           // contiene, ignorando mayúsculas
        List<Long> genreIds,    // películas que tengan al menos uno de estos géneros
        Double minRating,       // average_rating >= valor
        Integer releaseYear     // año exacto de estreno
) {
}
```

Spring MVC lo enlaza automáticamente con los parámetros de consulta (`?title=star&genreIds=1&genreIds=2&minRating=7.0&releaseYear=2020`).

### 17.2 `MovieSpecification`

Es la clase que construye los `Predicate` de JPA Criteria API según qué filtros vayan rellenados:

```java
package com.EnSe.Movienatic.specification;

import com.EnSe.Movienatic.dto.MovieFilter;
import com.EnSe.Movienatic.model.Movie;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MovieSpecification {

    public static Specification<Movie> withFilters(MovieFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + filter.title().toLowerCase() + "%"
                    )
                );
            }

            if (filter.genreIds() != null && !filter.genreIds().isEmpty()) {
                // Se une a la tabla intermedia movie_genres (relación many-to-many)
                predicates.add(root.join("genres").get("id").in(filter.genreIds()));
            }

            if (filter.minRating() != null) {
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), filter.minRating())
                );
            }

            if (filter.releaseYear() != null) {
                predicates.add(
                    criteriaBuilder.equal(root.get("releaseYear"), filter.releaseYear())
                );
            }

            query.distinct(true); // necesario por el JOIN de géneros para evitar duplicados
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

- `query.distinct(true)` es obligatorio porque el `JOIN` con la tabla de géneros multiplica filas si una película tiene varios géneros.
- Los filtros son **opcionales**: si no se pasa ningún parámetro, se devuelven todas las películas paginadas sin filtro.

### 17.3 Servicio (`MovieService`)

El servicio recibe el `MovieFilter` y lo envuelve en una `Specification` antes de consultar paginada:

```java
@Transactional(readOnly = true)
public Page<MovieDto> getMovies(MovieFilter filter, Pageable pageable) {
    Specification<Movie> spec = MovieSpecification.withFilters(filter);
    return movieRepository.findAll(spec, pageable).map(this::toMovieDto);
}
```

`movieRepository` extiende `JpaSpecificationExecutor<Movie>` además de `JpaRepository<Movie, Long>`, por lo que dispone del método `findAll(Specification, Pageable)`.

### 17.4 Controlador (`MovieController`)

El controlador recibe los parámetros de query automáticamente mapeados a `MovieFilter` y los pasa al servicio:

```java
@GetMapping("")
@JsonView(MovieDto.SummaryView.class)
public ResponseEntity<MoviePageResponse> getMovies(
        @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable,
        MovieFilter filter) {
    Page<MovieDto> page = movieService.getMovies(filter, pageable);
    MoviePageResponse response = new MoviePageResponse(
            page.getContent(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize());
    return ResponseEntity.ok(response);
}
```

### 17.5 Ejemplos de uso

| URL | Qué devuelve |
|-----|--------------|
| `GET /movies?page=0&size=20` | Todas las películas paginadas (sin filtro) |
| `GET /movies?title=star&page=0&size=10` | Películas cuyo título contenga "star" (case-insensitive) |
| `GET /movies?genreIds=1&genreIds=2&page=0&size=10` | Películas de Acción **o** Ciencia ficción |
| `GET /movies?minRating=7.0&page=0&size=10` | Películas con calificación >= 7.0 |
| `GET /movies?releaseYear=2020&page=0&size=10` | Películas estrenadas en 2020 |
| `GET /movies?title=matrix&minRating=8.0&releaseYear=1999` | Combinación de varios filtros |

### 17.6 Nota sobre `Page` + `@JsonView`

Debido a que `PageImpl` (el wrapper de paginación de Spring Data) y `@JsonView` no siempre aplican el filtro de vista de forma fiable sobre el wrapper, el endpoint envuelve la respuesta en `MoviePageResponse`, un DTO propio con los campos `@JsonView(SummaryView.class)` anotados uno a uno (`content`, `totalPages`, `totalElements`, `number`, `size`).

