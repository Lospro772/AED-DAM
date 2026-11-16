package es.ejemplo.cinefest.dao;

import static org.junit.jupiter.api.Assertions.*;

import es.ejemplo.cinefest.modelo.Sede;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Cada test trabaja sobre una copia de la base inicial para no alterar los datos de referencia. */
class SedeDaoTest {

    @TempDir Path tmp;
    private SedeDao dao;

    @BeforeEach
    void preparar() throws Exception {
        Path copia = tmp.resolve("cinefest.sqlite3");
        Files.copy(Path.of("datos", "cinefest.sqlite3"), copia, StandardCopyOption.REPLACE_EXISTING);
        dao = new SedeDao("jdbc:sqlite:" + copia.toAbsolutePath());
    }

    @AfterEach
    void cerrar() {
        dao.close();
    }

    private static Sede sede(String nombre, String municipio) {
        Sede s = new Sede();
        s.setNombre(nombre);
        s.setMunicipio(municipio);
        return s;
    }

    @Test
    void listarDevuelveLasTresSedesOrdenadas() {
        List<String> nombres = dao.listar().stream().map(Sede::getNombre).toList();
        assertEquals(List.of("Auditorio Atlántico", "Centro Cultural Sur", "Cine del Puerto"), nombres);
    }

    @Test
    void buscarPorId() {
        assertTrue(dao.buscarPorId(1).isPresent());
        assertTrue(dao.buscarPorId(999).isEmpty());
    }

    @Test
    void insertarActualizarEliminar() {
        int id = dao.insertar(sede("Teatro Norte", "Arucas"));
        assertTrue(id > 0);

        Sede guardada = dao.buscarPorId(id).orElseThrow();
        guardada.setMunicipio("Gáldar");
        assertEquals(1, dao.actualizar(guardada));
        assertEquals("Gáldar", dao.buscarPorId(id).orElseThrow().getMunicipio());

        assertEquals(1, dao.eliminar(id));
        assertTrue(dao.buscarPorId(id).isEmpty());
        assertEquals(0, dao.eliminar(id));
    }

    @Test
    void rechazaDatosNoValidos() {
        assertEquals(0, dao.insertar(null));
        assertEquals(0, dao.insertar(sede(" ", "Telde")));
        assertEquals(0, dao.insertar(sede("Cine del Puerto", "Telde")), "nombre duplicado");

        Sede inexistente = sede("Otra", "Telde");
        inexistente.setIdSede(999);
        assertEquals(0, dao.actualizar(inexistente));
    }

    @Test
    void noEliminaSedeConSalas() {
        assertEquals(0, dao.eliminar(1));
        assertTrue(dao.buscarPorId(1).isPresent());
    }

    @Test
    void salasPorSede() {
        List<Object[]> filas = dao.salasPorSede(1);
        assertEquals(2, filas.size());
        assertArrayEquals(new Object[] {"Auditorio Atlántico", "Sala Azul", 5}, filas.get(0));
        assertArrayEquals(new Object[] {"Auditorio Atlántico", "Sala Roja", 8}, filas.get(1));
        assertTrue(dao.salasPorSede(999).isEmpty());
    }

    @Test
    void aforoTotalPorSedeIncluyeSedesSinSalas() {
        dao.insertar(sede("Zona Vacía", "Agüimes"));
        List<Object[]> filas = dao.aforoTotalPorSede();
        assertEquals(4, filas.size());
        assertEquals("Auditorio Atlántico", filas.get(0)[0]);
        assertEquals(2L, ((Number) filas.get(0)[1]).longValue());
        assertEquals(13L, ((Number) filas.get(0)[2]).longValue());
        assertEquals("Zona Vacía", filas.get(3)[0]);
        assertEquals(0L, ((Number) filas.get(3)[1]).longValue());
        assertEquals(0L, ((Number) filas.get(3)[2]).longValue());
    }
}
