package algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import models.PCB;
import models.State;

public class RoundRobin {
    public int quantum;
    public LinkedList<PCB> readyQueue;
    private int currentTick;
    private int quantumCounter;
    private PCB running;
    private List<PCB> terminados;
    private List<PCB> pending;

    public RoundRobin(int quantum) {
        this.quantum = quantum;
        this.readyQueue = new LinkedList<>();
        this.currentTick = 0;
        this.quantumCounter = 0;
        this.running = null;
        this.terminados = new ArrayList<>();
        this.pending = new ArrayList<>();
    }

    public void cargarProcesos(List<PCB> processes) {
        this.pending = new ArrayList<>(processes);
    }

    public boolean isDone(){
        return pending.isEmpty() && readyQueue.isEmpty() && running == null;
    }

    public String tick(){
        admitirLlegadas(currentTick);

        if (running != null) {
            boolean quantumAgotado = quantumCounter >= quantum;
            if (quantumAgotado) {
                running.setState(State.READY);
                readyQueue.add(running);
                running = null;
                quantumCounter = 0;
            }
        }

        if (running == null && !readyQueue.isEmpty()) {
            running = readyQueue.poll();
            running.setState(State.RUNNING);
        }

        String cpu = (running != null) ? running.name : "-";

        if (running != null) {
            running.remainingTime--;
            quantumCounter++;

            if (running.remainingTime == 0) {
                running.state = State.TERMINATED;
                running.turnaroundTime = (currentTick + 1) - running.arrivalTime;
                running.waitingTime = running.turnaroundTime - running.burstTime;

                terminados.add(running);
                running = null;
                quantumCounter = 0;
            }
        }

        for (PCB p : readyQueue) {
            p.waitingTime++;
        }

        currentTick++;
        return cpu;
    }

    public List<PCB> getTerminatedProcesses() {
        return terminados;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    private void admitirLlegadas(int tick) {
        var it = pending.iterator();
        while (it.hasNext()) {
            PCB p = it.next();
            if (p.arrivalTime <= tick) {
                p.setState(State.READY);
                readyQueue.add(p);
                it.remove();
            }
        }
    }

    public void removeFromQueues(PCB target) {
        readyQueue.removeIf(p -> p.pid == target.pid);

        if (running != null && running.pid == target.pid) {
            running        = null;
            quantumCounter = 0;
        }

        pending.removeIf(p -> p.pid == target.pid);
    }
}
