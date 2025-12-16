import java.util.*;

public class SJFScheduler {

    public static void schedule(List<Process> processes, int contextSwitch) {

        int n = processes.size();
        int completed = 0;
        int time = 0;

        List<String> executionOrder = new ArrayList<>();
        Process running = null;

        while (completed < n) {
            // 1. Build ready queue
            List<Process> ready = new ArrayList<>();
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    ready.add(p);
                }
            }
            // 2. CPU idle
            if (ready.isEmpty()) {
                time++;
                continue;
            }
            // 3. Pick shortest remaining time
            ready.sort(Comparator.comparingInt((Process p) -> p.remainingTime).thenComparingInt(p -> p.arrivalTime));
            Process next = ready.get(0);

            // 4. Context switch → ONLY advance time
            if (running != next && !executionOrder.isEmpty()) {
                time += contextSwitch;
            }
            // 5. Record execution order
            if (running != next) {
                executionOrder.add(next.name);
            }
            running = next;

            // 6. Execute for 1 time unit
            running.remainingTime--;
            time++;

            // 7. Completion
            if (running.remainingTime == 0) {
                running.completionTime = time;
                completed++;
                running = null;
            }
        }
        // 9. Turnaround + averages
        double totalWT = 0, totalTAT = 0;

        for (Process p : processes) {
        p.turnaroundTime = p.completionTime - p.arrivalTime;
        p.waitingTime = p.turnaroundTime - p.burstTime;

        totalWT += p.waitingTime;
        totalTAT += p.turnaroundTime;

        System.out.println(p.name +" | WaitingTime=" + p.waitingTime +" | TurnaroundTime=" + p.turnaroundTime);
        }

        System.out.println("\nAverage Waiting Time: " + (totalWT / processes.size()));
        System.out.println("Average Turnaround Time: " + (totalTAT / processes.size()));

    }
}
