package com.ejemplo.catalogo.configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfiguration {
    private static Path path;
    private static Properties props;
    public static void main(String[] args) {
        path = Path.of("data", "app.properties");
        props = new Properties();

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fomato fichero "+props.getProperty("storage.format"));
        System.out.println("Ruta fichero "+props.getProperty("storage.fichero"));
        props.setProperty("app.name", "FileLab");
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            props.store(writer, "Configuracion de la aplicacion");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
