package com.docencia.sqlite;

import java.util.List;

public class ClienteDbServiceImpl implements ClienteDbService {
    private final ClienteRepository repository;

    public ClienteDbServiceImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Boolean create(Cliente cliente) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public Cliente findByDni(String dni) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByDni'");
    }

    @Override
    public List<Cliente> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public Boolean update(Cliente cliente) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public Boolean deleteByDni(String dni) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteByDni'");
    }

    @Override
    public List<Cliente> findByCiudad(String ciudad) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByCiudad'");
    }

    
}
