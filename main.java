import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        String serverAdress = JOptionPane.showInputDialog("Введите адрес сервера: ");
        if (serverAdress == null){
            serverAdress = "localhost";
        }

        String portStr = JOptionPane.showInputDialog("Port:");
        int port = 0;
        if (portStr != null){
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Неверный формат порта! Используется порт по умолчанию.");
                port = 8080; // порт по умолчанию
            }
        }

        JFrame window = new JFrame("TicTok");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(500, 500);
        window.setLayout(new BorderLayout());
        window.setLocationRelativeTo(null);

        TicTak game = new TicTak(serverAdress, port);
        window.add(game);

        window.setVisible(true);

        window.revalidate();
        window.repaint();
    }
}
