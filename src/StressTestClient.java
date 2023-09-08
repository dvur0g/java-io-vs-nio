import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StressTestClient {
    public static void main(String[] args) {
        List<Socket> sockets = new ArrayList<>();

        log("Opening sockets...");

        for (int i = 0; i < 1000; ++i) {
            try {
                sockets.add(new Socket("localhost", 45000));
            } catch (IOException e) {
                log(e.getMessage());
            }
        }

        log("Enter any string to quit...");

        new Scanner(System.in).next();

        log("Closing connections...");

        sockets.forEach(s -> {
            try {
                s.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private static void log(String message) {
        System.out.println(message);
    }
}