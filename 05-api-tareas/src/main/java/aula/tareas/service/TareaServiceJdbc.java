package aula.tareas.service;

import aula.tareas.api.TareaRequest;
import aula.tareas.repository.ITareaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("TareaServiceJdbc")
public class TareaServiceJdbc implements ITareaService {

    private final ITareaRepository tareaRepositoryJson;

    public TareaServiceJdbc(@Qualifier("repositoryJdbc") ITareaRepository tareaRepositoryJson) {
        this.tareaRepositoryJson = tareaRepositoryJson;
    }

    @Override
    public List<TareaRequest> listar() {
        return tareaRepositoryJson.findAll();
    }

    @Override
    public TareaRequest buscar(Long id) {
        return null;
    }

    @Override
    public TareaRequest crear(String titulo) {
        return tareaRepositoryJson.create(titulo);
    }
}
