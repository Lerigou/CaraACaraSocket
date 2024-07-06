import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BoardServer {

    public static final int PORT = 12345;
    private ServerSocket serverSocket;
    private List<PlayersSocketServer> clientes = new ArrayList<PlayersSocketServer>();
    private List<String> characters = Arrays.asList("Leah", "Abigail", "Sam", "Sebastian");
    // salva os personagens escolhidos por cada jogador
    private List<String> chosenCharacters = new ArrayList<>(Arrays.asList(null, null));

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado na porta: " + PORT);
        waitConnections();
    }

    public void waitConnections() throws IOException {
        while(true) {
            PlayersSocketServer socket = new PlayersSocketServer(serverSocket.accept());
            System.out.println("Cliente: " + socket.getRemoteSocketAddress() + " conectado!");
            clientes.add(socket);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    waitMessages(socket);
                }
            };
            new Thread(runnable).start();
        }
    }

    public void waitMessages(PlayersSocketServer socket) {
        try {
            String question = "";
            while ((question = socket.receiveQuestion()) != null) {
                System.out.println("Mensagem recebida do cliente " +
                        socket.getRemoteSocketAddress() + " > " + question);
                sendMessagesForAll(socket, question);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessagesForAll(PlayersSocketServer cliente, String question) {
        for (PlayersSocketServer socket : clientes) {
            if (!cliente.equals(socket)) {
                socket.sendQuestion(question);
            }
        }
    }

    public static void main(String[] args) {
        try {
            BoardServer server = new BoardServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
