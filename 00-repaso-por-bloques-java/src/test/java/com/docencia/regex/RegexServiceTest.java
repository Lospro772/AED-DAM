package com.docencia.regex;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegexServiceTest {
    private final RegexService service = new RegexServiceImpl();

    @Test @Order(1) void validarDniFormatoValidoDevuelveTrue() { assertTrue(service.validarDni("12345678Z")); }
    @Test @Order(2) void validarDniFormatoInvalidoDevuelveFalse() { assertFalse(service.validarDni("1234A678Z")); }
    @Test @Order(3) void validarDniTextoNullDevuelveFalse() { assertFalse(service.validarDni(null)); }
    @Test @Order(4) void validarEmailFormatoValidoDevuelveTrue() { assertTrue(service.validarEmail("usuario@mail.com")); }
    @Test @Order(5) void validarEmailFormatoInvalidoDevuelveFalse() { assertFalse(service.validarEmail("usuario@mail")); }
    @Test @Order(6) void validarTelefonoFormatoValidoDevuelveTrue() { assertTrue(service.validarTelefono("612345678")); }
    @Test @Order(7) void validarTelefonoFormatoInvalidoDevuelveFalse() { assertFalse(service.validarTelefono("512345678")); }
    @Test @Order(8) void validarNombreFormatoValidoDevuelveTrue() { assertTrue(service.validarNombre("María López")); }
    @Test @Order(9) void validarNombreMuyCortoDevuelveFalse() { assertFalse(service.validarNombre("A")); }
    @Test @Order(10) void validarCodigoPostalFormatoValidoDevuelveTrue() { assertTrue(service.validarCodigoPostal("38001")); }
    @Test @Order(11) void validarMatriculaFormatoValidoDevuelveTrue() { assertTrue(service.validarMatricula("1234ABC")); }
}
