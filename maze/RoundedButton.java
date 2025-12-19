import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    public RoundedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(new Font("Comic Sans MS", Font.BOLD, 13));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Agar grafik halus (Anti-aliasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Warna saat ditekan vs diam
        if (getModel().isArmed()) {
            g2.setColor(getBackground().darker());
        } else {
            g2.setColor(getBackground());
        }
        
        // Gambar tombol bulat
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
        
        // Hitung posisi teks agar pas di tengah (Manual Centering)
        FontMetrics fm = g2.getFontMetrics();
        Rectangle stringBounds = fm.getStringBounds(getText(), g2).getBounds();
        
        int textX = (getWidth() - stringBounds.width) / 2;
        int textY = (getHeight() - stringBounds.height) / 2 + fm.getAscent();
        
        g2.setColor(getForeground());
        g2.drawString(getText(), textX, textY);
        
        g2.dispose();
        
        // JANGAN PANGGIL super.paintComponent(g)! 
        // Ini yang bikin tulisan jadi dobel/jelek.
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // Kosongkan border bawaan
    }
}