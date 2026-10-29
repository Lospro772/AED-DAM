package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Reparacion;

import java.sql.*;
import java.util.*;

public class ReparacionDao extends AbstractDao<Reparacion> {
    public ReparacionDao(String url) {
        super(url);
    }

    @Override
    protected Optional<Reparacion> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Reparacion(
                    rs.getInt("id_reparacion"),
                    rs.getString("matricula"),
                    rs.getInt("id_mecanico"),
                    rs.getString("descripcion"),
                    rs.getString("fecha"),
                    rs.getBigDecimal("horas_trabajo"),
                    rs.getString("estado")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass reparacion", e);
            return Optional.empty();
        }
    }

    /**
     * Matrícula, descripción y fecha obligatorias; mecánico, horas y estado no pueden faltar.
     * Que el estado sea PENDIENTE/EN_PROCESO/FINALIZADA y las horas no negativas lo comprueba el CHECK.
     */
    private boolean esValido(Reparacion reparacion) {
        if (reparacion == null) return false;
        if (reparacion.getMatricula() == null || reparacion.getMatricula().isBlank()) return false;
        if (reparacion.getDescripcion() == null || reparacion.getDescripcion().isBlank()) return false;
        if (reparacion.getFecha() == null || reparacion.getFecha().isBlank()) return false;
        if (reparacion.getIdMecanico() == null) return false;
        if (reparacion.getHorasTrabajo() == null || reparacion.getEstado() == null) return false;
        return true;
    }

    public List<Reparacion> listar() {
        String sql = "SELECT * FROM reparaciones ORDER BY id_reparacion";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar reparaciones", e);
            return List.of();
        }
    }

    public Optional<Reparacion> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM reparaciones WHERE id_reparacion = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorId reparacion", e);
            return Optional.empty();
        }
    }

    /** Guarda una reparación nueva y devuelve su id (0 si no se pudo). */
    public int insertar(Reparacion reparacion) {
        if (!esValido(reparacion)) return 0;
        String sql = "INSERT INTO reparaciones (matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reparacion.getMatricula());
            ps.setInt(2, reparacion.getIdMecanico());
            ps.setString(3, reparacion.getDescripcion());
            ps.setString(4, reparacion.getFecha());
            ps.setBigDecimal(5, reparacion.getHorasTrabajo());
            ps.setString(6, reparacion.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | RuntimeException e) {
            registrar("insertar reparacion", e);
            return 0;
        }
    }

    /** Cambia los datos de una reparación. Devuelve cuántas filas cambiaron (1 o 0). */
    public int actualizar(Reparacion reparacion) {
        if (!esValido(reparacion) || reparacion.getIdReparacion() == null) return 0;
        String sql = "UPDATE reparaciones SET matricula = ?, id_mecanico = ?, descripcion = ?, fecha = ?, "
                + "horas_trabajo = ?, estado = ? WHERE id_reparacion = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reparacion.getMatricula());
            ps.setInt(2, reparacion.getIdMecanico());
            ps.setString(3, reparacion.getDescripcion());
            ps.setString(4, reparacion.getFecha());
            ps.setBigDecimal(5, reparacion.getHorasTrabajo());
            ps.setString(6, reparacion.getEstado());
            ps.setInt(7, reparacion.getIdReparacion());
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("actualizar reparacion", e);
            return 0;
        }
    }

    /** Borra una reparación. Devuelve 1 si la borró y 0 si no existía o tiene piezas. */
    public int eliminar(Integer id) {
        if (id == null) return 0;
        String sql = "DELETE FROM reparaciones WHERE id_reparacion = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("eliminar reparacion", e);
            return 0;
        }
    }

    public List<Reparacion> buscarPorVehiculo(String matricula) {
        if (matricula == null || matricula.isBlank()) return List.of();
        String sql = "SELECT * FROM reparaciones WHERE matricula = ? ORDER BY id_reparacion";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorVehiculo reparacion", e);
            return List.of();
        }
    }

    public List<Reparacion> buscarPorMecanico(Integer idMecanico) {
        if (idMecanico == null) return List.of();
        String sql = "SELECT * FROM reparaciones WHERE id_mecanico = ? ORDER BY id_reparacion";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMecanico);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorMecanico reparacion", e);
            return List.of();
        }
    }

    public List<Reparacion> listarPendientes() {
        String sql = "SELECT * FROM reparaciones WHERE estado = 'PENDIENTE' ORDER BY id_reparacion";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listarPendientes reparacion", e);
            return List.of();
        }
    }
}
