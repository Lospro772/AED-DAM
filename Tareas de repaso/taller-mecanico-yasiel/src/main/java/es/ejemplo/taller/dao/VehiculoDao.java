package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Vehiculo;

import java.sql.*;
import java.util.*;

/** La clave es la matrícula, que es texto: por eso ID = String. */
public class VehiculoDao extends AbstractDao<Vehiculo, String> {
    public VehiculoDao(String url) {
        super(url);
    }

    @Override
    protected String getTabla() {
        return "vehiculos";
    }

    @Override
    protected String getColumnaId() {
        return "matricula";
    }

    @Override
    protected Optional<Vehiculo> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Vehiculo(
                    rs.getString("matricula"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("anio"),
                    rs.getInt("id_cliente")));
        } catch (SQLException e) {
            registrar("mapRsToClass vehiculo", e);
            return Optional.empty();
        }
    }

    /** Matrícula, marca y modelo obligatorios; año y dueño no pueden faltar. */
    private boolean esValido(Vehiculo vehiculo) {
        return vehiculo != null
                && textoValido(vehiculo.getMatricula())
                && textoValido(vehiculo.getMarca())
                && textoValido(vehiculo.getModelo())
                && vehiculo.getAnio() != null
                && vehiculo.getIdCliente() != null;
    }

    /** La matrícula la trae el vehículo, no la genera la base de datos: devuelve 1 si se guarda y 0 si no. */
    public int insertar(Vehiculo vehiculo) {
        if (!esValido(vehiculo)) return 0;
        String sql = "INSERT INTO vehiculos (matricula, marca, modelo, anio, id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMatricula());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo());
            ps.setInt(4, vehiculo.getAnio());
            ps.setInt(5, vehiculo.getIdCliente());
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("insertar vehiculo", e);
            return 0;
        }
    }

    /** Cambia los datos de un vehículo. Devuelve cuántas filas cambiaron (1 o 0). */
    public int actualizar(Vehiculo vehiculo) {
        if (!esValido(vehiculo)) return 0;
        String sql = "UPDATE vehiculos SET marca = ?, modelo = ?, anio = ?, id_cliente = ? WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMarca());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getAnio());
            ps.setInt(4, vehiculo.getIdCliente());
            ps.setString(5, vehiculo.getMatricula());
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("actualizar vehiculo", e);
            return 0;
        }
    }

    public List<Vehiculo> buscarPorCliente(Integer idCliente) {
        return buscarPor("id_cliente", idCliente);
    }

    public List<Vehiculo> listarPorMarca(String marca) {
        return buscarPor("marca", marca);
    }
}
