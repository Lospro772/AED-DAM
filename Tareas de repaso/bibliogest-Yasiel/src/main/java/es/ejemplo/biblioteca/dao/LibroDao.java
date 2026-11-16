package es.ejemplo.biblioteca.dao;
import es.ejemplo.biblioteca.modelo.Libro;

import es.ejemplo.biblioteca.orm.entity.AutorEntity;
import es.ejemplo.biblioteca.orm.entity.LibroEntity;
import es.ejemplo.biblioteca.orm.mapper.AutorMapper;
import es.ejemplo.biblioteca.orm.mapper.LibroMapper;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DAO de libros. Además del CRUD tiene las consultas del catálogo: búsquedas, disponibilidad de
 * ejemplares y estadísticas por género.
 */
public class LibroDao implements AutoCloseable {
    private static final Logger LOG=Logger.getLogger(LibroDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;
    
    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param url URL JDBC de la base de datos SQLite
     */
    public LibroDao(String url) {managerFactory=OrmFactory.crear(url);ownFactory=true;}
    
    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public LibroDao(EntityManagerFactory factory) {managerFactory=factory;ownFactory=false;}
    private void registrar(String metodo,Exception ex) {LOG.log(Level.WARNING,metodo+" no se pudo completar: "+ex.getMessage());}
    
    /**
     * Devuelve el catálogo completo de libros.
     *
     * @return lista de libros, vacía si falla la consulta
     */
    public List<Libro> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l order by l.titulo, l.idLibro",
                            LibroEntity.class)
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listar",ex);
            return List.of();
        }
    }
    
    /**
     * Recupera un libro a partir de su id.
     *
     * @param id identificador del libro
     * @return el libro encontrado o un Optional vacío
     */
    public Optional<Libro> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(LibroEntity.class, id))
                    .map(LibroMapper::toDomain);
        } catch (Exception ex) {
            registrar("buscarPorId",ex);
            return Optional.empty();
        }
    }
    
    /**
     * Da de alta un libro. Necesita título, ISBN, género, autor y un número de ejemplares que no sea
     * negativo.
     *
     * @param libro libro que se va a guardar
     * @return id generado, o 0 si no se pudo guardar
     */
    public int insertar(Libro libro) {
        if (libro == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                LibroEntity entidad = LibroMapper.toEntity(libro);
                em.persist(entidad);
                em.flush();
                libro.setIdAutor(entidad.getIdAutor());
                tx.commit();
                return libro.getIdAutor();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }
    
    /**
     * Guarda los cambios de un libro que ya está en la base de datos.
     *
     * @param libro libro con los datos modificados
     * @return 1 si se actualizó, 0 si no existe o hubo un error
     */
    public int actualizar(Libro libro) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                LibroEntity l = em.find(LibroEntity.class, libro.getIdAutor());
                if (l == null) {
                    tx.commit();
                    return 0;
                }
                LibroEntity clienteActualizado = LibroMapper.toEntity(libro);
                l.setTitulo(clienteActualizado.getTitulo());
                l.setGenero(clienteActualizado.getGenero());
                l.setEjemplares(clienteActualizado.getEjemplares());
                l.setAnioPublicacion(clienteActualizado.getAnioPublicacion());
                l.setIsbn(clienteActualizado.getIsbn());
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }
    
    /**
     * Elimina un libro siempre que no tenga préstamos ni reservas que apunten a él.
     *
     * @param id identificador del libro
     * @return 1 si se borró, 0 si no
     */
    public int eliminar(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                LibroEntity a = em.find(LibroEntity.class, id);
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
    private boolean validar(Libro dato) {return dato != null && dato.getTitulo()!=null && !dato.getTitulo().isBlank() && dato.getIsbn()!=null && !dato.getIsbn().isBlank() && dato.getGenero()!=null && dato.getIdAutor()!=null && dato.getEjemplares()!=null && dato.getEjemplares()>=0;}
    
    /**
     * Libros de un género concreto, por ejemplo Novela.
     *
     * @param genero género que se busca
     * @return libros de ese género
     */
    public List<Libro> buscarPorGenero(String genero) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l where l.genero = :genero order by l.genero desc",
                            LibroEntity.class)
                    .setParameter("genero", genero)
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("buscarPorGenero", ex);
            return List.of();
        }
    }
    
    /**
     * Todos los libros escritos por un mismo autor.
     *
     * @param idAutor id del autor
     * @return libros del autor, vacía si no tiene
     */
    public List<Libro> buscarPorAutor(int idAutor) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l where l.idAutor = :id order by l.idAutor, l.titulo",
                            LibroEntity.class)
                    .setParameter("id", idAutor)
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("buscarPorAutor", ex);
            return List.of();
        }
    }
    
    /**
     * Busca libros cuyo título contenga el texto indicado (consulta 3).
     *
     * @param texto trozo del título que hay que buscar
     * @return libros que coinciden
     */
    public List<Libro> buscarPorTitulo(String texto) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l where lower(l.titulo) like :texto order by l.titulo, l.idLibro",
                            LibroEntity.class)
                    .setParameter("texto", "%"+texto.toLowerCase(Locale.ROOT)+"%")
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("buscarPorTitulo", ex);
            return List.of();
        }
    }
    
    /**
     * Libros que tienen al menos un ejemplar sin prestar en este momento.
     *
     * @return libros con algún ejemplar libre
     */
    public List<Libro> listarDisponibles() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l where l.ejemplares > (select count(p) from PrestamoEntity p "
                            + "where p.idLibro = l.idLibro and p.fechaDevolucion is null)  order by l.titulo",
                            LibroEntity.class)
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("listarDisponibles", ex);
            return List.of();
        }
    }
    
    /**
     * Libros de un género publicados a partir de un año, ese año incluido (consulta 4).
     *
     * @param anio año mínimo de publicación
     * @param genero género del libro
     * @return libros que cumplen los dos filtros
     */
    public List<Libro> librosDesdeAnio(int anio, String genero) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l where l.anioPublicacion >= :anio and l.genero = :genero order by l.anioPublicacion",
                            LibroEntity.class)
                    .setParameter("anio", anio)
                    .setParameter("genero", genero)
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("librosDesdeAnio", ex);
            return List.of();
        }
    }
    
    /**
     * Géneros distintos que hay en el catálogo, sin repetir (consulta 5).
     *
     * @return nombres de los géneros
     */
    public List<String> generos() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select distinct l.genero from LibroEntity l order by l.genero",
                            String.class)
                    .getResultList();
        } catch (RuntimeException ex) {
            registrar("generos", ex);
            return List.of();
        }
    }
    
    /**
     * Libros que no aparecen en ningún préstamo (consulta 16).
     *
     * @return libros que nunca se han prestado
     */
    public List<Libro> nuncaPrestados() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l from LibroEntity l where (select count(p) from PrestamoEntity p "
                            + "where p.idLibro = l.idLibro) = 0 order by l.titulo",
                            LibroEntity.class)
                    .getResultStream()
                    .map(LibroMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("nuncaPrestados", ex);
            return List.of();
        }
    }
    
    /**
     * Libros con más préstamos que la media del catálogo. Para calcular la media también cuentan los
     * libros sin préstamos (consulta 18).
     *
     * @return libros por encima de la media
     */
    public List<Libro> masPrestadosQueMedia() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Libros con reservas pendientes que ya no tienen ejemplares libres (consulta 20).
     *
     * @return libros reservados sin ejemplares disponibles
     */
    public List<Libro> reservadosSinDisponibilidad() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Cada libro con el nombre y la nacionalidad de su autor (consulta 6).
     *
     * @return una fila por libro con los datos del libro y del autor
     */
    public List<Object[]> librosConAutor() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Número de libros que hay de cada género (consulta 11).
     *
     * @return filas con el género y su cantidad de libros
     */
    public List<Object[]> contarPorGenero() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Géneros cuyo total de préstamos históricos supera el mínimo indicado (consulta 14).
     *
     * @param minimo cantidad de préstamos que hay que superar
     * @return filas con el género y su número de préstamos
     */
    public List<Object[]> prestamosPorGeneroMinimo(long minimo) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Para cada libro: ejemplares totales, cuántos están prestados ahora y cuántos quedan libres
     * (consulta 15).
     *
     * @return una fila por libro con esos tres valores
     */
    public List<Object[]> disponibilidadPorLibro() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }
    
    /**
     * Resumen por género con títulos, ejemplares, préstamos históricos y préstamos activos (consulta
     * 24).
     *
     * @return una fila por género
     */
    public List<Object[]> estadisticasGenero() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }


    
    @Override
    /**
     * Cierra la factoría solo si la creó este DAO. Si es compartida se deja abierta.
     */
    public void close() {if(ownFactory && managerFactory.isOpen())managerFactory.close();}
}
