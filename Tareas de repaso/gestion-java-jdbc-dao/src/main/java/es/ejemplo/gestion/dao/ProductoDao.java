package es.ejemplo.gestion.dao;

import es.ejemplo.gestion.config.ConexionBD;
import es.ejemplo.gestion.exception.AplicationException;
import es.ejemplo.gestion.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ProductoDao extends AbstractDao {
    //private  ConexionBD conexionBD;

    public ProductoDao(String url) {
        super(url);
    }

    @Override
    Producto mapToClass(ResultSet rs) {
        try {
            Producto producto = new Producto(rs.getInt(1), rs.getNString(2), rs.getDouble(3));
        } catch (Exception e) {
            throw new AplicationException(e.getMessage(), e.getCause());
        }
        return null;
    }


    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String query = "SELECT id,nombre,precio FROM Productos";
        try (Connection connection = this.obtenerConexion()) {

            PreparedStatement ps = connection.prepareStatement(query);

            ResultSet rs = ps.executeQuery();

            return consultar(ps);
        } catch (Exception e) {
            throw new AplicationException(e.getMessage(), e);
        }

        //return this.consultar(query);
    }

    public Optional<Producto> buscarPorId(int id) {
        String query = "SELECT id,nombre,precio FROM Productos WHERE id = ?";
        List<Producto> productos = null;
        try (Connection connection = this.obtenerConexion()) {

            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);

            productos = consultar(ps);
        } catch (Exception e) {
            throw new AplicationException(e.getMessage(), e);
        }
        if (productos != null && productos.size() > 0) {
            return productos.stream().findFirst();
        }
        return Optional.empty();
    }

    public boolean insertar(Producto producto) {
        return false;
    }

    public boolean actualizar(Producto producto) {
        return false;
    }

    public boolean eliminar(int id) {
        return false;
    }

    public List<Producto> buscarPorNombre(String texto) {
       return null;
    }

    public List<Producto> buscarPorPrecio(double minimo, double maximo) {
        return null;
    }

    public int contar() {

        return 0;
    }

    public double precioMedio() {

        return 0;
    }
    
    private Producto mapeoProducto(ResultSet rs) {
        try {
            Producto producto = new Producto(rs.getInt(1), rs.getNString(2), rs.getDouble(3));
        } catch (Exception e) {
            throw new AplicationException(e.getMessage(), e.getCause());
        }
        return null;
    }
}
