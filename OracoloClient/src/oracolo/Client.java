package oracolo;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        // Senza argomenti si collega al server sullo stesso computer.
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        final int PORT = 5555;
        try (Socket socket = new Socket(host, PORT)) {
            System.out.println("Connesso al server " + host + ":" + PORT);
            new Threads(socket).avvia();
            System.out.println("Client terminato.");
        } catch (IOException e) {
            System.out.println("Errore client: " + e.getMessage());
        }
    }
}
