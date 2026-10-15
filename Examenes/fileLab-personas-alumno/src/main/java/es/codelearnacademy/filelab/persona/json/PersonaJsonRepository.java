package es.codelearnacademy.filelab.persona.json;
import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.persona.IPersonaRepository;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import java.nio.file.*;
import java.io.*;
import java.util.*;
public class PersonaJsonRepository extends AbstractFileRepository<Persona,String> implements IPersonaRepository {
    private final Path path;
    public PersonaJsonRepository(Path path) {
        this.path=path;
    }
    @Override
    protected String getId(Persona p) {
        return p.dni();
    }
    @Override
    protected List<Persona> readAll() {
        throw new UnsupportedOperationException("TODO: leer JSON con ObjectMapper");
    }
    @Override
    protected void writeAll(List<Persona> personas) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
