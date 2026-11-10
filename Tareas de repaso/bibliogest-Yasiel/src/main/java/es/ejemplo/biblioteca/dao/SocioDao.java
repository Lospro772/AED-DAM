package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Socio;

import es.ejemplo.biblioteca.orm.entity.SocioEntity;
import es.ejemplo.biblioteca.orm.mapper.SocioMapper;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DAO de socios: altas, bajas, búsquedas y los informes que tienen que ver con sus préstamos.
 */
public class SocioDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocioDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param url URL JDBC de la base de datos SQLite
     */
    public SocioDao(String url) {
        managerFactory = OrmFactory.crear(url);
        ownFactory = true;
    }

    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public SocioDao(EntityManagerFactory factory) {
        managerFactory = factory;
        ownFactory = false;
    }

    private void registrar(String metodo, Exception ex) {
        LOG.log(Level.WARNING, metodo + " no se pudo completar: " + ex.getMessage());
    }

    /**
     * Lista todos los socios de la biblioteca.
     *
     * @return lista de socios, vacía si falla la consulta
     */
    public List<Socio> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select s from SocioEntity s order by s.nombre",
                            SocioEntity.class)
                    .getResultStream()
                    .map(SocioMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listar",ex);
            return List.of();
        }
    }

    /**
     * Busca un socio por su número de socio.
     *
     * @param id identificador del socio
     * @return el socio si existe; si no, un Optional vacío
     */
    public Optional<Socio> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SocioEntity.class, id))
                    .map(SocioMapper::toDomain);
        } catch (Exception ex) {
            registrar("buscarPorId",ex);
            return Optional.empty();
        }
    }

    /**
     * Da de alta un socio nuevo. El email no se puede repetir.
     *
     * @param socio datos del socio, sin id
     * @return id generado, o 0 si no se pudo dar de alta
     */
    public int insertar(Socio socio) {
        if (socio == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity entidad = SocioMapper.toEntity(socio);
                em.persist(entidad);
                em.flush();
                socio.setIdSocio(entidad.getIdSocio());
                tx.commit();
                return socio.getIdSocio();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }

    /**
     * Guarda los cambios de un socio que ya existe: nombre, email, teléfono o estado.
     *
     * @param socio socio con los datos modificados
     * @return 1 si se actualizó, 0 si no existe o hubo un error
     */
    public int actualizar(Socio socio) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity s = em.find(SocioEntity.class, socio.getIdSocio());
                if (s == null) {
                    tx.commit();
                    return 0;
                }
                SocioEntity clienteActualizado = SocioMapper.toEntity(socio);
                s.setNombre(clienteActualizado.getNombre());
                s.setEmail(clienteActualizado.getEmail());
                s.setTelefono(clienteActualizado.getTelefono());
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    /**
     * Elimina un socio. Si tiene préstamos o reservas no se puede borrar y se devuelve 0.
     *
     * @param id identificador del socio
     * @return 1 si se borró, 0 si no
     */
    public int eliminar(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity c = em.find(SocioEntity.class, id);
                if (c == null) {
                    tx.commit();
                    return 0;
                }
                em.remove(c);
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }

    private boolean validar(Socio dato) {
        return dato != null && dato.getNombre() != null && !dato.getNombre().isBlank() && dato.getEmail() != null && !dato.getEmail().isBlank() && dato.getFechaAlta() != null && dato.getActivo() != null;
    }

    /**
     * Socios activos, empezando por los que se dieron de alta más tarde (consulta 2).
     *
     * @return socios activos
     */
    public List<Socio> listarActivos() {
        try (var em = managerFactory.createEntityManager()) {
            //var tx = em.getTransaction();
            return em.createQuery(
                            "select s from SocioEntity s where s.activo = :activo order by s.fechaAlta desc, s.idSocio",
                            SocioEntity.class)
                    .setParameter("activo", true)
                    .getResultStream()
                    .map(SocioMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            //if (tx.isActive()) tx.rollback();
            //registrar("activo", ex);
            //return listarActivos();
            registrar("listarActivos", ex);
            return List.of();
        }
    }

    /**
     * Busca un socio por su email, que es único.
     *
     * @param email email del socio
     * @return el socio o un Optional vacío
     */
    public Optional<Socio> buscarPorEmail(String email) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select s from SocioEntity s where s.email = :email",
                            SocioEntity.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .map(SocioMapper::toDomain);
        }
    }

    /**
     * Socios que tienen algún préstamo vencido en la fecha indicada (consulta 17).
     *
     * @param fecha fecha de referencia, yyyy-MM-dd
     * @return socios con préstamos vencidos
     */
    public List<Socio> listarConPrestamosVencidos(String fecha) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }

    /**
     * Socios que nunca han pedido ningún libro prestado (consulta 9).
     *
     * @return socios sin préstamos
     */
    public List<Socio> sociosSinPrestamos() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select s from SocioEntity s where (select count(p) from PrestamoEntity p "
                            + "where p.idSocio = s.idSocio) = 0 order by s.nombre",
                            SocioEntity.class)
                    .getResultStream()
                    .map(SocioMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("sociosSinPrestamos", ex);
            return List.of();
        }
    }

    /**
     * Socios con más préstamos, de mayor a menor (consulta 13).
     *
     * @param limite número de socios del ranking
     * @return filas con el socio y su número de préstamos
     */
    public List<Object[]> rankingSocios(int limite) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }

    /**
     * Historial completo de préstamos de un socio, indicando si se devolvió y si hubo retraso (consulta 22).
     *
     * @param idSocio id del socio
     * @return una fila por préstamo
     */
    public List<Object[]> historialSocio(int idSocio) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }


    @Override
    /**
     * Cierra la factoría solo si la creó este DAO. Si es compartida se deja abierta.
     */
    public void close() {
        if (ownFactory && managerFactory.isOpen()) managerFactory.close();
    }
}
