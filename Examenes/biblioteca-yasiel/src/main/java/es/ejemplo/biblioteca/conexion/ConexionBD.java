package es.ejemplo.biblioteca.conexion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBD {

    public static Connection abrir(String url) throws SQLException {

        String ruta = url.substring("jdbc:sqlite:".length());

        if (!Files.isRegularFile(Path.of(ruta))) {
            throw new SQLException("No existe:" + ruta);
        }

        Connection connection = DriverManager.getConnection(url);

        try (Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

}
