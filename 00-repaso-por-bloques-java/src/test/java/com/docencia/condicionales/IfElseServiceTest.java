package com.docencia.condicionales;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IfElseServiceTest {
    private final IfElseService service = new IfElseServiceImpl();

    @Test @Order(1) void clasificarEdadMenorDevuelveMenor() { assertEquals("MENOR", service.clasificarEdad(17)); }
    @Test @Order(2) void clasificarEdadAdultoDevuelveAdulto() { assertEquals("ADULTO", service.clasificarEdad(18)); }
    @Test @Order(3) void clasificarEdadEdadInvalidaLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.clasificarEdad(-1)); }
    @Test @Order(4) void evaluarNotaAprobadoDevuelveAprobado() { assertEquals("APROBADO", service.evaluarNota(5)); }
    @Test @Order(5) void evaluarNotaSuspensoDevuelveSuspenso() { assertEquals("SUSPENSO", service.evaluarNota(4)); }
    @Test @Order(6) void puedeAccederActivoYMayorDevuelveTrue() { assertTrue(service.puedeAcceder(true, 18)); }
    @Test @Order(7) void clasificarTemperaturaCalorDevuelveCalor() { assertEquals("CALOR", service.clasificarTemperatura(30.0)); }
    @Test @Order(8) void calcularResultadoComparacionIgualesDevuelveIguales() { assertEquals("IGUALES", service.calcularResultadoComparacion(2, 2)); }
}
