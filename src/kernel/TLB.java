package kernel;

import java.util.LinkedList;
import java.util.Queue;

public class TLB {
    private static final int COL_PID = 0;
    private static final int COL_PAGE = 1;
    private static final int COL_FRAME = 2;
    private static final int COLS = 3;
    private final int size;
    private final int[][] entries;
    private final Queue<Integer> fifoQueue;
    private int hits;
    private int misses;

    public TLB(int size) {
        this.size = size;
        this.entries = new int[size][COLS];
        this.fifoQueue = new LinkedList<>();
        this.hits = 0;
        this.misses = 0;

        // Inicializar todas las entradas como vacias
        for (int i = 0; i < size; i++) {
            entries[i][COL_PID]   = -1;
            entries[i][COL_PAGE]  = -1;
            entries[i][COL_FRAME] = -1;
        }
    }

    public int lookup(int pid, int pageNumber) {
        for (int i = 0; i < size; i++) {
            if (entries[i][COL_PID]  == pid && entries[i][COL_PAGE] == pageNumber) {
                hits++;
                return entries[i][COL_FRAME];
            }
        }
        misses++;
        return -1;
    }

    public void insert(int pid, int pageNumber, int frameNumber) {
        for (int i = 0; i < size; i++) {
            if (entries[i][COL_PID]  == pid
             && entries[i][COL_PAGE] == pageNumber) {
                entries[i][COL_FRAME] = frameNumber;
                return;
            }
        }

        int targetRow = findEmptyRow();

        if (targetRow == -1) {
            targetRow = fifoQueue.poll();
        }

        // Escribir la nueva entrada
        entries[targetRow][COL_PID]   = pid;
        entries[targetRow][COL_PAGE]  = pageNumber;
        entries[targetRow][COL_FRAME] = frameNumber;

        fifoQueue.add(targetRow);
    }

    public void invalidate(int pid) {
        for (int i = 0; i < size; i++) {
            if (entries[i][COL_PID] == pid) {
                entries[i][COL_PID]   = -1;
                entries[i][COL_PAGE]  = -1;
                entries[i][COL_FRAME] = -1;
                fifoQueue.remove(i);
            }
        }
    }

    
    private int findEmptyRow() {
        for (int i = 0; i < size; i++) {
            if (entries[i][COL_PID] == -1) {
                return i;
            }
        }
        return -1;
    }

    public void printStats() {
        System.out.println("TLB Status");
        System.out.printf("%-6s %-8s %-8s %-8s%n", "Fila", "PID", "Página", "Marco");

        for (int i = 0; i < size; i++) {
            if (entries[i][COL_PID] == -1) {
                System.out.printf("%-6d %-8s %-8s %-8s%n", i, "---", "---", "---");
            } else {
                System.out.printf("%-6d %-8d %-8d %-8d%n",
                    i,
                    entries[i][COL_PID],
                    entries[i][COL_PAGE],
                    entries[i][COL_FRAME]);
            }
        }

        System.out.printf("Hits : %d%n", hits);
        System.out.printf("Misses : %d%n", misses);
        System.out.printf("Hit ratio: %.1f%%%n", getHitRatio() * 100);
        System.out.println();
    }

    // getters
    public int getSize() { 
        return size;
    }

    public int getHits() { 
        return hits;
    }
    
    public int getMisses() { 
        return misses; 
    }
    
    public double getHitRatio() {
        int total = hits + misses;
        return (total == 0) ? 0.0 : (double) hits / total;
    }


}
