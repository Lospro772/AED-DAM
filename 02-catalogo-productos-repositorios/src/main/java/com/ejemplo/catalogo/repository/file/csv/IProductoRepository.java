package com.ejemplo.catalogo.repository.file.csv;

import com.ejemplo.catalogo.model.Producto;

import java.util.List;
import java.util.Optional;

public interface IProductoRepository extends IRepository<Producto, Long> {
    boolean delete(long id);
}


