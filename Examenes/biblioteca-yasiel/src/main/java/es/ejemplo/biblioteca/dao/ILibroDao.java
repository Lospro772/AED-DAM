package es.ejemplo.biblioteca.dao;
import java.util.*;
import es.ejemplo.biblioteca.modelo.*;
public interface ILibroDao {

 List<Libro> listar();
 Optional<Libro> buscarPorId(Integer id);
 int insertar(Libro entidad);
 int actualizar(Libro entidad);
 Object eliminar(Integer id);
}
