package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

/**
 * Lo que es igual en todos los DAO: recorrer las filas de un SELECT y apuntar los errores.
 * Cada DAO hijo abre y cierra su Connection y su PreparedStatement.
 */
public abstract class AbstractDao<T> extends ConexionBD {
    private static final Logger LOG = Logger.getLogger(AbstractDao.class.getName());

    protected AbstractDao(String url) {
        super(url);
    }

    /** Cada hijo convierte la fila actual del ResultSet en su objeto. Si falla, Optional.empty(). */
    protected abstract Optional<T> mapRsToClass(ResultSet rs);

    /** Ejecuta un SELECT y devuelve todas las filas en una lista. Nunca devuelve null. */
    protected List<T> consultar(PreparedStatement ps) {
        if (ps == null) return List.of();
        try (ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                Optional<T> entidad = mapRsToClass(rs);
                if (entidad.isEmpty()) return List.of(); // no devolver una lista a medias
                resultado.add(entidad.get());
            }
            return resultado;
        } catch (SQLException | RuntimeException e) {
            registrar("consultar", e);
            return List.of();
        }
    }

    /** Ejecuta un SELECT que devuelve como mucho una fila. */
    protected Optional<T> consultarUno(PreparedStatement ps) {
        if (ps == null) return Optional.empty();
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRsToClass(rs);
            }
            return Optional.empty();
        } catch (SQLException | RuntimeException e) {
            registrar("consultarUno", e);
            return Optional.empty();
        }
    }

    /** Apunta el error en el registro (Logger). */
    protected void registrar(String operacion, Exception e) {
        LOG.log(Level.WARNING, "Fallo JDBC: " + operacion, e);
    }
}
