# intrale-back-test
# backend

Este proyecto utiliza Ktor y Gradle para construir y ejecutar el servicio.

## Requisitos
- JDK 17 o superior
- Acceso a Internet para descargar las dependencias de Gradle

## Compilar
Para descargar las dependencias y compilar el proyecto ejecute:

```bash
./gradlew build
```

## Ejecutar el servicio
Puede iniciar la aplicación localmente con:

```bash
./gradlew run
```

El servicio quedará escuchando en el puerto configurado por Ktor (Netty).

## Pruebas
Para lanzar las pruebas unitarias ejecute:

```bash
./gradlew test
```

