package es.ejemplo.taller.orm.mapper;

import es.ejemplo.taller.modelo.Cliente;
import es.ejemplo.taller.orm.entity.ClienteEntity;

public final class ClienteMapper {
    private ClienteMapper() {}

    public static ClienteEntity toEntity(Cliente cliente) {
        return new ClienteEntity(cliente.getIdCliente(), cliente.getNombre(),
                cliente.getEmail(), cliente.getTelefono());
    }

    public static Cliente toDomain(ClienteEntity entidad) {
        return new Cliente(entidad.getIdCliente(), entidad.getNombre(),
                entidad.getEmail(), entidad.getTelefono());
    }
}