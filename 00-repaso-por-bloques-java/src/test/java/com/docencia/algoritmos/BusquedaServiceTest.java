package com.docencia.algoritmos;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BusquedaServiceTest {
    private final BusquedaService service = new BusquedaServiceImpl();

    @Test @Order(1) void buscarIndiceElementoExistenteDevuelveIndice() { assertEquals(1, service.buscarIndiceElemento(List.of(4, 5, 6), 5)); }
    @Test @Order(2) void buscarIndiceElementoInexistenteDevuelveMenosUno() { assertEquals(-1, service.buscarIndiceElemento(List.of(4), 5)); }
    @Test @Order(3) void buscarIndiceElementoListaNullLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.buscarIndiceElemento(null, 5)); }
    @Test @Order(4) void existePalabraIgnorandoMayusculasDevuelveTrue() { assertTrue(service.existePalabra(List.of("Casa"), "casa")); }
    @Test @Order(5) void encontrarMaximoDatosValidosDevuelveMaximo() { assertEquals(9, service.encontrarMaximo(List.of(1, 9, 2))); }
    @Test @Order(6) void encontrarMaximoListaVaciaLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.encontrarMaximo(List.of())); }
    @Test @Order(7) void encontrarMinimoDatosValidosDevuelveMinimo() { assertEquals(1, service.encontrarMinimo(List.of(1, 9, 2))); }
    @Test @Order(8) void contarAparicionesValorExistenteDevuelveCantidad() { assertEquals(2, service.contarApariciones(List.of(1, 2, 2), 2)); }
}
