import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class Edge {
    int target;
    double weight;

    public Edge(int target, double weight) {
        this.target = target;
        this.weight = weight;
    }
}

public class Main {
    public static void main(String[] args) {
        String fileName = "input.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String line = br.readLine();
            if (line == null) return;
            StringTokenizer st = new StringTokenizer(line);
            int n = Integer.parseInt(st.nextToken());
            int e = Integer.parseInt(st.nextToken());

            int budget = Integer.parseInt(br.readLine().trim());

            int startHost = Integer.parseInt(br.readLine().trim());

            System.out.println("Nodes (n): " + n + ", Edges (e): " + e);
            System.out.println("Budget: " + budget + ", Starting Host: " + startHost);

            List<Edge>[] adjList = new ArrayList[n];
            for (int i = 0; i < n; i++) {
                adjList[i] = new ArrayList<Edge>();
            }

            for (int i = 0; i < e; i++) {
                String edgeLine = br.readLine();
                if (edgeLine == null) break;

                StringTokenizer edgeSt = new StringTokenizer(edgeLine);
                int fromHost = Integer.parseInt(edgeSt.nextToken());
                int toHost = Integer.parseInt(edgeSt.nextToken());
                double weight = Double.parseDouble(edgeSt.nextToken());

                adjList[fromHost].add(new Edge(toHost, weight));
            }

            System.out.println("Graph ok\n");

            if (budget == -1) {
                runUnboundedDijkstra(adjList, n, startHost);
            } else {
                runBoundedPath(adjList, n, startHost, budget);
            }

            System.out.println();

            // OPTION 1 FIX: Safety threshold to prevent StackOverflowError on huge graphs
            if (n <= 20) {
                runLongestPath(adjList, n, startHost);
            } else {
                System.out.println("Longest Simple Chain");
                System.out.println("Skipped: Dataset too large.");
            }

        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
            System.out.println("No input");
        }
    }

    public static void runUnboundedDijkstra(List<Edge>[] adjList, int n, int startHost) {
        double[] distances = new double[n];
        java.util.Arrays.fill(distances, Double.MAX_VALUE);
        distances[startHost] = 0.0;

        java.util.PriorityQueue<NodePair> pq = new java.util.PriorityQueue<>();
        pq.add(new NodePair(startHost, 0.0));

        while (!pq.isEmpty()) {
            NodePair current = pq.poll();
            int u = current.node;
            double currDist = current.distance;

            if (currDist > distances[u]) continue;

            for (Edge edge : adjList[u]) {
                double newDist = distances[u] + edge.weight;
                if (newDist < distances[edge.target]) {
                    distances[edge.target] = newDist;
                    pq.add(new NodePair(edge.target, newDist));
                }
            }
        }

        System.out.println("Unbounded Shortest Path");
        for (int i = 0; i < n; i++) {
            if (distances[i] == Double.MAX_VALUE) {
                System.out.println("Host " + i + ": Unreachable");
            } else {
                System.out.println("Host " + i + ": " + distances[i] + " weights");
            }
        }
    }

    public static void runBoundedPath(List<Edge>[] adjList, int n, int startHost, int budget) {
        double[] distances = new double[n];
        java.util.Arrays.fill(distances, Double.MAX_VALUE);
        distances[startHost] = 0.0;

        for (int k = 0; k < budget; k++) {
            double[] nextDistances = distances.clone();
            for (int u = 0; u < n; u++) {
                if (distances[u] == Double.MAX_VALUE) continue;

                for (Edge edge : adjList[u]) {
                    if (distances[u] + edge.weight < nextDistances[edge.target]) {
                        nextDistances[edge.target] = distances[u] + edge.weight;
                    }
                }
            }
            distances = nextDistances;
        }

        System.out.println("Bounded Shortest Path (Budget: " + budget + ")");
        for (int i = 0; i < n; i++) {
            if (distances[i] == Double.MAX_VALUE) {
                System.out.println("Host " + i + ": Unreachable within budget");
            } else {
                System.out.println("Host " + i + ": " + distances[i] + " weight");
            }
        }
    }

    public static void runLongestPath(List<Edge>[] adjList, int n, int startHost) {
        double[] maxDistances = new double[n];
        java.util.Arrays.fill(maxDistances, -1.0);
        maxDistances[startHost] = 0.0;

        boolean[] visited = new boolean[n];
        dfsLongest(startHost, 0.0, adjList, maxDistances, visited);

        System.out.println("Longest Simple Chain");
        for (int i = 0; i < n; i++) {
            if (maxDistances[i] == -1.0) {
                System.out.println("Infinite");
            } else {
                System.out.println("Host " + i + ": " + maxDistances[i] + " weight");
            }
        }
    }

    private static void dfsLongest(int u, double currentDist, List<Edge>[] adjList, double[] maxDistances, boolean[] visited) {
        visited[u] = true;

        if (currentDist > maxDistances[u]) {
            maxDistances[u] = currentDist;
        }

        for (Edge edge : adjList[u]) {
            if (!visited[edge.target]) {
                dfsLongest(edge.target, currentDist + edge.weight, adjList, maxDistances, visited);
            }
        }

        visited[u] = false;
    }
}

class NodePair implements Comparable<NodePair> {
    int node;
    double distance;

    public NodePair(int node, double distance) {
        this.node = node;
        this.distance = distance;
    }

    @Override
    public int compareTo(NodePair other) {
        return Double.compare(this.distance, other.distance);
    }
}