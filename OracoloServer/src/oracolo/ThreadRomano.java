package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Oracolo che risponde come un antico romano.
 * Dopo 3 o 4 domande si ferma e ti sfida al Colosseo: scrivi un numero da I a X
 * in numeri romani e lui ne estrae uno a caso. Se sono uguali hai perso e devi
 * cambiare personalita'; altrimenti puoi continuare a parlare con lui.
 */
public class ThreadRomano extends ThreadOracolo {
    private static final String[] NUMERI_ROMANI = {
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    private static final String SALUTO =
            "Ave, cittadino! Sono l'Oracolo di Roma. Poni la tua domanda "
            + "(scrivi 'cambia' per cambiare oracolo, 'fine' per congedarti).";

    private static final String[] RISPOSTE = {
        // positive
        "Cosi hanno decretato gli dei: e certo.",
        "Ave! Gli auspici sono favorevoli.",
        "Alea iacta est: procedi senza timore.",
        "Per Giove, senza alcun dubbio.",
        "Le aquile volano a tuo favore.",
        "Veni, vidi, vici: andra tutto bene.",
        "Il Senato approva: si.",
        "Gli aruspici hanno letto le viscere: esito felice.",
        "Gli dei sorridono, o cittadino.",
        "Vincerai, come le legioni di Cesare.",
        // incerte
        "Le viscere del pollo sono poco chiare: sacrifica e richiedi.",
        "Gli dei dormono, interrogali al mattino.",
        "Il fumo dell'incenso e confuso, riprova.",
        "Giove tace per ora.",
        "Attendi il prossimo mercato e chiedi ancora.",
        // negative
        "Per Plutone, no.",
        "Gli auguri sono funesti.",
        "Neanche Ercole ci riuscirebbe: la risposta e no.",
        "Il Senato respinge la richiesta.",
        "I corvi volano a sinistra: pessimo segno."
    };

    // Quante risposte restano prima della sfida al Colosseo.
    private int domandeRimaste = nuovoLimite();

    public ThreadRomano(Socket socket) {
        super(socket, SALUTO, RISPOSTE);
    }

    /** 3 o 4 domande. */
    private static int nuovoLimite() {
        return ThreadLocalRandom.current().nextInt(3, 5);
    }

    @Override
    protected Esito gestisci(String domanda, BufferedReader in, PrintWriter out) throws IOException {
        if (domandeRimaste > 0) {
            domandeRimaste--;
            out.println(rispondi());
            return Esito.CONTINUA;
        }
        return sfidaColosseo(in, out);
    }

    /** Duello al Colosseo: se i due numeri coincidono, l'utente perde e deve cambiare oracolo. */
    private Esito sfidaColosseo(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Fermati, cittadino! Hai osato troppo: ti sfido nel Colosseo! "
                + "Scrivi un numero da I a X in numeri romani. Io ne estraggo uno: "
                + "se e uguale al tuo hai perso e dovrai lasciare l'Oracolo di Roma, "
                + "altrimenti potrai continuare a parlare con me. "
                + "(Oppure scrivi 'cambia' per fuggire dall'arena.)");

        String riga;
        while ((riga = in.readLine()) != null) {
            riga = riga.trim();
            if (riga.equalsIgnoreCase("fine")) return Esito.FINE;
            if (riga.equalsIgnoreCase("cambia")) return Esito.CAMBIA;

            int tuo = indiceRomano(riga);
            if (tuo < 0) {
                out.println("Nel Colosseo si parla romano: scrivi un numero da I a X.");
                continue;
            }

            int mio = ThreadLocalRandom.current().nextInt(NUMERI_ROMANI.length);
            out.println("Tu hai scelto " + NUMERI_ROMANI[tuo] + ", io ho estratto " + NUMERI_ROMANI[mio] + ".");

            if (tuo == mio) {
                out.println("Gli stessi numeri! Hai perso, o cittadino: lascia l'Oracolo di Roma "
                        + "e scegline un altro.");
                return Esito.CAMBIA;
            }
            out.println("Numeri diversi: la folla ti acclama, hai vinto! Puoi continuare. "
                    + "Ecco la risposta alla tua domanda:");
            domandeRimaste = nuovoLimite() - 1; // questa risposta conta come la prima
            out.println(rispondi());
            return Esito.CONTINUA;
        }
        return Esito.FINE;
    }

    /** Posizione del numero romano da I a X (0-9), oppure -1 se non valido. */
    private static int indiceRomano(String testo) {
        String t = testo.toUpperCase();
        for (int i = 0; i < NUMERI_ROMANI.length; i++) {
            if (NUMERI_ROMANI[i].equals(t)) return i;
        }
        return -1;
    }
}
