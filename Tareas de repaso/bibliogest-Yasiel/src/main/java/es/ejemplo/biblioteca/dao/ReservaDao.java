package es.ejemplo.biblioteca.dao;
import es.ejemplo.biblioteca.modelo.Reserva;

import es.ejemplo.biblioteca.orm.entity.LibroEntity;
import es.ejemplo.biblioteca.orm.entity.ReservaEntity;
import es.ejemplo.biblioteca.orm.mapper.LibroMapper;
import es.ejemplo.biblioteca.orm.mapper.ReservaMapper;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Acceso a las reservas de libros. Una reserva puede estar PENDIENTE, ATENDIDA o CANCELADA;
 * cualquier otro estado se rechaza al validar.
 */
public class ReservaDao implements AutoCloseable {
    private static final Logger LOG=Logger.getLogger(ReservaDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;
    
    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param url URL JDBC de la base de datos SQLite
     */
    public ReservaDao(String url) {managerFactory=OrmFactory.crear(url);ownFactory=true;}
    
    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public ReservaDao(EntityManagerFactory factory) {managerFactory=factory;ownFactory=false;}
    private void registrar(String metodo,Exception ex) {LOG.log(Level.WARNING,metodo+" no se pudo completar: "+ex.getMessage());}
    
    /**
     * Devuelve todas las reservas registradas.
     *
     * @return lista de reservas, vacía si falla la consulta
     */
    public List<Reserva> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select r from ReservaEntity r order by r.fechaReserva, r.idReserva",
                            ReservaEntity.class)
                    .getResultStream()
                    .map(ReservaMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listar",ex);
            return List.of();
        }
    }
    
    /**
     * Obtiene una reserva a partir de su id.
     *
     * @param id identificador de la reserva
     * @return la reserva o un Optional vacío
     */
    public Optional<Reserva> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(ReservaEntity.class, id))
                    .map(ReservaMapper::toDomain);
        } catch (Exception ex) {
            registrar("buscarPorId",ex);
            return Optional.empty();
        }
    }
    
    /**
     * Crea una reserva nueva de un socio sobre un libro.
     *
     * @param reserva reserva que se va a guardar
     * @return id generado, o 0 si no se pudo crear
     */
    public int insertar(Reserva reserva) {
        if (reserva == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                ReservaEntity entidad = ReservaMapper.toEntity(reserva);
                em.persist(entidad);
                em.flush();
                reserva.setIdReserva(entidad.getIdReserva());
                tx.commit();
                return reserva.getIdReserva();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }
    
    /**
     * Actualiza una reserva existente, normalmente para cambiarle el estado.
     *
     * @param reserva reserva con los datos nuevos
     * @return 1 si se actualizó, 0 si no existe
     */
    public int actualizar(Reserva reserva) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                ReservaEntity r = em.find(ReservaEntity.class, reserva.getIdReserva());
                if (r == null) {
                    tx.commit();
                    return 0;
                }
                ReservaEntity clienteActualizado = ReservaMapper.toEntity(reserva);
                r.setIdLibro(clienteActualizado.getIdLibro());
                r.setIdSocio(clienteActualizado.getIdSocio());
                r.setEstado(clienteActualizado.getEstado());
                r.setFechaReserva(clienteActualizado.getFechaReserva());
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }
    
    /**
     * Borra una reserva a partir de su identificador.
     *
     * @param id identificador de la reserva
     * @return 1 si se borró, 0 si no
     */
    public int eliminar(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                ReservaEntity a = em.find(ReservaEntity.class, id);
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
    private boolean validar(Reserva dato) {return dato != null && dato.getIdSocio()!=null && dato.getIdLibro()!=null && dato.getFechaReserva()!=null && dato.getEstado()!=null && (dato.getEstado().equals("PENDIENTE") || dato.getEstado().equals("ATENDIDA") || dato.getEstado().equals("CANCELADA"));}
    
    /**
     * Reservas que todavía están en estado PENDIENTE.
     *
     * @return reservas pendientes
     */
    public List<Reserva> listarPendientes() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select r from ReservaEntity r where r.estado = 'PENDIENTE' "
                                    + "order by r.fechaReserva, r.idReserva",
                            ReservaEntity.class)
                    .getResultStream()
                    .map(ReservaMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listarPendientes", ex);
            return List.of();
        }
    }
    
    /**
     * Todas las reservas que ha hecho un socio.
     *
     * @param idSocio id del socio
     * @return reservas del socio
     */
    public List<Reserva> buscarPorSocio(int idSocio) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select r from ReservaEntity r where r.idSocio = :idSocio "
                                    + "order by r.fechaReserva, r.idReserva",
                            ReservaEntity.class)
                    .setParameter("idSocio", idSocio)
                    .getResultStream()
                    .map(ReservaMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("buscarPorSocio", ex);
            return List.of();
        }
    }
    
    /**
     * Reservas hechas sobre un libro concreto.
     *
     * @param idLibro id del libro
     * @return reservas de ese libro
     */
    public List<Reserva> listarPorLibro(int idLibro) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select r from ReservaEntity r where r.idLibro = :idLibro "
                                    + "order by r.fechaReserva, r.idReserva",
                            ReservaEntity.class)
                    .setParameter("idLibro", idLibro)
                    .getResultStream()
                    .map(ReservaMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listarPorLibro", ex);
            return List.of();
        }
    }
    
    /**
     * Cuántas reservas quedan por atender en total.
     *
     * @return número de reservas pendientes
     */
    public long contarReservasPendientes() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select count(r) from ReservaEntity r where r.estado = 'PENDIENTE'",
                            Long.class)
                    .getSingleResult();
        } catch (Exception ex) {
            registrar("contarReservasPendientes", ex);
            return 0;
        }
    }
    
    /**
     * Reservas pendientes con el nombre del socio, el título del libro y su autor (consulta 10).
     *
     * @return una fila por reserva
     */
    public List<Object[]> reservasConDetalles() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select r.idReserva, s.nombre, l.titulo, a.nombre, r.fechaReserva "
                                    + "from ReservaEntity r join r.socio s join r.libro l join l.autor a "
                                    + "where r.estado = 'PENDIENTE' "
                                    + "order by r.fechaReserva, r.idReserva",
                            Object[].class)
                    .getResultList();
        } catch (Exception ex) {
            registrar("reservasConDetalles", ex);
            return List.of();
        }
    }
    
    /**
     * Para cada libro, la reserva pendiente más antigua, que es la primera que se atiende (consulta
     * 23).
     *
     * @return una fila por libro con su reserva prioritaria
     */
    public List<Object[]> prioridadReservas() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l.titulo, r.idReserva, s.nombre, r.fechaReserva "
                                    + "from ReservaEntity r join r.libro l join r.socio s "
                                    + "where r.estado = 'PENDIENTE' and r.idReserva = ("
                                    + "  select min(r2.idReserva) from ReservaEntity r2 "
                                    + "  where r2.idLibro = r.idLibro and r2.estado = 'PENDIENTE' "
                                    + "  and r2.fechaReserva = (select min(r3.fechaReserva) from ReservaEntity r3 "
                                    + "    where r3.idLibro = r.idLibro and r3.estado = 'PENDIENTE')) "
                                    + "order by r.fechaReserva, l.titulo",
                            Object[].class)
                    .getResultList();
        } catch (Exception ex) {
            registrar("prioridadReservas", ex);
            return List.of();
        }
    }


    
    @Override
    /**
     * Cierra la factoría solo si la creó este DAO. Si es compartida se deja abierta.
     */
    public void close() {if(ownFactory && managerFactory.isOpen())managerFactory.close();}
}
