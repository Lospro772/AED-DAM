package com.docencia.sqlite;

import java.util.List;

/** Servicio de base de datos SQLite para Cliente. */
public interface ClienteDbService {
    Boolean create(Cliente cliente);
    Cliente findByDni(String dni);
    List<Cliente> findAll();
    Boolean update(Cliente cliente);
    Boolean deleteByDni(String dni);
    List<Cliente> findByCiudad(String ciudad);
}
