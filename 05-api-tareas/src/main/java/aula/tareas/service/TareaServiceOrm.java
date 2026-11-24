package aula.tareas.service;

import aula.tareas.api.TareaRequest;
import aula.tareas.repository.ITareaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("TareaServiceOrm")
public class TareaServiceOrm implements ITareaService {

    private final ITareaRepository tareaRepositoryJson;

    public TareaServiceOrm(@Qualifier("repositoryOrm") ITareaRepository tareaRepositoryJson) {
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
