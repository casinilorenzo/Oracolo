package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gestisce la conversazione con un singolo client: a ogni domanda
 * risponde con una frase presa dal file oracoli.json, scelta a caso.
 * Le altre personalita estendono questa classe e ridefiniscono gestisci().
 */
public class ThreadOracolo extends Thread {
    /** Come finisce la conversazione con questa personalita. */
    public enum Esito { CONTINUA, FINE, CAMBIA }

    private final Socket socket;
    private final String saluto;
    private final String[] risposte;
    private final String descrizione;

    /** Oracolo classico. */
    public ThreadOracolo(Socket socket) {
        this(socket, "classico");
    }

    /** Usato da tutte le personalita: la chiave e quella presente in oracoli.json. */
    protected ThreadOracolo(Socket socket, String chiave) {
        Oracoli.Dati dati = Oracoli.get(chiave);
        this.socket = socket;
        this.saluto = dati.saluto;
        this.risposte = dati.risposte.toArray(new String[0]);
        this.descrizione = dati.descrizione;
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
            if (out.checkError()) return Esito.FINE;
            if (esito != Esito.CONTINUA) return esito;
        }
        return Esito.FINE;
    }

    /** Di base risponde a caso; le personalita possono ridefinirlo. */
    protected Esito gestisci(String domanda, BufferedReader in, PrintWriter out) throws IOException {
        out.println(rispondi());
        return Esito.CONTINUA;
    }

    protected String rispondi() {
        return risposte[ThreadLocalRandom.current().nextInt(risposte.length)];
    }

    protected String descrizione() {
        return descrizione;
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
