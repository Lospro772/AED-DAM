package aula.tareas.repository;

import aula.tareas.modelo.Tarea;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Repository;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository("repositoryCsv")
public class TareaRepositoryCsv implements ITareaRepository{
    private  Path path;
    private final CSVFormat inputFormat = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true).build();
    private final CSVFormat outputFormat = CSVFormat.DEFAULT.builder()
            .setHeader("id", "titulo")
            .build();

    public TareaRepositoryCsv() {
        path = Path.of("data", "tarea.csv");
    }

    @Override
    public Tarea create(String titulo) {
        return null;
    }

    @Override
    public boolean delete(long identificador) {
        return false;
    }

    @Override
    public List<Tarea> findAll() {
        if (Files.notExists(path)) {
            return new ArrayList<>();
        }
        try {
            List<Tarea> result = new ArrayList<>();
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                 CSVParser parser = inputFormat.parse(reader)) {
                for (CSVRecord row : parser) {
                    result.add(new Tarea(
                            Long.parseLong(row.get("id")),
                            row.get("titulo")));
                }
            }
            return result;
        }catch (Exception e) {
            return List.of();
        }

    }

    @Override
    public Tarea findById(long identificador) {
        return null;
    }

    @Override
    public Tarea update(Tarea tarea) {
        return null;
    }
}
