package config;

import graph.Agent;
import graph.ParallelAgent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;

public class GenericConfig implements Config {

    private String confFile;
    private List<ParallelAgent> agents = new ArrayList<>();

    public void setConfFile(String file) {
        this.confFile = file;
    }

    @Override
    public void create() {
        agents.clear();
        if (confFile == null)
            return;

        List<String> lines = readValidLinesFromFile(confFile);
        if (lines == null)
            return;

        buildAgentsFromConfigLines(lines);
    }

    @Override
    public String getName() {
        return "GenericConfig";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void close() {
        for (ParallelAgent pa : agents) {
            pa.close();
        }
    }

    private List<String> readValidLinesFromFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading conf file: " + e.getMessage());
            return null;
        }

        if (lines.size() % 3 != 0) {
            System.err.println("Invalid configuration file format");
            return null;
        }

        return lines;
    }

    private void buildAgentsFromConfigLines(List<String> lines) {
        for (int i = 0; i < lines.size(); i += 3) {
            String className = lines.get(i);
            String subsStr = lines.get(i + 1);
            String pubsStr = lines.get(i + 2);

            String[] subs = subsStr.isEmpty() ? new String[0] : subsStr.split(",");
            String[] pubs = pubsStr.isEmpty() ? new String[0] : pubsStr.split(",");

            createSingleAgent(className, subs, pubs);
        }
    }

    private void createSingleAgent(String className, String[] subs, String[] pubs) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(String[].class, String[].class);
            Agent agent = (Agent) constructor.newInstance((Object) subs, (Object) pubs);

            ParallelAgent pa = new ParallelAgent(agent, 10);
            agents.add(pa);
        } catch (Exception e) {
            System.err.println("Error instantiating agent " + className + ": " + e.getMessage());
        }
    }
}