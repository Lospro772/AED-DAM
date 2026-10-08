package ies.puerto;

public record Producto() {
    static int id;
    static String nombre;
    static double precio;

    public Producto {
        if (id <= 0) throw new IllegalArgumentException("id debe ser positivo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre obligatorio");
        if (precio < 0) throw new IllegalArgumentException("precio no puede ser negativo");
    }

}
