package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Mecanico;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class MecanicoDao extends AbstractDao<Mecanico, Integer> {
    public MecanicoDao(String url) {
        super(url);
    }

    @Override
    protected String getTabla() {
        return "mecanicos";
    }

    @Override
    protected String getColumnaId() {
        return "id_mecanico";
    }

    @Override
    protected Optional<Mecanico> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Mecanico(
                    rs.getInt("id_mecanico"),
                    rs.getString("nombre"),
                    rs.getString("especialidad"),
                    rs.getBigDecimal("tarifa_hora")));
        } catch (SQLException e) {
            registrar("mapRsToClass mecanico", e);
            return Optional.empty();
        }
    }

    /** Nombre y especialidad obligatorios; la tarifa no puede faltar ni ser negativa. */
    private boolean esValido(Mecanico mecanico) {
        return mecanico != null
                && textoValido(mecanico.getNombre())
                && textoValido(mecanico.getEspecialidad())
                && mecanico.getTarifaHora() != null
                && mecanico.getTarifaHora().compareTo(BigDecimal.ZERO) >= 0;
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
            return insertarConId(ps);
        } catch (SQLException e) {
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
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("actualizar mecanico", e);
            return 0;
        }
    }

    public List<Mecanico> buscarPorEspecialidad(String especialidad) {
        return buscarPor("especialidad", especialidad);
    }
}
