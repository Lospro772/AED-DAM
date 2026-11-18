package aula.tareas.service;

import aula.tareas.modelo.Tarea;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface ITareaService {
    /**
     * Lista todas las tareas
     * @return
     */
    public List<Tarea> listar();

    /**
     * Busca toda las tareas
     * @param id
     * @return
     */
    public Tarea buscar(Long id);

    /**
     * Crea las tareas
     * @param titulo
     * @return
     */
    public Tarea crear(String titulo);
}
