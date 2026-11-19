package aula.tareas.service;

import aula.tareas.api.TareaRequest;

import java.util.List;

public interface ITareaService {
    /**
     * Lista todas las tareas
     * @return
     */
    public List<TareaRequest> listar();

    /**
     * Busca toda las tareas
     * @param id
     * @return
     */
    public TareaRequest buscar(Long id);

    /**
     * Crea las tareas
     * @param titulo
     * @return
     */
    public TareaRequest crear(String titulo);
}
