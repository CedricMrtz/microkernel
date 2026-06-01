import java.util.List;
import java.util.Scanner;

import kernel.MemoryManager;
import kernel.ProcessManager;
import kernel.Scheduler;
import kernel.SyncManager;
import models.PCB;
import models.State;

public class Main {
    private static final int STEP_QUANTUM = 2;

    private final Scanner sc = new Scanner(System.in);
    private final MemoryManager memory = new MemoryManager();
    private final ProcessManager processes = new ProcessManager(memory);
    private final SyncManager sync = new SyncManager();

    private Scheduler scheduler;

    public static void main(String[] args) {
        new Main().loop();
    }

    private void loop() {
        System.out.println("Mini-Kernel. Escribe help.");

        while (true) {
            System.out.print("\n> ");

            if (!sc.hasNextLine()) break;

            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] cmd = line.split("\\s+");

            try {
                switch (cmd[0].toLowerCase()) {
                    case "create" -> create(cmd);
                    case "run" -> run(cmd);
                    case "step" -> step(cmd);
                    case "kill" -> kill(cmd);
                    case "translate" -> translate(cmd);
                    case "sync" -> sync();
                    case "status" -> status();
                    case "metrics" -> metrics();
                    case "help" -> help();
                    case "exit" -> {
                        sc.close();
                        return;
                    }
                    default -> System.out.println("Comando no reconocido.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: usa numeros enteros.");
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        sc.close();
    }

    private void create(String[] cmd) {
        args(cmd, 4, "create <nombre> <burst> <prioridad>");

        PCB p = processes.create(cmd[1], positive(cmd[2]), positive(cmd[3]));
        System.out.println("Creado PID=" + p.pid + " Nombre=" + p.name);
    }

    private void run(String[] cmd) {
        args(cmd, 2, "run <quantum>");

        List<PCB> list = activeProcesses();
        if (list.isEmpty()) throw new IllegalStateException("no hay procesos activos.");

        scheduler = new Scheduler(positive(cmd[1]));
        scheduler.run(list);
    }

    private void step(String[] cmd) {
        args(cmd, 1, "step");

        if (scheduler == null || scheduler.isDone()) {
            List<PCB> list = activeProcesses();
            if (list.isEmpty()) throw new IllegalStateException("no hay procesos activos.");

            scheduler = new Scheduler(STEP_QUANTUM);
            scheduler.CargarStep(list);
        }

        String cpu = scheduler.Step();
        System.out.println("CPU: " + (cpu == null ? "--" : cpu));

        if (scheduler.isDone()) {
            System.out.println("Simulacion terminada.");
        }
    }

    private void kill(String[] cmd) {
        args(cmd, 2, "kill <pid>");

        int pid = positive(cmd[1]);
        PCB p = processes.getProcess(pid);

        if (p == null) throw new IllegalArgumentException("PID no encontrado.");

        processes.terminate(pid);

        if (scheduler != null && !scheduler.isDone()) {
            scheduler.Kill(p);
        }

        System.out.println("Proceso terminado: PID=" + pid);
    }

    private void translate(String[] cmd) {
        args(cmd, 3, "translate <pid> <direccion>");

        int pid = positive(cmd[1]);
        int address = nonNegative(cmd[2]);

        if (processes.getProcess(pid) == null) {
            throw new IllegalArgumentException("PID no encontrado.");
        }

        memory.translate(pid, address);
    }

    private void sync() {
        int n = ask("Productores: ");
        int m = ask("Consumidores: ");
        int k = ask("Buffer: ");

        sync.runUnsynchronized(n, m, k);
        sync.runSynchronized(n, m, k);
    }

    private void status() {
        System.out.println("\nProcesos:");
        processes.printProcesses();

        System.out.println("Cola ready:");
        if (scheduler == null || scheduler.getReadyQueue().isEmpty()) {
            System.out.println("(vacia)");
        } else {
            for (PCB p : scheduler.getReadyQueue()) {
                System.out.println("PID=" + p.pid + " Nombre=" + p.name);
            }
        }

        System.out.println("\nMemoria:");
        memory.printStatus();
    }

    private void metrics() {
        if (scheduler == null || !scheduler.isDone()) {
            throw new IllegalStateException("no hay metricas disponibles.");
        }

        List<PCB> done = scheduler.getTerminated();

        System.out.println("\nProceso | Llegada | Rafaga | Espera | Retorno");

        for (PCB p : done) {
            System.out.printf("%-7s | %7d | %6d | %6d | %7d%n",
                p.name,
                p.arrivalTime,
                p.burstTime,
                p.waitingTime,
                p.turnaroundTime
            );
        }

        System.out.printf("%nPromedio espera  : %.2f%n",
            scheduler.getGanttRenderer().getAverageWaitingTime(done));

        System.out.printf("Promedio retorno : %.2f%n",
            scheduler.getGanttRenderer().getAverageTurnaroundTime(done));

        System.out.printf("CPU Utilization  : %.1f%%%n",
            scheduler.getGanttRenderer().getCpuUtilization());
    }

    private void help() {
        System.out.println("""
            create <nombre> <burst> <prioridad>
            run <quantum>
            step
            kill <pid>
            translate <pid> <direccion>
            sync
            status
            metrics
            help
            exit
            """);
    }

    private List<PCB> activeProcesses() {
        return processes.getAllProcesses()
            .stream()
            .filter(p -> p.state != State.TERMINATED)
            .toList();
    }

    private int ask(String text) {
        System.out.print(text);
        return positive(sc.nextLine().trim());
    }

    private int positive(String text) {
        int n = Integer.parseInt(text);
        if (n <= 0) throw new IllegalArgumentException("el valor debe ser mayor que 0.");
        return n;
    }

    private int nonNegative(String text) {
        int n = Integer.parseInt(text);
        if (n < 0) throw new IllegalArgumentException("la direccion no puede ser negativa.");
        return n;
    }

    private void args(String[] cmd, int expected, String usage) {
        if (cmd.length != expected) {
            throw new IllegalArgumentException("uso: " + usage);
        }
    }
}