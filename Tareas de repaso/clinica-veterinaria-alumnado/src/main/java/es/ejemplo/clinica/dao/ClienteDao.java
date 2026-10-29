package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Cliente;

import java.sql.*;
import java.util.*;

/** Lee, guarda, cambia y borra clientes. Cada método hace todo el trabajo él solo. */
public final class ClienteDao implements IClienteDao {
    private final String url;

    public ClienteDao(String url) {
        this.url = url;
    }

    /** Todos los clientes ordenados por id. Si no hay ninguno, lista vacía. */
    @Override
    public List<Cliente> listar() {
        String sql = "SELECT id, nombre, email, telefono FROM clientes ORDER BY id";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { // mientras quede otra fila por leer
                clientes.add(new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("telefono")));
            }
            return clientes;
        } catch (SQLException e) {
            throw new DaoException("Error al listar clientes", e);
        }
    }

    /** El cliente con ese id, o Optional vacío si no existe. */
    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, email, telefono FROM clientes WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); // rellena el primer ? con el id
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("telefono")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar el cliente " + id, e);
        }
    }

    /** Guarda un cliente nuevo y devuelve el id que le pone la base de datos. */
    @Override
    public int insertar(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser null");
        String sql = "INSERT INTO clientes (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // el número es la posición del ? (empieza en 1)
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // la columna 1 es el id nuevo
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar cliente", e);
        }
    }

    /** Cambia los datos de un cliente que ya existe. Devuelve cuántas filas cambiaron (1 o 0). */
    @Override
    public int actualizar(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente no puede ser null");
        if (cliente.getId() == null) throw new IllegalArgumentException("Para actualizar hace falta el id");
        String sql = "UPDATE clientes SET nombre = ?, email = ?, telefono = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            ps.setInt(4, cliente.getId()); // el id va el último, como en el WHERE
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar el cliente " + cliente.getId(), e);
        }
    }

    /** Borra el cliente con ese id. Devuelve cuántas filas se borraron (1 o 0). */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar el cliente " + id, e);
        }
    }
}
