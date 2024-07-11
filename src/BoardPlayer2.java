import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BoardPlayer2 implements Runnable {

    private static final String HOST = "127.0.0.1";
    private PlayersSocketServer playersSocketServer;
    private Scanner scanner = new Scanner(System.in);
    private String userChar;
    private boolean isMyTurn = false;
    private List<String> characters = new ArrayList<>(List.of("Leah", "Abigail", "Sam", "Sebastian", "Robin", "Alex", "Junimo", "Prefeito Luis"));

    public void start() throws IOException {
        playersSocketServer = new PlayersSocketServer(new Socket(HOST, BoardServer.PORT));
        System.out.println("Novo player conectado ao servidor.");
        new Thread(this).start();
        chooseCharacter();
        gameLoop();
    }

    public void chooseCharacter() {
        System.out.println("Escolha seu personagem:");
        for (int i = 0; i < characters.size(); i++) {
            System.out.println((i + 1) + ". " + characters.get(i));
        }
        int choice = scanner.nextInt();
        userChar = characters.get(choice - 1);
        scanner.nextLine();

        System.out.println("Você escolheu o personagem " + userChar);
    }

    public synchronized void gameLoop() {
        while (true) {
            while (!isMyTurn) {
                try {
                    System.out.println("Vez do outro jogador, aguarde!");
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (isMyTurn) {
                answerQuestion();
            }

            isMyTurn = false;
            notifyAll();
        }
    }

    public void answerQuestion() {
        System.out.println("Você recebeu uma pergunta. Insira a resposta: ");
        String answer = scanner.nextLine();
        playersSocketServer.sendQuestion(answer);
    }

    public static void main(String[] args) {
        try {
            BoardPlayer2 client = new BoardPlayer2();
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Cliente finalizado!");
    }

    @Override
    public void run() {
        String message;
        while ((message = playersSocketServer.receiveQuestion()) != null) {
            System.out.println("Pergunta recebida do outro jogador: " + message);
            synchronized (this) {
                isMyTurn = true;
                notifyAll();
            }
        }
    }
}
