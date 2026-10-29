package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Cliente;

import java.sql.*;
import java.util.*;

public class ClienteDao extends AbstractDao<Cliente, Integer> {
    public ClienteDao(String url) {
        super(url);
    }

    @Override
    protected String getTabla() {
        return "clientes";
    }

    @Override
    protected String getColumnaId() {
        return "id_cliente";
    }

    @Override
    protected Optional<Cliente> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono")));
        } catch (SQLException e) {
            registrar("mapRsToClass cliente", e);
            return Optional.empty();
        }
    }

    /** Nombre y email obligatorios; el teléfono puede faltar. */
    private boolean esValido(Cliente cliente) {
        return cliente != null
                && textoValido(cliente.getNombre())
                && textoValido(cliente.getEmail());
    }

    // listar(), buscarPorId(Integer) y eliminar(Integer) no se escriben aquí: vienen de AbstractDao

    /** Guarda un cliente nuevo y devuelve su id (0 si no se pudo). */
    public int insertar(Cliente cliente) {
        if (!esValido(cliente)) return 0;
        String sql = "INSERT INTO clientes (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            return insertarConId(ps);
        } catch (SQLException e) {
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
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("actualizar cliente", e);
            return 0;
        }
    }
}
