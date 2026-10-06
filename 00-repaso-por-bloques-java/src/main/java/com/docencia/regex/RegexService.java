package com.docencia.regex;
/** Servicio para practicar expresiones regulares. */
public interface RegexService {
    Boolean validarDni(String dni);
    Boolean validarEmail(String email);
    Boolean validarTelefono(String telefono);
    Boolean validarNombre(String nombre);
    Boolean validarCodigoPostal(String codigoPostal);
    Boolean validarMatricula(String matricula);
}
