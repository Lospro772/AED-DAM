package aula.tareas.service;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import aula.tareas.modelo.Tarea;
import aula.tareas.repository.ITareaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service("TareaRepositoryCsv")
@Repository
public class TareaServiceCsv implements ITareaService {
    private final AtomicLong secuencia = new AtomicLong();
    private final ConcurrentSkipListMap<Long, Tarea> tareas = new ConcurrentSkipListMap<>();
    private final ITareaRepository repository;

    //El pasado
    /**
    public TareaService() {
        this.repository = new TareaRepository();
    }
     **/

    //Spring
    public TareaServiceCsv(@Qualifier("repositoryCsv")  ITareaRepository repository) {
        this.repository = repository;
    }


    public List<Tarea> listar() {
        return repository.findAll();
    }

    @Override
    public Tarea buscar(Long id) {
        return null;
    }


    public Tarea crear(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }
        return repository.create(titulo);
    }
}
