package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Sesion;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.PeliculaEntity;
import es.ejemplo.cinefest.orm.entity.SalaEntity;
import es.ejemplo.cinefest.orm.entity.SesionEntity;
import es.ejemplo.cinefest.orm.mapper.SesionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SesionDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(SesionDao.class.getName());
    private static final List<String> ESTADOS = List.of("PROGRAMADA", "CANCELADA", "FINALIZADA");
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param jdbcUrl URL JDBC de la base de datos SQLite
     */
    public SesionDao(String jdbcUrl) {
        managerFactory = OrmFactory.crear(jdbcUrl);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public SesionDao(EntityManagerFactory factory) {
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
     * Devuelve todas las sesiones ordenadas por fecha y hora.
     *
     * @return lista de sesiones; lista vacía si no hay o si falla la consulta (el error se registra
     *     en el log)
     */
    public List<Sesion> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select x from SesionEntity x order by x.fecha, x.hora, x.idSesion", SesionEntity.class)
                    .getResultStream()
                    .map(SesionMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca una sesión por su clave primaria.
     *
     * @param id identificador de la sesión
     * @return la sesión si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Sesion> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SesionEntity.class, id)).map(SesionMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta una sesión. El id lo genera la base de datos, así que se ignora el que traiga el objeto.
     * La película y la sala tienen que existir, fecha y hora obligatorias, precio no negativo y
     * estado PROGRAMADA, CANCELADA o FINALIZADA.
     *
     * @param dato la sesión a insertar; si no trae estado se guarda como PROGRAMADA
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción; en ese caso se hace rollback
     */
    public int insertar(Sesion dato) {
        if (dato != null && dato.getEstado() == null) {
            dato.setEstado("PROGRAMADA");
        }
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
                SesionEntity entidad = SesionMapper.aEntidad(dato);
                entidad.setIdSesion(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdSesion(entidad.getIdSesion());
                return entidad.getIdSesion();
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
     * Actualiza todos los campos de una sesión existente. Se aplican las mismas reglas que al insertar.
     *
     * @param dato la sesión con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la fila no existe o la base rechaza
     *     el cambio, con rollback
     */
    public int actualizar(Sesion dato) {
        if (!datosValidos(dato) || dato.getIdSesion() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (em.find(SesionEntity.class, dato.getIdSesion()) == null
                        || !existenReferencias(em, dato)) {
                    tx.rollback();
                    return 0;
                }
                em.merge(SesionMapper.aEntidad(dato));
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
     * Elimina una sesión. No se permite si todavía tiene entradas.
     *
     * @param id identificador de la sesión
     * @return 1 si se eliminó; 0 si no existe, si tiene entradas asociadas o si la base rechaza el borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                SesionEntity entidad = em.find(SesionEntity.class, id);
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

    public List<Object[]> sesionesConDetalles() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> sesionesPorFecha(String fecha) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> sesionesSinVentas() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> disponibilidadSesiones() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> sesionesAgotadas() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> rankingRecaudacion() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> ingresosPorSede() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> informeDiario() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public int venderEntrada(int idSesion, int idEspectador, String fechaCompra) {
        // TODO: implementar conforme al contrato del README.md.
        return 0;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean datosValidos(Sesion dato) {
        return dato != null
                && dato.getIdPelicula() != null
                && dato.getIdSala() != null
                && !vacio(dato.getFecha())
                && !vacio(dato.getHora())
                && dato.getPrecio() != null
                && dato.getPrecio() >= 0
                && dato.getEstado() != null
                && ESTADOS.contains(dato.getEstado());
    }

    /** Comprueba en código las claves foráneas, por si la conexión no tiene foreign_keys activo. */
    private boolean existenReferencias(EntityManager em, Sesion dato) {
        return em.find(PeliculaEntity.class, dato.getIdPelicula()) != null
                && em.find(SalaEntity.class, dato.getIdSala()) != null;
    }

    private boolean tieneDependencias(EntityManager em, int id) {
        return contar(em, "select count(d) from EntradaEntity d where d.idSesion = :id", id) > 0;
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
