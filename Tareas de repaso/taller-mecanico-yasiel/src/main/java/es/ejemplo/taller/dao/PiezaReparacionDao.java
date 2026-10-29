package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.PiezaReparacion;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class PiezaReparacionDao extends AbstractDao<PiezaReparacion, Integer> {
    public PiezaReparacionDao(String url) {
        super(url);
    }

    @Override
    protected String getTabla() {
        return "piezas_reparacion";
    }

    @Override
    protected String getColumnaId() {
        return "id_pieza";
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
        } catch (SQLException e) {
            registrar("mapRsToClass pieza", e);
            return Optional.empty();
        }
    }

    /** Reparación y nombre obligatorios; cantidad mayor que 0; precio no negativo. */
    private boolean esValido(PiezaReparacion pieza) {
        return pieza != null
                && pieza.getIdReparacion() != null
                && textoValido(pieza.getNombre())
                && pieza.getCantidad() != null && pieza.getCantidad() > 0
                && pieza.getPrecioUnitario() != null
                && pieza.getPrecioUnitario().compareTo(BigDecimal.ZERO) >= 0;
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
            return insertarConId(ps);
        } catch (SQLException e) {
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
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("actualizar pieza", e);
            return 0;
        }
    }

    public List<PiezaReparacion> listarPorReparacion(Integer idReparacion) {
        return buscarPor("id_reparacion", idReparacion);
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
        } catch (SQLException e) {
            registrar("calcularCostePiezas", e);
            return BigDecimal.ZERO;
        }
    }
}
