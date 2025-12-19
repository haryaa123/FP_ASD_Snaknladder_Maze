import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import javax.swing.Timer; // Pastikan import Timer swing

public class SnakesLaddersGUI extends JFrame {
    private GameGraph graph;
    private GraphBoardPanel boardPanel; 
    private LinkedList<Player> playerQueue; 
    private ArrayList<Player> allPlayersList; // Master list player
    private SoundManager soundManager;
    
    // Statistik Global (Priority Queue untuk Top Score)
    private PriorityQueue<Player> topScoresPQ;
    
    // UI
    private JLabel infoLabel;
    private JButton rollButton;
    private JButton statsButton;
    private JTextArea logArea;
    private DiceVisualizer diceVisualizer;
    
    private boolean isAnimating = false;
    
    public SnakesLaddersGUI() {
        soundManager = new SoundManager();
        playerQueue = new LinkedList<>();
        allPlayersList = new ArrayList<>();
        
        // Priority Queue: Descending order by Highest Score
        topScoresPQ = new PriorityQueue<>((p1, p2) -> Integer.compare(p2.highestScore, p1.highestScore));
        
        setupPlayers();
        setupGame();
        initUI();
        
        soundManager.playBGM("bgm.wav"); // Pastikan file ada
    }
    
    private void setupPlayers() {
        String input = JOptionPane.showInputDialog(this, "How many players?", "2");
        int count = 2;
        try { count = Integer.parseInt(input); } catch (Exception e) {}
        if(count < 1) count = 1; if(count > 4) count = 4; // Batasi 4 biar layar muat
        
        Color[] colors = {Color.RED, Color.BLUE, Color.MAGENTA, Color.CYAN};
        
        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog(this, "Name Player " + (i+1) + ":", "Player " + (i+1));
            Player p = new Player("P" + (i+1), name, colors[i]);
            allPlayersList.add(p);
            playerQueue.add(p);
        }
    }
    
    private void setupGame() {
        // Bebas tentukan size, misalnya 50 node saja
        graph = new GameGraph(50); 
        
        // Reset posisi & score saat ini (tapi history win tetap)
        for(Player p : allPlayersList) {
            p.resetForNewGame();
        }
        
        // Re-populate queue
        playerQueue.clear();
        playerQueue.addAll(allPlayersList);
        
        if(boardPanel != null) {
            remove(boardPanel);
        }
        boardPanel = new GraphBoardPanel(graph, allPlayersList);
        add(boardPanel, BorderLayout.CENTER);
        revalidate();
    }
    
    private void initUI() {
        setTitle("Snakes & Ladders: Road Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        add(boardPanel, BorderLayout.CENTER);
        
        // PANEL KANAN
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(new EmptyBorder(10,10,10,10));
        sidePanel.setPreferredSize(new Dimension(250, 600));
        sidePanel.setBackground(new Color(240, 240, 240));
        
        infoLabel = new JLabel("Welcome!");
        infoLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        diceVisualizer = new DiceVisualizer();
        diceVisualizer.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rollButton = new RoundedButton("ROLL DICE");
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statsButton = new RoundedButton("VIEW STATS");
        statsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsButton.setBackground(Color.ORANGE);
        
        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(logArea);
        
        sidePanel.add(infoLabel);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(diceVisualizer);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(rollButton);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(statsButton);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(new JLabel("Game Log:"));
        sidePanel.add(scrollLog);
        
        add(sidePanel, BorderLayout.EAST);
        
        // ACTIONS
        rollButton.addActionListener(e -> performRoll());
        statsButton.addActionListener(e -> showStatistics());
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        updateTurnInfo();
    }
    
    private void updateTurnInfo() {
        if(!playerQueue.isEmpty()) {
            Player current = playerQueue.peek();
            infoLabel.setText("Turn: " + current.name);
            infoLabel.setForeground(Color.BLACK);
        }
    }
    
    private void log(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void performRoll() {
        if(playerQueue.isEmpty() || isAnimating) return;
        
        Player current = playerQueue.poll(); // Ambil pemain terdepan
        isAnimating = true;
        rollButton.setEnabled(false);
        
        // LOGIC DADU 1-6
        int diceVal = new Random().nextInt(6) + 1;
        
        // LOGIC PROBABILITAS (80% Green/Maju, 20% Red/Mundur)
        double chance = new Random().nextDouble(); // 0.0 - 1.0
        boolean isGreen = chance < 0.8; 
        
        log(current.name + " rolls " + diceVal + " (" + (isGreen?"Green":"Red") + ")");
        
        // Animasi dadu dulu
        diceVisualizer.roll(diceVal, isGreen, () -> {
            movePlayer(current, diceVal, isGreen);
        });
    }
    
    private void movePlayer(Player p, int steps, boolean forward) {
        Timer timer = new Timer(300, null);
        final int[] stepCount = {0};
        
        timer.addActionListener(e -> {
            if (stepCount[0] < steps) {
                soundManager.playSound("step.wav");
                
                if(forward) {
                    p.position++;
                    if(p.position >= graph.nodes.length-1) p.position = graph.nodes.length-1;
                } else {
                    p.position--;
                    if(p.position < 1) p.position = 1;
                }
                
                boardPanel.setActivePlayer(p);
                boardPanel.repaint();
                stepCount[0]++;
                
                // Cek Win
                if(p.position == graph.nodes.length-1) {
                    ((Timer)e.getSource()).stop();
                    handleWin(p);
                    return;
                }
            } else {
                ((Timer)e.getSource()).stop();
                checkLanding(p);
            }
        });
        timer.start();
    }
    
    private void checkLanding(Player p) {
        Node currentNode = graph.getNode(p.position);
        
        // 1. Cek Score Bonus
        if (currentNode.scoreBonus > 0) {
            p.currentScore += currentNode.scoreBonus;
            log(p.name + " gets score +" + currentNode.scoreBonus + "!");
            soundManager.playSound("coin.wav");
        }
        
        // 2. Cek Shortcut (Ular/Tangga)
        if (currentNode.jumpTo != null) {
            log(p.name + " takes a shortcut!");
            p.position = currentNode.jumpTo.id;
            soundManager.playSound("jump.wav");
            boardPanel.repaint();
            
            // Cek lagi setelah lompat (siapa tau mendarat di bonus/bintang lagi)
            // Tapi untuk simplifikasi kita stop di sini
        }
        
        // 3. Cek BINTANG (Kelipatan 5) -> Double Turn
        // Logic Antrian: poll() sudah dilakukan di awal.
        // Kalau Double Turn -> addFirst (masuk depan lagi).
        // Kalau Biasa -> addLast (masuk belakang).
        
        if (currentNode.isStar) {
            log("STAR NODE! " + p.name + " gets Double Turn!");
            soundManager.playSound("powerup.wav");
            JOptionPane.showMessageDialog(this, "WOW! You landed on a Star!\nRoll Dice Again!");
            
            playerQueue.addFirst(p); // MASUKKAN KE DEPAN
        } else {
            playerQueue.addLast(p);  // MASUKKAN KE BELAKANG
        }
        
        isAnimating = false;
        rollButton.setEnabled(true);
        boardPanel.setActivePlayer(null); // Matikan highlight
        updateTurnInfo();
    }
    
    private void handleWin(Player winner) {
        soundManager.playSound("win.wav");
        winner.totalWins++;
        winner.currentScore += 100; // Bonus menang
        
        // Update High Score Record
        if(winner.currentScore > winner.highestScore) {
            winner.highestScore = winner.currentScore;
        }
        
        // Update Priority Queue Stats
        topScoresPQ.clear();
        topScoresPQ.addAll(allPlayersList);

        JOptionPane.showMessageDialog(this, 
            "CONGRATULATIONS!\n" + winner.name + " Wins!", 
            "Game Over", JOptionPane.INFORMATION_MESSAGE);
            
        int choice = JOptionPane.showConfirmDialog(this, "Play Again?", "Restart", JOptionPane.YES_NO_OPTION);
        if(choice == JOptionPane.YES_OPTION) {
            setupGame();
            logArea.setText("");
            log("Game Restarted.");
            isAnimating = false;
            rollButton.setEnabled(true);
            updateTurnInfo();
        } else {
            System.exit(0);
        }
    }
    
    private void showStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LEADERBOARD (Top Score) ===\n");
        
        // Clone PQ biar ga ngerusak aslinya saat di-poll
        PriorityQueue<Player> tempPQ = new PriorityQueue<>(topScoresPQ);
        int rank = 1;
        while(!tempPQ.isEmpty() && rank <= 3) {
            Player p = tempPQ.poll();
            sb.append(rank).append(". ").append(p.name)
              .append(" - HighScore: ").append(p.highestScore).append("\n");
            rank++;
        }
        
        sb.append("\n=== TOP WINNERS ===\n");
        // Sort manual list untuk win count
        ArrayList<Player> sortedByWins = new ArrayList<>(allPlayersList);
        sortedByWins.sort((p1, p2) -> Integer.compare(p2.totalWins, p1.totalWins));
        
        for(int i=0; i<Math.min(3, sortedByWins.size()); i++) {
            Player p = sortedByWins.get(i);
            sb.append((i+1)).append(". ").append(p.name)
              .append(" - Wins: ").append(p.totalWins).append("\n");
        }
        
        JOptionPane.showMessageDialog(this, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
}