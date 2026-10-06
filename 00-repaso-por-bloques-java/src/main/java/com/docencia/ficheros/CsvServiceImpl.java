package com.docencia.ficheros;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvServiceImpl implements CsvService {

    @Override
    public List<String> leerLineasCsv(Path ruta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leerLineasCsv'");
    }

    @Override
    public List<String[]> leerRegistrosCsv(Path ruta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leerRegistrosCsv'");
    }

    @Override
    public void escribirLineasCsv(Path ruta, List<String> lineas) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'escribirLineasCsv'");
    }

    @Override
    public Integer contarRegistrosCsv(Path ruta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarRegistrosCsv'");
    }

    @Override
    public List<String[]> filtrarRegistrosPorValor(Path ruta, Integer columna, String valor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'filtrarRegistrosPorValor'");
    }

    @Override
    public Map<String, Integer> contarFrecuenciaColumna(Path ruta, Integer columna) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarFrecuenciaColumna'");
    }
    
}
