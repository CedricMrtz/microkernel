package kernel;

import java.util.ArrayList;
import java.util.List;
import models.PCB;
import models.State;

public class ProcessManager {
    
    private static final int max_processes = 10;

    private final List<PCB> processList;
    private int nextPid;

    public ProcessManager() {
        this.processList = new ArrayList<>();
        this.nextPid = 1;
    }

    //metodo para crear un nuevo proceso
    public PCB create(String name, int burst, int priority) {
        if (countActiveProcesses() >= max_processes) {
            throw new IllegalStateException("No se pueden crear mas de 10 procesos activos.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del proceso no puede estar vacíi.");
        }

        if (burst <= 0) {
            throw new IllegalArgumentException("La rafaga debe ser mayor que 0.");
        }

        if (priority <= 0) {
            throw new IllegalArgumentException("La prioridad debe ser mayor que 0.");
        }

        int pid = nextPid;
        nextPid++;

        int arrivalTime = 0;

        PCB process = new PCB(pid, name, burst, arrivalTime, priority);
        process.setState(State.READY);

        processList.add(process);

        return process;
    }

    //metodo para eliminar un proceso
    private int countActiveProcesses() {
        int activeProcesses = 0;

        for (PCB process : processList) {
            if (process.getState() != State.TERMINATED) {
                activeProcesses++;
            }
        }

        return activeProcesses;
    }
}
