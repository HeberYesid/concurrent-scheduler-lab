# Scheduler Concurrente

Proyecto Maven en Java 17 para simular y evaluar un scheduler de procesos con prioridad dinamica y aging. El sistema usa un binary max-heap para seleccionar el siguiente proceso en O(log n) y busca evitar starvation mediante distintas estrategias de envejecimiento.

## Caracteristicas

- Scheduling basado en prioridad efectiva.
- Estrategias de aging lineal, exponencial y por pasos.
- Modelos de dominio inmutables para procesos, metricas y resultados.
- Repositorio en memoria e IO por consola.
- Pruebas unitarias, de propiedad, integracion y benchmark con JMH.

## Requisitos

- Java 17
- Maven 3.8+

## Estructura

- `src/main/java/com/proyecto/Main.java`: punto de entrada de la simulacion.
- `src/main/java/com/proyecto/domain`: reglas de negocio, modelos y algoritmos.
- `src/main/java/com/proyecto/infrastructure`: IO y persistencia en memoria.
- `src/main/java/com/proyecto/benchmark/SchedulerBenchmark.java`: benchmarks JMH.
- `src/test/java`: pruebas automatizadas.
- `docs/adr`: decisiones de arquitectura.
- `analisis_scheduler_concurrente.md`: analisis formal del problema y de la solucion.

## Como ejecutar

### Compilar y probar

```bash
mvn clean test
```

### Empaquetar

```bash
mvn clean package
```

### Ejecutar la simulacion principal

La clase principal es `com.proyecto.Main`.

```bash
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.proyecto.Main
```

## Benchmarks

El benchmark principal esta en `com.proyecto.benchmark.SchedulerBenchmark`. Evalua escalas de 1.000 a 500.000 procesos y compara el scheduler con un baseline de cola de prioridad estatica.

## Pruebas

El proyecto incluye:

- pruebas de dominio y validacion de modelos,
- pruebas del heap y de las estrategias de aging,
- pruebas de integracion del scheduler,
- pruebas de propiedad con jqwik,
- pruebas de benchmark asociadas a JMH.

## Documentacion adicional

- `docs/adr/ADR-001.md`
- `docs/adr/ADR-002.md`
- `docs/adr/ADR-003.md`
- `docs/adr/ADR-004.md`
- `analisis_scheduler_concurrente.md`

## Nota

Si ejecutas `mvn exec:java` por primera vez, Maven puede descargar el plugin correspondiente automaticamente. Si prefieres, tambien puedes abrir el proyecto en tu IDE y ejecutar `com.proyecto.Main` directamente.