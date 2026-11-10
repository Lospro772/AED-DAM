package es.ejemplo.biblioteca.dao;
import es.ejemplo.biblioteca.modelo.Autor;

import es.ejemplo.biblioteca.orm.entity.AutorEntity;
import es.ejemplo.biblioteca.orm.entity.SocioEntity;
import es.ejemplo.biblioteca.orm.mapper.AutorMapper;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.mapper.SocioMapper;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Acceso a la tabla de autores con JPA. Si algo falla, el error se registra en el log y el método
 * devuelve 0, una lista vacía o un Optional vacío; nunca se lanza la excepción hacia fuera.
 */
public class AutorDao implements AutoCloseable {
    private static final Logger LOG=Logger.getLogger(AutorDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;
    
    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param url URL JDBC de la base de datos SQLite
     */
    public AutorDao(String url) {managerFactory=OrmFactory.crear(url);ownFactory=true;}
    
    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public AutorDao(EntityManagerFactory factory) {managerFactory=factory;ownFactory=false;}
    private void registrar(String metodo,Exception ex) {LOG.log(Level.WARNING,metodo+" no se pudo completar: "+ex.getMessage());}
    
    /**
     * Devuelve todos los autores guardados en la base de datos.
     *
     * @return lista de autores, vacía si no hay ninguno o si falla la consulta
     */
    public List<Autor> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select a from AutorEntity a order by a.nombre",
                            AutorEntity.class)
                    .getResultStream()
                    .map(AutorMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listar",ex);
            return List.of();
        }
    }
    
    /**
     * Busca un autor por su clave primaria.
     *
     * @param id identificador del autor
     * @return el autor si existe; si no, un Optional vacío
     */
    public Optional<Autor> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(AutorEntity.class, id))
                    .map(AutorMapper::toDomain);
        } catch (Exception ex) {
            registrar("buscarPorId",ex);
            return Optional.empty();
        }
    }
    
    /**
     * Guarda un autor nuevo y le asigna el id que genera la base de datos.
     *
     * @param autor datos del autor, todavía sin id
     * @return id generado, o 0 si no se pudo insertar
     */
    public int insertar(Autor autor) {
        if (autor == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                AutorEntity entidad = AutorMapper.toEntity(autor);
                em.persist(entidad);
                em.flush();
                autor.setIdAutor(entidad.getIdAutor());
                tx.commit();
                return autor.getIdAutor();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }
    
    /**
     * Actualiza los datos de un autor que ya existe, buscándolo por su id.
     *
     * @param autor autor con los datos nuevos
     * @return 1 si se actualizó, 0 si no existe o hubo un error
     */
    public int actualizar(Autor autor) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                AutorEntity a = em.find(AutorEntity.class, autor.getIdAutor());
                if (a == null) {
                    tx.commit();
                    return 0;
                }
                AutorEntity clienteActualizado = AutorMapper.toEntity(autor);
                a.setNombre(clienteActualizado.getNombre());
                a.setNacionalidad(clienteActualizado.getNacionalidad());
                a.setAnioNacimiento(clienteActualizado.getAnioNacimiento());
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }
    
    /**
     * Borra un autor. Si todavía tiene libros asociados la base de datos no lo permite y se devuelve
     * 0.
     *
     * @param id identificador del autor que se quiere borrar
     * @return 1 si se borró, 0 en cualquier otro caso
     */
    public int eliminar(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                AutorEntity a = em.find(AutorEntity.class, id);
                if (a == null) {
                    tx.commit();
                    return 0;
                }
                em.remove(a);
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }
    private boolean validar(Autor dato) {return dato != null && dato.getNombre()!=null && !dato.getNombre().isBlank();}
    
    /**
     * Filtra los autores por su nacionalidad.
     *
     * @param nacionalidad nacionalidad a buscar, por ejemplo España
     * @return autores de esa nacionalidad
     */
    public List<Autor> buscarPorNacionalidad(String nacionalidad) {
        try (var em = managerFactory.createEntityManager()) {
            //var tx = em.getTransaction();
            return em.createQuery(
                            "select s from AutorEntity s where s.nacionalidad = :nacionalidad order by s.nombre desc",
                            AutorEntity.class)
                    .setParameter("nacionalidad", nacionalidad)
                    .getResultStream()
                    .map(AutorMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            //if (tx.isActive()) tx.rollback();
            //registrar("activo", ex);
            //return listarActivos();
            registrar("buscarPorNacionalidad", ex);
            return List.of();
        }
    }
    
    /**
     * Cuenta cuántos libros del catálogo tiene un autor.
     *
     * @param idAutor id del autor
     * @return número de libros; 0 si no tiene ninguno o si falla
     */
    public long contarLibrosPorAutor(int idAutor) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select count(l) from LibroEntity l where l.idAutor = :idAutor",
                            Long.class)
                    .setParameter("idAutor", idAutor)
                    .getSingleResult();
        } catch (RuntimeException ex) {
            registrar("contarLibrosPorAutor", ex);
            return 0;
        }
    }
    
    /**
     * Cada autor con el número de libros que tiene, incluidos los que no tienen ninguno (consulta
     * 12).
     *
     * @return una fila por autor con su nombre y su cantidad de libros
     */
    public List<Object[]> autoresConRecuento() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Autores cuyos libros han sido prestados por al menos tres socios distintos (consulta 19).
     *
     * @return autores que cumplen la condición
     */
    public List<Autor> autoresLeidosPorTresSocios() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }


    
    @Override
    /**
     * Cierra la factoría solo si la creó este DAO. Si es compartida se deja abierta.
     */
    public void close() {if(ownFactory && managerFactory.isOpen())managerFactory.close();}
}
