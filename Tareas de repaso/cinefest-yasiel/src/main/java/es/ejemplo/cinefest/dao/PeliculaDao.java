package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Pelicula;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.PeliculaEntity;
import es.ejemplo.cinefest.orm.mapper.PeliculaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeliculaDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(PeliculaDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param jdbcUrl URL JDBC de la base de datos SQLite
     */
    public PeliculaDao(String jdbcUrl) {
        managerFactory = OrmFactory.crear(jdbcUrl);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public PeliculaDao(EntityManagerFactory factory) {
        managerFactory = factory;
        ownFactory = false;
    }

    private void registrar(String metodo, Exception ex) {
        LOG.log(Level.WARNING, metodo + " no se pudo completar: " + ex.getMessage());
    }

    private void deshacer(EntityTransaction tx) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } catch (Exception ex) {
            registrar("rollback", ex);
        }
    }

    /**
     * Devuelve todas las películas ordenadas por título.
     *
     * @return lista de películas; lista vacía si no hay o si falla la consulta (el error se registra
     *     en el log)
     */
    public List<Pelicula> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select x from PeliculaEntity x order by x.titulo, x.idPelicula", PeliculaEntity.class)
                    .getResultStream()
                    .map(PeliculaMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca una película por su clave primaria.
     *
     * @param id identificador de la película
     * @return la película si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Pelicula> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(PeliculaEntity.class, id)).map(PeliculaMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta una película. El id lo genera la base de datos, así que se ignora el que traiga el objeto.
     * Título, género y país obligatorios, duración mayor que 0 y año informado.
     *
     * @param dato la película a insertar
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción; en ese caso se hace rollback
     */
    public int insertar(Pelicula dato) {
        if (!datosValidos(dato)) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                PeliculaEntity entidad = PeliculaMapper.aEntidad(dato);
                entidad.setIdPelicula(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdPelicula(entidad.getIdPelicula());
                return entidad.getIdPelicula();
            } catch (Exception ex) {
                // El rollback va aquí dentro: fuera del try-with-resources el EntityManager ya está cerrado.
                deshacer(tx);
                registrar("insertar", ex);
                return 0;
            }
        } catch (Exception ex) {
            registrar("insertar", ex);
            return 0;
        }
    }

    /**
     * Actualiza todos los campos de una película existente. Se aplican las mismas reglas que al insertar.
     *
     * @param dato la película con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la fila no existe o la base rechaza
     *     el cambio, con rollback
     */
    public int actualizar(Pelicula dato) {
        if (!datosValidos(dato) || dato.getIdPelicula() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (em.find(PeliculaEntity.class, dato.getIdPelicula()) == null) {
                    tx.rollback();
                    return 0;
                }
                em.merge(PeliculaMapper.aEntidad(dato));
                tx.commit();
                return 1;
            } catch (Exception ex) {
                deshacer(tx);
                registrar("actualizar", ex);
                return 0;
            }
        } catch (Exception ex) {
            registrar("actualizar", ex);
            return 0;
        }
    }

    /**
     * Elimina una película. No se permite si todavía tiene sesiones o valoraciones.
     *
     * @param id identificador de la película
     * @return 1 si se eliminó; 0 si no existe, si tiene sesiones o valoraciones asociadas o si la base rechaza el borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                PeliculaEntity entidad = em.find(PeliculaEntity.class, id);
                if (entidad == null || tieneDependencias(em, id)) {
                    tx.rollback();
                    return 0;
                }
                em.remove(entidad);
                tx.commit();
                return 1;
            } catch (Exception ex) {
                deshacer(tx);
                registrar("eliminar", ex);
                return 0;
            }
        } catch (Exception ex) {
            registrar("eliminar", ex);
            return 0;
        }
    }

    public List<Pelicula> buscarPorGenero(String genero) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Pelicula> peliculasDesdeAnio(int anio) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Pelicula> peliculasSinSesiones() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> peliculasConMediaValoraciones() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> mejorValoradas(int minimo) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> masVendidasQueMedia() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> estadisticasGenero() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean datosValidos(Pelicula dato) {
        return dato != null
                && !vacio(dato.getTitulo())
                && !vacio(dato.getGenero())
                && !vacio(dato.getPais())
                && dato.getDuracionMin() != null
                && dato.getDuracionMin() > 0
                && dato.getAnio() != null;
    }

    private boolean tieneDependencias(EntityManager em, int id) {
        return contar(em, "select count(d) from SesionEntity d where d.idPelicula = :id", id) > 0
                || contar(em, "select count(d) from ValoracionEntity d where d.idPelicula = :id", id) > 0;
    }

    private long contar(EntityManager em, String jpql, int id) {
        return em.createQuery(jpql, Long.class).setParameter("id", id).getSingleResult();
    }

    /** Cierra la factoría solo si la creó este DAO; una factoría compartida la cierra su dueño. */
    @Override
    public void close() {
        if (ownFactory && managerFactory.isOpen()) {
            managerFactory.close();
        }
    }
}
