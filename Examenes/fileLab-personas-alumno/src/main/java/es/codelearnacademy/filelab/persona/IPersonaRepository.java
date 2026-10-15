package es.codelearnacademy.filelab.persona;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.repository.IRepository;

import java.util.List;
import java.util.Optional;

/**
 * TODO: documenta la finalidad de esta interfaz.
 */
public interface IPersonaRepository extends IRepository<Persona, String> {
    /**
     * TODO: documenta edad, retorno y ausencia de resultados.
     */
    
    XmlMapper xmlmapper = new XmlMapper();

    default List<Persona> findByEdadMinima(int edad) {
        return findAll().stream().filter(p -> p.edad() == edad).toList();
    }

    /**
     * TODO: documenta el parametro activo y el resultado.
     */
    default List<Persona> findByActivo(boolean activo) {
        return findAll().stream().filter(p -> p.activo() == activo).toList();
    }
}
