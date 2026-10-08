package es.codelearnacademy.filelab.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

public class FilesService {

    public boolean existe(Path path) {
        return Files.exists(path);
    }

    public Optional<Path> crearDirectorio(Path path) {
        try {
            return Optional.of(Files.createDirectory(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Path> crearDirectorios(Path path) {
        try {
            return Optional.of(Files.createDirectories(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Path> crearArchivo(Path path) {
        try {
            return Optional.of(Files.createFile(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Path> copiar(Path origen, Path destino) {
        try {
            return Optional.of(Files.copy(origen, destino));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Path> mover(Path origen, Path destino) {
        try {
            return Optional.of(Files.move(origen, destino));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean eliminar(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (Exception e) {
            return false;
        }
    }

    public OptionalLong tamanio(Path path) {
        try {
            return OptionalLong.of(Files.size(path));
        } catch (Exception e) {
            return OptionalLong.empty();
        }
    }
}
