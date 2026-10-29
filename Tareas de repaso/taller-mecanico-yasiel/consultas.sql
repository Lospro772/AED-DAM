-- 1. Lista los clientes ordenados por nombre.
SELECT id_cliente, nombre, email, telefono
FROM clientes
ORDER BY nombre;

-- 2. Consulta matrícula, marca, modelo y año de los vehículos de una marca elegida.
SELECT matricula, marca, modelo, anio
FROM vehiculos
WHERE marca = 'Seat';

-- 3. Obtén los vehículos de un cliente utilizando su identificador.
SELECT matricula, marca, modelo, anio
FROM vehiculos
WHERE id_cliente = 1;

-- 4. Lista los mecánicos de una especialidad y ordénalos por tarifa.
SELECT id_mecanico, nombre, especialidad, tarifa_hora
FROM mecanicos
WHERE especialidad = 'Motor'
ORDER BY tarifa_hora;

-- 5. Obtén las reparaciones pendientes, ordenadas por fecha.
SELECT id_reparacion, matricula, descripcion, fecha
FROM reparaciones
WHERE estado = 'PENDIENTE'
ORDER BY fecha;

-- 6a. Matrícula de cada vehículo y nombre de su propietario, con WHERE.
SELECT vehiculos.matricula, clientes.nombre
FROM vehiculos, clientes
WHERE vehiculos.id_cliente = clientes.id_cliente;

-- 6b. Lo mismo con INNER JOIN (mismo resultado que 6a).
SELECT vehiculos.matricula, clientes.nombre
FROM vehiculos
INNER JOIN clientes ON vehiculos.id_cliente = clientes.id_cliente;

-- 7. Reparaciones con el nombre del cliente, la matrícula y el nombre del mecánico.
SELECT reparaciones.id_reparacion, reparaciones.fecha,
       clientes.nombre AS cliente, vehiculos.matricula, mecanicos.nombre AS mecanico
FROM reparaciones
INNER JOIN vehiculos ON reparaciones.matricula = vehiculos.matricula
INNER JOIN clientes  ON vehiculos.id_cliente = clientes.id_cliente
INNER JOIN mecanicos ON reparaciones.id_mecanico = mecanicos.id_mecanico
ORDER BY reparaciones.fecha;

-- 8. Cuenta los vehículos de cada cliente, incluyendo clientes sin vehículos.
--    LEFT JOIN conserva los clientes sin vehículos y COUNT(vehiculos.matricula) cuenta 0 para ellos.
SELECT clientes.id_cliente, clientes.nombre, COUNT(vehiculos.matricula) AS num_vehiculos
FROM clientes
LEFT JOIN vehiculos ON vehiculos.id_cliente = clientes.id_cliente
GROUP BY clientes.id_cliente, clientes.nombre
ORDER BY clientes.nombre;

-- 9. Coste de piezas por reparación, incluyendo las reparaciones sin piezas con coste cero.
--    SUM de ninguna fila da NULL y COALESCE lo cambia por 0.
SELECT reparaciones.id_reparacion, reparaciones.descripcion,
       COALESCE(SUM(piezas_reparacion.cantidad * piezas_reparacion.precio_unitario), 0) AS coste_piezas
FROM reparaciones
LEFT JOIN piezas_reparacion ON piezas_reparacion.id_reparacion = reparaciones.id_reparacion
GROUP BY reparaciones.id_reparacion, reparaciones.descripcion
ORDER BY reparaciones.id_reparacion;

-- 10. Reparaciones que no tienen piezas asociadas.
--     LEFT JOIN + IS NULL = se quedan las reparaciones que no encontraron pareja.
SELECT reparaciones.id_reparacion, reparaciones.descripcion, reparaciones.estado
FROM reparaciones
LEFT JOIN piezas_reparacion ON piezas_reparacion.id_reparacion = reparaciones.id_reparacion
WHERE piezas_reparacion.id_pieza IS NULL;

-- 10b. Alternativa con NOT EXISTS.
SELECT reparaciones.id_reparacion, reparaciones.descripcion, reparaciones.estado
FROM reparaciones
WHERE NOT EXISTS (SELECT 1 FROM piezas_reparacion
                  WHERE piezas_reparacion.id_reparacion = reparaciones.id_reparacion);

-- INNER JOIN vs LEFT JOIN:
--   INNER JOIN solo devuelve las filas que tienen pareja en las dos tablas.
--   LEFT JOIN devuelve TODAS las filas de la tabla de la izquierda. Si no hay pareja, las columnas
--   de la derecha salen a NULL. Un vehículo con un cliente inexistente NO sale en el INNER JOIN,
--   pero SÍ sale en un LEFT JOIN desde vehiculos (con el nombre del cliente a NULL).
-- GROUP BY agrupa filas para aplicar funciones como COUNT o SUM a cada grupo.
-- PRAGMA foreign_keys = ON activa las claves foráneas, pero solo en ESA conexión.
--   Con ellas activas: insertar un vehículo de un cliente que no existe, asociar una reparación a un
--   mecánico que no existe o borrar un cliente con vehículos da error (FOREIGN KEY constraint failed).
--   Sin ellas, SQLite lo permite. Por eso se activa en ConexionBD.abrir, que es por donde pasan todas.
