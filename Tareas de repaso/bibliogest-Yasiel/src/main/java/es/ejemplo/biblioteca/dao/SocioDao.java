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

public class SocioDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocioDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    public SocioDao(String url) {
        managerFactory = OrmFactory.crear(url);
        ownFactory = true;
    }

    public SocioDao(EntityManagerFactory factory) {
        managerFactory = factory;
        ownFactory = false;
    }

    private void registrar(String metodo, Exception ex) {
        LOG.log(Level.WARNING, metodo + " no se pudo completar: " + ex.getMessage());
    }

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

    public Optional<Socio> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SocioEntity.class, id))
                    .map(SocioMapper::toDomain);
        } catch (Exception ex) {
            registrar("buscarPorId",ex);
            return Optional.empty();
        }
    }

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

    public List<Socio> listarConPrestamosVencidos(String fecha) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }

    public List<Socio> sociosSinPrestamos() {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }

    public List<Object[]> rankingSocios(int limite) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }

    public List<Object[]> historialSocio(int idSocio) {
        // TODO: implementar con EntityManager, JPQL y rollback cuando proceda.
        return List.of();
    }


    @Override
    public void close() {
        if (ownFactory && managerFactory.isOpen()) managerFactory.close();
    }
}
