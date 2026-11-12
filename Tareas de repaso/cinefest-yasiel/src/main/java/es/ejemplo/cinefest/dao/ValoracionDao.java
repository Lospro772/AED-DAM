package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Valoracion;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.EspectadorEntity;
import es.ejemplo.cinefest.orm.entity.PeliculaEntity;
import es.ejemplo.cinefest.orm.entity.ValoracionEntity;
import es.ejemplo.cinefest.orm.mapper.ValoracionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValoracionDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(ValoracionDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param jdbcUrl URL JDBC de la base de datos SQLite
     */
    public ValoracionDao(String jdbcUrl) {
        managerFactory = OrmFactory.crear(jdbcUrl);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public ValoracionDao(EntityManagerFactory factory) {
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
     * Devuelve todas las valoraciones ordenadas por id.
     *
     * @return lista de valoraciones; lista vacía si no hay o si falla la consulta (el error se registra
     *     en el log)
     */
    public List<Valoracion> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select x from ValoracionEntity x order by x.idValoracion", ValoracionEntity.class)
                    .getResultStream()
                    .map(ValoracionMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca una valoración por su clave primaria.
     *
     * @param id identificador de la valoración
     * @return la valoración si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Valoracion> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(ValoracionEntity.class, id)).map(ValoracionMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta una valoración. El id lo genera la base de datos, así que se ignora el que traiga el objeto.
     * La película y el espectador tienen que existir, puntuación entre 1 y 5 y fecha obligatoria;
     * el comentario es opcional.
     *
     * @param dato la valoración a insertar
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción; en ese caso se hace rollback
     */
    public int insertar(Valoracion dato) {
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
                ValoracionEntity entidad = ValoracionMapper.aEntidad(dato);
                entidad.setIdValoracion(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdValoracion(entidad.getIdValoracion());
                return entidad.getIdValoracion();
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
     * Actualiza todos los campos de una valoración existente. Se aplican las mismas reglas que al insertar.
     *
     * @param dato la valoración con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la fila no existe o la base rechaza
     *     el cambio, con rollback
     */
    public int actualizar(Valoracion dato) {
        if (!datosValidos(dato) || dato.getIdValoracion() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (em.find(ValoracionEntity.class, dato.getIdValoracion()) == null
                        || !existenReferencias(em, dato)) {
                    tx.rollback();
                    return 0;
                }
                em.merge(ValoracionMapper.aEntidad(dato));
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
     * Elimina una valoración.
     *
     * @param id identificador de la valoración
     * @return 1 si se eliminó; 0 si no existe o si la base rechaza el borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                ValoracionEntity entidad = em.find(ValoracionEntity.class, id);
                if (entidad == null) {
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

    public List<Object[]> valoracionesConDetalles() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> peliculasSinValoraciones() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> valoracionesPorPelicula(int idPelicula) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> estadisticasPuntuaciones() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean datosValidos(Valoracion dato) {
        return dato != null
                && dato.getIdPelicula() != null
                && dato.getIdEspectador() != null
                && dato.getPuntuacion() != null
                && dato.getPuntuacion() >= 1
                && dato.getPuntuacion() <= 5
                && !vacio(dato.getFecha());
    }

    /** Comprueba en código las claves foráneas, por si la conexión no tiene foreign_keys activo. */
    private boolean existenReferencias(EntityManager em, Valoracion dato) {
        return em.find(PeliculaEntity.class, dato.getIdPelicula()) != null
                && em.find(EspectadorEntity.class, dato.getIdEspectador()) != null;
    }

    /** Cierra la factoría solo si la creó este DAO; una factoría compartida la cierra su dueño. */
    @Override
    public void close() {
        if (ownFactory && managerFactory.isOpen()) {
            managerFactory.close();
        }
    }
}
