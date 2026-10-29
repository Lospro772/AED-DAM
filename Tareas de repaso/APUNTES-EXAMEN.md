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

Si piden comprobar las tablas: `SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name IN ('clientes', ...)` tiene que dar el número de tablas.

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
    Objects.requireNonNull(cliente, "Cliente null");
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
- **coste**: `SELECT COALESCE(SUM(cantidad * precio_unitario), 0) AS total FROM ... WHERE id_consulta = ?` y luego `rs.getBigDecimal("total")`.

## 6. Estilo TALLER: la clase abstracta

### La idea

Es como una carta de plantilla: "Estimado ____, su ____ está listo". El texto común se escribe una vez y cada carta solo rellena los huecos.

- **Plantilla** = `AbstractDao`: el código que es igual en todos los DAO.
- **Huecos** = 3 métodos `abstract` que escribe cada hijo:
  - `getTabla()` → `"clientes"`
  - `getColumnaId()` → `"id_cliente"`
  - `mapRsToClass(rs)` → el `new Cliente(...)`
- **Hijos** = `ClienteDao`, `VehiculoDao`... Escriben los huecos, `insertar` y `actualizar`.

`<T, ID>` también son huecos, pero de tipos.
- `ClienteDao extends AbstractDao<Cliente, Integer>` → T es `Cliente` e ID es `Integer`.
- En `VehiculoDao` el ID es `String` (la matrícula).

Por eso el molde usa `ps.setObject(1, id)`, que vale para los dos.

### El molde (lo importante)

```java
public abstract class AbstractDao<T, ID> extends ConexionBD {
    protected AbstractDao(String url) {
        super(url);   // ConexionBD guarda la url
    }

    // Los 3 huecos
    protected abstract String getTabla();
    protected abstract String getColumnaId();
    protected abstract Optional<T> mapRsToClass(ResultSet rs);

    // Vale para todas las tablas gracias a los huecos
    public List<T> listar() {
        String sql = "SELECT * FROM " + getTabla()
                + " ORDER BY " + getColumnaId();
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException e) {
            registrar("listar", e);
            return List.of();
        }
    }

    // Recorre las filas y las convierte con el hueco
    protected List<T> consultar(PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                Optional<T> objeto = mapRsToClass(rs);
                if (objeto.isEmpty()) {
                    return List.of();
                }
                resultado.add(objeto.get());
            }
            return resultado;
        } catch (SQLException e) {
            registrar("consultar", e);
            return List.of();
        }
    }
}
```

Los demás métodos del molde siguen la misma idea:
- `buscarPorId(ID id)`: `WHERE columnaId = ?`, luego `ps.setObject(1, id)` y `consultarUno(ps)`.
- `eliminar(ID id)`: `DELETE ... WHERE columnaId = ?` y `ejecutar(ps)`.
- `buscarPor(columna, valor)`: `WHERE columna = ?` y `consultar(ps)`. Sirve para todos los filtros.
- `consultarUno(ps)`: como `consultar`, pero con `if` y devolviendo un `Optional`.
- `ejecutar(ps)`: `return ps.executeUpdate();` (0 si falla).
- `insertarConId(ps)`: `executeUpdate()` y luego `getGeneratedKeys()` para sacar el id nuevo.
- `textoValido(texto)`: `texto != null && !texto.isBlank()`.
- `registrar(operacion, e)`: apunta el error con el Logger.

### Un hijo

```java
public class ClienteDao extends AbstractDao<Cliente, Integer> {
    public ClienteDao(String url) {
        super(url);
    }

    @Override
    protected String getTabla() {
        return "clientes";
    }

    @Override
    protected String getColumnaId() {
        return "id_cliente";
    }

    @Override
    protected Optional<Cliente> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono")));
        } catch (SQLException e) {
            registrar("mapRsToClass", e);
            return Optional.empty();
        }
    }

    // listar, buscarPorId y eliminar NO se escriben: los da el molde

    public int insertar(Cliente cliente) {
        // esValido: no es null y nombre y email no están en blanco
        if (!esValido(cliente)) {
            return 0;
        }
        String sql = "INSERT INTO clientes (nombre, email, telefono) "
                + "VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            return insertarConId(ps);   // el molde ejecuta
        } catch (SQLException e) {
            registrar("insertar", e);
            return 0;
        }
    }
}
```

- `actualizar` es igual que `insertar`, pero:
  - con UPDATE;
  - sin `RETURN_GENERATED_KEYS`;
  - termina con `return ejecutar(ps);`.
- Un filtro es una sola línea: `return buscarPor("marca", marca);`

### Qué pasa al llamar a `clientes.listar()`

1. `listar()` no está en `ClienteDao`, así que Java usa el del molde.
2. El molde pide los huecos: `getTabla()` → `"clientes"` y `getColumnaId()` → `"id_cliente"`.
3. Ejecuta `SELECT * FROM clientes ORDER BY id_cliente`.
4. Por cada fila llama a `mapRsToClass(rs)` del hijo, que crea un `Cliente`. Al final devuelve la lista.

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

## 8. Que no se me olvide

- Los `?` empiezan en 1. Hay que rellenarlos ANTES de ejecutar.
- `rs.next()` siempre antes de leer la fila.
- Nunca devolver `null`: `List.of()` u `Optional.empty()`.
- `setInt` con un Integer que es `null` → NullPointerException.
- BigDecimal: crearlo con `new BigDecimal("10.25")` y compararlo con `compareTo(...) == 0`.
- Texto obligatorio: `texto != null && !texto.isBlank()`.
- Nada de `+ valor +` dentro del SQL: siempre `?` (evita la inyección SQL).

## 9. Teoría en una línea

- **PreparedStatement**: evita la inyección SQL.
- **try-with-resources**: cierra `con`, `ps` y `rs` aunque haya error.
- **Optional**: obliga a tratar el "no encontrado" sin `null`.
- **Clase abstracta**: lo común se escribe una vez y los hijos rellenan los huecos. No se puede hacer `new`.
- **Interfaz vs abstracta**: la interfaz solo dice qué métodos hay; la abstracta trae código hecho.
- **`<T, ID>`**: el mismo molde vale para ids `Integer` y `String`.
- **Devolver 0 si falla**: no distingue "error" de "no hay datos"; por eso el error se apunta en el log.
