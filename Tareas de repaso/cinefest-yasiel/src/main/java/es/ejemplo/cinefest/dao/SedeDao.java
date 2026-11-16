package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Sede;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.SedeEntity;
import es.ejemplo.cinefest.orm.mapper.SedeMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SedeDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(SedeDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param url URL JDBC de la base de datos SQLite
     */
    public SedeDao(String url) {
        managerFactory = OrmFactory.crear(url);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public SedeDao(EntityManagerFactory factory) {
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
     * Devuelve todas las sedes ordenadas por nombre.
     *
     * @return lista de sedes; lista vacía si no hay ninguna o si falla la consulta (el error se
     *     registra en el log)
     */
    public List<Sede> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select s from SedeEntity s order by s.nombre", SedeEntity.class)
                    .getResultStream()
                    .map(SedeMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca una sede por su clave primaria.
     *
     * @param id identificador de la sede
     * @return la sede si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Sede> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SedeEntity.class, id)).map(SedeMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta una sede nueva. El id lo genera la base de datos, así que se ignora el que traiga el
     * objeto.
     *
     * @param dato sede a insertar; nombre y municipio son obligatorios
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción (por ejemplo, nombre duplicado); en ese caso se hace rollback
     */
    public int insertar(Sede dato) {
        if (!esValida(dato)) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                SedeEntity entidad = SedeMapper.aEntidad(dato);
                entidad.setIdSede(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdSede(entidad.getIdSede());
                return entidad.getIdSede();
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
     * Actualiza el nombre y el municipio de una sede existente.
     *
     * @param dato sede con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la sede no existe o la base rechaza
     *     el cambio (por ejemplo, nombre duplicado), con rollback
     */
    public int actualizar(Sede dato) {
        if (!esValida(dato) || dato.getIdSede() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                SedeEntity entidad = em.find(SedeEntity.class, dato.getIdSede());
                if (entidad == null) {
                    tx.rollback();
                    return 0;
                }
                entidad.setNombre(dato.getNombre());
                entidad.setMunicipio(dato.getMunicipio());
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
     * Elimina una sede. No se permite borrar una sede que todavía tiene salas.
     *
     * @param id identificador de la sede
     * @return 1 si se eliminó; 0 si no existe, si tiene salas asociadas o si la base rechaza el
     *     borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                SedeEntity entidad = em.find(SedeEntity.class, id);
                long salas = em.createQuery(
                                "select count(sa) from SalaEntity sa where sa.idSede = :id", Long.class)
                        .setParameter("id", id)
                        .getSingleResult();
                if (entidad == null || salas > 0) {
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

    /**
     * Lista las salas de una sede.
     *
     * @param idSede identificador de la sede
     * @return filas con las columnas {sede, sala, aforo} ordenadas por nombre de sala; lista vacía
     *     si la sede no existe, no tiene salas o falla la consulta
     */
    public List<Object[]> salasPorSede(int idSede) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select s.nombre, sa.nombre, sa.aforo "
                                    + "from SalaEntity sa join SedeEntity s on s.idSede = sa.idSede "
                                    + "where s.idSede = :idSede "
                                    + "order by sa.nombre",
                            Object[].class)
                    .setParameter("idSede", idSede)
                    .getResultList();
        } catch (Exception ex) {
            registrar("salasPorSede", ex);
            return List.of();
        }
    }

    /**
     * Número de salas y aforo total de cada sede, incluidas las sedes sin salas (C12).
     *
     * @return filas con las columnas {sede, total_salas, aforo_total} ordenadas por sede; las sedes
     *     sin salas salen con 0 y 0. Lista vacía si falla la consulta
     */
    public List<Object[]> aforoTotalPorSede() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select s.nombre, count(sa.idSala), coalesce(sum(sa.aforo), 0) "
                                    + "from SedeEntity s left join SalaEntity sa on sa.idSede = s.idSede "
                                    + "group by s.idSede, s.nombre "
                                    + "order by s.nombre",
                            Object[].class)
                    .getResultList();
        } catch (Exception ex) {
            registrar("aforoTotalPorSede", ex);
            return List.of();
        }
    }

    private boolean esValida(Sede dato) {
        return dato != null
                && dato.getNombre() != null
                && !dato.getNombre().isBlank()
                && dato.getMunicipio() != null
                && !dato.getMunicipio().isBlank();
    }

    /** Cierra la factoría solo si la creó este DAO; una factoría compartida la cierra su dueño. */
    @Override
    public void close() {
        if (ownFactory && managerFactory.isOpen()) {
            managerFactory.close();
        }
    }
}
