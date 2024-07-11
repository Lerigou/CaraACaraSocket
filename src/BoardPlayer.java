import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class BoardPlayer implements Runnable {

    protected static final String HOST = "127.0.0.1";
    protected PlayersSocketServer playersSocketServer;
    protected Scanner scanner = new Scanner(System.in);
    protected String userName;
    protected boolean isMyTurn = true;
    protected List<String> characters = new ArrayList<>(List.of("Leah", "Abigail", "Sam", "Sebastian", "Robin", "Alex", "Junimo", "Prefeito Luis"));
    protected List<String> onBoardCharacters = new ArrayList<>(List.of("Leah", "Abigail", "Sam", "Sebastian", "Robin", "Alex", "Junimo", "Prefeito Luis"));

    public void start() throws IOException {
        playersSocketServer = new PlayersSocketServer(new Socket(HOST, BoardServer.PORT));
        System.out.println("Novo player conectado ao servidor.");
        new Thread(this).start();
        configUser();
        gameLoop();
    }

    public void configUser() {
        System.out.println("Bem vindo ao cara a cara da vila pelicanos!");
        System.out.println("Insira seu nome de usuário: ");
        userName = scanner.nextLine();
        System.out.println("Olá " + userName + "!");
    }

    public abstract void gameLoop();

    @Override
    public void run() {
        String message;
        while ((message = playersSocketServer.receiveQuestion()) != null) {
            handleMessage(message);
            synchronized (this) {
                isMyTurn = true;
                notifyAll();
            }
        }
    }

    protected void handleMessage(String message) {
        System.out.println("Mensagem do outro jogador: " + message);
    }

    public String getUserName() {
        return userName;
    }
}
