import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class IoServer {
    public static void main(String[] args) throws IOException {
        int port = 45000;

        log("Server started at port " + port  + "\nWaiting for connections...");

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handle(clientSocket)).start();
            }
        }
    }

    private static void handle(Socket socket) {
        log("Client connected: " + socket.getRemoteSocketAddress());

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // Блокирование на получение данных от клиента
            String clientRequest = reader.readLine();

            log("Received from " + socket.getRemoteSocketAddress() + " > " + clientRequest);

            String serverResponse = clientRequest + ", occurredOn=" + System.currentTimeMillis();
            PrintWriter writer = new PrintWriter(out);

            // Блокирование на отправку данных клиенту
            writer.write(serverResponse);
            writer.flush();

            log("Sent response to " + socket.getRemoteSocketAddress() + " > " + serverResponse);

            socket.close();
        } catch (IOException e) {
            log(e.getMessage());
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }
}