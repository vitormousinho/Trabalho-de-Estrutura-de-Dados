import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node; // Importação adicionada


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Locale;


import caminhoes.CaminhaoGrande;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoPequenoPadrao;
import caminhoes.CaminhaoGrandePadrao;
import estacoes.EstacaoPadrao;
import estacoes.EstacaoTransferencia;
import zonas.ZonaUrbana;
import Estruturas.Lista; // Sua classe Lista
import caminhoes.StatusCaminhao;
// Se MetricaEstacaoModel estiver em outro pacote, importe-o
// import seu_pacote.MetricaEstacaoModel;

public class SimuladorController {

    private Simulador simulador;
    private Stage primaryStage;
    private Timeline atualizacaoUITimer;
    private static final int MINUTOS_EM_UM_DIA = 24 * 60;
    private Locale brLocale = new Locale("pt", "BR"); // Para formatação de números

    // --- IDs dos Componentes FXML da Aba Principal ---
    @FXML private Button btnIniciar;
    @FXML private Button btnPausar;
    @FXML private Button btnContinuar;
    @FXML private Button btnEncerrar;
    @FXML private Button btnAdicionarCaminhaoGrande;
    @FXML private Button btnGerarRelatorio; // Para salvar relatório textual
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
    @FXML private TabPane tabPanePrincipal; // Seu TabPane principal
    @FXML private Tab tabRelatorioFinal; // A Tab que conterá o RelatorioFinalTab.fxml

    // --- Componentes da Aba de Relatório Gráfico (RelatorioFinalTab.fxml) ---
    @FXML private BarChart<String, Number> barChartLixoPorZona;
    @FXML private CategoryAxis xAxisZonas; // Não estritamente necessário ter FXML para eixos se configurados em código
    @FXML private NumberAxis yAxisLixoGerado; // Mesmo acima
    @FXML private TableView<MetricaEstacaoModel> tableViewMetricasEstacao;
    @FXML private TableColumn<MetricaEstacaoModel, String> colEstacaoNome;
    @FXML private TableColumn<MetricaEstacaoModel, Integer> colCaminhoesAtendidos;
    @FXML private TableColumn<MetricaEstacaoModel, Double> colTempoMedioEspera;
    @FXML private TableColumn<MetricaEstacaoModel, Integer> colLixoTransferidoEstacao;
    @FXML private Label lblEficienciaColeta;
    @FXML private Label lblDistribuicaoLixo;
    @FXML private Label lblGargalosSistema;
    @FXML private Label lblOtimizacaoFrota;
    @FXML private Label lblConclusaoTexto1;
    @FXML private Label lblConclusaoCaminhoesNecessarios;
    @FXML private Label lblConclusaoTexto2;


    // --- Interfaces Funcionais (se ainda usadas em outro lugar) ---
    @FunctionalInterface
    public interface TriConsumerCustom<T, U, V> {
        void accept(T t, U u, V v);
    }

    @FunctionalInterface
    public interface SixConsumerCustom<A, B, C, D, E, F> {
        void accept(A a, B b, C c, D d, E e, F f);
    }

    // --- Métodos de Setup ---
    public void setSimulador(Simulador simulador) {
        this.simulador = simulador;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
        // Configurações iniciais dos botões e labels da aba principal
        btnPausar.setDisable(true);
        btnContinuar.setDisable(true);
        btnEncerrar.setDisable(true);

        if (listViewEstacaoAFilaCP != null) listViewEstacaoAFilaCP.setPlaceholder(new Label("Fila vazia"));
        if (listViewEstacaoBFilaCP != null) listViewEstacaoBFilaCP.setPlaceholder(new Label("Fila vazia"));

        // Nomes fixos podem ser definidos diretamente no FXML, mas aqui para consistência
        if (lblZonaSulNome != null) lblZonaSulNome.setText("Sul");
        // ... outros nomes de zonas e estações ...
        if (lblEstacaoANome != null) lblEstacaoANome.setText("Estação A");
        if (lblEstacaoBNome != null) lblEstacaoBNome.setText("Estação B");


        atualizarLabelsZonas(null, null, null, null, null); // Atualiza com dados nulos inicialmente
        atualizarLabelsMetricasGerais();
        atualizarLabelsEstacoes(null, null);
        if (lblTempoSimulado != null) lblTempoSimulado.setText("00:00 (Simulado)");

        configurarSpinners();
        configurarCheckBoxes();

        // Carregar o conteúdo da aba de relatório final
        if (tabRelatorioFinal != null) {
            try {
                // Certifique-se que "RelatorioFinalTab.fxml" está na mesma pasta que esta classe
                // ou ajuste o caminho getResource("/caminho/para/RelatorioFinalTab.fxml")
                FXMLLoader loader = new FXMLLoader(getClass().getResource("RelatorioFinalTab.fxml"));
                loader.setController(this); // Importante: esta classe também controla o FXML da aba de relatório
                Node relatorioNode = loader.load();
                tabRelatorioFinal.setContent(relatorioNode);
            } catch (IOException e) {
                adicionarLog("ERRO CRÍTICO: Não foi possível carregar RelatorioFinalTab.fxml. Verifique o caminho e o arquivo.\n" + e.getMessage());
                e.printStackTrace();
                // Mostra um alerta para o usuário
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro de Carregamento da Interface");
                alert.setHeaderText("Falha ao carregar a aba de relatório");
                alert.setContentText("O arquivo RelatorioFinalTab.fxml não pôde ser encontrado ou carregado. Verifique o console para mais detalhes.");
                alert.showAndWait();
            } catch (Exception e_geral) {
                adicionarLog("ERRO GERAL ao carregar RelatorioFinalTab.fxml: " + e_geral.getMessage());
                e_geral.printStackTrace();
            }
        } else {
            adicionarLog("AVISO: A Tab com fx:id='tabRelatorioFinal' não foi encontrada no FXML principal. A aba de relatório gráfico não será carregada.");
        }

        // Configurar TableView de Métricas da Estação (apenas uma vez)
        // É importante que colEstacaoNome, etc., sejam injetados corretamente pelo FXML carregado
        if (colEstacaoNome != null) colEstacaoNome.setCellValueFactory(new PropertyValueFactory<>("nomeEstacao"));
        if (colCaminhoesAtendidos != null) colCaminhoesAtendidos.setCellValueFactory(new PropertyValueFactory<>("caminhoesAtendidos"));
        if (colTempoMedioEspera != null) {
            colTempoMedioEspera.setCellValueFactory(new PropertyValueFactory<>("tempoMedioEspera"));
            colTempoMedioEspera.setCellFactory(column -> new TableCell<MetricaEstacaoModel, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format(brLocale, "%.1f minutos", item));
                    }
                }
            });
        }
        if (colLixoTransferidoEstacao != null) {
            colLixoTransferidoEstacao.setCellValueFactory(new PropertyValueFactory<>("lixoTransferido"));
            colLixoTransferidoEstacao.setCellFactory(column -> new TableCell<MetricaEstacaoModel, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format(brLocale, "%,d kg", item));
                    }
                }
            });
        }

        // Timer para atualização da UI principal
        atualizacaoUITimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (simulador != null && !simulador.isPausado()) {
                atualizarInterfaceCompleta(); // Atualiza a aba principal
            }
        }));
        atualizacaoUITimer.setCycleCount(Timeline.INDEFINITE);
    }

    // ... (configurarSpinners, configurarCheckBoxes, inicializarInterface, handleIniciarSimulacao, configurarSimuladorComValoresPadraoExemplo, handleAplicarConfiguracao, confirmarSubstituicaoSimulacao, adicionarCaminhoesConfig, criarZonaConfig, handlePausarSimulacao, handleContinuarSimulacao, handleAdicionarCaminhaoGrande, handleGerarRelatorio - MANTENHA ESTES MÉTODOS COMO ESTAVAM ANTES)
    private void configurarSpinners() {
        if (spinnerCaminhoes2T != null) spinnerCaminhoes2T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 2));
        if (spinnerCaminhoes4T != null) spinnerCaminhoes4T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        if (spinnerCaminhoes8T != null) spinnerCaminhoes8T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        if (spinnerCaminhoes10T != null) spinnerCaminhoes10T.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        if (spinnerCaminhoesGrandes != null) spinnerCaminhoesGrandes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 1));
        if (spinnerZonaSulMin != null) spinnerZonaSulMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        if (spinnerZonaSulMax != null) spinnerZonaSulMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        if (spinnerZonaNorteMin != null) spinnerZonaNorteMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        if (spinnerZonaNorteMax != null) spinnerZonaNorteMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        if (spinnerZonaCentroMin != null) spinnerZonaCentroMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        if (spinnerZonaCentroMax != null) spinnerZonaCentroMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        if (spinnerZonaLesteMin != null) spinnerZonaLesteMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        if (spinnerZonaLesteMax != null) spinnerZonaLesteMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        if (spinnerZonaSudesteMin != null) spinnerZonaSudesteMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 100, 50));
        if (spinnerZonaSudesteMax != null) spinnerZonaSudesteMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 500, 50));
        if (spinnerEstacaoATempoEspera != null) spinnerEstacaoATempoEspera.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30, 5));
        if (spinnerEstacaoBTempoEspera != null) spinnerEstacaoBTempoEspera.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30, 5));
        if (spinnerToleranciaCG != null) spinnerToleranciaCG.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30, 5));
        if (spinnerLimiteViagensDiarias != null) spinnerLimiteViagensDiarias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10, 1));
        if (spinnerCaminhoesPorZona != null) spinnerCaminhoesPorZona.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1));
    }

    private void configurarCheckBoxes() {
        if (checkBoxUsarGaragem != null && checkBoxGarantirDistribuicao != null && spinnerCaminhoesPorZona != null) {
            checkBoxUsarGaragem.setSelected(true);
            checkBoxGarantirDistribuicao.setSelected(true);
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
                adicionarLog("Simulador (re)criado. Aplique configurações ou use os padrões.");
            } else if (simulador.getTempoSimulado() > 0 && !simulador.isPausado()){
                if (!confirmarSubstituicaoSimulacao()) return; // Usuário cancelou
                // Se confirmou, o simulador já foi resetado em confirmarSubstituicaoSimulacao
            }


            if (simulador.getListaZonas() == null || simulador.getListaZonas().estaVazia() ||
                    simulador.getListaEstacoes() == null || simulador.getListaEstacoes().estaVazia() ||
                    simulador.getTodosOsCaminhoesPequenos() == null || simulador.getTodosOsCaminhoesPequenos().estaVazia()) {
                configurarSimuladorComValoresPadraoExemplo();
                adicionarLog("Simulador configurado com valores padrão de exemplo.");
            } else {
                int totalCaminhoesPequenos = simulador.getTodosOsCaminhoesPequenos().tamanho();
                if (totalCaminhoesPequenos < 5) {
                    Lista<CaminhaoPequeno> listaCPs = simulador.getTodosOsCaminhoesPequenos();
                    int caminhoesFaltantes = 5 - totalCaminhoesPequenos;
                    for (int i = 0; i < caminhoesFaltantes; i++) {
                        int capacidade = (i % 4 == 0) ? 2000 : (i % 4 == 1) ? 4000 : (i % 4 == 2) ? 8000 : 10000;
                        listaCPs.adicionar(new CaminhaoPequenoPadrao(capacidade));
                    }
                    simulador.setListaCaminhoesPequenos(listaCPs);
                    adicionarLog("Adicionados " + caminhoesFaltantes + " caminhões pequenos para atingir o mínimo de 5.");
                }
            }

            simulador.iniciar();
            atualizacaoUITimer.play();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(false);
            adicionarLog("Simulação iniciada com " + simulador.getTodosOsCaminhoesPequenos().tamanho() + " caminhões pequenos.");

            if (tabPanePrincipal != null) {
                tabPanePrincipal.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            mostrarAlertaErro("Erro ao iniciar a simulação", "Erro de Configuração", e.getMessage());
            adicionarLog("Falha ao iniciar: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void configurarSimuladorComValoresPadraoExemplo() {
        // Este método já cria uma nova instância do simulador se necessário
        // ou usa a existente.
        if (simulador == null) simulador = new Simulador();

        Lista<CaminhaoPequeno> listaCP = new Lista<>();
        listaCP.adicionar(new CaminhaoPequenoPadrao(2000));
        listaCP.adicionar(new CaminhaoPequenoPadrao(2000));
        listaCP.adicionar(new CaminhaoPequenoPadrao(4000));
        listaCP.adicionar(new CaminhaoPequenoPadrao(8000));
        listaCP.adicionar(new CaminhaoPequenoPadrao(10000));
        simulador.setListaCaminhoesPequenos(listaCP);

        Lista<CaminhaoGrande> listaCG = new Lista<>();
        listaCG.adicionar(new CaminhaoGrandePadrao(30));
        simulador.setListaCaminhoesGrandes(listaCG);

        Lista<ZonaUrbana> listaZ = new Lista<>();
        String[] nomesZonas = {"Sul", "Norte", "Centro", "Leste", "Sudeste"};
        for (String nome : nomesZonas) {
            ZonaUrbana z = new ZonaUrbana(nome);
            z.setIntervaloGeracao(100, 500);
            listaZ.adicionar(z);
        }
        simulador.setListaZonas(listaZ);

        Lista<EstacaoTransferencia> listaE = new Lista<>();
        listaE.adicionar(new EstacaoPadrao("Estação A", 30));
        listaE.adicionar(new EstacaoPadrao("Estação B", 30));
        simulador.setListaEstacoes(listaE);

        simulador.setLimiteViagensDiarias(10);
        simulador.setUsarGaragemCentral(true);
        simulador.setGarantirDistribuicaoMinima(true);
        simulador.setCaminhoesPorZonaMinimo(1);
        adicionarLog("Simulador configurado com: 5 CPs, 1 CG, 5 Zonas, 2 Estações (padrão).");
    }

    @FXML
    private void handleAplicarConfiguracao(ActionEvent event) {
        try {
            if (simulador == null || (simulador.getTempoSimulado() > 0 && !confirmarSubstituicaoSimulacao())) {
                if (simulador != null && simulador.getTempoSimulado() > 0 && simulador.isPausado()) {
                    // Se estava pausado e cancelou a substituição, não faz nada.
                } else if (simulador != null && simulador.getTempoSimulado() > 0) {
                    return; // Usuário cancelou a substituição de uma simulação em andamento/concluída
                }
                // Se chegou aqui, ou não havia simulação, ou era uma simulação já encerrada,
                // ou o usuário confirmou a substituição.
                // A lógica de `confirmarSubstituicaoSimulacao` já trata de recriar o simulador se necessário.
            }
            if (simulador == null) { // Garante que simulador exista se foi cancelada a substituição de um não existente
                simulador = new Simulador();
            }


            Lista<CaminhaoPequeno> listaCP = new Lista<>();
            adicionarCaminhoesConfig(listaCP, spinnerCaminhoes2T.getValue(), 2000);
            adicionarCaminhoesConfig(listaCP, spinnerCaminhoes4T.getValue(), 4000);
            adicionarCaminhoesConfig(listaCP, spinnerCaminhoes8T.getValue(), 8000);
            adicionarCaminhoesConfig(listaCP, spinnerCaminhoes10T.getValue(), 10000);

            int totalCP = listaCP.tamanho();
            if (totalCP < 5) {
                mostrarAlertaInfo("Ajuste Automático", "Número mínimo de caminhões não atingido",
                        "A simulação requer pelo menos 5 caminhões pequenos. " +
                                "Serão adicionados " + (5 - totalCP) + " caminhões de 4 toneladas.");
                for (int i = 0; i < 5 - totalCP; i++) listaCP.adicionar(new CaminhaoPequenoPadrao(4000));
            }
            simulador.setListaCaminhoesPequenos(listaCP);

            Lista<CaminhaoGrande> listaCG = new Lista<>();
            int toleranciaCG = spinnerToleranciaCG.getValue();
            for (int i = 0; i < spinnerCaminhoesGrandes.getValue(); i++) listaCG.adicionar(new CaminhaoGrandePadrao(toleranciaCG));
            simulador.setListaCaminhoesGrandes(listaCG);
            simulador.setToleranciaCaminhoesGrandes(toleranciaCG);

            Lista<ZonaUrbana> listaZ = new Lista<>();
            listaZ.adicionar(criarZonaConfig("Sul", spinnerZonaSulMin.getValue(), spinnerZonaSulMax.getValue()));
            listaZ.adicionar(criarZonaConfig("Norte", spinnerZonaNorteMin.getValue(), spinnerZonaNorteMax.getValue()));
            listaZ.adicionar(criarZonaConfig("Centro", spinnerZonaCentroMin.getValue(), spinnerZonaCentroMax.getValue()));
            listaZ.adicionar(criarZonaConfig("Leste", spinnerZonaLesteMin.getValue(), spinnerZonaLesteMax.getValue()));
            listaZ.adicionar(criarZonaConfig("Sudeste", spinnerZonaSudesteMin.getValue(), spinnerZonaSudesteMax.getValue()));
            simulador.setListaZonas(listaZ);

            Lista<EstacaoTransferencia> listaE = new Lista<>();
            listaE.adicionar(new EstacaoPadrao("Estação A", spinnerEstacaoATempoEspera.getValue()));
            listaE.adicionar(new EstacaoPadrao("Estação B", spinnerEstacaoBTempoEspera.getValue()));
            simulador.setListaEstacoes(listaE);

            simulador.setLimiteViagensDiarias(spinnerLimiteViagensDiarias.getValue());
            simulador.setUsarGaragemCentral(checkBoxUsarGaragem.isSelected());
            simulador.setGarantirDistribuicaoMinima(checkBoxGarantirDistribuicao.isSelected());
            simulador.setCaminhoesPorZonaMinimo(spinnerCaminhoesPorZona.getValue());

            simulador.iniciar(); // Inicia a simulação com as novas configurações
            atualizacaoUITimer.play();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(false);

            if (tabPanePrincipal != null) tabPanePrincipal.getSelectionModel().select(0); // Vai para a aba de simulação principal
            adicionarLog("Simulação iniciada com configurações personalizadas: " +
                    listaCP.tamanho() + " CPs, " + listaCG.tamanho() + " CGs.");

        } catch (Exception e) {
            mostrarAlertaErro("Erro de Configuração", "Falha ao aplicar configurações", "Erro: " + e.getMessage());
            adicionarLog("Erro ao aplicar configurações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean confirmarSubstituicaoSimulacao() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Substituir Simulação");
        alert.setHeaderText("Uma simulação já está em progresso ou foi concluída.");
        alert.setContentText("Deseja encerrar a simulação atual e iniciar uma nova com estas configurações?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (simulador != null) { // Se existe uma simulação anterior
                if (simulador.getTempoSimulado() > 0 && !simulador.isPausado()) { // Se estava rodando
                    simulador.encerrar(); // Encerra formalmente
                }
                if (atualizacaoUITimer != null) {
                    atualizacaoUITimer.stop(); // Para o timer da simulação anterior
                }
            }
            simulador = new Simulador(); // Cria uma nova instância para a nova simulação
            adicionarLog("Simulação anterior encerrada. Preparando para nova configuração.");
            return true;
        }
        adicionarLog("Aplicação de novas configurações cancelada pelo usuário.");
        return false; // Usuário cancelou
    }


    private void adicionarCaminhoesConfig(Lista<CaminhaoPequeno> lista, int qtd, int capacidade) {
        for (int i = 0; i < qtd; i++) lista.adicionar(new CaminhaoPequenoPadrao(capacidade));
    }

    private ZonaUrbana criarZonaConfig(String nome, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Para a zona " + nome + ", a geração mínima (" + min + ") não pode ser maior que a máxima (" + max +").");
        }
        ZonaUrbana z = new ZonaUrbana(nome);
        z.setIntervaloGeracao(min, max);
        return z;
    }


    @FXML
    private void handlePausarSimulacao(ActionEvent event) {
        if (simulador != null) {
            simulador.pausar();
            if (atualizacaoUITimer != null) atualizacaoUITimer.pause();
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
            if (atualizacaoUITimer != null) atualizacaoUITimer.play();
            btnIniciar.setDisable(true);
            btnPausar.setDisable(false);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(false);
            adicionarLog("Simulação continuada.");
        }
    }

    @FXML
    private void handleEncerrarSimulacao(ActionEvent event) { // AGORA ATUALIZA A ABA GRÁFICA
        if (simulador != null) {
            simulador.encerrar(); // O método encerrar do simulador já gera o relatório textual interno e salva
            if (atualizacaoUITimer != null) atualizacaoUITimer.stop();

            atualizarInterfaceCompleta(); // Atualiza a aba principal uma última vez
            exibirRelatorioFinalGrafico();  // Popula a aba de relatório gráfico

            btnIniciar.setDisable(false);
            btnPausar.setDisable(true);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(true); // Desabilita após encerrar
            adicionarLog("Simulação encerrada. Relatório final disponível na aba 'Relatório Final'.");
        }
    }


    @FXML
    private void handleAdicionarCaminhaoGrande(ActionEvent event) {
        if (simulador != null && simulador.getTempoSimulado() > 0 && !simulador.isPausado()) {
            simulador.adicionarCaminhaoGrande();
            adicionarLog("Novo caminhão grande adicionado à frota.");
            atualizarLabelsMetricasGerais(); // Atualiza a contagem na UI principal
        } else {
            adicionarLog("Simulador não iniciado ou está pausado/encerrado. Não é possível adicionar caminhão grande.");
            mostrarAlertaInfo("Ação Inválida", "Não é possível adicionar caminhão", "A simulação precisa estar em andamento (não pausada ou encerrada) para adicionar caminhões grandes.");
        }
    }

    @FXML
    private void handleGerarRelatorio(ActionEvent event) { // Este botão agora salva o relatório TEXTUAL
        if (simulador != null && simulador.getEstatisticas() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Relatório Textual da Simulação");
            fileChooser.setInitialFileName("relatorio_textual_simulacao_" + System.currentTimeMillis() + ".txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto (*.txt)", "*.txt"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    simulador.getEstatisticas().salvarRelatorio(file.getAbsolutePath()); // Salva o relatório textual
                    adicionarLog("Relatório textual salvo em: " + file.getAbsolutePath());
                    mostrarAlertaInfo("Relatório Salvo", "Sucesso", "Relatório textual salvo com sucesso!");
                } catch (IOException e) {
                    adicionarLog("Erro ao salvar relatório textual: " + e.getMessage());
                    mostrarAlertaErro("Erro", "Erro ao salvar relatório textual", e.getMessage());
                }
            }
        } else {
            adicionarLog("Nenhuma simulação ativa ou estatísticas disponíveis para gerar relatório textual.");
            mostrarAlertaInfo("Atenção", "Sem Dados", "Nenhuma simulação ativa ou estatísticas disponíveis.");
        }
    }

    // Métodos de atualização da UI principal (MANTENHA COMO ESTAVAM)
    private void atualizarInterfaceCompleta() {
        if (simulador == null) return;
        Platform.runLater(() -> {
            if (lblTempoSimulado != null) lblTempoSimulado.setText(formatarTempo(simulador.getTempoSimulado()) + " (Simulado)");

            Lista<ZonaUrbana> zonas = simulador.getListaZonas();
            if (zonas != null && lblZonaSulLixoGerado != null) { // Checa se os labels existem
                ZonaUrbana[] arrZonas = new ZonaUrbana[5];
                for(int i=0; i < zonas.tamanho() && i < 5; i++) arrZonas[i] = zonas.obter(i);
                atualizarLabelsZonas(arrZonas[0], arrZonas[1], arrZonas[2], arrZonas[3], arrZonas[4]);
            } else if (lblZonaSulLixoGerado != null) { // Labels existem, mas não há zonas
                atualizarLabelsZonas(null, null, null, null, null);
            }

            atualizarLabelsMetricasGerais();

            Lista<EstacaoTransferencia> estacoes = simulador.getListaEstacoes();
            if (estacoes != null && lblEstacaoAStatus != null) { // Checa se os labels existem
                EstacaoPadrao estA = null, estB = null;
                if (estacoes.tamanho() > 0 && estacoes.obter(0) instanceof EstacaoPadrao) estA = (EstacaoPadrao) estacoes.obter(0);
                if (estacoes.tamanho() > 1 && estacoes.obter(1) instanceof EstacaoPadrao) estB = (EstacaoPadrao) estacoes.obter(1);
                atualizarLabelsEstacoes(estA, estB);
            } else if (lblEstacaoAStatus != null) { // Labels existem, mas não há estações
                atualizarLabelsEstacoes(null, null);
            }

            if (vboxCaminhoesEmAtividade != null) atualizarListaCaminhoesAtividade();
        });
    }

    private void atualizarLabelsZonas(ZonaUrbana zonaSul, ZonaUrbana zonaNorte, ZonaUrbana zonaCentro, ZonaUrbana zonaLeste, ZonaUrbana zonaSudeste) {
        Estatisticas stats = simulador != null ? simulador.getEstatisticas() : null;
        TriConsumerCustom<Label, Label, ZonaUrbana> atualizarZonaUI = (lblGerado, lblColetado, zona) -> {
            if (lblGerado == null || lblColetado == null) return;
            if (zona != null && stats != null) {
                int gerado = stats.getLixoGeradoPorZona(zona.getNome());
                int coletado = stats.getLixoColetadoPorZona(zona.getNome());
                lblGerado.setText("Gerado: " + String.format(brLocale, "%,d", gerado) + " kg");
                lblColetado.setText("Coletado: " + String.format(brLocale, "%,d", coletado) + " kg");
            } else if (zona != null) { // Sem stats, mas com zona (simulação pode não ter rodado ainda)
                lblGerado.setText("Gerado: " + String.format(brLocale, "%,d", zona.getLixoAcumulado()) + " kg (atual)");
                lblColetado.setText("Coletado: -- kg");
            } else { // Sem zona e sem stats
                lblGerado.setText("Gerado: -- kg");
                lblColetado.setText("Coletado: -- kg");
            }
        };
        atualizarZonaUI.accept(lblZonaSulLixoGerado, lblZonaSulLixoColetado, zonaSul);
        atualizarZonaUI.accept(lblZonaNorteLixoGerado, lblZonaNorteLixoColetado, zonaNorte);
        atualizarZonaUI.accept(lblZonaCentroLixoGerado, lblZonaCentroLixoColetado, zonaCentro);
        atualizarZonaUI.accept(lblZonaLesteLixoGerado, lblZonaLesteLixoColetado, zonaLeste);
        atualizarZonaUI.accept(lblZonaSudesteLixoGerado, lblZonaSudesteLixoColetado, zonaSudeste);
    }

    private void atualizarLabelsMetricasGerais() {
        Estatisticas stats = simulador != null ? simulador.getEstatisticas() : null;
        String defaultText = "0";
        if (stats != null) {
            if (lblMetricaLixoTotal != null) lblMetricaLixoTotal.setText(String.format(brLocale, "%,d", stats.getLixoTotalGerado()));
            if (lblMetricaLixoColetado != null) lblMetricaLixoColetado.setText(String.format(brLocale, "%,d", stats.getLixoTotalColetado()));
            if (lblMetricaLixoTransportado != null) lblMetricaLixoTransportado.setText(String.format(brLocale, "%,d", stats.getLixoTotalTransportado()));

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
            if (lblMetricaPequenosTotal != null) lblMetricaPequenosTotal.setText(String.valueOf(pequenosTotal));
            if (lblMetricaPequenosEmAtividade != null) lblMetricaPequenosEmAtividade.setText(String.valueOf(pequenosAtivos));
            if (lblMetricaGrandesTotal != null) lblMetricaGrandesTotal.setText(String.valueOf(simulador.getTotalCaminhoesGrandes()));
            if (lblMetricaGrandesEmUso != null) lblMetricaGrandesEmUso.setText(String.valueOf(simulador.getCaminhoesGrandesEmUso()));
        } else {
            // Código para quando stats é nulo (interface antes da primeira simulação)
            if (lblMetricaLixoTotal != null) lblMetricaLixoTotal.setText(defaultText);
            // ... etc para todos os labels de métricas
        }
    }

    private void atualizarLabelsEstacoes(EstacaoPadrao estacaoA, EstacaoPadrao estacaoB) {
        Estatisticas stats = simulador != null ? simulador.getEstatisticas() : null;

        // Atualizar Estação A
        if (lblEstacaoAStatus != null) { // Verifica se o label existe
            if (estacaoA != null) {
                lblEstacaoAStatus.setText("Operacional");
                lblEstacaoACaminhoesFila.setText(String.valueOf(estacaoA.getCaminhoesNaFila()));
                lblEstacaoATempoEsperaMedio.setText(String.format(brLocale, "%.1f min", stats != null ? stats.getTempoMedioEsperaPorEstacao(estacaoA.getNome()) : 0));
                lblEstacaoALixoTransferido.setText(String.format(brLocale, "%,d kg", stats != null ? stats.getLixoTransferidoPorEstacao(estacaoA.getNome()) : 0));
                if (lblEstacaoACaminhaoGrandeInfo != null) {
                    if (estacaoA.temCaminhaoGrande()) {
                        CaminhaoGrande cg = estacaoA.getCaminhaoGrandeAtual();
                        lblEstacaoACaminhaoGrandeInfo.setText(String.format(brLocale, "CG (%,d/%,d kg)", cg.getCargaAtual(), cg.getCapacidadeMaxima()));
                    } else {
                        lblEstacaoACaminhaoGrandeInfo.setText("Sem CG");
                    }
                }
                if (listViewEstacaoAFilaCP != null) {
                    ObservableList<String> filaAItems = FXCollections.observableArrayList();
                    Lista<CaminhaoPequeno> snapshotFilaA = estacaoA.getFilaCaminhoesPequenosSnapshot();
                    for (int i = 0; i < snapshotFilaA.tamanho(); i++) filaAItems.add(snapshotFilaA.obter(i).getPlaca());
                    listViewEstacaoAFilaCP.setItems(filaAItems);
                }
            } else {
                lblEstacaoAStatus.setText("--");
                lblEstacaoACaminhoesFila.setText("0");
                lblEstacaoATempoEsperaMedio.setText("0 min");
                lblEstacaoALixoTransferido.setText("0 kg");
                if (lblEstacaoACaminhaoGrandeInfo != null) lblEstacaoACaminhaoGrandeInfo.setText("Sem CG");
                if (listViewEstacaoAFilaCP != null) listViewEstacaoAFilaCP.getItems().clear();
            }
        }

        // Atualizar Estação B
        if (lblEstacaoBStatus != null) { // Verifica se o label existe
            if (estacaoB != null) {
                lblEstacaoBStatus.setText("Operacional");
                lblEstacaoBCaminhoesFila.setText(String.valueOf(estacaoB.getCaminhoesNaFila()));
                lblEstacaoBTempoEsperaMedio.setText(String.format(brLocale, "%.1f min", stats != null ? stats.getTempoMedioEsperaPorEstacao(estacaoB.getNome()) : 0));
                lblEstacaoBLixoTransferido.setText(String.format(brLocale, "%,d kg", stats != null ? stats.getLixoTransferidoPorEstacao(estacaoB.getNome()) : 0));
                if (lblEstacaoBCaminhaoGrandeInfo != null) {
                    if (estacaoB.temCaminhaoGrande()) {
                        CaminhaoGrande cg = estacaoB.getCaminhaoGrandeAtual();
                        lblEstacaoBCaminhaoGrandeInfo.setText(String.format(brLocale, "CG (%,d/%,d kg)", cg.getCargaAtual(), cg.getCapacidadeMaxima()));
                    } else {
                        lblEstacaoBCaminhaoGrandeInfo.setText("Sem CG");
                    }
                }
                if (listViewEstacaoBFilaCP != null) {
                    ObservableList<String> filaBItems = FXCollections.observableArrayList();
                    Lista<CaminhaoPequeno> snapshotFilaB = estacaoB.getFilaCaminhoesPequenosSnapshot();
                    for (int i = 0; i < snapshotFilaB.tamanho(); i++) filaBItems.add(snapshotFilaB.obter(i).getPlaca());
                    listViewEstacaoBFilaCP.setItems(filaBItems);
                }
            } else {
                lblEstacaoBStatus.setText("--");
                lblEstacaoBCaminhoesFila.setText("0");
                lblEstacaoBTempoEsperaMedio.setText("0 min");
                lblEstacaoBLixoTransferido.setText("0 kg");
                if (lblEstacaoBCaminhaoGrandeInfo != null) lblEstacaoBCaminhaoGrandeInfo.setText("Sem CG");
                if (listViewEstacaoBFilaCP != null) listViewEstacaoBFilaCP.getItems().clear();
            }
        }
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
        HBox hbox = new HBox(10); // Espaçamento entre elementos
        hbox.getStyleClass().add("caminhao-item"); // Para CSS
        hbox.setAlignment(Pos.CENTER_LEFT); // Alinhamento dos itens

        // Ícone de status (círculo colorido)
        javafx.scene.shape.Circle statusIcon = new javafx.scene.shape.Circle(8); // Raio do círculo
        statusIcon.getStyleClass().add("status-icon"); // Para CSS
        switch (caminhao.getStatus()) {
            case COLETANDO: statusIcon.setStyle("-fx-fill: #4CAF50;"); break; // Verde
            case VIAJANDO_ESTACAO: case RETORNANDO_ZONA: statusIcon.setStyle("-fx-fill: #FFC107;"); break; // Ambar
            case NA_FILA: statusIcon.setStyle("-fx-fill: #03A9F4;"); break; // Azul claro
            case DESCARREGANDO: statusIcon.setStyle("-fx-fill: #9C27B0;"); break; // Roxo
            default: statusIcon.setStyle("-fx-fill: #9E9E9E;"); // Cinza para outros
        }

        // Informações do caminhão (Placa, Tipo, Local, Status)
        Label lblPlacaTipo = new Label(String.format(brLocale, "%s (%,d Ton)", caminhao.getPlaca(), caminhao.getCapacidade() / 1000));
        lblPlacaTipo.getStyleClass().add("caminhao-placa");

        String localStatus = "Local desconhecido";
        if (caminhao.getZonaAtual() != null) {
            localStatus = "Zona: " + caminhao.getZonaAtual().getNome();
        } else if (caminhao.getEstacaoDestino() != null &&
                (caminhao.getStatus() == StatusCaminhao.VIAJANDO_ESTACAO ||
                        caminhao.getStatus() == StatusCaminhao.NA_FILA ||
                        caminhao.getStatus() == StatusCaminhao.DESCARREGANDO)) {
            localStatus = "Estação: " + caminhao.getEstacaoDestino().getNome();
        }
        Label lblLocal = new Label(localStatus);
        lblLocal.getStyleClass().add("caminhao-local");

        Label lblStatusCaminhao = new Label("Status: " + caminhao.getStatus().toString().replace("_", " "));
        lblStatusCaminhao.getStyleClass().add("caminhao-status-texto");

        VBox infoLabels = new VBox(2, lblPlacaTipo, new HBox(5, lblLocal, lblStatusCaminhao)); // HBox para Local e Status na mesma linha

        // Barra de Progresso da Carga
        ProgressBar progressBarCarga = new ProgressBar((double) caminhao.getCargaAtual() / caminhao.getCapacidade());
        progressBarCarga.setPrefWidth(100); // Largura da barra
        progressBarCarga.setMinWidth(80);

        Label lblCarga = new Label(String.format(brLocale, "%,d/%,d kg", caminhao.getCargaAtual(), caminhao.getCapacidade()));
        lblCarga.getStyleClass().add("caminhao-carga-texto");
        lblCarga.setMinWidth(Label.USE_PREF_SIZE); // Para não cortar o texto

        HBox cargaBox = new HBox(5, progressBarCarga, lblCarga);
        cargaBox.setAlignment(Pos.CENTER_LEFT); // Alinha barra e texto

        // Adiciona tudo ao HBox principal
        hbox.getChildren().addAll(statusIcon, infoLabels, cargaBox);
        // Fazer infoLabels e cargaBox dividirem o espaço restante
        HBox.setHgrow(infoLabels, javafx.scene.layout.Priority.SOMETIMES);
        HBox.setHgrow(cargaBox, javafx.scene.layout.Priority.SOMETIMES);


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
            System.out.println(mensagem); // Fallback se areaLog não estiver disponível
        }
    }

    public void encerrarTimerAtualizacao() {
        if (atualizacaoUITimer != null) {
            atualizacaoUITimer.stop();
        }
    }

    // Novo método para popular a aba de relatório gráfico
    private void exibirRelatorioFinalGrafico() {
        if (simulador == null || simulador.getEstatisticas() == null) {
            adicionarLog("Simulador ou estatísticas não disponíveis para gerar relatório gráfico.");
            // Limpar campos do relatório se estiverem preenchidos de simulação anterior
            if (barChartLixoPorZona != null) barChartLixoPorZona.getData().clear();
            if (tableViewMetricasEstacao != null) tableViewMetricasEstacao.getItems().clear();
            if (lblEficienciaColeta != null) lblEficienciaColeta.setText("Eficiência de Coleta: N/D");
            // ... limpar outros labels
            return;
        }

        Estatisticas stats = simulador.getEstatisticas();
        Platform.runLater(() -> {
            // 1. Popular BarChart Lixo Gerado por Zona
            if (barChartLixoPorZona != null) {
                barChartLixoPorZona.getData().clear();
                XYChart.Series<String, Number> seriesLixoGerado = new XYChart.Series<>();
                // seriesLixoGerado.setName("Lixo Gerado"); // Legenda não é visível no FXML

                Lista<Estatisticas.EntradaZona> zonasStats = stats.getEstatisticasZonas();
                String[] nomesZonasOrdenadas = {"Sul", "Norte", "Centro", "Leste", "Sudeste"}; // Ordem desejada

                for (String nomeZona : nomesZonasOrdenadas) {
                    boolean encontrada = false;
                    for (int i = 0; i < zonasStats.tamanho(); i++) {
                        Estatisticas.EntradaZona zonaStat = zonasStats.obter(i);
                        if (zonaStat.nomeZona.equals(nomeZona)) {
                            seriesLixoGerado.getData().add(new XYChart.Data<>(zonaStat.nomeZona, zonaStat.lixoGerado));
                            encontrada = true;
                            break;
                        }
                    }
                    if (!encontrada) { // Adiciona com valor 0 se não houver dados, para manter a ordem no gráfico
                        seriesLixoGerado.getData().add(new XYChart.Data<>(nomeZona, 0));
                    }
                }
                barChartLixoPorZona.getData().add(seriesLixoGerado);
                // Aplicar estilo às barras
                for(Node n:barChartLixoPorZona.lookupAll(".chart-bar")) {
                    n.setStyle("-fx-bar-fill: #2980b9;"); // Cor azul das imagens
                }
            }

            // 2. Popular TableView Métricas por Estação
            if (tableViewMetricasEstacao != null) {
                ObservableList<MetricaEstacaoModel> metricasEstacaoList = FXCollections.observableArrayList();
                Lista<Estatisticas.EntradaEstacao> estacoesStats = stats.getEstatisticasEstacoes();
                for (int i = 0; i < estacoesStats.tamanho(); i++) {
                    Estatisticas.EntradaEstacao estStat = estacoesStats.obter(i);
                    metricasEstacaoList.add(new MetricaEstacaoModel(
                            estStat.nomeEstacao,
                            estStat.caminhoesPequenosDescarregados,
                            stats.getTempoMedioEsperaPorEstacao(estStat.nomeEstacao),
                            estStat.getLixoTransferidoCG()
                    ));
                }
                tableViewMetricasEstacao.setItems(metricasEstacaoList);
            }

            // 3. Popular Principais Descobertas
            double eficienciaColeta = stats.calcularPercentualLixoColetado();
            String eficienciaTexto = String.format(brLocale, "O sistema conseguiu coletar %.2f%% do lixo gerado ", eficienciaColeta);
            if (stats.getLixoTotalGerado() == 0 && stats.getLixoTotalColetado() == 0) { // Caso a simulação não tenha rodado ou gerado lixo
                eficienciaTexto += "(simulação não gerou/coletou lixo significativamente).";
            } else if (eficienciaColeta == 100.0) {
                eficienciaTexto += "em todas as cinco zonas, demonstrando excelente capacidade de coleta.";
            } else if (eficienciaColeta >= 80) {
                eficienciaTexto += ", indicando uma boa capacidade de coleta.";
            } else {
                eficienciaTexto += ", o que pode indicar necessidade de otimização na frota de caminhões pequenos ou na distribuição.";
            }
            if (lblEficienciaColeta != null) lblEficienciaColeta.setText("Eficiência de Coleta: " + eficienciaTexto);


            Estatisticas.EntradaZona zonaMaiorGeracao = null;
            Estatisticas.EntradaZona zonaMenorGeracao = null;
            Lista<Estatisticas.EntradaZona> zonasStatsList = stats.getEstatisticasZonas();

            if (zonasStatsList != null && !zonasStatsList.estaVazia()) {
                // Inicializa com a primeira zona, se existir
                zonaMaiorGeracao = zonasStatsList.obter(0);
                zonaMenorGeracao = zonasStatsList.obter(0);
                for (int i = 1; i < zonasStatsList.tamanho(); i++) {
                    Estatisticas.EntradaZona atual = zonasStatsList.obter(i);
                    if (atual.lixoGerado > zonaMaiorGeracao.lixoGerado) zonaMaiorGeracao = atual;
                    if (atual.lixoGerado < zonaMenorGeracao.lixoGerado) zonaMenorGeracao = atual;
                }
            }

            if (zonaMaiorGeracao != null && zonaMenorGeracao != null && stats.getLixoTotalGerado() > 0) {
                double percMaior = ((double) zonaMaiorGeracao.lixoGerado / stats.getLixoTotalGerado()) * 100.0;
                double percMenor = ((double) zonaMenorGeracao.lixoGerado / stats.getLixoTotalGerado()) * 100.0;
                if (lblDistribuicaoLixo != null) lblDistribuicaoLixo.setText(String.format(brLocale, "Distribuição de Lixo: A zona %s gerou a maior quantidade de lixo (%,d kg, %.1f%% do total), enquanto a zona %s produziu a menor quantidade (%,d kg, %.1f%% do total).",
                        zonaMaiorGeracao.nomeZona, zonaMaiorGeracao.lixoGerado, percMaior,
                        zonaMenorGeracao.nomeZona, zonaMenorGeracao.lixoGerado, percMenor));
            } else {
                if (lblDistribuicaoLixo != null) lblDistribuicaoLixo.setText("Distribuição de Lixo: Dados insuficientes ou lixo não gerado para análise detalhada.");
            }

            double tempoMedioGlobalEsperaEstacoes = stats.calcularTempoMedioEsperaFilaPequenos();
            if (lblGargalosSistema != null) lblGargalosSistema.setText(String.format(brLocale, "Gargalos no Sistema: O tempo médio de espera nas estações (global: %.1f minutos) pode indicar o nível de congestionamento nas estações de transferência.", tempoMedioGlobalEsperaEstacoes));


            if (stats.getCaminhoesGrandesAdicionados() > 0) {
                if (lblOtimizacaoFrota != null) lblOtimizacaoFrota.setText(String.format(brLocale, "Otimização da Frota: O sistema precisou adicionar %d caminhões grandes além da frota inicial para atender à demanda.", stats.getCaminhoesGrandesAdicionados()));
            } else {
                if (lblOtimizacaoFrota != null) lblOtimizacaoFrota.setText("Otimização da Frota: A frota inicial de caminhões grandes foi suficiente para a demanda observada, não havendo adições durante a simulação.");
            }

            // 4. Popular Conclusão
            int numCGNecessarios = stats.calcularEstimativaCaminhoesGrandesNecessarios();
            String conclusaoTexto1 = String.format(brLocale,
                    "A simulação demonstra que o sistema de coleta de lixo de Teresina, com a configuração testada, apresentou uma eficiência de %.2f%% na coleta. ", eficienciaColeta);
            conclusaoTexto1 += "O principal fator limitante para o transporte final ao aterro sanitário frequentemente reside no número e na gestão dos caminhões grandes disponíveis. ";
            if (stats.getCaminhoesGrandesAdicionados() > 0) {
                conclusaoTexto1 += String.format(brLocale, "Na simulação atual, %d caminhões grandes foram adicionados dinamicamente, indicando uma demanda que excedeu a capacidade inicial da frota de transporte.", stats.getCaminhoesGrandesAdicionados());
            } else if (stats.getLixoTotalGerado() == 0 && stats.getLixoTotalColetado() == 0 && stats.getLixoTotalTransportado() == 0) { // Se nada aconteceu
                conclusaoTexto1 += "A simulação não apresentou dados de coleta para avaliar a frota de caminhões grandes.";
            }
            else {
                conclusaoTexto1 += "A frota inicial de caminhões grandes mostrou-se adequada durante esta simulação.";
            }
            if (lblConclusaoTexto1 != null) lblConclusaoTexto1.setText(conclusaoTexto1);

            if (lblConclusaoCaminhoesNecessarios != null) lblConclusaoCaminhoesNecessarios.setText(String.format(brLocale, "%d Caminhões de %d Toneladas", numCGNecessarios, CaminhaoGrande.CAPACIDADE_MAXIMA_KG / 1000));

            if (lblConclusaoTexto2 != null) lblConclusaoTexto2.setText(String.format(brLocale, "Esta conclusão considera a quantidade total de lixo coletado diariamente nas cinco zonas e a capacidade de transporte dos caminhões grandes. Com uma frota mínima de %d caminhões grandes, o sistema pode operar com eficiência, minimizando tempos de espera nas estações de transferência e garantindo o transporte adequado do lixo coletado.", numCGNecessarios));

            // Mudar para a aba de relatório
            if (tabPanePrincipal != null && tabRelatorioFinal != null) {
                tabPanePrincipal.getSelectionModel().select(tabRelatorioFinal);
            }
        });
    }

    private void mostrarAlertaErro(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    private void mostrarAlertaInfo(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }
}