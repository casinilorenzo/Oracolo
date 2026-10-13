package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThreadRicezione extends Thread {
    private final Socket socket;

    public ThreadRicezione(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                System.out.println("Oracolo: " + messaggio);
            }
        } catch (IOException e) {
            if (!socket.isClosed()) System.out.println("Errore ricezione: " + e.getMessage());
        } finally {
            System.out.println("Connessione terminata.");
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Errore chiusura: " + e.getMessage());
            }
        }
    }
}
