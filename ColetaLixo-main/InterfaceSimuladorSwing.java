// Pacote (se houver)
// package seu.pacote.aqui;

import Estruturas.Lista;
import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoGrandePadrao;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import estacoes.EstacaoPadrao;
import estacoes.EstacaoTransferencia;
// import estacoes.ResultadoProcessamentoFila; // Não usado diretamente aqui, mas pode ser em Simulador
import zonas.ZonaUrbana;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
// Imports necessários

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

    // Componentes para Configuração
    private JSpinner spinnerCaminhoes2T, spinnerCaminhoes4T, spinnerCaminhoes8T, spinnerCaminhoes10T;
    private JSpinner spinnerCaminhoesGrandes;
    private JSpinner spinnerZonaSulMin, spinnerZonaSulMax;
    private JSpinner spinnerZonaNorteMin, spinnerZonaNorteMax;
    private JSpinner spinnerZonaCentroMin, spinnerZonaCentroMax;
    private JSpinner spinnerZonaLesteMin, spinnerZonaLesteMax;
    private JSpinner spinnerZonaSudesteMin, spinnerZonaSudesteMax;
    private JSpinner spinnerEstacaoATempoEspera, spinnerEstacaoBTempoEspera;
    private JSpinner spinnerToleranciaCG;

    // Constantes
    private static final int CAPACIDADE_2T = 2000;
    private static final int CAPACIDADE_4T = 4000;
    private static final int CAPACIDADE_8T = 8000;
    private static final int CAPACIDADE_10T = 10000;
    private static final String ZONA_SUL = "Sul";
    private static final String ZONA_NORTE = "Norte";
    private static final String ZONA_CENTRO = "Centro";
    private static final String ZONA_LESTE = "Leste";
    private static final String ZONA_SUDESTE = "Sudeste";
    private static final String ESTACAO_A = "Estação A";
    private static final String ESTACAO_B = "Estação B";
    private static final int MINUTOS_EM_UM_DIA = 24 * 60;


    public InterfaceSimuladorSwing(Simulador simulador) {
        this.simulador = simulador;
        setTitle("Simulador de Coleta de Lixo - Teresina");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        criarComponentes();
        organizarLayout(); // Chama o método refatorado
        configurarEventos();
        redirecionarSaidaPadrao();
        habilitarBotoes(true, false, false, false); // Estado inicial

        setLocationRelativeTo(null);
    }

    private void criarComponentes() {
        // Botões
        iniciarBtn = new JButton("Iniciar Simulação");
        pausarBtn = new JButton("Pausar");
        continuarBtn = new JButton("Continuar");
        encerrarBtn = new JButton("Encerrar/Relatório");

        // Áreas de texto
        Font logFont = new Font("Monospaced", Font.PLAIN, 12);
        logArea = new JTextArea(15, 50); logArea.setEditable(false); logArea.setFont(logFont);
        estatisticasArea = new JTextArea(15, 50); estatisticasArea.setEditable(false); estatisticasArea.setFont(logFont);
        relatorioArea = new JTextArea(15, 50); relatorioArea.setEditable(false); relatorioArea.setFont(logFont);

        // Campo de comando
        comandoField = new JTextField(30);

        // Componentes de Configuração (Spinners)
        spinnerCaminhoes2T = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        spinnerCaminhoes4T = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        spinnerCaminhoes8T = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        spinnerCaminhoes10T = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        spinnerCaminhoesGrandes = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));
        spinnerZonaSulMin = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
        spinnerZonaSulMax = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        spinnerZonaNorteMin = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
        spinnerZonaNorteMax = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        spinnerZonaCentroMin = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
        spinnerZonaCentroMax = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        spinnerZonaLesteMin = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
        spinnerZonaLesteMax = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        spinnerZonaSudesteMin = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
        spinnerZonaSudesteMax = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        spinnerEstacaoATempoEspera = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));
        spinnerEstacaoBTempoEspera = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));
        spinnerToleranciaCG = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));
    }

    // --- MÉTODO organizarLayout() SEM MÉTODOS AUXILIARES ---
    private void organizarLayout() {
        // Painel de botões (topo)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(iniciarBtn);
        buttonPanel.add(pausarBtn);
        buttonPanel.add(continuarBtn);
        buttonPanel.add(encerrarBtn);

        // Abas (centro)
        tabPane = new JTabbedPane();

        // --- Aba de Configurações ---
        JPanel configFormPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5); // Espaçamento externo padrão
        int y = 0; // Contador de linha

        // Adicionando Título Caminhões Pequenos
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.insets = new Insets(8, 5, 2, 5); gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(new JLabel("<html><b>--- Frota de Caminhões Pequenos ---</b></html>"), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(3, 5, 3, 5); // Reset insets
        y++;

        // Adicionando Linha Caminhões 2T
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_2T / 1000) + "T:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerCaminhoes2T, gbc);
        y++;

        // Adicionando Linha Caminhões 4T
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_4T / 1000) + "T:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerCaminhoes4T, gbc);
        y++;

        // Adicionando Linha Caminhões 8T
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_8T / 1000) + "T:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerCaminhoes8T, gbc);
        y++;

        // Adicionando Linha Caminhões 10T
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_10T / 1000) + "T:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerCaminhoes10T, gbc);
        y++;

        // Adicionando Título Caminhões Grandes
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.insets = new Insets(8, 5, 2, 5); gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(new JLabel("<html><b>--- Frota de Caminhões Grandes ---</b></html>"), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(3, 5, 3, 5);
        y++;

        // Adicionando Linha Qtd CG
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel("Qtd. Caminhões Grandes (20T):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerCaminhoesGrandes, gbc);
        y++;

        // Adicionando Linha Tolerância CG
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel("Tolerância Espera CG (min):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerToleranciaCG, gbc);
        y++;

        // Adicionando Título Zonas
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.insets = new Insets(8, 5, 2, 5); gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(new JLabel("<html><b>--- Configuração das Zonas (Lixo kg/dia) ---</b></html>"), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(3, 5, 3, 5);
        y++;

        // Adicionando Linha Zona Sul Min
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_SUL + " - Mín:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaSulMin, gbc);
        y++;
        // Adicionando Linha Zona Sul Max
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_SUL + " - Máx:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaSulMax, gbc);
        y++;

        // Adicionando Linha Zona Norte Min
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_NORTE + " - Mín:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaNorteMin, gbc);
        y++;
        // Adicionando Linha Zona Norte Max
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_NORTE + " - Máx:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaNorteMax, gbc);
        y++;

        // Adicionando Linha Zona Centro Min
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_CENTRO + " - Mín:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaCentroMin, gbc);
        y++;
        // Adicionando Linha Zona Centro Max
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_CENTRO + " - Máx:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaCentroMax, gbc);
        y++;

        // Adicionando Linha Zona Leste Min
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_LESTE + " - Mín:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaLesteMin, gbc);
        y++;
        // Adicionando Linha Zona Leste Max
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_LESTE + " - Máx:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaLesteMax, gbc);
        y++;

        // Adicionando Linha Zona Sudeste Min
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_SUDESTE + " - Mín:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaSudesteMin, gbc);
        y++;
        // Adicionando Linha Zona Sudeste Max
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ZONA_SUDESTE + " - Máx:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerZonaSudesteMax, gbc);
        y++;

        // Adicionando Título Estações
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.insets = new Insets(8, 5, 2, 5); gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(new JLabel("<html><b>--- Configuração das Estações ---</b></html>"), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(3, 5, 3, 5);
        y++;

        // Adicionando Linha Estação A
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ESTACAO_A + " - Limite Espera CP (min):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerEstacaoATempoEspera, gbc);
        y++;

        // Adicionando Linha Estação B
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.EAST;
        configFormPanel.add(new JLabel(ESTACAO_B + " - Limite Espera CP (min):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        configFormPanel.add(spinnerEstacaoBTempoEspera, gbc);
        y++;

        // --- Fim da adição de componentes ---

        // Painel para envolver o formulário e permitir scroll
        JPanel configPanelWrapper = new JPanel(new BorderLayout());
        configPanelWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        configPanelWrapper.add(configFormPanel, BorderLayout.NORTH);
        JScrollPane configScroll = new JScrollPane(configPanelWrapper);
        tabPane.addTab("Configurações", configScroll);

        // Outras Abas
        JScrollPane logScroll = new JScrollPane(logArea);
        tabPane.addTab("Logs da Simulação", logScroll);
        JScrollPane estatisticasScroll = new JScrollPane(estatisticasArea);
        tabPane.addTab("Estatísticas (Tempo Real)", estatisticasScroll);
        JScrollPane relatorioScroll = new JScrollPane(relatorioArea);
        tabPane.addTab("Relatório Final", relatorioScroll);

        // Painel de comando
        JPanel comandoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comandoPanel.add(new JLabel("Comando:"));
        comandoPanel.add(comandoField);
        JButton enviarBtn = new JButton("Enviar");
        comandoPanel.add(enviarBtn);
        enviarBtn.addActionListener(e -> processarComando());
        comandoField.addActionListener(e -> processarComando()); // Permite Enter no campo de texto

        // Adiciona componentes ao frame
        add(buttonPanel, BorderLayout.NORTH);
        add(tabPane, BorderLayout.CENTER);
        add(comandoPanel, BorderLayout.SOUTH);
    }


    private void configurarEventos() {
        iniciarBtn.addActionListener(e -> {
            try {
                configurarESetarSimuladorPelaUI();
                simulador.iniciar();
                adicionarLog("Simulação iniciada com configurações da UI.\n"); // Adiciona \n aqui se a chamada é direta
                habilitarBotoes(false, true, false, true); // Pausar e Encerrar ativos
                if (tabPane.getTabCount() > 1) tabPane.setSelectedIndex(1); // Vai para aba de Logs

                // Timer para atualizar estatísticas periodicamente
                new Timer(1000, evt -> { // Atualiza a cada 1 segundo
                    if (simulador != null && !simulador.isPausado()) {
                        atualizarEstatisticas();
                    }
                }).start();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Erro nos valores de configuração: Verifique os números.\nDetalhe: " + nfe.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                adicionarLog("Falha ao iniciar: Erro nos valores.\n");
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(this, "Erro de configuração: " + iae.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                adicionarLog("Falha ao iniciar: " + iae.getMessage() + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro inesperado ao iniciar a simulação: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
                adicionarLog("Falha ao iniciar: " + ex.getMessage() + "\n");
                ex.printStackTrace(); // Ajuda a depurar
            }
        });

        pausarBtn.addActionListener(e -> {
            if (simulador != null) simulador.pausar();
            // A mensagem de "Simulação pausada" já vem do simulador.println()
            // adicionarLog("Simulação pausada\n");
            habilitarBotoes(false, false, true, true); // Continuar e Encerrar ativos
        });

        continuarBtn.addActionListener(e -> {
            if (simulador != null) simulador.continuarSimulacao();
            // A mensagem de "Simulação continuada" já vem do simulador.println()
            // adicionarLog("Simulação continuada\n");
            habilitarBotoes(false, true, false, true); // Pausar e Encerrar ativos
        });

        encerrarBtn.addActionListener(e -> {
            if (simulador != null) {
                simulador.encerrar(); // Encerra a simulação e gera relatório interno
                // A mensagem de "Simulação encerrada" já vem do simulador.println()
                atualizarRelatorio(); // Pega o relatório final do simulador
                if (tabPane.getTabCount() > 3) tabPane.setSelectedIndex(3); // Vai para aba de Relatório Final
            } else {
                adicionarLog("Simulador não iniciado. Nada para encerrar.\n");
            }
            habilitarBotoes(true, false, false, false); // Habilita Iniciar, desabilita outros
        });
    }

    private void configurarESetarSimuladorPelaUI() {
        // As chamadas a adicionarLog() aqui não precisam de \n extra pois System.out já o faz.
        adicionarLog("Configurando simulador com parâmetros da UI...");
        int qtd2T = (Integer) spinnerCaminhoes2T.getValue();
        int qtd4T = (Integer) spinnerCaminhoes4T.getValue();
        int qtd8T = (Integer) spinnerCaminhoes8T.getValue();
        int qtd10T = (Integer) spinnerCaminhoes10T.getValue();
        int qtdCG = (Integer) spinnerCaminhoesGrandes.getValue();
        int toleranciaCG = (Integer) spinnerToleranciaCG.getValue();
        int zonaSulMinVal = (Integer) spinnerZonaSulMin.getValue();
        int zonaSulMaxVal = (Integer) spinnerZonaSulMax.getValue();
        if (zonaSulMinVal > zonaSulMaxVal) throw new IllegalArgumentException("Zona Sul: Geração Mínima não pode ser maior que Máxima.");
        int zonaNorteMinVal = (Integer) spinnerZonaNorteMin.getValue();
        int zonaNorteMaxVal = (Integer) spinnerZonaNorteMax.getValue();
        if (zonaNorteMinVal > zonaNorteMaxVal) throw new IllegalArgumentException("Zona Norte: Geração Mínima não pode ser maior que Máxima.");
        int zonaCentroMinVal = (Integer) spinnerZonaCentroMin.getValue();
        int zonaCentroMaxVal = (Integer) spinnerZonaCentroMax.getValue();
        if (zonaCentroMinVal > zonaCentroMaxVal) throw new IllegalArgumentException("Zona Centro: Geração Mínima não pode ser maior que Máxima.");
        int zonaLesteMinVal = (Integer) spinnerZonaLesteMin.getValue();
        int zonaLesteMaxVal = (Integer) spinnerZonaLesteMax.getValue();
        if (zonaLesteMinVal > zonaLesteMaxVal) throw new IllegalArgumentException("Zona Leste: Geração Mínima não pode ser maior que Máxima.");
        int zonaSudesteMinVal = (Integer) spinnerZonaSudesteMin.getValue();
        int zonaSudesteMaxVal = (Integer) spinnerZonaSudesteMax.getValue();
        if (zonaSudesteMinVal > zonaSudesteMaxVal) throw new IllegalArgumentException("Zona Sudeste: Geração Mínima não pode ser maior que Máxima.");
        int estacaoATempoVal = (Integer) spinnerEstacaoATempoEspera.getValue();
        int estacaoBTempoVal = (Integer) spinnerEstacaoBTempoEspera.getValue();

        Lista<CaminhaoPequeno> listaCaminhoesPequenos = new Lista<>();
        adicionarCaminhoes(listaCaminhoesPequenos, qtd2T, CAPACIDADE_2T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd4T, CAPACIDADE_4T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd8T, CAPACIDADE_8T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd10T, CAPACIDADE_10T);
        Lista<CaminhaoGrande> listaCaminhoesGrandes = new Lista<>();
        for (int i = 0; i < qtdCG; i++) { listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao(toleranciaCG)); }

        Lista<ZonaUrbana> listaZonas = new Lista<>();
        ZonaUrbana zonaSul = new ZonaUrbana(ZONA_SUL); zonaSul.setIntervaloGeracao(zonaSulMinVal, zonaSulMaxVal); listaZonas.adicionar(zonaSul);
        ZonaUrbana zonaNorte = new ZonaUrbana(ZONA_NORTE); zonaNorte.setIntervaloGeracao(zonaNorteMinVal, zonaNorteMaxVal); listaZonas.adicionar(zonaNorte);
        ZonaUrbana zonaCentro = new ZonaUrbana(ZONA_CENTRO); zonaCentro.setIntervaloGeracao(zonaCentroMinVal, zonaCentroMaxVal); listaZonas.adicionar(zonaCentro);
        ZonaUrbana zonaLeste = new ZonaUrbana(ZONA_LESTE); zonaLeste.setIntervaloGeracao(zonaLesteMinVal, zonaLesteMaxVal); listaZonas.adicionar(zonaLeste);
        ZonaUrbana zonaSudeste = new ZonaUrbana(ZONA_SUDESTE); zonaSudeste.setIntervaloGeracao(zonaSudesteMinVal, zonaSudesteMaxVal); listaZonas.adicionar(zonaSudeste);

        Lista<EstacaoTransferencia> listaEstacoes = new Lista<>();
        listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_A, estacaoATempoVal));
        listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_B, estacaoBTempoVal));

        simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
        simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
        simulador.setListaZonas(listaZonas);
        simulador.setListaEstacoes(listaEstacoes);
        simulador.setToleranciaCaminhoesGrandes(toleranciaCG);
        // simulador.setIntervaloRelatorio(60); // Exemplo, se quiser configurar o intervalo do relatório no console
        System.out.println("Simulador configurado e pronto para iniciar."); // Esta mensagem irá para o log com \n
    }

    private static void adicionarCaminhoes(Lista<CaminhaoPequeno> lista, int quantidade, int capacidade) {
        if (quantidade < 0) return;
        for (int i = 0; i < quantidade; i++) {
            try {
                lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
            } catch (IllegalArgumentException e) {
                System.err.println("Erro CRÍTICO ao criar caminhão pequeno com capacidade " + capacidade + ": " + e.getMessage());
            }
        }
    }

    private void habilitarBotoes(boolean iniciar, boolean pausar, boolean continuar, boolean encerrar) {
        iniciarBtn.setEnabled(iniciar);
        pausarBtn.setEnabled(pausar);
        continuarBtn.setEnabled(continuar);
        encerrarBtn.setEnabled(encerrar);
    }

    private void processarComando() {
        String comandoTexto = comandoField.getText().trim().toLowerCase();
        if (comandoTexto.isEmpty()) return;

        // Não adicionamos \n aqui, pois System.out.println já o fará
        System.out.println("> " + comandoTexto);
        comandoField.setText(""); // Limpa o campo

        try {
            if (comandoTexto.equals("ajuda")) {
                mostrarAjuda();
            } else if (comandoTexto.equals("status")) {
                if (simulador != null) {
                    System.out.println("Tempo Simulado: " + formatarTempo(simulador.getTempoSimulado()));
                    System.out.println("Simulação " + (simulador.isPausado() ? "PAUSADA" : "EM EXECUÇÃO"));
                    System.out.println("CPs Total: " + (simulador.getTodosOsCaminhoesPequenos() != null ? simulador.getTodosOsCaminhoesPequenos().tamanho() : 0));
                    System.out.println("CGs Frota: " + simulador.getTotalCaminhoesGrandes() +
                            " (Em Uso: " + simulador.getCaminhoesGrandesEmUso() +
                            ", Disp: " + simulador.getCaminhoesGrandesDisponiveis() + ")");
                } else {
                    System.out.println("Simulador não inicializado.");
                }
            } else if (comandoTexto.equals("estatisticas") || comandoTexto.equals("stats")) {
                atualizarEstatisticas(); // Mostra na aba de estatísticas
                System.out.println("Estatísticas atualizadas na aba 'Estatísticas (Tempo Real)'.");
            } else if (comandoTexto.startsWith("salvar ")) {
                String caminho = comandoTexto.substring(7).trim();
                if (simulador != null && !caminho.isEmpty()) {
                    simulador.gravar(caminho); // Simulador já imprime mensagem de sucesso/erro
                } else {
                    System.out.println("Uso: salvar <nome_arquivo.dat>");
                }
            } else if (comandoTexto.startsWith("carregar ")) {
                String caminho = comandoTexto.substring(9).trim();
                if (!caminho.isEmpty()) {
                    this.simulador = Simulador.carregar(caminho); // Simulador já imprime msg
                    System.out.println("Simulação carregada. Reinicie ou continue se aplicável.");
                    habilitarBotoes(true, false, true, true); // Permite iniciar nova ou continuar
                } else {
                    System.out.println("Uso: carregar <nome_arquivo.dat>");
                }
            } else if (comandoTexto.equals("adicionar grande")) {
                if (simulador != null) {
                    simulador.adicionarCaminhaoGrande(); // Simulador já imprime mensagem
                } else {
                    System.out.println("Simulador não iniciado. Não é possível adicionar caminhão.");
                }
            }
            else {
                System.out.println("Comando desconhecido: " + comandoTexto + ". Digite 'ajuda'.");
            }
        } catch (IOException ioe) {
            System.err.println("Erro de I/O ao processar comando: " + ioe.getMessage());
            JOptionPane.showMessageDialog(this, "Erro de I/O: " + ioe.getMessage(), "Erro de Comando", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Erro ao carregar simulação (classe não encontrada): " + cnfe.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao carregar: Classe não encontrada.", "Erro de Comando", JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception e) {
            System.err.println("Erro ao processar comando '" + comandoTexto + "': " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Erro ao processar comando: " + e.getMessage(), "Erro de Comando", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mostrarAjuda() {
        // Usa System.out.println para que as quebras de linha sejam tratadas pelo PrintStream
        System.out.println("\n--- Comandos Disponíveis ---");
        System.out.println("ajuda                - Mostra esta ajuda.");
        System.out.println("status               - Exibe o status atual da simulação.");
        System.out.println("stats / estatisticas - Atualiza e mostra estatísticas na aba correspondente.");
        System.out.println("adicionar grande     - Adiciona um novo caminhão grande à frota.");
        System.out.println("salvar <arquivo.dat> - Salva o estado atual da simulação.");
        System.out.println("carregar <arquivo.dat> - Carrega um estado salvo da simulação.");
        System.out.println("---------------------------");
    }

    public void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensagem); // MODIFICADO: Removido o + "\n"
            // A quebra de linha (\n) deve vir da própria mensagem, ex: System.out.println()
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }

    public void atualizarEstatisticas() {
        if (simulador != null && getSimuladorEstatisticas() != null) {
            Estatisticas stats = getSimuladorEstatisticas();
            StringBuilder sb = new StringBuilder();
            sb.append("--- ESTATÍSTICAS EM TEMPO REAL ---\n");
            sb.append("Tempo Simulado: ").append(formatarTempo(simulador.getTempoSimulado())).append("\n\n");

            sb.append(String.format("Lixo Gerado Total: %,d kg\n", stats.getLixoTotalGerado()));
            sb.append(String.format("Lixo Coletado (CPs): %,d kg (%.2f%%)\n", stats.getLixoTotalColetado(), stats.calcularPercentualLixoColetado()));
            sb.append(String.format("Lixo Transportado (CGs): %,d kg\n\n", stats.getLixoTotalTransportado()));

            sb.append("--- Caminhões Grandes ---\n");
            sb.append(String.format("Frota Total CGs: %d (Inicial: %d, Adicionados: %d)\n",
                    stats.getTotalInicialCaminhoesGrandes() + stats.getCaminhoesGrandesAdicionados(),
                    stats.getTotalInicialCaminhoesGrandes(), stats.getCaminhoesGrandesAdicionados()));
            sb.append(String.format("CGs em Uso (Pico): %d\n", stats.getMaxCaminhoesGrandesEmUsoSimultaneo()));
            sb.append(String.format("Viagens CGs ao Aterro: %d\n\n", stats.getViagensCaminhoesGrandesAterro()));

            sb.append("--- Caminhões Pequenos ---\n");
            sb.append(String.format("CPs Atendidos nas Estações: %d\n", stats.getTotalCaminhoesPequenosAtendidosEstacao()));
            sb.append(String.format("Tempo Médio Espera Fila CPs: %.2f min\n\n", stats.calcularTempoMedioEsperaFilaPequenos()));

            SwingUtilities.invokeLater(() -> estatisticasArea.setText(sb.toString()));
        } else {
            SwingUtilities.invokeLater(() -> estatisticasArea.setText("Simulador não iniciado ou estatísticas indisponíveis.\n"));
        }
    }

    public void atualizarRelatorio() {
        if (simulador != null && getSimuladorEstatisticas() != null) {
            String relatorioCompleto = getSimuladorEstatisticas().gerarRelatorio();
            SwingUtilities.invokeLater(() -> relatorioArea.setText(relatorioCompleto));
        } else {
            SwingUtilities.invokeLater(() -> relatorioArea.setText("Nenhum relatório final para exibir (simulação não encerrada ou não iniciada).\n"));
        }
    }

    private String formatarTempo(int minutosTotais) {
        if (minutosTotais < 0) return "N/A"; // Valor inválido
        int dias = minutosTotais / MINUTOS_EM_UM_DIA;
        int minutosNoDia = minutosTotais % MINUTOS_EM_UM_DIA;
        int horas = minutosNoDia / 60;
        int minutos = minutosNoDia % 60;
        if (dias > 0) {
            return String.format("Dia %d, %02d:%02d (Total: %d min)", dias + 1, horas, minutos, minutosTotais);
        }
        return String.format("%02d:%02d (Total: %d min)", horas, minutos, minutosTotais);
    }

    private void redirecionarSaidaPadrao() {
        OutputStream os = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Redireciona para o JTextArea
                // Esta chamada individual de caractere não deve adicionar \n por si só.
                adicionarLog(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                // A string 's' aqui já contém \n se originou de um println.
                String s = new String(b, off, len, StandardCharsets.UTF_8);
                adicionarLog(s);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
        try {
            PrintStream ps = new PrintStream(os, true, StandardCharsets.UTF_8.name());
            System.setOut(ps);
            System.setErr(ps);
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Codificação UTF-8 não suportada para redirecionamento de saída: " + e.getMessage());
            PrintStream psFallback = new PrintStream(os, true);
            System.setOut(psFallback);
            System.setErr(psFallback);
        }
    }

    public void iniciar() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }

    public Estatisticas getSimuladorEstatisticas() {
        if (simulador != null) {
            return simulador.getEstatisticas();
        }
        return null;
    }
}