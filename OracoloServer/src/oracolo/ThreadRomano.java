package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Oracolo che risponde come un antico romano.
 * Dopo 3 o 4 domande si ferma e ti sfida al Colosseo.
 */
public class ThreadRomano extends ThreadOracolo {
    private static final String[] NUMERI_ROMANI = {
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    // Quante risposte restano prima della sfida al Colosseo.
    private int domandeRimaste = nuovoLimite();

    public ThreadRomano(Socket socket) {
        super(socket, "romano");
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
            if (riga.equalsIgnoreCase("help")) {
                stampaComandi(out);
                out.println("Sfida al Colosseo -> scrivi un numero romano da I a X (I, II, III, IV, V, VI, VII, VIII, IX, X). "
                        + "Se e uguale a quello estratto da me hai perso e torni al menu, altrimenti continui a parlare con me.");
                continue;
            }

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
