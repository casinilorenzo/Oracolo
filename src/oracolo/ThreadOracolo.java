package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gestisce la conversazione con un singolo client: a ogni domanda
 * risponde con una frase predefinita scelta a caso, come una palla 8.
 */
public class ThreadOracolo extends Thread {
    private static final String[] RISPOSTE = {
        // positive
        "È certo.",
        "È decisamente così.",
        "Senza dubbio.",
        "Sì, senza dubbio.",
        "Puoi contarci.",
        "Per come la vedo io, sì.",
        "Molto probabilmente.",
        "Le prospettive sono buone.",
        "Sì.",
        "I segni indicano di sì.",
        // incerte
        "Risposta confusa, riprova.",
        "Chiedimelo più tardi.",
        "Meglio non dirtelo ora.",
        "Non posso prevederlo adesso.",
        "Concentrati e chiedi di nuovo.",
        // negative
        "Non contarci.",
        "La mia risposta è no.",
        "Le mie fonti dicono di no.",
        "Le prospettive non sono buone.",
        "È molto dubbio."
    };

    private final Socket socket;

    public ThreadOracolo(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String client = socket.getInetAddress() + ":" + socket.getPort();
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8)) {

            out.println("Sono l'Oracolo. Fai una domanda (scrivi 'fine' per uscire).");

            String domanda;
            while ((domanda = in.readLine()) != null) {
                domanda = domanda.trim();
                if (domanda.equalsIgnoreCase("fine")) break;
                if (domanda.isEmpty()) {
                    out.println("Devi farmi una domanda.");
                    continue;
                }
                System.out.println("Domanda da " + client + ": " + domanda);
                out.println(rispondi());
                if (out.checkError()) break; // il client si e disconnesso
            }
        } catch (IOException e) {
            System.out.println("Errore con " + client + ": " + e.getMessage());
        }
        System.out.println("Client disconnesso: " + client);
    }

    private static String rispondi() {
        return RISPOSTE[ThreadLocalRandom.current().nextInt(RISPOSTE.length)];
    }
}
