package com.docencia.colecciones;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ListServiceTest {
    private final ListService service = new ListServiceImpl();

    @Test @Order(1)
    void filtrarPalabrasPorLongitudDatosValidosDevuelveListaFiltrada() {
        assertEquals(List.of("casa", "perro"), service.filtrarPalabrasPorLongitud(List.of("sol", "casa", "perro"), 4));
    }

    @Test @Order(2)
    void filtrarPalabrasPorLongitudListaVaciaDevuelveListaVacia() {
        assertTrue(service.filtrarPalabrasPorLongitud(List.of(), 3).isEmpty());
    }

    @Test @Order(3)
    void filtrarPalabrasPorLongitudLongitudInvalidaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.filtrarPalabrasPorLongitud(List.of("a"), -1));
    }

    @Test @Order(4)
    void ordenarNumerosAscendenteDatosValidosDevuelveListaOrdenada() {
        assertEquals(List.of(1, 2, 3), service.ordenarNumerosAscendente(List.of(3, 1, 2)));
    }

    @Test @Order(5)
    void sumarElementosListaDatosValidosDevuelveSuma() {
        assertEquals(6, service.sumarElementosLista(List.of(1, 2, 3)));
    }

    @Test @Order(6)
    void calcularMediaListaDatosValidosDevuelveMedia() {
        assertEquals(2.0, service.calcularMediaLista(List.of(1, 2, 3)));
    }

    @Test @Order(7)
    void calcularMediaListaListaVaciaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.calcularMediaLista(List.of()));
    }

    @Test @Order(8)
    void eliminarNumerosDuplicadosConDuplicadosDevuelveListaSinDuplicados() {
        assertEquals(List.of(1, 2, 3), service.eliminarNumerosDuplicados(List.of(1, 2, 2, 3)));
    }
}
