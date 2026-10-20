package oracolo;

import java.net.Socket;

/** Oracolo che risponde come un imprenditore milanese. */
public class ThreadImprenditore extends ThreadOracolo {
    private static final String SALUTO =
            "Buongiorno, sono l'Oracolo e a Milano il tempo e denaro. Fai la tua domanda (scrivi 'fine' per uscire).";

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

    public ThreadImprenditore(Socket socket) {
        super(socket, SALUTO, RISPOSTE);
    }
}
