package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThreadInvio extends Thread {
    private static final BufferedReader TASTIERA =
            new BufferedReader(new InputStreamReader(System.in));

    private final Socket socket;

    public ThreadInvio(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            while (!socket.isClosed()) {
                // Legge dalla tastiera solo se c'e qualcosa, cosi il thread puo terminare
                // quando la connessione finisce.
                if (!TASTIERA.ready()) {
                    Thread.sleep(100);
                    continue;
                }
                String messaggio = TASTIERA.readLine();
                if (messaggio == null) break;
                out.println(messaggio);
                if (out.checkError() || messaggio.equalsIgnoreCase("fine")) {
                    socket.close();
                    break;
                }
            }
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            if (!socket.isClosed()) System.out.println("Errore invio: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
