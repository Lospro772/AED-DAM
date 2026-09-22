# Práctica: Introducción a Maven y Construcción de Proyectos Java

## 1. Objetivos de la Práctica
* Aprender como se usa Maven y como se construyen proyectos.

## 2. Requisitos Previos
* **Java**: 21
* **Maven**: Apache Maven 3.9
* **IDE/Editor**: IntelliJ IDEA
* **Variables de entorno**: Verificar configuración de `JAVA_HOME` y `M2_HOME`.

## 3. Enunciado del Problema / Descripción del Proyecto
En esta practica vamos a aprender a crear un proyecto de java usando maven para la construcción de proyectos

## 4. Pasos para el Desarrollo de la Práctica

### Paso 03: Crear el primer proyecto Maven
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

### Paso 04: Compilar y entender los archivos generados
* Una ves descargamos ejecutamos el `mvn compile` nos descargara todo lo que ocupe y sabremos si esta bien si sale lo siguiente:
```
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 1 source file with javac [debug release 21] to target/classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.576 s
[INFO] Finished at: 2026-09-23T00:10:21+01:00
[INFO] ------------------------------------------------------------------------
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ 
```
* por ultimo podemos hacer:

```
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ java -cp target/classes com.codelearn.tareas.Main
Gestor de tareas preparado
```

### Paso 05: Ciclos de vida, fases y goals

#### Fase 1. Identificar el usuario y el entorno

* Lo primero que haremos sera ejecutar lo siguiente: 

```
whoami
java -version
javac -version
mvn -version
echo "$JAVA_HOME"
```
* Qué nos mostrara lo siguiente usando java 21:
```
yasiel
openjdk version "21.0.12.1" 2026-08-18
OpenJDK Runtime Environment (build 21.0.12.1+1-1-deb13u1-Debian)
OpenJDK 64-Bit Server VM (build 21.0.12.1+1-1-deb13u1-Debian, mixed mode, sharing)
javac 21.0.12.1
Apache Maven 3.9.9
Maven home: /usr/share/maven
Java version: 21.0.12.1, vendor: Debian, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: es_ES, platform encoding: UTF-8
OS name: "linux", version: "6.12.107+deb13-amd64", arch: "amd64", family: "unix"
/usr/lib/jvm/java-1.21.0-openjdk-amd64
```
* También podemos verlo con java 17:
```
openjdk version "17.0.14" 2026-01-20
OpenJDK Runtime Environment (build 17.0.14+7)
Eclipse Temurin JDK (build 17.0.14+7, mixed mode, sharing)
javac 17.0.14
Apache Maven 3.9.9
Maven home: /usr/share/maven
Java version: 17.0.14, vendor: Eclipse Adoptium, runtime: /usr/lib/jvm/java-17-temurin-amd64
Default locale: es_ES, platform encoding: UTF-8
OS name: "linux", version: "6.12.107+deb13-amd64", arch: "amd64", family: "unix"
/usr/lib/jvm/java-1.17.0-temurin-amd64
```

* Pôr ultimo podemos ver si todo funciono ccon java 21:
```
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ cd ~/gestor-tareas
mvn clean verify
bash: cd: /home/yasiel/gestor-tareas: No existe el fichero o el directorio
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.codelearn:gestor-tareas >---------------------
[INFO] Building gestor-tareas 1.0.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ gestor-tareas ---
[INFO] Deleting /home/yasiel/Escritorio/AED/gestor-tareas/target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ gestor-tareas ---
[INFO] Copying 0 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ gestor-tareas ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 1 source file with javac [debug release 21] to target/classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ gestor-tareas ---
[INFO] skip non existing resourceDirectory /home/yasiel/Escritorio/AED/gestor-tareas/src/test/resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ gestor-tareas ---
[INFO] Recompiling the module because of changed dependency.
[INFO] 
[INFO] --- surefire:3.5.2:test (default-test) @ gestor-tareas ---
[INFO] 
[INFO] --- jar:3.4.2:jar (default-jar) @ gestor-tareas ---
[INFO] Building jar: /home/yasiel/Escritorio/AED/gestor-tareas/target/gestor-tareas-1.0.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.072 s
[INFO] Finished at: 2026-09-23T00:34:12+01:00
[INFO] ------------------------------------------------------------------------
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ 
```

### Paso 06: Añadir y utilizar una dependencia
* En este paso aprenderemos a descargar una dependencia nueva y utilizarla en nuestro `main.java` a continuación vemos como todo funciono:

```
[INFO] com.codelearn:gestor-tareas:jar:1.0.0-SNAPSHOT
[INFO] \- com.google.code.gson:gson:jar:2.11.0:compile
[INFO]    \- com.google.errorprone:error_prone_annotations:jar:2.27.0:compile
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  8.176 s
[INFO] Finished at: 2026-09-23T00:39:18+01:00
[INFO] ------------------------------------------------------------------------
yasiel@debian-des:~/Escritorio/AED/gestor-tareas$ 
```
* NOTA: Descaga muchos paquetes y el mensaje era muy largo, pero si termina así todo estara correcto.

### Paso 07: Añadir y utilizar una dependencia
* En este caso podemos localizar dentro del POM.xml el gson que sería:

```
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>
```

* Por otro lado es importante saber que al ejecutar `mvn install` no publica el proyecto para otros personas debido a que cuando ejecutas el comando, maven hace un Aislamiento local, por tanto nadie de fuera de nuestra maquina podra usarlo.
