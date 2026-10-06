package com.docencia.condicionales;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitchServiceTest {
    private final SwitchService service = new SwitchServiceImpl();

    @Test @Order(1) void obtenerNombreDiaNumeroValidoDevuelveDia() { assertEquals("LUNES", service.obtenerNombreDia(1)); }
    @Test @Order(2) void obtenerNombreDiaNumeroInvalidoLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.obtenerNombreDia(9)); }
    @Test @Order(3) void calcularDescuentoPorTipoVipDevuelveDescuento() { assertEquals(20.0, service.calcularDescuentoPorTipo("VIP", 100.0)); }
    @Test @Order(4) void calcularDescuentoPorTipoNormalDevuelveSinDescuento() { assertEquals(0.0, service.calcularDescuentoPorTipo("NORMAL", 100.0)); }
    @Test @Order(5) void obtenerMensajeEstadoActivoDevuelveMensaje() { assertEquals("Elemento activo", service.obtenerMensajeEstado("ACTIVO")); }
    @Test @Order(6) void obtenerDiasDelMesFebreroDevuelveVeintiocho() { assertEquals(28, service.obtenerDiasDelMes(2)); }
    @Test @Order(7) void obtenerCategoriaProductoCodigoValidoDevuelveCategoria() { assertEquals("TECNOLOGIA", service.obtenerCategoriaProducto("T001")); }
}
