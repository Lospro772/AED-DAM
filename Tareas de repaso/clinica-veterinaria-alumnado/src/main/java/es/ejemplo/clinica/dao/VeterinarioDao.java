package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Veterinario;

import java.sql.*;
import java.util.*;

/** Lee, guarda, cambia y borra veterinarios. Cada método hace todo el trabajo él solo. */
public final class VeterinarioDao implements IVeterinarioDao {
    private final String url;

    public VeterinarioDao(String url) {
        this.url = url;
    }

    /** Todos los veterinarios ordenados por id. Si no hay ninguno, lista vacía. */
    @Override
    public List<Veterinario> listar() {
        String sql = "SELECT id, nombre, especialidad, tarifa_hora FROM veterinarios ORDER BY id";
        List<Veterinario> veterinarios = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                veterinarios.add(new Veterinario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("especialidad"),
                        rs.getBigDecimal("tarifa_hora")));
            }
            return veterinarios;
        } catch (SQLException e) {
            throw new DaoException("Error al listar veterinarios", e);
        }
    }

    /** El veterinario con ese id, o Optional vacío si no existe. */
    @Override
    public Optional<Veterinario> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, especialidad, tarifa_hora FROM veterinarios WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Veterinario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("especialidad"),
                            rs.getBigDecimal("tarifa_hora")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar el veterinario " + id, e);
        }
    }

    /** Guarda un veterinario nuevo y devuelve el id que le pone la base de datos. */
    @Override
    public int insertar(Veterinario veterinario) {
        if (veterinario == null) {
            throw new NullPointerException("El veterinario no puede ser null");
        }
        String sql = "INSERT INTO veterinarios (nombre, especialidad, tarifa_hora) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, veterinario.getNombre());
            ps.setString(2, veterinario.getEspecialidad());
            ps.setBigDecimal(3, veterinario.getTarifaHora());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // la columna 1 es el id nuevo
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar veterinario", e);
        }
    }

    /** Cambia los datos de un veterinario que ya existe. Devuelve cuántas filas cambiaron (1 o 0). */
    @Override
    public int actualizar(Veterinario veterinario) {
        if (veterinario == null) {
            throw new NullPointerException("El veterinario no puede ser null");
        }
        if (veterinario.getId() == null) throw new IllegalArgumentException("Para actualizar hace falta el id");
        String sql = "UPDATE veterinarios SET nombre = ?, especialidad = ?, tarifa_hora = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, veterinario.getNombre());
            ps.setString(2, veterinario.getEspecialidad());
            ps.setBigDecimal(3, veterinario.getTarifaHora());
            ps.setInt(4, veterinario.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar el veterinario " + veterinario.getId(), e);
        }
    }

    /** Borra el veterinario con ese id. Devuelve cuántas filas se borraron (1 o 0). */
    @Override
    public int eliminar(Integer id) {
        if (id == null) {
            throw new NullPointerException("El id no puede ser null");
        }
        String sql = "DELETE FROM veterinarios WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar el veterinario " + id, e);
        }
    }

    /** Veterinarios de una especialidad, del más barato al más caro. */
    @Override
    public List<Veterinario> buscarPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) return List.of();
        String sql = "SELECT id, nombre, especialidad, tarifa_hora FROM veterinarios "
                + "WHERE especialidad = ? ORDER BY tarifa_hora, id";
        List<Veterinario> veterinarios = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, especialidad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    veterinarios.add(new Veterinario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("especialidad"),
                            rs.getBigDecimal("tarifa_hora")));
                }
            }
            return veterinarios;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar veterinarios por especialidad", e);
        }
    }
}
