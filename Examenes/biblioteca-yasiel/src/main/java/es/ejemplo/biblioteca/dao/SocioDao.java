package es.ejemplo.biblioteca.dao;

import java.sql.*;
import java.util.*;

import es.ejemplo.biblioteca.conexion.ConexionBD;
import es.ejemplo.biblioteca.modelo.*;

/**
 * Implementación pendiente: el alumnado debe completar los métodos JDBC.
 */
public final class SocioDao implements ISocioDao {
    private final String url;
    private PreparedStatement ps;

    public SocioDao(String url) {
        this.url = Objects.requireNonNull(url);
    }

    @Override
    public List<Socio> listar() {
        List<Socio> socios = new ArrayList<>();
        String sql = "SELECT id, nombre, email, fecha_alta FROM socios";

        try (Connection connection = ConexionBD.abrir(url);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Optional.of(new Socio(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("fecha_alta")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return socios;
    }

    @Override
    public Optional<Socio> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, nombre, email, fecha_alta FROM libros WHERE id = ?";

        try (Connection connection = ConexionBD.abrir(url);

             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Socio(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("fecha_alta")));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el socio", e);
        }
    }

    @Override
    public int insertar(Socio entidad) {
        if (entidad == null) {
            throw new NullPointerException("Entidad nula");
        }

        String sql = "INSERT INTO socios (nombre, email, fecha_alta)" + "VALUES (?, ?, ?)";

        try (Connection connection = ConexionBD.abrir(url);

             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEmail());
            ps.setString(3, entidad.getFechaAlta());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el socio", e);
        }
    }

    @Override
    public int actualizar(Socio entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("Entidad nula");
        }

        String sql = "UPDATE socios SET nombre = ?, email = ?, fecha_alta = ?, id = ?";

        try (Connection connection = ConexionBD.abrir(url);

             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEmail());
            ps.setString(3, entidad.getFechaAlta());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el socio", e);
        }
    }

    @Override
    public int eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Entidad nula");
        }

        String sql = "DELETE FROM socios WHERE id = ?";

        try (Connection connection = ConexionBD.abrir(url);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            try {
                ps.setInt(1, id);
             return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el socio", e);
        }
    }

    @Override
    public Optional<Socio> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, nombre, email, fecha_alta FROM libros WHERE id = ?";

        try (Connection connection = ConexionBD.abrir(url);

             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Socio(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("fecha_alta")));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Error al buscar por email", e);
        }
    }

    @Override
    public List<Socio> listarAltasDesde(String fecha) {
        List<Socio> Socio = new ArrayList<>();
        String sql = "SELECT id, nombre, email, fecha_alta FROM libros WHERE id = ?";

        try (Connection connection = ConexionBD.abrir(url);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fecha);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Socio;
    }
}
