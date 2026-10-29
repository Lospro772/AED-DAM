package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Mascota;

import java.sql.*;
import java.util.*;

/** Lee, guarda, cambia y borra mascotas. Cada método hace todo el trabajo él solo. */
public final class MascotaDao implements IMascotaDao {
    private final String url;

    public MascotaDao(String url) {
        this.url = url;
    }

    /** Todas las mascotas ordenadas por id. Si no hay ninguna, lista vacía. */
    @Override
    public List<Mascota> listar() {
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas ORDER BY id";
        List<Mascota> mascotas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mascotas.add(new Mascota(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("especie"),
                        rs.getString("raza"),
                        rs.getString("fecha_nacimiento"),
                        rs.getInt("id_cliente")));
            }
            return mascotas;
        } catch (SQLException e) {
            throw new DaoException("Error al listar mascotas", e);
        }
    }

    /** La mascota con ese id, o Optional vacío si no existe. */
    @Override
    public Optional<Mascota> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Mascota(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("especie"),
                            rs.getString("raza"),
                            rs.getString("fecha_nacimiento"),
                            rs.getInt("id_cliente")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar la mascota " + id, e);
        }
    }

    /** Guarda una mascota nueva y devuelve el id que le pone la base de datos. */
    @Override
    public int insertar(Mascota mascota) {
        Objects.requireNonNull(mascota, "La mascota no puede ser null");
        String sql = "INSERT INTO mascotas (nombre, especie, raza, fecha_nacimiento, id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mascota.getNombre());
            ps.setString(2, mascota.getEspecie());
            ps.setString(3, mascota.getRaza()); // la raza puede ser null
            ps.setString(4, mascota.getFechaNacimiento());
            ps.setInt(5, mascota.getIdCliente());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // la columna 1 es el id nuevo
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar mascota", e);
        }
    }

    /** Cambia los datos de una mascota que ya existe. Devuelve cuántas filas cambiaron (1 o 0). */
    @Override
    public int actualizar(Mascota mascota) {
        Objects.requireNonNull(mascota, "La mascota no puede ser null");
        if (mascota.getId() == null) throw new IllegalArgumentException("Para actualizar hace falta el id");
        String sql = "UPDATE mascotas SET nombre = ?, especie = ?, raza = ?, fecha_nacimiento = ?, id_cliente = ? "
                + "WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, mascota.getNombre());
            ps.setString(2, mascota.getEspecie());
            ps.setString(3, mascota.getRaza());
            ps.setString(4, mascota.getFechaNacimiento());
            ps.setInt(5, mascota.getIdCliente());
            ps.setInt(6, mascota.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar la mascota " + mascota.getId(), e);
        }
    }

    /** Borra la mascota con ese id. Devuelve cuántas filas se borraron (1 o 0). */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El id no puede ser null");
        String sql = "DELETE FROM mascotas WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar la mascota " + id, e);
        }
    }

    /** Mascotas de un cliente (su dueño). */
    @Override
    public List<Mascota> buscarPorCliente(Integer idCliente) {
        if (idCliente == null) return List.of();
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas "
                + "WHERE id_cliente = ? ORDER BY id";
        List<Mascota> mascotas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mascotas.add(new Mascota(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("especie"),
                            rs.getString("raza"),
                            rs.getString("fecha_nacimiento"),
                            rs.getInt("id_cliente")));
                }
            }
            return mascotas;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar mascotas del cliente " + idCliente, e);
        }
    }

    /** Mascotas de una especie exacta, por ejemplo "Perro". */
    @Override
    public List<Mascota> listarPorEspecie(String especie) {
        if (especie == null || especie.isBlank()) return List.of();
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas "
                + "WHERE especie = ? ORDER BY id";
        List<Mascota> mascotas = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, especie); // el ? evita la inyección SQL
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mascotas.add(new Mascota(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("especie"),
                            rs.getString("raza"),
                            rs.getString("fecha_nacimiento"),
                            rs.getInt("id_cliente")));
                }
            }
            return mascotas;
        } catch (SQLException e) {
            throw new DaoException("Error al listar mascotas por especie", e);
        }
    }
}
