package algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import models.PCB;
import models.State;

public class RoundRobin {
    public int quantum;
    public LinkedList<PCB> readyQueue;

    public RoundRobin(int quantum) {
        this.quantum      = quantum;
        this.readyQueue   = new LinkedList<>();
    }

    public List<PCB> run(List<PCB> processes) {
        int tick = 0;
        int quantumCounter = 0;
        PCB running = null;
        List<PCB> terminados = new ArrayList<>();

        List<PCB> pending = new ArrayList<>(processes);

        System.out.println(quantum);

        while (!pending.isEmpty() || !readyQueue.isEmpty() || running != null) {
            admitirLlegadas(pending, tick);

            if (running != null) {
                boolean quantumAgotado = quantumCounter >= quantum;
                boolean termino = running.remainingTime == 0;

                if (termino) {
                    running.state = State.TERMINATED;
                    running.turnaroundTime = tick - running.arrivalTime;
                    running.waitingTime = running.turnaroundTime - running.burstTime;

                    terminados.add(running);
                    running = null;
                    quantumCounter = 0;

                } else if (quantumAgotado) {
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

            if (running != null) {
                running.remainingTime--;
                quantumCounter++;
            }

            for (PCB p : readyQueue) {
                p.waitingTime++;
            }

            tick++;
        }

        return terminados;
    }

    private void admitirLlegadas(List<PCB> pending, int tick) {
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
}
