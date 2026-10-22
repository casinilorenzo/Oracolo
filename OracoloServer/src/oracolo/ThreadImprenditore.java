package oracolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Oracolo che risponde come un imprenditore milanese.
 * Dopo 3 o 4 domande dice che deve andare a fatturare: per continuare
 * bisogna fargli un bonifico di almeno 10000 euro (se e' meno, ne chiede
 * un altro), oppure si cambia personalita'.
 */
public class ThreadImprenditore extends ThreadOracolo {
    private static final double BONIFICO_MINIMO = 10000;

    private static final String SALUTO =
            "Buongiorno, sono l'Oracolo e a Milano il tempo e denaro. "
            + "Fai la tua domanda (scrivi 'cambia' per cambiare oracolo, 'fine' per uscire).";

    private static final String[] RISPOSTE = {
        // positive
        "Certo, e un business sicuro: spingi!",
        "Assolutamente si, ci metto la firma.",
        "Ottimo ritorno sull'investimento: vai tranquillo.",
        "E un'opportunita da cogliere subito, ASAP.",
        "Fidati, ho fiuto per gli affari: si.",
        "Il mercato e dalla tua parte, spingi sull'acceleratore.",
        "Win-win: e un si convinto.",
        "Si, e ti offro pure l'aperitivo ai Navigli.",
        "Numeri alla mano, i segni sono positivi.",
        "Perfetto, facciamo squadra e portiamolo a casa.",
        // incerte
        "Devo sentire il mio commercialista, riprova.",
        "Ne parliamo dopo il meeting, richiedimelo.",
        "Facciamo una call lunedi e vediamo.",
        "Non ho abbastanza dati, mandami un report.",
        "Ho l'agenda piena: chiedimelo piu tardi.",
        // negative
        "No, il budget non regge.",
        "Non e scalabile: lascia perdere.",
        "Per me e un flop, non investirei un euro.",
        "Le previsioni sono in rosso: no.",
        "Il mio avvocato dice di no, e io mi fido."
    };

    // Quante risposte gratis restano prima che l'imprenditore chieda soldi.
    private int domandeRimaste = nuovoLimite();

    public ThreadImprenditore(Socket socket) {
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
        return chiediBonifico(domanda, in, out);
    }

    /** Blocca la conversazione finche' non arriva un bonifico valido, o si cambia/esce. */
    private Esito chiediBonifico(String domanda, BufferedReader in, PrintWriter out) throws IOException {
        out.println("Scusa, non posso piu rispondere: devo andare a fatturare e tu mi stai "
                + "facendo perdere tempo e soldi! Se vuoi continuare, fammi un bonifico: "
                + "scrivi l'importo in euro (minimo 10000). Oppure scrivi 'cambia' per cambiare oracolo.");

        String riga;
        while ((riga = in.readLine()) != null) {
            riga = riga.trim();
            if (riga.equalsIgnoreCase("fine")) return Esito.FINE;
            if (riga.equalsIgnoreCase("cambia")) return Esito.CAMBIA;

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
            t = t.replace(".", ""); // separatore delle migliaia
        }
        if (!t.matches("\\d+(\\.\\d+)?")) return -1;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
