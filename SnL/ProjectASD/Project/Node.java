public class Node {
    int id;
    Node next;    // Jalan ke depan
    Node prev;    // Jalan ke belakang
    Node jumpTo;  // Shortcut (Tangga/Ular)
    Node jumpFrom;// Asal shortcut
    
    int scoreBonus; // FITUR BARU: Random Score di node
    boolean isStar; // FITUR BARU: Penanda kelipatan 5
    
    int x, y;
    
    public Node(int id) {
        this.id = id;
        this.scoreBonus = 0;
        this.isStar = (id % 5 == 0); // Otomatis set bintang jika kelipatan 5
    }
}