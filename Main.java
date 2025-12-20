import java.util.*;

class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int priority;
    int completionTime;
    int waitingTime;
    int turnaroundTime;
    int tempArrivalTime;

    public Process(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.tempArrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.completionTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
}

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

class SJFScheduler {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int contextSwitch;

    public SJFScheduler(List<Process> processes, int contextSwitch) {
        this.contextSwitch = contextSwitch;
        for (Process p : processes) {
            this.processes.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority));
        }
    }

    public void execute() {
        int n = processes.size();
        int completed = 0;
        int time = 0;
        Process running = null;

        while (completed < n) {
            // Build ready queue
            List<Process> ready = new ArrayList<>();
            for (Process p : processes) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    ready.add(p);
                }
            }
            // CPU idle
            if (ready.isEmpty()) {
                time++;
                continue;
            }
            // Pick shortest remaining time
            ready.sort(Comparator.comparingInt((Process p) -> p.remainingTime).thenComparingInt(p -> p.arrivalTime));
            Process next = ready.get(0);

            // Context switch → ONLY advance time
            if (running != next && !executionOrder.isEmpty()) {
                time += contextSwitch;
            }
            // Record execution order
            if (running != next) {
                executionOrder.add(next);
            }
            running = next;

            // Execute for 1 time unit
            running.remainingTime--;
            time++;

            // Completion
            if (running.remainingTime == 0) {
                running.completionTime = time;
                running.turnaroundTime = running.completionTime - running.arrivalTime;
                running.waitingTime = running.turnaroundTime - running.burstTime;
                completed++;
                running = null;
            }
        }
    }

    public void printResults() {
        double totalWT = 0, totalTAT = 0;

        System.out.print("Execution Order: ");
        for (Process p : executionOrder) {
            System.out.print(p.name + " ");
        }

        System.out.println("\nProcess Results:");
        for (Process p : processes) {
            System.out.printf("Name: %-5s | WaitingTime = %-5d | TurnaroundTime = %-5d%n",
                    p.name, p.waitingTime, p.turnaroundTime);

            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.println("Average Waiting Time = " + Math.round((totalWT / processes.size()) * 100.0) / 100.0);
        System.out.println("Average Turnaround Time = " + Math.round((totalTAT / processes.size()) * 100.0) / 100.0);
    }
}

class RRScheduler {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int contextSwitch;
    int timeQuantum;

    public RRScheduler(List<Process> inputProcesses, int contextSwitch, int timeQuantum) {
        this.contextSwitch = contextSwitch;
        this.timeQuantum = timeQuantum;
        for (Process p : inputProcesses) {
            this.processes.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority));
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

        boolean firstExecution = true;

        while (!queue.isEmpty()) {
            Process current = queue.poll();
            executionOrder.add(current);

            // Add context switch before execution (except for the very first process)
            if (!firstExecution) {
                currentTime += contextSwitch;
            }
            firstExecution = false;

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
                queue.add(current);
            } else {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
            }
        }
    }

    public void printResults() {
        double totalWT = 0, totalTAT = 0;

        System.out.print("Execution Order: ");
        for (Process p : executionOrder) {
            System.out.print(p.name + " ");
        }

        System.out.println("\nProcess Results:");
        for (Process p : processes) {
            System.out.printf("Name: %-5s | WaitingTime = %-5d | TurnaroundTime = %-5d%n",
                    p.name, p.waitingTime, p.turnaroundTime);

            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.println("Average Waiting Time = " + Math.round((totalWT / processes.size()) * 100.0) / 100.0);
        System.out.println("Average Turnaround Time = " + Math.round((totalTAT / processes.size()) * 100.0) / 100.0);
    }
}

class PreemptivePriorityScheduling {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int agingInterval;
    int contextSwitch;

    public PreemptivePriorityScheduling(List<Process> inputProcesses, int agingInterval, int contextSwitch) {
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
                });

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = processes.getFirst().arrivalTime;
        int i = 0;
        while (i < processes.size() && processes.get(i).arrivalTime == currentTime) {
            queue.add(processes.get(i));
            i++;
        }

        String lastProcess = "";
        List<Process> arrivedProcesses = new ArrayList<>();
        String curName = "";

        while (!queue.isEmpty() || i < processes.size()) {
            Process current = null;
            curName = "Null";
            if (!queue.isEmpty()) {
                current = queue.poll();
                executionOrder.add(current);
                curName = current.name;
            }
            if (current != null && lastProcess.isEmpty())
                lastProcess = current.name;
            else if (!lastProcess.equals(curName) && !lastProcess.equals("Null")) {
                lastProcess = curName;
                if (!curName.equals("Null")) {
                    queue.add(current);
                }

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
            if (current != null)
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

            if (current == null)
                continue;
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

    public void printResults() {
        double totalWT = 0, totalTAT = 0;

        System.out.print("Execution Order: ");
        String lastProcess = "";

        for (Process p : executionOrder) {
            if (p.name.equals(lastProcess)) {
                continue;
            }
            System.out.print(p.name + " ");
            lastProcess = p.name;
        }

        System.out.println("\nProcess Results:");
        for (Process p : processes) {
            System.out.printf("Name: %-5s | WaitingTime = %-5d | TurnaroundTime = %-5d%n",
                    p.name, p.waitingTime, p.turnaroundTime);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.println("Average Waiting Time = " + Math.round((totalWT / processes.size()) * 100.0) / 100.0);
        System.out.println("Average Turnaround Time = " + Math.round((totalTAT / processes.size()) * 100.0) / 100.0);
    }
}

class AGScheduler {

    public static void execute(List<AGProcess> processes, int contextSwitch) {
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

                        continue;
                    }
                }
            }

            // Quantum Expired
            if (current.remainingTime > 0 && timeInQuantum == quantum) {
                current.quantumExpired();
                readyQueue.add(current);
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

        System.out.print("Execution Order: ");
        for (int i = 0; i < compactOrder.size(); i++) {
            System.out.print(compactOrder.get(i) + " ");
        }

        double totalWT = 0, totalTAT = 0;
        System.out.println("\nProcess Results:");
        for (AGProcess p : processes) {
            System.out.printf("Name: %-5s | WaitingTime = %-5d | TurnaroundTime = %-5d | QuantumHistory = %s%n",
                    p.name, p.waitingTime, p.turnaroundTime, p.quantumHistory);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        System.out.println("Average Waiting Time = " + Math.round((totalWT / processes.size()) * 100.0) / 100.0);
        System.out.println("Average Turnaround Time = " + Math.round((totalTAT / processes.size()) * 100.0) / 100.0);
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("CPU SCHEDULERS SIMULATOR - TEST SUITE");
        System.out.println("=".repeat(80));

        // Test OtherSchedulers (SJF, RR, Priority)
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TESTING OTHER SCHEDULERS (SJF, RR, Priority)");
        System.out.println("=".repeat(80));

        String[] otherTests = {
                "OtherSchedulers/test_1.json",
                "OtherSchedulers/test_2.json",
                "OtherSchedulers/test_3.json",
                "OtherSchedulers/test_4.json",
                "OtherSchedulers/test_5.json",
                "OtherSchedulers/test_6.json"
        };

        for (String testFile : otherTests) {
            runOtherSchedulersTest(testFile);
        }

        // Test AG Scheduler
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TESTING AG SCHEDULER");
        System.out.println("=".repeat(80));

        String[] agTests = {
                "AG/AG_test1.json",
                "AG/AG_test2.json",
                "AG/AG_test3.json",
                "AG/AG_test4.json",
                "AG/AG_test5.json",
                "AG/AG_test6.json"
        };

        for (String testFile : agTests) {
            runAGSchedulerTest(testFile);
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ALL TESTS COMPLETED");
        System.out.println("=".repeat(80));
    }

    private static void runOtherSchedulersTest(String filename) {
        try {
            System.out.println("\n" + "-".repeat(80));
            System.out.println("Testing: " + filename);
            System.out.println("-".repeat(80));

            // Read JSON file
            String content = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(filename)));

            // Parse JSON manually (simple parsing)
            int contextSwitch = extractInt(content, "contextSwitch");
            int rrQuantum = extractInt(content, "rrQuantum");
            int agingInterval = extractInt(content, "agingInterval");

            List<Process> processes = parseProcesses(content);

            // Test SJF Scheduler
            System.out.println("\n>>> SJF Scheduler:");
            SJFScheduler sjfScheduler = new SJFScheduler(processes, contextSwitch);
            sjfScheduler.execute();
            sjfScheduler.printResults();

            // Test RR Scheduler
            System.out.println("\n>>> Round Robin Scheduler:");
            RRScheduler rrScheduler = new RRScheduler(processes, contextSwitch, rrQuantum);
            rrScheduler.execute();
            rrScheduler.printResults();

            // Test Priority Scheduler
            System.out.println("\n>>> Preemptive Priority Scheduler:");
            PreemptivePriorityScheduling priorityScheduler = new PreemptivePriorityScheduling(processes, agingInterval,
                    contextSwitch);
            priorityScheduler.execute();
            priorityScheduler.printResults();

        } catch (Exception e) {
            System.out.println("Error reading test file: " + filename);
            e.printStackTrace();
        }
    }

    private static void runAGSchedulerTest(String filename) {
        try {
            System.out.println("\n" + "-".repeat(80));
            System.out.println("Testing: " + filename);
            System.out.println("-".repeat(80));

            // Read JSON file
            String content = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(filename)));

            int contextSwitch = 1; // Default context switch for AG
            List<AGProcess> processes = parseAGProcesses(content);

            System.out.println("\n>>> AG Scheduler:");
            AGScheduler.execute(processes, contextSwitch);

        } catch (Exception e) {
            System.out.println("Error reading test file: " + filename);
            e.printStackTrace();
        }
    }

    private static List<Process> parseProcesses(String json) {
        List<Process> processes = new ArrayList<>();

        // Find processes array
        int processesStart = json.indexOf("\"processes\"");
        int arrayStart = json.indexOf("[", processesStart);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        String processesJson = json.substring(arrayStart + 1, arrayEnd);

        // Split by objects
        String[] processObjs = processesJson.split("\\},\\s*\\{");

        for (String obj : processObjs) {
            obj = obj.replaceAll("[\\{\\}]", "");

            String name = extractValue(obj, "name");
            int arrival = Integer.parseInt(extractValue(obj, "arrival"));
            int burst = Integer.parseInt(extractValue(obj, "burst"));
            int priority = Integer.parseInt(extractValue(obj, "priority"));

            processes.add(new Process(name, arrival, burst, priority));
        }

        return processes;
    }

    private static List<AGProcess> parseAGProcesses(String json) {
        List<AGProcess> processes = new ArrayList<>();

        // Find processes array
        int processesStart = json.indexOf("\"processes\"");
        int arrayStart = json.indexOf("[", processesStart);
        int arrayEnd = findMatchingBracket(json, arrayStart);
        String processesJson = json.substring(arrayStart + 1, arrayEnd);

        // Split by objects
        String[] processObjs = processesJson.split("\\},\\s*\\{");

        for (String obj : processObjs) {
            obj = obj.replaceAll("[\\{\\}]", "");

            String name = extractValue(obj, "name");
            int arrival = Integer.parseInt(extractValue(obj, "arrival"));
            int burst = Integer.parseInt(extractValue(obj, "burst"));
            int priority = Integer.parseInt(extractValue(obj, "priority"));
            int quantum = Integer.parseInt(extractValue(obj, "quantum"));

            processes.add(new AGProcess(name, arrival, burst, priority, quantum));
        }

        return processes;
    }

    private static String extractValue(String json, String key) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos == -1)
            return "";

        int colonPos = json.indexOf(":", keyPos);
        int commaPos = json.indexOf(",", colonPos);
        if (commaPos == -1)
            commaPos = json.length();

        String value = json.substring(colonPos + 1, commaPos).trim();
        return value.replaceAll("\"", "").trim();
    }

    private static int extractInt(String json, String key) {
        String value = extractValue(json, key);
        if (value.isEmpty())
            return 0;
        return Integer.parseInt(value);
    }

    private static int findMatchingBracket(String str, int start) {
        int count = 1;
        for (int i = start + 1; i < str.length(); i++) {
            if (str.charAt(i) == '[')
                count++;
            if (str.charAt(i) == ']')
                count--;
            if (count == 0)
                return i;
        }
        return str.length();
    }
}