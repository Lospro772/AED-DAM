-- ALUMNADO: escribe aquí las 25 consultas del enunciado, identificadas del 01 al 25.
-- Utiliza schema.sql, los DAO y los tests para comprobar cada consulta.

-- ===== NIVEL 1 =====

-- 01. Libros ordenados por título. (LibroDao.listar)
SELECT id_libro, titulo, isbn, genero, anio_publicacion, ejemplares, id_autor
FROM libros
ORDER BY titulo;

-- 02. Socios activos ordenados por fecha de alta descendente. (SocioDao.listarActivos)
SELECT id_socio, nombre, email, telefono, fecha_alta, activo
FROM socios
WHERE activo = 1
ORDER BY fecha_alta DESC;

-- 03. Libros cuyo título contiene el texto indicado. (LibroDao.buscarPorTitulo)
SELECT id_libro, titulo, isbn, genero, anio_publicacion, ejemplares, id_autor
FROM libros
WHERE lower(titulo) LIKE '%libro%'
ORDER BY titulo;

-- 04. Libros publicados desde un año concreto y de un género determinado. (LibroDao.librosDesdeAnio)
SELECT id_libro, titulo, isbn, genero, anio_publicacion, ejemplares, id_autor
FROM libros
WHERE anio_publicacion >= 2000
  AND genero = 'Novela'
ORDER BY anio_publicacion;

-- 05. Géneros distintos registrados en el catálogo. (LibroDao.generos)
SELECT DISTINCT genero
FROM libros
ORDER BY genero;


-- ===== NIVEL 2 =====

-- 06. Libros con nombre y nacionalidad del autor.
-- PENDIENTE

-- 07. Préstamos con socio, libro y fechas. (PrestamoDao.prestamosConSocioYLibro)
SELECT p.id_prestamo, s.nombre, l.titulo, p.fecha_prestamo, p.fecha_limite, p.fecha_devolucion
FROM prestamos p
JOIN socios s ON s.id_socio = p.id_socio
JOIN libros l ON l.id_libro = p.id_libro
ORDER BY p.fecha_prestamo;

-- 08. Préstamos activos de un socio. (PrestamoDao.listarPorSocio)
SELECT id_prestamo, id_socio, id_libro, fecha_prestamo, fecha_limite, fecha_devolucion
FROM prestamos
WHERE id_socio = 5
  AND fecha_devolucion IS NULL;

-- 09. Socios que nunca han realizado préstamos.
-- PENDIENTE

-- 10. Reservas pendientes con nombre del socio, título y autor. (ReservaDao.reservasConDetalles)
SELECT r.id_reserva, s.nombre, l.titulo, a.nombre, r.fecha_reserva
FROM reservas r
JOIN socios s ON s.id_socio = r.id_socio
JOIN libros l ON l.id_libro = r.id_libro
JOIN autores a ON a.id_autor = l.id_autor
WHERE r.estado = 'PENDIENTE'
ORDER BY r.fecha_reserva;


-- ===== NIVEL 3 =====

-- 11. Cantidad de libros por género.
-- PENDIENTE

-- 12. Cantidad de libros por autor, incluyendo autores sin libros.
-- PENDIENTE

-- 13. Cinco socios con más préstamos.
-- PENDIENTE

-- 14. Géneros con más de cinco préstamos históricos.
-- PENDIENTE

-- 15. Por libro: ejemplares totales, prestados actualmente y disponibles.
-- PENDIENTE


-- ===== NIVEL 4 =====

-- 16. Libros que nunca se han prestado.
-- PENDIENTE

-- 17. Socios con préstamos vencidos en una fecha de referencia.
-- PENDIENTE

-- 18. Libros con más préstamos históricos que la media del catálogo.
-- PENDIENTE

-- 19. Autores cuyos libros han sido prestados por al menos tres socios distintos.
-- PENDIENTE

-- 20. Libros con reservas pendientes que no tienen ejemplares disponibles.
-- PENDIENTE


-- ===== NIVEL 5 =====

-- 21. Ranking de los tres libros más prestados de un mes indicado, con autor y cantidad de préstamos.
-- PENDIENTE

-- 22. Historial completo de préstamos de un socio, indicando devoluciones y retrasos.
-- PENDIENTE

-- 23. Reserva pendiente más antigua de cada libro.
-- PENDIENTE

-- 24. Estadísticas por género: títulos, ejemplares, préstamos históricos y préstamos activos.
-- PENDIENTE

-- 25. Registro atómico de un préstamo.
-- PENDIENTE
