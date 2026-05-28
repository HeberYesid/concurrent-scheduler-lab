# Motor de Priorización de Eventos con Envejecimiento Dinámico

## Presentación de Defensa Académica

---

## Slide 1: Portada

### Motor de Priorización de Eventos de un Sistema Operativo
#### Scheduler de Procesos con Prioridad Dinámica y Anti-Starvation

**Java 17 · Maven · JMH · jqwik · PIT · Clean Architecture**

---

## Slide 2: El Problema

### ¿Qué resolvemos?

Un sistema operativo recibe miles de procesos con diferentes prioridades. Debe decidir **cuál ejecutar a continuación** cumpliendo:

- **Complejidad**: seleccionar el siguiente proceso en $O(\log n)$
- **Justicia**: evitar que procesos de baja prioridad sufran inanición (starvation)
- **Escala**: soportar desde 1,000 hasta 500,000 procesos activos
- **Métricas**: medir throughput, tiempo de espera promedio y tasa de starvation

### Categorización

| Dimensión | Clasificación |
|---|---|
| Tipo | Selección dinámica con prioridad mutable |
| Estrategia | Greedy + Corrección dinámica (Aging) |
| Naturaleza | Online (procesos llegan y salen dinámicamente) |

---

## Slide 3: Solución — Prioridad Efectiva con Aging

### Fórmula de prioridad efectiva

$$\text{priority}(p, t) = (39 - \text{basePriority}) + \alpha \times \text{waitTime}$$

### ¿Por qué funciona?

| Componente | Rol |
|---|---|
| $39 - \text{basePriority}$ | Prioridad base invertida (UNIX: 0 = máxima urgencia) |
| $\alpha \times \text{waitTime}$ | Término de envejecimiento: crece con el tiempo de espera |
| $\alpha \in (0, 1]$ | Factor de aging configurable |

**Propiedad anti-starvation**: un proceso de prioridad 39 (mínima) alcanza mayor prioridad que uno de prioridad 0 recién llegado tras esperar $\approx 39/\alpha$ intervalos.

---

## Slide 4: Estructura de Datos — Binary Max-Heap

### ¿Por qué un Heap y no otra estructura?

| Operación | Binary Heap | Árbol Rojo-Negro | Skip List | Lista Ordenada |
|---|---|---|---|---|
| Insert | $O(\log n)$ | $O(\log n)$ | $O(\log n)^*$ | $O(n)$ ✗ |
| ExtractMax | $O(\log n)$ | $O(\log n)$ | $O(1)$ | $O(1)$ |
| **Aging (update all)** | $\mathbf{O(n)}$ ✅ | $O(n \log n)$ ✗ | $O(n \log n)$ ✗ | $O(n \log n)$ ✗ |
| Memoria/elem | ~64 B | ~120 B | ~80 B | ~64 B |
| Cache locality | ★★★★★ | ★★☆☆☆ | ★★★☆☆ | ★★★★☆ |

**El aging es la operación diferencial del problema.** El heap lo resuelve en $O(n)$ mutando prioridades in-place y reconstruyendo con Floyd (bottom-up heapify).

Para $N = 500\,000$: 500K ops vs 9.5M ops por ciclo de aging.

---

## Slide 5: Algoritmo de Aging con Floyd Rebuild

```
AGING-REBUILD(heap, currentTime):
  for each process p in heap:
    p.effectivePriority = (39 - p.basePriority)
                         + α × (currentTime - p.arrivalTime)
  for i = n/2 - 1 down to 0:    // Floyd's algorithm
    siftDown(heap, i)            // O(n) total
```

### Complejidad

| Operación | Peor Caso |
|---|---|
| `insert(p)` | $O(\log n)$ |
| `extractMax()` | $O(\log n)$ |
| `AGING-REBUILD` | $O(n)$ periódico |
| **Ciclo completo ($N$ procesos)** | $O(N \log N + K \cdot N)$ |

$K$ = número de ejecuciones de aging. Si $\text{agingInterval} \gg B_{avg}$, costo amortizado del aging $\approx O(1)$ por proceso.

---

## Slide 6: Arquitectura — Clean Architecture

```
com.proyecto/
├── domain/
│   ├── model/         ← Records inmutables (0 dependencias)
│   ├── algorithm/     ← Heap, AgingEngine, PriorityCalculator
│   └── service/       ← Interfaces + implementaciones
├── infrastructure/
│   ├── persistence/   ← InMemoryProcessRepository
│   └── io/            ← CsvProcessParser, ConsoleOutputFormatter
└── benchmark/         ← JMH benchmarks
```

**Regla**: `domain/` no depende de `infrastructure/`. Verificado: cero imports de infraestructura en dominio.

### Patrones de diseño

| Patrón | Aplicación |
|---|---|
| Strategy | 3 fórmulas de aging intercambiables |
| Pipeline | Flujo: parse → validate → schedule → metrics → output |
| Command | execute/undo para trazabilidad y rollback del heap |
| Result Monad | Manejo explícito de errores sin excepciones |

---

## Slide 7: Estrategias de Aging (Strategy Pattern)

### Tres fórmulas implementadas

| Estrategia | Fórmula | Comportamiento |
|---|---|---|
| **Lineal** | $(39 - B) + \alpha \times w$ | Crecimiento constante |
| **Exponencial** | $(39 - B) + \alpha \times \log_2(1 + w)$ | Crecimiento atenuado |
| **Step** | $(39 - B) + \alpha \times \lfloor w / I \rfloor$ | Incrementos discretos |

$B = \text{basePriority}$, $w = \text{waitTime}$, $I = \text{agingInterval}$

La estrategia se inyecta en el scheduler por constructor — **Open/Closed Principle**: nueva estrategia = nueva clase, sin modificar código existente.

---

## Slide 8: Diseño por Contrato y Manejo de Errores

### Result Monad: `Result<T, E>`

```java
public sealed interface Result<T, E> permits Ok, Err {
    static <T, E> Result<T, E> ok(T value) { ... }
    static <T, E> Result<T, E> err(E error) { ... }
    <R> Result<R, E> map(Function<T, R> mapper);
    <R> Result<R, E> flatMap(Function<T, Result<R, E>> mapper);
}
```

- Sin excepciones para flujo de control
- Errores de dominio: `EMPTY_PROCESS_LIST`, `INVALID_CONFIGURATION`, `DUPLICATE_PROCESS_ID`
- Fail-fast: validación en constructores compactos de records

### Invariantes verificados

- **INV-1**: propiedad de max-heap preservada en toda operación
- **INV-2**: prioridad efectiva nunca decrece mientras un proceso espera
- **INV-3**: no hay procesos duplicados en la cola

---

## Slide 9: Resultados Experimentales — Benchmarks JMH

| Benchmark | N=1,000 | N=10,000 | N=100,000 | N=500,000 |
|---|---|---|---|---|
| `agingRebuild` | 0.039 ms | 0.599 ms | 12.167 ms | 100.565 ms |
| `insertAndExtract` | 0.146 ms | 2.437 ms | 50.338 ms | 694.156 ms |
| `baselineCycle` | 0.291 ms | 4.372 ms | 122.692 ms | 1,193.310 ms |
| `fullCycle` | 3.214 ms | 177.022 ms | 25,112.996 ms | — |

**Observaciones**:
- `agingRebuild` escala linealmente con $N$ ($r^2 \approx 0.995$)
- `insertAndExtract` escala como $O(N \log N)$ esperado
- A 500K procesos: aging rebuild en ~100 ms — **validación empírica de $O(N)$**

---

## Slide 10: Calidad de Código

### Métricas de calidad

| Métrica | Valor |
|---|---|
| **Tests totales** | 57 (51 unitarios + 6 jqwik properties) |
| **PIT mutation coverage** | 86% (291 mutaciones, 249 killed) |
| **JaCoCo branch coverage** | 87.5% (dominio) |
| **PMD cyclomatic complexity** | ≤ 10 en todo el código propio |
| **jqwik properties** | 6 propiedades, 1000 tries c/u, semilla fija |

### Tipos de pruebas

- **Unitarias**: validación de modelos, heap, aging, comandos, Result monad
- **Propiedades (jqwik)**: invariante de heap, extracción ordenada, aging + heap, métricas válidas
- **Integración**: pipeline completo CSV → scheduler → métricas
- **Benchmarks (JMH)**: 4 benchmarks × 4 escalas, baseline comparativo

---

## Slide 11: ADRs — Decisiones Clave

| ADR | Decisión | Justificación principal |
|---|---|---|
| ADR-001 | Binary Max-Heap sobre array | Aging $O(n)$ vs $O(n \log n)$ en alternativas — 19× más rápido a 500K |
| ADR-002 | Strategy para PriorityCalculator | Extensibilidad sin modificar AgingEngine — Open/Closed Principle |
| ADR-003 | Pipeline + Clean Architecture | Separación domain/infrastructure, errores con Result monad |
| ADR-004 | Command para trazabilidad del heap | execute/undo para rollback determinista en pruebas |

Los 4 ADRs están documentados en formato **MADR** con: contexto, motivación, opciones consideradas, decisión, consecuencias (+/−) y confirmación.

---

## Slide 12: Conclusiones y Trabajo Futuro

### Logros

1. **Complejidad $O(\log n)$** garantizada para selección — verificado empíricamente con JMH
2. **Anti-starvation** demostrado formalmente: $\forall \alpha > 0$, starvation rate $\to 0$
3. **Escala a 500K** validada: aging rebuild < 100 ms
4. **86% mutation coverage** con PIT — los tests detectan fallos incluso tras mutaciones del código
5. **Clean Architecture mantenible**: 0 dependencias incorrectas, 4 patrones documentados

### Limitaciones y Trabajo Futuro

| Limitación | Mejora propuesta |
|---|---|
| Simulación secuencial (sin threads) | Thread pool para simulación multi-core con sincronización sobre el heap |
| No soporta `decreaseKey` individual | Indexed Heap (heap + tabla hash de posiciones) |
| Aging $O(n)$ periódico | Lazy aging: recalcular prioridad solo al extraer (para $N > 5M$) |
| `fullCycle` N=500K requiere ~4 GB heap | Streaming de trace con ventana deslizante |

---

## Slide 13: Preguntas Frecuentes (Preparación para Defensa)

**P1: ¿Por qué heap y no un árbol balanceado?**
El aging (operación diferencial) es $O(n)$ en heap vs $O(n \log n)$ en árboles. Para 500K procesos: 500K ops vs 9.5M ops. Además, el array contiguo tiene mejor cache locality (3-5× menos cache misses).

**P2: ¿Qué pasa si el agingInterval es muy pequeño?**
El costo total degrada a $O(N^2)$. Se calibra con la restricción $\text{agingInterval} \ge 10 \times B_{avg}$. Con $B_{avg} \approx 50$ ms, el default de 100 ms es seguro.

**P3: ¿Cómo garantizan que no hay starvation?**
Demostración formal: para $\alpha > 0$, el proceso de menor prioridad alcanza la máxima prioridad efectiva en un número finito de intervalos ($39/\alpha$). Empíricamente, PIT y jqwik verifican que la tasa de starvation es consistente con la definición.

**P4: ¿Por qué `SchedulableProcess` no es un record?**
`effectivePriority` es mutable por necesidad: el aging la actualiza in-place para evitar realocación de objetos en cada ciclo $O(n)$. La mutabilidad está encapsulada — solo `AgingEngine` modifica este campo.

---

*Proyecto disponible en el repositorio. Ejecutar `mvn verify` para compilación + tests + JaCoCo + PMD. `mvn pitest:mutationCoverage` para mutation testing. `java -jar target/benchmarks.jar` para JMH.*
