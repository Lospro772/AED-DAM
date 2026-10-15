package es.codelearnacademy.filelab.persona.csv;

import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.persona.IPersonaRepository;
import es.codelearnacademy.filelab.repository.AbstractFileRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class PersonaCsvRepository extends AbstractFileRepository<Persona, String> implements IPersonaRepository {
    private final Path path;
    private CSVFormat outputFormat;
    private CSVParser inputFormat;

    public PersonaCsvRepository(Path path) {
        this.path = path;
    }

    @Override
    protected String getId(Persona p) {
        return p.dni();
    }

    @Override
    protected List<Persona> readAll() {
        List<Persona> personas = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = inputFormat) {
            for (CSVRecord row : parser) {
                personas.add(new Persona(
                        String.valueOf(row.get("dni")),
                        row.get("nombre"),
                        String.valueOf(row.get("email")),
                        Integer.parseInt(row.get("edad")),
                        Boolean.parseBoolean(row.get("activo"))));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return personas;
    }

    @Override
    protected void writeAll(List<Persona> personas) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, outputFormat)) {
            for (Persona p : personas) printer.printRecord(p.dni(), p.nombre(), p.email(), p.edad(), p.activo()) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
