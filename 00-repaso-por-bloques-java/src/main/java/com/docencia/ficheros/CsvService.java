package com.docencia.ficheros;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Servicio para trabajar con ficheros CSV. */
public interface CsvService {
    List<String> leerLineasCsv(Path ruta);
    List<String[]> leerRegistrosCsv(Path ruta);
    void escribirLineasCsv(Path ruta, List<String> lineas);
    Integer contarRegistrosCsv(Path ruta);
    List<String[]> filtrarRegistrosPorValor(Path ruta, Integer columna, String valor);
    Map<String, Integer> contarFrecuenciaColumna(Path ruta, Integer columna);
}
