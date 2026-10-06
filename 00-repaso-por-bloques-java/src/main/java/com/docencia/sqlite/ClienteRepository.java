package com.docencia.sqlite;

import java.util.List;

public interface ClienteRepository {
    Boolean save(Cliente cliente);
    Cliente findByDni(String dni);
    List<Cliente> findAll();
    Boolean update(Cliente cliente);
    Boolean deleteByDni(String dni);
    List<Cliente> findByCiudad(String ciudad);
}
