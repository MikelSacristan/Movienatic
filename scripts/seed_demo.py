#!/usr/bin/env python3
"""
Puebla Movienatic con usuarios de demostración y actividad entre ellos.

Qué crea (de forma idempotente, se puede ejecutar varias veces):
  - 20 usuarios demo (contraseña: Password123!)
  - Reseñas de cada usuario sobre películas distintas
  - Favoritas y pendientes
  - Listas de películas con contenido
  - Relaciones de seguimiento entre usuarios
  - Likes y dislikes cruzados entre reseñas

Uso:
  python3 scripts/seed_demo.py
  python3 scripts/seed_demo.py --base-url http://localhost:8080
"""

import argparse
import json
import random
import sys
from urllib import error, request

PASSWORD = "Password123!"
DEFAULT_BASE_URL = "http://localhost:8080"

USERS = [
    "lucia_cine",
    "mario_films",
    "nuria_series",
    "diego_noir",
    "alba_scifi",
    "jorge_terror",
    "irene_drama",
    "pablo_risas",
    "sara_clasicos",
    "hugo_accion",
    "clara_animacion",
    "raul_indie",
    "eva_misterio",
    "toni_romance",
    "marta_docu",
    "ivan_western",
    "laura_thriller",
    "adrian_fantasia",
    "carmen_musical",
    "guille_belico",
]

REVIEW_TEMPLATES = [
    "Me ha sorprendido mucho {title}. Tiene un ritmo muy cuidado y un final que no esperaba.",
    "No terminó de convencerme {title}, aunque tiene momentos muy buenos y una fotografía preciosa.",
    "{title} es de esas películas que se quedan contigo. La volvería a ver sin dudarlo.",
    "Fui con pocas expectativas y {title} me pareció una pequeña joya. Muy recomendable.",
    "La premisa de {title} es interesante, pero el desarrollo se me hizo algo largo.",
    "Gran trabajo de todo el reparto en {title}. La banda sonora también está a otro nivel.",
    "{title} no es perfecta, pero consigue emocionar en los momentos clave.",
    "Una película para disfrutar sin prisa. {title} tiene alma y eso hoy no es fácil.",
    "Me esperaba otra cosa de {title}. Aun así, tiene escenas que merecen la pena.",
    "Si te gusta el cine bien contado, {title} es una apuesta segura.",
    "El guion de {title} juega muy bien con el espectador. Me ha tenido enganchado hasta el final.",
    "{title} me ha parecido valiente y muy original. Ojalá más películas así.",
    "Visualmente es espectacular, aunque la historia de {title} se queda un poco corta.",
    "No es mi género favorito, pero {title} me ha ganado. Muy entretenida.",
    "Salí del cine con una sonrisa. {title} es puro disfrute.",
    "Hay que reconocerle a {title} su ambición. No todo funciona, pero merece la pena.",
    "Una de esas películas que mejoran al pensarlas. {title} tiene muchas capas.",
    "El montaje de {title} es una maravilla. Se nota el cuidado en cada plano.",
    "{title} es ideal para una tarde de domingo. Cine clásico bien hecho.",
    "Me ha costado entrar en {title}, pero cuando lo hace, no te suelta.",
]

LIST_NAMES = [
    ("Imprescindibles", "Las películas que siempre recomiendo a todo el mundo."),
    ("Noche de palomitas", "Entretenimiento puro para desconectar."),
    ("Ciencia ficción de culto", "Mundos imposibles y preguntas enormes."),
    ("Clásicos que envejecen bien", "Películas que siguen funcionando décadas después."),
    ("Para llorar un rato", "Historias que llegan directas al corazón."),
    ("Acción sin descanso", "Adrenalina de principio a fin."),
    ("Joyas ocultas", "Películas que quizá se te pasaron y merecen una oportunidad."),
    ("Terror para valientes", "Para ver con las luces encendidas."),
    ("Romance de domingo", "Amores, desamores y alguna lágrima."),
    ("Animación para todos", "No solo para niños: animación con mucho que decir."),
]


def call(base_url, method, path, body=None, token=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    headers = {}
    if body is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = request.Request(f"{base_url}{path}", data=data, headers=headers, method=method)

    try:
        with request.urlopen(req, timeout=30) as response:
            raw = response.read().decode("utf-8")
            try:
                return response.status, json.loads(raw)
            except json.JSONDecodeError:
                return response.status, raw
    except error.HTTPError as exc:
        raw = exc.read().decode("utf-8")
        try:
            return exc.code, json.loads(raw)
        except json.JSONDecodeError:
            return exc.code, raw
    except Exception as exc:  # noqa: BLE001
        return 0, str(exc)


def rating():
    return random.choice([1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0])


def main():
    parser = argparse.ArgumentParser(description="Crea usuarios y actividad de demostración.")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--seed", type=int, default=2026)
    args = parser.parse_args()

    random.seed(args.seed)
    base_url = args.base_url.rstrip("/")

    print("→ Registrando usuarios demo…")
    for username in USERS:
        status, _ = call(
            base_url,
            "POST",
            "/auth/register",
            {
                "username": username,
                "email": f"{username}@movienatic.demo",
                "password": PASSWORD,
            },
        )
        if status not in (201, 409):
            print(f"  ! {username}: no se pudo registrar (HTTP {status})")

    print("→ Iniciando sesión para obtener un token…")
    status, login = call(
        base_url,
        "POST",
        "/auth/login",
        {"email": f"{USERS[0]}@movienatic.demo", "password": PASSWORD},
    )
    if status != 200 or not isinstance(login, dict) or "token" not in login:
        print(f"No se pudo iniciar sesión (HTTP {status}): {login}")
        return 1

    token = login["token"]

    print("→ Cargando usuarios y películas…")
    status, users_page = call(base_url, "GET", "/users?size=1000", token=token)
    if status != 200:
        print(f"No se pudieron cargar los usuarios (HTTP {status}): {users_page}")
        return 1

    users_by_name = {user["username"]: user["id"] for user in users_page["content"]}
    all_user_ids = [user["id"] for user in users_page["content"]]

    status, movies_page = call(base_url, "GET", "/movies?size=1000", token=token)
    if status != 200:
        print(f"No se pudieron cargar las películas (HTTP {status}): {movies_page}")
        return 1

    movies = movies_page["content"]
    movie_by_id = {movie["id"]: movie for movie in movies}
    movie_ids = list(movie_by_id.keys())

    if not movie_ids:
        print("No hay películas en la base de datos.")
        return 1

    demo_ids = {users_by_name[name] for name in USERS if name in users_by_name}
    reviewed_movie_ids = set()

    for index, username in enumerate(USERS, start=1):
        user_id = users_by_name.get(username)
        if user_id is None:
            print(f"  ! {username}: no aparece en la lista de usuarios, se omite")
            continue

        print(f"  [{index:02d}/{len(USERS)}] {username} (id {user_id})")

        # Reseñas
        review_count = random.randint(9, 14)
        for movie_id in random.sample(movie_ids, review_count):
            movie = movie_by_id[movie_id]
            content = random.choice(REVIEW_TEMPLATES).format(title=movie["title"])
            status, _ = call(
                base_url,
                "POST",
                f"/users/{user_id}/reviews",
                {"movieId": movie_id, "content": content, "rating": rating()},
                token=token,
            )
            if status in (200, 201):
                reviewed_movie_ids.add(movie_id)

        # Favoritas
        for movie_id in random.sample(movie_ids, random.randint(5, 9)):
            call(
                base_url,
                "POST",
                f"/users/{user_id}/favourites",
                {"movieId": movie_id},
                token=token,
            )

        # Pendientes
        for movie_id in random.sample(movie_ids, random.randint(3, 6)):
            call(
                base_url,
                "POST",
                f"/users/{user_id}/pending",
                {"movieId": movie_id},
                token=token,
            )

        # Listas creadas (1 o 2 por usuario)
        chosen_lists = random.sample(LIST_NAMES, random.randint(1, 2))
        status, current_lists = call(base_url, "GET", f"/users/{user_id}/lists", token=token)
        existing_names = {
            item.get("name") for item in (current_lists if isinstance(current_lists, list) else [])
        }
        for name, description in chosen_lists:
            if name not in existing_names:
                call(
                    base_url,
                    "POST",
                    f"/users/{user_id}/lists",
                    {"name": name, "description": description},
                    token=token,
                )

        status, user_lists = call(base_url, "GET", f"/users/{user_id}/lists", token=token)
        if status == 200 and isinstance(user_lists, list):
            for movie_list in user_lists[:2]:
                for movie_id in random.sample(movie_ids, random.randint(4, 7)):
                    call(
                        base_url,
                        "POST",
                        f"/users/{user_id}/lists/{movie_list['id']}",
                        {"id": movie_id},
                        token=token,
                    )

        # Seguimientos (entre usuarios demo y también hacia usuarios existentes)
        follow_targets = [target for target in all_user_ids if target != user_id]
        for target_id in random.sample(follow_targets, min(random.randint(4, 8), len(follow_targets))):
            call(
                base_url,
                "POST",
                f"/users/{user_id}/following/{target_id}",
                token=token,
            )

    # Cadena circular de seguimientos: garantiza que todos tengan seguidores.
    for index, username in enumerate(USERS):
        user_id = users_by_name.get(username)
        if user_id is None:
            continue
        for offset in (1, 2):
            target_name = USERS[(index + offset) % len(USERS)]
            target_id = users_by_name.get(target_name)
            if target_id is not None and target_id != user_id:
                call(
                    base_url,
                    "POST",
                    f"/users/{user_id}/following/{target_id}",
                    token=token,
                )

    print("→ Repartiendo likes y dislikes entre reseñas…")
    all_reviews = []
    for movie_id in sorted(reviewed_movie_ids):
        status, reviews = call(base_url, "GET", f"/movies/{movie_id}/reviews")
        if status == 200 and isinstance(reviews, list):
            all_reviews.extend(reviews)

    # Elimina duplicados por id
    all_reviews = list({review["id"]: review for review in all_reviews if review.get("id")}.values())
    print(f"  {len(all_reviews)} reseñas disponibles para votar")

    for index, username in enumerate(USERS, start=1):
        user_id = users_by_name.get(username)
        if user_id is None:
            continue

        status, existing_votes = call(
            base_url, "GET", f"/users/{user_id}/reviews/votes", token=token
        )
        voted_ids = {
            vote["reviewId"]
            for vote in (existing_votes if isinstance(existing_votes, list) else [])
        }

        candidates = [
            review
            for review in all_reviews
            if review.get("userId") != user_id and review["id"] not in voted_ids
        ]
        sample_size = min(random.randint(10, 20), len(candidates))
        for review in random.sample(candidates, sample_size):
            action = "like" if random.random() < 0.78 else "dislike"
            call(
                base_url,
                "PATCH",
                f"/users/{user_id}/reviews/{review['id']}/{action}",
                token=token,
            )

        if index % 5 == 0:
            print(f"  votos: {index}/{len(USERS)} usuarios procesados")

    print("\n✓ Datos de demostración creados.")
    print(f"  Usuarios: {len(USERS)}")
    print(f"  Contraseña común: {PASSWORD}")
    print("  Ejemplo: lucia_cine@movienatic.demo / Password123!")
    return 0


if __name__ == "__main__":
    sys.exit(main())
