import java.awt.Color;

public class Player {
    String id;
    String name;
    int position;
    Color color;
    int currentScore; // Skor match saat ini
    int totalWins;    // History kemenangan
    int highestScore; // History skor tertinggi

    public Player(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.position = 1;
        this.currentScore = 0;
        this.totalWins = 0;
        this.highestScore = 0;
    }
    
    // Reset untuk game baru (tapi win count tetap disimpan)
    public void resetForNewGame() {
        if (currentScore > highestScore) highestScore = currentScore;
        this.position = 1;
        this.currentScore = 0;
    }
}