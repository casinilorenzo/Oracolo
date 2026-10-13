package oracolo;

import java.util.Scanner;

/**
 * Punto di ingresso opzionale: sceglie se avviare Server o Client.
 * Server e Client si possono avviare anche direttamente (ognuno ha il proprio main).
 *
 * @author casini.lorenzo
 */
public class Oracolo {

    public static void main(String[] args) {
        String modo;
        String host = "127.0.0.1";

        if (args.length > 0) {
            // Uso da riga di comando: Oracolo server | Oracolo client [host]
            modo = args[0];
            if (args.length > 1) host = args[1];
        } else {
            // Lo Scanner non va chiuso, altrimenti chiude System.in.
            Scanner tastiera = new Scanner(System.in);
            System.out.print("Avviare come server o client? (server/client): ");
            modo = tastiera.nextLine().trim();
            if (modo.equalsIgnoreCase("client")) {
                System.out.print("Indirizzo del server [127.0.0.1]: ");
                String input = tastiera.nextLine().trim();
                if (!input.isEmpty()) host = input;
            }
        }

        if (modo.equalsIgnoreCase("server")) {
            Server.main(new String[0]);
        } else if (modo.equalsIgnoreCase("client")) {
            Client.main(new String[]{host});
        } else {
            System.out.println("Modalita non valida: usa 'server' oppure 'client'.");
        }
    }
}
