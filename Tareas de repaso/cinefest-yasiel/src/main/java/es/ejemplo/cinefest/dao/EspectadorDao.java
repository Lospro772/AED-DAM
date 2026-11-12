package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Espectador;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.EspectadorEntity;
import es.ejemplo.cinefest.orm.mapper.EspectadorMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EspectadorDao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(EspectadorDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param jdbcUrl URL JDBC de la base de datos SQLite
     */
    public EspectadorDao(String jdbcUrl) {
        managerFactory = OrmFactory.crear(jdbcUrl);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public EspectadorDao(EntityManagerFactory factory) {
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
     * Devuelve todos los espectadores ordenados por nombre.
     *
     * @return lista de espectadores; lista vacía si no hay o si falla la consulta (el error se registra
     *     en el log)
     */
    public List<Espectador> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select x from EspectadorEntity x order by x.nombre, x.idEspectador", EspectadorEntity.class)
                    .getResultStream()
                    .map(EspectadorMapper::desdeEntidad)
                    .toList();
        } catch (Exception ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    /**
     * Busca un espectador por su clave primaria.
     *
     * @param id identificador de el espectador
     * @return el espectador si existe; Optional.empty() si no existe o si falla la consulta
     */
    public Optional<Espectador> buscarPorId(int id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(EspectadorEntity.class, id)).map(EspectadorMapper::desdeEntidad);
        } catch (Exception ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta un espectador. El id lo genera la base de datos, así que se ignora el que traiga el objeto.
     * Nombre, email y fecha de alta obligatorios y activo con valor 0 o 1. El email duplicado lo
     * rechaza la base.
     *
     * @param dato el espectador a insertar; si no trae el campo activo se guarda como 1
     * @return id generado (mayor que 0), o 0 si los datos no son válidos o la base rechaza la
     *     inserción; en ese caso se hace rollback
     */
    public int insertar(Espectador dato) {
        if (dato != null && dato.getActivo() == null) {
            dato.setActivo(1);
        }
        if (!datosValidos(dato)) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                EspectadorEntity entidad = EspectadorMapper.aEntidad(dato);
                entidad.setIdEspectador(null);
                em.persist(entidad);
                tx.commit();
                dato.setIdEspectador(entidad.getIdEspectador());
                return entidad.getIdEspectador();
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
     * Actualiza todos los campos de un espectador existente. Se aplican las mismas reglas que al insertar.
     *
     * @param dato el espectador con el id de la fila a modificar y los valores nuevos
     * @return 1 si se actualizó; 0 si los datos no son válidos, la fila no existe o la base rechaza
     *     el cambio, con rollback
     */
    public int actualizar(Espectador dato) {
        if (!datosValidos(dato) || dato.getIdEspectador() == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (em.find(EspectadorEntity.class, dato.getIdEspectador()) == null) {
                    tx.rollback();
                    return 0;
                }
                em.merge(EspectadorMapper.aEntidad(dato));
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
     * Elimina un espectador. No se permite si todavía tiene entradas o valoraciones.
     *
     * @param id identificador de el espectador
     * @return 1 si se eliminó; 0 si no existe, si tiene entradas o valoraciones asociadas o si la base rechaza el borrado, con rollback
     */
    public int eliminar(int id) {
        try (var em = managerFactory.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                EspectadorEntity entidad = em.find(EspectadorEntity.class, id);
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

    public Optional<Espectador> buscarPorEmail(String email) {
        // TODO: implementar conforme al contrato del README.md.
        return Optional.empty();
    }
    public List<Espectador> espectadoresSinEntradas() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> rankingEspectadores() {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }
    public List<Object[]> espectadoresVariasSedes(int minimo) {
        // TODO: implementar conforme al contrato del README.md.
        return List.of();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean datosValidos(Espectador dato) {
        return dato != null
                && !vacio(dato.getNombre())
                && !vacio(dato.getEmail())
                && !vacio(dato.getFechaAlta())
                && dato.getActivo() != null
                && (dato.getActivo() == 0 || dato.getActivo() == 1);
    }

    private boolean tieneDependencias(EntityManager em, int id) {
        return contar(em, "select count(d) from EntradaEntity d where d.idEspectador = :id", id) > 0
                || contar(em, "select count(d) from ValoracionEntity d where d.idEspectador = :id", id) > 0;
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
