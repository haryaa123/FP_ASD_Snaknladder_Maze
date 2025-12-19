import java.util.*;

public class MazeAlgorithm {
    private int rows, cols;
    private Node[][] grid;
    private List<Node> visitedOrderForAnimation; 
    
    public MazeAlgorithm(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Node[rows][cols];
        visitedOrderForAnimation = new ArrayList<>();
        initializeGrid();
    }
    
    private void initializeGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Node(c, r); 
            }
        }
    }

    public Node[][] getGrid() { return grid; }
    public List<Node> getVisitedOrder() { return visitedOrderForAnimation; }

    // --- GENERATE MAZE (Prim's Algorithm) ---
    public void generateMaze() {
        initializeGrid(); 
        Random rand = new Random();
        Node start = grid[0][0];
        start.visited = true; 
        
        List<Node> frontier = new ArrayList<>();
        addFrontier(start, frontier);

        while (!frontier.isEmpty()) {
            int randIndex = rand.nextInt(frontier.size());
            Node current = frontier.remove(randIndex);
            
            List<Node> visitedNeighbors = getVisitedNeighbors(current);
            
            if (!visitedNeighbors.isEmpty()) {
                Node neighbor = visitedNeighbors.get(rand.nextInt(visitedNeighbors.size()));
                current.addNeighbor(neighbor); 
                current.visited = true;       
                addFrontier(current, frontier);
            }
        }
        assignTerrainWeights();
    }
    
    private void assignTerrainWeights() {
        Random rand = new Random();
        for(int r=0; r<rows; r++) {
            for(int c=0; c<cols; c++) {
                if((r==0 && c==0) || (r==rows-1 && c==cols-1)) continue;
                double chance = rand.nextDouble();
                if (chance < 0.60) { grid[r][c].type = "Grass"; grid[r][c].weight = 1; } 
                else if (chance < 0.85) { grid[r][c].type = "Mud"; grid[r][c].weight = 5; } 
                else { grid[r][c].type = "Water"; grid[r][c].weight = 10; }
            }
        }
    }

    // --- BFS ---
    public List<Node> solveBFS(Node start, Node end) {
        resetData();
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(start); visited.add(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            visitedOrderForAnimation.add(current);
            if (current == end) return reconstructPath(end);

            for (Node neighbor : current.neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.parent = current;
                    queue.add(neighbor);
                }
            }
        }
        return new ArrayList<>();
    }

    // --- DFS ---
    public List<Node> solveDFS(Node start, Node end) {
        resetData();
        Stack<Node> stack = new Stack<>();
        Set<Node> visited = new HashSet<>();
        stack.push(start); visited.add(start);

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            if(!visitedOrderForAnimation.contains(current)) visitedOrderForAnimation.add(current);
            if (current == end) return reconstructPath(end);

            for (Node neighbor : current.neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    neighbor.parent = current;
                    stack.push(neighbor);
                }
            }
        }
        return new ArrayList<>();
    }

    // --- DIJKSTRA (Minimize Cost) ---
    public List<Node> solveDijkstra(Node start, Node end) {
        resetData();
        PriorityQueue<Node> pq = new PriorityQueue<>();
        
        start.gCost = 0;
        start.fCost = 0; 
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            visitedOrderForAnimation.add(current);

            if (current == end) return reconstructPath(end);

            for (Node neighbor : current.neighbors) {
                int newCost = current.gCost + neighbor.weight;

                if (newCost < neighbor.gCost) {
                    neighbor.gCost = newCost;
                    neighbor.fCost = newCost; // Dijkstra: f = g
                    neighbor.parent = current;
                    
                    // Update priority queue
                    pq.remove(neighbor); 
                    pq.add(neighbor);
                }
            }
        }
        return new ArrayList<>();
    }

    // --- A* ALGORITHM (Minimize Cost + Heuristic) ---
    public List<Node> solveAStar(Node start, Node end) {
        resetData();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        start.gCost = 0;
        start.hCost = calculateHeuristic(start, end);
        start.fCost = start.gCost + start.hCost;
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            visitedOrderForAnimation.add(current);

            if (current == end) return reconstructPath(end);

            for (Node neighbor : current.neighbors) {
                int newGCost = current.gCost + neighbor.weight;

                if (newGCost < neighbor.gCost) {
                    neighbor.gCost = newGCost;
                    neighbor.hCost = calculateHeuristic(neighbor, end);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost; // f = g + h
                    neighbor.parent = current;

                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }
        return new ArrayList<>();
    }

    // Heuristic: Manhattan Distance
    private int calculateHeuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // Helpers
    private void resetData() {
        visitedOrderForAnimation.clear();
        for(int r=0; r<rows; r++) {
            for(int c=0; c<cols; c++) {
                grid[r][c].parent = null;
                // Reset costs untuk algoritma weighted
                grid[r][c].gCost = Integer.MAX_VALUE;
                grid[r][c].fCost = Integer.MAX_VALUE;
            }
        }
    }
    
    private void addFrontier(Node node, List<Node> frontier) {
        int[] dr = {-1, 1, 0, 0}; int[] dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = node.y + dr[i]; int nc = node.x + dc[i];
            if (isValid(nr, nc)) {
                Node n = grid[nr][nc];
                if (!n.visited && !frontier.contains(n)) frontier.add(n);
            }
        }
    }
    
    private List<Node> getVisitedNeighbors(Node node) {
        List<Node> list = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}; int[] dc = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int nr = node.y + dr[i]; int nc = node.x + dc[i];
            if (isValid(nr, nc) && grid[nr][nc].visited) list.add(grid[nr][nc]);
        }
        return list;
    }
    
    private boolean isValid(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }
    
    private List<Node> reconstructPath(Node end) {
        List<Node> path = new ArrayList<>();
        Node curr = end;
        while (curr != null) { path.add(0, curr); curr = curr.parent; }
        return path;
    }
}