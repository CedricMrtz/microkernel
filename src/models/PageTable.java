package models;

public class PageTable {
    public final int pid;
    private final int[] frameMap; // frameMap[página] = marco (-1 si no asignado)

    public PageTable(int pid, int numPages) { 
        this.pid = pid;
        this.frameMap = new int[numPages];
        for (int i = 0; i < numPages; i++) {
            frameMap[i] = -1; // -1 indica que la página no está asignada
        }
    }

    public int getFrame(int pageNumber) { 
        isValidPage(pageNumber);
        return frameMap[pageNumber];
    }


    public void map(int pageNumber, int frameNumber) { 
        isValidPage(pageNumber);

        if (frameNumber < 0) {
            throw new IllegalArgumentException("Número de marco inválido");
        }
        frameMap[pageNumber] = frameNumber;
    }

    public void unmap(int pageNumber) { 
        isValidPage(pageNumber);
        frameMap[pageNumber] = -1;
    }

    private void isValidPage(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= frameMap.length) {
            throw new IllegalArgumentException("Número de página fuera de rango");
        }
    }

    public int getNumPages() {
        return frameMap.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PageTable(pid=").append(pid).append(", frameMap=[");
        for (int i = 0; i < frameMap.length; i++) {
            sb.append(frameMap[i]);
            if (i < frameMap.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("])");
        return sb.toString();
    }
}
