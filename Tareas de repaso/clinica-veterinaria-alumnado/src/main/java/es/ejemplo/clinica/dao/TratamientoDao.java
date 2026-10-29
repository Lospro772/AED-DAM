package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Tratamiento;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Lee, guarda, cambia y borra tratamientos. Cada método hace todo el trabajo él solo. */
public final class TratamientoDao implements ITratamientoDao {
    private final String url;

    public TratamientoDao(String url) {
        this.url = url;
    }

    /** Todos los tratamientos ordenados por id. Si no hay ninguno, lista vacía. */
    @Override
    public List<Tratamiento> listar() {
        String sql = "SELECT id, id_consulta, descripcion, cantidad, precio_unitario FROM tratamientos ORDER BY id";
        List<Tratamiento> tratamientos = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tratamientos.add(new Tratamiento(
                        rs.getInt("id"),
                        rs.getInt("id_consulta"),
                        rs.getString("descripcion"),
                        rs.getInt("cantidad"),
                        rs.getBigDecimal("precio_unitario")));
            }
            return tratamientos;
        } catch (SQLException e) {
            throw new DaoException("Error al listar tratamientos", e);
        }
    }

    /** El tratamiento con ese id, o Optional vacío si no existe. */
    @Override
    public Optional<Tratamiento> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, id_consulta, descripcion, cantidad, precio_unitario FROM tratamientos WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Tratamiento(
                            rs.getInt("id"),
                            rs.getInt("id_consulta"),
                            rs.getString("descripcion"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar el tratamiento " + id, e);
        }
    }

    /** Guarda un tratamiento nuevo y devuelve el id que le pone la base de datos. */
    @Override
    public int insertar(Tratamiento tratamiento) {
        if (tratamiento == null) {
            throw new NullPointerException("El tratamiento no puede ser null");
        }
        String sql = "INSERT INTO tratamientos (id_consulta, descripcion, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tratamiento.getIdConsulta());
            ps.setString(2, tratamiento.getDescripcion());
            ps.setInt(3, tratamiento.getCantidad());
            ps.setBigDecimal(4, tratamiento.getPrecioUnitario());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // la columna 1 es el id nuevo
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar tratamiento", e);
        }
    }

    /** Cambia los datos de un tratamiento que ya existe. Devuelve cuántas filas cambiaron (1 o 0). */
    @Override
    public int actualizar(Tratamiento tratamiento) {
        if (tratamiento == null) {
            throw new NullPointerException("El tratamiento no puede ser null");
        }
        if (tratamiento.getId() == null) throw new IllegalArgumentException("Para actualizar hace falta el id");
        String sql = "UPDATE tratamientos SET id_consulta = ?, descripcion = ?, cantidad = ?, precio_unitario = ? "
                + "WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tratamiento.getIdConsulta());
            ps.setString(2, tratamiento.getDescripcion());
            ps.setInt(3, tratamiento.getCantidad());
            ps.setBigDecimal(4, tratamiento.getPrecioUnitario());
            ps.setInt(5, tratamiento.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar el tratamiento " + tratamiento.getId(), e);
        }
    }

    /** Borra el tratamiento con ese id. Devuelve cuántas filas se borraron (1 o 0). */
    @Override
    public int eliminar(Integer id) {
        if (id == null) {
            throw new NullPointerException("El id no puede ser null");
        }
        String sql = "DELETE FROM tratamientos WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar el tratamiento " + id, e);
        }
    }

    /** Tratamientos de una consulta. */
    @Override
    public List<Tratamiento> listarPorConsulta(Integer idConsulta) {
        if (idConsulta == null) return List.of();
        String sql = "SELECT id, id_consulta, descripcion, cantidad, precio_unitario FROM tratamientos "
                + "WHERE id_consulta = ? ORDER BY id";
        List<Tratamiento> tratamientos = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tratamientos.add(new Tratamiento(
                            rs.getInt("id"),
                            rs.getInt("id_consulta"),
                            rs.getString("descripcion"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario")));
                }
            }
            return tratamientos;
        } catch (SQLException e) {
            throw new DaoException("Error al listar tratamientos de la consulta " + idConsulta, e);
        }
    }

    /** Lo que cuestan los tratamientos de una consulta: suma de cantidad × precio. Sin tratamientos, 0. */
    @Override
    public BigDecimal calcularCosteTratamientos(Integer idConsulta) {
        if (idConsulta == null) return BigDecimal.ZERO;
        // COALESCE cambia el NULL de SUM (cuando no hay filas) por 0
        String sql = "SELECT COALESCE(SUM(cantidad * precio_unitario), 0) AS total FROM tratamientos "
                + "WHERE id_consulta = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    if (total != null) return total;
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al calcular el coste de la consulta " + idConsulta, e);
        }
    }
}
