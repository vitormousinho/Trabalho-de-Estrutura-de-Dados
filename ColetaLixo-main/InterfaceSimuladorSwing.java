import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;

/**
 * Interface gráfica para o Simulador de Coleta de Lixo usando Swing
 */
public class InterfaceSimuladorSwing extends JFrame {
    private Simulador simulador;
    private JTextArea logArea;
    private JTextField comandoField;
    private JButton iniciarBtn, pausarBtn, continuarBtn, encerrarBtn;
    private JTabbedPane tabPane;
    private JTextArea estatisticasArea;
    private JTextArea relatorioArea;

    public InterfaceSimuladorSwing(Simulador simulador) {
        this.simulador = simulador;
        setTitle("Simulador de Coleta de Lixo - Teresina");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        criarComponentes();
        organizarLayout();
        configurarEventos();
        redirecionarSaidaPadrao();

        setLocationRelativeTo(null); // Centraliza na tela
    }

    private void criarComponentes() {
        // Botões
        iniciarBtn = new JButton("Iniciar Simulação");
        pausarBtn = new JButton("Pausar");
        continuarBtn = new JButton("Continuar");
        encerrarBtn = new JButton("Encerrar/Relatório");

        // Áreas de texto
        logArea = new JTextArea(20, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        estatisticasArea = new JTextArea(20, 50);
        estatisticasArea.setEditable(false);
        estatisticasArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        relatorioArea = new JTextArea(20, 50);
        relatorioArea.setEditable(false);
        relatorioArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Campo de comando
        comandoField = new JTextField(30);
    }

    private void organizarLayout() {
        // Painel de botões (topo)
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(iniciarBtn);
        buttonPanel.add(pausarBtn);
        buttonPanel.add(continuarBtn);
        buttonPanel.add(encerrarBtn);

        // Abas (centro)
        tabPane = new JTabbedPane();

        // Aba de Logs
        JScrollPane logScroll = new JScrollPane(logArea);
        tabPane.addTab("Logs da Simulação", logScroll);

        // Aba de Estatísticas
        JScrollPane estatisticasScroll = new JScrollPane(estatisticasArea);
        tabPane.addTab("Estatísticas", estatisticasScroll);

        // Aba de Relatório
        JScrollPane relatorioScroll = new JScrollPane(relatorioArea);
        tabPane.addTab("Relatório Final", relatorioScroll);

        // Painel de comando (inferior)
        JPanel comandoPanel = new JPanel();
        comandoPanel.add(new JLabel("Comando:"));
        comandoPanel.add(comandoField);
        JButton enviarBtn = new JButton("Enviar");
        comandoPanel.add(enviarBtn);
        enviarBtn.addActionListener(e -> processarComando());

        // Organizando no frame principal
        add(buttonPanel, BorderLayout.NORTH);
        add(tabPane, BorderLayout.CENTER);
        add(comandoPanel, BorderLayout.SOUTH);
    }

    private void configurarEventos() {
        iniciarBtn.addActionListener(e -> {
            simulador.iniciar();
            adicionarLog("Simulação iniciada");
            habilitarBotoes(false, true, true, true);
        });

        pausarBtn.addActionListener(e -> {
            simulador.pausar();
            adicionarLog("Simulação pausada");
            habilitarBotoes(false, false, true, true);
        });

        continuarBtn.addActionListener(e -> {
            simulador.continuarSimulacao();
            adicionarLog("Simulação continuada");
            habilitarBotoes(false, true, false, true);
        });

        encerrarBtn.addActionListener(e -> {
            simulador.encerrar();
            adicionarLog("Simulação encerrada");
            habilitarBotoes(true, false, false, false);
            atualizarRelatorio();
        });

        // Configurar o campo de comando
        comandoField.addActionListener(e -> processarComando());
    }

    private void habilitarBotoes(boolean iniciar, boolean pausar, boolean continuar, boolean encerrar) {
        iniciarBtn.setEnabled(iniciar);
        pausarBtn.setEnabled(pausar);
        continuarBtn.setEnabled(continuar);
        encerrarBtn.setEnabled(encerrar);
    }

    private void processarComando() {
        String comando = comandoField.getText().trim();
        if (comando.isEmpty()) return;

        adicionarLog("> " + comando);

        switch (comando.toLowerCase()) {
            case "iniciar":
                simulador.iniciar();
                habilitarBotoes(false, true, true, true);
                break;

            case "pausar":
                simulador.pausar();
                habilitarBotoes(false, false, true, true);
                break;

            case "continuar":
                simulador.continuarSimulacao();
                habilitarBotoes(false, true, false, true);
                break;

            case "encerrar":
                simulador.encerrar();
                habilitarBotoes(true, false, false, false);
                atualizarRelatorio();
                break;

            case "estatisticas":
            case "stats":
                atualizarEstatisticas();
                tabPane.setSelectedIndex(1); // Muda para a aba de estatísticas
                break;

            case "relatorio":
                atualizarRelatorio();
                tabPane.setSelectedIndex(2); // Muda para a aba de relatório
                break;

            case "adicionar grande":
                simulador.adicionarCaminhaoGrande();
                adicionarLog("Caminhão grande adicionado manualmente");
                break;

            case "ajuda":
                mostrarAjuda();
                break;

            default:
                adicionarLog("Comando desconhecido. Digite 'ajuda' para ver os comandos disponíveis.");
        }

        comandoField.setText("");
    }

    private void mostrarAjuda() {
        String ajuda = "Comandos disponíveis:\n" +
                "  iniciar    - Inicia a simulação\n" +
                "  pausar     - Pausa a simulação em andamento\n" +
                "  continuar  - Continua uma simulação pausada\n" +
                "  encerrar   - Encerra a simulação e gera relatório\n" +
                "  estatisticas - Exibe estatísticas atuais\n" +
                "  relatorio  - Gera relatório completo\n" +
                "  adicionar grande - Adiciona um novo caminhão grande\n" +
                "  ajuda      - Exibe esta mensagem de ajuda\n";

        JOptionPane.showMessageDialog(this, ajuda, "Ajuda - Comandos Disponíveis", JOptionPane.INFORMATION_MESSAGE);
    }

    public void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensagem + "\n");
            // Auto-scroll para a última linha
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void atualizarEstatisticas() {
        SwingUtilities.invokeLater(() -> {
            estatisticasArea.setText(""); // Limpa o conteúdo atual

            // Obter estatísticas atuais
            Estatisticas stats = simulador.getEstatisticas();
            if (stats != null) {
                // Esta é uma versão resumida para mostrar em tempo real
                estatisticasArea.append("=== ESTATÍSTICAS ATUAIS ===\n\n");
                estatisticasArea.append("Tempo simulado: " + formatarTempo(simulador.getTempoSimulado()) + "\n\n");

                estatisticasArea.append("CAMINHÕES:\n");
                estatisticasArea.append("- Caminhões Pequenos: \n");
                // Aqui adicionaria informações dos caminhões pequenos

                estatisticasArea.append("- Caminhões Grandes: " + simulador.getTotalCaminhoesGrandes() +
                        " (Em uso: " + simulador.getCaminhoesGrandesEmUso() + ")\n\n");

                estatisticasArea.append("ZONAS:\n");
                // Aqui adicionaria informações das zonas

                estatisticasArea.append("ESTAÇÕES:\n");
                // Aqui adicionaria informações das estações
            } else {
                estatisticasArea.append("Não há estatísticas disponíveis ainda.");
            }
        });
    }

    public void atualizarRelatorio() {
        SwingUtilities.invokeLater(() -> {
            relatorioArea.setText(""); // Limpa o conteúdo atual

            // Obter relatório completo
            Estatisticas stats = simulador.getEstatisticas();
            if (stats != null) {
                relatorioArea.append(stats.gerarRelatorio());
            } else {
                relatorioArea.append("Não há relatório disponível ainda.");
            }
        });
    }

    private String formatarTempo(int minutos) {
        int horas = minutos / 60;
        int mins = minutos % 60;
        return String.format("%02d:%02d", horas, mins);
    }

    // Método para redirecionar a saída padrão para o painel de log
    private void redirecionarSaidaPadrao() {
        // Criar um OutputStream personalizado que redireciona para o logArea
        OutputStream out = new OutputStream() {
            private StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                buffer.append(c);
                if (c == '\n') {
                    final String texto = buffer.toString();
                    SwingUtilities.invokeLater(() -> adicionarLog(texto.trim()));
                    buffer = new StringBuilder();
                }
            }
        };

        // Criar um PrintStream com nosso OutputStream personalizado
        PrintStream ps = new PrintStream(out, true);

        // Redirecionar System.out para o nosso PrintStream
        System.setOut(ps);
    }

    public void iniciar() {
        setVisible(true);
        adicionarLog("Interface de simulação iniciada");
        adicionarLog("Digite 'ajuda' para ver os comandos disponíveis");
        habilitarBotoes(true, false, false, true);
    }
}
