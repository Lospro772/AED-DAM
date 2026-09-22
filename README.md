# 🗄️ Acceso a Datos (AD)

Repositorio dedicado a las prácticas, proyectos y ejercicios prácticos del módulo de **Acceso a Datos** (2º DAM). Aquí se documentan las distintas formas de persistencia e integración de datos en aplicaciones Java, desde el manejo directo de ficheros y bases de datos relacionales/NoSQL hasta frameworks ORM modernos.

---

## 🧭 Estructura del Repositorio

| Directorio | Práctica / Tema | Descripción |
| :--- | :--- | :--- |
| `01-maven-setup/` | **Práctica 1: Proyecto Maven desde cero** | Inicialización del entorno, estructura estándar y configuración de `pom.xml`. |
| `02-ficheros/` | *Próximamente* | Lectura/escritura de ficheros secuenciales, binarios, XML y JSON. |
| `03-jdbc/` | *Próximamente* | Conexión a bases de datos relacionales mediante conectores JDBC. |
| `04-orm-hibernate/` | *Próximamente* | Mapeo objeto-relacional y persistencia con JPA / Hibernate. |

---

## 🚀 Práctica Destacada

👉 **Empieza aquí:** [Práctica 01: Construcción y Configuración de un Proyecto Maven desde Cero](./Maven%20y%20construcion%20de%20proyectos%20java/)

### Resumen de la Práctica 1
En esta primera toma de contacto se sienta la base de trabajo para el resto del curso:
* **Generación de la estructura base:** Arquetipo estándar de directorios Maven (`src/main/java`, `src/test/java`, `src/main/resources`).
* **Configuración del `pom.xml`:** Definición de `groupId`, `artifactId`, versión del compilador Java (JDK 21+) y codificación UTF-8.
* **Gestión de dependencias:** Inclusión de librerías esenciales (JUnit 5, conectores, utilidades) y resolución automática del ciclo de vida de compilación y empaquetado (`mvn clean install`).

---

##  Requisitos del Entorno

* **Java Development Kit (JDK):** Versión 21 o superior.
* **Apache Maven:** Versión 3.8+ (o el wrapper `mvnw` integrado).
* **IDE recomendado:** IntelliJ IDEA (con *Extension Pack for Java*).
