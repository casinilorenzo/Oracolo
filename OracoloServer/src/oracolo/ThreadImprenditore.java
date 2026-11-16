package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Oracolo che risponde come un imprenditore milanese.
 * Dopo 3 o 4 domande chiede un bonifico di almeno 10000 euro per continuare.
 */
public class ThreadImprenditore extends ThreadOracolo {
    private static final double BONIFICO_MINIMO = 10000;

    // Quante risposte gratis restano prima che l'imprenditore chieda soldi.
    private int domandeRimaste = nuovoLimite();

    public ThreadImprenditore(Socket socket) {
        super(socket, "imprenditore");
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
        return chiediBonifico(in, out);
    }

    /** Blocca la conversazione finche' non arriva un bonifico valido, o si cambia/esce. */
    private Esito chiediBonifico(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Scusa, non posso piu rispondere: devo andare a fatturare e tu mi stai "
                + "facendo perdere tempo e soldi! Se vuoi continuare, fammi un bonifico: "
                + "scrivi l'importo in euro (minimo 10000). Oppure scrivi 'cambia' per cambiare oracolo.");

        String riga;
        while ((riga = in.readLine()) != null) {
            riga = riga.trim();
            if (riga.equalsIgnoreCase("fine")) return Esito.FINE;
            if (riga.equalsIgnoreCase("cambia")) return Esito.CAMBIA;
            if (riga.equalsIgnoreCase("help")) {
                stampaComandi(out);
                out.println("Bonifico -> scrivi solo l'importo in euro (minimo 10000), ad esempio 10000 oppure 12500,50. "
                        + "Se e meno di 10000 ne chiedo un altro; appena arriva una cifra valida rispondo alla tua domanda.");
                continue;
            }

            double importo = leggiImporto(riga);
            if (importo < 0) {
                out.println("Non ho capito: scrivi solo l'importo del bonifico in euro, ad esempio 10000.");
            } else if (importo < BONIFICO_MINIMO) {
                out.println(String.format(Locale.ITALY, "%,.2f euro? Con questa cifra non pago nemmeno "
                        + "il caffe in Galleria. Fanne un altro: almeno 10000!", importo));
            } else {
                out.println(String.format(Locale.ITALY, "Bonifico da %,.2f euro ricevuto: ora si che "
                        + "ragioniamo! Rispondo alla tua domanda.", importo));
                domandeRimaste = nuovoLimite() - 1; // questa risposta conta come la prima
                out.println(rispondi());
                return Esito.CONTINUA;
            }
        }
        return Esito.FINE;
    }

    /** Legge un importo come "10000", "10.000", "12500,50" o "10000 euro". Restituisce -1 se non valido. */
    private static double leggiImporto(String testo) {
        String t = testo.toLowerCase().replace("euro", "").replace("\u20ac", "").replace(" ", "");
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", "");
        }
        if (!t.matches("\\d+(\\.\\d+)?")) return -1;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
