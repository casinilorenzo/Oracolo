package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gestisce la conversazione con un singolo client: a ogni domanda
 * risponde con una frase predefinita scelta a caso, come una palla 8.
 * Le altre personalita (ThreadRomano, ThreadImprenditore)
 * estendono questa classe: cambiano saluto e risposte e, se serve,
 * ridefiniscono gestisci() per avere un comportamento tutto loro.
 *
 * Non scrive piu' sul socket in modo autonomo: usa i flussi ricevuti da
 * ThreadScelta, cosi' si puo' tornare al menu senza perdere la connessione.
 */
public class ThreadOracolo extends Thread {
    /** Come finisce la conversazione con questa personalita'. */
    public enum Esito { CONTINUA, FINE, CAMBIA }

    public static final String DESCRIZIONE_CLASSICO =
            "Oracolo Classico: risponde a ogni domanda con una frase a caso (si, no o forse), "
            + "come una palla 8. Nessuna regola particolare.";

    private static final String SALUTO_CLASSICO =
            "Sono l'Oracolo. Fai una domanda (scrivi 'help' per l'aiuto, 'cambia' per cambiare oracolo, 'fine' per uscire).";

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

    /**
     * Conversa con il client finche' scrive "fine" (esce) o "cambia" (torna al menu).
     * "help" mostra l'aiuto e la conversazione prosegue.
     * Non chiude il socket: ci pensa ThreadScelta.
     */
    public Esito conversa(BufferedReader in, PrintWriter out) throws IOException {
        String client = socket.getInetAddress() + ":" + socket.getPort();
        out.println(saluto);

        String domanda;
        while ((domanda = in.readLine()) != null) {
            domanda = domanda.trim();
            if (domanda.equalsIgnoreCase("fine")) return Esito.FINE;
            if (domanda.equalsIgnoreCase("cambia")) return Esito.CAMBIA;
            if (domanda.equalsIgnoreCase("help")) {
                stampaAiuto(out);
                continue;
            }
            if (domanda.isEmpty()) {
                out.println("Devi farmi una domanda.");
                continue;
            }
            System.out.println("Domanda da " + client + ": " + domanda);
            Esito esito = gestisci(domanda, in, out);
            if (out.checkError()) return Esito.FINE; // il client si e disconnesso
            if (esito != Esito.CONTINUA) return esito;
        }
        return Esito.FINE;
    }

    /**
     * Cosa fare a ogni domanda. Di base risponde a caso; le personalita
     * possono ridefinirlo (l'imprenditore, per esempio, chiede soldi).
     */
    protected Esito gestisci(String domanda, BufferedReader in, PrintWriter out) throws IOException {
        out.println(rispondi());
        return Esito.CONTINUA;
    }

    protected String rispondi() {
        return risposte[ThreadLocalRandom.current().nextInt(risposte.length)];
    }

    /** Descrizione di questa personalita': le sottoclassi la ridefiniscono. */
    protected String descrizione() {
        return DESCRIZIONE_CLASSICO;
    }

    /** Aiuto durante la conversazione: comandi + regole dell'oracolo attuale. */
    protected void stampaAiuto(PrintWriter out) {
        stampaComandi(out);
        out.println("Oracolo attuale -> " + descrizione());
    }

    /** Elenco dei comandi disponibili, uguale per tutti. */
    public static void stampaComandi(PrintWriter out) {
        out.println("=== AIUTO ===");
        out.println("Come funziona: scegli un oracolo dal menu (scrivendo il suo numero), "
                + "poi fagli una domanda a cui si puo rispondere si o no: lui ti risponde.");
        out.println("Comandi (puoi scriverli in qualsiasi momento, anche maiuscoli):");
        out.println("  help   -> mostra questo aiuto");
        out.println("  cambia -> torna al menu per scegliere un altro oracolo");
        out.println("  fine   -> chiude la connessione ed esce");
        out.println("Nel menu: 1, 2 o 3 per scegliere l'oracolo, 'help' per l'aiuto, 'fine' per uscire.");
    }
}
