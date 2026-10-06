package com.docencia.fechas;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalDateServiceTest {
    private final LocalDateService service = new LocalDateServiceImpl();

    @Test @Order(1) void calcularEdadFechaValidaDevuelveEdad() { assertTrue(service.calcularEdad(LocalDate.now().minusYears(20)) >= 19); }
    @Test @Order(2) void calcularEdadFechaFuturaLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.calcularEdad(LocalDate.now().plusDays(1))); }
    @Test @Order(3) void esFechaFuturaFechaFuturaDevuelveTrue() { assertTrue(service.esFechaFutura(LocalDate.now().plusDays(1))); }
    @Test @Order(4) void esFechaFuturaFechaPasadaDevuelveFalse() { assertFalse(service.esFechaFutura(LocalDate.now().minusDays(1))); }
    @Test @Order(5) void calcularDiasEntreFechasDatosValidosDevuelveDias() { assertEquals(5, service.calcularDiasEntreFechas(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 6))); }
    @Test @Order(6) void calcularDiasEntreFechasFinAnteriorLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.calcularDiasEntreFechas(LocalDate.now(), LocalDate.now().minusDays(1))); }
    @Test @Order(7) void sumarDiasDiasNegativosRestaDias() { assertEquals(LocalDate.of(2024, 1, 1), service.sumarDias(LocalDate.of(2024, 1, 2), -1)); }
    @Test @Order(8) void esMayorDeEdadMenorDevuelveFalse() { assertFalse(service.esMayorDeEdad(LocalDate.now().minusYears(10))); }
}
