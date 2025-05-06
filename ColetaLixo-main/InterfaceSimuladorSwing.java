import Estruturas.Lista;
import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoGrandePadrao;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import estacoes.EstacaoPadrao;
import estacoes.EstacaoTransferencia;
import zonas.ZonaUrbana;

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

    // Constantes para capacidades e nomes (para fácil referência aqui)
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


    public InterfaceSimuladorSwing(Simulador simulador) {
        this.simulador = simulador;
        setTitle("Simulador de Coleta de Lixo - Teresina");
        setSize(900, 700); // Aumentado um pouco para acomodar mais campos
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
        logArea = new JTextArea(15, 50); // Ajustado tamanho
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        estatisticasArea = new JTextArea(15, 50);
        estatisticasArea.setEditable(false);
        estatisticasArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        relatorioArea = new JTextArea(15, 50);
        relatorioArea.setEditable(false);
        relatorioArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Campo de comando
        comandoField = new JTextField(30);

        // Componentes de Configuração
        // Caminhões Pequenos
        spinnerCaminhoes2T = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1)); // valor inicial, min, max, step
        spinnerCaminhoes4T = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        spinnerCaminhoes8T = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        spinnerCaminhoes10T = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));

        // Caminhões Grandes
        spinnerCaminhoesGrandes = new JSpinner(new SpinnerNumberModel(1, 0, 50, 1));

        // Zonas (Exemplo para Zona Sul)
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

        // Estações
        spinnerEstacaoATempoEspera = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));
        spinnerEstacaoBTempoEspera = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));

        // Tolerância Caminhões Grandes
        spinnerToleranciaCG = new JSpinner(new SpinnerNumberModel(30, 1, 300, 5));
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

        // --- Aba de Configurações ---
        JPanel configFormPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // Espaçamento
        gbc.anchor = GridBagConstraints.WEST;
        int y = 0; // Linha atual no GridBagLayout

        // Título Caminhões Pequenos
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        configFormPanel.add(new JLabel("<html><b>--- Frota de Caminhões Pequenos ---</b></html>"), gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_2T / 1000) + "T:"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerCaminhoes2T, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_4T / 1000) + "T:"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerCaminhoes4T, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_8T / 1000) + "T:"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerCaminhoes8T, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel("Caminhões " + (CAPACIDADE_10T / 1000) + "T:"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerCaminhoes10T, gbc); y++;

        // Título Caminhões Grandes
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        configFormPanel.add(new JLabel("<html><b>--- Frota de Caminhões Grandes ---</b></html>"), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel("Qtd. Caminhões Grandes (20T):"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerCaminhoesGrandes, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel("Tolerância Espera CG (min):"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerToleranciaCG, gbc); y++;

        // Título Zonas
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        configFormPanel.add(new JLabel("<html><b>--- Configuração das Zonas (Lixo kg/dia) ---</b></html>"), gbc);
        gbc.gridwidth = 1;

        addZonaConfig(configFormPanel, gbc, ZONA_SUL, spinnerZonaSulMin, spinnerZonaSulMax, y); y+=2;
        addZonaConfig(configFormPanel, gbc, ZONA_NORTE, spinnerZonaNorteMin, spinnerZonaNorteMax, y); y+=2;
        addZonaConfig(configFormPanel, gbc, ZONA_CENTRO, spinnerZonaCentroMin, spinnerZonaCentroMax, y); y+=2;
        addZonaConfig(configFormPanel, gbc, ZONA_LESTE, spinnerZonaLesteMin, spinnerZonaLesteMax, y); y+=2;
        addZonaConfig(configFormPanel, gbc, ZONA_SUDESTE, spinnerZonaSudesteMin, spinnerZonaSudesteMax, y); y+=2;

        // Título Estações
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2;
        configFormPanel.add(new JLabel("<html><b>--- Configuração das Estações ---</b></html>"), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel(ESTACAO_A + " - Limite Espera CP (min):"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerEstacaoATempoEspera, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; configFormPanel.add(new JLabel(ESTACAO_B + " - Limite Espera CP (min):"), gbc);
        gbc.gridx = 1; configFormPanel.add(spinnerEstacaoBTempoEspera, gbc); y++;

        // Painel para envolver o formulário e permitir scroll
        JPanel configPanelWrapper = new JPanel(new BorderLayout());
        configPanelWrapper.add(configFormPanel, BorderLayout.NORTH); // Adiciona o formPanel no topo
        JScrollPane configScroll = new JScrollPane(configPanelWrapper);
        tabPane.addTab("Configurações", configScroll);


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

    private void addZonaConfig(JPanel panel, GridBagConstraints gbc, String nomeZona, JSpinner spinnerMin, JSpinner spinnerMax, int startY) {
        gbc.gridx = 0; gbc.gridy = startY; panel.add(new JLabel(nomeZona + " - Mín:"), gbc);
        gbc.gridx = 1; panel.add(spinnerMin, gbc);
        gbc.gridx = 0; gbc.gridy = startY + 1; panel.add(new JLabel(nomeZona + " - Máx:"), gbc);
        gbc.gridx = 1; panel.add(spinnerMax, gbc);
    }


    private void configurarEventos() {
        iniciarBtn.addActionListener(e -> {
            try {
                configurarESetarSimuladorPelaUI();
                simulador.iniciar();
                adicionarLog("Simulação iniciada com configurações da UI.");
                habilitarBotoes(false, true, true, true); // Iniciar desabilitado, Pausar/Continuar/Encerrar habilitados
                tabPane.setSelectedIndex(1); // Mudar para a aba de Logs
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Erro nos valores de configuração: Por favor, verifique os números inseridos.\nDetalhe: " + nfe.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                adicionarLog("Falha ao iniciar: Erro nos valores de configuração.");
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(this, "Erro de configuração: " + iae.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                adicionarLog("Falha ao iniciar: " + iae.getMessage());
            }
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
            simulador.encerrar(); // Isso já deve gerar o relatório via System.out e salvar em arquivo
            adicionarLog("Simulação encerrada. Relatório gerado (ver console/arquivo e aba 'Relatório Final').");
            habilitarBotoes(true, false, false, false); // Reabilita Iniciar, desabilita outros
            atualizarRelatorio(); // Atualiza a aba de relatório
            tabPane.setSelectedIndex(tabPane.getTabCount() - 1); // Vai para a última aba (Relatório Final)
        });

        comandoField.addActionListener(e -> processarComando());
    }

    private void configurarESetarSimuladorPelaUI() {
        adicionarLog("Configurando simulador com parâmetros da UI...");

        // 1. Coletar valores dos JSpinners
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


        // 2. Criar as Listas e Objetos
        Lista<CaminhaoPequeno> listaCaminhoesPequenos = new Lista<>();
        adicionarCaminhoes(listaCaminhoesPequenos, qtd2T, CAPACIDADE_2T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd4T, CAPACIDADE_4T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd8T, CAPACIDADE_8T);
        adicionarCaminhoes(listaCaminhoesPequenos, qtd10T, CAPACIDADE_10T);

        Lista<CaminhaoGrande> listaCaminhoesGrandes = new Lista<>();
        for (int i = 0; i < qtdCG; i++) {
            listaCaminhoesGrandes.adicionar(new CaminhaoGrandePadrao(toleranciaCG)); // Passa a tolerância individual
        }

        Lista<ZonaUrbana> listaZonas = new Lista<>();
        ZonaUrbana zonaSul = new ZonaUrbana(ZONA_SUL);
        zonaSul.setIntervaloGeracao(zonaSulMinVal, zonaSulMaxVal);
        listaZonas.adicionar(zonaSul);

        ZonaUrbana zonaNorte = new ZonaUrbana(ZONA_NORTE);
        zonaNorte.setIntervaloGeracao(zonaNorteMinVal, zonaNorteMaxVal);
        listaZonas.adicionar(zonaNorte);

        ZonaUrbana zonaCentro = new ZonaUrbana(ZONA_CENTRO);
        zonaCentro.setIntervaloGeracao(zonaCentroMinVal, zonaCentroMaxVal);
        listaZonas.adicionar(zonaCentro);

        ZonaUrbana zonaLeste = new ZonaUrbana(ZONA_LESTE);
        zonaLeste.setIntervaloGeracao(zonaLesteMinVal, zonaLesteMaxVal);
        listaZonas.adicionar(zonaLeste);

        ZonaUrbana zonaSudeste = new ZonaUrbana(ZONA_SUDESTE);
        zonaSudeste.setIntervaloGeracao(zonaSudesteMinVal, zonaSudesteMaxVal);
        listaZonas.adicionar(zonaSudeste);


        Lista<EstacaoTransferencia> listaEstacoes = new Lista<>();
        listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_A, estacaoATempoVal));
        listaEstacoes.adicionar(new EstacaoPadrao(ESTACAO_B, estacaoBTempoVal));

        // 3. (Re)Configurar o Simulador
        // Se o simulador for uma nova instância a cada 'iniciar', não precisa de reset explícito aqui.
        // Se for a mesma instância, um método simulador.resetInterno() seria bom antes de setar novas listas.
        // Por ora, assumindo que `simulador.iniciar()` já lida com a reinicialização do estado interno.
        // O `Estatisticas.resetar()` é chamado dentro de `simulador.iniciar()` ou deveria ser.
        // No seu Simulador.java, `iniciar()` chama `estatisticas.resetar()`.

        simulador.setListaCaminhoesPequenos(listaCaminhoesPequenos);
        simulador.setListaCaminhoesGrandes(listaCaminhoesGrandes);
        simulador.setListaZonas(listaZonas);
        simulador.setListaEstacoes(listaEstacoes);
        simulador.setToleranciaCaminhoesGrandes(toleranciaCG); // Define a tolerância padrão para novos CGs adicionados dinamicamente

        adicionarLog("Simulador configurado e pronto para iniciar.");
    }

    private static void adicionarCaminhoes(Lista<CaminhaoPequeno> lista, int quantidade, int capacidade) {
        if (quantidade < 0) return;
        for (int i = 0; i < quantidade; i++) {
            try {
                lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
            } catch (IllegalArgumentException e) {
                // Este erro não deve ocorrer se as capacidades forem positivas.
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
        String comando = comandoField.getText().trim();
        if (comando.isEmpty()) return;

        adicionarLog("> " + comando); // Ecoa o comando no log

        switch (comando.toLowerCase()) {
            case "iniciar":
                // Verifica se o botão iniciar está habilitado para evitar reconfiguração se já estiver rodando
                if (iniciarBtn.isEnabled()) {
                    iniciarBtn.doClick(); // Simula o clique no botão, que já tem a lógica de configuração
                } else {
                    adicionarLog("Simulação já está em andamento ou precisa ser encerrada primeiro.");
                }
                break;
            case "pausar":
                pausarBtn.doClick();
                break;
            case "continuar":
                continuarBtn.doClick();
                break;
            case "encerrar":
                encerrarBtn.doClick();
                break;
            case "estatisticas":
            case "stats":
                atualizarEstatisticas();
                tabPane.setSelectedIndex(2); // Aba de Estatísticas (índice pode mudar se Configurações for 0)
                break;
            case "relatorio":
                atualizarRelatorio();
                tabPane.setSelectedIndex(3); // Aba de Relatório
                break;
            case "adicionar grande":
                if (simulador.isPausado() || !iniciarBtn.isEnabled()){ // Só adiciona se simulação iniciada ou pausada
                    simulador.adicionarCaminhaoGrande();
                    adicionarLog("Caminhão grande adicionado manualmente.");
                } else {
                    adicionarLog("Inicie a simulação para adicionar caminhões grandes dinamicamente.");
                }
                break;
            case "ajuda":
                mostrarAjuda();
                break;
            default:
                adicionarLog("Comando desconhecido: '" + comando + "'. Digite 'ajuda'.");
        }
        comandoField.setText("");
    }

    private void mostrarAjuda() {
        String ajuda = "Comandos disponíveis no campo de texto:\n" +
                "  iniciar    - Configura e inicia uma nova simulação\n" +
                "  pausar     - Pausa a simulação em andamento\n" +
                "  continuar  - Continua uma simulação pausada\n" +
                "  encerrar   - Encerra a simulação e gera relatório\n" +
                "  estatisticas - Exibe estatísticas atuais (na aba Estatísticas)\n" +
                "  relatorio  - Gera relatório completo (na aba Relatório Final)\n" +
                "  adicionar grande - Adiciona um novo caminhão grande à frota (se simulação iniciada)\n" +
                "  ajuda      - Exibe esta mensagem de ajuda\n\n" +
                "Use os botões para as operações principais ou a aba 'Configurações' para definir os parâmetros antes de iniciar.";
        JOptionPane.showMessageDialog(this, ajuda, "Ajuda - Comandos Disponíveis", JOptionPane.INFORMATION_MESSAGE);
    }

    public void adicionarLog(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensagem + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void atualizarEstatisticas() {
        SwingUtilities.invokeLater(() -> {
            estatisticasArea.setText(""); // Limpa
            Estatisticas stats = simulador.getEstatisticas();
            if (stats != null) {
                // Formato resumido para a aba de estatísticas
                estatisticasArea.append("=== ESTATÍSTICAS EM TEMPO REAL ===\n");
                estatisticasArea.append("Tempo Simulado: " + formatarTempo(simulador.getTempoSimulado()) + "\n");
                estatisticasArea.append("Simulação Pausada: " + (simulador.isPausado() ? "Sim" : "Não") + "\n\n");

                estatisticasArea.append(String.format("Lixo Total Gerado: %,d kg\n", stats.getLixoTotalGerado()));
                estatisticasArea.append(String.format("Lixo Total Coletado (CP): %,d kg (%.2f%%)\n", stats.getLixoTotalColetado(), stats.calcularPercentualLixoColetado()));
                estatisticasArea.append(String.format("Lixo Total Transportado (CG): %,d kg\n\n", stats.getLixoTotalTransportado()));

                estatisticasArea.append("--- Caminhões Grandes ---\n");
                estatisticasArea.append("Frota Inicial: " + stats.getTotalInicialCaminhoesGrandes() + "\n");
                estatisticasArea.append("Adicionados: " + stats.getCaminhoesGrandesAdicionados() + "\n");
                estatisticasArea.append("Frota Atual Total: " + (stats.getTotalInicialCaminhoesGrandes() + stats.getCaminhoesGrandesAdicionados()) + "\n");
                estatisticasArea.append("Máx. em Uso Simultâneo: " + stats.getMaxCaminhoesGrandesEmUsoSimultaneo() + "\n");
                estatisticasArea.append("Em Uso Agora: " + simulador.getCaminhoesGrandesEmUso() + "\n");
                estatisticasArea.append("Disponíveis Agora: " + simulador.getCaminhoesGrandesDisponiveis() + "\n");
                estatisticasArea.append("Viagens para Aterro: " + stats.getViagensCaminhoesGrandesAterro() + "\n\n");

                estatisticasArea.append("--- Caminhões Pequenos ---\n");
                estatisticasArea.append("Total Atendidos nas Estações: " + stats.getTotalCaminhoesPequenosAtendidosEstacao() + "\n");
                estatisticasArea.append(String.format("Tempo Médio Espera Fila: %.2f min\n\n", stats.calcularTempoMedioEsperaFilaPequenos()));

                estatisticasArea.append("--- Zonas (Lixo Acumulado Atual) ---\n");
                if (simulador.getListaZonas() != null && !simulador.getListaZonas().estaVazia()) {
                    for (int i = 0; i < simulador.getListaZonas().tamanho(); i++) {
                        ZonaUrbana z = simulador.getListaZonas().obter(i);
                        estatisticasArea.append(String.format("  - %-10s: %,6d kg\n", z.getNome(), z.getLixoAcumulado()));
                    }
                } else {
                    estatisticasArea.append("  (Nenhuma zona configurada na simulação atual)\n");
                }

                estatisticasArea.append("\n--- Estações (Situação Atual) ---\n");
                if (simulador.getListaEstacoes() != null && !simulador.getListaEstacoes().estaVazia()) {
                    for (int i = 0; i < simulador.getListaEstacoes().tamanho(); i++) {
                        if (simulador.getListaEstacoes().obter(i) instanceof EstacaoPadrao) {
                            EstacaoPadrao est = (EstacaoPadrao) simulador.getListaEstacoes().obter(i);
                            estatisticasArea.append(String.format("  - Estação %-10s: %d na fila | CG: %s (Carga: %,d kg)\n",
                                    est.getNome(),
                                    est.getCaminhoesNaFila(),
                                    est.temCaminhaoGrande() ? "Sim" : "Não",
                                    est.getCargaCaminhaoGrandeAtual()));
                        }
                    }
                } else {
                    estatisticasArea.append("  (Nenhuma estação configurada na simulação atual)\n");
                }

            } else {
                estatisticasArea.append("Não há estatísticas disponíveis (simulação não iniciada/encerrada?).");
            }
            estatisticasArea.setCaretPosition(0); // Scroll para o topo
        });
    }

    public void atualizarRelatorio() {
        SwingUtilities.invokeLater(() -> {
            relatorioArea.setText(""); // Limpa
            Estatisticas stats = simulador.getEstatisticas();
            if (stats != null && (stats.getLixoTotalGerado() > 0 || stats.getTotalCaminhoesPequenosAtendidosEstacao() > 0)) { // Verifica se há algo para relatar
                relatorioArea.append(stats.gerarRelatorio());
            } else {
                relatorioArea.append("Nenhum dado para o relatório. A simulação pode não ter sido executada ou encerrada corretamente.");
            }
            relatorioArea.setCaretPosition(0); // Scroll para o topo
        });
    }

    private String formatarTempo(int minutos) {
        int dias = minutos / (24 * 60);
        minutos %= (24*60);
        int horas = minutos / 60;
        int mins = minutos % 60;
        if (dias > 0) {
            return String.format("Dia %d, %02d:%02d", dias + 1, horas, mins);
        }
        return String.format("%02d:%02d", horas, mins);
    }

    private void redirecionarSaidaPadrao() {
        OutputStream out = new OutputStream() {
            private StringBuilder buffer = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                char c = (char) b;
                buffer.append(c);
                if (c == '\n' || buffer.length() > 1024) { // Flush no newline ou se buffer grande
                    final String textoParaLog = buffer.toString();
                    adicionarLog(textoParaLog.endsWith("\n") ? textoParaLog.substring(0, textoParaLog.length() -1) : textoParaLog);
                    buffer.setLength(0); // Limpa o buffer
                }
            }
        };
        PrintStream ps = new PrintStream(out, true); // true para autoFlush
        System.setOut(ps);
        // System.setErr(ps); // Opcional: redirecionar System.err também
    }

    public void iniciar() {
        setVisible(true);
        adicionarLog("Interface de simulação pronta.");
        adicionarLog("Configure os parâmetros na aba 'Configurações' e clique em 'Iniciar Simulação'.");
        adicionarLog("Ou digite 'ajuda' no campo de comando para ver opções.");
        habilitarBotoes(true, false, false, false); // Apenas Iniciar e Encerrar habilitados inicialmente
        pausarBtn.setEnabled(false);
        continuarBtn.setEnabled(false);
        encerrarBtn.setEnabled(true); // Permitir encerrar mesmo antes de iniciar, para limpar
    }

    // Adicionar getters para estatísticas se necessário por outras classes
    public Estatisticas getSimuladorEstatisticas() {
        return simulador.getEstatisticas();
    }
}