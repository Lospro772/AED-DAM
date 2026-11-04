package es.ejemplo.taller.orm.mapper;

import es.ejemplo.taller.modelo.Vehiculo;
import es.ejemplo.taller.orm.entity.VehiculoEntity;

public class VehiculoMapper {

    public static Vehiculo toEntity(Vehiculo vehiculo) {
        return new Vehiculo(vehiculo.getMatricula(), vehiculo.getMarca(), vehiculo.getModelo(), vehiculo.getAnio(), vehiculo.getIdCliente());
    }

    public static Vehiculo toDomain(VehiculoEntity entity) {
        return new Vehiculo(entity.getMatricula(), entity.getMarca(), entity.getModelo(), entity.getAnio(), entity.getIdCliente());
    }
}
