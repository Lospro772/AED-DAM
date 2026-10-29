package es.ejemplo.clinica.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Único sitio donde se abren conexiones. Cada DAO llama a abrir(url) dentro de un
 * try-with-resources, así la conexión se cierra sola al terminar cada método.
 */
public final class ConexionBD {
    private static final String PREFIJO = "jdbc:sqlite:";

    private ConexionBD() { }

    public static Connection abrir(String url) throws SQLException {
        // 1. La URL tiene que ser de SQLite
        if (url == null || !url.startsWith(PREFIJO)) {
            throw new IllegalArgumentException("URL SQLite no válida: " + url);
        }
        // 2. Comprobar que el archivo existe ANTES de conectar (si no, SQLite crea uno vacío)
        String ruta = url.substring(PREFIJO.length());
        if (!Files.isRegularFile(Path.of(ruta))) {
            throw new IllegalArgumentException("La base de datos no existe: " + ruta);
        }
        // 3. Conectar y activar las claves foráneas (hay que hacerlo en CADA conexión)
        Connection con = DriverManager.getConnection(url);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            con.close();
            throw e;
        }
        return con;
    }
}
