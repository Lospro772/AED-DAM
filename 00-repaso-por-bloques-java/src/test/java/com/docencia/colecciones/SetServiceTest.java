package com.docencia.colecciones;

import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SetServiceTest {
    private final SetService service = new SetServiceImpl();

    @Test @Order(1)
    void obtenerElementosUnicosConDuplicadosDevuelveSetSinDuplicados() {
        assertEquals(Set.of("a", "b"), service.obtenerElementosUnicos(List.of("a", "b", "a")));
    }

    @Test @Order(2)
    void contieneElementoExistenteDevuelveTrue() {
        assertTrue(service.contieneElemento(Set.of("a"), "a"));
    }

    @Test @Order(3)
    void contieneElementoValorNullLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.contieneElemento(Set.of("a"), null));
    }

    @Test @Order(4)
    void unirConjuntosDatosValidosDevuelveUnion() {
        assertEquals(Set.of("a", "b"), service.unirConjuntos(Set.of("a"), Set.of("b")));
    }

    @Test @Order(5)
    void intersectarConjuntosConCoincidenciasDevuelveInterseccion() {
        assertEquals(Set.of("b"), service.intersectarConjuntos(Set.of("a", "b"), Set.of("b", "c")));
    }

    @Test @Order(6)
    void restarConjuntosDatosValidosDevuelveDiferencia() {
        assertEquals(Set.of("a"), service.restarConjuntos(Set.of("a", "b"), Set.of("b")));
    }
}
