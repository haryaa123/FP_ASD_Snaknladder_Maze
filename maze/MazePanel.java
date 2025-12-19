import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MazePanel extends JPanel {
    private Node[][] grid;
    private int rows, cols;
    
    // --- UKURAN DIPERBESAR ---
    private int totalCellSize = 45; // Ukuran total per kotak (Zoom In)
    private int pathSize = 25;      // Lebar jalanannya saja (sisanya jadi tebal dinding)
    
    private List<Node> finalPath; 
    private List<Node> searchHistory; 
    
    // List sinkronisasi agar aman diakses Thread animasi
    private List<Node> animatedSearch = java.util.Collections.synchronizedList(new ArrayList<>());
    private List<Node> animatedPath = java.util.Collections.synchronizedList(new ArrayList<>());
    
    private Thread animationThread;
    private boolean isBFS = true; // Untuk membedakan warna

    // --- WARNA ---
    private final Color WALL_COLOR = Color.decode("#2B3A42"); // Abu-abu gelap (Dinding)
    
    // Terrain
    private final Color PATH_GRASS = Color.decode("#A8E6CF"); // Hijau Pastel
    private final Color PATH_MUD   = Color.decode("#DCC7AA"); // Coklat Lumpur Pastel
    private final Color PATH_WATER = Color.decode("#74B9FF"); // Biru Air
    
    // Animasi
    private final Color COLOR_BFS_SEARCH = Color.decode("#FDCB6E"); // Kuning-Oranye
    private final Color COLOR_DFS_SEARCH = Color.decode("#E84393"); // Pink Tua
    private final Color COLOR_FINAL_PATH = Color.RED;

    public MazePanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        setPreferredSize(new Dimension(cols * totalCellSize, rows * totalCellSize));
        setBackground(WALL_COLOR);
    }

    public void setGrid(Node[][] grid) {
        this.grid = grid;
        stopAnimation();
        animatedSearch.clear();
        animatedPath.clear();
        repaint();
    }

    public void animate(List<Node> history, List<Node> path, boolean isBFS) {
        this.searchHistory = new ArrayList<>(history);
        this.finalPath = new ArrayList<>(path);
        this.isBFS = isBFS;
        
        stopAnimation();
        animatedSearch.clear();
        animatedPath.clear();

        // Menggunakan Thread agar bisa sleep dinamis (efek lambat di lumpur)
        animationThread = new Thread(() -> {
            try {
                // TAHAP 1: SEARCH ANIMATION
                for (Node n : searchHistory) {
                    animatedSearch.add(n);
                    repaint();
                    
                    // --- LOGIKA KECEPATAN BERDASARKAN TERRAIN ---
                    int baseDelay = 30; // Kecepatan dasar
                    int delay = baseDelay * n.weight; // Grass=30ms, Mud=150ms, Water=300ms
                    
                    Thread.sleep(delay); 
                }

                // TAHAP 2: FINAL PATH ANIMATION
                for (Node n : finalPath) {
                    animatedPath.add(n);
                    repaint();
                    Thread.sleep(40); // Jalur akhir agak cepat konstan
                }
            } catch (InterruptedException e) {
                // Thread stopped
            }
        });
        animationThread.start();
    }

    private void stopAnimation() {
        if (animationThread != null && animationThread.isAlive()) {
            animationThread.interrupt();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (grid == null) return;

        // Hitung margin agar jalanan ada di tengah "totalCellSize"
        int margin = (totalCellSize - pathSize) / 2;

        // 1. GAMBAR LORONG/JALAN
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Node n = grid[r][c];
                int x = c * totalCellSize + margin;
                int y = r * totalCellSize + margin;

                // Warna Dasar
                if (n.type.equals("Mud")) g2.setColor(PATH_MUD);
                else if (n.type.equals("Water")) g2.setColor(PATH_WATER);
                else g2.setColor(PATH_GRASS);

                g2.fillRect(x, y, pathSize, pathSize);

                // Gambar "Jembatan" ke tetangga agar temboknya hilang di koneksi
                for (Node neighbor : n.neighbors) {
                    int nx = neighbor.x * totalCellSize + margin;
                    int ny = neighbor.y * totalCellSize + margin;
                    
                    int bridgeX = Math.min(x, nx);
                    int bridgeY = Math.min(y, ny);
                    int bridgeW = (n.x == neighbor.x) ? pathSize : totalCellSize; // Melebar horizontal
                    int bridgeH = (n.y == neighbor.y) ? pathSize : totalCellSize; // Melebar vertikal
                    
                    g2.fillRect(bridgeX, bridgeY, bridgeW, bridgeH);
                }
            }
        }

        // 2. GAMBAR ANIMASI SEARCH (Kuning/Pink)
        g2.setColor(isBFS ? COLOR_BFS_SEARCH : COLOR_DFS_SEARCH);
        synchronized (animatedSearch) {
            for (Node n : animatedSearch) {
                // Gambar kotak kecil di tengah jalan menandakan sudah dikunjungi
                int x = n.x * totalCellSize + margin + 4;
                int y = n.y * totalCellSize + margin + 4;
                g2.fillRoundRect(x, y, pathSize - 8, pathSize - 8, 5, 5);
            }
        }

        // 3. GAMBAR FINAL PATH (Garis Merah Tebal)
        if (!animatedPath.isEmpty()) {
            g2.setColor(COLOR_FINAL_PATH);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            synchronized (animatedPath) {
                for (int i = 0; i < animatedPath.size() - 1; i++) {
                    Node curr = animatedPath.get(i);
                    Node next = animatedPath.get(i+1);
                    
                    int cx1 = curr.x * totalCellSize + totalCellSize/2;
                    int cy1 = curr.y * totalCellSize + totalCellSize/2;
                    int cx2 = next.x * totalCellSize + totalCellSize/2;
                    int cy2 = next.y * totalCellSize + totalCellSize/2;
                    
                    g2.drawLine(cx1, cy1, cx2, cy2);
                }
            }
        }

        // 4. START & END
        // Start
        int sx = grid[0][0].x * totalCellSize + margin;
        int sy = grid[0][0].y * totalCellSize + margin;
        g2.setColor(Color.GREEN);
        g2.fillOval(sx-2, sy-2, pathSize+4, pathSize+4);
        g2.setColor(Color.BLACK); g2.drawString("S", sx+8, sy+18);

        // End
        int ex = grid[rows-1][cols-1].x * totalCellSize + margin;
        int ey = grid[rows-1][cols-1].y * totalCellSize + margin;
        g2.setColor(Color.RED);
        g2.fillOval(ex-2, ey-2, pathSize+4, pathSize+4);
        g2.setColor(Color.WHITE); g2.drawString("E", ex+8, ey+18);
    }
}