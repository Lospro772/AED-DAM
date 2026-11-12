package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Entrada;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.EntradaEntity;
import es.ejemplo.cinefest.orm.entity.EspectadorEntity;
import es.ejemplo.cinefest.orm.entity.SesionEntity;
import es.ejemplo.cinefest.orm.mapper.EntradaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntradaDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(EntradaDao.class.getName());
    private static final List<String> ESTADOS = List.of("PAGADA", "USADA", "CANCELADA");
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param jdbcUrl URL JDBC de la base de datos SQLite
     */
    public EntradaDao(String jdbcUrl) {
        managerFactory = OrmFactory.crear(jdbcUrl);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public EntradaDao(EntityManagerFactory factory) {
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
     * Devuelve todas las entradas ordenadas por id.
     *
     * @return lista de entradas; lista vacía si no hay o si falla la consulta (el error se registra
     *     en el log)
     */
    public List<Entrada> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select x from EntradaEntity x order by x.idEntrada", EntradaEntity.class)
                    .getResultStream()
                    .map(EntradaMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca una entrada por su clave primaria.
     *
     * @param id identificador de la entrada
     * @return la entrada si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Entrada> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(EntradaEntity.class, id)).map(EntradaMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta una entrada. El id lo genera la base de datos, así que se ignora el que traiga el objeto.
     * La sesión y el espectador tienen que existir, fecha de compra obligatoria y estado PAGADA,
     * USADA o CANCELADA. Es el alta básica: no controla aforo, para eso está venderEntrada.
     *
     * @param dato la entrada a insertar; si no trae estado se guarda como PAGADA
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción; en ese caso se hace rollback
     */
    public int insertar(Entrada dato) {
        if (dato != null && dato.getEstado() == null) {
            dato.setEstado("PAGADA");
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
                EntradaEntity entidad = EntradaMapper.aEntidad(dato);
                entidad.setIdEntrada(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdEntrada(entidad.getIdEntrada());
                return entidad.getIdEntrada();
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
     * Actualiza todos los campos de una entrada existente. Se aplican las mismas reglas que al insertar.
     *
     * @param dato la entrada con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la fila no existe o la base rechaza
     *     el cambio, con rollback
     */
    public int actualizar(Entrada dato) {
        if (!datosValidos(dato) || dato.getIdEntrada() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (em.find(EntradaEntity.class, dato.getIdEntrada()) == null
                        || !existenReferencias(em, dato)) {
                    tx.rollback();
                    return 0;
                }
                em.merge(EntradaMapper.aEntidad(dato));
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
     * Elimina una entrada.
     *
     * @param id identificador de la entrada
     * @return 1 si se eliminó; 0 si no existe o si la base rechaza el borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                EntradaEntity entidad = em.find(EntradaEntity.class, id);
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

    public List<Object[]> entradasConDetalles() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> entradasPorEspectador(int idEspectador) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> ventasPorDia() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public int cancelarEntrada(int idEntrada) {
        // TODO: implementar conforme al contrato del README.md.
        return 0;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean datosValidos(Entrada dato) {
        return dato != null
                && dato.getIdSesion() != null
                && dato.getIdEspectador() != null
                && !vacio(dato.getFechaCompra())
                && dato.getEstado() != null
                && ESTADOS.contains(dato.getEstado());
    }

    /** Comprueba en código las claves foráneas, por si la conexión no tiene foreign_keys activo. */
    private boolean existenReferencias(EntityManager em, Entrada dato) {
        return em.find(SesionEntity.class, dato.getIdSesion()) != null
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
