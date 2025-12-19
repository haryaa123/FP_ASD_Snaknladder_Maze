import java.util.*;

public class GameGraph {
    Node head; 
    Node tail; 
    Node[] nodes; // Ukuran dinamis

    public GameGraph(int size) {
        nodes = new Node[size + 1];

        // 1. Buat Node Linear
        for (int i = 1; i <= size; i++) {
            nodes[i] = new Node(i);
            
            // FITUR BARU: Random Score (30% node punya skor)
            if (Math.random() < 0.3 && i != 1 && i != size) {
                int randomScore = (new Random().nextInt(5) + 1) * 10; // 10, 20, .. 50
                nodes[i].scoreBonus = randomScore;
            }

            if (i > 1) {
                Node prevNode = nodes[i-1];
                Node currNode = nodes[i];
                prevNode.next = currNode;
                currNode.prev = prevNode;
            }
        }
        head = nodes[1];
        tail = nodes[size];
        
        // 2. Tambahkan Koneksi Random (Ular/Tangga)
        addRandomConnections(5, size); 
    }
    
    public Node getNode(int id) {
        if (id < 1) return nodes[1];
        if (id >= nodes.length) return nodes[nodes.length - 1];
        return nodes[id];
    }
    
    private void addRandomConnections(int count, int size) {
        Random rand = new Random();
        int created = 0;
        
        while (created < count) {
            int start = rand.nextInt(size - 2) + 2; 
            int end = rand.nextInt(size - 2) + 2;
            
            // Logic: Jangan di node Bintang, jangan di node yang ada skor
            if (nodes[start].isStar || nodes[start].scoreBonus > 0) continue;

            int startRow = (start - 1) / 8;
            int endRow = (end - 1) / 8;
            
            if (startRow != endRow && start != end && 
                nodes[start].jumpTo == null && nodes[end].jumpTo == null) {
                
                nodes[start].jumpTo = nodes[end];
                nodes[end].jumpFrom = nodes[start]; 
                created++;
            }
        }
    }
    
    // (Method getShortestPath bisa kamu keep atau hapus jika tidak dipakai di versi baru)
    // Untuk ringkas, saya skip di sini agar fokus ke fitur baru.
}