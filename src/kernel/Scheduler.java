package kernel;

import java.util.List;
import java.util.LinkedList;
import algorithms.RoundRobin;
import simulation.GanttRenderer;
import models.PCB;
import models.State;

public class Scheduler {
    private RoundRobin roundRobin;
    private GanttRenderer ganttRenderer;

    public Scheduler(int quantum) {
        this.roundRobin = new RoundRobin(quantum);
        this.ganttRenderer = new GanttRenderer();
    }

    public void run(List<PCB> processes) {
        List<PCB> activos = processes.stream().filter(p -> p.state != State.TERMINATED).toList();

        roundRobin.cargarProcesos(activos);

        while (!roundRobin.isDone()) {
            String cpu = roundRobin.tick();
            ganttRenderer.registrarTick(cpu);
        }
        ganttRenderer.imprimir(roundRobin.getTerminatedProcesses());
    }

    public void CargarStep(List<PCB> processes) {
        List<PCB> activos = processes.stream().filter(p -> p.state != State.TERMINATED).toList();
        roundRobin.cargarProcesos(activos);
    }

    public String Step() {
        if(roundRobin.isDone()) {
            return null;
        }
        String cpu = roundRobin.tick();
        ganttRenderer.registrarTick(cpu);
        return cpu;
    }

    public boolean isDone() {
        return roundRobin.isDone();
    }

    // Expose
    public LinkedList<PCB> getReadyQueue() {
        return roundRobin.readyQueue;
    }

    public List<PCB> getTerminated() {
        return roundRobin.getTerminatedProcesses();
    }

    public GanttRenderer getGanttRenderer() {
        return ganttRenderer;
    }

    public void Kill(PCB process) {
        roundRobin.removeFromQueues(process);
        process.state = State.TERMINATED;
    }
}
