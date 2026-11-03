package es.ejemplo.biblioteca.dao;

import java.sql.*;
import java.util.*;

import es.ejemplo.biblioteca.conexion.ConexionBD;
import es.ejemplo.biblioteca.modelo.*;

public final class LibroDao implements ILibroDao {
    private final String url;
    private PreparedStatement ps;

    public LibroDao(String url) {
        this.url = Objects.requireNonNull(url);
    }

    @Override
    public List<Libro> listar() {
        List<Libro> libros = new ArrayList<>();
        String sql = "SELECT id, titulo, autor, anio_publicacion FROM libros";

        try (Connection connection = ConexionBD.abrir(url);
            PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Optional.of(new Libro(
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getInt("anio_publicacion")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return libros;
    }

    @Override
    public Optional<Libro> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, titulo, autor, anio_publicacion FROM libros WHERE id = ?";

        try (Connection connection = ConexionBD.abrir(url);

        PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Libro(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getInt("anio_publicacion")));
            }
            return Optional.empty();
            } catch (SQLException e) {
            throw new DaoException("Error al insertar el libro", e);
        }
    }

    @Override
    public int insertar(Libro entidad) {
        if (entidad == null) {
            throw new NullPointerException("Entidad nula");
        }

        String sql = "INSERT INTO libros (titulo, autor, anio_publicacion)" + "VALUES (?, ?, ?)";

        try (Connection connection = ConexionBD.abrir(url);

        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getTitulo());
            ps.setString(2, entidad.getAutor());
            ps.setInt(3, entidad.getAnioPublicacion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el libro", e);
        }
    }

    @Override
    public int actualizar(Libro entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("Entidad nula");
        }

        String sql = "UPDATE libros SET titulo = ?, autor = ?, anio_publicacion = ?, id = ?";

        try (Connection connection = ConexionBD.abrir(url);

             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getTitulo());
            ps.setString(2, entidad.getAutor());
            ps.setInt(3, entidad.getAnioPublicacion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el libro", e);
        }
    }

    @Override
    public Object eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Entidad nula");
        }

        String sql = "DELETE FROM libros WHERE id = ?";


        try (Connection connection = ConexionBD.abrir(url);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.getGeneratedKeys()){
                ps.setInt(1, id);
                if (rs.next()) {
                    return ps.executeUpdate();
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar el libro", e);
        }
    }
}
