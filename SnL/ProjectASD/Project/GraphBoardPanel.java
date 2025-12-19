import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

public class GraphBoardPanel extends JPanel {
    private GameGraph graph;
    private List<Player> players; 
    private Player activePlayer = null;
    
    private static final int R = 20; // Radius node
    
    public GraphBoardPanel(GameGraph g, List<Player> p) {
        this.graph = g; this.players = p;
        setPreferredSize(new Dimension(700, 600)); // Sedikit lebih lebar
    }
    
    public void setActivePlayer(Player p) { 
        activePlayer = p; 
    }

    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 1. Background Rumput
        g2d.setColor(Color.decode("#8FBC8F")); // Dark Sea Green
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 2. Hitung Koordinat (Zig-Zag Layout)
        int w=getWidth(), h=getHeight();
        int cols = 8;
        int rows = (graph.nodes.length / cols) + 1;
        int xStep = w / cols;
        int yStep = h / (rows + 1);

        for(int i=1; i<graph.nodes.length; i++) {
            Node n = graph.getNode(i);
            int rowInv = (i-1)/cols; 
            int col = (i-1)%cols;
            
            int r = (rows - 1) - rowInv; // Balik urutan baris biar start di bawah
            int c = (rowInv % 2 == 0) ? col : (cols - 1 - col); // ZigZag
            
            n.x = xStep/2 + c*xStep; 
            n.y = yStep/2 + r*yStep + 20;
        }

        // 3. GAMBAR JALAN (ROAD) - Logika Koneksi Tebal
        g2d.setStroke(new BasicStroke(25, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(Color.decode("#D2B48C")); // Warna Tanah/Jalan
        
        Path2D path = new Path2D.Double();
        Node first = graph.getNode(1);
        path.moveTo(first.x, first.y);
        
        for(int i=2; i<graph.nodes.length; i++) {
            Node n = graph.getNode(i);
            path.lineTo(n.x, n.y);
        }
        g2d.draw(path); // Gambar jalan tanahnya

        // 4. Gambar Shortcut (Garis Putus-putus)
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{10}, 0));
        g2d.setColor(Color.WHITE);
        for(int i=1; i<graph.nodes.length; i++){
            Node n = graph.getNode(i);
            if(n.jumpTo != null) {
                g2d.setColor(n.id < n.jumpTo.id ? Color.BLUE : Color.RED); // Biru Naik, Merah Turun
                g2d.drawLine(n.x, n.y, n.jumpTo.x, n.jumpTo.y);
                // Panah indikator simple
                g2d.fillOval(n.jumpTo.x-5, n.jumpTo.y-5, 10, 10);
            }
        }
        
        // 5. Gambar Node & Ornamen
        g2d.setStroke(new BasicStroke(2));
        for(int i=1; i<graph.nodes.length; i++) {
            Node n = graph.getNode(i);
            
            // Base Node
            g2d.setColor(Color.WHITE);
            if (n.isStar) g2d.setColor(Color.YELLOW); // Node Bintang Kuning
            g2d.fillOval(n.x-R, n.y-R, R*2, R*2);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(n.x-R, n.y-R, R*2, R*2);
            
            // Text Angka
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String txt = String.valueOf(i);
            g2d.drawString(txt, n.x-5, n.y+5);
            
            // Icon Bintang (Visual Tambahan)
            if (n.isStar) {
                g2d.setColor(Color.ORANGE);
                g2d.setFont(new Font("Serif", Font.BOLD, 20));
                g2d.drawString("★", n.x-8, n.y-R-2);
            }
            
            // Score Bonus
            if (n.scoreBonus > 0) {
                g2d.setColor(Color.BLUE);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString("+" + n.scoreBonus, n.x+R, n.y);
            }
        }
        
        // 6. Gambar Pemain
        for(Player p : players) drawP(g2d, p);
        if(activePlayer!=null) drawP(g2d, activePlayer); // Re-draw active player on top
    }
    
    private void drawP(Graphics2D g, Player p) {
        Node n = graph.getNode(p.position);
        
        // Offset sedikit berdasarkan index player biar tidak numpuk total
        int offset = (Integer.parseInt(p.id.substring(1)) * 5) - 10; 
        
        g.setColor(p.color);
        g.fillOval(n.x - 10 + offset, n.y - 10 - offset, 20, 20);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        g.drawOval(n.x - 10 + offset, n.y - 10 - offset, 20, 20);
        
        // Nama kecil
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString(p.id, n.x - 5 + offset, n.y - 15 - offset);
    }
}