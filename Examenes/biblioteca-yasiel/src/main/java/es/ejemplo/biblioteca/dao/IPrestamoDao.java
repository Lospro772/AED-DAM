package es.ejemplo.biblioteca.dao;
import java.util.*;
import es.ejemplo.biblioteca.modelo.*;
public interface IPrestamoDao {
 
    List<Prestamo> listar();
    Optional<Prestamo> buscarPorId(Integer id);
    int insertar(Prestamo entidad);
    int actualizar(Prestamo entidad);
    int eliminar(Integer id);
    List<PrestamosPorSocio> contarPrestamosPorSocio();
    List<Prestamo> listarPrestamosPorAutor(String autor);
}
