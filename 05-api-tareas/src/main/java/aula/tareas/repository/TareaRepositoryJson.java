package aula.tareas.repository;

import aula.tareas.api.TareaRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository("repositoryJson")
public class TareaRepositoryJson implements ITareaRepository{
    private final AtomicLong secuencia = new AtomicLong();
    private Path path;
    private ObjectMapper mapper;
    private final String url = "jdbc:sqlite:data/tarea.sqlite3 / db";
    private List<TareaRequest> lista;

    public TareaRepositoryJson() {
    //path = Path.of("data", "tarea.json");
    //path =Path.of("data/test.json");
    /**
     if (!Files.exists(path)) {
     try {
     Files.createFile(path);
     } catch (IOException e) {
     throw new RuntimeException(e);
     }
     }
     **/
        this("data/tarea.json");
        lista = getAll();
        //String jdbcUrl = "jdbc:sqlite:data/tarea.sqlite3";
        //ormFactory.crear(jdbcUrl);

    }
                                    //jdbcUrl
    public TareaRepositoryJson(String url) {
        path = Path.of(url); //jdbcUrl
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public TareaRequest create(String titulo) {
        long id = secuencia.incrementAndGet();
        var tarea = new TareaRequest(id, titulo.strip());
        lista.add(tarea);
        saveAll(lista);
        return tarea;
    }

    @Override
    public boolean delete(long identificador) {
        return false;
    }

    @Override
    public List<TareaRequest> findAll() {
        return List.copyOf(lista);
    }

    @Override
    public TareaRequest findById(long identificador) {
        return null;
    }

    @Override
    public TareaRequest update(TareaRequest tareaRequest) {
        return null;
    }

    private void saveAll(List<TareaRequest> items) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), items);
        }catch (Exception exception) {
//Logger
        }

    }

    public List<TareaRequest> getAll() {
        try {
            return new ArrayList<>(mapper.readValue(
                    path.toFile(), new TypeReference<List<TareaRequest>>() {}));
        }catch (Exception exception) {
//Logger
            return new ArrayList<>();
        }

    }


}