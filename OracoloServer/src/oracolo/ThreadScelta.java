package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Gestisce la connessione di un client: chiede quale oracolo vuole,
 * gli passa la conversazione e, se il client scrive "cambia", torna
 * al menu per scegliere un'altra personalita'.
 */
public class ThreadScelta extends Thread {
    private static final String MENU =
            "Scegli l'oracolo: 1 = Classico, 2 = Romano antico, "
            + "3 = Imprenditore milanese, 4 = Trio comico (scrivi 'fine' per uscire).";

    private final Socket socket;

    public ThreadScelta(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String client = socket.getInetAddress() + ":" + socket.getPort();
        try (Socket s = socket) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);

            out.println(MENU);

            String scelta;
            while ((scelta = in.readLine()) != null) {
                scelta = scelta.trim();
                if (scelta.equalsIgnoreCase("fine")) break;

                ThreadOracolo oracolo = creaOracolo(scelta);
                if (oracolo == null) {
                    out.println("Scelta non valida: scrivi un numero da 1 a 4.");
                    continue;
                }

                // conversa() e non start(): questo thread e gia dedicato a questo client.
                ThreadOracolo.Esito esito = oracolo.conversa(in, out);
                if (esito == ThreadOracolo.Esito.FINE) break;

                // CAMBIA: si torna al menu.
                out.println(MENU);
            }
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
            default:  return null;
        }
    }
}
