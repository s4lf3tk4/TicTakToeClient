import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


public class TicTak extends JComponent {

    public static final int FIELD_EMPTY = 0;
    public static final int FIELD_X = 10;
    public static final int FIELD_O = 100;
    int[][] field;
    boolean xTurn;
    boolean gameOver;
    private String playerSymbol;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String serverAddress;
    private final int port;

    public TicTak(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        field = new int[3][3];
        initGame();
        connectServer();
    }

    private void connectServer(){
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            playerSymbol = in.readLine();
            System.out.println("CONNECTED as " + playerSymbol);

            new Thread(new ServerListener()).start();
        }
        catch(IOException e){
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private class ServerListener implements Runnable{
        public void run(){
            try{
                String message;
                while((message = in.readLine()) != null){
                    processServerMessage(message);
                }
            }
            catch(IOException e){
                JOptionPane.showMessageDialog(TicTak.this, "Fail");
            }
        }
    }

    private void processServerMessage(String message){
        System.out.println("Server: "+ message);

        if (message.startsWith("BOARD:")){
            String boardData = message.substring(6);
            updateBoard(boardData);
            repaint();
        }
        else if (message.startsWith("TURN:")){
            String turn = message.substring(5);
            xTurn = turn.equals(playerSymbol);
            repaint();
            gameOver = false;
        }
        else if (message.startsWith("WINNER:")){
            String winner = message.substring(7);
            gameOver = true;

            SwingUtilities.invokeLater(()->{
                if(winner.equals("DRAW")){
                    JOptionPane.showMessageDialog(this, "Ничья!", "Результат", JOptionPane.INFORMATION_MESSAGE);
                }
                else if (winner.equals(playerSymbol)){
                    JOptionPane.showMessageDialog(this, "Вы Выиграли!", "Победа!", JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    JOptionPane.showMessageDialog(this, "Вы Проиграли!", "Проиграли", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
        else if (message.startsWith("ERROR:")) {
            String error = message.substring(6);
            JOptionPane.showMessageDialog(this, error, "Проиграли", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateBoard(String boardData){
        String[] cells = boardData.split(",");
        int index = 0;
        for (int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                field[i][j]=Integer.parseInt(cells[index++]);
            }
        }
    }
    private void sendMove(int i, int j){
        if(out != null){
            out.println("MOVE:" + i + "," + j);
        }
    }

    public void diconnect(){
        try{
            if(out != null) out.close();
            if(in != null) in.close();
            if(socket != null) socket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = FIELD_EMPTY;
            }
        }
        xTurn = false;
        gameOver = false;
    }



    protected void processMouseEvent(MouseEvent mouseEvent) {
        super.processMouseEvent(mouseEvent);

        if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED &&
                mouseEvent.getButton() == MouseEvent.BUTTON1 &&
                !gameOver && xTurn) {

            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            int i = (int) ((float) x / getWidth() * 3);
            int j = (int) ((float) y / getHeight() * 3);

            // Проверяем границы
            if (i >= 0 && i < 3 && j >= 0 && j < 3) {
                if (field[i][j] == FIELD_EMPTY) {
                    sendMove(i, j);
                } else {
                    // Клетка уже занята
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

    void drawX(int i, int j, Graphics graphics) {
        graphics.setColor(Color.RED);
        int dw = getWidth() / 3;
        int dh = getHeight() / 3;
        int x = i * dw;
        int y = j * dh;
        int padding = 20;

        graphics.drawLine(x + padding, y + padding, x + dw - padding, y + dh - padding);
        graphics.drawLine(x + dw - padding, y + padding, x + padding, y + dh - padding);
    }

    void drawO(int i, int j, Graphics graphics) {
        graphics.setColor(Color.BLUE);
        int dw = getWidth() / 3;
        int dh = getHeight() / 3;
        int x = i * dw;
        int y = j * dh;
        int padding = 20;

        graphics.drawOval(x + padding, y + padding, dw - 2 * padding, dh - 2 * padding);
    }

    void drawXO(Graphics graphics) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j] == FIELD_X) {
                    drawX(i, j, graphics);
                } else if (field[i][j] == FIELD_O) {
                    drawO(i, j, graphics);
                }
            }
        }
    }

    int checkState() {
        // Проверка строк и столбцов
        for (int i = 0; i < 3; i++) {
            int rowSum = field[i][0] + field[i][1] + field[i][2];
            int colSum = field[0][i] + field[1][i] + field[2][i];

            if (rowSum == FIELD_X * 3 || rowSum == FIELD_O * 3) {
                return rowSum;
            }
            if (colSum == FIELD_X * 3 || colSum == FIELD_O * 3) {
                return colSum;
            }
        }

        // Проверка диагоналей
        int diag1 = field[0][0] + field[1][1] + field[2][2];
        int diag2 = field[0][2] + field[1][1] + field[2][0];

        if (diag1 == FIELD_X * 3 || diag1 == FIELD_O * 3) {
            return diag1;
        }
        if (diag2 == FIELD_X * 3 || diag2 == FIELD_O * 3) {
            return diag2;
        }

        // Проверка на ничью
        boolean hasEmpty = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j] == FIELD_EMPTY) {
                    hasEmpty = true;
                    break;
                }
            }
            if (hasEmpty) break;
        }

        return hasEmpty ? 0 : -1;
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawGrind(graphics);
        drawXO(graphics);
    }

    void drawGrind(Graphics graphics) {
        int width = getWidth();
        int height = getHeight();
        int dw = width / 3;
        int dh = height / 3;
        graphics.setColor(Color.BLACK);


        for (int i = 1; i < 3; i++) {
            graphics.drawLine(0, dh * i, width, dh * i);
            graphics.drawLine(dw * i, 0, dw * i, height);
        }
    }
}
