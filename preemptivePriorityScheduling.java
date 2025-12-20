import java.util.*;

public class preemptivePriorityScheduling {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int agingInterval;
    int contextSwitch;

    public preemptivePriorityScheduling(List<Process> inputProcesses, int agingInterval , int contextSwitch) {
        this.agingInterval = agingInterval;
        this.contextSwitch = contextSwitch;
        for (Process p : inputProcesses) {
            this.processes.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority));
        }
    }

    public void execute() {
        PriorityQueue<Process> queue = new PriorityQueue<>(
                (p1, p2) -> {
                    if (p1.priority != p2.priority)
                        return Integer.compare(p1.priority, p2.priority);
                    else if (p1.arrivalTime != p2.arrivalTime)
                        return Integer.compare(p1.arrivalTime, p2.arrivalTime);
                    return p1.name.compareTo(p2.name);
                }
        );

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = processes.getFirst().arrivalTime;
        int i = 0;
        while (i < processes.size() && processes.get(i).arrivalTime == currentTime) {
            queue.add(processes.get(i));
            i++;
        }

        String lastProcess = "";
        List<Process> arrivedProcesses = new ArrayList<>();

        while (!queue.isEmpty()) {

            Process current = queue.poll();
            executionOrder.add(current);
            if (lastProcess.isEmpty())
                lastProcess = current.name;
            else if (!lastProcess.equals(current.name)) {
                lastProcess = current.name;
                queue.add(current);
                for (int j = 0; j < contextSwitch; j++) {
                    currentTime++;
                    arrivedProcesses = new ArrayList<>(queue);
                    queue.clear();
                    for (Process p : arrivedProcesses) {
                        if ((currentTime - p.tempArrivalTime) % agingInterval == 0) {
                            p.priority = Math.max(1, p.priority - 1);
                        }
                        queue.add(p);
                    }

                    while (i < processes.size() && processes.get(i).arrivalTime == currentTime) {
                        queue.add(processes.get(i));
                        i++;
                    }
                }

                continue;
            }
            currentTime++;
            current.remainingTime--;

            arrivedProcesses = new ArrayList<>(queue);
            queue.clear();
            for (Process p : arrivedProcesses) {
                if ((currentTime - p.tempArrivalTime) % agingInterval == 0) {
                    p.priority = Math.max(1, p.priority - 1);
                }
                queue.add(p);
            }

            while (i < processes.size() && processes.get(i).arrivalTime == currentTime) {
                queue.add(processes.get(i));
                i++;
            }

            if (current.remainingTime > 0) {
                current.tempArrivalTime = currentTime;
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
        String lstProcess = "";
        for (Process p : executionOrder) {
            if (p.name.equals(lstProcess)) {
                continue;
            }
            System.out.print(p.name + " ");
            lstProcess = p.name;
        }
        System.out.println();
    }

    public void printProcessResults() {
        System.out.println("Process Results:");
        for (Process p : processes) {
            System.out.println("Name : " + p.name + " Waiting Time : " + p.waitingTime + " Turnaround Time : " + p.turnaroundTime);
        }
    }

    public void averageWaitingTime() {
        double totalWaitingTime = 0;
        for (Process p : processes) {
            totalWaitingTime += p.waitingTime;
        }
        System.out.println("Average Waiting Time: " + Math.round((totalWaitingTime / processes.size()) * 100.0) / 100.0);
    }

    public void averageTurnaroundTime() {
        double totalTurnaroundTime = 0;
        for (Process p : processes) {
            totalTurnaroundTime += p.turnaroundTime;
        }
        System.out.println("Average Turnaround Time: " + Math.round((totalTurnaroundTime / processes.size()) * 100.0) / 100.0);
    }
}

