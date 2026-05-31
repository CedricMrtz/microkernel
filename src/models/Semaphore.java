package models;

import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
    private int value;
    private final Queue<String> waitingQueue;

    public Semaphore(int initialValue) {
        this.value = initialValue;
        this.waitingQueue = new LinkedList<>();
    }

    public synchronized void wait(String threadName) throws InterruptedException {
        waitingQueue.add(threadName);
        while (value <= 0) {
            this.wait();
        }
        waitingQueue.remove(threadName);
        value--;
    }

    public synchronized void signal() {
        value++;
        this.notifyAll();
    }

    public synchronized int getValue() {
        return value;
    }

    public synchronized String getWaitingThreads() {
        return waitingQueue.toString();
    }

}
