package com.EnSe.Movienatic.service;

import java.sql.Timestamp;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.EnSe.Movienatic.dto.GenreDto;
import com.EnSe.Movienatic.dto.ListLikeResponse;
import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.MovieListDto;
import com.EnSe.Movienatic.dto.ReviewDto;
import com.EnSe.Movienatic.dto.ReviewVoteDto;
import com.EnSe.Movienatic.dto.UpdateReviewRequest;
import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.model.MovieList;
import com.EnSe.Movienatic.model.MovieListLike;
import com.EnSe.Movienatic.model.Review;
import com.EnSe.Movienatic.model.ReviewLike;
import com.EnSe.Movienatic.repository.MovieRepository;
import com.EnSe.Movienatic.repository.MovieListLikeRepository;
import com.EnSe.Movienatic.repository.MovieListRepository;
import com.EnSe.Movienatic.repository.ReviewLikeRepository;
import com.EnSe.Movienatic.repository.ReviewRepository;
import com.EnSe.Movienatic.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.EnSe.Movienatic.exception.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MovieListRepository movieListRepository;
    private final MovieListLikeRepository movieListLikeRepository;

    public UserService(UserRepository userRepository, MovieRepository movieRepository,
            ReviewRepository reviewRepository, MovieListRepository movieListRepository,
            ReviewLikeRepository reviewLikeRepository,
            MovieListLikeRepository movieListLikeRepository) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.reviewRepository = reviewRepository;
        this.movieListRepository = movieListRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.movieListLikeRepository = movieListLikeRepository;
    }

    // Orden alfabético estable (en español, sin distinguir mayúsculas ni tildes).
    // Se usa para que las colecciones de tipo Set no cambien de orden entre
    // peticiones.
    private static Comparator<String> textComparator() {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es"));
        collator.setStrength(Collator.PRIMARY);
        return Comparator.nullsLast((left, right) -> collator.compare(left, right));
    }

    // Devuelve un bloque de usuarios paginado (por ejemplo 20 por página) ya en
    // DTO, con el total y los metadatos de paginación de la Page
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserDto);
    }

    @Transactional(readOnly = true)
    public User getUserByName(String name) {
        return userRepository.findByUsername(name).orElseThrow(() -> new UserNotFoundException(name));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public UserDto getUserPublicById(Long id) {
        User user = getUserById(id);
        return toUserDto(user);
    }

    // Películas pendientes (paginadas: el perfil pide solo las 6 primeras)
    @Transactional(readOnly = true)
    public Page<MovieDto> getPendingMovies(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(userId);
        return movieRepository.findPendingMoviesPageByUserId(userId, pageable)
                .map(this::toMovieDto);
    }

    @Transactional
    public void addPending(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        user.getPendingMovies().add(movie);
        userRepository.save(user);
    }

    @Transactional
    public void removePending(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        if (!user.getPendingMovies().contains(movie))
            throw new PendingMovieNotFoundException(userId, movieId);
        user.getPendingMovies().remove(movie);
        userRepository.save(user);
    }

    // Peliculas favoritas (paginadas: el perfil pide solo las 6 primeras)
    @Transactional(readOnly = true)
    public Page<MovieDto> getFavourites(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(userId);
        return movieRepository.findFavouriteMoviesPageByUserId(userId, pageable)
                .map(this::toMovieDto);
    }

    @Transactional(readOnly = true)
    public boolean isFavourite(Long userId, Long movieId) {
        return movieRepository.existsFavouriteMovie(userId, movieId);
    }

    @Transactional(readOnly = true)
    public boolean isPending(Long userId, Long movieId) {
        return movieRepository.existsPendingMovie(userId, movieId);
    }

    @Transactional
    public void addFavourite(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        user.getFavouriteMovies().add(movie);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavourite(Long userId, Long movieId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        if (!user.getFavouriteMovies().contains(movie))
            throw new FavoriteMovieNotFoundException(userId, movieId);
        user.getFavouriteMovies().remove(movie);
        userRepository.save(user);
    }

    // Listas del usuario
    @Transactional(readOnly = true)
    public List<MovieListDto> getLists(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getMovieLists().stream()
                .sorted(Comparator
                        .comparing(MovieList::getName, textComparator())
                        .thenComparing(MovieList::getId))
                .map(this::toMovieListDto)
                .toList();
    }

    // Ids de listas a las que el usuario ha dado like
    @Transactional(readOnly = true)
    public List<Long> getLikedListIds(Long userId) {
        return movieListLikeRepository.findByUser_Id(userId).stream()
                .map(like -> like.getMovieList().getId())
                .toList();
    }

    // Da like a una lista (idempotente)
    @Transactional
    public ListLikeResponse likeList(Long userId, Long listId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        MovieList movieList = movieListRepository.findById(listId)
                .orElseThrow(() -> new ListNotFoundException(listId));

        if (movieListLikeRepository.findByUser_IdAndMovieList_Id(userId, listId).isEmpty()) {
            MovieListLike like = new MovieListLike();
            like.setUser(user);
            like.setMovieList(movieList);
            movieListLikeRepository.save(like);
        }

        return new ListLikeResponse(true, movieListLikeRepository.countByMovieList_Id(listId));
    }

    // Quita el like de una lista (idempotente)
    @Transactional
    public ListLikeResponse unlikeList(Long userId, Long listId) {
        movieListLikeRepository.findByUser_IdAndMovieList_Id(userId, listId)
                .ifPresent(movieListLikeRepository::delete);

        return new ListLikeResponse(false, movieListLikeRepository.countByMovieList_Id(listId));
    }

    @Transactional(readOnly = true)
    public Page<MovieDto> getListById(Long userId, Long listId, Pageable pageable) {
        movieListRepository.findByUser_IdAndId(userId, listId)
                .orElseThrow(() -> new ListNotFoundException(listId));
        return movieRepository.findMoviesByListIdAndUserId(listId, userId, pageable)
                .map(this::toMovieDto);
    }

    // Listas de todos los usuarios ordenadas por likes (portada y Social).
    @Transactional(readOnly = true)
    public Page<MovieListDto> getListsOrderedByLikes(Pageable pageable) {
        return movieListRepository.findAllOrderByLikes(pageable)
                .map(this::toMovieListDto);
    }

    // Crea una lista con todas sus películas en una sola operación.
    // Devuelve el DTO de la lista creada para que el cliente no tenga que
    // volver a pedir todas las listas solo para descubrir el id.
    @Transactional
    public MovieListDto addList(Long userId, String name, String description, List<Long> movieIds) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        MovieList movieList = new MovieList();
        movieList.setUser(user);
        movieList.setName(name);
        movieList.setDescription(description);
        if (movieIds != null && !movieIds.isEmpty()) {
            movieList.setMovies(resolveMovies(movieIds));
        }
        user.getMovieLists().add(movieList);
        movieListRepository.save(movieList);
        userRepository.save(user);
        return toMovieListDto(movieList);
    }

    // Actualiza los metadatos y, si se envían movieIds, reemplaza el contenido
    // de la lista. Todo en una sola transacción.
    @Transactional
    public MovieListDto updateList(Long userId, Long listId, String name, String description,
            List<Long> movieIds) {
        MovieList movieList = movieListRepository.findByUser_IdAndId(userId, listId)
                .orElseThrow(() -> new ListNotFoundException(listId));
        if (name != null && !name.isBlank()) {
            movieList.setName(name.trim());
        }
        movieList.setDescription(description);
        if (movieIds != null) {
            movieList.getMovies().clear();
            movieList.getMovies().addAll(resolveMovies(movieIds));
        }
        movieListRepository.save(movieList);
        return toMovieListDto(movieList);
    }

    // Resuelve una lista de ids de película a entidades, sin duplicados.
    private Set<Movie> resolveMovies(List<Long> movieIds) {
        return movieIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(movieId -> movieRepository.findById(movieId)
                        .orElseThrow(() -> new MovieNotFoundException(movieId)))
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Transactional
    public void removeList(Long userId, Long listId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        MovieList movieList = movieListRepository.findByUser_IdAndId(userId, listId)
                .orElseThrow(() -> new ListNotFoundException(listId));
        if (!user.getMovieLists().contains(movieList))
            throw new ListNotFoundException(listId);
        user.getMovieLists().remove(movieList);
        movieListLikeRepository.deleteByMovieList_Id(listId);
        movieListRepository.delete(movieList);
        userRepository.save(user);
    }

    @Transactional
    public void addMovieToList(Long userId, Long listId, Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        MovieList movieList = movieListRepository.findByUser_IdAndId(userId, listId)
                .orElseThrow(() -> new ListNotFoundException(listId));
        // Idempotente: si la película ya está en la lista, no se duplica.
        boolean alreadyInList = movieList.getMovies().stream()
                .anyMatch(existing -> existing.getId().equals(movieId));
        if (!alreadyInList) {
            movieList.getMovies().add(movie);
            movieListRepository.save(movieList);
        }
    }

    @Transactional
    public void removeMovieToList(Long userId, Long listId, Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));
        MovieList movieList = movieListRepository.findByUser_IdAndId(userId, listId)
                .orElseThrow(() -> new ListNotFoundException(listId));
        movieList.getMovies().remove(movie);
        movieListRepository.save(movieList);
    }

    // Siguiendo
    @Transactional(readOnly = true)
    public List<UserDto> getFollowing(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getFollowing()
                .stream()
                .map(this::toUserDto)
                .toList();
    }

    @Transactional
    public void followUser(Long userId, Long followUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User userToFollow = userRepository.findById(followUserId)
                .orElseThrow(() -> new UserNotFoundException(followUserId));
        user.getFollowing().add(userToFollow);
        userRepository.save(user);
    }

    @Transactional
    public void unfollowUser(Long userId, Long unfollowUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User userToUnfollow = userRepository.findById(unfollowUserId)
                .orElseThrow(() -> new UserNotFoundException(unfollowUserId));
        if (!user.getFollowing().contains(userToUnfollow))
            throw new FollowingUserNotFoundException(userId, unfollowUserId);
        user.getFollowing().remove(userToUnfollow);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getFollowers(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getFollowers()
                .stream()
                .map(this::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviews(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.getReviews()
                .stream()
                .map(this::toReviewDto)
                .toList();
    }

    @Transactional
    public void addReview(Long userId, Long movieId, String content, Double rating) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException(movieId));

        // Verificar si el usuario ya ha escrito una reseña para esta película, no hacer
        // update, sino lanzar excepción
        movie.getReviews().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .findFirst()
                .ifPresent(r -> {
                    throw new ReviewAlreadyExistsException(userId, movieId);
                });

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setContent(content);
        review.setRating(rating);
        review.setTimestamp(new Timestamp(System.currentTimeMillis()));

        movie.getReviews().add(review);
        user.getReviews().add(review);

        recalculateMovieRating(movie);

        reviewRepository.save(review);
        userRepository.save(user);
        movieRepository.save(movie);
    }

    @Transactional
    public ReviewDto updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));
        Movie movie = review.getMovie();

        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotOwnedException(userId, reviewId);
        }

        if (request.content() != null) {
            review.setContent(request.content());
        }
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        review.setTimestamp(new Timestamp(System.currentTimeMillis()));

        recalculateMovieRating(movie);

        reviewRepository.save(review);
        movieRepository.save(movie);

        return toReviewDto(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));
        Movie movie = review.getMovie();

        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotOwnedException(userId, reviewId);
        }

        movie.getReviews().remove(review);
        recalculateMovieRating(movie);

        reviewRepository.delete(review);
        movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public List<ReviewVoteDto> getReviewVotes(Long userId, Long movieId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId.toString());
        }

        List<ReviewLike> votes = movieId == null
                ? reviewLikeRepository.findByUserId(userId)
                : reviewLikeRepository.findByUserIdAndMovieId(userId, movieId);

        return votes.stream()
                .map(vote -> new ReviewVoteDto(vote.getReview().getId(), vote.getIsLike()))
                .toList();
    }

    @Transactional
    public String likeReview(Long userId, Long reviewId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));

        Optional<ReviewLike> existing = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId);
        if (existing.isPresent()) {
            ReviewLike vote = existing.get();
            if (Boolean.TRUE.equals(vote.getIsLike())) {
                // Ya tenía like: se lo quita
                reviewLikeRepository.delete(vote);
                return "Like quitado de la reseña";
            } else {
                // Tenía dislike: lo cambia a like
                vote.setIsLike(true);
                reviewLikeRepository.save(vote);
                return "Voto cambiado a útil";
            }
        } else {
            ReviewLike newVote = new ReviewLike();
            newVote.setUser(user);
            newVote.setReview(review);
            newVote.setIsLike(true);
            reviewLikeRepository.save(newVote);
            return "Reseña marcada como útil";
        }
    }

    @Transactional
    public String dislikeReview(Long userId, Long reviewId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId.toString()));

        Optional<ReviewLike> existing = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId);
        if (existing.isPresent()) {
            ReviewLike vote = existing.get();
            if (Boolean.FALSE.equals(vote.getIsLike())) {
                // Ya tenía dislike: se lo quita
                reviewLikeRepository.delete(vote);
                return "Dislike quitado de la reseña";
            } else {
                // Tenía like: lo cambia a dislike
                vote.setIsLike(false);
                reviewLikeRepository.save(vote);
                return "Voto cambiado a no útil";
            }
        } else {
            ReviewLike newVote = new ReviewLike();
            newVote.setUser(user);
            newVote.setReview(review);
            newVote.setIsLike(false);
            reviewLikeRepository.save(newVote);
            return "Reseña marcada como no útil";
        }
    }

    // Método privado para recalcular la calificación promedio de una película y el
    // número de calificaciones
    private void recalculateMovieRating(Movie movie) {
        if (movie.getReviews() == null || movie.getReviews().isEmpty()) {
            // Sin reseñas no hay media: dejamos los campos vacíos para no
            // incumplir @DecimalMin("0.5") y permitir borrar la última reseña.
            movie.setAverageRating(null);
            movie.setNumberOfRatings(0);
            return;
        }

        Double averageRating = movie.getReviews().stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        movie.setAverageRating(averageRating);
        movie.setNumberOfRatings(movie.getReviews().size());
    }

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPhotoURL(),
                user.getFavouriteMovies() == null ? 0 : user.getFavouriteMovies().size(),
                user.getPendingMovies() == null ? 0 : user.getPendingMovies().size(),
                user.getReviews() == null ? 0 : user.getReviews().size(),
                user.getMovieLists() == null ? 0 : user.getMovieLists().size(),
                user.getFollowing() == null ? 0 : user.getFollowing().size(),
                user.getFollowers() == null ? 0 : user.getFollowers().size());
    }

    // Método privado para convertir una entidad Movie a un DTO MovieDto
    private MovieDto toMovieDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenres().stream()
                        .map(g -> new GenreDto(g.getId(), g.getName()))
                        .toList(),
                movie.getReleaseYear(),
                movie.getCasting(),
                movie.getPosterURL(),
                movie.getAverageRating(),
                movie.getNumberOfRatings(),
                movie.getReviews().stream()
                        .map(review -> new ReviewDto(
                                review.getId(),
                                review.getMovie().getId(),
                                review.getMovie().getTitle(),
                                review.getMovie().getPosterURL(),
                                review.getUser().getId(),
                                review.getUser().getUsername(),
                                review.getContent(),
                                review.getRating(),
                                reviewLikeRepository.countByReviewAndIsLike(review, true),
                                reviewLikeRepository.countByReviewAndIsLike(review, false),
                                review.getTimestamp()))
                        .toList());
    }

    private ReviewDto toReviewDto(com.EnSe.Movienatic.model.Review review) {
        return new ReviewDto(
                review.getId(),
                review.getMovie().getId(),
                review.getMovie().getTitle(),
                review.getMovie().getPosterURL(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getContent(),
                review.getRating(),
                reviewLikeRepository.countByReviewAndIsLike(review, true),
                reviewLikeRepository.countByReviewAndIsLike(review, false),
                review.getTimestamp());
    }

    private MovieListDto toMovieListDto(com.EnSe.Movienatic.model.MovieList movieList) {
        Long listId = movieList.getId();
        return new MovieListDto(
                listId,
                movieList.getName(),
                movieList.getDescription(),
                (int) movieRepository.countMoviesByListId(listId),
                movieRepository.findPosterUrlsByListId(listId, PageRequest.of(0, 6)),
                movieListLikeRepository.countByMovieList_Id(listId),
                movieList.getUser() == null ? null : movieList.getUser().getId(),
                movieList.getUser() == null ? null : movieList.getUser().getUsername(),
                movieList.getUser() == null ? null : movieList.getUser().getPhotoURL());
    }
}
