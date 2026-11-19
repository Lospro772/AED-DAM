package aula.tareas.controller;

import aula.tareas.api.TareaRequest;
import aula.tareas.service.ITareaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/jdbc/tareas")
@Tag(name="Tareas Jdbc", description = "Operaciones crud de tareas en json")
public class TareaControllerJdbc {

    private final ITareaService serviceJson;

    public TareaControllerJdbc(@Qualifier("TareaServiceJdbc") ITareaService serviceJson) {
        this.serviceJson = serviceJson;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las tareas desde el fichero json")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Obtener todas las tareas desde el fichero json"))
    List<TareaRequest> listar() {
        return serviceJson.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener todas las tareas a traves de su id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Se obtiene la tarea indicaba a traves de su id"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    ResponseEntity<TareaRequest> buscar(@PathVariable long id) {
        //return ResponseEntity.ofNullable(servicio.buscar(id));
    return null;
    }

    @PostMapping
    @Operation(summary = "Obtener todas las tareas a traves de su id")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    ResponseEntity<TareaRequest> crear(@Valid @RequestBody TareaControllerJdbc.NuevaTarea peticion) {
        TareaRequest tareaRequest = serviceJson.crear(peticion.titulo());
        return ResponseEntity.created(URI.create("/api/json/tareas/" + tareaRequest.id())).body(tareaRequest);
    }

    @PutMapping("/{id}")
    ResponseEntity<TareaRequest> update(@PathVariable long id, @Valid @RequestBody TareaRequest tarea) {
        return null;
    };

    public record NuevaTarea(@NotBlank String titulo) {}

}
