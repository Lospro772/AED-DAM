package com.docencia.sqlite;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClienteDbServiceTest {
    private ClienteDbService service;

    @BeforeEach
    void setUp() throws Exception {
        Path db = Files.createTempFile("clientes", ".db");
        service = new ClienteDbServiceImpl(new ClienteSqliteRepository(db.toString()));
    }

    @Test @Order(1) void findAllSinDatosDevuelveListaVacia() { assertTrue(service.findAll().isEmpty()); }
    @Test @Order(2) void findByDniInexistenteDevuelveNull() { assertNull(service.findByDni("00000000A")); }
    @Test @Order(3) void createClienteValidoDevuelveTrue() { assertTrue(service.create(new Cliente("12345678Z", "Ana", "ana@mail.com", "Madrid"))); }
    @Test @Order(4) void createClienteDuplicadoDevuelveFalse() {
        Cliente cliente = new Cliente("12345678Z", "Ana", "ana@mail.com", "Madrid");
        assertTrue(service.create(cliente));
        assertFalse(service.create(cliente));
    }
    @Test @Order(5) void findByDniExistenteDevuelveCliente() {
        service.create(new Cliente("12345678Z", "Ana", "ana@mail.com", "Madrid"));
        assertEquals("Ana", service.findByDni("12345678Z").getNombre());
    }
    @Test @Order(6) void updateClienteExistenteDevuelveTrue() {
        service.create(new Cliente("12345678Z", "Ana", "ana@mail.com", "Madrid"));
        assertTrue(service.update(new Cliente("12345678Z", "Ana", "a@mail.com", "Sevilla")));
    }
    @Test @Order(7) void deleteByDniExistenteDevuelveTrue() {
        service.create(new Cliente("12345678Z", "Ana", "ana@mail.com", "Madrid"));
        assertTrue(service.deleteByDni("12345678Z"));
    }
    @Test @Order(8) void findByCiudadExistenteDevuelveClientes() {
        service.create(new Cliente("12345678Z", "Ana", "ana@mail.com", "Madrid"));
        assertEquals(1, service.findByCiudad("Madrid").size());
    }
}
