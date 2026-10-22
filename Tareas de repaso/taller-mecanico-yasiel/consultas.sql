-- 1. Lista los clientes ordenados por nombre.
SELECT id_clientes, nombre FROM clientes ORDER BY nombre;
-- 2. Consulta matrícula, marca, modelo y año de los vehículos de una marca elegida.
SELECT matricula, marca, modelo, anio FROM vehiculos;
-- 3. Obtén los vehículos de un cliente utilizando su identificador.
SELECT
-- 4. Lista los mecánicos de una especialidad y ordénalos por tarifa.
SELECT
-- 5. Obtén las reparaciones pendientes, ordenadas por fecha.
SELECT
-- 6. Muestra la matrícula de cada vehículo y el nombre de su propietario. Resuélvelo una vez con `WHERE` y otra con `INNER JOIN`.
SELECT
-- 7. Muestra las reparaciones con el nombre del cliente, la matrícula y el nombre del mecánico.
SELECT
-- 8. Cuenta los vehículos de cada cliente, incluyendo clientes sin vehículos.
SELECT
-- 9. Calcula el coste de piezas por reparación, incluyendo las reparaciones sin piezas con coste cero.
SELECT
-- 10. Obtén las reparaciones que no tienen piezas asociadas.
SELECT