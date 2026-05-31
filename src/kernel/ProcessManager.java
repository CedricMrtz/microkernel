package kernel;

import models.PCB;
import models.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessManager {
    
    private static final int maxProcesses = 10;
    private static int defaultProcessPages = 3;

    private final List<PCB> processList;
    private final MemoryManager memoryManager;
    private int nextPid;

    public ProcessManager(){
        this(new MemoryManager());
    }

    public ProcessManager(MemoryManager memoryManager) { 
        if(memoryManager == null){
            throw new IllegalArgumentException("MemoryManager no puede ser null");
        }
        this.processList = new ArrayList<>();
        this.memoryManager = memoryManager;
        this.nextPid = 1;
    }

    public PCB create(String name, int burst, int priority){
        return create(name, burst, priority, defaultProcessPages);
    }
    

    //metodo para crear un nuevo proceso
    public PCB create(String name, int burst, int priority, int numPages) {
        validateCreateData(name, burst, priority, numPages);

        if(countActiveProcesses() >= maxProcesses) {
            throw new IllegalStateException("No se pueden crear mas de 10 procesos activos.");
        }
        int pid = nextPid;
        if (!memoryManager.allocate(pid, numPages)) {
            throw new IllegalStateException("No hay suficiente memoria para asignar al proceso " + pid);
        }
        PCB process = new PCB(pid, name, burst, 0, priority);
        processList.add(process);
        nextPid++;
        
        return process;
    }

    public void terminate(int pid){
        PCB process = getProcess(pid);

        if(process == null){
            throw new IllegalArgumentException("Proceso con PID " + pid + " no encontrado.");
        }
        if(process.getState() == State.TERMINATED){
            throw new IllegalStateException("El proceso " + pid + " ya esta terminado.");
        }
        memoryManager.free(pid);         //liberar memoria asignada al proceso
        process.state = State.TERMINATED;
        process.remainingTime = 0; 
    }

    //metodo para obtener un proceso por su PID
    public PCB getProcess(int pid){
        for (PCB process : processList){
            if(process.getPid() == pid){
                return process;
            }
        }
        return null;
    }

    public List<PCB> getAllProcesses(){
        return Collections.unmodifiableList(processList);
    }

    public void printProcesses(){
        if(processList.isEmpty()){
            System.out.println("No hay procesos registrados.");
            return;
        }
        for(PCB process : processList){
            System.out.println(process);
            System.out.println();
        }
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
    
    private void validateCreateData(String name, int burst, int priority, int numPages) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del proceso no puede estar vacio.");
        }

        if (burst <= 0) {
            throw new IllegalArgumentException("La rafaga debe ser mayor que 0.");
        }

        if (priority <= 0) {
            throw new IllegalArgumentException("La prioridad debe ser mayor que 0.");
        }

        if (numPages <= 0) {
            throw new IllegalArgumentException("El numero de paginas debe ser mayor que 0.");
        }
    }
}