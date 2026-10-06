package com.docencia.cadenas;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StringServiceTest {
    private final StringService service = new StringServiceImpl();

    @Test @Order(1) void normalizarTextoConEspaciosDevuelveTextoLimpio() { assertEquals("hola mundo", service.normalizarTexto("  Hola   Mundo ")); }
    @Test @Order(2) void normalizarTextoTextoNullLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.normalizarTexto(null)); }
    @Test @Order(3) void esPalindromoTextoPalindromoDevuelveTrue() { assertTrue(service.esPalindromo("ana")); }
    @Test @Order(4) void esPalindromoIgnorandoEspaciosDevuelveTrue() { assertTrue(service.esPalindromo("anita lava la tina")); }
    @Test @Order(5) void contarVocalesTextoValidoDevuelveCantidad() { assertEquals(5, service.contarVocales("murcielago")); }
    @Test @Order(6) void extraerInicialesNombreCompletoDevuelveIniciales() { assertEquals("JPG", service.extraerIniciales("Juan Perez Garcia")); }
    @Test @Order(7) void invertirTextoDatosValidosDevuelveTextoInvertido() { assertEquals("aloH", service.invertirTexto("Hola")); }
    @Test @Order(8) void contieneSoloLetrasConNumerosDevuelveFalse() { assertFalse(service.contieneSoloLetras("Ana1")); }
}
