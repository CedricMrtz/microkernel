package models;

public class PCB {
    public final int    pid;
    public final String name;
    public State        state;
    public int          burstTime;       // ráfaga de CPU restante (ticks)
    public final int    arrivalTime;     // tick de llegada
    public final int    priority;        // 1 = más alta
    public int          waitingTime;     // acumulado en estado READY
    public int          turnaroundTime;  // calculado al terminar
    public int          remainingTime;   // tick de inicio de ejecución

    public PCB(int pid, String name, int burstTime, int arrivalTime, int priority) { 
        this.pid = pid;
        this.name = name;
        this.state = State.NEW;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
     }

    public void setState(State newState) { 
        if (this.state == State.TERMINATED) {
            throw new IllegalStateException("El proceso " + pid + " ya termino");
        }

        // Checa si la transicion es valida segun el estado actual
        boolean valido = 
        switch (this.state) {
            case NEW -> newState == State.READY;
            case READY -> newState == State.RUNNING;
            case RUNNING -> newState == State.READY ||
                             newState == State.WAITING ||
                             newState == State.TERMINATED;
            case WAITING -> newState == State.READY;
            default -> false;
        };

        if (!valido) {
            throw new IllegalStateException(
                "Transicion invalida de " + this.state + " a " + newState
            );
         }
        this.state = newState;
     }

    @Override
    public String toString() {
        return String.format(
            "PID: %d,\n Nombre: %s,\n Estado: %s,\n Prioridad: %d,\n Llegada: %d,\n Rafaga: %d,\n Restante: %d,\n Espera: %d,\n Fin: %d",
            pid, name, state, priority, arrivalTime, burstTime, remainingTime, waitingTime, turnaroundTime
        );
    }
}
