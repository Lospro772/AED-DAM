package es.ejemplo.biblioteca.dao;
import java.util.*;
import es.ejemplo.biblioteca.modelo.*;
/** Implementación pendiente: el alumnado debe completar los métodos JDBC. */
public final class PrestamoDao implements IPrestamoDao {
 private final String url;
 
 public PrestamoDao(String url) { this.url=Objects.requireNonNull(url); }
 
 @Override
 public List<Prestamo> listar() {
  throw new UnsupportedOperationException("Pendiente: listar");
 }
 
 @Override
 public Optional<Prestamo> buscarPorId(Integer id) {
  throw new UnsupportedOperationException("Pendiente: buscarPorId");
 }
 
 @Override
 public int insertar(Prestamo entidad) {
  throw new UnsupportedOperationException("Pendiente: insertar");
 }
 @Override
 public int actualizar(Prestamo entidad) {
  throw new UnsupportedOperationException("Pendiente: actualizar");
 }
 @Override
 public int eliminar(Integer id) {
  throw new UnsupportedOperationException("Pendiente: eliminar");
 }
 @Override
 public List<PrestamosPorSocio> contarPrestamosPorSocio() {
  throw new UnsupportedOperationException("Pendiente: contarPrestamosPorSocio");
 }
 @Override
 public List<Prestamo> listarPrestamosPorAutor(String autor) {
  throw new UnsupportedOperationException("Pendiente: listarPrestamosPorAutor");
 }
}
