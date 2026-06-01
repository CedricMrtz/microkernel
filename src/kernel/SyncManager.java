package kernel;

import models.Semaphore;

public class SyncManager {

    private int tickCounter = 0;

    private synchronized int getNextTick() {
        return ++tickCounter;
    }

    public void runUnsynchronized(int N, int M, int K) {
        System.out.println("\nModo no sincronizado ");
        int[] sharedCounter = new int[1];
        sharedCounter[0] = 0;

        Runnable badProducer = () -> {
            for (int i = 0; i < 5; i++) {
                int temp = sharedCounter[0];
                try {
                    Thread.sleep(18);
                } catch (InterruptedException e) {
                }

                sharedCounter[0] = temp + 1;
            }
        };

        Runnable badConsumer = () -> {
            for (int i = 0; i < 5; i++) {
                int temp = sharedCounter[0];
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                sharedCounter[0] = temp - 1;
            }
        };

        Thread[] prodThreads = new Thread[N];
        Thread[] consThreads = new Thread[M];

        for (int i = 0; i < N; i++) {
            prodThreads[i] = new Thread(badProducer);
            prodThreads[i].start();
        }
        for (int i = 0; i < M; i++) {
            consThreads[i] = new Thread(badConsumer);
            consThreads[i].start();
        }

        try {
            for (Thread t : prodThreads)
                t.join();
            for (Thread t : consThreads)
                t.join();
        } catch (InterruptedException e) {
        }

        int expected = (N * 5) - (M * 5);
        System.out.println(
                "Valor final del contador (race condition): " + sharedCounter[0] + " (esperado: " + expected + ")");
    }

    public void runSynchronized(int N, int M, int K) {
        System.out.println("\nModo sincronizado ");

        int[] buffer = new int[K];
        int[] inOut = { 0, 0 };
        int[] count = { 0 };
        int[] itemCounter = { 1 };

        Semaphore mutex = new Semaphore(1);
        Semaphore full = new Semaphore(0);
        Semaphore empty = new Semaphore(K);

        Runnable producer = () -> {
            String name = Thread.currentThread().getName();
            try {
                while (true) {
                    empty.wait(name);
                    mutex.wait(name);

                    int item = itemCounter[0]++;
                    buffer[inOut[0]] = item;
                    inOut[0] = (inOut[0] + 1) % K;
                    count[0]++;

                    int tick = getNextTick();
                    System.out.printf("[tick=%d] %s produce item #%d | buffer = %d/%d%n",
                            tick, name, item, count[0], K);

                    mutex.signal();
                    full.signal();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
            }
        };

        Runnable consumer = () -> {
            String name = Thread.currentThread().getName();
            try {
                while (true) {
                    full.wait(name);
                    mutex.wait(name);

                    int item = buffer[inOut[1]];
                    inOut[1] = (inOut[1] + 1) % K;
                    count[0]--;

                    int tick = getNextTick();
                    System.out.printf("[tick=%d] %s consume item #%d | buffer = %d/%d%n",
                            tick, name, item, count[0], K);

                    mutex.signal();
                    empty.signal();
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
            }
        };

        Thread[] prodThreads = new Thread[N];
        Thread[] consThreads = new Thread[M];

        for (int i = 0; i < N; i++) {
            prodThreads[i] = new Thread(producer, "Productor-" + (i + 1));
            prodThreads[i].start();
        }

        for (int i = 0; i < M; i++) {
            consThreads[i] = new Thread(consumer, "Consumidor-" + (i + 1));
            consThreads[i].start();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        for (Thread t : prodThreads)
            t.interrupt();
        for (Thread t : consThreads)
            t.interrupt();

        System.out.println("\n--- Demostracion Finalizada ---");
    }
}