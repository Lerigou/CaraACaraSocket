import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class BoardPlayer1 implements Runnable{

    private static final String HOST = "127.0.0.1";
    private PlayersSocketServer playersSocketServer;
    private Scanner scanner = new Scanner(System.in);
    private String userName;
    private String userChar;
    private boolean isMyTurn = true;
    private boolean firstRound = true;

    private String[] characters = {"Leah", "Abigail", "Sam", "Sebastian", "Robin", "Alex", "Junimo", "Prefeito Luis"};

    public void start() throws IOException {
        playersSocketServer = new PlayersSocketServer(new Socket(HOST, BoardServer.PORT));
        System.out.println("Novo player conectado ao servidor.");
        new Thread(this).start();
        configUser();
        sendQuestion();
    }

    public void configUser(){
        System.out.println("Bem vindo ao cara a cara da vila pelicanos!");
        System.out.println("Insira seu nome de usuário: ");
        userName = scanner.nextLine();


        System.out.println("Escolha seu personagem:");
        for (int i = 0; i < characters.length; i++) {
            System.out.println((i + 1) + ". " + characters[i]);
        }
        int choice = scanner.nextInt();
        userChar = characters[choice - 1];
        scanner.nextLine();

        System.out.println("Olá " + userName + "! Você escolheu o personagem " + userChar);
    }

    public synchronized void sendQuestion(){
        String question, answer = "";
        while (true) {
            while (!isMyTurn) {
                try {
                    System.out.println("Vez do outro jogador, aguarde!");
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (firstRound) {
                // Primeira rodada: envia apenas a pergunta
                System.out.println("Insira a pergunta para o outro jogador: ");
                question = scanner.nextLine();
                playersSocketServer.sendQuestion(question);
                firstRound = false;
            } else {
                System.out.println("LEMBRETE: Você escolheu o personagem " + userChar + "\n");
                // Rodadas subsequentes: primeiro responde a pergunta recebida
                System.out.println("Insira a resposta para o outro jogador: ");
                answer = scanner.nextLine();
                playersSocketServer.sendQuestion(answer);

                // Depois faz a própria pergunta
                System.out.println("Insira a pergunta para o outro jogador: ");
                question = scanner.nextLine();
                playersSocketServer.sendQuestion(question);
            }

//            if (question.equalsIgnoreCase("sair")) {
//                break;
//            }

            isMyTurn = false;
            notifyAll();
        }

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
        String question = "";
        while((question = playersSocketServer.receiveQuestion()) != null) {
            System.out.println("Pergunta do outro player: " + question);
            // após receber a pergunta do outro jogador, ele informa que é a vez dele
            synchronized (this) {
                isMyTurn = true;
                notifyAll();
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getUserChar() {
        return userChar;
    }
}
