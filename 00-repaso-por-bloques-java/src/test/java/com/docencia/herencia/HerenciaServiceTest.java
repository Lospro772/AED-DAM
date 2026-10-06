package com.docencia.herencia;
import org.junit.jupiter.api.*;
import java.lang.reflect.Modifier;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HerenciaServiceTest {
    private final HerenciaService service = new HerenciaServiceImpl();

    @Test @Order(1) void personaEsClaseAbstracta() { assertTrue(Modifier.isAbstract(Persona.class.getModifiers())); }
    @Test @Order(2) void alumnoHeredaDePersona() { assertEquals(Persona.class, Alumno.class.getSuperclass()); }
    @Test @Order(3) void alumnoEsInstanciaDePersona() { assertInstanceOf(Persona.class, new Alumno("Ana", "Lopez", 20, "E1")); }
    @Test @Order(4) void obtenerNombreCompletoAlumnoValidoDevuelveNombreCompleto() { assertEquals("Ana Lopez", service.obtenerNombreCompleto(new Alumno("Ana", "Lopez", 20, "E1"))); }
    @Test @Order(5) void obtenerNombreCompletoPersonaNullLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.obtenerNombreCompleto(null)); }
    @Test @Order(6) void esAlumnoConAlumnoDevuelveTrue() { assertTrue(service.esAlumno(new Alumno("Ana", "Lopez", 20, "E1"))); }
    @Test @Order(7) void obtenerDescripcionPersonaAlumnoDevuelveDescripcion() { assertEquals("ALUMNO: Ana Lopez (20)", service.obtenerDescripcionPersona(new Alumno("Ana", "Lopez", 20, "E1"))); }
    @Test @Order(8) void tieneEdadMinimaEdadInsuficienteDevuelveFalse() { assertFalse(service.tieneEdadMinima(new Alumno("Ana", "Lopez", 17, "E1"), 18)); }
    @Test @Order(9) void obtenerTipoPersonaAlumnoDevuelveAlumno() { assertEquals("ALUMNO", service.obtenerTipoPersona(new Alumno("Ana", "Lopez", 20, "E1"))); }
}
