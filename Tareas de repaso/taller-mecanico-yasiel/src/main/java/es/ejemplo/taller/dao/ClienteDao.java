package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Cliente;

import java.sql.*;
import java.util.*;

public class ClienteDao extends AbstractDao<Cliente> {
    public ClienteDao(String url) {
        super(url);
    }

    @Override
    protected Optional<Cliente> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass cliente", e);
            return Optional.empty();
        }
    }

    /** Nombre y email obligatorios; el teléfono puede faltar. */
    private boolean esValido(Cliente cliente) {
        if (cliente == null) return false;
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) return false;
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) return false;
        return true;
    }

    public List<Cliente> listar() {
        String sql = "SELECT * FROM clientes ORDER BY id_cliente";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar clientes", e);
            return List.of();
        }
    }

    public Optional<Cliente> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorId cliente", e);
            return Optional.empty();
        }
    }

    /** Guarda un cliente nuevo y devuelve su id (0 si no se pudo). */
    public int insertar(Cliente cliente) {
        if (!esValido(cliente)) return 0;
        String sql = "INSERT INTO clientes (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | RuntimeException e) {
            registrar("insertar cliente", e);
            return 0;
        }
    }

    /** Cambia los datos de un cliente. Devuelve cuántas filas cambiaron (1 o 0). */
    public int actualizar(Cliente cliente) {
        if (!esValido(cliente) || cliente.getIdCliente() == null) return 0;
        String sql = "UPDATE clientes SET nombre = ?, email = ?, telefono = ? WHERE id_cliente = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            ps.setInt(4, cliente.getIdCliente());
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("actualizar cliente", e);
            return 0;
        }
    }

    /** Borra un cliente. Devuelve 1 si lo borró y 0 si no existía o tiene vehículos. */
    public int eliminar(Integer id) {
        if (id == null) return 0;
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("eliminar cliente", e);
            return 0;
        }
    }
}
