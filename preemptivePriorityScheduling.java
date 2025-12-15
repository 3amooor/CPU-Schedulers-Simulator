import java.util.*;

public class preemptivePriorityScheduling {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int agingInterval;

    public preemptivePriorityScheduling(List<Process> inputProcesses, int agingInterval) {
        this.agingInterval = agingInterval;
        for (Process p : inputProcesses) {
            this.processes.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority));
        }
    }

    public void execute() {
        PriorityQueue<Process> queue = new PriorityQueue<>(Comparator.comparingInt(p -> p.priority));
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = processes.getFirst().arrivalTime;
        int i = 0;
        while (i < processes.size() && processes.get(i).arrivalTime == currentTime) {
            queue.add(processes.get(i));
            i++;
        }

        while (!queue.isEmpty()) {
            Process current = queue.poll();
            executionOrder.add(current);

            currentTime++;
            current.remainingTime--;

            // Aging mechanism
            for (Process p : queue) {
                if (currentTime - p.arrivalTime >= agingInterval) {
                    p.priority = Math.max(0, p.priority - 1); // Increase priority
                }
            }

            while (i < processes.size() && processes.get(i).arrivalTime == currentTime) {
                queue.add(processes.get(i));
                i++;
            }

            if (current.remainingTime > 0) {
                queue.add(current);
            } else {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
            }
        }
    }

    public void printExecutionOrder() {
        System.out.print("Execution Order: ");
        for (Process p : executionOrder) {
            System.out.print(p.name + " ");
        }
        System.out.println();
    }

    public void printProcessResults() {
        System.out.println("Process Results:\n");
        for (Process p : processes) {
            System.out.println("Name :" + p.name + "Waiting Time :" + p.waitingTime + " Turnaround Time :" + p.turnaroundTime);
        }
        System.out.println();
    }

    public void averageWaitingTime() {
        double totalWaitingTime = 0;
        for (Process p : processes) {
            totalWaitingTime += p.waitingTime;
        }
        System.out.println("Average Waiting Time: " + (totalWaitingTime / processes.size()));
    }

    public void averageTurnaroundTime() {
        double totalTurnaroundTime = 0;
        for (Process p : processes) {
            totalTurnaroundTime += p.turnaroundTime;
        }
        System.out.println("Average Turnaround Time: " + (totalTurnaroundTime / processes.size()));
    }
}

