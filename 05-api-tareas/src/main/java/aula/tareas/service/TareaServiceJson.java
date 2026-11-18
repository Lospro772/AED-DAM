package aula.tareas.service;

import aula.tareas.modelo.Tarea;
import aula.tareas.repository.ITareaRepository;
import aula.tareas.repository.TareaRepositoryJson;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("TareaServiceJson")
public class TareaServiceJson implements ITareaService {

    private final ITareaRepository tareaRepositoryJson;

    public TareaServiceJson(@Qualifier("repositoryJson") ITareaRepository tareaRepositoryJson) {
        this.tareaRepositoryJson = tareaRepositoryJson;
    }

    @Override
    public List<Tarea> listar() {
        return tareaRepositoryJson.findAll();
    }

    @Override
    public Tarea buscar(Long id) {
        return null;
    }

    @Override
    public Tarea crear(String titulo) {
        return tareaRepositoryJson.create(titulo);
    }
}
