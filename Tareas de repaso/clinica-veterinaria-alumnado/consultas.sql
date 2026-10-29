-- 1. Todos los clientes alfabéticamente
SELECT id, nombre, email, telefono
FROM clientes
ORDER BY nombre;

-- 2. Mascotas de una especie (modifica 'Perro' si deseas)
SELECT id, nombre, especie, raza, fecha_nacimiento
FROM mascotas
WHERE especie = 'Perro';

-- 3. Mascotas de un cliente
SELECT id, nombre, especie, raza
FROM mascotas
WHERE id_cliente = 1;

-- 4. Veterinarios de una especialidad (ordenados por tarifa)
SELECT id, nombre, especialidad, tarifa_hora
FROM veterinarios
WHERE especialidad = 'Medicina general'
ORDER BY tarifa_hora;

-- 5. Consultas programadas (ordenadas por fecha)
SELECT id, id_mascota, id_veterinario, fecha, horas
FROM consultas
WHERE estado = 'PROGRAMADA'
ORDER BY fecha;

-- 6a. Mascota y propietario con WHERE
SELECT mascotas.nombre AS mascota, clientes.nombre AS propietario
FROM mascotas, clientes
WHERE mascotas.id_cliente = clientes.id;

-- 6b. Mascota y propietario con INNER JOIN (mismo resultado que 6a)
SELECT mascotas.nombre AS mascota, clientes.nombre AS propietario
FROM mascotas
INNER JOIN clientes ON mascotas.id_cliente = clientes.id;

-- 7. Consultas, mascota, propietario y veterinario
SELECT consultas.id, consultas.fecha, consultas.estado,
       mascotas.nombre AS mascota, clientes.nombre AS propietario, veterinarios.nombre AS veterinario
FROM consultas
INNER JOIN mascotas     ON consultas.id_mascota = mascotas.id
INNER JOIN clientes     ON mascotas.id_cliente = clientes.id
INNER JOIN veterinarios ON consultas.id_veterinario = veterinarios.id
ORDER BY consultas.fecha;

-- 8. Cantidad de mascotas por cliente, incluidos los de cero mascotas
--    LEFT JOIN conserva los clientes sin mascotas y COUNT(mascotas.id) cuenta 0 para ellos
SELECT clientes.id, clientes.nombre, COUNT(mascotas.id) AS num_mascotas
FROM clientes
LEFT JOIN mascotas ON mascotas.id_cliente = clientes.id
GROUP BY clientes.id, clientes.nombre
ORDER BY clientes.nombre;

-- 9. Importe tratamientos, incluyendo consultas sin tratamientos
--    SUM de ninguna fila da NULL y COALESCE lo cambia por 0
SELECT consultas.id, consultas.fecha,
       COALESCE(SUM(tratamientos.cantidad * tratamientos.precio_unitario), 0) AS importe
FROM consultas
LEFT JOIN tratamientos ON tratamientos.id_consulta = consultas.id
GROUP BY consultas.id, consultas.fecha
ORDER BY consultas.id;

-- 10. Consultas sin tratamientos
--     LEFT JOIN + IS NULL = se quedan las consultas que no encontraron pareja
SELECT consultas.id, consultas.fecha, consultas.estado
FROM consultas
LEFT JOIN tratamientos ON tratamientos.id_consulta = consultas.id
WHERE tratamientos.id IS NULL;
