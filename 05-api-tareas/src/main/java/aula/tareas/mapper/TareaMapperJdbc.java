package aula.tareas.mapper;

import aula.tareas.api.TareaRequest;
import aula.tareas.entity.TareaEntityJdbc;
import aula.tareas.modelo.Tarea;

public class TareaMapperJdbc {

    static public TareaRequest toDomain(TareaEntityJdbc entityJdbc) {
        return null;
    }

    static public TareaEntityJdbc toJdbc(TareaRequest tareaRequest) {
        return null;
    }

    static public TareaEntityJdbc to(Tarea tarea) {
        if (tarea == null) {
            return null;
        }
        return new TareaEntityJdbc(tarea.getTitulo(), tarea.getIdentificador());
    }

}
