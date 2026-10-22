package oracolo;

import java.net.Socket;

/** Oracolo che risponde come un antico romano. */
public class ThreadRomano extends ThreadOracolo {
    private static final String SALUTO =
            "Ave, cittadino! Sono l'Oracolo di Roma. Poni la tua domanda (scrivi 'cambia' per cambiare oracolo, 'fine' per congedarti).";

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

    public ThreadRomano(Socket socket) {
        super(socket, SALUTO, RISPOSTE);
    }
}
