package com.docencia.colecciones;

import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MapServiceTest {
    private final MapService service = new MapServiceImpl();

    @Test @Order(1)
    void contarFrecuenciaPalabrasDatosValidosDevuelveFrecuencias() {
        assertEquals(2, service.contarFrecuenciaPalabras(List.of("a", "b", "a")).get("a"));
    }

    @Test @Order(2)
    void contarFrecuenciaPalabrasListaVaciaDevuelveMapaVacio() {
        assertTrue(service.contarFrecuenciaPalabras(List.of()).isEmpty());
    }

    @Test @Order(3)
    void obtenerValorPorClaveClaveInexistenteDevuelveCero() {
        assertEquals(0, service.obtenerValorPorClave(Map.of("a", 1), "x"));
    }

    @Test @Order(4)
    void calcularMediaPorCategoriaDatosValidosDevuelveMedias() {
        assertEquals(2.0, service.calcularMediaPorCategoria(Map.of("x", List.of(1, 2, 3))).get("x"));
    }

    @Test @Order(5)
    void obtenerClaveConMayorValorDatosValidosDevuelveClave() {
        assertEquals("b", service.obtenerClaveConMayorValor(Map.of("a", 1, "b", 3)));
    }

    @Test @Order(6)
    void obtenerClaveConMayorValorMapaVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerClaveConMayorValor(Map.of()));
    }

    @Test @Order(7)
    void filtrarPorValorMinimoDatosValidosDevuelveMapaFiltrado() {
        assertEquals(Map.of("b", 5), service.filtrarPorValorMinimo(Map.of("a", 1, "b", 5), 3));
    }
}
