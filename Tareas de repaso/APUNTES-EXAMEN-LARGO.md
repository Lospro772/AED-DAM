# APUNTES EXAMEN — Guardar y leer datos con Java (DAO + JDBC + SQLite)

**Cómo leer estos apuntes:**
- Antes de cada trozo de código hay una frase que dice **qué hace**.
- Dentro del código, el comentario `//` de **encima** explica la línea de **debajo**.
- Algunas líneas están partidas en dos para que se entiendan mejor. Hacen lo mismo que el código de los proyectos.

---

## 0. Las palabras, explicadas fácil

- **Base de datos (SQLite)**: un archivo con tablas, como un libro de Excel con varias hojas.
- **Tabla / fila / columna**: la hoja, cada renglón (un cliente) y cada dato del renglón (su nombre).
- **id (clave primaria)**: el "DNI" de cada fila. No se repite nunca.
- **Clave foránea (FK)**: una columna que apunta al id de otra tabla. Ejemplo: la mascota guarda el id de su dueño.
- **DAO**: la clase que se encarga de UNA tabla: leer, guardar, cambiar y borrar.
- **JDBC**: la herramienta de Java para hablar con bases de datos.
- **`con` (Connection)**: la "llamada" abierta con la base de datos. Hay que colgar (cerrar) al terminar.
- **`ps` (PreparedStatement)**: la orden SQL con huecos `?` que rellenamos después.
- **`rs` (ResultSet)**: las filas que devuelve un SELECT. Se leen una a una con `rs.next()`.
- **`e`**: el error (excepción) que recogemos en el `catch`.
- **try-with-resources** `try (...) { }`: cierra solo lo que se abre dentro del paréntesis, aunque haya error.
- **Optional**: una caja que puede traer un objeto o venir vacía (en vez de devolver `null`).
- **BigDecimal**: números con decimales exactos, para dinero y horas.
- **Clase abstracta**: un molde a medio hacer. No se usa sola; las clases hijas la completan.
- **Interfaz**: la lista de métodos que una clase promete tener, sin el código.

## 1. Plan de ataque (los 5 primeros minutos)

1. Leer `schema.sql`:
   - el nombre EXACTO de tablas y columnas;
   - qué columnas son obligatorias (`NOT NULL`), únicas (`UNIQUE`), con condición (`CHECK`) o que apuntan a otra tabla (`REFERENCES` = FK);
   - qué id pone solo la base de datos (`AUTOINCREMENT`, es un Integer) y cuál es texto (String, por ejemplo la matrícula).
2. Leer los tests: **cuando algo falla, ¿esperan 0 / lista vacía o esperan una excepción?** Eso decide qué poner en el `catch`.
3. ¿Piden `AbstractDao`? → estilo **Taller** (punto 6). ¿No la piden? → estilo **Clínica** (punto 5).
4. Orden de las tablas: primero la que no depende de nadie (Cliente), luego las que dependen de ella (Vehículo/Mascota) y así hasta la última (Piezas/Tratamientos).
5. Orden dentro del DAO: `insertar` → `buscarPorId` → `listar` → `actualizar` → `eliminar` → filtros.

## 2. Qué tiene que devolver cada método (lo que miran los tests)

| Método | Taller (devuelve algo "vacío") | Clínica (lanza error) |
|---|---|---|
| `listar()` | lista vacía, **nunca null** | igual |
| `buscarPorId(null)` | `Optional.empty()` | `Optional.empty()` |
| `insertar` | el id nuevo; `0` si los datos están mal o falla | el id nuevo; si falla → `DaoException` |
| `insertar(null)` | `0` | `Objects.requireNonNull` → NullPointerException |
| `actualizar` | filas cambiadas (1, o 0 si no existe) | sin id → `IllegalArgumentException` |
| `eliminar` | filas borradas (1 / 0) | `eliminar(null)` → NullPointerException |
| filtro con null o "  " | lista vacía | lista vacía |
| coste sin filas | `BigDecimal.ZERO` (nunca null) | igual |

- Fallo en Taller: `catch (SQLException e) { registrar("...", e); return 0; }`
- Fallo en Clínica: `catch (SQLException e) { throw new DaoException("mensaje", e); }`

## 3. La receta: TODOS los métodos siguen los mismos 6 pasos

1. **Comprobar lo que me dan.** Si es `null` (o está mal), salgo ya.
2. **Escribir la orden SQL.** Donde va un dato, pongo `?`.
3. **Abrir la conexión y preparar la orden** dentro de `try ( )`, para que se cierren solas: `con` y `ps`.
4. **Rellenar los `?`** con `ps.setString(1, ...)`, `ps.setInt(2, ...)`... El número es la posición del `?`, contando desde 1.
5. **Ejecutar.**
   - Para **leer** (SELECT): `ps.executeQuery()` → me da las filas en `rs`.
   - Para **cambiar** (INSERT, UPDATE, DELETE): `ps.executeUpdate()` → me da cuántas filas cambiaron.
6. **Devolver el resultado.** Si algo falla, Java salta al `catch` y ahí decido: devolver "vacío" (Taller) o lanzar un error (Clínica).

Si te pierdes en un método, busca estos 6 pasos: siempre están. En el código de abajo van marcados como `Paso 1`, `Paso 2`...

## 4. ConexionBD: la puerta de entrada a la base de datos

**Qué hace:** recibe la dirección (`url`) de la base de datos, comprueba que el archivo existe, se conecta y devuelve la conexión abierta. Todos los DAO entran por aquí.

**Por qué se comprueba el archivo ANTES de conectar:** si el archivo no existe, SQLite no da error; crea uno nuevo vacío. Trabajarías con una base de datos vacía sin enterarte.

```java
public static Connection abrir(String url) throws SQLException {
    // 1. La url tiene que empezar por "jdbc:sqlite:"
    if (url == null || !url.startsWith("jdbc:sqlite:")) {
        throw new SQLException("URL no válida");
    }
    // 2. Quito "jdbc:sqlite:" y me queda la ruta del archivo
    String ruta = url.substring("jdbc:sqlite:".length());
    Path rutaArchivo = Path.of(ruta);
    // 3. Si no existe, no es un archivo o no se puede leer: error
    if (!Files.exists(rutaArchivo)
            || !Files.isRegularFile(rutaArchivo)
            || !Files.isReadable(rutaArchivo)) {
        throw new SQLException("No existe: " + rutaArchivo);
    }
    // 4. Ahora sí, me conecto
    Connection con = DriverManager.getConnection(url);
    try {
        // 5. (Solo si lo piden) compruebo que están las tablas
        comprobarTablas(con);
        // 6. Activo las claves foráneas. Hay que hacerlo en
        //    CADA conexión, por eso se hace aquí
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        // 7. Todo bien: devuelvo la conexión abierta
        return con;
    } catch (SQLException e) {
        // Si algo ha fallado, cierro la conexión y aviso
        con.close();
        throw e;
    }
}
```

**Comprobar las tablas.** `sqlite_master` es la lista donde SQLite apunta qué tablas tiene. Cuento cuántas de mis 5 tablas aparecen ahí. Si el archivo es de texto y no una base de datos, esta consulta ya falla sola.

```java
private static void comprobarTablas(Connection con) throws SQLException {
    String sql = "SELECT COUNT(*) FROM sqlite_master "
            + "WHERE type = 'table' AND name IN ('clientes', "
            + "'vehiculos', 'mecanicos', 'reparaciones', "
            + "'piezas_reparacion')";
    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        int tablasEncontradas = 0;
        if (rs.next()) {
            // La columna 1 es el número que da COUNT(*)
            tablasEncontradas = rs.getInt(1);
        }
        if (tablasEncontradas != 5) {
            throw new SQLException("Faltan tablas del taller");
        }
    }
}
```

En la clínica, el README usa `IllegalArgumentException` en vez de `SQLException`. Las dos valen: mira qué piden los tests.

---

## 5. Estilo CLÍNICA: cada método lo hace todo él solo

Así empieza la clase. El DAO guarda la dirección de la base de datos para usarla en cada método.

```java
public final class ClienteDao implements IClienteDao {
    // La dirección de la base de datos. Me la dan al crear el DAO
    private final String url;

    public ClienteDao(String url) {
        this.url = url;
    }
```

### 5.1 `listar()`: todos los clientes

**Qué hace:** lee todas las filas de la tabla y devuelve una lista de objetos `Cliente`. Si no hay ninguno, la lista vuelve vacía (nunca `null`).

**Fíjate:** como el SQL no tiene ningún `?`, se puede ejecutar directamente. Por eso `rs` va en el mismo paréntesis del `try` que `con` y `ps`.

```java
public List<Cliente> listar() {
    // Paso 2: la orden SQL (aquí no hay ningún ?)
    String sql = "SELECT id, nombre, email, telefono "
            + "FROM clientes ORDER BY id";
    // Aquí voy metiendo los clientes que lea
    List<Cliente> clientes = new ArrayList<>();
    // Pasos 3 y 5: abro la conexión (con), preparo la orden (ps)
    // y la ejecuto (rs). Las tres se cierran solas al final
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        // rs.next() pasa a la siguiente fila.
        // Da false cuando ya no quedan filas
        while (rs.next()) {
            // Con los datos de la fila creo un Cliente.
            // Lo de las comillas es el nombre de la columna
            Cliente cliente = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono"));
            clientes.add(cliente);
        }
        // Paso 6: devuelvo la lista
        return clientes;
    } catch (SQLException e) {
        // Si algo falla, aviso con DaoException
        // y guardo dentro el error original (e)
        throw new DaoException("Error al listar clientes", e);
    }
}
```

### 5.2 `buscarPorId(id)`: un cliente

**Qué hace:** busca el cliente con ese id. Devuelve una "caja" (`Optional`): con el cliente dentro si existe, o vacía si no.

**Diferencia con `listar`:** aquí hay un `?` y hay que rellenarlo ANTES de ejecutar. Por eso `rs` va en un segundo `try`, después del `setInt`. Y como como mucho hay una fila, se usa `if` en vez de `while`.

```java
public Optional<Cliente> buscarPorId(Integer id) {
    // Paso 1: si no me dan id, devuelvo la caja vacía
    if (id == null) {
        return Optional.empty();
    }
    // Paso 2: el ? es el hueco donde irá el id
    String sql = "SELECT id, nombre, email, telefono "
            + "FROM clientes WHERE id = ?";
    // Paso 3: abro la conexión y preparo la orden
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {
        // Paso 4: relleno el primer ? con el id
        ps.setInt(1, id);
        // Paso 5: AHORA ejecuto (en otro try para que rs
        // también se cierre solo)
        try (ResultSet rs = ps.executeQuery()) {
            // Como mucho hay una fila: if, no while
            if (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("telefono"));
                // Paso 6: devuelvo la caja con el cliente
                return Optional.of(cliente);
            }
            // No había ninguna fila: caja vacía
            return Optional.empty();
        }
    } catch (SQLException e) {
        throw new DaoException("Error al buscar el cliente", e);
    }
}
```

### 5.3 `insertar(cliente)`: guardar uno nuevo

**Qué hace:** guarda el cliente y devuelve el id que le ha puesto la base de datos.

**Fíjate:**
- La columna `id` NO aparece en el INSERT, porque es `AUTOINCREMENT`: el número lo inventa la base de datos.
- `Statement.RETURN_GENERATED_KEYS` significa "luego dame el id nuevo". Se recoge con `ps.getGeneratedKeys()`.

```java
public int insertar(Cliente cliente) {
    // Paso 1: si el cliente es null, error.
    // requireNonNull lanza NullPointerException con este mensaje
    Objects.requireNonNull(cliente, "El cliente no puede ser null");
    // Paso 2: no pongo la columna id, la pone la base de datos
    String sql = "INSERT INTO clientes (nombre, email, telefono) "
            + "VALUES (?, ?, ?)";
    // Paso 3: RETURN_GENERATED_KEYS = "luego dame el id nuevo"
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql,
                 Statement.RETURN_GENERATED_KEYS)) {
        // Paso 4: relleno los 3 ? en el mismo orden que el SQL
        ps.setString(1, cliente.getNombre());
        ps.setString(2, cliente.getEmail());
        ps.setString(3, cliente.getTelefono());
        // Paso 5: es un cambio, así que executeUpdate
        ps.executeUpdate();
        // El id nuevo llega como una fila con una sola columna
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                // Paso 6: la columna 1 es el id nuevo
                return rs.getInt(1);
            }
            return 0;
        }
    } catch (SQLException e) {
        // Por ejemplo: email repetido (UNIQUE)
        throw new DaoException("Error al insertar cliente", e);
    }
}
```

### 5.4 `actualizar(cliente)`: cambiar uno que ya existe

**Qué hace:** cambia los datos de un cliente. Devuelve cuántas filas ha cambiado: 1 si lo encontró, 0 si ese id no existe.

**Truco:** el id va en el `WHERE`, que está al final del SQL. Por eso su `?` es el último.

```java
public int actualizar(Cliente cliente) {
    // Paso 1: sin cliente o sin id no se puede actualizar
    Objects.requireNonNull(cliente, "El cliente no puede ser null");
    if (cliente.getId() == null) {
        throw new IllegalArgumentException("Hace falta el id");
    }
    // Paso 2: 4 huecos = 3 datos nuevos + el id del WHERE
    String sql = "UPDATE clientes SET nombre = ?, email = ?, "
            + "telefono = ? WHERE id = ?";
    // Paso 3 (aquí no hace falta RETURN_GENERATED_KEYS)
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {
        // Paso 4: primero los datos nuevos...
        ps.setString(1, cliente.getNombre());
        ps.setString(2, cliente.getEmail());
        ps.setString(3, cliente.getTelefono());
        // ...y el id el último, como en el SQL
        ps.setInt(4, cliente.getId());
        // Pasos 5 y 6: ejecuto y devuelvo las filas cambiadas
        return ps.executeUpdate();
    } catch (SQLException e) {
        throw new DaoException("Error al actualizar", e);
    }
}
```

### 5.5 `eliminar(id)`: borrar uno

**Qué hace:** borra el cliente con ese id. Devuelve 1 si lo borró y 0 si no existía.

```java
public int eliminar(Integer id) {
    // Paso 1: sin id → NullPointerException (lo piden los tests)
    Objects.requireNonNull(id, "El id no puede ser null");
    // Paso 2
    String sql = "DELETE FROM clientes WHERE id = ?";
    // Paso 3
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {
        // Paso 4
        ps.setInt(1, id);
        // Pasos 5 y 6: 1 si lo borró, 0 si no existía
        return ps.executeUpdate();
    } catch (SQLException e) {
        // Por ejemplo: el cliente aún tiene mascotas (FK)
        throw new DaoException("Error al eliminar", e);
    }
}
```

### 5.6 Un filtro: `listarPorEspecie(especie)`

**Qué hace:** como `listar`, pero solo las filas que cumplen una condición.

**Es una mezcla de los dos anteriores:**
- tiene un `?` → como `buscarPorId`, se rellena antes de ejecutar y `rs` va en un segundo `try`;
- puede devolver muchas filas → como `listar`, usa `while` y una lista.

```java
public List<Mascota> listarPorEspecie(String especie) {
    // Paso 1: sin especie → lista vacía (NO todas las mascotas)
    if (especie == null || especie.isBlank()) {
        return List.of();
    }
    // Paso 2
    String sql = "SELECT id, nombre, especie, raza, "
            + "fecha_nacimiento, id_cliente FROM mascotas "
            + "WHERE especie = ? ORDER BY id";
    List<Mascota> mascotas = new ArrayList<>();
    // Paso 3
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {
        // Paso 4: gracias al ?, un texto como "Perro' OR 1=1 --"
        // se busca como texto normal y no como SQL
        ps.setString(1, especie);
        // Paso 5
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Mascota mascota = new Mascota(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("especie"),
                        rs.getString("raza"),
                        rs.getString("fecha_nacimiento"),
                        rs.getInt("id_cliente"));
                mascotas.add(mascota);
            }
        }
        // Paso 6
        return mascotas;
    } catch (SQLException e) {
        throw new DaoException("Error al filtrar mascotas", e);
    }
}
```

### 5.7 Un cálculo: `calcularCosteTratamientos(idConsulta)`

**Qué hace:** suma lo que cuestan los tratamientos de una consulta (cantidad × precio de cada uno).

**Fíjate:**
- La suma la hace la base de datos con `SUM`.
- Si no hay tratamientos, `SUM` da `NULL`; `COALESCE(..., 0)` lo cambia por 0.
- `AS total` le pone nombre a la columna del resultado, para luego leerla con `rs.getBigDecimal("total")`.

```java
public BigDecimal calcularCosteTratamientos(Integer idConsulta) {
    // Paso 1
    if (idConsulta == null) {
        return BigDecimal.ZERO;
    }
    // Paso 2
    String sql = "SELECT COALESCE(SUM(cantidad * precio_unitario), 0)"
            + " AS total FROM tratamientos WHERE id_consulta = ?";
    // Paso 3
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {
        // Paso 4
        ps.setInt(1, idConsulta);
        // Paso 5: una suma siempre devuelve exactamente una fila
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                if (total != null) {
                    // Paso 6
                    return total;
                }
            }
            return BigDecimal.ZERO;
        }
    } catch (SQLException e) {
        throw new DaoException("Error al calcular el coste", e);
    }
}
```

---

## 6. Estilo TALLER: la clase abstracta, explicada despacio

### 6.1 La idea, con un ejemplo de la vida real

Piensa en una carta hecha con plantilla:

> "Estimado/a ______: le informamos de que su ______ está listo."

El texto es igual para todos y solo cambian los huecos. Quien escribe cada carta solo rellena los huecos; no vuelve a escribir la carta entera.

- La **plantilla** es la clase abstracta `AbstractDao`: tiene escrito el código que es igual en todos los DAO.
- Los **huecos** son los métodos `abstract`: el nombre de la tabla, la columna del id y cómo convertir una fila en objeto.
- Cada **carta rellena** es un DAO hijo (`ClienteDao`, `VehiculoDao`...). Solo escribe los huecos y lo que es solo suyo (`insertar` y `actualizar`).

### 6.2 Por qué hace falta: el código se repite

Mira `listar()` de la clínica en dos DAO distintos:

```java
// En ClienteDao
String sql = "SELECT ... FROM clientes ORDER BY id";
try (Connection con = ConexionBD.abrir(url);
     PreparedStatement ps = con.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        clientes.add(new Cliente(rs.getInt("id"), ...));
    }
    return clientes;
}

// En MascotaDao
String sql = "SELECT ... FROM mascotas ORDER BY id";
try (Connection con = ConexionBD.abrir(url);
     PreparedStatement ps = con.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    while (rs.next()) {
        mascotas.add(new Mascota(rs.getInt("id"), ...));
    }
    return mascotas;
}
```

Solo cambian **3 cosas**:
1. la tabla (`clientes` / `mascotas`);
2. la columna del id;
3. el `new Cliente(...)` / `new Mascota(...)`.

Todo lo demás es idéntico. Así que lo idéntico se escribe UNA vez en el molde, y las 3 cosas que cambian se convierten en huecos:
1. la tabla → `getTabla()`
2. la columna del id → `getColumnaId()`
3. el `new Cliente(...)` → `mapRsToClass(rs)`

### 6.3 Quién hace qué

**El molde (`AbstractDao`) hace:**
- abrir la conexión (lo hereda de `ConexionBD`);
- `listar()`, `buscarPorId()` y `eliminar()` completos;
- `buscarPor(columna, valor)` para todos los filtros;
- recorrer las filas: `consultar()` (muchas) y `consultarUno()` (una);
- ejecutar cambios: `ejecutar()`, y sacar el id nuevo: `insertarConId()`;
- apuntar los errores: `registrar()`.

**Cada hijo (`ClienteDao`, `VehiculoDao`...) hace:**
- decir su tabla: `getTabla()`;
- decir su columna id: `getColumnaId()`;
- convertir una fila en objeto: `mapRsToClass(rs)`;
- `insertar()` y `actualizar()`, porque las columnas y los `?` son distintos en cada tabla;
- sus filtros, en una sola línea, con `buscarPor(...)`.

### 6.4 Palabras nuevas de Java

- **`abstract class`**: clase molde. No se puede hacer `new AbstractDao(...)`; solo se usan sus hijas.
- **`abstract` en un método**: el método no tiene código, solo el nombre. Cada hijo está obligado a escribirlo.
- **`extends`**: "es hijo de". El hijo tiene todo lo del padre sin escribirlo.
- **`super(url)`**: el hijo le pasa la `url` al constructor del padre, que la guarda.
- **`protected`**: lo pueden usar el padre y los hijos, pero no el resto del programa.
- **`@Override`**: "estoy escribiendo un hueco del padre". Si me equivoco en el nombre, Java avisa.

### 6.5 Qué es `<T, ID>`

Son huecos, pero de **tipos de dato**:
- `T` = qué objeto maneja el DAO.
- `ID` = de qué tipo es su id.

Se rellenan al escribir el hijo:
- `ClienteDao extends AbstractDao<Cliente, Integer>` → T es `Cliente` e ID es `Integer`.
- `VehiculoDao extends AbstractDao<Vehiculo, String>` → T es `Vehiculo` e ID es `String` (la matrícula).

Java hace el cambio solo. Este método del molde:

```java
public Optional<T> buscarPorId(ID id)
```

en `ClienteDao` funciona como:

```java
public Optional<Cliente> buscarPorId(Integer id)
```

y en `VehiculoDao` como:

```java
public Optional<Vehiculo> buscarPorId(String id)
```

Por eso, para rellenar el `?` del id, el molde usa `ps.setObject(1, id)`: vale tanto para un número como para un texto.

### 6.6 El molde completo, comentado

```java
// "abstract" = es un molde.
// "extends ConexionBD" = hereda obtenerConexion(),
// así no hay que repetir cómo se conecta
public abstract class AbstractDao<T, ID> extends ConexionBD {

    // Sirve para apuntar los errores
    private static final Logger LOG =
            Logger.getLogger(AbstractDao.class.getName());

    // Recibe la url y se la pasa a ConexionBD, que la guarda
    protected AbstractDao(String url) {
        super(url);
    }

    // ========== LOS 3 HUECOS: los escribe cada hijo ==========

    // El nombre de la tabla, por ejemplo "clientes"
    protected abstract String getTabla();

    // La columna del id, por ejemplo "id_cliente"
    protected abstract String getColumnaId();

    // Convierte la fila en la que está rs en un objeto T
    protected abstract Optional<T> mapRsToClass(ResultSet rs);

    // ===== LO QUE ES IGUAL PARA TODAS LAS TABLAS =====

    public List<T> listar() {
        // Le pido al hijo su tabla y su columna id. Queda, por
        // ejemplo: SELECT * FROM clientes ORDER BY id_cliente
        String sql = "SELECT * FROM " + getTabla()
                + " ORDER BY " + getColumnaId();
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // consultar() ejecuta y convierte las filas
            return consultar(ps);
        } catch (SQLException e) {
            registrar("listar " + getTabla(), e);
            return List.of();
        }
    }

    public Optional<T> buscarPorId(ID id) {
        if (!idValido(id)) {
            return Optional.empty();
        }
        // SELECT * FROM clientes WHERE id_cliente = ?
        String sql = "SELECT * FROM " + getTabla()
                + " WHERE " + getColumnaId() + " = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // setObject vale para Integer y para String
            ps.setObject(1, id);
            // consultarUno() ejecuta y convierte UNA fila
            return consultarUno(ps);
        } catch (SQLException e) {
            registrar("buscarPorId " + getTabla(), e);
            return Optional.empty();
        }
    }

    public int eliminar(ID id) {
        if (!idValido(id)) {
            return 0;
        }
        // DELETE FROM clientes WHERE id_cliente = ?
        String sql = "DELETE FROM " + getTabla()
                + " WHERE " + getColumnaId() + " = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("eliminar " + getTabla(), e);
            return 0;
        }
    }

    // Para los filtros: WHERE columna = valorBuscado.
    // Ejemplo: buscarPor("marca", "Seat")
    protected List<T> buscarPor(String columna, Object valorBuscado) {
        // Sin valor, o texto en blanco → lista vacía
        if (valorBuscado == null) {
            return List.of();
        }
        if (valorBuscado instanceof String texto && texto.isBlank()) {
            return List.of();
        }
        String sql = "SELECT * FROM " + getTabla() + " WHERE "
                + columna + " = ? ORDER BY " + getColumnaId();
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, valorBuscado);
            return consultar(ps);
        } catch (SQLException e) {
            registrar("buscarPor " + columna, e);
            return List.of();
        }
    }

    // ===== AYUDANTES: reciben el ps ya preparado =====

    // Ejecuta un SELECT y devuelve TODAS las filas en una lista
    protected List<T> consultar(PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                // El hijo convierte la fila en objeto
                Optional<T> objeto = mapRsToClass(rs);
                // Si una fila falla, no devuelvo la lista a medias
                if (objeto.isEmpty()) {
                    return List.of();
                }
                // Saco el objeto de la caja y lo meto en la lista
                resultado.add(objeto.get());
            }
            return resultado;
        } catch (SQLException e) {
            registrar("consultar", e);
            return List.of();
        }
    }

    // Ejecuta un SELECT que da como mucho UNA fila
    protected Optional<T> consultarUno(PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRsToClass(rs);
            }
            return Optional.empty();
        } catch (SQLException e) {
            registrar("consultarUno", e);
            return Optional.empty();
        }
    }

    // Ejecuta un INSERT/UPDATE/DELETE.
    // Devuelve cuántas filas cambiaron (0 si falla)
    protected int ejecutar(PreparedStatement ps) {
        try {
            return ps.executeUpdate();
        } catch (SQLException e) {
            registrar("ejecutar", e);
            return 0;
        }
    }

    // Ejecuta un INSERT y devuelve el id nuevo (0 si falla).
    // El ps tiene que estar creado con RETURN_GENERATED_KEYS
    protected int insertarConId(PreparedStatement ps) {
        try {
            int filasInsertadas = ps.executeUpdate();
            if (filasInsertadas == 0) {
                return 0;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    // La columna 1 es el id nuevo
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            registrar("insertar", e);
            return 0;
        }
    }

    // ===== COMPROBACIONES QUE USAN TODOS =====

    // Vale si no es null, no está vacío y no son solo espacios
    protected static boolean textoValido(String texto) {
        return texto != null && !texto.isBlank();
    }

    // Un id vale si no es null y, si es texto, no está en blanco
    protected boolean idValido(ID id) {
        if (id == null) {
            return false;
        }
        if (id instanceof String texto) {
            return !texto.isBlank();
        }
        return true;
    }

    // Apunta el error sin datos personales:
    // solo qué operación falló y el mensaje de la base de datos
    protected void registrar(String operacion, SQLException e) {
        LOG.log(Level.WARNING,
                "Fallo en " + operacion + ": " + e.getMessage());
    }
}
```

### 6.7 Un hijo completo, comentado: `ClienteDao`

```java
// Hijo del molde: T = Cliente, ID = Integer
public class ClienteDao extends AbstractDao<Cliente, Integer> {

    // Le paso la url al molde
    public ClienteDao(String url) {
        super(url);
    }

    // ========== Relleno los 3 huecos ==========

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
            // Es el mismo new Cliente(...) que en la clínica
            Cliente cliente = new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono"));
            return Optional.of(cliente);
        } catch (SQLException e) {
            registrar("mapRsToClass cliente", e);
            return Optional.empty();
        }
    }

    // listar(), buscarPorId() y eliminar() NO se escriben:
    // ya los tiene el molde

    // ========== Lo que es solo de los clientes ==========

    // Nombre y email obligatorios; el teléfono puede faltar
    private boolean esValido(Cliente cliente) {
        return cliente != null
                && textoValido(cliente.getNombre())
                && textoValido(cliente.getEmail());
    }

    public int insertar(Cliente cliente) {
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
            // El molde ejecuta y me devuelve el id nuevo
            return insertarConId(ps);
        } catch (SQLException e) {
            registrar("insertar cliente", e);
            return 0;
        }
    }

    public int actualizar(Cliente cliente) {
        if (!esValido(cliente) || cliente.getIdCliente() == null) {
            return 0;
        }
        String sql = "UPDATE clientes SET nombre = ?, email = ?, "
                + "telefono = ? WHERE id_cliente = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getEmail());
            ps.setString(3, cliente.getTelefono());
            ps.setInt(4, cliente.getIdCliente());
            // El molde ejecuta y me dice cuántas filas cambiaron
            return ejecutar(ps);
        } catch (SQLException e) {
            registrar("actualizar cliente", e);
            return 0;
        }
    }
}
```

**Los filtros, en otro hijo (`VehiculoDao`), son una sola línea:**

```java
public List<Vehiculo> listarPorMarca(String marca) {
    // WHERE marca = ?  → todo lo hace el molde
    return buscarPor("marca", marca);
}
```

**Ojo con `VehiculoDao.insertar`:** la matrícula la trae el vehículo; no la inventa la base de datos. Así que NO se usa `RETURN_GENERATED_KEYS` y se termina con `return ejecutar(ps);` (devuelve 1 si se guardó).

### 6.8 Sigue una llamada, paso a paso

**`clientes.listar()`**
1. Java busca `listar()` en `ClienteDao`. No está, así que usa el del padre, `AbstractDao`.
2. El molde necesita la tabla y llama a `getTabla()`. Como el objeto es un `ClienteDao`, se ejecuta el de `ClienteDao` → `"clientes"`.
3. Lo mismo con `getColumnaId()` → `"id_cliente"`.
4. Queda el SQL: `SELECT * FROM clientes ORDER BY id_cliente`.
5. `obtenerConexion()` (de `ConexionBD`, el "abuelo") abre la conexión.
6. `consultar(ps)` ejecuta y, por cada fila, llama a `mapRsToClass(rs)`. Otra vez se usa el de `ClienteDao`, que crea un `Cliente`.
7. Devuelve la lista de clientes.

**`clientes.insertar(ana)`**
1. `insertar` SÍ está en `ClienteDao`, así que se ejecuta ese.
2. Comprueba que Ana es válida, escribe el SQL y rellena los 3 `?`.
3. Llama a `insertarConId(ps)` del molde, que ejecuta y devuelve el id nuevo.

**`vehiculos.buscarPorId("1234ABC")`**
1. No está en `VehiculoDao`, así que usa el del molde.
2. Como `VehiculoDao` es `AbstractDao<Vehiculo, String>`, el id es un `String`.
3. SQL: `SELECT * FROM vehiculos WHERE matricula = ?`, rellenado con `setObject(1, "1234ABC")`.
4. `consultarUno(ps)` llama a `mapRsToClass` de `VehiculoDao` y devuelve la caja con el vehículo.

### 6.9 Receta: pasar del estilo clínica al estilo taller

1. Copia `AbstractDao<T, ID>` tal cual. No depende del ejercicio: solo cambia el `package`.
2. En cada DAO pon `extends AbstractDao<Entidad, TipoDelId>`, y en el constructor `super(url);`.
3. Escribe los 3 huecos: `getTabla()`, `getColumnaId()` y `mapRsToClass(rs)`. Para este último, copia el `new Cliente(...)` que ya tenías en `listar`.
4. Borra del hijo `listar`, `buscarPorId` y `eliminar`: ya los tiene el molde.
5. En `insertar` y `actualizar` deja el SQL y los `set`, y cambia solo el final:
   - si el id lo pone la base de datos: `return insertarConId(ps);`
   - si no (matrícula) o es un `actualizar`: `return ejecutar(ps);`
6. Cada filtro se queda en una línea: `return buscarPor("columna", valor);`
7. Si el examen quiere excepciones (como la clínica) en vez de 0: en los `catch` del molde cambia `registrar(...); return 0;` por `throw new DaoException("...", e);`.
8. Si los DAO tienen interfaz: `extends AbstractDao<Cliente, Integer> implements IClienteDao`. Funciona porque los métodos que vienen del molde ya cumplen la interfaz.

---

## 7. Chuleta JDBC

- `executeQuery()` es para **leer** (SELECT) y devuelve `rs`. `executeUpdate()` es para **cambiar** (INSERT/UPDATE/DELETE) y devuelve cuántas filas cambiaron.
- Los `?` se cuentan desde **1**, no desde 0. En un UPDATE el id va el **último** (como en el WHERE).
- Para meter datos: `setInt / setString / setBigDecimal / setObject`. Para sacarlos: `getInt / getString / getBigDecimal`.
- `setInt(posicion, numero)` con `numero` a null da **NullPointerException**. Compruébalo antes o usa `setObject`.
- `setString(posicion, null)` sí guarda NULL. Sirve para datos que pueden faltar (teléfono, raza).
- Siempre `rs.next()` antes de leer la fila.
- El `try (...)` cierra solo, de dentro afuera: `rs` → `ps` → `con`.
- La base de datos da `SQLException` si se rompe una regla:
  - `UNIQUE`: email repetido;
  - `CHECK`: estado que no existe, cantidad 0, tarifa negativa;
  - `FK`: apuntar a un id que no existe.

## 8. BigDecimal y comprobaciones

- Crear desde texto: `new BigDecimal("10.25")`, nunca desde un `double` (los double no son exactos).
- Comparar con `compareTo(...) == 0`, no con `equals` (para `equals`, `20.8` y `20.80` son distintos).
- No negativo: `numero.compareTo(BigDecimal.ZERO) >= 0`.
- Texto obligatorio: `texto != null && !texto.isBlank()` (NOT NULL no impide `""` ni `"   "`).
- Fecha `AAAA-MM-DD`: `LocalDate.parse(fecha)` dentro de `try { } catch (DateTimeParseException e) { }`.
- Estados permitidos: `Set.of("PENDIENTE", "EN_PROCESO", "FINALIZADA").contains(estado)`.

## 9. Chuleta SQL

```sql
-- Unir dos tablas: con WHERE o con INNER JOIN da lo mismo
SELECT vehiculos.matricula, clientes.nombre
FROM vehiculos, clientes
WHERE vehiculos.id_cliente = clientes.id_cliente;

SELECT vehiculos.matricula, clientes.nombre
FROM vehiculos
INNER JOIN clientes
    ON vehiculos.id_cliente = clientes.id_cliente;

-- Contar incluyendo a los que tienen 0:
-- LEFT JOIN + COUNT(columna de la tabla de la derecha)
SELECT clientes.id_cliente, clientes.nombre,
       COUNT(vehiculos.matricula) AS num_vehiculos
FROM clientes
LEFT JOIN vehiculos
    ON vehiculos.id_cliente = clientes.id_cliente
GROUP BY clientes.id_cliente, clientes.nombre;

-- Sumar incluyendo a los que no tienen filas:
-- COALESCE(SUM(...), 0)
SELECT reparaciones.id_reparacion,
       COALESCE(SUM(piezas_reparacion.cantidad
           * piezas_reparacion.precio_unitario), 0) AS coste
FROM reparaciones
LEFT JOIN piezas_reparacion
    ON piezas_reparacion.id_reparacion = reparaciones.id_reparacion
GROUP BY reparaciones.id_reparacion;

-- Los que NO tienen hijos: LEFT JOIN ... WHERE hijo.id IS NULL
SELECT reparaciones.id_reparacion
FROM reparaciones
LEFT JOIN piezas_reparacion
    ON piezas_reparacion.id_reparacion = reparaciones.id_reparacion
WHERE piezas_reparacion.id_pieza IS NULL;
```

- **INNER JOIN**: solo las filas que tienen pareja en las dos tablas.
- **LEFT JOIN**: TODAS las filas de la tabla de la izquierda. Si no tienen pareja, lo de la derecha sale vacío (NULL).
- **GROUP BY**: hace montoncitos para contar (COUNT) o sumar (SUM) por cada uno.
- **COALESCE(a, b)**: si `a` está vacío (NULL), usa `b`.
- `COUNT(*)` cuenta también la fila sin pareja del LEFT JOIN. Por eso se usa `COUNT(columna de la derecha)`, que ahí da 0.
- En clase verás `FROM vehiculos v`: la `v` es un apodo de la tabla. Es lo mismo que escribir `vehiculos.` delante de cada columna.
- `PRAGMA foreign_keys = ON` solo sirve para **esa conexión**.
  - Sin ella, SQLite deja guardar FK que apuntan a ids inexistentes y borrar un padre que tiene hijos.
  - Con ella, eso da `FOREIGN KEY constraint failed`.
- La FK protege los datos, pero NO une las tablas por ti: la condición del `ON` la escribes tú.

## 10. Fallos que me pueden pillar

- Escribir mal una columna (`id_clientes` en vez de `id_cliente`).
- Meter valores pegados al SQL (`"... WHERE nombre = '" + nombre + "'"`): eso permite inyección SQL. Siempre `?`.
- Devolver `null` en vez de `List.of()` u `Optional.empty()`.
- Leer `rs` sin hacer `rs.next()` antes.
- Ejecutar (`executeQuery`) antes de rellenar los `?`.
- Abrir la conexión fuera del `try (...)`: luego no se cierra.
- Usar una conexión fija para todo (estática): los tests se mezclan. Hay que abrir una por operación.
- Comprobar el archivo DESPUÉS de conectar: SQLite ya lo habría creado vacío.
- Poner `catch (Exception e)` para todo: esconde los fallos de programación.

## 11. Preguntas de teoría (respuesta corta)

- **¿Por qué `PreparedStatement`?** Porque el valor va como dato y no como código: evita la inyección SQL. Además convierte los tipos.
- **¿Por qué try-with-resources?** Cierra `con`, `ps` y `rs` aunque haya un error.
- **¿Por qué `Optional`?** Obliga a pensar en el caso "no encontrado" sin devolver `null`.
- **¿Por qué una clase abstracta?** Para escribir lo común UNA vez y arreglarlo en un solo sitio. No se puede crear con `new`; los hijos completan lo que falta (tabla, id y `mapRsToClass`).
- **¿Interfaz o clase abstracta?** La interfaz solo dice QUÉ métodos hay. La abstracta además trae código hecho y puede guardar datos (la url). Un DAO puede usar las dos: `extends AbstractDao<...> implements IClienteDao`.
- **¿Por qué `<T, ID>`?** Para que el mismo código sirva para claves `Integer` y `String` sin perder los tipos.
- **¿Qué problema tiene devolver 0 o lista vacía cuando falla?** Que no se distingue "hubo un error" de "no hay datos". Por eso el error se apunta en el registro (Logger), sin datos personales.
- **¿Por qué ConexionBD en una sola clase?** Hay un único sitio para:
  - la url;
  - comprobar el archivo;
  - activar las FK;
  - tratar los errores.

  Además, para cambiar de base de datos (la de trabajo o la de pruebas) no hay que tocar los DAO.
