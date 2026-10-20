package oracolo;

import java.net.Socket;

/** Oracolo con tre voci comiche: Aldo, Giovanni e Giacomo. */
public class ThreadTrio extends ThreadOracolo {
    private static final String SALUTO =
            "Siamo il trio dell'Oracolo: Aldo, Giovanni e Giacomo rispondono alla tua domanda (scrivi 'fine' per uscire).";

    private static final String[] RISPOSTE = {
        // positive
        "Aldo: Ma certo che si, vai sereno, ci penso io!",
        "Giovanni: Statisticamente si, ho fatto il calcolo sul retro di uno scontrino.",
        "Giacomo: Si, pero non guardarmi mentre lo dico.",
        "Aldo: Sei un fenomeno, la risposta e si!",
        "Giovanni: Tutto in regola, i segni sono favorevoli.",
        "Giacomo: Mah, io direi di si, ma sono emozionato.",
        "Aldo: Si, e dopo festeggiamo con una bella cena!",
        "Giovanni: E certo, e pure con la ricevuta.",
        "Giacomo: Si, e ho gia preparato anche un discorso.",
        "Aldo: Sicuro come il sole a mezzogiorno!",
        // incerte
        "Aldo: Eh, e complicato... chiedilo a Giovanni.",
        "Giovanni: Mi servono piu informazioni, e magari un modulo da compilare.",
        "Giacomo: Non lo so, mi gira la testa, riprova dopo.",
        "Aldo: Aspetta, mi sono perso. Richiedimelo.",
        "Giovanni: Dammi cinque minuti e un caffe, poi riformula la domanda.",
        // negative
        "Aldo: No no no, lascia perdere, fidati!",
        "Giovanni: Ho controllato tre volte: la risposta e no.",
        "Giacomo: Purtroppo no, mi dispiace tantissimo.",
        "Aldo: Neanche se ci mettiamo tutti e tre!",
        "Giovanni: Le probabilita sono contro di te, e pure il regolamento."
    };

    public ThreadTrio(Socket socket) {
        super(socket, SALUTO, RISPOSTE);
    }
}
