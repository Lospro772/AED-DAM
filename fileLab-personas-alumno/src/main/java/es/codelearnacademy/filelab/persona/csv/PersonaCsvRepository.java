package es.codelearnacademy.filelab.persona.csv;
import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.persona.IPersonaRepository;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import java.nio.file.*;
import java.io.*;
import java.util.*;
public class PersonaCsvRepository extends AbstractFileRepository<Persona,String> implements IPersonaRepository {
    private final Path path;
    public PersonaCsvRepository(Path path) {
        this.path=path;
    }
    @Override
    protected String getId(Persona p) {
        return p.dni();
    }
    @Override
    protected List<Persona> readAll()  {
        throw new UnsupportedOperationException("TODO: leer CSV con Commons CSV");
    }
    @Override
    protected void writeAll(List<Persona> personas)  {
        throw new UnsupportedOperationException("TODO: guardar CSV con Commons CSV");
    }
}
