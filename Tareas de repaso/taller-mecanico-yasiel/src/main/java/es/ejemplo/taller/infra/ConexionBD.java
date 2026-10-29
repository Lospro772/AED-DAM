package es.ejemplo.taller.infra;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.*;

/**
 * Única clase que crea las conexiones con la base de datos.
 * No deja ninguna conexión abierta: cada operación abre la suya y la cierra con try-with-resources.
 */
public class ConexionBD {
    private static final String PREFIJO_SQLITE = "jdbc:sqlite:";

    private final String url;

    public ConexionBD(String url) {
        this.url = url;
    }

    /** Abre una conexión con la url de este objeto. Quien la pide es quien la cierra. */
    public Connection obtenerConexion() throws SQLException {
        return abrir(url);
    }

    public static Connection abrir(String url) throws SQLException {
        // 1. La URL tiene que ser de SQLite
        if (url == null || !url.startsWith(PREFIJO_SQLITE)) {
            throw new SQLException("URL SQLite no válida: " + url);
        }
        // 2. Comprobar el archivo ANTES de conectar (si no existe, SQLite crearía uno vacío sin avisar)
        Path rutaArchivo;
        try {
            rutaArchivo = Path.of(url.substring(PREFIJO_SQLITE.length()));
        } catch (InvalidPathException e) {
            throw new SQLException("Ruta no válida: " + url, e);
        }
        if (!Files.exists(rutaArchivo) || !Files.isRegularFile(rutaArchivo) || !Files.isReadable(rutaArchivo)) {
            throw new SQLException("No existe o no se puede leer la base de datos: " + rutaArchivo);
        }
        // 3. Conectar
        Connection con = DriverManager.getConnection(url);
        try {
            // 4. Comprobar que es la base de datos del taller y activar las claves foráneas
            comprobarTablas(con);
            try (Statement st = con.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            return con;
        } catch (SQLException e) {
            con.close(); // si algo falla, no dejamos la conexión abierta
            throw e;
        }
    }

    /** Cuenta cuántas de las 5 tablas del taller hay. Si no están las 5, error. */
    private static void comprobarTablas(Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                + "AND name IN ('clientes', 'vehiculos', 'mecanicos', 'reparaciones', 'piezas_reparacion')";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int tablasEncontradas = 0;
            if (rs.next()) {
                tablasEncontradas = rs.getInt(1); // la columna 1 es el COUNT(*)
            }
            if (tablasEncontradas != 5) {
                throw new SQLException("La base de datos no tiene las 5 tablas del taller");
            }
        }
    }
}
