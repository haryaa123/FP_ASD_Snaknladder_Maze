import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainMaze extends JFrame {
    private MazeAlgorithm mazeAlg;
    private MazePanel mazePanel;
    private final int ROWS = 13; // Ganjil lebih bagus untuk maze
    private final int COLS = 21;

    private final Color COLOR_BG = Color.decode("#202020"); 

    public MainMaze() {
        setTitle("Final Project: Maze Solver (BFS, DFS, Dijkstra, A*)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        mazeAlg = new MazeAlgorithm(ROWS, COLS);
        mazePanel = new MazePanel(ROWS, COLS);
        
        JPanel mazeContainer = new JPanel(new GridBagLayout());
        mazeContainer.setBackground(COLOR_BG);
        mazeContainer.add(mazePanel);
        
        JScrollPane scrollPane = new JScrollPane(mazeContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // --- CONTROL PANEL (KANAN) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(Color.decode("#FDFCF5")); 
        controlPanel.setBorder(new EmptyBorder(25, 25, 25, 25)); 
        
        controlPanel.setPreferredSize(new Dimension(320, 0)); 

        JLabel title = new JLabel("Maze Controls");
        title.setFont(new Font("Comic Sans MS", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // --- TOMBOL-TOMBOL ---
        Dimension btnSize = new Dimension(280, 40);

        RoundedButton btnGen = new RoundedButton("GENERATE MAZE");
        btnGen.setBackground(Color.decode("#FFB7B2"));
        btnGen.setMaximumSize(btnSize);

        RoundedButton btnBFS = new RoundedButton("SOLVE: BFS (Queue)");
        btnBFS.setBackground(Color.decode("#FDCB6E")); 
        btnBFS.setForeground(Color.BLACK); 
        btnBFS.setMaximumSize(btnSize);

        RoundedButton btnDFS = new RoundedButton("SOLVE: DFS (Stack)");
        btnDFS.setBackground(Color.decode("#E84393")); 
        btnDFS.setMaximumSize(btnSize);

        // -- TOMBOL BARU --
        RoundedButton btnDijkstra = new RoundedButton("SOLVE: Dijkstra (Cost)");
        btnDijkstra.setBackground(Color.decode("#74B9FF")); // Biru
        btnDijkstra.setMaximumSize(btnSize);

        RoundedButton btnAStar = new RoundedButton("SOLVE: A* (Heuristic)");
        btnAStar.setBackground(Color.decode("#A29BFE")); // Ungu
        btnAStar.setMaximumSize(btnSize);

        // LEGEND
        JPanel legend = createLegend();
        legend.setAlignmentX(Component.CENTER_ALIGNMENT);
        legend.setMaximumSize(new Dimension(280, 250));

        // --- ACTION LISTENERS ---
        btnGen.addActionListener(e -> {
            mazeAlg.generateMaze();
            mazePanel.setGrid(mazeAlg.getGrid());
        });

        btnBFS.addActionListener(e -> {
            Node start = mazeAlg.getGrid()[0][0];
            Node end = mazeAlg.getGrid()[ROWS-1][COLS-1];
            List<Node> path = mazeAlg.solveBFS(start, end);
            mazePanel.animate(mazeAlg.getVisitedOrder(), path, true);
        });

        btnDFS.addActionListener(e -> {
            Node start = mazeAlg.getGrid()[0][0];
            Node end = mazeAlg.getGrid()[ROWS-1][COLS-1];
            List<Node> path = mazeAlg.solveDFS(start, end);
            mazePanel.animate(mazeAlg.getVisitedOrder(), path, false);
        });

        // Action Dijkstra
        btnDijkstra.addActionListener(e -> {
            Node start = mazeAlg.getGrid()[0][0];
            Node end = mazeAlg.getGrid()[ROWS-1][COLS-1];
            List<Node> path = mazeAlg.solveDijkstra(start, end);
            mazePanel.animate(mazeAlg.getVisitedOrder(), path, true);
        });

        // Action A*
        btnAStar.addActionListener(e -> {
            Node start = mazeAlg.getGrid()[0][0];
            Node end = mazeAlg.getGrid()[ROWS-1][COLS-1];
            List<Node> path = mazeAlg.solveAStar(start, end);
            mazePanel.animate(mazeAlg.getVisitedOrder(), path, true);
        });

        // Susun Layout
        controlPanel.add(title);
        controlPanel.add(Box.createVerticalStrut(15)); 
        controlPanel.add(legend);
        controlPanel.add(Box.createVerticalStrut(20)); 
        controlPanel.add(btnGen);
        controlPanel.add(Box.createVerticalStrut(15));
        controlPanel.add(btnBFS);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(btnDFS);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(btnDijkstra);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(btnAStar);
        
        controlPanel.add(Box.createVerticalGlue());

        add(controlPanel, BorderLayout.EAST);

        // Init First Maze
        mazeAlg.generateMaze();
        mazePanel.setGrid(mazeAlg.getGrid());

        setSize(1150, 750); 
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JPanel createLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.decode("#FDFCF5"));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Info & Weights"));
        
        p.add(createLegendItem("#A8E6CF", "Grass (Cost: 1)"));
        p.add(Box.createVerticalStrut(5));
        p.add(createLegendItem("#DCC7AA", "Mud (Cost: 5)"));
        p.add(Box.createVerticalStrut(5));
        p.add(createLegendItem("#74B9FF", "Water (Cost: 10)"));
        p.add(Box.createVerticalStrut(5));
        p.add(createLegendItem("#FDCB6E", "Scan Animation"));
        p.add(Box.createVerticalStrut(5));
        p.add(createLegendItem("#FF0000", "Final Path"));
        
        p.setBorder(BorderFactory.createCompoundBorder(
            p.getBorder(), 
            new EmptyBorder(10, 10, 10, 10)));
            
        return p;
    }
    
    private JPanel createLegendItem(String hexColor, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT); 
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(Color.decode(hexColor));
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
        
        item.add(colorBox);
        item.add(lbl);
        return item;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(MainMaze::new);
    }
}