package simulation;
import java.util.*;

public class GanttRenderer {

    private final List<String> timeline = new ArrayList<>();

    public void registrarTick(String nombreProceso) {
        if (nombreProceso == null) {
            timeline.add("--");
        } else {
            timeline.add(nombreProceso);
        }
    }

    public void imprimir() {
        System.out.print("t= ");
        for (int i = 0; i < timeline.size(); i++) {
            System.out.printf("%2d ", i);
        }

        System.out.println();
        System.out.print("   ");

        for (String p : timeline) {
            System.out.print(p + " ");
        }

        System.out.println();
    }
}
