package oracolo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Carica da oracoli.json la descrizione, il saluto e le risposte di ogni oracolo. */
public class Oracoli {
    public static class Dati {
        public String descrizione;
        public String saluto;
        public List<String> risposte;
    }

    private static final String FILE = "oracoli.json";
    private static Map<String, Dati> mappa;

    /** Va chiamato una sola volta, all'avvio del server. */
    public static void carica() throws IOException {
        try {
            String testo = Files.readString(Path.of(FILE), StandardCharsets.UTF_8);
            Type tipo = new TypeToken<Map<String, Dati>>() {}.getType();
            mappa = new Gson().fromJson(testo, tipo);
        } catch (JsonParseException e) {
            throw new IOException("JSON non valido in " + FILE + ": " + e.getMessage());
        }
        if (mappa == null) throw new IOException("File vuoto: " + FILE);
    }

    public static Dati get(String nome) {
        if (mappa == null) throw new IllegalStateException("oracoli.json non caricato");
        Dati d = mappa.get(nome);
        if (d == null) throw new IllegalArgumentException("Oracolo sconosciuto: " + nome);
        return d;
    }
}
