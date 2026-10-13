package oracolo;

import java.io.IOException;
import java.net.Socket;

public class Threads {
    private final Socket socket;

    public Threads(Socket socket) {
        this.socket = socket;
    }

    public void avvia() throws IOException {
        ThreadInvio invio = new ThreadInvio(socket);
        ThreadRicezione ricezione = new ThreadRicezione(socket);
        invio.setDaemon(true);
        invio.start();
        ricezione.start();
        try {
            ricezione.join();
            invio.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            socket.close();
        }
    }
}
