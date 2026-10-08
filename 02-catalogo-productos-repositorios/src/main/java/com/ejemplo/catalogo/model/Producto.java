package com.ejemplo.catalogo.model;

public record Producto(Long id, String nombre, double precio) implements Identificable<Long> {}