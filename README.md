# Microkernel (Java)

A small, modular microkernel framework in Java providing a minimal core runtime and plugin/service extension points.

# UML Class Diagram
```mermaid
classDiagram
  direction TB

  namespace modelo {
    class State {
      <<enum>>
      NEW
      READY
      RUNNING
      WAITING
      TERMINATED
    }

    class PCB {
      +int pid
      +String name
      +State state
      +int burstTime
      +int arrivalTime
      +int priority
      +int waitingTime
      +int turnaroundTime
      +PCB(pid, name, burstTime, arrivalTime, priority)
      +setState(State newState) void
      +toString() String
      +getPid() int
      +getName() String
      +getState() State
      +getBurstTime() int
      +getArrivalTime() int
      +getPriority() int
      +getWaitingTime() int
      +getTurnaroundTime() int
    }
  }

  namespace kernel {
    class ProcessManager {
      -List~PCB~ processList
      -int nextPid
      -MemoryManager memoryManager
      +create(name, burst, priority) PCB
      +terminate(pid) void
      +getProcess(pid) PCB
      +getAllProcesses() List~PCB~
    }

    class Scheduler {
      -LinkedList~PCB~ readyQueue
      -PCB runningProcess
      -int currentTick
      -int quantum
      -int quantumCounter
      -List~String~ ganttLog
      -List~PCB~ finishedProcesses
      -ProcessManager processManager
      +run(quantum) void
      +step() void
      +getGanttLog() List~String~
      +getFinishedProcesses() List~PCB~
      +getCurrentTick() int
    }

    class SyncManager {
      -int producers
      -int consumers
      -int bufferSize
      -Semaphore mutex
      -Semaphore full
      -Semaphore empty
      +runUnsynchronized(N, M, K) void
      +runSynchronized(N, M, K) void
    }

    class MemoryManager {
      -int PAGE_SIZE
      -int NUM_FRAMES
      -boolean[] frameMap
      -Map~Integer,PageTable~ pageTables
      -TLB tlb
      +allocate(pid, numPages) boolean
      +free(pid) void
      +translate(pid, logicalAddr) int
      +printStatus() void
    }
  }

  namespace sincronizacion {
    class Semaphore {
      -int value
      -Queue~String~ waitingQueue
      +Semaphore(initialValue)
      +wait(threadName) void
      +signal() void
      +getValue() int
      +getWaitingThreads() String
    }
  }

  namespace memoria {
    class TLB {
      -int size
      -int[][] entries
      -Queue~Integer~ fifoQueue
      -int hits
      -int misses
      +TLB(size)
      +lookup(pid, pageNumber) int
      +insert(pid, pageNumber, frameNumber) void
      +invalidate(pid) void
      +getHitRatio() double
      +printStats() void
    }

    class PageTable {
      +int pid
      -int[] frameMap
      +PageTable(pid, numPages)
      +getFrame(pageNumber) int
      +map(pageNumber, frameNumber) void
      +unmap(pageNumber) void
    }
  }

  namespace presentacion {
    class GanttRenderer {
      -List~String~ ganttLog
      -List~PCB~ finishedProcesses
      -int totalTicks
      +GanttRenderer(ganttLog, finishedProcesses, totalTicks)
      +printGantt() void
      +printMetrics() void
      +printCpuUtilization() void
    }
  }

  class Main {
    -ProcessManager processManager
    -Scheduler scheduler
    -SyncManager syncManager
    -MemoryManager memoryManager
    -GanttRenderer ganttRenderer
    +runCommandLoop() void
  }

  %% ── Relaciones modelo ──
  PCB --> State : usa

  %% ── Relaciones kernel ──
  ProcessManager o-- PCB : gestiona
  ProcessManager --> MemoryManager : usa
  Scheduler o-- PCB : planifica
  Scheduler --> ProcessManager : usa
  Scheduler --> GanttRenderer : delega presentación
  SyncManager *-- Semaphore : contiene

  %% ── Relaciones memoria ──
  MemoryManager  *--  TLB : contiene
  MemoryManager  *--  PageTable : contiene

  %% ── Relaciones Main ──
  Main --> ProcessManager : instancia
  Main --> Scheduler : instancia
  Main --> SyncManager : instancia
  Main --> MemoryManager : instancia
  Main --> GanttRenderer : instancia
```

