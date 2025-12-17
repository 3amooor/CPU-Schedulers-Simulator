import java.util.*;

class AGProcess extends Process {
    int quantum;
    int initialQuantum;
    List<Integer> quantumHistory;
    boolean inQueue = false;

    public AGProcess(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        super(name, arrivalTime, burstTime, priority);
        this.quantum = quantum;
        this.initialQuantum = quantum;
        this.quantumHistory = new ArrayList<>();
        this.quantumHistory.add(quantum);
    }

    void finish(int currentTime) {
        quantum = 0;
        quantumHistory.add(0);
        completionTime = currentTime;
        turnaroundTime = completionTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;
    }

    void quantumExpired() {
        quantum += 2;
        quantumHistory.add(quantum);
    }

    void priorityPreempted(int remainingQuantum) {
        quantum += (int) Math.ceil(remainingQuantum / 2.0);
        quantumHistory.add(quantum);
    }

    void sjfPreempted(int remainingQuantum) {
        quantum += remainingQuantum;
        quantumHistory.add(quantum);
    }
}
