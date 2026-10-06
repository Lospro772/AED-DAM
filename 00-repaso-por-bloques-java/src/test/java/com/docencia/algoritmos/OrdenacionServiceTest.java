package com.docencia.algoritmos;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrdenacionServiceTest {
    private final OrdenacionService service = new OrdenacionServiceImpl();

    @Test @Order(1) void ordenarBurbujaAscendenteDatosValidosDevuelveListaOrdenada() { assertEquals(List.of(1, 2, 3), service.ordenarBurbujaAscendente(List.of(3, 1, 2))); }
    @Test @Order(2) void ordenarBurbujaAscendenteListaNullLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.ordenarBurbujaAscendente(null)); }
    @Test @Order(3) void ordenarBurbujaDescendenteDatosValidosDevuelveListaOrdenada() { assertEquals(List.of(3, 2, 1), service.ordenarBurbujaDescendente(List.of(1, 3, 2))); }
    @Test @Order(4) void ordenarPalabrasAlfabeticamenteDatosValidosDevuelveListaOrdenada() { assertEquals(List.of("ana", "Luis"), service.ordenarPalabrasAlfabeticamente(List.of("Luis", "ana"))); }
    @Test @Order(5) void invertirListaDatosValidosDevuelveListaInvertida() { assertEquals(List.of(3, 2, 1), service.invertirLista(List.of(1, 2, 3))); }
    @Test @Order(6) void estaOrdenadaAscendenteListaOrdenadaDevuelveTrue() { assertTrue(service.estaOrdenadaAscendente(List.of(1, 2, 3))); }
    @Test @Order(7) void estaOrdenadaAscendenteListaDesordenadaDevuelveFalse() { assertFalse(service.estaOrdenadaAscendente(List.of(1, 3, 2))); }
}
