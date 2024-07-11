import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class BoardPlayer1 implements Runnable {

    private static final String HOST = "127.0.0.1";
    private PlayersSocketServer playersSocketServer;
    private Scanner scanner = new Scanner(System.in);
    private boolean isMyTurn = true;
    private boolean hasAskedQuestion = false;
    private boolean hasReceivedAnswer = false;

    public void start() throws IOException {
        playersSocketServer = new PlayersSocketServer(new Socket(HOST, BoardServer.PORT));
        System.out.println("Novo player conectado ao servidor.");
        new Thread(this).start();
        gameLoop();
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

            if (hasReceivedAnswer) {
                playerAction();
            } else if (hasAskedQuestion) {
                System.out.println("Aguardando resposta do outro jogador...");
            } else {
                sendQuestion();
            }

            isMyTurn = false;
            notifyAll();
        }
    }

    public void sendQuestion() {
        System.out.println("Insira a pergunta para o outro jogador: ");
        String question = scanner.nextLine();
        playersSocketServer.sendQuestion(question);
        hasAskedQuestion = true;
    }

    public void playerAction() {
        System.out.println("Você recebeu uma resposta do outro jogador. Escolha uma ação.");
        hasReceivedAnswer = false;
    }

    public static void main(String[] args) {
        try {
            BoardPlayer1 client = new BoardPlayer1();
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
            handleAnswer(message);
            synchronized (this) {
                isMyTurn = true;
                notifyAll();
            }
        }
    }

    private void handleAnswer(String message) {
        System.out.println("Resposta do outro jogador: " + message);
        hasReceivedAnswer = true;
    }
}
