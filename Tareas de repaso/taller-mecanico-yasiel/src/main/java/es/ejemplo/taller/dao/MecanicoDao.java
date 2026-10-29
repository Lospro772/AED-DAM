package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Mecanico;

import java.sql.*;
import java.util.*;

public class MecanicoDao extends AbstractDao<Mecanico> {
    public MecanicoDao(String url) {
        super(url);
    }

    @Override
    protected Optional<Mecanico> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Mecanico(
                    rs.getInt("id_mecanico"),
                    rs.getString("nombre"),
                    rs.getString("especialidad"),
                    rs.getBigDecimal("tarifa_hora")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass mecanico", e);
            return Optional.empty();
        }
    }

    /** Nombre y especialidad obligatorios y la tarifa no puede faltar (que no sea negativa lo comprueba el CHECK). */
    private boolean esValido(Mecanico mecanico) {
        if (mecanico == null) return false;
        if (mecanico.getNombre() == null || mecanico.getNombre().isBlank()) return false;
        if (mecanico.getEspecialidad() == null || mecanico.getEspecialidad().isBlank()) return false;
        if (mecanico.getTarifaHora() == null) return false;
        return true;
    }

    public List<Mecanico> listar() {
        String sql = "SELECT * FROM mecanicos ORDER BY id_mecanico";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar mecanicos", e);
            return List.of();
        }
    }

    public Optional<Mecanico> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM mecanicos WHERE id_mecanico = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorId mecanico", e);
            return Optional.empty();
        }
    }

    /** Guarda un mecánico nuevo y devuelve su id (0 si no se pudo). */
    public int insertar(Mecanico mecanico) {
        if (!esValido(mecanico)) return 0;
        String sql = "INSERT INTO mecanicos (nombre, especialidad, tarifa_hora) VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mecanico.getNombre());
            ps.setString(2, mecanico.getEspecialidad());
            ps.setBigDecimal(3, mecanico.getTarifaHora());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | RuntimeException e) {
            registrar("insertar mecanico", e);
            return 0;
        }
    }

    /** Cambia los datos de un mecánico. Devuelve cuántas filas cambiaron (1 o 0). */
    public int actualizar(Mecanico mecanico) {
        if (!esValido(mecanico) || mecanico.getIdMecanico() == null) return 0;
        String sql = "UPDATE mecanicos SET nombre = ?, especialidad = ?, tarifa_hora = ? WHERE id_mecanico = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mecanico.getNombre());
            ps.setString(2, mecanico.getEspecialidad());
            ps.setBigDecimal(3, mecanico.getTarifaHora());
            ps.setInt(4, mecanico.getIdMecanico());
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("actualizar mecanico", e);
            return 0;
        }
    }

    /** Borra un mecánico. Devuelve 1 si lo borró y 0 si no existía o tiene reparaciones. */
    public int eliminar(Integer id) {
        if (id == null) return 0;
        String sql = "DELETE FROM mecanicos WHERE id_mecanico = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("eliminar mecanico", e);
            return 0;
        }
    }

    public List<Mecanico> buscarPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) return List.of();
        String sql = "SELECT * FROM mecanicos WHERE especialidad = ? ORDER BY id_mecanico";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, especialidad);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorEspecialidad mecanico", e);
            return List.of();
        }
    }
}
