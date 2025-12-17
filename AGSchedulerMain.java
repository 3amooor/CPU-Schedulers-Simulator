import java.io.*;
import java.util.*;
import com.google.gson.*;

public class AGSchedulerMain {

    // Matches individual process in JSON
    static class ProcessJson {
        String name;
        int arrival;
        int burst;
        int priority;
        int quantum;
    }

    // Matches "input" object in JSON
    static class InputJson {
        ProcessJson[] processes;
    }

    // Matches root of JSON file
    static class TestJson {
        InputJson input;
    }

    public static void main(String[] args) throws IOException {

        File folder = new File("AG"); // your folder with JSON files
        File[] testFiles = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (testFiles == null || testFiles.length == 0) {
            System.out.println("No test files found!");
            return;
        }

        int contextSwitch = 0; // adjust if needed
        Gson gson = new Gson();

        for (File file : testFiles) {
            System.out.println("\n=== Running Test: " + file.getName() + " ===");

            BufferedReader br = new BufferedReader(new FileReader(file));
            TestJson test = gson.fromJson(br, TestJson.class);
            br.close();

            List<AGProcess> processes = new ArrayList<>();
            for (ProcessJson p : test.input.processes) { // <-- note .input.processes
                AGProcess proc = new AGProcess(p.name, p.arrival, p.burst, p.priority,p.quantum);
                processes.add(proc);
            }

            AGScheduler.schedule(processes, contextSwitch);
        }
    }
}
