# APUNTES EXAMEN (versión corta) — DAO con JDBC + SQLite

> La versión larga, con todos los métodos explicados línea a línea, está en `APUNTES-EXAMEN-LARGO.md`.

## 1. Palabras clave

- **DAO**: la clase que lee, guarda, cambia y borra en UNA tabla.
- **`con`** (Connection): la conexión abierta con la base de datos.
- **`ps`** (PreparedStatement): la orden SQL con huecos `?`.
- **`rs`** (ResultSet): las filas que devuelve un SELECT. Se avanza fila a fila con `rs.next()`.
- **`try ( ... )`**: cierra solo lo que abres dentro del paréntesis.
- **Optional**: una caja con un objeto o vacía (en vez de `null`).
- **BigDecimal**: decimales exactos (dinero, horas).

## 2. Antes de escribir nada

1. En `schema.sql`: los nombres EXACTOS de las columnas y qué id es automático (`AUTOINCREMENT`).
2. En los tests: si algo falla, ¿esperan **0 / lista vacía** (Taller) o **una excepción** (Clínica)?
3. ¿Piden `AbstractDao`? Sí → punto 6. No → punto 5.

## 3. La receta: todos los métodos son estos 6 pasos

1. **Comprobar** lo que me dan (si es `null`, salgo).
2. **Escribir el SQL** con `?` donde van los datos.
3. **Abrir** `con` y `ps` dentro de `try ( )`.
4. **Rellenar los `?`**: `ps.setString(1, ...)` (se cuenta desde 1).
5. **Ejecutar**:
   - para leer, `executeQuery()` → da `rs`;
   - para cambiar, `executeUpdate()` → da el nº de filas cambiadas.
6. **Devolver** el resultado. Si algo falla, el `catch` devuelve 0/vacío o lanza la excepción.

## 4. ConexionBD: la única que abre conexiones

```java
public static Connection abrir(String url) throws SQLException {
    // 1. Comprobar el archivo ANTES de conectar
    //    (si no existe, SQLite crea uno vacío sin avisar)
    String ruta = url.substring("jdbc:sqlite:".length());
    if (!Files.isRegularFile(Path.of(ruta))) {
        throw new SQLException("No existe: " + ruta);
    }
    // 2. Conectar
    Connection con = DriverManager.getConnection(url);
    // 3. Activar las claves foráneas (en CADA conexión)
    try (Statement st = con.createStatement()) {
        st.execute("PRAGMA foreign_keys = ON");
    } catch (SQLException e) {
        con.close();
        throw e;
    }
    return con;
}
```

- Si piden comprobar las tablas: `SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN ('clientes', ...)` tiene que dar el número de tablas.
- En el taller, `ConexionBD` además guarda la url y tiene `obtenerConexion()`, que hace `return abrir(url);`. Los DAO del taller la heredan.

## 5. Estilo CLÍNICA: cada método lo hace todo

La clase guarda la url: `private final String url;` y en el constructor `this.url = url;`.

### buscarPorId: el modelo para LEER

```java
public Optional<Cliente> buscarPorId(Integer id) {
    // 1. Comprobar
    if (id == null) {
        return Optional.empty();
    }
    // 2. SQL con un hueco
    String sql = "SELECT id, nombre, email, telefono "
            + "FROM clientes WHERE id = ?";
    // 3. Abrir
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {
        // 4. Rellenar el ?
        ps.setInt(1, id);
        // 5. Ejecutar (rs en otro try: antes hay que rellenar)
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                // 6. La fila se convierte en objeto, dentro de la caja
                return Optional.of(new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("telefono")));
            }
            return Optional.empty();
        }
    } catch (SQLException e) {
        throw new DaoException("Error al buscar cliente", e);
    }
}
```

### insertar: el modelo para GUARDAR

```java
public int insertar(Cliente cliente) {
    // Sin cliente → NullPointerException
    if (cliente == null) {
        throw new NullPointerException("Cliente null");
    }
    // Sin columna id: la pone la base de datos
    String sql = "INSERT INTO clientes (nombre, email, telefono) "
            + "VALUES (?, ?, ?)";
    // RETURN_GENERATED_KEYS = "luego dame el id nuevo"
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql,
                 Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, cliente.getNombre());
        ps.setString(2, cliente.getEmail());
        ps.setString(3, cliente.getTelefono());
        ps.executeUpdate();
        // El id nuevo llega como una fila de una columna
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    } catch (SQLException e) {
        throw new DaoException("Error al insertar cliente", e);
    }
}
```

### Los demás son variaciones de esos dos

- **listar**: como `buscarPorId`, pero:
  - no hay `?`, así que `rs` va en el mismo `try` que `con` y `ps`;
  - `while` en vez de `if`;
  - mete cada objeto en una `List` y la devuelve.
- **filtro** (`listarPorEspecie`): como `buscarPorId` (`WHERE especie = ?`), pero con `while` y `List`.
- **actualizar**:
  - SQL: `UPDATE clientes SET nombre = ?, email = ?, telefono = ? WHERE id = ?`;
  - el id es el **último** `set`;
  - termina con `return ps.executeUpdate();`;
  - si no tiene id → `IllegalArgumentException`.
- **eliminar**: `DELETE FROM clientes WHERE id = ?`, luego `ps.setInt(1, id);` y `return ps.executeUpdate();`.
  - si el id es `null` → `throw new NullPointerException("...")` (lo piden los tests).
- **coste**: `SELECT COALESCE(SUM(cantidad * precio_unitario), 0) AS total FROM ... WHERE id_consulta = ?` y luego `rs.getBigDecimal("total")`.

**Truco (lo hace tu compañero):** para no escribir el `new Cliente(...)` en cada método, sácalo a un método privado y llámalo desde todos:

```java
private Cliente mapCliente(ResultSet rs) throws SQLException {
    return new Cliente(rs.getInt("id"), rs.getString("nombre"),
            rs.getString("email"), rs.getString("telefono"));
}
// En buscarPorId: return Optional.of(mapCliente(rs));
// En listar:      clientes.add(mapCliente(rs));
```

## 6. Estilo TALLER: la clase abstracta (como el profe)

### La idea

En todos los DAO se repite el mismo trozo: **recorrer las filas del `rs` y convertir cada una en un objeto**. Ese trozo se escribe UNA vez en el padre, `AbstractDao`.

Lo único que cambia de una tabla a otra es **qué objeto se crea** (`new Cliente(...)`, `new Vehiculo(...)`). Eso es el **hueco**: el método `abstract` `mapRsToClass(rs)`, que escribe cada hijo.

- **El padre (`AbstractDao`)** tiene:
  - `consultar(ps)`: ejecuta el SELECT y devuelve **todas** las filas en una `List`;
  - `consultarUno(ps)`: ejecuta el SELECT y devuelve **una** fila en un `Optional`;
  - `registrar(...)`: apunta el error con el Logger.
- **Cada hijo (`ClienteDao`...)** escribe:
  - el hueco `mapRsToClass(rs)`;
  - TODOS sus métodos (`listar`, `buscarPorId`, `insertar`, `actualizar`, `eliminar` y filtros), con la misma receta de 6 pasos.
- **`<T>`** es el hueco del tipo: `ClienteDao extends AbstractDao<Cliente>` → T es `Cliente`.

### Clínica vs Taller

| | Clínica | Taller |
|---|---|---|
| Abrir | `ConexionBD.abrir(url)` | `obtenerConexion()` (heredado) |
| Leer filas | `while`/`if` con `rs` en cada método | `return consultar(ps);` o `return consultarUno(ps);` |
| `new Cliente(...)` | en cada método | una vez, en `mapRsToClass` |
| `catch` | `catch (SQLException e)` | `catch (SQLException \| RuntimeException e)` |
| Si falla | `throw new DaoException(...)` | `registrar(...)` y `0` / `List.of()` / `Optional.empty()` |

`SQLException | RuntimeException` significa "recoge cualquiera de los dos". Así, si llega un `null` y salta un NullPointerException, también devuelve 0.

### El padre

```java
public abstract class AbstractDao<T> extends ConexionBD {
    private static final Logger LOG =
            Logger.getLogger(AbstractDao.class.getName());

    protected AbstractDao(String url) {
        super(url);   // ConexionBD guarda la url
    }

    // EL HUECO: cada hijo convierte una fila en su objeto
    protected abstract Optional<T> mapRsToClass(ResultSet rs);

    // Muchas filas → lista
    protected List<T> consultar(PreparedStatement ps) {
        if (ps == null) return List.of();
        try (ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                // El hijo crea el objeto de esta fila
                Optional<T> entidad = mapRsToClass(rs);
                if (entidad.isEmpty()) return List.of();
                resultado.add(entidad.get());
            }
            return resultado;
        } catch (SQLException | RuntimeException e) {
            registrar("consultar", e);
            return List.of();
        }
    }

    // Una fila → caja
    protected Optional<T> consultarUno(PreparedStatement ps) {
        if (ps == null) return Optional.empty();
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRsToClass(rs);
            }
            return Optional.empty();
        } catch (SQLException | RuntimeException e) {
            registrar("consultarUno", e);
            return Optional.empty();
        }
    }

    // Apunta el error
    protected void registrar(String operacion, Exception e) {
        LOG.log(Level.WARNING, "Fallo JDBC: " + operacion, e);
    }
}
```

En el proyecto del profe el padre tiene además dos métodos con el mismo estilo:
- `ejecutar(ps)`: `return ps.executeUpdate() > 0;`. Da `true` si cambió alguna fila y se usa cuando `insertar`/`eliminar` devuelven `boolean`.
- `consultarEntero(ps)`: `return rs.next() ? rs.getInt(1) : 0;`. Sirve para `COUNT(*)`.

En el taller los tests piden `int`, por eso ahí se hace `return ps.executeUpdate();`.

### Un hijo

```java
public class ClienteDao extends AbstractDao<Cliente> {
    public ClienteDao(String url) {
        super(url);
    }

    // EL HUECO: una fila → un Cliente
    @Override
    protected Optional<Cliente> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass cliente", e);
            return Optional.empty();
        }
    }

    // LEER: abro, relleno y el padre recorre las filas
    public Optional<Cliente> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM clientes WHERE id_cliente = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("buscarPorId cliente", e);
            return Optional.empty();
        }
    }

    // GUARDAR: igual que en la clínica, pero el catch devuelve 0
    public int insertar(Cliente cliente) {
        if (!esValido(cliente)) return 0;
        String sql = "INSERT INTO clientes (nombre, email, telefono) "
                + "VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException | RuntimeException e) {
            registrar("insertar cliente", e);
            return 0;
        }
    }

    // Nombre y email obligatorios
    private boolean esValido(Cliente cliente) {
        if (cliente == null) return false;
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) return false;
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) return false;
        return true;
    }
}
```

Los demás métodos del hijo:
- **listar**: `SELECT * FROM clientes ORDER BY id_cliente`, sin `?`, `return consultar(ps);` y en el catch `return List.of();`.
- **filtro**: `WHERE especialidad = ?`, `ps.setString(1, especialidad);` y `return consultar(ps);`. Si el valor es `null` o está en blanco → `List.of()`.
- **actualizar** y **eliminar**: igual que en la clínica (`return ps.executeUpdate();`), pero el catch hace `registrar(...)` y `return 0;`.
- **VehiculoDao**: la matrícula es `String`, así que usa `setString`. Como no la inventa la base de datos, `insertar` va sin `RETURN_GENERATED_KEYS` y termina con `return ps.executeUpdate();` (da 1).

### Qué pasa al llamar a `clientes.buscarPorId(5)`

1. `buscarPorId` está en `ClienteDao`: abre con `obtenerConexion()` (viene de `ConexionBD`) y rellena el `?` con 5.
2. Llama a `consultarUno(ps)`, que es del padre: ejecuta el SELECT y, si hay fila, llama a `mapRsToClass(rs)`.
3. El padre no sabe crear un Cliente, así que Java usa el `mapRsToClass` del hijo (`ClienteDao`), que hace el `new Cliente(...)`.
4. Vuelve la caja con el Cliente.

## 7. SQL: los 4 patrones

```sql
-- Unir dos tablas (WHERE o INNER JOIN dan lo mismo)
SELECT vehiculos.matricula, clientes.nombre
FROM vehiculos
INNER JOIN clientes
    ON vehiculos.id_cliente = clientes.id_cliente;

-- Contar incluyendo los que tienen 0: LEFT JOIN + COUNT(columna)
SELECT clientes.nombre, COUNT(vehiculos.matricula)
FROM clientes
LEFT JOIN vehiculos
    ON vehiculos.id_cliente = clientes.id_cliente
GROUP BY clientes.id_cliente;

-- Sumar con 0 si no hay filas: COALESCE(SUM(...), 0)
SELECT reparaciones.id_reparacion,
       COALESCE(SUM(piezas_reparacion.cantidad
           * piezas_reparacion.precio_unitario), 0)
FROM reparaciones
LEFT JOIN piezas_reparacion
    ON piezas_reparacion.id_reparacion = reparaciones.id_reparacion
GROUP BY reparaciones.id_reparacion;

-- Los que NO tienen hijos: LEFT JOIN + IS NULL
SELECT reparaciones.id_reparacion
FROM reparaciones
LEFT JOIN piezas_reparacion
    ON piezas_reparacion.id_reparacion = reparaciones.id_reparacion
WHERE piezas_reparacion.id_pieza IS NULL;
```

- **INNER JOIN**: solo las filas que tienen pareja.
- **LEFT JOIN**: todas las de la izquierda; si no tienen pareja, lo de la derecha sale NULL.
- **COALESCE(a, b)**: si `a` es NULL, usa `b`.
- `PRAGMA foreign_keys = ON` hay que ponerlo en **cada conexión** (por eso va en `ConexionBD`).
- En `consultas.sql` se ponen valores de ejemplo (`WHERE marca = 'Seat'`), no `?`. Los `?` son solo para Java.

## 8. Que no se me olvide

- Los `?` empiezan en 1. Hay que rellenarlos ANTES de ejecutar.
- `rs.next()` siempre antes de leer la fila.
- Nunca devolver `null`: `List.of()` u `Optional.empty()`.
- `setInt` con un Integer que es `null` → NullPointerException.
- BigDecimal: crearlo con `new BigDecimal("10.25")` y compararlo con `compareTo(...) == 0`.
- Texto obligatorio: `texto != null && !texto.isBlank()`.
- Lo que ya comprueba un `CHECK` de la base de datos (estado válido, cantidad > 0, tarifa >= 0) no hace falta comprobarlo en Java: da SQLException y el `catch` devuelve 0.
- Nada de `+ valor +` dentro del SQL: siempre `?` (evita la inyección SQL).
- Repasa el SQL letra a letra: un `UPDATE PDATE` o un `id_clientes` hace fallar el método entero.

## 9. Teoría en una línea

- **PreparedStatement**: evita la inyección SQL.
- **try-with-resources**: cierra `con`, `ps` y `rs` aunque haya error.
- **Optional**: obliga a tratar el "no encontrado" sin `null`.
- **Clase abstracta**: lo común (recorrer las filas) se escribe una vez y cada hijo rellena el hueco `mapRsToClass`. No se puede hacer `new`.
- **Interfaz vs abstracta**: la interfaz solo dice qué métodos hay; la abstracta trae código hecho.
- **`<T>`**: el padre no sabe si maneja Clientes o Vehículos; el hijo se lo dice con `AbstractDao<Cliente>`.
- **Devolver 0 si falla**: no distingue "error" de "no hay datos"; por eso el error se apunta en el log.
