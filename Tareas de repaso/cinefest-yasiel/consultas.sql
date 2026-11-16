-- CINEFEST. Completa C01 a C30. No se entregan las soluciones.

-- C01: Películas ordenadas alfabéticamente
SELECT titulo, genero, anio
FROM peliculas
ORDER BY titulo ASC;

-- C02: Espectadores activos por antigüedad
SELECT nombre, fecha_alta
FROM espectadores
WHERE activo = 1
ORDER BY fecha_alta ASC, nombre ASC;

-- C03: Películas de un género
SELECT titulo, pais, anio
FROM peliculas
WHERE genero = 'Drama'
ORDER BY titulo ASC;

-- C04: Sesiones entre dos fechas
SELECT fecha, hora, id_sesion, precio
FROM sesiones
WHERE fecha BETWEEN '2026-11-13' AND '2026-11-14'
ORDER BY fecha ASC, hora ASC, id_sesion ASC;

-- C05: Géneros distintos
SELECT DISTINCT genero
FROM peliculas
ORDER BY genero ASC;

-- C06: Salas con su sede
SELECT s.nombre AS sede, sa.nombre AS sala, sa.aforo
FROM salas sa
JOIN sedes s ON sa.id_sede = s.id_sede
ORDER BY sede ASC, sala ASC;

-- C07: Sesiones con película, sala y sede
SELECT se.id_sesion, p.titulo AS pelicula, s.nombre AS sede, sa.nombre AS sala, se.fecha, se.hora
FROM sesiones se
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
JOIN salas sa ON se.id_sala = sa.id_sala
JOIN sedes s ON sa.id_sede = s.id_sede
ORDER BY se.id_sesion ASC;

-- C08: Entradas con espectador y película
SELECT e.id_entrada, es.nombre AS espectador, p.titulo AS pelicula, e.estado
FROM entradas e
JOIN espectadores es ON e.id_espectador = es.id_espectador
JOIN sesiones se ON e.id_sesion = se.id_sesion
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
ORDER BY e.id_entrada ASC;

-- C09: Espectadores que nunca compraron entradas
SELECT es.nombre, es.email
FROM espectadores es
LEFT JOIN entradas e ON es.id_espectador = e.id_espectador
WHERE e.id_entrada IS NULL
ORDER BY es.nombre ASC;

-- C10: Valoraciones con datos de película y espectador
SELECT p.titulo AS pelicula, es.nombre AS espectador, v.puntuacion
FROM valoraciones v
JOIN peliculas p ON v.id_pelicula = p.id_pelicula
JOIN espectadores es ON v.id_espectador = es.id_espectador
ORDER BY pelicula ASC, espectador ASC;

-- C11: Número de películas por género
SELECT genero, COUNT(*) AS total_peliculas
FROM peliculas
GROUP BY genero
ORDER BY genero ASC;

-- C12: TODO

-- C13: TODO

-- C14: TODO

-- C15: TODO

-- C16: Películas sin sesiones
SELECT p.titulo
FROM peliculas p
LEFT JOIN sesiones se ON p.id_pelicula = se.id_pelicula
WHERE se.id_sesion IS NULL
ORDER BY p.titulo ASC;

-- C17: TODO

-- C18: TODO

-- C19: TODO

-- C20: TODO

-- C21: TODO

-- C22: TODO

-- C23: TODO

-- C24: Historial de entradas de un espectador
SELECT p.titulo AS pelicula, se.fecha AS fecha_sesion, e.estado AS estado_entrada, se.precio
FROM entradas e
JOIN sesiones se ON e.id_sesion = se.id_sesion
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
WHERE e.id_espectador = 1
ORDER BY fecha_sesion ASC, pelicula ASC;

-- C25: TODO

-- C26: TODO

-- C27: TODO

-- C28: Salas sin sesiones programadas
SELECT s.nombre AS sede, sa.nombre AS sala
FROM salas sa
JOIN sedes s ON sa.id_sede = s.id_sede
LEFT JOIN sesiones se ON sa.id_sala = se.id_sala AND se.estado = 'PROGRAMADA'
WHERE se.id_sesion IS NULL
ORDER BY sede ASC, sala ASC;

-- C29: TODO

-- C30: TODO
