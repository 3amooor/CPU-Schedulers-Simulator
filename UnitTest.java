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

class Scheduler {
    List<Process> processes = new ArrayList<>();
    List<Process> executionOrder = new ArrayList<>();
    int contextSwitch;

    public Scheduler(List<Process> processes, int contextSwitch) {
        this.contextSwitch = contextSwitch;
        for (Process p : processes) {
            this.processes.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority));
        }
    }

    public void execute() {
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

class AGProcess extends Process {
    int quantum;
    List<Integer> quantumHistory = new ArrayList<>();

    public AGProcess(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        super(name, arrivalTime, burstTime, priority);
        this.quantum = quantum;
    }
}

class SJFScheduler extends Scheduler {
    public SJFScheduler(List<Process> processes, int contextSwitch) {
        super(processes, contextSwitch);
    }

    @Override
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
}

class RRScheduler extends Scheduler {
    int timeQuantum;

    public RRScheduler(List<Process> inputProcesses, int contextSwitch, int timeQuantum) {
        super(inputProcesses, contextSwitch);
        this.timeQuantum = timeQuantum;
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
}

class PreemptivePriorityScheduling extends Scheduler {
    int agingInterval;

    public PreemptivePriorityScheduling(List<Process> inputProcesses, int agingInterval, int contextSwitch) {
        super(inputProcesses, contextSwitch);
        this.agingInterval = agingInterval;
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
}

class AGScheduler {

    enum State {
        FCFS, PRIORITY, SJF
    }

    public static void execute(List<AGProcess> processes, int contextSwitch) {
        List<AGProcess> pending = new ArrayList<>(processes);
        List<AGProcess> readyQueue = new ArrayList<>();
        List<AGProcess> finished = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();

        int currentTime = 0;
        State state = State.FCFS;
        int timeExecuted = 0;
        AGProcess currentProcess = null;

        // Sort pending processes by arrival time
        pending.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (!pending.isEmpty() || !readyQueue.isEmpty()) {

            // Add newly arrived processes to ready queue
            for (int i = 0; i < pending.size(); i++) {
                if (pending.get(i).arrivalTime <= currentTime) {
                    readyQueue.add(pending.get(i));
                    pending.remove(i);
                    i--;
                }
            }

            if (!readyQueue.isEmpty()) {
                // FCFS Phase: Execute first 25% of quantum
                if (state == State.FCFS) {
                    currentProcess = readyQueue.get(0);

                    // Calculate cycle limit for 25% of quantum
                    int cycleLimit = currentTime + (int) Math.ceil(currentProcess.quantum * 0.25);
                    executionOrder.add(currentProcess.name);
                    currentProcess.quantumHistory.add(currentProcess.quantum);
                    timeExecuted = 0;

                    // Execute for 25% of quantum
                    while (currentTime < cycleLimit && currentProcess.remainingTime > 0) {
                        currentTime++;
                        timeExecuted++;
                        currentProcess.remainingTime--;
                    }

                    if (currentProcess.remainingTime == 0) {
                        completeProcess(currentProcess, currentTime, readyQueue, finished);
                        state = State.FCFS;
                    } else {
                        state = State.PRIORITY;
                    }

                    // Priority Phase: Check for higher priority preemption (25%-50% of quantum)
                } else if (state == State.PRIORITY) {
                    currentProcess = readyQueue.get(0);
                    AGProcess bestPriority = getBestPriority(readyQueue);

                    if (bestPriority != currentProcess) {
                        // Preemption by higher priority process
                        currentProcess.completionTime = currentTime;
                        int addedQuantum = (int) Math.ceil((currentProcess.quantum - timeExecuted) / 2.0);
                        currentProcess.quantum += addedQuantum;

                        // Reorder queue: best priority to front
                        readyQueue.remove(currentProcess);
                        readyQueue.remove(bestPriority);
                        readyQueue.add(0, bestPriority);
                        readyQueue.add(currentProcess);

                        state = State.FCFS;
                        timeExecuted = 0;

                    } else {
                        // No preemption, continue to 50% of quantum
                        int cycleLimit = currentTime + (int) Math.ceil(currentProcess.quantum * 0.25);

                        while (currentTime < cycleLimit && currentProcess.remainingTime > 0) {
                            currentTime++;
                            timeExecuted++;
                            currentProcess.remainingTime--;
                        }

                        if (currentProcess.remainingTime == 0) {
                            completeProcess(currentProcess, currentTime, readyQueue, finished);
                            state = State.FCFS;
                        } else {
                            state = State.SJF;
                        }
                    }

                    // SJF Phase: Check for shorter job preemption (50%-100% of quantum)
                } else if (state == State.SJF) {
                    currentProcess = readyQueue.get(0);
                    AGProcess shortestJob = getShortestJob(readyQueue);

                    if (shortestJob != currentProcess) {
                        // Preemption by shorter job
                        currentProcess.completionTime = currentTime;
                        int addedQuantum = currentProcess.quantum - timeExecuted;
                        currentProcess.quantum += addedQuantum;

                        readyQueue.remove(currentProcess);
                        readyQueue.remove(shortestJob);
                        readyQueue.add(0, shortestJob);
                        readyQueue.add(currentProcess);

                        state = State.FCFS;
                    } else {
                        // Continue executing until quantum expires
                        int quantumEndTime = currentTime + (currentProcess.quantum - timeExecuted);

                        currentTime++;
                        timeExecuted++;
                        currentProcess.remainingTime--;

                        if (currentProcess.remainingTime == 0) {
                            completeProcess(currentProcess, currentTime, readyQueue, finished);
                            state = State.FCFS;
                        } else if (currentTime >= quantumEndTime) {
                            // Quantum expired, add 2 to quantum
                            state = State.FCFS;
                            currentProcess.completionTime = currentTime;
                            currentProcess.quantum += 2;
                            readyQueue.remove(0);
                            readyQueue.add(currentProcess);
                        }
                    }
                }
            } else {
                // CPU idle, advance time
                currentTime++;
            }
        }

        printResults(finished, executionOrder);
    }

    private static void completeProcess(AGProcess p, int time, List<AGProcess> ready, List<AGProcess> finished) {
        p.completionTime = time;
        p.quantum = 0;
        p.quantumHistory.add(0);
        p.turnaroundTime = p.completionTime - p.arrivalTime;
        p.waitingTime = p.turnaroundTime - p.burstTime;
        ready.remove(p);
        finished.add(p);
    }

    private static AGProcess getBestPriority(List<AGProcess> ready) {
        AGProcess best = ready.get(0);
        for (AGProcess p : ready) {
            if (p.priority < best.priority)
                best = p;
        }
        return best;
    }

    private static AGProcess getShortestJob(List<AGProcess> ready) {
        AGProcess best = ready.get(0);
        for (AGProcess p : ready) {
            if (p.remainingTime < best.remainingTime)
                best = p;
        }
        return best;
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
        // Sort processes by name for consistent output
        processes.sort(Comparator.comparing(p -> p.name));
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

public class UnitTest {
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