package es.ejemplo.biblioteca.dao;
import es.ejemplo.biblioteca.modelo.Prestamo;

import es.ejemplo.biblioteca.orm.entity.LibroEntity;
import es.ejemplo.biblioteca.orm.entity.PrestamoEntity;
import es.ejemplo.biblioteca.orm.entity.SocioEntity;
import jakarta.persistence.LockModeType;
import es.ejemplo.biblioteca.orm.mapper.LibroMapper;
import es.ejemplo.biblioteca.orm.mapper.PrestamoMapper;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Gestión de los préstamos. Un préstamo está activo mientras no tenga fecha de devolución, y está
 * vencido si además ya ha pasado su fecha límite.
 */
public class PrestamoDao implements AutoCloseable {
    private static final Logger LOG=Logger.getLogger(PrestamoDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;
    
    /**
     * Crea el DAO con su propia factoría a partir de la URL JDBC. Esa factoría se cierra al llamar a
     * close().
     *
     * @param url URL JDBC de la base de datos SQLite
     */
    public PrestamoDao(String url) {managerFactory=OrmFactory.crear(url);ownFactory=true;}
    
    /**
     * Usa una factoría que ya existe y que comparten varios DAO. En este caso close() no la cierra,
     * eso lo hace quien la creó.
     *
     * @param factory factoría de EntityManager compartida
     */
    public PrestamoDao(EntityManagerFactory factory) {managerFactory=factory;ownFactory=false;}
    private void registrar(String metodo,Exception ex) {LOG.log(Level.WARNING,metodo+" no se pudo completar: "+ex.getMessage());}
    
    /**
     * Devuelve todos los préstamos ordenados por fecha de préstamo.
     *
     * @return lista de préstamos, vacía si falla la consulta
     */
    public List<Prestamo> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select p from PrestamoEntity p order by p.fechaPrestamo, p.idPrestamo",
                            PrestamoEntity.class)
                    .getResultStream()
                    .map(PrestamoMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listar",ex);
            return List.of();
        }
    }
    
    /**
     * Busca un préstamo concreto por su id.
     *
     * @param id identificador del préstamo
     * @return el préstamo si existe; si no, un Optional vacío
     */
    public Optional<Prestamo> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(PrestamoEntity.class, id))
                    .map(PrestamoMapper::toDomain);
        } catch (Exception ex) {
            registrar("buscarPorId",ex);
            return Optional.empty();
        }
    }
    
    /**
     * Inserta el préstamo tal cual, sin comprobar si quedan ejemplares.
     *
     * @param prestamo préstamo que se guarda
     * @return id generado, o 0 si no se pudo insertar
     */
    public int insertar(Prestamo prestamo) {
        if (prestamo == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                PrestamoEntity entidad = PrestamoMapper.toEntity(prestamo);
                em.persist(entidad);
                em.flush();
                prestamo.setIdPrestamo(entidad.getIdPrestamo());
                tx.commit();
                return prestamo.getIdPrestamo();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                return 0;
            }
        }
    }
    
    /**
     * Modifica el socio, el libro y las fechas de un préstamo que ya existe.
     *
     * @param prestamo préstamo con los cambios
     * @return 1 si se actualizó, 0 si no existe
     */
    public int actualizar(Prestamo prestamo) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                PrestamoEntity p = em.find(PrestamoEntity.class, prestamo.getIdPrestamo());
                if (p == null) {
                    tx.commit();
                    return 0;
                }
                PrestamoEntity clienteActualizado = PrestamoMapper.toEntity(prestamo);
                p.setIdLibro(clienteActualizado.getIdLibro());
                p.setIdSocio(clienteActualizado.getIdSocio());
                p.setFechaDevolucion(clienteActualizado.getFechaDevolucion());
                p.setFechaLimite(clienteActualizado.getFechaLimite());
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }
    
    /**
     * Borra un préstamo a partir de su identificador.
     *
     * @param id identificador del préstamo
     * @return 1 si se borró, 0 si no
     */
    public int eliminar(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                PrestamoEntity a = em.find(PrestamoEntity.class, id);
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
    private boolean validar(Prestamo dato) {return dato != null && dato.getIdSocio()!=null && dato.getIdLibro()!=null && dato.getFechaPrestamo()!=null && dato.getFechaLimite()!=null && dato.getFechaLimite().compareTo(dato.getFechaPrestamo())>=0 && (dato.getFechaDevolucion()==null || dato.getFechaDevolucion().compareTo(dato.getFechaPrestamo())>=0);}
    
    /**
     * Préstamos de un socio, del más antiguo al más reciente.
     *
     * @param idSocio id del socio
     * @return préstamos del socio, vacía si no tiene
     */
    public List<Prestamo> listarPorSocio(int idSocio) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select p from PrestamoEntity p where p.idSocio = :idSocio "
                                    + "order by p.fechaPrestamo, p.idPrestamo",
                            PrestamoEntity.class)
                    .setParameter("idSocio", idSocio)
                    .getResultStream()
                    .map(PrestamoMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listarPorSocio", ex);
            return List.of();
        }
    }

    /**
     * Préstamos que todavía no se han devuelto, ordenados por fecha límite.
     *
     * @return préstamos activos
     */
    public List<Prestamo> listarActivos() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select p from PrestamoEntity p where p.fechaDevolucion is null "
                                    + "order by p.fechaLimite, p.idPrestamo",
                            PrestamoEntity.class)
                    .getResultStream()
                    .map(PrestamoMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listarActivos", ex);
            return List.of();
        }
    }

    /**
     * Préstamos sin devolver cuya fecha límite es anterior a la fecha de referencia.
     *
     * @param fecha fecha de referencia en formato yyyy-MM-dd
     * @return préstamos vencidos en esa fecha
     */
    public List<Prestamo> listarVencidos(String fecha) {
        if (fecha == null) return List.of();
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select p from PrestamoEntity p where p.fechaDevolucion is null "
                                    + "and p.fechaLimite < :fecha order by p.fechaLimite, p.idPrestamo",
                            PrestamoEntity.class)
                    .setParameter("fecha", fecha)
                    .getResultStream()
                    .map(PrestamoMapper::toDomain)
                    .toList();
        } catch (Exception ex) {
            registrar("listarVencidos", ex);
            return List.of();
        }
    }

    /**
     * Cuenta todos los préstamos que ha tenido un libro, devueltos o no.
     *
     * @param idLibro id del libro
     * @return número de préstamos del libro
     */
    public long contarPrestamosPorLibro(int idLibro) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select count(p) from PrestamoEntity p where p.idLibro = :idLibro",
                            Long.class)
                    .setParameter("idLibro", idLibro)
                    .getSingleResult();
        } catch (Exception ex) {
            registrar("contarPrestamosPorLibro", ex);
            return 0;
        }
    }

    /**
     * Listado de préstamos con el nombre del socio y el título del libro (consulta 7).
     *
     * @return filas con id, socio, título, fecha de préstamo, fecha límite y fecha de devolución
     */
    public List<Object[]> prestamosConSocioYLibro() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select p.idPrestamo, s.nombre, l.titulo, p.fechaPrestamo, p.fechaLimite, p.fechaDevolucion "
                                    + "from PrestamoEntity p join p.socio s join p.libro l "
                                    + "order by p.fechaPrestamo, p.idPrestamo",
                            Object[].class)
                    .getResultList();
        } catch (Exception ex) {
            registrar("prestamosConSocioYLibro", ex);
            return List.of();
        }
    }

    /**
     * Libros más prestados en un mes, con su autor y el número de préstamos (consulta 21).
     *
     * @param anioMes mes en formato yyyy-MM
     * @param limite cuántos libros devolver como máximo
     * @return filas con título, autor y préstamos, de más a menos
     */
    public List<Object[]> rankingMensual(String anioMes, int limite) {
        if (anioMes == null || limite <= 0) return List.of();
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select l.titulo, a.nombre, count(p) "
                                    + "from PrestamoEntity p join p.libro l join l.autor a "
                                    + "where p.fechaPrestamo like :mes "
                                    + "group by l.idLibro, l.titulo, a.nombre "
                                    + "order by count(p) desc, l.titulo",
                            Object[].class)
                    .setParameter("mes", anioMes + "%")
                    .setMaxResults(limite)
                    .getResultList();
        } catch (Exception ex) {
            registrar("rankingMensual", ex);
            return List.of();
        }
    }

    /**
     * Registra un préstamo nuevo en una sola transacción. Solo se hace si el socio está activo y
     * queda algún ejemplar libre. El libro se bloquea mientras se cuentan sus préstamos activos para
     * que dos préstamos a la vez no superen los ejemplares (consulta 25).
     *
     * @param idSocio socio que se lleva el libro
     * @param idLibro libro que se presta
     * @param inicio fecha del préstamo, yyyy-MM-dd
     * @param limite fecha límite de devolución, yyyy-MM-dd
     * @return id del préstamo creado, o 0 si no se pudo registrar
     */
    public int registrarPrestamo(int idSocio, int idLibro, String inicio, String limite) {
        Prestamo nuevo = new Prestamo(null, idSocio, idLibro, inicio, limite, null);
        if (!validar(nuevo)) return 0;
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity socio = em.find(SocioEntity.class, idSocio);
                LibroEntity libro = em.find(LibroEntity.class, idLibro, LockModeType.PESSIMISTIC_WRITE);
                if (socio == null || !Boolean.TRUE.equals(socio.getActivo()) || libro == null) {
                    tx.rollback();
                    return 0;
                }
                long activos = em.createQuery(
                                "select count(p) from PrestamoEntity p "
                                        + "where p.idLibro = :idLibro and p.fechaDevolucion is null",
                                Long.class)
                        .setParameter("idLibro", idLibro)
                        .getSingleResult();
                if (activos >= libro.getEjemplares()) {
                    tx.rollback();
                    return 0;
                }
                PrestamoEntity entidad = PrestamoMapper.toEntity(nuevo);
                em.persist(entidad);
                em.flush();
                tx.commit();
                return entidad.getIdPrestamo();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                registrar("registrarPrestamo", ex);
                return 0;
            }
        }
    }

    /**
     * Marca un préstamo como devuelto. No se acepta si ya estaba devuelto o si la fecha es anterior a
     * la del préstamo.
     *
     * @param id id del préstamo
     * @param fecha fecha de devolución, yyyy-MM-dd
     * @return 1 si se registró la devolución, 0 si no
     */
    public int devolver(int id, String fecha) {
        if (fecha == null) return 0;
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                PrestamoEntity p = em.find(PrestamoEntity.class, id);
                if (p == null || p.getFechaDevolucion() != null || fecha.compareTo(p.getFechaPrestamo()) < 0) {
                    tx.rollback();
                    return 0;
                }
                p.setFechaDevolucion(fecha);
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                registrar("devolver", ex);
                return 0;
            }
        }
    }

    
    @Override

    /**
     * Cierra la factoría solo si la creó este DAO. Si es compartida se deja abierta.
     */
    public void close() {if(ownFactory && managerFactory.isOpen())managerFactory.close();}
}
