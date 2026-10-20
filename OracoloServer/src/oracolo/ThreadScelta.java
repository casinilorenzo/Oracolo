package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Prima parte della connessione: chiede al client quale oracolo vuole
 * e poi passa la conversazione alla personalita scelta.
 */
public class ThreadScelta extends Thread {
    private final Socket socket;

    public ThreadScelta(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String client = socket.getInetAddress() + ":" + socket.getPort();
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            out.println("Scegli l'oracolo: 1 = Classico, 2 = Romano antico, "
                    + "3 = Imprenditore milanese, 4 = Trio comico (scrivi 'fine' per uscire).");

            String scelta;
            while ((scelta = in.readLine()) != null) {
                scelta = scelta.trim();
                if (scelta.equalsIgnoreCase("fine")) break;
                ThreadOracolo oracolo = creaOracolo(scelta);
                if (oracolo != null) {
                    // run() e non start(): questo thread e gia dedicato a questo client.
                    // L'oracolo scelto chiude il socket quando la conversazione finisce.
                    oracolo.run();
                    return;
                }
                out.println("Scelta non valida: scrivi un numero da 1 a 4.");
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Errore con " + client + ": " + e.getMessage());
        }
        System.out.println("Client disconnesso: " + client);
    }

    private ThreadOracolo creaOracolo(String scelta) {
        switch (scelta) {
            case "1": return new ThreadOracolo(socket);
            case "2": return new ThreadRomano(socket);
            case "3": return new ThreadImprenditore(socket);
            case "4": return new ThreadTrio(socket);
            default:  return null;
        }
    }
}
