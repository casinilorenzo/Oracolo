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
 * Le altre personalita (ThreadRomano, ThreadImprenditore, ThreadTrio)
 * estendono questa classe e cambiano solo il saluto e le risposte.
 */
public class ThreadOracolo extends Thread {
    private static final String SALUTO_CLASSICO =
            "Sono l'Oracolo. Fai una domanda (scrivi 'fine' per uscire).";

    private static final String[] RISPOSTE_CLASSICHE = {
        // positive
        "E certo.",
        "E decisamente cosi.",
        "Senza dubbio.",
        "Si, senza dubbio.",
        "Puoi contarci.",
        "Per come la vedo io, si.",
        "Molto probabilmente.",
        "Le prospettive sono buone.",
        "Si.",
        "I segni indicano di si.",
        // incerte
        "Risposta confusa, riprova.",
        "Chiedimelo piu tardi.",
        "Meglio non dirtelo ora.",
        "Non posso prevederlo adesso.",
        "Concentrati e chiedi di nuovo.",
        // negative
        "Non contarci.",
        "La mia risposta e no.",
        "Le mie fonti dicono di no.",
        "Le prospettive non sono buone.",
        "E molto dubbio."
    };

    private final Socket socket;
    private final String saluto;
    private final String[] risposte;

    /** Oracolo classico. */
    public ThreadOracolo(Socket socket) {
        this(socket, SALUTO_CLASSICO, RISPOSTE_CLASSICHE);
    }

    /** Usato dalle personalita che estendono questa classe. */
    protected ThreadOracolo(Socket socket, String saluto, String[] risposte) {
        this.socket = socket;
        this.saluto = saluto;
        this.risposte = risposte;
    }

    @Override
    public void run() {
        String client = socket.getInetAddress() + ":" + socket.getPort();
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8)) {

            out.println(saluto);

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

    private String rispondi() {
        return risposte[ThreadLocalRandom.current().nextInt(risposte.length)];
    }
}
