package com.docencia.ficheros;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CsvServiceTest {
    private final CsvService service = new CsvServiceImpl();
    private Path ruta;

    @BeforeEach
    void setUp() throws Exception {
        ruta = Files.createTempFile("datos", ".csv");
        Files.write(ruta, List.of("Ana,20", "Luis,30", "Ana,40"));
    }

    @Test @Order(1) void leerLineasCsvFicheroExistenteDevuelveLineas() { assertEquals(3, service.leerLineasCsv(ruta).size()); }
    @Test @Order(2) void leerLineasCsvFicheroInexistenteLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.leerLineasCsv(Path.of("no.csv"))); }
    @Test @Order(3) void leerRegistrosCsvFicheroValidoDevuelveRegistros() { assertEquals("Ana", service.leerRegistrosCsv(ruta).get(0)[0]); }
    @Test @Order(4) void escribirLineasCsvDatosValidosCreaFichero() throws Exception {
        Path salida = Files.createTempFile("salida", ".csv");
        service.escribirLineasCsv(salida, List.of("x,y"));
        assertEquals(1, Files.readAllLines(salida).size());
    }
    @Test @Order(5) void contarRegistrosCsvFicheroValidoDevuelveCantidad() { assertEquals(3, service.contarRegistrosCsv(ruta)); }
    @Test @Order(6) void filtrarRegistrosPorValorCoincidenciasDevuelveRegistros() { assertEquals(2, service.filtrarRegistrosPorValor(ruta, 0, "Ana").size()); }
    @Test @Order(7) void filtrarRegistrosPorValorColumnaInvalidaLanzaExcepcion() { assertThrows(IllegalArgumentException.class, () -> service.filtrarRegistrosPorValor(ruta, 3, "Ana")); }
    @Test @Order(8) void contarFrecuenciaColumnaDatosValidosDevuelveMapaFrecuencias() { assertEquals(2, service.contarFrecuenciaColumna(ruta, 0).get("Ana")); }
}
