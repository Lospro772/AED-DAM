package es.codelearnacademy.filelab.persona;
import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.repository.IRepository;
import java.util.List;
/** TODO: documenta la finalidad de esta interfaz. */
public interface IPersonaRepository extends IRepository<Persona,String> {
    /** TODO: documenta edad, retorno y ausencia de resultados. */
    default List<Persona> findByEdadMinima(int edad) {
        throw new UnsupportedOperationException("Pendiente: filtro por edad");
    }
    /** TODO: documenta el parametro activo y el resultado. */
    default List<Persona> findByActivo(boolean activo) {
        throw new UnsupportedOperationException("Pendiente: filtro por estado");
    }
}
