package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Cliente;
import es.ejemplo.taller.orm.config.OrmFactory;
import es.ejemplo.taller.orm.entity.ClienteEntity;
import es.ejemplo.taller.orm.mapper.ClienteMapper;
import jakarta.persistence.EntityManagerFactory;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class ClienteDao {
    private final String url;
    private final EntityManagerFactory managerFactory;

    public ClienteDao(String url) {
        this.url = url;
        managerFactory = OrmFactory.crear(url);
    }

    public List<Cliente> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select c from ClienteEntity c order by c.nombre",
                            ClienteEntity.class)
                    .getResultStream()
                    .map(ClienteMapper::toDomain)
                    .toList();
        }
    }

    public Optional<Cliente> buscarPorId(Integer id) {
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(ClienteEntity.class, id))
                    .map(ClienteMapper::toDomain);
        }

    }

    public Optional<Cliente> buscarPorEmail(String email) {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "select v from VehiculoEntity v where v.idCliente = :id",
                            ClienteEntity.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .map(ClienteMapper::toDomain);
        }

    }

    public int insertar(Cliente cliente) {
        if (cliente == null) {
            return 0;
        }
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                ClienteEntity entidad = ClienteMapper.toEntity(cliente);
                em.persist(entidad);
                em.flush(); // Necesario aquí para leer inmediatamente el ID generado
                cliente.setIdCliente(entidad.getIdCliente());
                tx.commit();
                return cliente.getIdCliente();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }

    public int actualizar(Cliente cliente) {
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                ClienteEntity c = em.find(ClienteEntity.class, cliente.getIdCliente());
                if (c == null) {
                    tx.commit();
                    return 0;
                }
                ClienteEntity clienteActualizado = ClienteMapper.toEntity(cliente);
                c.setNombre(clienteActualizado.getNombre());
                c.setEmail(clienteActualizado.getEmail());// dirty checking
                c.setTelefono(clienteActualizado.getTelefono());
                tx.commit();           // Hibernate detecta el cambio
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
                    ClienteEntity c = em.find(ClienteEntity.class, id);
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
}
