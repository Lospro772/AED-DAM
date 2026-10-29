package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Reparacion;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ReparacionDao extends AbstractDao<Reparacion, Integer> {
    private static final Set<String> ESTADOS_PERMITIDOS = Set.of("PENDIENTE", "EN_PROCESO", "FINALIZADA");

    public ReparacionDao(String url) {
        super(url);
    }

    @Override
    protected String getTabla() {
        return "reparaciones";
    }

    @Override
    protected String getColumnaId() {
        return "id_reparacion";
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
        } catch (SQLException e) {
            registrar("mapRsToClass reparacion", e);
            return Optional.empty();
        }
    }

    /** Fecha con formato AAAA-MM-DD y que exista de verdad (no vale 2026-02-30). */
    private boolean fechaValida(String fecha) {
        if (fecha == null) return false;
        try {
            LocalDate.parse(fecha);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean esValido(Reparacion reparacion) {
        return reparacion != null
                && textoValido(reparacion.getMatricula())
                && reparacion.getIdMecanico() != null
                && textoValido(reparacion.getDescripcion())
                && fechaValida(reparacion.getFecha())
                && reparacion.getHorasTrabajo() != null
                && reparacion.getHorasTrabajo().compareTo(BigDecimal.ZERO) >= 0
                && ESTADOS_PERMITIDOS.contains(reparacion.getEstado());
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
            return insertarConId(ps);
        } catch (SQLException e) {
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
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("actualizar reparacion", e);
            return 0;
        }
    }

    public List<Reparacion> buscarPorVehiculo(String matricula) {
        return buscarPor("matricula", matricula);
    }

    public List<Reparacion> buscarPorMecanico(Integer idMecanico) {
        return buscarPor("id_mecanico", idMecanico);
    }

    public List<Reparacion> listarPendientes() {
        return buscarPor("estado", "PENDIENTE");
    }
}
