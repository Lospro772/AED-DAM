package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Molde común de todos los DAO del taller: aquí está lo que todos hacen igual.
 *  - T  = el tipo de objeto que maneja el DAO (Cliente, Vehiculo, ...)
 *  - ID = el tipo de su clave: Integer para los ids automáticos, String para la matrícula
 * Cada DAO hijo solo dice: su tabla, su columna id y cómo pasar una fila a objeto.
 * Si algo falla: se apunta el error y se devuelve lista vacía, Optional vacío o 0.
 */
public abstract class AbstractDao<T, ID> extends ConexionBD {
    private static final Logger LOG = Logger.getLogger(AbstractDao.class.getName());

    protected AbstractDao(String url) {
        super(url);
    }

    // ===== Lo que cada DAO hijo está obligado a escribir =====

    /** Nombre de la tabla, por ejemplo "clientes". */
    protected abstract String getTabla();

    /** Columna que hace de id, por ejemplo "id_cliente". */
    protected abstract String getColumnaId();

    /** Pasa la fila en la que está el ResultSet a un objeto. Si falla, Optional vacío. */
    protected abstract Optional<T> mapRsToClass(ResultSet rs);

    // ===== Operaciones que sirven igual para todas las tablas =====

    /** Todas las filas de la tabla, ordenadas por id. */
    public List<T> listar() {
        String sql = "SELECT * FROM " + getTabla() + " ORDER BY " + getColumnaId();
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException e) {
            registrar("listar " + getTabla(), e);
            return List.of();
        }
    }

    /** La fila con ese id, o Optional vacío si no existe. */
    public Optional<T> buscarPorId(ID id) {
        if (!idValido(id)) return Optional.empty();
        String sql = "SELECT * FROM " + getTabla() + " WHERE " + getColumnaId() + " = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, id); // setObject vale tanto para Integer como para String
            return consultarUno(ps);
        } catch (SQLException e) {
            registrar("buscarPorId " + getTabla(), e);
            return Optional.empty();
        }
    }

    /** Borra la fila con ese id. Devuelve cuántas filas se borraron (1 o 0). */
    public int eliminar(ID id) {
        if (!idValido(id)) return 0;
        String sql = "DELETE FROM " + getTabla() + " WHERE " + getColumnaId() + " = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("eliminar " + getTabla(), e);
            return 0;
        }
    }

    /** Filas cuya columna es exactamente igual al valor buscado (WHERE columna = valor). */
    protected List<T> buscarPor(String columna, Object valorBuscado) {
        if (valorBuscado == null) return List.of();
        if (valorBuscado instanceof String texto && texto.isBlank()) return List.of();
        String sql = "SELECT * FROM " + getTabla() + " WHERE " + columna + " = ? ORDER BY " + getColumnaId();
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, valorBuscado);
            return consultar(ps);
        } catch (SQLException e) {
            registrar("buscarPor " + columna, e);
            return List.of();
        }
    }

    // ===== Ayudantes: el DAO hijo prepara el ps (rellena los ?) y estos lo ejecutan =====

    /** Ejecuta un SELECT y pasa todas las filas a una lista de objetos. */
    protected List<T> consultar(PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                Optional<T> objeto = mapRsToClass(rs);
                if (objeto.isEmpty()) return List.of(); // si una fila falla, no devolvemos una lista a medias
                resultado.add(objeto.get());
            }
            return resultado;
        } catch (SQLException e) {
            registrar("consultar", e);
            return List.of();
        }
    }

    /** Ejecuta un SELECT que como mucho devuelve una fila. */
    protected Optional<T> consultarUno(PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRsToClass(rs);
            }
            return Optional.empty();
        } catch (SQLException e) {
            registrar("consultarUno", e);
            return Optional.empty();
        }
    }

    /** Ejecuta un INSERT, UPDATE o DELETE. Devuelve cuántas filas cambiaron (0 si falla). */
    protected int ejecutar(PreparedStatement ps) {
        try {
            return ps.executeUpdate();
        } catch (SQLException e) {
            registrar("ejecutar", e);
            return 0;
        }
    }

    /**
     * Ejecuta un INSERT y devuelve el id nuevo que pone la base de datos (0 si falla).
     * El ps tiene que crearse con Statement.RETURN_GENERATED_KEYS.
     */
    protected int insertarConId(PreparedStatement ps) {
        try {
            int filasInsertadas = ps.executeUpdate();
            if (filasInsertadas == 0) return 0;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // la columna 1 es el id nuevo
                }
                return 0;
            }
        } catch (SQLException e) {
            registrar("insertar", e);
            return 0;
        }
    }

    // ===== Comprobaciones comunes =====

    /** Un texto vale si no es null y no está vacío ni lleno de espacios. */
    protected static boolean textoValido(String texto) {
        return texto != null && !texto.isBlank();
    }

    /** Un id vale si no es null y, si es texto (matrícula), no está en blanco. */
    protected boolean idValido(ID id) {
        if (id == null) return false;
        if (id instanceof String texto) return !texto.isBlank();
        return true;
    }

    /** Apunta el error sin mostrar datos personales: solo qué operación falló y por qué. */
    protected void registrar(String operacion, SQLException e) {
        LOG.log(Level.WARNING, "Fallo JDBC en " + operacion + ": " + e.getMessage());
    }
}
