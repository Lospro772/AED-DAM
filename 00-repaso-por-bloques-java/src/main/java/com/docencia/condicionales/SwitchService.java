package com.docencia.condicionales;
/** Servicio para practicar switch. */
public interface SwitchService {
    String obtenerNombreDia(Integer numeroDia);
    Double calcularDescuentoPorTipo(String tipoCliente, Double importe);
    String obtenerMensajeEstado(String estado);
    Integer obtenerDiasDelMes(Integer mes);
    String obtenerCategoriaProducto(String codigo);
}
