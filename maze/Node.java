import java.util.ArrayList;
import java.util.List;

// Implement Comparable agar bisa dimasukkan ke PriorityQueue berdasarkan fCost
public class Node implements Comparable<Node> {
    int x, y;
    boolean visited;       // Untuk generation (Prim's)
    List<Node> neighbors;  // Graph connections
    Node parent;           // Untuk backtracking path

    // --- NEW: WEIGHTED PROPERTIES ---
    int weight;            // Biaya melewati node ini (1, 5, atau 10)
    String type;           // "Grass", "Mud", "Water"

    // --- NEW: PATHFINDING COSTS (Dijkstra / A*) ---
    int gCost;             // Jarak dari Start ke node ini
    int hCost;             // Heuristic (estimasi jarak ke End)
    int fCost;             // Total cost (g + h)

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.visited = false;
        this.neighbors = new ArrayList<>();
        this.parent = null;
        
        // Default: Grass
        this.weight = 1;
        this.type = "Grass";
    }

    public void addNeighbor(Node other) {
        if (!this.neighbors.contains(other)) this.neighbors.add(other);
        if (!other.neighbors.contains(this)) other.neighbors.add(this);
    }

    // Logic sorting untuk PriorityQueue: yang fCost-nya lebih kecil didahulukan
    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.fCost, other.fCost);
    }
}