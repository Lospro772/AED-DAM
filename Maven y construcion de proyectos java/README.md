# Práctica: Introducción a Maven y Construcción de Proyectos Java

## 1. Objetivos de la Práctica
* [Objetivo 1: ej. Comprender el ciclo de vida de Maven]
* [Objetivo 2: ej. Gestionar dependencias externas]
* [Objetivo 3: ej. Empaquetar una aplicación Java]

## 2. Requisitos Previos
* **Java**: 21
* **Maven**: Apache Maven 3.9
* **IDE/Editor**: IntelliJ IDEA
* **Variables de entorno**: Verificar configuración de `JAVA_HOME` y `M2_HOME`.

## 3. Enunciado del Problema / Descripción del Proyecto
En esta practica vamos a aprender a crear un proyecto de java usando maven para la construcción de proyectos

## 4. Pasos para el Desarrollo de la Práctica

### Paso 1: Crear el primer proyecto Maven
Creamos la estructura: 
```gestor-tareas/
├── pom.xml
└── src/main/java/com/codelearn/tareas/Main.java
```
Una vez completamos la estructura, pom y main podemos ejecutar lo siguiente:
```
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ mvn validate
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.codelearn:gestor-tareas >---------------------
[INFO] Building gestor-tareas 1.0.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.100 s
[INFO] Finished at: 2026-09-22T23:46:46+01:00
[INFO] ------------------------------------------------------------------------
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ 
```

### Paso 2: Compilar y entender los archivos generados
* Explicación de la estructura estándar (`src/main/java`, `src/test/java`, etc.).

### Paso 3: Configuración del Archivo `pom.xml`
* Definición de coordenadas del proyecto (`groupId`, `artifactId`, `version`).
* Configuración de la versión del compilador de Java.
* Inclusión de dependencias necesarias [ej. JUnit, Log4j].

