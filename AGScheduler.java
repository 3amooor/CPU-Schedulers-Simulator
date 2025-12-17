import java.util.*;

public class AGScheduler {

    public static void schedule(List<AGProcess> processes, int contextSwitch) {
        int time = 0;
        int completed = 0;
        List<String> executionOrder = new ArrayList<>();
        Queue<AGProcess> readyQueue = new LinkedList<>();

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (completed < processes.size()) {

            for (AGProcess p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0 && !readyQueue.contains(p)) {
                    readyQueue.add(p);
                }
            }

            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }

            AGProcess current = readyQueue.poll();
            int timeInQuantum = 0;
            
            int quantum = current.quantum;
            int q25 = (int) Math.ceil(quantum * 0.25);
            int q50 = q25 + (int) Math.ceil(quantum * 0.25); 


            while (current.remainingTime > 0 && timeInQuantum < quantum) {
                
                executionOrder.add(current.name);
                current.remainingTime--;
                time++;
                timeInQuantum++;

                addNewArrivals(processes, readyQueue, time, current);

                if (current.remainingTime == 0) {
                    current.finish(time);
                    completed++;
                    break;
                }

                // Check Priority Phase 
                if (timeInQuantum >= q25 && timeInQuantum < q50) {
                    AGProcess bestPriority = getBestPriorityProcess(readyQueue);
                    if (bestPriority != null && bestPriority.priority < current.priority) {
                        int remainingQuantum = current.quantum - timeInQuantum;
                        current.priorityPreempted(remainingQuantum);
                        readyQueue.add(current);
                        
                        readyQueue.remove(bestPriority);
                        current = bestPriority;
                        
                        // Reset for new process
                        timeInQuantum = 0;
                        quantum = current.quantum;
                        q25 = (int) Math.ceil(quantum * 0.25);
                        q50 = q25 + (int) Math.ceil(quantum * 0.25);
                        
                        time += contextSwitch;
                        continue; 
                    }
                }

                // Check SJF Phase 
                if (timeInQuantum >= q50) {
                    AGProcess shortestJob = getShortestJob(readyQueue);
                    if (shortestJob != null && shortestJob.remainingTime < current.remainingTime) {
                        int remainingQuantum = current.quantum - timeInQuantum;
                        current.sjfPreempted(remainingQuantum);
                        readyQueue.add(current);

                        readyQueue.remove(shortestJob);
                        current = shortestJob;

                        // Reset for new process
                        timeInQuantum = 0;
                        quantum = current.quantum;
                        q25 = (int) Math.ceil(quantum * 0.25);
                        q50 = q25 + (int) Math.ceil(quantum * 0.25);

                        time += contextSwitch;
                        continue;
                    }
                }
            }

            // Quantum Expired 
            if (current.remainingTime > 0 && timeInQuantum == quantum) {
                current.quantumExpired();
                readyQueue.add(current);
                time += contextSwitch;
            }
        }

        printResults(processes, executionOrder);
    }

    private static AGProcess getBestPriorityProcess(Queue<AGProcess> queue) {
        AGProcess best = null;
        for (AGProcess p : queue) {
            if (best == null || p.priority < best.priority) {
                best = p;
            }
        }
        return best;
    }
    // SJF 
    private static AGProcess getShortestJob(Queue<AGProcess> queue) {
        AGProcess best = null;
        for (AGProcess p : queue) {
            if (best == null || p.remainingTime < best.remainingTime) {
                best = p;
            }
        }
        return best;
    }

    private static void addNewArrivals(List<AGProcess> processes, Queue<AGProcess> readyQueue, int time, AGProcess current) {
        for (AGProcess p : processes) {
            if (p.arrivalTime <= time && p.remainingTime > 0 && !readyQueue.contains(p) && p != current) {
                readyQueue.add(p);
            }
        }
    }

    private static void printResults(List<AGProcess> processes, List<String> order) {
        List<String> compactOrder = new ArrayList<>();
        if (!order.isEmpty()) {
            String last = order.get(0);
            for (int i = 1; i < order.size(); i++) {
                String cur = order.get(i);
                if (!cur.equals(last)) {
                    compactOrder.add(last);
                    last = cur;
                }
            }
            compactOrder.add(last);
        }

        System.out.println("\nExecution Order:");
        System.out.println(compactOrder);

        double totalWT = 0, totalTAT = 0;
        System.out.println("\nProcess Results:");
        for (AGProcess p : processes) {
            System.out.println(p.name + " | Waiting=" + p.waitingTime + " | Turnaround=" + p.turnaroundTime
                    + " | QuantumHistory=" + p.quantumHistory);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.println("\nAverage Waiting Time = " + totalWT / processes.size());
        System.out.println("Average Turnaround Time = " + totalTAT / processes.size());
    }
}