package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Consulta;

import java.sql.*;
import java.util.*;

/** Lee, guarda, cambia y borra consultas (citas). Cada método hace todo el trabajo él solo. */
public final class ConsultaDao implements IConsultaDao {
    private final String url;

    public ConsultaDao(String url) {
        this.url = url;
    }

    /** Todas las consultas ordenadas por id. Si no hay ninguna, lista vacía. */
    @Override
    public List<Consulta> listar() {
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas ORDER BY id";
        List<Consulta> consultas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                consultas.add(new Consulta(
                        rs.getInt("id"),
                        rs.getInt("id_mascota"),
                        rs.getInt("id_veterinario"),
                        rs.getString("fecha"),
                        rs.getBigDecimal("horas"),
                        rs.getString("estado")));
            }
            return consultas;
        } catch (SQLException e) {
            throw new DaoException("Error al listar consultas", e);
        }
    }

    /** La consulta con ese id, o Optional vacío si no existe. */
    @Override
    public Optional<Consulta> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Consulta(
                            rs.getInt("id"),
                            rs.getInt("id_mascota"),
                            rs.getInt("id_veterinario"),
                            rs.getString("fecha"),
                            rs.getBigDecimal("horas"),
                            rs.getString("estado")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar la consulta " + id, e);
        }
    }

    /** Guarda una consulta nueva y devuelve el id que le pone la base de datos. */
    @Override
    public int insertar(Consulta consulta) {
        Objects.requireNonNull(consulta, "La consulta no puede ser null");
        String sql = "INSERT INTO consultas (id_mascota, id_veterinario, fecha, horas, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, consulta.getIdMascota());
            ps.setInt(2, consulta.getIdVeterinario());
            ps.setString(3, consulta.getFecha());
            ps.setBigDecimal(4, consulta.getHoras());
            ps.setString(5, consulta.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // la columna 1 es el id nuevo
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar consulta", e);
        }
    }

    /** Cambia los datos de una consulta que ya existe. Devuelve cuántas filas cambiaron (1 o 0). */
    @Override
    public int actualizar(Consulta consulta) {
        Objects.requireNonNull(consulta, "La consulta no puede ser null");
        if (consulta.getId() == null) throw new IllegalArgumentException("Para actualizar hace falta el id");
        String sql = "UPDATE consultas SET id_mascota = ?, id_veterinario = ?, fecha = ?, horas = ?, estado = ? "
                + "WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, consulta.getIdMascota());
            ps.setInt(2, consulta.getIdVeterinario());
            ps.setString(3, consulta.getFecha());
            ps.setBigDecimal(4, consulta.getHoras());
            ps.setString(5, consulta.getEstado());
            ps.setInt(6, consulta.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar la consulta " + consulta.getId(), e);
        }
    }

    /** Borra la consulta con ese id. Devuelve cuántas filas se borraron (1 o 0). */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        String sql = "DELETE FROM consultas WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar la consulta " + id, e);
        }
    }

    /** Consultas de una mascota, de la más antigua a la más nueva. */
    @Override
    public List<Consulta> buscarPorMascota(Integer idMascota) {
        if (idMascota == null) return List.of();
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas "
                + "WHERE id_mascota = ? ORDER BY fecha, id";
        List<Consulta> consultas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultas.add(new Consulta(
                            rs.getInt("id"),
                            rs.getInt("id_mascota"),
                            rs.getInt("id_veterinario"),
                            rs.getString("fecha"),
                            rs.getBigDecimal("horas"),
                            rs.getString("estado")));
                }
            }
            return consultas;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar consultas de la mascota " + idMascota, e);
        }
    }

    /** Consultas de un veterinario, de la más antigua a la más nueva. */
    @Override
    public List<Consulta> buscarPorVeterinario(Integer idVeterinario) {
        if (idVeterinario == null) return List.of();
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas "
                + "WHERE id_veterinario = ? ORDER BY fecha, id";
        List<Consulta> consultas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVeterinario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultas.add(new Consulta(
                            rs.getInt("id"),
                            rs.getInt("id_mascota"),
                            rs.getInt("id_veterinario"),
                            rs.getString("fecha"),
                            rs.getBigDecimal("horas"),
                            rs.getString("estado")));
                }
            }
            return consultas;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar consultas del veterinario " + idVeterinario, e);
        }
    }

    /** Solo las consultas con estado PROGRAMADA, ordenadas por fecha. */
    @Override
    public List<Consulta> listarProgramadas() {
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas "
                + "WHERE estado = ? ORDER BY fecha, id";
        List<Consulta> consultas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "PROGRAMADA");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultas.add(new Consulta(
                            rs.getInt("id"),
                            rs.getInt("id_mascota"),
                            rs.getInt("id_veterinario"),
                            rs.getString("fecha"),
                            rs.getBigDecimal("horas"),
                            rs.getString("estado")));
                }
            }
            return consultas;
        } catch (SQLException e) {
            throw new DaoException("Error al listar consultas programadas", e);
        }
    }
}
