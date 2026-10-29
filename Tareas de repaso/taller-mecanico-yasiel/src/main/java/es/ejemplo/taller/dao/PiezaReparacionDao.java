package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.PiezaReparacion;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class PiezaReparacionDao extends AbstractDao<PiezaReparacion> {
    public PiezaReparacionDao(String url) {
        super(url);
    }

    @Override
    protected Optional<PiezaReparacion> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new PiezaReparacion(
                    rs.getInt("id_pieza"),
                    rs.getInt("id_reparacion"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    rs.getBigDecimal("precio_unitario")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass pieza", e);
            return Optional.empty();
        }
    }

    /** Nombre obligatorio; reparación, cantidad y precio no pueden faltar (cantidad > 0 y precio >= 0 lo comprueba el CHECK). */
    private boolean esValido(PiezaReparacion pieza) {
        if (pieza == null) return false;
        if (pieza.getNombre() == null || pieza.getNombre().isBlank()) return false;
        if (pieza.getIdReparacion() == null) return false;
        if (pieza.getCantidad() == null || pieza.getPrecioUnitario() == null) return false;
        return true;
    }

    public List<PiezaReparacion> listar() {
        String sql = "SELECT * FROM piezas_reparacion ORDER BY id_pieza";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar piezas", e);
            return List.of();
        }
    }

    public Optional<PiezaReparacion> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM piezas_reparacion WHERE id_pieza = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorId pieza", e);
            return Optional.empty();
        }
    }

    /** Guarda una pieza nueva y devuelve su id (0 si no se pudo). */
    public int insertar(PiezaReparacion pieza) {
        if (!esValido(pieza)) return 0;
        String sql = "INSERT INTO piezas_reparacion (id_reparacion, nombre, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pieza.getIdReparacion());
            ps.setString(2, pieza.getNombre());
            ps.setInt(3, pieza.getCantidad());
            ps.setBigDecimal(4, pieza.getPrecioUnitario());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | RuntimeException e) {
            registrar("insertar pieza", e);
            return 0;
        }
    }

    /** Cambia los datos de una pieza. Devuelve cuántas filas cambiaron (1 o 0). */
    public int actualizar(PiezaReparacion pieza) {
        if (!esValido(pieza) || pieza.getIdPieza() == null) return 0;
        String sql = "UPDATE piezas_reparacion SET id_reparacion = ?, nombre = ?, cantidad = ?, precio_unitario = ? "
                + "WHERE id_pieza = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pieza.getIdReparacion());
            ps.setString(2, pieza.getNombre());
            ps.setInt(3, pieza.getCantidad());
            ps.setBigDecimal(4, pieza.getPrecioUnitario());
            ps.setInt(5, pieza.getIdPieza());
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("actualizar pieza", e);
            return 0;
        }
    }

    /** Borra una pieza. Devuelve 1 si la borró y 0 si no existía. */
    public int eliminar(Integer id) {
        if (id == null) return 0;
        String sql = "DELETE FROM piezas_reparacion WHERE id_pieza = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException | RuntimeException e) {
            registrar("eliminar pieza", e);
            return 0;
        }
    }

    public List<PiezaReparacion> listarPorReparacion(Integer idReparacion) {
        if (idReparacion == null) return List.of();
        String sql = "SELECT * FROM piezas_reparacion WHERE id_reparacion = ? ORDER BY id_pieza";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReparacion);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listarPorReparacion pieza", e);
            return List.of();
        }
    }

    /** Lo que cuestan las piezas de una reparación: suma de cantidad × precio. Sin piezas, 0 (nunca null). */
    public BigDecimal calcularCostePiezas(Integer idReparacion) {
        if (idReparacion == null) return BigDecimal.ZERO;
        // COALESCE cambia el NULL de SUM (cuando no hay filas) por 0
        String sql = "SELECT COALESCE(SUM(cantidad * precio_unitario), 0) AS total "
                + "FROM piezas_reparacion WHERE id_reparacion = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReparacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    if (total != null) return total;
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException | RuntimeException e) {
            registrar("calcularCostePiezas", e);
            return BigDecimal.ZERO;
        }
    }
}
