import java.util.*;

public class RR {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int contextSwitch;
    int timeQuantum;

    public RR(List<Process> inputProcesses, int contextSwitch, int timeQuantum) {
        this.contextSwitch = contextSwitch;
        this.timeQuantum = timeQuantum;
        for (Process p : inputProcesses) {
            this.processes.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority, p.quantum));
        }
    }

    public void execute() {
        Queue<Process> queue = new LinkedList<>();
        int currentTime = 0;
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int i = 0;
        while (i < processes.size() && processes.get(i).arrivalTime <= currentTime) {
            queue.add(processes.get(i));
            i++;
        }

        while (!queue.isEmpty()) {
            Process current = queue.poll();
            executionOrder.add(current);

            int execTime = Math.min(timeQuantum, current.remainingTime);

            for (int t = 0; t < execTime; t++) {
                currentTime++;
                while (i < processes.size() && processes.get(i).arrivalTime <= currentTime) {
                    queue.add(processes.get(i));
                    i++;
                }
            }

            current.remainingTime -= execTime;

            if (current.remainingTime > 0) {
                currentTime += contextSwitch;
                queue.add(current);
            } else {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
            }
        }
    }
}
