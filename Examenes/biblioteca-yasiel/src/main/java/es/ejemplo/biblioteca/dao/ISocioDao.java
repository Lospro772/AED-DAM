package es.ejemplo.biblioteca.dao;
import java.util.*;
import es.ejemplo.biblioteca.modelo.*;
public interface ISocioDao {

 List<Socio> listar();
 Optional<Socio> buscarPorId(Integer id);
 int insertar(Socio entidad);
 int actualizar(Socio entidad);
 int eliminar(Integer id);
 Optional<Socio> buscarPorEmail(String email);
 List<Socio> listarAltasDesde(String fecha);
}
