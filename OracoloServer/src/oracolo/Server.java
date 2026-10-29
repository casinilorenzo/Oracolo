package oracolo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        final int PORT = 5555;
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Oracolo in ascolto sulla porta " + PORT);
            // Il server resta acceso e ogni client e gestito da un thread proprio.
            // Per spegnerlo, ferma il programma (Stop in NetBeans o Ctrl+C).
            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connesso: " + socket.getInetAddress() + ":" + socket.getPort());
                new ThreadScelta(socket).start();
            }
        } catch (IOException e) {
            System.out.println("Errore server: " + e.getMessage());
        }
    }
}
