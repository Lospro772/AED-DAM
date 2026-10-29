package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Vehiculo;

import java.sql.*;
import java.util.*;

/** La clave es la matrícula, que es texto: se usa setString y no hay id generado. */
public class VehiculoDao extends AbstractDao<Vehiculo> {
    public VehiculoDao(String url) {
        super(url);
    }

    @Override
    protected Optional<Vehiculo> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Vehiculo(
                    rs.getString("matricula"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("anio"),
                    rs.getInt("id_cliente")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass vehiculo", e);
            return Optional.empty();
        }
    }

    /** Matrícula, marca y modelo obligatorios; año y dueño no pueden faltar. */
    private boolean esValido(Vehiculo vehiculo) {
        if (vehiculo == null) return false;
        if (vehiculo.getMatricula() == null || vehiculo.getMatricula().isBlank()) return false;
        if (vehiculo.getMarca() == null || vehiculo.getMarca().isBlank()) return false;
        if (vehiculo.getModelo() == null || vehiculo.getModelo().isBlank()) return false;
        if (vehiculo.getAnio() == null || vehiculo.getIdCliente() == null) return false;
        return true;
    }

    public List<Vehiculo> listar() {
        String sql = "SELECT * FROM vehiculos ORDER BY matricula";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar vehiculos", e);
            return List.of();
        }
    }

    public Optional<Vehiculo> buscarPorId(String matricula) {
        if (matricula == null || matricula.isBlank()) return Optional.empty();
        String sql = "SELECT * FROM vehiculos WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorId vehiculo", e);
            return Optional.empty();
        }
    }

    /** La matrícula la trae el vehículo, no la genera la base de datos: devuelve 1 si se guarda y 0 si no. */
    public int insertar(Vehiculo vehiculo) {
        if (!esValido(vehiculo)) return 0;
        String sql = "INSERT INTO vehiculos (matricula, marca, modelo, anio, id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMatricula());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo());
            ps.setInt(4, vehiculo.getAnio());
            ps.setInt(5, vehiculo.getIdCliente());
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("insertar vehiculo", e);
            return 0;
        }
    }

    /** Cambia los datos de un vehículo. Devuelve cuántas filas cambiaron (1 o 0). */
    public int actualizar(Vehiculo vehiculo) {
        if (!esValido(vehiculo)) return 0;
        String sql = "UPDATE vehiculos SET marca = ?, modelo = ?, anio = ?, id_cliente = ? WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMarca());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getAnio());
            ps.setInt(4, vehiculo.getIdCliente());
            ps.setString(5, vehiculo.getMatricula());
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("actualizar vehiculo", e);
            return 0;
        }
    }

    /** Borra un vehículo. Devuelve 1 si lo borró y 0 si no existía o tiene reparaciones. */
    public int eliminar(String matricula) {
        if (matricula == null || matricula.isBlank()) return 0;
        String sql = "DELETE FROM vehiculos WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("eliminar vehiculo", e);
            return 0;
        }
    }

    public List<Vehiculo> buscarPorCliente(Integer idCliente) {
        if (idCliente == null) return List.of();
        String sql = "SELECT * FROM vehiculos WHERE id_cliente = ? ORDER BY matricula";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorCliente vehiculo", e);
            return List.of();
        }
    }

    public List<Vehiculo> listarPorMarca(String marca) {
        if (marca == null || marca.isBlank()) return List.of();
        String sql = "SELECT * FROM vehiculos WHERE marca = ? ORDER BY matricula";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, marca);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listarPorMarca vehiculo", e);
            return List.of();
        }
    }
}
