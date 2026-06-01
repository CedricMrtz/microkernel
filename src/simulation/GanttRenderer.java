package simulation;
import java.util.*;

import models.PCB;

public class GanttRenderer {

    private final List<String> timeline = new ArrayList<>();

    public void registrarTick(String nombreProceso) {
        if (nombreProceso == null) {
            timeline.add("--");
        } else {
            timeline.add(nombreProceso);
        }
    }

    public void imprimir(List<PCB> terminados) {
        imprimirGantt();
        imprimirMetricas(terminados);
    }

    private void imprimirGantt() {
        System.out.println("=== Diagrama de Gantt ===");

        System.out.print("Tick  : ");
        for (int i = 0; i < timeline.size(); i++) {
            System.out.printf("%-3d", i);
        }
        System.out.println();

        System.out.print("CPU   : ");
        for (String entry : timeline) {
            System.out.printf("%-3s", entry);
        }
        System.out.println();
    }

    private void imprimirMetricas(List<PCB> terminados) {
        System.out.println("\n=== Métricas ===");
        System.out.printf("%-10s| %-7s | %-6s | %-6s | %-7s%n",
            "Proceso", "Llegada", "Ráfaga", "Espera", "Retorno");
        System.out.println("-".repeat(48));

        double sumWT = 0, sumTAT = 0;
        for (PCB p : terminados) {
            System.out.printf("%-10s| %7d | %6d | %6d | %7d%n",
                p.name, p.arrivalTime, p.burstTime, p.waitingTime, p.turnaroundTime);
            sumWT  += p.waitingTime;
            sumTAT += p.turnaroundTime;
        }

        int n = terminados.size();
        System.out.println();
        System.out.printf("Promedio espera  : %.2f ticks%n", sumWT  / n);
        System.out.printf("Promedio retorno : %.2f ticks%n", sumTAT / n);
        System.out.printf("CPU Utilization  : %.1f%%%n", getCpuUtilization());
    }

    public double getAverageWaitingTime(List<PCB> terminados) {
        return terminados.stream().mapToInt(p -> p.waitingTime).average().orElse(0);
    }

    public double getAverageTurnaroundTime(List<PCB> terminados) {
        return terminados.stream().mapToInt(p -> p.turnaroundTime).average().orElse(0);
    }

    public double getCpuUtilization() {
        long busy = timeline.stream().filter(t -> !t.equals("-") && !t.equals("--")).count();
        return timeline.isEmpty() ? 0 : (busy * 100.0) / timeline.size();
    }
}
