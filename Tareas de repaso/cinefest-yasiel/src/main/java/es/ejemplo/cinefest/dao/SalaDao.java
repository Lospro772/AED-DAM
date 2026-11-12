package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Sala;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.SalaEntity;
import es.ejemplo.cinefest.orm.entity.SedeEntity;
import es.ejemplo.cinefest.orm.mapper.SalaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalaDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(SalaDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param jdbcUrl URL JDBC de la base de datos SQLite
     */
    public SalaDao(String jdbcUrl) {
        managerFactory = OrmFactory.crear(jdbcUrl);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public SalaDao(EntityManagerFactory factory) {
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
     * Devuelve todas las salas ordenadas por nombre.
     *
     * @return lista de salas; lista vacía si no hay o si falla la consulta (el error se registra
     *     en el log)
     */
    public List<Sala> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select x from SalaEntity x order by x.nombre, x.idSala", SalaEntity.class)
                    .getResultStream()
                    .map(SalaMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca una sala por su clave primaria.
     *
     * @param id identificador de la sala
     * @return la sala si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Sala> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SalaEntity.class, id)).map(SalaMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta una sala. El id lo genera la base de datos, así que se ignora el que traiga el objeto.
     * Nombre obligatorio, aforo mayor que 0 y la sede indicada tiene que existir.
     *
     * @param dato la sala a insertar
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción; en ese caso se hace rollback
     */
    public int insertar(Sala dato) {
        if (!datosValidos(dato)) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (!existenReferencias(em, dato)) {
                    tx.rollback();
                    return 0;
                }
                SalaEntity entidad = SalaMapper.aEntidad(dato);
                entidad.setIdSala(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdSala(entidad.getIdSala());
                return entidad.getIdSala();
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
     * Actualiza todos los campos de una sala existente. Se aplican las mismas reglas que al insertar.
     *
     * @param dato la sala con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la fila no existe o la base rechaza
     *     el cambio, con rollback
     */
    public int actualizar(Sala dato) {
        if (!datosValidos(dato) || dato.getIdSala() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (em.find(SalaEntity.class, dato.getIdSala()) == null
                        || !existenReferencias(em, dato)) {
                    tx.rollback();
                    return 0;
                }
                em.merge(SalaMapper.aEntidad(dato));
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
     * Elimina una sala. No se permite si todavía tiene sesiones.
     *
     * @param id identificador de la sala
     * @return 1 si se eliminó; 0 si no existe, si tiene sesiones asociadas o si la base rechaza el borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                SalaEntity entidad = em.find(SalaEntity.class, id);
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

    public List<Object[]> salasConSede() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Sala> salasSinSesiones() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> ocupacionPorSala() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean datosValidos(Sala dato) {
        return dato != null
                && !vacio(dato.getNombre())
                && dato.getAforo() != null
                && dato.getAforo() > 0
                && dato.getIdSede() != null;
    }

    /** Comprueba en código las claves foráneas, por si la conexión no tiene foreign_keys activo. */
    private boolean existenReferencias(EntityManager em, Sala dato) {
        return em.find(SedeEntity.class, dato.getIdSede()) != null;
    }

    private boolean tieneDependencias(EntityManager em, int id) {
        return contar(em, "select count(d) from SesionEntity d where d.idSala = :id", id) > 0;
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
