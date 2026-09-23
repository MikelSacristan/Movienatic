# Movienatic — Tecnologías y estructura del proyecto

Documento de referencia con los términos y librerías clave del proyecto, y el mapa de carpetas del código.

---

## 1. Tecnologías y conceptos

### Spring Framework
Framework de desarrollo para Java. Su pilar es el **contenedor de IoC** (Inversión de Control): en lugar de que tú crees los objetos (`new Servicio()`), los crea y gestiona el propio framework (los llama *beans*). Tú solo declaras las dependencias en el constructor y Spring se las inyecta.

**El proyecto usa estos módulos de Spring:**
- **Spring Core / Beans**: el contenedor de IoC (`@Component`, `@Service`, `@Repository`, ...).
- **Spring Data JPA**: acceso a bases de datos por repositorios (ver abajo).
- **Spring Web Services / Web**: servidor embebido Tomcat y soporte HTTP (lo usa `RestClient`).
- **Spring Transaction**: gestión de transacciones (`@Transactional`).

### Spring Boot
Extensión de Spring que **elimina la configuración manual**:

- **Auto-configuración**: detecta qué librerías hay (`spring-boot-starter-data-jpa`, ...) y configura por ti (EntityManagerFactory, Tomcat, Jackson...).
- **Starters**: dependencias preempaquetadas que traen todo lo necesario (p. ej. `spring-boot-starter-data-jpa` ya incluye Hibernate, PostgreSQL, transacciones...).
- **Servidor embebido**: la app arranca con su propio Tomcat (`./gradlew bootRun` → correrá en `http://localhost:8080`).
- **Fichero `application.properties`**: aquí configuras la app (conexión a BD, credenciales TMDB, `ddl-auto`, ...). Los valores se leen con `@Value("${clave}")`.
- **`@SpringBootApplication`** en `MovienaticApplication`: indica *component scanning* (escanea ese paquete y subpaquetes buscando beans) y activa la auto-configuración.

### Componentes de Spring usados en este proyecto
- **`@Service`**: bean que contiene la lógica de negocio (p. ej. `TmdbService`).
- **`@Repository`**: bean de acceso a datos (interfaz de JPA).
- **`@Component`**: bean genérico (p. ej. `DataInitializer`).
- **`@Value("${...}")`**: inyecta valores de `application.properties` en un constructor o campo.
- **`CommandLineRunner`**: interfaz que ejecuta `run()` al terminar el arranque de la app (la usa `DataInitializer`).
- **`RestClient`**: cliente HTTP de Spring para llamar a la API de TMDB.
- **`@Transactional`**: envuelve un método en una transacción; los `save` no se hacen definitivos (commit) hasta que el método termina bien.

### Spring Data JPA
Capa que simplifica el acceso a la BD: **tú declaras una interfaz** (`MovieRepository extends JpaRepository<Movie, Long>`) y Spring Data genera la implementación en runtime. Heredando `JpaRepository` obtienes CRUD (`save`, `findById`, `count`, ...) y, definiendo métodos por nombre, **derived queries**: `existsByTitle(...)` se traduce solo en `SELECT ... WHERE title = ?`.

### JPA (Jakarta Persistence) y Hibernate
- **JPA** es el *estándar* de mapeo objeto-relacional (ORM) en Java. Con él marcas clases como entidades: `@Entity`, `@Table`, `@Id`, `@Column`, `@OneToMany`, `@ManyToOne`, `@ElementCollection`, ...
- **Hibernate** es la *implementación* (el proveedor) de ese estándar que se usa aquí. Se encarga de traducir entidades a SQL y de gestionar el **persistence context**: las entidades "managed" se guardan o actualizan contra la BD al hacer flush/commit.
- **`spring.jpa.hibernate.ddl-auto=update`**: Hibernate sincroniza el esquema al arrancar (crea tablas/columnas que falten). Limitaiones: no altera columnas existentes ni borra columnas sobrantes; para producción se usa `validate`/`none`.
- **`@ElementCollection` + `@Embeddable`**: colección de valores embebidos en una tabla aparte (ejemplo: `Movie.casting` ↔ tabla `movie_casting`).

### Jackson
**Librería de serialización/deserialización JSON** (convierte objetos Java ↔ JSON). Spring la usa automáticamente:

- Al recibir la respuesta de TMDB, Jackson convierte el JSON en los **records** del paquete `dto` (p. ej. `TmdbMovieDto`).
- `@JsonProperty("release_date")` enlaza el nombre JSON con el campo Java.
- Al responder un controlador REST, también serializaría los objetos Java a JSON.
- (Nota: `record` funciona bien con Jackson en Spring Boot moderno sin configuración extra.)

### Lombok
Librería que **genera código repetitivo en compilación** (getters, setters, constructores, `toString`, ...) mediante anotaciones (`@Getter`, `@Data`, `@Builder`, ...). Está declarada en `build.gradle` (`compileOnly`).

⚠️ **Importante en este proyecto**: está en las dependencias pero **todavía no se usa** en el código: los modelos (`Movie`, `Review`, ...) tienen getters/setters escritos a mano. Si en el futuro se añade `@Data` a una entidad, hay que cuidar el `equals/hashCode` por sus relaciones.

### API TMDB
API REST externa de películas. El proyecto llama a:
- `GET /discover/movie` — listado de películas (traducidas a `es-ES`).
- `GET /genre/movie/list` — ids de género → nombres.
- `GET /movie/{id}/credits` — reparto de una película (actor + personaje).

Las URLs y el token se configuran en `application.properties` (`tmdb.api.*`).

### PostgreSQL
Base de datos relacional usada (`movienaticdb`). Parámetros en `application.properties` (`spring.datasource.*`).

### Gradle
Herramienta de build. `build.gradle` declara dependencias; `./gradlew` es el wrapper. Comandos útiles:
- `./gradlew bootRun` — arranca la app.
- `./gradlew compileJava` — compila.
- `./gradlew test` — ejecuta tests.

---

## 2. Estructura de carpetas

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
    │   │   ├── dto/                         → records que mapean el JSON de TMDB
    │   │   │   ├── TmdbResponse.java
    │   │   │   ├── TmdbMovieDto.java
    │   │   │   ├── TmdbGenreResponse.java
    │   │   │   ├── TmdbGenreDto.java
    │   │   │   ├── TmdbCreditsResponse.java
    │   │   │   └── TmdbCastDto.java
    │   │   ├── exception/                   → excepciones propias de la app
    │   │   │   ├── MovieNotFoundException.java
    │   │   │   ├── UserNotFoundException.java
    │   │   │   ├── DuplicatedMovieException.java
    │   │   │   └── DuplicatedUserException.java
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
    │   │       └── UserService.java         → lógica de usuarios (por completar)
    │   └── resources/
    │       └── application.properties       → configuración (BD, TMDB, Hibernate)
    ├── test/java/com/EnSe/Movienatic/       → tests (MovienaticApplicationTests)
    └── ...
```

### Qué significa cada paquete

- **`config`**: beans de configuración / lógica de arranque (`DataInitializer`, `AppConfig`).
- **`dto`** (Data Transfer Object): objetos "de transporte" que reflejan la forma del JSON externo. Aquí son *records* inmutables de Java.
- **`model`**: entidades con `@Entity` que representan las tablas (`movies`, `reviews`, `users`, `movie_lists`).
- **`repository`**: interfaces de Spring Data JPA; puente entre la app y la BD.
- **`service`**: lógica de negocio. Se inyectan los repositorios y otros servicios; los expone a los controladores (cuando existan).
- **`exception`**: excepciones propias para errores concretos (película no encontrada, usuario duplicado...).
- **`resources/application.properties`**: configuración central.

---

## 3. Flujo de datos resumido

```
TMDB (HTTP) → TmdbService (RestClient) → DTOs (records)
                ↓
         MovieImportService (mapea DTO → entidad, evita duplicados)
                ↓
         MovieRepository (Spring Data JPA / Hibernate)
                ↓
         PostgreSQL (tablas movies, movie_casting, reviews, ...)
                ↓
         (futuro) Controladores REST → JSON al frontend
```

La importación inicial la dispara `DataInitializer` al arrancar, solo si la tabla `movies` está vacía.