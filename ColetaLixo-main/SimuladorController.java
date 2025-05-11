import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import caminhoes.CaminhaoGrandePadrao;
import estacoes.EstacaoPadrao;
import estacoes.EstacaoTransferencia;
import zonas.ZonaUrbana;
import Estruturas.Lista;
import caminhoes.StatusCaminhao;

public class SimuladorController {

    private Simulador simulador;
    private Stage primaryStage;
    private Timeline atualizacaoUITimer;
    private static final int MINUTOS_EM_UM_DIA = 24 * 60;

    // --- IDs dos Componentes FXML ---
    @FXML private Button btnIniciar;
    @FXML private Button btnPausar;
    @FXML private Button btnContinuar;
    @FXML private Button btnEncerrar;
    @FXML private Button btnAdicionarCaminhaoGrande;
    @FXML private Button btnGerarRelatorio;
    @FXML private Label lblZonaSulNome;
    @FXML private Label lblZonaSulLixoGerado;
    @FXML private Label lblZonaSulLixoColetado;
    @FXML private Label lblZonaNorteNome;
    @FXML private Label lblZonaNorteLixoGerado;
    @FXML private Label lblZonaNorteLixoColetado;
    @FXML private Label lblZonaCentroNome;
    @FXML private Label lblZonaCentroLixoGerado;
    @FXML private Label lblZonaCentroLixoColetado;
    @FXML private Label lblZonaLesteNome;
    @FXML private Label lblZonaLesteLixoGerado;
    @FXML private Label lblZonaLesteLixoColetado;
    @FXML private Label lblZonaSudesteNome;
    @FXML private Label lblZonaSudesteLixoGerado;
    @FXML private Label lblZonaSudesteLixoColetado;
    @FXML private Label lblTempoSimulado;
    @FXML private Label lblMetricaLixoTotal;
    @FXML private Label lblMetricaLixoColetado;
    @FXML private Label lblMetricaLixoTransportado;
    @FXML private Label lblMetricaPequenosTotal;
    @FXML private Label lblMetricaPequenosEmAtividade;
    @FXML private Label lblMetricaGrandesTotal;
    @FXML private Label lblMetricaGrandesEmUso;
    @FXML private Label lblEstacaoANome;
    @FXML private Label lblEstacaoAStatus;
    @FXML private Label lblEstacaoACaminhoesFila;
    @FXML private Label lblEstacaoATempoEsperaMedio;
    @FXML private Label lblEstacaoALixoTransferido;
    @FXML private Label lblEstacaoACaminhaoGrandeInfo;
    @FXML private ListView<String> listViewEstacaoAFilaCP;
    @FXML private Label lblEstacaoBNome;
    @FXML private Label lblEstacaoBStatus;
    @FXML private Label lblEstacaoBCaminhoesFila;
    @FXML private Label lblEstacaoBTempoEsperaMedio;
    @FXML private Label lblEstacaoBLixoTransferido;
    @FXML private Label lblEstacaoBCaminhaoGrandeInfo;
    @FXML private ListView<String> listViewEstacaoBFilaCP;
    @FXML private VBox vboxCaminhoesEmAtividade;
    @FXML private TextArea areaLog;
    @FXML private TextArea areaRelatorioFinal;

    // --- Componentes de Configuração ---
    @FXML private Spinner<Integer> spinnerCaminhoes2T;
    @FXML private Spinner<Integer> spinnerCaminhoes4T;
    @FXML private Spinner<Integer> spinnerCaminhoes8T;
    @FXML private Spinner<Integer> spinnerCaminhoes10T;
    @FXML private Spinner<Integer> spinnerCaminhoesGrandes;
    @FXML private Spinner<Integer> spinnerZonaSulMin;
    @FXML private Spinner<Integer> spinnerZonaSulMax;
    @FXML private Spinner<Integer> spinnerZonaNorteMin;
    @FXML private Spinner<Integer> spinnerZonaNorteMax;
    @FXML private Spinner<Integer> spinnerZonaCentroMin;
    @FXML private Spinner<Integer> spinnerZonaCentroMax;
    @FXML private Spinner<Integer> spinnerZonaLesteMin;
    @FXML private Spinner<Integer> spinnerZonaLesteMax;
    @FXML private Spinner<Integer> spinnerZonaSudesteMin;
    @FXML private Spinner<Integer> spinnerZonaSudesteMax;
    @FXML private Spinner<Integer> spinnerEstacaoATempoEspera;
    @FXML private Spinner<Integer> spinnerEstacaoBTempoEspera;
    @FXML private Spinner<Integer> spinnerToleranciaCG;
    @FXML private Spinner<Integer> spinnerLimiteViagensDiarias;
    @FXML private Spinner<Integer> spinnerCaminhoesPorZona;
    @FXML private CheckBox checkBoxUsarGaragem;
    @FXML private CheckBox checkBoxGarantirDistribuicao;
    @FXML private Button btnAplicarConfig;

    // --- Interface funcional customizada para três argumentos ---
    @FunctionalInterface
    public interface TriConsumerCustom<T, U, V> {
        void accept(T t, U u, V v);
    }

    // --- Interface funcional customizada para seis argumentos (para atualizarEstacaoUI) ---
    @FunctionalInterface
    public interface SixConsumerCustom<A, B, C, D, E, F> {
        void accept(A a, B b, C c, D d, E e, F f);
    }

    public void setSimulador(Simulador simulador) {
        this.simulador = simulador;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        // Inicializar componentes da UI
        btnPausar.setDisable(true);
        btnContinuar.setDisable(true);
        btnEncerrar.setDisable(true);

        // Configurar placeholders
        listViewEstacaoAFilaCP.setPlaceholder(new Label("Fila vazia"));
        listViewEstacaoBFilaCP.setPlaceholder(new Label("Fila vazia"));

        // Configurar nomes das zonas
        if (lblZonaSulNome != null) lblZonaSulNome.setText("Sul");
        if (lblZonaNorteNome != null) lblZonaNorteNome.setText("Norte");
        if (lblZonaCentroNome != null) lblZonaCentroNome.setText("Centro");
        if (lblZonaLesteNome != null) lblZonaLesteNome.setText("Leste");
        if (lblZonaSudesteNome != null) lblZonaSudesteNome.setText("Sudeste");

        // Configurar nomes das estações
        if (lblEstacaoANome != null) lblEstacaoANome.setText("Estação A");
        if (lblEstacaoBNome != null) lblEstacaoBNome.setText("Estação B");

        // Inicializar labels
        atualizarLabelsZonas(null, null, null, null, null);
        atualizarLabelsMetricasGerais();
        atualizarLabelsEstacoes(null, null);
        lblTempoSimulado.setText("00:00 (Simulado)");

        // Configurar spinners (se existirem)
        configurarSpinners();

        // Configurar checkboxes (se existirem)
        configurarCheckBoxes();

        // Configurar timer de atualização
        atualizacaoUITimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (simulador != null && !simulador.isPausado()) {
                atualizarInterfaceCompleta();
            }
        }));
        atualizacaoUITimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void configurarSpinners() {
        // Verificar se os spinners existem e configurá-los
        if (spinnerCaminhoes2T != null) {
            spinnerCaminhoes2T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 2));
        }
        if (spinnerCaminhoes4T != null) {
            spinnerCaminhoes4T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        }
        if (spinnerCaminhoes8T != null) {
            spinnerCaminhoes8T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        }
        if (spinnerCaminhoes10T != null) {
            spinnerCaminhoes10T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        }
        if (spinnerCaminhoesGrandes != null) {
            spinnerCaminhoesGrandes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 1));
        }

        // Spinners para zonas
        if (spinnerZonaSulMin != null) {
            spinnerZonaSulMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        }
        if (spinnerZonaSulMax != null) {
            spinnerZonaSulMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        }
        if (spinnerZonaNorteMin != null) {
            spinnerZonaNorteMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        }
        if (spinnerZonaNorteMax != null) {
            spinnerZonaNorteMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        }
        if (spinnerZonaCentroMin != null) {
            spinnerZonaCentroMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        }
        if (spinnerZonaCentroMax != null) {
            spinnerZonaCentroMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        }
        if (spinnerZonaLesteMin != null) {
            spinnerZonaLesteMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        }
        if (spinnerZonaLesteMax != null) {
            spinnerZonaLesteMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        }
        if (spinnerZonaSudesteMin != null) {
            spinnerZonaSudesteMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        }
        if (spinnerZonaSudesteMax != null) {
            spinnerZonaSudesteMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        }

        // Spinners para estações
        if (spinnerEstacaoATempoEspera != null) {
            spinnerEstacaoATempoEspera.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30, 5));
        }
        if (spinnerEstacaoBTempoEspera != null) {
            spinnerEstacaoBTempoEspera.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30, 5));
        }

        // Outros spinners
        if (spinnerToleranciaCG != null) {
            spinnerToleranciaCG.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30, 5));
        }
        if (spinnerLimiteViagensDiarias != null) {
            spinnerLimiteViagensDiarias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10, 1));
        }
        if (spinnerCaminhoesPorZona != null) {
            spinnerCaminhoesPorZona.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1));
        }
    }

    private void configurarCheckBoxes() {
        // Configurar interdependência dos checkboxes
        if (checkBoxUsarGaragem != null && checkBoxGarantirDistribuicao != null && spinnerCaminhoesPorZona != null) {
            checkBoxUsarGaragem.setSelected(true);
            checkBoxGarantirDistribuicao.setSelected(true);

            // Quando "Usar Garagem Central" é desmarcado, desabilita as opções relacionadas
            checkBoxUsarGaragem.selectedProperty().addListener((obs, oldVal, newVal) -> {
                checkBoxGarantirDistribuicao.setDisable(!newVal);
                spinnerCaminhoesPorZona.setDisable(!newVal);
            });
        }
    }

    public void inicializarInterface() {
        adicionarLog("Interface do Simulador inicializada.");
    }

    @FXML
    private void handleIniciarSimulacao(ActionEvent event) {
        try {
            if (simulador == null) {
                simulador = new Simulador();
                adicionarLog("Simulador (re)criado. Aplique configurações da aba 'Configurações' ou use os padrões.");
            } else if (simulador.getTempoSimulado() > 0 && !simulador.isPausado()){
                simulador = new Simulador();
                adicionarLog("Simulação anterior encerrada. Iniciando uma nova simulação.");
            }

            if (simulador.getListaZonas() == null || simulador.getListaZonas().estaVazia() ||
                    simulador.getListaEstacoes() == null || simulador.getListaEstacoes().estaVazia() ||
                    simulador.getTodosOsCaminhoesPequenos() == null || simulador.getTodosOsCaminhoesPequenos().estaVazia()) {

                configurarSimuladorComValoresPadraoExemplo();
                adicionarLog("Simulador configurado com valores padrão de exemplo.");
            } else {
                // Verificar se existem pelo menos 5 caminhões pequenos
                int totalCaminhoesPequenos = simulador.getTodosOsCaminhoesPequenos().tamanho();
                if (totalCaminhoesPequenos < 5) {
                    // Adicionar mais caminhões pequenos para atingir o mínimo de 5
                    Lista<CaminhaoPequeno> listaCPs = simulador.getTodosOsCaminhoesPequenos();
                    int caminhoesFaltantes = 5 - totalCaminhoesPequenos;

                    for (int i = 0; i < caminhoesFaltantes; i++) {
                        // Adicionar caminhões de capacidades variadas
                        int capacidade = (i % 4 == 0) ? 2000 :
                                (i % 4 == 1) ? 4000 :
                                        (i % 4 == 2) ? 8000 : 10000;
                        listaCPs.adicionar(new CaminhaoPequenoPadrao(capacidade));
                    }
                    simulador.setListaCaminhoesPequenos(listaCPs);
                    adicionarLog("Adicionados " + caminhoesFaltantes +
                            " caminhões pequenos para atingir o mínimo de 5.");
                }
            }

            simulador.iniciar();
            atualizacaoUITimer.play();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(false);
            adicionarLog("Simulação iniciada com " + simulador.getTodosOsCaminhoesPequenos().tamanho() +
                    " caminhões pequenos.");

            TabPane tabPane = findTabPaneParent(lblTempoSimulado);
            if (tabPane != null) {
                tabPane.getSelectionModel().select(0); // Seleciona a primeira aba (Simulação Principal)
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao iniciar a simulação");
            alert.setHeaderText("Erro de Configuração");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

            adicionarLog("Falha ao iniciar: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void configurarSimuladorComValoresPadraoExemplo() {
        // Criando lista com 5 caminhões pequenos (requisito mínimo)
        Lista<CaminhaoPequeno> listaCP = new Lista<>();
        // Caminhões de 2 Toneladas
        listaCP.adicionar(new CaminhaoPequenoPadrao(2000));
        listaCP.adicionar(new CaminhaoPequenoPadrao(2000));
        // Caminhões de 4 Toneladas
        listaCP.adicionar(new CaminhaoPequenoPadrao(4000));
        // Caminhões de 8 Toneladas
        listaCP.adicionar(new CaminhaoPequenoPadrao(8000));
        // Caminhões de 10 Toneladas
        listaCP.adicionar(new CaminhaoPequenoPadrao(10000));

        simulador.setListaCaminhoesPequenos(listaCP);

        // Criando um caminhão grande inicial
        Lista<CaminhaoGrande> listaCG = new Lista<>();
        listaCG.adicionar(new CaminhaoGrandePadrao(30));
        simulador.setListaCaminhoesGrandes(listaCG);

        // Configurando as zonas
        Lista<ZonaUrbana> listaZ = new Lista<>();
        String[] nomesZonas = {"Sul", "Norte", "Centro", "Leste", "Sudeste"};
        for (String nome : nomesZonas) {
            ZonaUrbana z = new ZonaUrbana(nome);
            z.setIntervaloGeracao(100, 500);
            listaZ.adicionar(z);
        }
        simulador.setListaZonas(listaZ);

        // Configurando as estações
        Lista<EstacaoTransferencia> listaE = new Lista<>();
        listaE.adicionar(new EstacaoPadrao("Estação A", 30));
        listaE.adicionar(new EstacaoPadrao("Estação B", 30));
        simulador.setListaEstacoes(listaE);

        // Configurando outros parâmetros
        simulador.setLimiteViagensDiarias(10);
        simulador.setUsarGaragemCentral(true);
        simulador.setGarantirDistribuicaoMinima(true);
        simulador.setCaminhoesPorZonaMinimo(1);

        adicionarLog("Simulador configurado com: 5 caminhões pequenos (2x2T, 1x4T, 1x8T, 1x10T), 1 caminhão grande, 5 zonas e 2 estações");
    }

    @FXML
    private void handleAplicarConfiguracao(ActionEvent event) {
        try {
            // Criar nova simulação ou verificar se já existe uma em andamento
            if (simulador == null) {
                simulador = new Simulador();
            } else if (simulador.getTempoSimulado() > 0) {
                // Se uma simulação já estiver em progresso, pergunte antes de substituir
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Substituir Simulação");
                alert.setHeaderText("Uma simulação já está em progresso.");
                alert.setContentText("Deseja substituir a simulação atual por uma nova?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    simulador = new Simulador();
                } else {
                    return; // Usuário cancelou
                }
            }

            // Configurar caminhões pequenos
            Lista<CaminhaoPequeno> listaCP = new Lista<>();
            int qtd2T = spinnerCaminhoes2T.getValue();
            int qtd4T = spinnerCaminhoes4T.getValue();
            int qtd8T = spinnerCaminhoes8T.getValue();
            int qtd10T = spinnerCaminhoes10T.getValue();

            // Adicionar caminhões por tipo
            for (int i = 0; i < qtd2T; i++) {
                listaCP.adicionar(new CaminhaoPequenoPadrao(2000));
            }
            for (int i = 0; i < qtd4T; i++) {
                listaCP.adicionar(new CaminhaoPequenoPadrao(4000));
            }
            for (int i = 0; i < qtd8T; i++) {
                listaCP.adicionar(new CaminhaoPequenoPadrao(8000));
            }
            for (int i = 0; i < qtd10T; i++) {
                listaCP.adicionar(new CaminhaoPequenoPadrao(10000));
            }

            // Verificar mínimo de 5 caminhões
            int totalCP = listaCP.tamanho();
            if (totalCP < 5) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ajuste Automático");
                alert.setHeaderText("Número mínimo de caminhões não atingido");
                alert.setContentText("A simulação requer pelo menos 5 caminhões pequenos. " +
                        "Serão adicionados " + (5 - totalCP) + " caminhões de 4 toneladas.");
                alert.showAndWait();

                // Adicionar caminhões extras para atingir o mínimo
                for (int i = 0; i < 5 - totalCP; i++) {
                    listaCP.adicionar(new CaminhaoPequenoPadrao(4000));
                }
            }

            simulador.setListaCaminhoesPequenos(listaCP);

            // Configurar caminhões grandes
            Lista<CaminhaoGrande> listaCG = new Lista<>();
            int qtdCG = spinnerCaminhoesGrandes.getValue();
            int toleranciaCG = spinnerToleranciaCG.getValue();

            for (int i = 0; i < qtdCG; i++) {
                listaCG.adicionar(new CaminhaoGrandePadrao(toleranciaCG));
            }

            simulador.setListaCaminhoesGrandes(listaCG);
            simulador.setToleranciaCaminhoesGrandes(toleranciaCG);

            // Configurar zonas
            Lista<ZonaUrbana> listaZ = new Lista<>();

            ZonaUrbana zonaSul = new ZonaUrbana("Sul");
            zonaSul.setIntervaloGeracao(spinnerZonaSulMin.getValue(), spinnerZonaSulMax.getValue());
            listaZ.adicionar(zonaSul);

            ZonaUrbana zonaNorte = new ZonaUrbana("Norte");
            zonaNorte.setIntervaloGeracao(spinnerZonaNorteMin.getValue(), spinnerZonaNorteMax.getValue());
            listaZ.adicionar(zonaNorte);

            ZonaUrbana zonaCentro = new ZonaUrbana("Centro");
            zonaCentro.setIntervaloGeracao(spinnerZonaCentroMin.getValue(), spinnerZonaCentroMax.getValue());
            listaZ.adicionar(zonaCentro);

            ZonaUrbana zonaLeste = new ZonaUrbana("Leste");
            zonaLeste.setIntervaloGeracao(spinnerZonaLesteMin.getValue(), spinnerZonaLesteMax.getValue());
            listaZ.adicionar(zonaLeste);

            ZonaUrbana zonaSudeste = new ZonaUrbana("Sudeste");
            zonaSudeste.setIntervaloGeracao(spinnerZonaSudesteMin.getValue(), spinnerZonaSudesteMax.getValue());
            listaZ.adicionar(zonaSudeste);

            simulador.setListaZonas(listaZ);

            // Configurar estações
            Lista<EstacaoTransferencia> listaE = new Lista<>();
            listaE.adicionar(new EstacaoPadrao("Estação A", spinnerEstacaoATempoEspera.getValue()));
            listaE.adicionar(new EstacaoPadrao("Estação B", spinnerEstacaoBTempoEspera.getValue()));
            simulador.setListaEstacoes(listaE);

            // Configurações adicionais
            simulador.setLimiteViagensDiarias(spinnerLimiteViagensDiarias.getValue());
            simulador.setUsarGaragemCentral(checkBoxUsarGaragem.isSelected());
            simulador.setGarantirDistribuicaoMinima(checkBoxGarantirDistribuicao.isSelected());
            simulador.setCaminhoesPorZonaMinimo(spinnerCaminhoesPorZona.getValue());

            // Iniciar simulação
            simulador.iniciar();
            atualizacaoUITimer.play();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(false);

            // Mudar para a aba de simulação
            TabPane tabPane = findTabPaneParent(lblTempoSimulado);
            if (tabPane != null) {
                tabPane.getSelectionModel().select(0); // Seleciona a primeira aba (Simulação Principal)
            }

            // Adicionar log
            adicionarLog("Simulação iniciada com configurações personalizadas: " +
                    listaCP.tamanho() + " caminhões pequenos, " +
                    listaCG.tamanho() + " caminhões grandes.");

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Configuração");
            alert.setHeaderText("Falha ao aplicar configurações");
            alert.setContentText("Erro: " + e.getMessage());
            alert.showAndWait();
            adicionarLog("Erro ao aplicar configurações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePausarSimulacao(ActionEvent event) {
        if (simulador != null) {
            simulador.pausar();
            atualizacaoUITimer.pause();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(true);
            btnContinuar.setDisable(false);
            btnEncerrar.setDisable(false);
            adicionarLog("Simulação pausada.");
        }
    }

    @FXML
    private void handleContinuarSimulacao(ActionEvent event) {
        if (simulador != null) {
            simulador.continuarSimulacao();
            atualizacaoUITimer.play();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(false);
            adicionarLog("Simulação continuada.");
        }
    }

    @FXML
    private void handleEncerrarSimulacao(ActionEvent event) {
        if (simulador != null) {
            simulador.encerrar();
            atualizacaoUITimer.stop();
            atualizarInterfaceCompleta();
            btnIniciar.setDisable(false);
            btnPausar.setDisable(true);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(true);
            adicionarLog("Simulação encerrada. Relatório final disponível.");
            exibirRelatorioFinal();
        }
    }

    @FXML
    private void handleAdicionarCaminhaoGrande(ActionEvent event) {
        if (simulador != null) {
            simulador.adicionarCaminhaoGrande();
            adicionarLog("Novo caminhão grande adicionado à frota.");
            atualizarLabelsMetricasGerais();
        } else {
            adicionarLog("Simulador não iniciado. Não é possível adicionar caminhão grande.");
        }
    }

    @FXML
    private void handleGerarRelatorio(ActionEvent event) {
        if (simulador != null && simulador.getEstatisticas() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Relatório da Simulação");
            fileChooser.setInitialFileName("relatorio_simulacao_" + System.currentTimeMillis() + ".txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto (*.txt)", "*.txt"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    simulador.getEstatisticas().salvarRelatorio(file.getAbsolutePath());
                    adicionarLog("Relatório salvo em: " + file.getAbsolutePath());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Relatório salvo com sucesso!", ButtonType.OK);
                    alert.showAndWait();
                } catch (IOException e) {
                    adicionarLog("Erro ao salvar relatório: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar relatório: " + e.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                }
            }
        } else {
            adicionarLog("Nenhuma simulação ativa ou estatísticas disponíveis para gerar relatório.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Nenhuma simulação ativa ou estatísticas disponíveis.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void atualizarInterfaceCompleta() {
        if (simulador == null) return;
        Platform.runLater(() -> {
            lblTempoSimulado.setText(formatarTempo(simulador.getTempoSimulado()) + " (Simulado)");
            Lista<ZonaUrbana> zonas = simulador.getListaZonas();
            if (zonas != null && zonas.tamanho() >= 5) {
                atualizarLabelsZonas(zonas.obter(0), zonas.obter(1), zonas.obter(2), zonas.obter(3), zonas.obter(4));
            } else if (zonas != null) {
                ZonaUrbana[] arrZonas = new ZonaUrbana[5];
                for(int i=0; i < zonas.tamanho() && i < 5; i++) arrZonas[i] = zonas.obter(i);
                atualizarLabelsZonas(arrZonas[0], arrZonas[1], arrZonas[2], arrZonas[3], arrZonas[4]);
            } else {
                atualizarLabelsZonas(null, null, null, null, null);
            }
            atualizarLabelsMetricasGerais();
            Lista<EstacaoTransferencia> estacoes = simulador.getListaEstacoes();
            if (estacoes != null && estacoes.tamanho() >= 2) {
                if (estacoes.obter(0) instanceof EstacaoPadrao && estacoes.obter(1) instanceof EstacaoPadrao) {
                    atualizarLabelsEstacoes((EstacaoPadrao) estacoes.obter(0), (EstacaoPadrao) estacoes.obter(1));
                } else {
                    atualizarLabelsEstacoes(null,null);
                }
            } else if (estacoes != null && estacoes.tamanho() == 1 && estacoes.obter(0) instanceof EstacaoPadrao) {
                atualizarLabelsEstacoes((EstacaoPadrao) estacoes.obter(0), null);
            } else {
                atualizarLabelsEstacoes(null, null);
            }
            atualizarListaCaminhoesAtividade();
        });
    }

    private void atualizarLabelsZonas(ZonaUrbana zonaSul, ZonaUrbana zonaNorte, ZonaUrbana zonaCentro, ZonaUrbana zonaLeste, ZonaUrbana zonaSudeste) {
        Estatisticas stats = simulador != null ? simulador.getEstatisticas() : null;

        // Usando a interface TriConsumerCustom definida dentro desta classe
        TriConsumerCustom<Label, Label, ZonaUrbana> atualizarZonaUI = (lblGerado, lblColetado, zona) -> {
            if (zona != null && stats != null) {
                int gerado = stats.getLixoGeradoPorZona(zona.getNome());
                int coletado = stats.getLixoColetadoPorZona(zona.getNome());
                lblGerado.setText("Gerado: " + gerado + " kg");
                lblColetado.setText("Coletado: " + coletado + " kg");
            } else if (zona != null) {
                lblGerado.setText("Gerado: " + zona.getLixoAcumulado() + " kg (atual)");
                lblColetado.setText("Coletado: -- kg");
            }
            else {
                lblGerado.setText("Gerado: -- kg");
                lblColetado.setText("Coletado: -- kg");
            }
        };

        if (lblZonaSulLixoGerado != null) atualizarZonaUI.accept(lblZonaSulLixoGerado, lblZonaSulLixoColetado, zonaSul);
        if (lblZonaNorteLixoGerado != null) atualizarZonaUI.accept(lblZonaNorteLixoGerado, lblZonaNorteLixoColetado, zonaNorte);
        if (lblZonaCentroLixoGerado != null) atualizarZonaUI.accept(lblZonaCentroLixoGerado, lblZonaCentroLixoColetado, zonaCentro);
        if (lblZonaLesteLixoGerado != null) atualizarZonaUI.accept(lblZonaLesteLixoGerado, lblZonaLesteLixoColetado, zonaLeste);
        if (lblZonaSudesteLixoGerado != null) atualizarZonaUI.accept(lblZonaSudesteLixoGerado, lblZonaSudesteLixoColetado, zonaSudeste);
    }

    private void atualizarLabelsMetricasGerais() {
        Estatisticas stats = simulador != null ? simulador.getEstatisticas() : null;
        if (stats != null) {
            lblMetricaLixoTotal.setText(String.format("%,d", stats.getLixoTotalGerado()));
            lblMetricaLixoColetado.setText(String.format("%,d", stats.getLixoTotalColetado()));
            lblMetricaLixoTransportado.setText(String.format("%,d", stats.getLixoTotalTransportado()));
            int pequenosTotal = simulador.getTodosOsCaminhoesPequenos() != null ? simulador.getTodosOsCaminhoesPequenos().tamanho() : 0;
            int pequenosAtivos = 0;
            if (simulador.getTodosOsCaminhoesPequenos() != null) {
                for (int i = 0; i < simulador.getTodosOsCaminhoesPequenos().tamanho(); i++) {
                    CaminhaoPequeno cp = simulador.getTodosOsCaminhoesPequenos().obter(i);
                    if (cp.getStatus() != StatusCaminhao.OCIOSO && cp.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        pequenosAtivos++;
                    }
                }
            }
            lblMetricaPequenosTotal.setText(String.valueOf(pequenosTotal));
            lblMetricaPequenosEmAtividade.setText(String.valueOf(pequenosAtivos));
            lblMetricaGrandesTotal.setText(String.valueOf(simulador.getTotalCaminhoesGrandes()));
            lblMetricaGrandesEmUso.setText(String.valueOf(simulador.getCaminhoesGrandesEmUso()));
        } else {
            String defaultText = "0";
            lblMetricaLixoTotal.setText(defaultText);
            lblMetricaLixoColetado.setText(defaultText);
            lblMetricaLixoTransportado.setText(defaultText);
            lblMetricaPequenosTotal.setText(defaultText);
            lblMetricaPequenosEmAtividade.setText(defaultText);
            lblMetricaGrandesTotal.setText(defaultText);
            lblMetricaGrandesEmUso.setText(defaultText);
        }
    }

    private void atualizarLabelsEstacoes(EstacaoPadrao estacaoA, EstacaoPadrao estacaoB) {
        Estatisticas stats = simulador != null ? simulador.getEstatisticas() : null;

        // Usando a interface SixConsumerCustom definida dentro desta classe
        SixConsumerCustom<Label, Label, Label, Label, ListView<String>, EstacaoPadrao> atualizarEstacaoUI =
                (lblStatus, lblFila, lblEspera, lblTransf, listView, estacao) -> {
                    Label lblCGInfo = (listView == listViewEstacaoAFilaCP) ? lblEstacaoACaminhaoGrandeInfo : lblEstacaoBCaminhaoGrandeInfo;
                    if (estacao != null) {
                        lblStatus.setText("Operacional");
                        lblFila.setText(String.valueOf(estacao.getCaminhoesNaFila()));
                        lblEspera.setText(String.format("%.0f min", stats != null ? stats.getTempoMedioEsperaPorEstacao(estacao.getNome()) : 0));
                        lblTransf.setText(String.format("%,d kg", stats != null ? stats.getLixoTransferidoPorEstacao(estacao.getNome()) : 0));
                        if (lblCGInfo != null) {
                            if (estacao.temCaminhaoGrande()) {
                                CaminhaoGrande cg = estacao.getCaminhaoGrandeAtual();
                                String idCG = (estacao.getNome().equals("Estação A")) ? "G1" : "G2";
                                lblCGInfo.setText(String.format("%s (%d/%d kg)", idCG, cg.getCargaAtual(), cg.getCapacidadeMaxima()));
                            } else {
                                lblCGInfo.setText("Sem CG");
                            }
                        }
                        ObservableList<String> filaItems = FXCollections.observableArrayList();
                        Lista<CaminhaoPequeno> snapshotFila = estacao.getFilaCaminhoesPequenosSnapshot();
                        if (snapshotFila != null) {
                            for (int i = 0; i < snapshotFila.tamanho(); i++) {
                                filaItems.add(snapshotFila.obter(i).getPlaca());
                            }
                        }
                        listView.setItems(filaItems);
                    } else {
                        lblStatus.setText("--");
                        lblFila.setText("0");
                        lblEspera.setText("0 min");
                        lblTransf.setText("0 kg");
                        if (lblCGInfo != null) lblCGInfo.setText("Sem CG");
                        listView.getItems().clear();
                    }
                };
        if (lblEstacaoAStatus != null) atualizarEstacaoUI.accept(lblEstacaoAStatus, lblEstacaoACaminhoesFila, lblEstacaoATempoEsperaMedio, lblEstacaoALixoTransferido, listViewEstacaoAFilaCP, estacaoA);
        if (lblEstacaoBStatus != null) atualizarEstacaoUI.accept(lblEstacaoBStatus, lblEstacaoBCaminhoesFila, lblEstacaoBTempoEsperaMedio, lblEstacaoBLixoTransferido, listViewEstacaoBFilaCP, estacaoB);
    }

    private void atualizarListaCaminhoesAtividade() {
        if (vboxCaminhoesEmAtividade == null) return;
        vboxCaminhoesEmAtividade.getChildren().clear();
        if (simulador != null && simulador.getTodosOsCaminhoesPequenos() != null) {
            Lista<CaminhaoPequeno> caminhoes = simulador.getTodosOsCaminhoesPequenos();
            for (int i = 0; i < caminhoes.tamanho(); i++) {
                CaminhaoPequeno cp = caminhoes.obter(i);
                if (cp.getStatus() != StatusCaminhao.OCIOSO && cp.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                    HBox caminhaoBox = criarPainelCaminhao(cp);
                    vboxCaminhoesEmAtividade.getChildren().add(caminhaoBox);
                }
            }
        }
    }

    private HBox criarPainelCaminhao(CaminhaoPequeno caminhao) {
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("caminhao-item");
        hbox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Circle statusIcon = new javafx.scene.shape.Circle(8);
        statusIcon.getStyleClass().add("status-icon");
        switch (caminhao.getStatus()) {
            case COLETANDO: statusIcon.setStyle("-fx-fill: #4CAF50;"); break;
            case VIAJANDO_ESTACAO: case RETORNANDO_ZONA: statusIcon.setStyle("-fx-fill: #FFC107;"); break;
            case NA_FILA: statusIcon.setStyle("-fx-fill: #03A9F4;"); break;
            case DESCARREGANDO: statusIcon.setStyle("-fx-fill: #9C27B0;"); break;
            default: statusIcon.setStyle("-fx-fill: #9E9E9E;");
        }
        Label lblPlacaTipo = new Label(String.format("%s (%d Ton)", caminhao.getPlaca(), caminhao.getCapacidade() / 1000));
        lblPlacaTipo.getStyleClass().add("caminhao-placa");
        String localStatus = "";
        if (caminhao.getZonaAtual() != null) {
            localStatus = "Zona: " + caminhao.getZonaAtual().getNome();
        } else if (caminhao.getEstacaoDestino() != null &&
                (caminhao.getStatus() == StatusCaminhao.VIAJANDO_ESTACAO ||
                        caminhao.getStatus() == StatusCaminhao.NA_FILA ||
                        caminhao.getStatus() == StatusCaminhao.DESCARREGANDO) ) {
            localStatus = "Estação: " + caminhao.getEstacaoDestino().getNome();
        } else {
            localStatus = "Local desconhecido";
        }
        Label lblLocal = new Label(localStatus);
        lblLocal.getStyleClass().add("caminhao-local");
        Label lblStatusCaminhao = new Label("Status: " + caminhao.getStatus().toString().replace("_", " "));
        lblStatusCaminhao.getStyleClass().add("caminhao-status-texto");
        VBox infoLabels = new VBox(2, lblPlacaTipo, new HBox(5,lblLocal, lblStatusCaminhao));
        ProgressBar progressBarCarga = new ProgressBar((double) caminhao.getCargaAtual() / caminhao.getCapacidade());
        progressBarCarga.setPrefWidth(80);
        progressBarCarga.setMinWidth(60);
        Label lblCarga = new Label(String.format("%d/%d kg", caminhao.getCargaAtual(), caminhao.getCapacidade()));
        lblCarga.getStyleClass().add("caminhao-carga-texto");
        lblCarga.setMinWidth(Label.USE_PREF_SIZE);
        HBox cargaBox = new HBox(5, progressBarCarga, lblCarga);
        cargaBox.setAlignment(Pos.CENTER_RIGHT);
        hbox.getChildren().addAll(statusIcon, infoLabels, cargaBox);
        return hbox;
    }

    private String formatarTempo(int minutosTotais) {
        if (minutosTotais < 0) return "00:00";
        int dias = minutosTotais / MINUTOS_EM_UM_DIA;
        int minutosNoDia = minutosTotais % MINUTOS_EM_UM_DIA;
        int horas = minutosNoDia / 60;
        int minutos = minutosNoDia % 60;
        if (dias > 0) {
            return String.format("Dia %d, %02d:%02d", dias + 1, horas, minutos);
        }
        return String.format("%02d:%02d", horas, minutos);
    }

    private void adicionarLog(String mensagem) {
        if (areaLog != null) {
            Platform.runLater(() -> {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                areaLog.appendText(timestamp + " - " + mensagem + "\n");
            });
        } else {
            System.out.println(mensagem);
        }
    }

    public void encerrarTimerAtualizacao() {
        if (atualizacaoUITimer != null) {
            atualizacaoUITimer.stop();
        }
    }

    private void exibirRelatorioFinal() {
        if (simulador != null && simulador.getEstatisticas() != null) {
            String relatorio = simulador.getEstatisticas().gerarRelatorio();
            if (areaRelatorioFinal != null) {
                areaRelatorioFinal.setText(relatorio);
                TabPane tabPane = findTabPaneParent(areaRelatorioFinal);
                if (tabPane != null) {
                    for (Tab tab : tabPane.getTabs()) {
                        if (isParent(tab.getContent(), areaRelatorioFinal)) {
                            tabPane.getSelectionModel().select(tab);
                            break;
                        }
                    }
                }
            } else {
                Alert alertRelatorio = new Alert(Alert.AlertType.INFORMATION);
                alertRelatorio.setTitle("Relatório Final da Simulação");
                alertRelatorio.setHeaderText("Simulação Concluída");
                TextArea textAreaRelatorioContent = new TextArea(relatorio);
                textAreaRelatorioContent.setEditable(false);
                textAreaRelatorioContent.setWrapText(true);
                alertRelatorio.getDialogPane().setContent(textAreaRelatorioContent);
                alertRelatorio.setResizable(true);
                alertRelatorio.showAndWait();
            }
            adicionarLog("\n========= RELATÓRIO FINAL (disponível na aba/janela de relatório) =========\n");
        }
    }

    private TabPane findTabPaneParent(javafx.scene.Node node) {
        if (node == null) return null;
        javafx.scene.Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof TabPane) {
                return (TabPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private boolean isParent(javafx.scene.Node potentialParent, javafx.scene.Node potentialChild) {
        if (potentialChild == null || potentialParent == null) return false;
        javafx.scene.Parent p = potentialChild.getParent();
        while (p != null) {
            if (p.equals(potentialParent)) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }
}
