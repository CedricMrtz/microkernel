package kernel;

import java.util.HashMap;
import java.util.Map;
import models.PageTable;

public class MemoryManager {
    public static final int PAGE_SIZE = 64;
    public static final int NUM_FRAMES = 16;
    public static final int TLB_SIZE = 4;
    private final boolean[] frameMap;
    private final Map<Integer, PageTable> pageTables;
    private final TLB tlb;

    public MemoryManager() {
        this.frameMap = new boolean[NUM_FRAMES];   // todo false = libre
        this.pageTables = new HashMap<>();
        this.tlb = new TLB(TLB_SIZE);
    }

    public boolean allocate(int pid, int numPages) {
        if (countFreeFrames() < numPages) {
            System.out.printf("No hay marcos suficientes para PID=%d " + "(solicitados=%d, disponibles=%d)%n", pid, numPages, countFreeFrames());
            return false;
        }

        PageTable pt = new PageTable(pid, numPages);
        int page = 0;

        for (int frame = 0; frame < NUM_FRAMES && page < numPages; frame++) {
            if (!frameMap[frame]) {
                frameMap[frame] = true;  // marcar como ocupado
                pt.map(page, frame);  // registrar en la tabla de páginas
                page++;
            }
        }

        pageTables.put(pid, pt);
        System.out.printf("PID=%d asignado: %d paginas, %d marcos libres restantes%n", pid, numPages, countFreeFrames());

        return true;
    }

    public void free(int pid) {
        PageTable pt = pageTables.get(pid);

        if (pt == null) {
            System.out.printf(
                "WARN: PID=%d no tiene memoria asignada%n", pid);
            return;
        }

        // Liberar cada marco que tenia asignado el proceso
        int pagesFreed = 0;
        for (int page = 0; page < NUM_FRAMES; page++) {
            int frame = pt.getFrame(page);
            if (frame != -1) {
                frameMap[frame] = false; 
                pt.unmap(page);
                pagesFreed++;
            }
        }

        tlb.invalidate(pid);
        pageTables.remove(pid);

        System.out.printf("PID=%d liberado: %d marcos devueltos, %d libres ahora%n", pid, pagesFreed, countFreeFrames());
    }

    public int translate(int pid, int logicalAddr) {
        PageTable pt = pageTables.get(pid);
        if (pt == null) {
            System.out.printf("PID=%d no tiene memoria asignada%n", pid);
            return -1;
        }

        int pageNumber = logicalAddr / PAGE_SIZE;
        int offset = logicalAddr % PAGE_SIZE;

        int frameNumber = tlb.lookup(pid, pageNumber);
        String tlbResult;

        if (frameNumber != -1) {
            tlbResult = "HIT";
        } else {
            // consultar tabla de paginas
            tlbResult = "MISS";
            frameNumber = pt.getFrame(pageNumber);

            if (frameNumber == -1) {
                System.out.printf("PID=%d pagina %d no esta asignada%n", pid, pageNumber);
                return -1;
            }

            tlb.insert(pid, pageNumber, frameNumber);
        }

        int physicalAddr = (frameNumber * PAGE_SIZE) + offset;

        System.out.printf("PID=%d , Logica=%d , Pagina=%d , Marco=%d , Fisica=%d , TLB: %s%n", pid, logicalAddr, pageNumber, frameNumber, physicalAddr, tlbResult);

        return physicalAddr;
    }

    public void printStatus() {
        System.out.println("Memory Manager Status");

        System.out.println("\nMapa de marcos fisicos");
        System.out.printf("%-8s %-10s %-6s%n", "Marco", "Estado", "PID");

        for (int frame = 0; frame < NUM_FRAMES; frame++) {
            if (!frameMap[frame]) {
                System.out.printf("%-8d %-10s %-6s%n", frame, "LIBRE", "--");
            } else {
                // Buscar que proceso ocupa este marco
                int ownerPid = findFrameOwner(frame);
                System.out.printf("%-8d %-10s %-6d%n", frame, "OCUPADO", ownerPid);
            }
        }

        System.out.println("\nTablas de paginas activas");
        if (pageTables.isEmpty()) {
            System.out.println("(ningun proceso con memoria asignada)");
        } else {
            for (Map.Entry<Integer, PageTable> entry : pageTables.entrySet()) {
                int pid       = entry.getKey();
                PageTable pt  = entry.getValue();
                System.out.printf("  PID=%d to %s%n", pid, pt.toString());
            }
        }
        System.out.println();
        tlb.printStats();

        System.out.printf("Marcos libres : %d / %d%n", countFreeFrames(), NUM_FRAMES);
        System.out.printf("Marcos ocupados : %d / %d%n", NUM_FRAMES - countFreeFrames(), NUM_FRAMES);
        System.out.println();
    }

    private int findFrameOwner(int frame) {
        for (Map.Entry<Integer, PageTable> entry : pageTables.entrySet()) {
            PageTable pt = entry.getValue();
            for (int page = 0; page < NUM_FRAMES; page++) {
                if (pt.getFrame(page) == frame) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    // getters
    public PageTable getPageTable(int pid) {
        return pageTables.get(pid);
    }

    public TLB getTlb() {
        return tlb;
    }

    public int countFreeFrames() {
        int free = 0;
        for (boolean occupied : frameMap) {
            if (!occupied) free++;
        }
        return free;
    }
}
