package com.docencia.sqlite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteSqliteRepository implements ClienteRepository {
    private final String url;

    public ClienteSqliteRepository(String databasePath) {
        this.url = "jdbc:sqlite:" + databasePath;
        crearTablaSiNoExiste();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS cliente (dni TEXT PRIMARY KEY, nombre TEXT NOT NULL, email TEXT NOT NULL, ciudad TEXT NOT NULL)";
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo crear la tabla cliente", exception);
        }
    }

    @Override
    public Boolean save(Cliente cliente) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
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
