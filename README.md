# Host Connectivity in a Data Farm

> **Course:** Algorithms & Complexity | **Language:** Java | **Scope:** High-Scale Graph Evaluation

---

## Problem Statement

This project models and simulates optimized communication routes inside a data farm with up to **10⁵ network links** and **10⁴ physical hosts**.

Two main scenarios are handled:

- **No budget (`Budget = -1`):** Find the shortest path in terms of latency from a source host to every other reachable host, with no restriction on the number of hops.
- **With a budget (`Budget ≥ 0`):** Same as above, but limited to at most **K hops**.

> Too many hops can cause packet drops across physical switches — hence the hop constraint matters in practice.

**Bonus:** Find the longest simple path (no cycles, no revisiting nodes) from the source to every destination.

---

## Graph Representation & Data Structures

Given the scale (up to 10⁴ nodes and 10⁵ edges), memory efficiency is critical.

| Structure | Description |
|---|---|
| `List<Edge>[]` (Adjacency List) | Only allocates memory for real edges — O(V + E) space vs. O(V²) for a matrix |
| `Edge` | Holds `int target` and `double weight` (latency in nanoseconds) |
| `NodePair` | Pairs a node ID with a running distance; implements `Comparable<NodePair>` for use in Java's `PriorityQueue` |

---

## Algorithmic Approach

### Scenario A: No Hop Limit — Dijkstra's Algorithm

Uses a min-heap to always process the closest node first.

1. Set all distances to `∞` except the source (starts at `0`).
2. Push the source into the priority queue.
3. While the queue is not empty, pop the node with the smallest distance.
4. Skip stale entries where a shorter path has already been found.
5. Relax all outgoing edges and update distances when a shorter path is discovered.

### Scenario B: Hop-Limited — Layered DP (Bellman-Ford Variant)

Standard Dijkstra does not support strict hop limits — a more expensive path early on may be the only valid one within K hops.

- Run **one iteration per hop**, up to K iterations.
- **Clone** the current distance array before each update to ensure layer isolation.
- After K iterations, the array holds the shortest paths reachable within the allowed hop count.

---

## Code Snippets

### NodePair Comparison

```java
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
```

### Dijkstra Main Loop

```java
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
```

### Layer Isolation in Bounded Mode

```java
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
```

---

## Complexity Analysis

| Mode | Algorithm | Time Complexity | Space Complexity |
|---|---|---|---|
| Unbounded | Dijkstra + Min-Heap | O((V + E) log V) | O(V) |
| Bounded | Layered DP (Bellman-Ford Variant) | O(K × (V + E)) | O(V) |
| Longest Path (Bonus) | DFS Backtracking | O(V!) | O(V) stack |

---

## Safety Considerations

Finding the longest simple path is **NP-hard**. Running full DFS on very large graphs may cause excessive execution time or stack overflow.

> ⚠️ If `n > 20`, the longest path search is automatically skipped. Shortest path calculations continue normally.

---

## Modular Sub-Problems

- **Edge relaxation:** `dist[v] = min(dist[v], dist[u] + weight(u, v))`
- **Layer isolation:** `.clone()` prevents updates from the current hop from bleeding into other calculations in the same iteration.
- **Cycle prevention (Bonus):** DFS backtracking marks nodes as visited and unmarks them on return, enabling correct simple path exploration.

---

## References

- Standard graph theory — Dijkstra's algorithm and edge relaxation
- Bellman-Ford adaptations for hop-constrained shortest path problems
