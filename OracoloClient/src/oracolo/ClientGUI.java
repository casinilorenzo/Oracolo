package oracolo;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Interfaccia grafica per il client dell'Oracolo.
 *
 * E' solo uno strato di presentazione: mostra in una chat ogni riga che arriva
 * dal server e invia al server esattamente il testo che l'utente scrive (o quello
 * dei pulsanti rapidi), senza modificarlo. Non contiene regole, testi, controlli o
 * logica del progetto: tutti i controlli (comandi, domande vuote, scelte non valide...)
 * restano nel Server e nelle personalita', come con il client a console.
 *
 * Avvio: prima il Server, poi questa classe. Senza argomenti si collega a 127.0.0.1;
 * con un argomento si collega all'indirizzo indicato.
 *
 * Se nel progetto c'e' la libreria FlatLaf viene usata in automatico (via reflection),
 * altrimenti funziona lo stesso con l'aspetto standard di Swing.
 */
public class ClientGUI extends JFrame {
    private static final int PORT = 5555;

    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font FONT_PICCOLO = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font FONT_TITOLO = new Font(Font.SANS_SERIF, Font.BOLD, 16);

    private static final Color SFONDO = Color.WHITE;
    private static final Color SFONDO_CHAT = new Color(0xFAFAF8);
    private static final Color BORDO = new Color(0xE3E1DA);
    private static final Color TESTO = new Color(0x2C2C2A);
    private static final Color TESTO_SOFT = new Color(0x6B6A64);
    private static final Color TESTO_SPENTO = new Color(0xB4B2A9);
    private static final Color ACCENTO = new Color(0x185FA5);
    private static final Color ACCENTO_SCURO = new Color(0x0C447C);
    private static final Color MIO_BG = new Color(0xE6F1FB);
    private static final Color TESTO_MIO = new Color(0x0C447C);
    private static final Color SERVER_BG = new Color(0xF1EFE8);
    private static final Color VERDE = new Color(0x3B6D11);
    private static final Color ROSSO = new Color(0xA32D2D);

    private final Socket socket;
    private final PrintWriter out;

    private final List<Bubble> bolle = new ArrayList<>();
    private final ChatPanel chat = new ChatPanel();
    private final JScrollPane scroll = new JScrollPane(chat);
    private final JLabel pallino = new JLabel("\u25CF");
    private final JLabel stato = new JLabel();
    private final List<PillButton> comandi = new ArrayList<>();
    private final InviaButton btnInvia = new InviaButton();

    private final JTextField campo = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(new Color(0xA3A199));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Scrivi un messaggio...", getInsets().left,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    };

    private Bubble ultimaBollaServer;
    private long ultimoArrivo;
    private int maxW = 440;

    public ClientGUI(Socket socket, String host) throws IOException {
        super("Oracolo");
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        costruisciInterfaccia(host);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(560, 720);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                campo.requestFocusInWindow();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                chiudiSocket();
            }
        });

        avviaRicezione();
    }

    // ------------------------------------------------------------------ interfaccia

    private void costruisciInterfaccia(String host) {
        getContentPane().setBackground(SFONDO);
        setLayout(new BorderLayout());

        // Intestazione: titolo, stato della connessione, comandi rapidi.
        JLabel titolo = new JLabel("Oracolo");
        titolo.setFont(FONT_TITOLO);
        titolo.setForeground(TESTO);
        pallino.setFont(FONT_PICCOLO);
        pallino.setForeground(VERDE);
        pallino.setBorder(new EmptyBorder(0, 0, 0, 6));
        stato.setFont(FONT_PICCOLO);
        stato.setForeground(TESTO_SOFT);
        stato.setText("Connesso a " + host + ":" + PORT);

        JPanel rigaStato = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rigaStato.setOpaque(false);
        rigaStato.add(pallino);
        rigaStato.add(stato);

        JPanel testi = new JPanel();
        testi.setLayout(new BoxLayout(testi, BoxLayout.Y_AXIS));
        testi.setOpaque(false);
        titolo.setAlignmentX(LEFT_ALIGNMENT);
        rigaStato.setAlignmentX(LEFT_ALIGNMENT);
        testi.add(titolo);
        testi.add(rigaStato);

        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pulsanti.setOpaque(false);
        for (String c : new String[]{"help", "cambia", "fine"}) {
            pulsanti.add(creaComando(c, Character.toUpperCase(c.charAt(0)) + c.substring(1)));
        }

        JPanel iconaBox = new JPanel(new GridBagLayout());
        iconaBox.setOpaque(false);
        iconaBox.add(new Icona());
        JPanel pulsantiBox = new JPanel(new GridBagLayout());
        pulsantiBox.setOpaque(false);
        pulsantiBox.add(pulsanti);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(SFONDO);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDO), new EmptyBorder(14, 18, 14, 18)));
        header.add(iconaBox, BorderLayout.WEST);
        header.add(testi, BorderLayout.CENTER);
        header.add(pulsantiBox, BorderLayout.EAST);

        // Scelte rapide: inviano solo il numero, come se lo scrivessi.
        JLabel etichetta = new JLabel("Scelte rapide");
        etichetta.setFont(FONT_PICCOLO);
        etichetta.setForeground(TESTO_SOFT);
        JPanel rapide = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rapide.setBackground(SFONDO);
        rapide.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDO), new EmptyBorder(10, 18, 10, 18)));
        rapide.add(etichetta);
        for (String n : new String[]{"1", "2", "3"}) {
            rapide.add(creaComando(n, n));
        }

        JPanel nord = new JPanel();
        nord.setLayout(new BoxLayout(nord, BoxLayout.Y_AXIS));
        nord.setBackground(SFONDO);
        nord.add(header);
        nord.add(rapide);
        add(nord, BorderLayout.NORTH);

        // Chat.
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(SFONDO_CHAT);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int nuovo = Math.max(200, (int) ((scroll.getViewport().getWidth() - 36) * 0.75));
                if (nuovo != maxW) {
                    maxW = nuovo;
                    for (Bubble b : bolle) b.setMaxW(maxW);
                    chat.revalidate();
                    chat.repaint();
                }
            }
        });
        add(scroll, BorderLayout.CENTER);

        // Campo di input e pulsante di invio.
        campo.setBorder(null);
        campo.setOpaque(false);
        campo.setFont(FONT);
        campo.setForeground(TESTO);
        campo.setCaretColor(ACCENTO);
        campo.addActionListener(e -> inviaDalCampo());
        btnInvia.addActionListener(e -> inviaDalCampo());

        CampoPanel box = new CampoPanel();
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(0, 16, 0, 16));
        box.setPreferredSize(new Dimension(100, 40));
        box.add(campo, BorderLayout.CENTER);

        JPanel sud = new JPanel(new BorderLayout(8, 0));
        sud.setBackground(SFONDO);
        sud.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDO), new EmptyBorder(12, 18, 12, 18)));
        sud.add(box, BorderLayout.CENTER);
        sud.add(btnInvia, BorderLayout.EAST);
        add(sud, BorderLayout.SOUTH);
    }

    /** Pulsante che invia al server il testo indicato, come se fosse scritto a mano. */
    private PillButton creaComando(String testoInviato, String etichetta) {
        PillButton b = new PillButton(etichetta);
        b.addActionListener(e -> invia(testoInviato));
        comandi.add(b);
        return b;
    }

    // ------------------------------------------------------------------ rete

    private void avviaRicezione() {
        Thread t = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                String riga;
                while ((riga = in.readLine()) != null) {
                    final String r = riga;
                    SwingUtilities.invokeLater(() -> riceviRiga(r));
                }
            } catch (IOException e) {
                // socket chiuso: si passa alla gestione della disconnessione
            }
            SwingUtilities.invokeLater(this::connessioneChiusa);
        }, "ricezione-gui");
        t.setDaemon(true);
        t.start();
    }

    private void inviaDalCampo() {
        String t = campo.getText();
        campo.setText("");
        invia(t);
        campo.requestFocusInWindow();
    }

    /** Mostra il testo nella chat e lo invia al server cosi' com'e': i controlli li fa il server. */
    private void invia(String testo) {
        aggiungiMessaggio(testo, true);
        ultimaBollaServer = null;
        out.println(testo);
    }

    private void riceviRiga(String riga) {
        long ora = System.currentTimeMillis();
        if (ultimaBollaServer != null && ora - ultimoArrivo < 150) {
            // Righe arrivate quasi insieme: un solo fumetto, piu' leggibile.
            ultimaBollaServer.aggiungi(riga);
            ultimoArrivo = ora;
            chat.revalidate();
            chat.repaint();
            scrollInFondo();
        } else {
            aggiungiMessaggio(riga, false);
        }
    }

    private void connessioneChiusa() {
        chiudiSocket();
        ultimaBollaServer = null;
        pallino.setForeground(ROSSO);
        stato.setText("Disconnesso");
        campo.setEnabled(false);
        btnInvia.setEnabled(false);
        for (PillButton b : comandi) b.setEnabled(false);
        aggiungiMessaggio("Connessione terminata.", false);
    }

    private void chiudiSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            // niente da fare
        }
    }

    // ------------------------------------------------------------------ chat

    private void aggiungiMessaggio(String testo, boolean mio) {
        Bubble b = new Bubble(testo, mio, maxW);
        bolle.add(b);

        JPanel riga = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        riga.setOpaque(false);
        riga.setBorder(new EmptyBorder(4, 0, 4, 0));
        riga.add(b, mio ? BorderLayout.EAST : BorderLayout.WEST);
        chat.add(riga);

        if (!mio) {
            ultimaBollaServer = b;
            ultimoArrivo = System.currentTimeMillis();
        }
        chat.revalidate();
        chat.repaint();
        scrollInFondo();
    }

    private void scrollInFondo() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar barra = scroll.getVerticalScrollBar();
            barra.setValue(barra.getMaximum());
        });
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------------------ componenti grafici

    /** Pannello della chat: segue la larghezza della finestra e scorre in verticale. */
    private static class ChatPanel extends JPanel implements Scrollable {
        ChatPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(SFONDO_CHAT);
            setBorder(new EmptyBorder(14, 18, 14, 18));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Fumetto di testo: blu a destra per l'utente, grigio a sinistra per il server. */
    private static class Bubble extends JPanel {
        private final JLabel label = new JLabel();
        private final boolean mio;
        private String testo;
        private int maxW;

        Bubble(String testo, boolean mio, int maxW) {
            this.testo = testo;
            this.mio = mio;
            this.maxW = maxW;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(9, 14, 9, 14));
            label.setFont(FONT);
            label.setForeground(mio ? TESTO_MIO : TESTO);
            add(label, BorderLayout.CENTER);
            aggiorna();
        }

        void aggiungi(String altro) {
            testo = testo + "\n" + altro;
            aggiorna();
        }

        void setMaxW(int maxW) {
            this.maxW = maxW;
            aggiorna();
        }

        private void aggiorna() {
            FontMetrics fm = label.getFontMetrics(FONT);
            int piuLunga = 0;
            for (String r : testo.split("\n")) piuLunga = Math.max(piuLunga, fm.stringWidth(r));
            int w = Math.max(20, Math.min(maxW, piuLunga + 8));
            label.setText("<html><body style='width:" + w + "px'>"
                    + escapeHtml(testo).replace("\n", "<br>") + "</body></html>");
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(mio ? MIO_BG : SERVER_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
        }
    }

    /** Pulsante arrotondato per i comandi rapidi. */
    private static class PillButton extends JButton {
        private boolean sopra;

        PillButton(String t) {
            super(t);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(FONT_PICCOLO);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sopra = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sopra = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(Math.max(34, fm.stringWidth(getText()) + 28), 32);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w, h, h, h);
            if (isEnabled() && sopra) {
                g2.setColor(new Color(0, 0, 0, 14));
                g2.fillRoundRect(0, 0, w, h, h, h);
            }
            g2.setColor(BORDO);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(isEnabled() ? TESTO : TESTO_SPENTO);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Pulsante di invio: cerchio blu con la freccia verso l'alto. */
    private static class InviaButton extends JButton {
        private boolean sopra;

        InviaButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(40, 40));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Invia");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sopra = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sopra = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? TESTO_SPENTO : sopra ? ACCENTO_SCURO : ACCENTO);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.drawLine(cx, cy + 7, cx, cy - 7);
            g2.drawLine(cx - 6, cy - 1, cx, cy - 7);
            g2.drawLine(cx + 6, cy - 1, cx, cy - 7);
            g2.dispose();
        }
    }

    /** Sfondo arrotondato con bordo per il campo di testo. */
    private static class CampoPanel extends JPanel {
        CampoPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SFONDO_CHAT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(BORDO);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
        }
    }

    /** Piccolo cerchio decorativo nell'intestazione. */
    private static class Icona extends JComponent {
        Icona() {
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(MIO_BG);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(ACCENTO);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("O", (getWidth() - fm.stringWidth("O")) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ avvio

    /** Usa FlatLaf se e' presente nel progetto, senza obbligarne la presenza. */
    private static void impostaStile() {
        try {
            Class.forName("com.formdev.flatlaf.FlatLightLaf").getMethod("setup").invoke(null);
        } catch (Exception | LinkageError e) {
            // FlatLaf non disponibile: resta l'aspetto standard di Swing
        }
    }

    public static void main(String[] args) {
        // Senza argomenti si collega al server sullo stesso computer.
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        impostaStile();
        try {
            Socket socket = new Socket(host, PORT);
            SwingUtilities.invokeLater(() -> {
                try {
                    new ClientGUI(socket, host).setVisible(true);
                } catch (IOException e) {
                    mostraErrore(host, e);
                }
            });
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> mostraErrore(host, e));
        }
    }

    private static void mostraErrore(String host, IOException e) {
        JOptionPane.showMessageDialog(null,
                "Impossibile collegarsi a " + host + ":" + PORT + "\n" + e.getMessage()
                + "\n\nControlla che il Server sia acceso.",
                "Errore di connessione", JOptionPane.ERROR_MESSAGE);
    }
}
