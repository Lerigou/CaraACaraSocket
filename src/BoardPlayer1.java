import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BoardPlayer1 implements Runnable {

    private static final String HOST = "127.0.0.1";
    private PlayersSocketServer playersSocketServer;
    private Scanner scanner = new Scanner(System.in);
    private boolean isMyTurn = true;
    private boolean hasAskedQuestion = false;
    private boolean hasReceivedAnswer = false;
    private List<String> onBoardCharacters = new ArrayList<>(List.of("Leah", "Abigail", "Sam", "Sebastian", "Robin", "Alex", "Junimo", "Prefeito Luis"));

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
        System.out.println("\n" +
                "\n1. Abaixar personagem" +
                "\n2. Dar palpite" +
                "\n3. Não fazer nada");
        int action = scanner.nextInt();
        scanner.nextLine();

        switch (action) {
            case 1:
                lowerCharacter();
                sendQuestion();
                break;
            case 2:
                guessCharacter();
                break;
            case 3:
                System.out.println("Você optou por não fazer nada nesta rodada.");
                break;
            default:
                System.out.println("Opção inválida. Tente novamente.");
                playerAction();
        }
    }
    public void lowerCharacter() {
        while (true) {
            System.out.println("Personagens disponíveis no tabuleiro:");
            for (int i = 0; i < onBoardCharacters.size(); i++) {
                System.out.println((i + 1) + ". " + onBoardCharacters.get(i));
            }
            System.out.println("\nInsira o número do personagem que deseja abaixar (ou 0 para parar): ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 0) {
                break;
            } else if (choice > 0 && choice <= onBoardCharacters.size()) {
                System.out.println("Você abaixou o personagem: " + onBoardCharacters.remove(choice - 1));
            } else {
                System.out.println("Personagem inválido. Tente novamente.");
            }
        }
    }

    public void guessCharacter() {
        System.out.println("\nPersonagens disponíveis no tabuleiro:");
        for (int i = 0; i < onBoardCharacters.size(); i++) {
            System.out.println((i + 1) + ". " + onBoardCharacters.get(i));
        }
        System.out.println("\nInsira o número do personagem para realizar o palpite: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        String guessedCharacter = onBoardCharacters.get(choice - 1);
        playersSocketServer.sendQuestion(guessedCharacter);
        System.out.println("Esperando resposta do adversário...");
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
