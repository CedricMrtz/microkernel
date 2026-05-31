package kernel;

import java.util.List;
import algorithms.RoundRobin;
import simulation.GanttRenderer;
import models.PCB;

public class Scheduler {
    private RoundRobin roundRobin;
    private GanttRenderer ganttRenderer;

    public Scheduler(int quantum) {
        this.roundRobin = new RoundRobin(quantum);
        this.ganttRenderer = new GanttRenderer();
    }

    public void run(List<PCB> processes) {
        roundRobin.cargarProcesos(processes);

        while (!roundRobin.isDone()) {
            String cpu = roundRobin.tick();
            ganttRenderer.registrarTick(cpu);
        }
        ganttRenderer.imprimir();
    }
}
