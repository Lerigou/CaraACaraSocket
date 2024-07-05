import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BoardServer {

    public static final int PORT = 12345;
    private ServerSocket serverSocket;
    private List<PlayersSocketServer> clientes = new ArrayList<PlayersSocketServer>();
    private List<String> userNames = new CopyOnWriteArrayList<>();

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado na porta: " + PORT);
        esperarConexoes();
    }

    public void esperarConexoes() throws IOException {
        while(true) {
            PlayersSocketServer socket = new PlayersSocketServer(serverSocket.accept());
            System.out.println("Cliente: " + socket.getRemoteSocketAddress() + " conectado!");
            clientes.add(socket);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    esperarMensagens(socket);
                }
            };
            new Thread(runnable).start();
        }
    }

    public void esperarMensagens(PlayersSocketServer socket) {
        try {
            String question = "";
            while ((question = socket.receiveQuestion()) != null) {
                System.out.println("Mensagem recebida do cliente " +
                        socket.getRemoteSocketAddress() + " > " + question);
                enviarMensagemParaTodosClientes(socket, question);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarMensagemParaTodosClientes(PlayersSocketServer cliente, String question) {
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
