# APUNTES — CRUD de un DAO (SocioDao, JPA)

- **Leer** (`listar`, `buscarPorId`): sin transacción.
- **Escribir** (`insertar`, `actualizar`, `eliminar`): `begin` → operación → `flush` → `commit`; si falla, `rollback` y `return 0`.
- Los otros DAO son iguales: cambia `Socio` por `Libro`, `Autor`... y los `set` del `actualizar`.

```java
package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Socio;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.entity.SocioEntity;
import es.ejemplo.biblioteca.orm.mapper.SocioMapper;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocioDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocioDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    // Crea su propia factoría (la cierra en close)
    public SocioDao(String url) {
        managerFactory = OrmFactory.crear(url);
        ownFactory = true;
    }

    // Usa una factoría compartida (no la cierra)
    public SocioDao(EntityManagerFactory factory) {
        managerFactory = factory;
        ownFactory = false;
    }

    private void registrar(String metodo, Exception ex) {
        LOG.log(Level.WARNING, metodo + " no se pudo completar: " + ex.getMessage());
    }

    private boolean validar(Socio s) {
        return s != null
                && s.getNombre() != null && !s.getNombre().isBlank()
                && s.getEmail() != null && !s.getEmail().isBlank()
                && s.getFechaAlta() != null
                && s.getActivo() != null;
    }

    // ===================== READ (todos) =====================
    public List<Socio> listar() {
        try (var em = managerFactory.createEntityManager()) {
            return em.createQuery("select s from SocioEntity s order by s.nombre", SocioEntity.class)
                    .getResultStream()
                    .map(SocioMapper::toDomain)
                    .toList();
        } catch (RuntimeException ex) {
            registrar("listar", ex);
            return List.of();
        }
    }

    // ===================== READ (uno) =====================
    public Optional<Socio> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        try (var em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SocioEntity.class, id))   // find -> null si no existe
                    .map(SocioMapper::toDomain);
        } catch (RuntimeException ex) {
            registrar("buscarPorId", ex);
            return Optional.empty();
        }
    }

    // ===================== CREATE =====================
    public int insertar(Socio socio) {
        if (!validar(socio)) return 0;
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity entidad = SocioMapper.toEntity(socio);
                entidad.setIdSocio(null);          // el id lo genera la BD
                em.persist(entidad);
                em.flush();                        // hace el INSERT -> ya hay id
                tx.commit();
                socio.setIdSocio(entidad.getIdSocio());
                return entidad.getIdSocio();
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                registrar("insertar", ex);
                return 0;
            }
        }
    }

    // ===================== UPDATE =====================
    public int actualizar(Socio socio) {
        if (!validar(socio) || socio.getIdSocio() == null) return 0;
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity s = em.find(SocioEntity.class, socio.getIdSocio());
                if (s == null) {
                    tx.rollback();
                    return 0;
                }
                // la entidad ya está gestionada: con los set basta, el commit hace el UPDATE
                s.setNombre(socio.getNombre());
                s.setEmail(socio.getEmail());
                s.setTelefono(socio.getTelefono());
                s.setFechaAlta(socio.getFechaAlta());
                s.setActivo(socio.getActivo());
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                registrar("actualizar", ex);
                return 0;
            }
        }
    }

    // ===================== DELETE =====================
    public int eliminar(Integer id) {
        if (id == null) return 0;
        try (var em = managerFactory.createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                SocioEntity s = em.find(SocioEntity.class, id);
                if (s == null) {
                    tx.rollback();
                    return 0;
                }
                em.remove(s);
                em.flush();                        // si tiene préstamos (FK) falla aquí -> rollback
                tx.commit();
                return 1;
            } catch (RuntimeException ex) {
                if (tx.isActive()) tx.rollback();
                registrar("eliminar", ex);
                return 0;
            }
        }
    }

    @Override
    public void close() {
        if (ownFactory && managerFactory.isOpen()) managerFactory.close();
    }
}
```

---

## Explicación del código

### Las piezas

| Pieza | Para qué sirve |
|---|---|
| `EntityManagerFactory` | Se crea **una vez** (es caro). Lee `persistence.xml` y fabrica `EntityManager`. |
| `EntityManager em` | Una "sesión" con la BD. Se abre y se cierra **en cada método** (`try (var em = ...)`). |
| `ownFactory` | `true` si el DAO creó la factoría, y entonces la cierra en `close()`. `false` si se la pasaron desde fuera (compartida): no la cierra. |
| `Socio` (modelo) | Lo que usa el resto del programa. No sabe nada de JPA. |
| `SocioEntity` | La clase con `@Entity`, `@Table`, `@Column`. Es lo que JPA guarda y lee. |
| `SocioMapper` | Convierte entre los dos: `toEntity(socio)` y `toDomain(entity)`. |
| `registrar(...)` | Escribe el error en el log. El DAO **no lanza excepciones**: devuelve 0, lista vacía u `Optional.empty()`. |
| `validar(...)` | Comprueba los campos obligatorios antes de tocar la BD. |

### Leer vs escribir

- **Leer** (`listar`, `buscarPorId`): no cambia nada, así que **no necesita transacción**.
- **Escribir** (`insertar`, `actualizar`, `eliminar`): **siempre** con transacción:

```java
var tx = em.getTransaction();
try {
    tx.begin();       // empieza
    // ... persist / set / remove
    em.flush();       // manda el SQL ya (los errores salen aquí, dentro del try)
    tx.commit();      // confirma
} catch (RuntimeException ex) {
    if (tx.isActive()) tx.rollback();   // deshace todo si algo falla
    return 0;
}
```

### Cada método

- **listar**: JPQL `select s from SocioEntity s`. En JPQL se usa el **nombre de la clase y de los atributos Java**, no el de la tabla ni las columnas (`SocioEntity`, `s.idSocio`; no `socios`, `id_socio`).
  - `getResultStream().map(SocioMapper::toDomain).toList()` pasa cada entidad a modelo.
- **buscarPorId**: `em.find(Clase, id)` busca por clave primaria y devuelve `null` si no existe. Con `Optional.ofNullable` ese `null` pasa a ser `Optional.empty()`.
- **insertar**:
  - `persist` deja la entidad lista para el INSERT.
  - `flush` lo ejecuta, y así la BD genera el id (`@GeneratedValue(IDENTITY)`).
  - Después se copia ese id al modelo y se devuelve.
  - `setIdSocio(null)` asegura que no se intenta meter un id a mano.
- **actualizar**:
  - `find` carga la entidad, que queda **gestionada** (*managed*).
  - Cualquier `set` sobre ella se detecta solo (*dirty checking*) y en el `commit` se hace el UPDATE. **No hace falta `merge` ni `persist`.**
  - Si `find` da `null`, no existe: rollback y `return 0`.
- **eliminar**:
  - `remove` solo funciona sobre una entidad gestionada, por eso primero `find`.
  - Si el socio tiene préstamos, la FK (`ON DELETE RESTRICT`) hace fallar el `flush`: salta al `catch`, se hace rollback y `return 0`.

### Valores de retorno

| Método | Bien | Mal |
|---|---|---|
| `insertar` | id generado | `0` |
| `actualizar` / `eliminar` | `1` | `0` |
| `listar` | lista | `List.of()` |
| `buscarPorId` | `Optional.of(...)` | `Optional.empty()` |

---

## Si en el examen cambian cosas

### Otra entidad (Libro, Autor, Cliente...)
Cambia: `Socio`→`Libro`, `SocioEntity`→`LibroEntity`, `SocioMapper`→`LibroMapper`, `getIdSocio`→`getIdLibro`, la JPQL y los `set` de `actualizar` (uno por cada campo menos el id). Y `validar` con sus campos obligatorios.

### Que lancen excepción en vez de devolver 0
```java
} catch (RuntimeException ex) {
    if (tx.isActive()) tx.rollback();
    throw new DaoException("Error al insertar", ex);   // o new RuntimeException(...)
}
```
```java
public class DaoException extends RuntimeException {
    public DaoException(String msg, Throwable causa) { super(msg, causa); }
}
```

### Que devuelvan `boolean` en vez de `int`
`return true;` / `return false;` en los mismos sitios donde está `1` / `0`.

### Actualizar con `merge` (si lo piden así)
```java
tx.begin();
if (em.find(SocioEntity.class, socio.getIdSocio()) == null) { tx.rollback(); return 0; }
em.merge(SocioMapper.toEntity(socio));   // copia todos los campos sobre la fila
tx.commit();
return 1;
```
⚠️ Sin el `find` previo, `merge` **inserta** si no existe.

### Update / delete con JPQL (sin `find`)
```java
tx.begin();
int filas = em.createQuery("delete from SocioEntity s where s.idSocio = :id")
        .setParameter("id", id)
        .executeUpdate();          // devuelve filas afectadas (0 o 1)
tx.commit();
return filas;
```
```java
int filas = em.createQuery("update SocioEntity s set s.activo = false where s.idSocio = :id")
        .setParameter("id", id)
        .executeUpdate();
```

### Buscar por otro campo (email, nombre...)
```java
public Optional<Socio> buscarPorEmail(String email) {
    try (var em = managerFactory.createEntityManager()) {
        return em.createQuery("select s from SocioEntity s where s.email = :email", SocioEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .map(SocioMapper::toDomain);
    } catch (RuntimeException ex) {
        registrar("buscarPorEmail", ex);
        return Optional.empty();
    }
}
```
No uses `getSingleResult()` para esto: si no hay ninguno, lanza `NoResultException`.

### Lista filtrada
```java
"select s from SocioEntity s where s.activo = true order by s.fechaAlta desc"
"select l from LibroEntity l where lower(l.titulo) like :t"   // .setParameter("t", "%" + txt.toLowerCase() + "%")
```

### Contar
```java
long n = em.createQuery("select count(s) from SocioEntity s", Long.class).getSingleResult();
```

### Sin mapper (devolver la entidad tal cual)
Quita los `.map(SocioMapper::toDomain)` y en `insertar` haz `em.persist(entidad)` directamente con la entidad que te pasan.

### Id que NO es autogenerado (te lo dan ellos)
Quita `entidad.setIdSocio(null)`. Antes de `persist`, comprueba que no existe:
```java
if (em.find(SocioEntity.class, socio.getIdSocio()) != null) { tx.rollback(); return 0; }
```

---

## Errores típicos

- Usar nombres de tabla/columna en JPQL (`from socios`) → error. Va el nombre de la **clase** y los **atributos**.
- Olvidar `tx.begin()` antes de `persist`/`remove` → `TransactionRequiredException`.
- `em.remove(new SocioEntity(...))` → falla, la entidad tiene que venir de `find`.
- Copiar y pegar y dejar el id equivocado (`getIdAutor()` en `LibroDao`) → actualiza o devuelve otra cosa.
- En `actualizar`, olvidar algún `set` → ese campo no se guarda.
- `getSingleResult()` sin resultados → excepción; usa `getResultStream().findFirst()`.

---

## Entity simple

Tabla:
```sql
CREATE TABLE socios (
  id_socio   INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre     TEXT NOT NULL,
  email      TEXT NOT NULL UNIQUE,
  telefono   TEXT,
  fecha_alta TEXT NOT NULL,
  activo     INTEGER NOT NULL DEFAULT 1
);
```

```java
package es.ejemplo.biblioteca.orm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity                          // es una entidad JPA
@Table(name = "socios")          // nombre de la tabla
public class SocioEntity {

    @Id                                                   // clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTOINCREMENT
    @Column(name = "id_socio")                            // nombre de la columna
    private Integer idSocio;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "fecha_alta", nullable = false)
    private String fechaAlta;

    @Column(name = "activo")
    private Boolean activo;

    public SocioEntity() {}      // OBLIGATORIO: constructor vacío

    public Integer getIdSocio() { return idSocio; }
    public void setIdSocio(Integer idSocio) { this.idSocio = idSocio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(String fechaAlta) { this.fechaAlta = fechaAlta; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
```

Si la tabla tiene **clave ajena** (ej. `libros.id_autor`):
```java
@Column(name = "id_autor")
private Integer idAutor;                         // el valor de la FK (se guarda)

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_autor", insertable = false, updatable = false)
private AutorEntity autor;                       // solo para hacer JOIN en JPQL: join l.autor a
```

⚠️ Una entity nueva hay que añadirla en `persistence.xml`:
```xml
<class>es.ejemplo.biblioteca.orm.entity.SocioEntity</class>
```

## Mapper

```java
package es.ejemplo.biblioteca.orm.mapper;

import es.ejemplo.biblioteca.modelo.Socio;
import es.ejemplo.biblioteca.orm.entity.SocioEntity;

public final class SocioMapper {
    private SocioMapper() {}     // clase de utilidad: no se instancia

    // modelo -> entity (para guardar)
    public static SocioEntity toEntity(Socio dato) {
        SocioEntity entidad = new SocioEntity();
        entidad.setIdSocio(dato.getIdSocio());
        entidad.setNombre(dato.getNombre());
        entidad.setEmail(dato.getEmail());
        entidad.setTelefono(dato.getTelefono());
        entidad.setFechaAlta(dato.getFechaAlta());
        entidad.setActivo(dato.getActivo());
        return entidad;
    }

    // entity -> modelo (para devolver)
    public static Socio toDomain(SocioEntity entidad) {
        return new Socio(
                entidad.getIdSocio(),
                entidad.getNombre(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getFechaAlta(),
                entidad.getActivo());
    }
}
```

---

## Consultas SQL

> Todas probadas contra `datos/biblioteca.sqlite3`.

### 1. INNER JOIN — préstamos con socio y libro
```sql
SELECT p.id_prestamo, s.nombre AS socio, l.titulo, p.fecha_prestamo, p.fecha_devolucion
FROM prestamos p
INNER JOIN socios s ON s.id_socio = p.id_socio
INNER JOIN libros l ON l.id_libro = p.id_libro
ORDER BY p.fecha_prestamo;
```

### 2. INNER JOIN — libros con su autor
```sql
SELECT l.titulo, a.nombre AS autor, a.nacionalidad
FROM libros l
INNER JOIN autores a ON a.id_autor = l.id_autor
ORDER BY l.titulo;
```

### 3. LEFT JOIN + COUNT — autores con nº de libros (también los que tienen 0)
```sql
SELECT a.nombre, COUNT(l.id_libro) AS num_libros
FROM autores a
LEFT JOIN libros l ON l.id_autor = a.id_autor
GROUP BY a.id_autor, a.nombre
ORDER BY num_libros DESC, a.nombre;
```
`INNER JOIN` quita a los autores sin libros; `LEFT JOIN` los deja con 0. Se cuenta `COUNT(l.id_libro)`, no `COUNT(*)`, porque si no salen con 1.

### 4. GROUP BY + HAVING — géneros con más de 2 préstamos
```sql
SELECT l.genero, COUNT(p.id_prestamo) AS total_prestamos
FROM prestamos p
INNER JOIN libros l ON l.id_libro = p.id_libro
GROUP BY l.genero
HAVING COUNT(p.id_prestamo) > 2
ORDER BY total_prestamos DESC;
```
`WHERE` filtra filas **antes** de agrupar; `HAVING` filtra grupos **después**.

### 5. Ranking (TOP N) — socios con más préstamos
```sql
SELECT s.nombre, COUNT(p.id_prestamo) AS prestamos
FROM socios s
INNER JOIN prestamos p ON p.id_socio = s.id_socio
GROUP BY s.id_socio, s.nombre
ORDER BY prestamos DESC
LIMIT 3;
```

### 6. Subconsulta con IN — socios que han leído alguna Novela
```sql
SELECT s.nombre, s.email
FROM socios s
WHERE s.id_socio IN (
    SELECT p.id_socio
    FROM prestamos p
    INNER JOIN libros l ON l.id_libro = p.id_libro
    WHERE l.genero = 'Novela'
)
ORDER BY s.nombre;
```

### 7. Subconsulta NOT EXISTS — socios sin ningún préstamo
```sql
SELECT s.nombre
FROM socios s
WHERE NOT EXISTS (
    SELECT 1 FROM prestamos p WHERE p.id_socio = s.id_socio
)
ORDER BY s.nombre;
```

### 8. Subconsulta correlacionada — libros con ejemplares libres
```sql
SELECT l.titulo, l.ejemplares,
       l.ejemplares - (SELECT COUNT(*) FROM prestamos p
                       WHERE p.id_libro = l.id_libro AND p.fecha_devolucion IS NULL) AS libres
FROM libros l
WHERE l.ejemplares > (SELECT COUNT(*) FROM prestamos p
                      WHERE p.id_libro = l.id_libro AND p.fecha_devolucion IS NULL)
ORDER BY l.titulo;
```
"Correlacionada" = la subconsulta usa `l.id_libro` de la consulta de fuera; se ejecuta una vez por cada libro.

### 9. Subconsulta con la media — libros más prestados que la media
```sql
SELECT l.titulo, COUNT(p.id_prestamo) AS prestamos
FROM libros l
LEFT JOIN prestamos p ON p.id_libro = l.id_libro
GROUP BY l.id_libro, l.titulo
HAVING COUNT(p.id_prestamo) > (
    SELECT CAST(COUNT(*) AS REAL) / (SELECT COUNT(*) FROM libros) FROM prestamos
)
ORDER BY prestamos DESC;
```

### 10. Filtro con fechas y NULL — préstamos vencidos
```sql
SELECT s.nombre, l.titulo, p.fecha_limite
FROM prestamos p
INNER JOIN socios s ON s.id_socio = p.id_socio
INNER JOIN libros l ON l.id_libro = p.id_libro
WHERE p.fecha_devolucion IS NULL          -- nunca "= NULL"
  AND p.fecha_limite < '2025-03-01'       -- fechas TEXT 'yyyy-MM-dd' se comparan bien
ORDER BY p.fecha_limite;
```

### Las mismas en JPQL (para el DAO)

| SQL | JPQL |
|---|---|
| `FROM prestamos p` | `from PrestamoEntity p` |
| `INNER JOIN socios s ON s.id_socio = p.id_socio` | `join p.socio s` (con `@ManyToOne`) |
| `LEFT JOIN libros l ON l.id_autor = a.id_autor` | `left join LibroEntity l on l.idAutor = a.idAutor` |
| `p.fecha_devolucion IS NULL` | `p.fechaDevolucion is null` |
| `WHERE x = ?` | `where x = :param` + `.setParameter("param", valor)` |
| `LIMIT 3` | `.setMaxResults(3)` |

```java
// INNER JOIN -> Object[]
List<Object[]> filas = em.createQuery(
        "select p.idPrestamo, s.nombre, l.titulo "
      + "from PrestamoEntity p join p.socio s join p.libro l "
      + "order by p.fechaPrestamo", Object[].class)
    .getResultList();

for (Object[] f : filas) {
    Integer id = (Integer) f[0];
    String socio = (String) f[1];
    String titulo = (String) f[2];
}

// NOT EXISTS
em.createQuery(
        "select s from SocioEntity s where not exists ("
      + "select p from PrestamoEntity p where p.idSocio = s.idSocio)", SocioEntity.class)
    .getResultList();

// GROUP BY + HAVING
em.createQuery(
        "select l.genero, count(p) from PrestamoEntity p join p.libro l "
      + "group by l.genero having count(p) > :min", Object[].class)
    .setParameter("min", 2L)        // count devuelve Long -> pasa un long
    .getResultList();
```
