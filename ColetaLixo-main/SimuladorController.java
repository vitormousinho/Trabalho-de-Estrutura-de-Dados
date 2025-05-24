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
import javafx.scene.Node;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import javafx.geometry.Insets;

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
import Estruturas.Lista;
import caminhoes.StatusCaminhao;

public class SimuladorController {

    private Simulador simulador;
    private Stage primaryStage;
    private Timeline atualizacaoUITimer;
    private static final int MINUTOS_EM_UM_DIA = 24 * 60;
    private Locale brLocale = new Locale("pt", "BR"); // Para formatação de números

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
                // Abordagem de último recurso com caminho totalmente explícito
                String diretorioAtual = System.getProperty("user.dir");
                System.out.println("Diretório de trabalho atual: " + diretorioAtual);

                // Tenta caminhos em ordem de probabilidade
                String[] tentativasCaminhos = {
                        diretorioAtual + "/ColetaLixo-main/resources/RelatorioFinalTab.fxml",
                        diretorioAtual + "/resources/RelatorioFinalTab.fxml",
                        diretorioAtual + "/RelatorioFinalTab.fxml",
                        diretorioAtual + "/ColetaLixo-main/RelatorioFinalTab.fxml",
                        "./ColetaLixo-main/resources/RelatorioFinalTab.fxml",
                        "./resources/RelatorioFinalTab.fxml",
                        "./RelatorioFinalTab.fxml",
                        "out/production/Trabalho-de-Estrutura-de-Dados/RelatorioFinalTab.fxml",
                        "out/production/Trabalho-de-Estrutura-de-Dados/resources/RelatorioFinalTab.fxml"
                };

                boolean carregado = false;
                for (String caminho : tentativasCaminhos) {
                    File arquivoFXML = new File(caminho);
                    System.out.println("Tentando carregar de: " + caminho + " (existe: " + arquivoFXML.exists() + ")");

                    if (arquivoFXML.exists()) {
                        FXMLLoader loader = new FXMLLoader(arquivoFXML.toURI().toURL());
                        loader.setController(this);
                        Node relatorioNode = loader.load();
                        tabRelatorioFinal.setContent(relatorioNode);
                        adicionarLog("RelatorioFinalTab.fxml carregado com sucesso de: " + caminho);
                        carregado = true;
                        break;
                    }
                }

                if (!carregado) {
                    // Se tudo falhar, crie uma interface simples programaticamente
                    VBox conteudoFallback = new VBox(10);
                    conteudoFallback.setPadding(new Insets(20));
                    conteudoFallback.getChildren().add(new Label("Não foi possível carregar o FXML. Usando interface alternativa."));

                    // Adicione componentes básicos programaticamente
                    tabRelatorioFinal.setContent(conteudoFallback);
                    adicionarLog("ERRO: Nenhum arquivo FXML encontrado. Usando interface alternativa.");

                    // Mostra um alerta para o usuário
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Interface Alternativa");
                    alert.setHeaderText("Usando interface simplificada");
                    alert.setContentText("O arquivo RelatorioFinalTab.fxml não pôde ser encontrado. Uma interface simplificada será usada em seu lugar.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                adicionarLog("ERRO ao carregar FXML: " + e.getMessage());
                e.printStackTrace();
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
    private void handleEncerrarSimulacao(ActionEvent event) {
        if (simulador != null) {
            simulador.encerrar();
            atualizarInterfaceCompleta(); // Atualiza a aba principal uma última vez
            exibirRelatorioFinalGrafico(); // Chame este método aqui!

            btnIniciar.setDisable(false);
            btnPausar.setDisable(true);
            btnContinuar.setDisable(true);
            btnEncerrar.setDisable(true);
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

        // Sul
        if (lblZonaSulLixoGerado != null && lblZonaSulLixoColetado != null) {
            if (zonaSul != null && stats != null) {
                lblZonaSulLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", stats.getLixoGeradoPorZona(zonaSul.getNome())) + " kg");
                lblZonaSulLixoColetado.setText("Coletado: " + String.format(brLocale, "%,d", stats.getLixoColetadoPorZona(zonaSul.getNome())) + " kg");
            } else if (zonaSul != null) {
                lblZonaSulLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", zonaSul.getLixoAcumulado()) + " kg");
                lblZonaSulLixoColetado.setText("Coletado: -- kg");
            } else {
                lblZonaSulLixoGerado.setText("Gerado: -- kg");
                lblZonaSulLixoColetado.setText("Coletado: -- kg");
            }
        }

        // Norte
        if (lblZonaNorteLixoGerado != null && lblZonaNorteLixoColetado != null) {
            if (zonaNorte != null && stats != null) {
                lblZonaNorteLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", stats.getLixoGeradoPorZona(zonaNorte.getNome())) + " kg");
                lblZonaNorteLixoColetado.setText("Coletado: " + String.format(brLocale, "%,d", stats.getLixoColetadoPorZona(zonaNorte.getNome())) + " kg");
            } else if (zonaNorte != null) {
                lblZonaNorteLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", zonaNorte.getLixoAcumulado()) + " kg");
                lblZonaNorteLixoColetado.setText("Coletado: -- kg");
            } else {
                lblZonaNorteLixoGerado.setText("Gerado: -- kg");
                lblZonaNorteLixoColetado.setText("Coletado: -- kg");
            }
        }

        // Centro
        if (lblZonaCentroLixoGerado != null && lblZonaCentroLixoColetado != null) {
            if (zonaCentro != null && stats != null) {
                lblZonaCentroLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", stats.getLixoGeradoPorZona(zonaCentro.getNome())) + " kg");
                lblZonaCentroLixoColetado.setText("Coletado: " + String.format(brLocale, "%,d", stats.getLixoColetadoPorZona(zonaCentro.getNome())) + " kg");
            } else if (zonaCentro != null) {
                lblZonaCentroLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", zonaCentro.getLixoAcumulado()) + " kg");
                lblZonaCentroLixoColetado.setText("Coletado: -- kg");
            } else {
                lblZonaCentroLixoGerado.setText("Gerado: -- kg");
                lblZonaCentroLixoColetado.setText("Coletado: -- kg");
            }
        }

        // Leste
        if (lblZonaLesteLixoGerado != null && lblZonaLesteLixoColetado != null) {
            if (zonaLeste != null && stats != null) {
                lblZonaLesteLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", stats.getLixoGeradoPorZona(zonaLeste.getNome())) + " kg");
                lblZonaLesteLixoColetado.setText("Coletado: " + String.format(brLocale, "%,d", stats.getLixoColetadoPorZona(zonaLeste.getNome())) + " kg");
            } else if (zonaLeste != null) {
                lblZonaLesteLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", zonaLeste.getLixoAcumulado()) + " kg");
                lblZonaLesteLixoColetado.setText("Coletado: -- kg");
            } else {
                lblZonaLesteLixoGerado.setText("Gerado: -- kg");
                lblZonaLesteLixoColetado.setText("Coletado: -- kg");
            }
        }

        // Sudeste
        if (lblZonaSudesteLixoGerado != null && lblZonaSudesteLixoColetado != null) {
            if (zonaSudeste != null && stats != null) {
                lblZonaSudesteLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", stats.getLixoGeradoPorZona(zonaSudeste.getNome())) + " kg");
                lblZonaSudesteLixoColetado.setText("Coletado: " + String.format(brLocale, "%,d", stats.getLixoColetadoPorZona(zonaSudeste.getNome())) + " kg");
            } else if (zonaSudeste != null) {
                lblZonaSudesteLixoGerado.setText("Gerado: " + String.format(brLocale, "%,d", zonaSudeste.getLixoAcumulado()) + " kg");
                lblZonaSudesteLixoColetado.setText("Coletado: -- kg");
            } else {
                lblZonaSudesteLixoGerado.setText("Gerado: -- kg");
                lblZonaSudesteLixoColetado.setText("Coletado: -- kg");
            }
        }
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

    /**
     * Atualiza o relatório final gráfico com os dados da simulação.
     * Este método preenche os gráficos, tabelas e textos informativos
     * na aba de relatório final.
     */
    /**
     * Cria e exibe o relatório final programaticamente, sem depender do FXML.
     * Esta versão cria todos os componentes de UI via código para maior confiabilidade.
     */
    /**
     * Atualiza o relatório final gráfico com os dados da simulação.
     * Este método preenche os gráficos, tabelas e textos informativos
     * na aba de relatório final.
     */
    private void exibirRelatorioFinalGrafico() {
        if (simulador == null || simulador.getEstatisticas() == null) {
            adicionarLog("Simulador ou estatísticas não disponíveis para gerar relatório gráfico.");
            return;
        }

        final Estatisticas stats = simulador.getEstatisticas();
        adicionarLog("Gerando relatório final com " + stats.getLixoTotalGerado() + "kg de lixo gerado.");

        Platform.runLater(() -> {
            try {
                // Criar o layout principal
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);

                VBox mainContainer = new VBox(20);
                mainContainer.setAlignment(Pos.TOP_CENTER);
                mainContainer.setPadding(new Insets(30));
                mainContainer.setStyle("-fx-background-color: #f4f6f8;");

                // Título principal
                Label titleLabel = new Label("RELATÓRIO FINAL DA SIMULAÇÃO DE COLETA DE LIXO EM TERESINA");
                titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                titleLabel.setPadding(new Insets(10, 0, 20, 0));
                mainContainer.getChildren().add(titleLabel);

                // ---- Seção 1: Gráfico de Lixo por Zona ----
                VBox chartSection = new VBox(10);
                chartSection.setAlignment(Pos.CENTER_LEFT);
                chartSection.setPadding(new Insets(20));
                chartSection.setStyle("-fx-background-color: white; -fx-border-color: #dfe4ea; " +
                        "-fx-border-radius: 8px; -fx-background-radius: 8px;");

                Label chartTitle = new Label("Lixo Gerado por Zona");
                chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e; " +
                        "-fx-border-width: 0 0 2px 0; -fx-border-color: #1a237e;");

                // Criar o gráfico
                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Zonas Urbanas");
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Lixo Gerado (kg)");

                BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                barChart.setLegendVisible(true);
                barChart.setPrefHeight(350);

                // Adicionar dados ao gráfico
                XYChart.Series<String, Number> seriesLixoGerado = new XYChart.Series<>();
                seriesLixoGerado.setName("Lixo Gerado");

                XYChart.Series<String, Number> seriesLixoColetado = new XYChart.Series<>();
                seriesLixoColetado.setName("Lixo Coletado");

                String[] nomesZonasOrdenadas = {"Sul", "Norte", "Centro", "Leste", "Sudeste"};

                if (stats.getEstatisticasZonas() != null && stats.getEstatisticasZonas().tamanho() > 0) {
                    Lista<Estatisticas.EntradaZona> zonasStats = stats.getEstatisticasZonas();
                    for (String nomeZona : nomesZonasOrdenadas) {
                        boolean encontrada = false;
                        for (int i = 0; i < zonasStats.tamanho(); i++) {
                            Estatisticas.EntradaZona zonaStat = zonasStats.obter(i);
                            if (zonaStat.nomeZona.equals(nomeZona)) {
                                seriesLixoGerado.getData().add(new XYChart.Data<>(nomeZona, zonaStat.lixoGerado));
                                seriesLixoColetado.getData().add(new XYChart.Data<>(nomeZona, zonaStat.lixoColetado));
                                encontrada = true;
                                break;
                            }
                        }
                        if (!encontrada) {
                            seriesLixoGerado.getData().add(new XYChart.Data<>(nomeZona, 0));
                            seriesLixoColetado.getData().add(new XYChart.Data<>(nomeZona, 0));
                        }
                    }
                } else {
                    for (String nomeZona : nomesZonasOrdenadas) {
                        seriesLixoGerado.getData().add(new XYChart.Data<>(nomeZona, 0));
                        seriesLixoColetado.getData().add(new XYChart.Data<>(nomeZona, 0));
                    }
                }

                barChart.getData().addAll(seriesLixoGerado, seriesLixoColetado);

                // ADICIONADO: Definir cores personalizadas para as séries
                for (int i = 0; i < barChart.getData().size(); i++) {
                    XYChart.Series<String, Number> s = barChart.getData().get(i);
                    if (i == 0) { // Lixo Gerado - Vermelho
                        for (XYChart.Data<String, Number> data : s.getData()) {
                            data.getNode().setStyle("-fx-bar-fill: #e74c3c;"); // Vermelho
                        }
                    } else if (i == 1) { // Lixo Coletado - Azul
                        for (XYChart.Data<String, Number> data : s.getData()) {
                            data.getNode().setStyle("-fx-bar-fill: #3498db;"); // Azul
                        }
                    }
                }

                chartSection.getChildren().addAll(chartTitle, barChart);
                mainContainer.getChildren().add(chartSection);

                // ---- Seção 2: Tabela de Métricas por Estação ----
                VBox tableSection = new VBox(10);
                tableSection.setAlignment(Pos.CENTER_LEFT);
                tableSection.setPadding(new Insets(20));
                tableSection.setStyle("-fx-background-color: white; -fx-border-color: #dfe4ea; " +
                        "-fx-border-radius: 8px; -fx-background-radius: 8px;");

                Label tableTitle = new Label("Métricas por Estação");
                tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e; " +
                        "-fx-border-width: 0 0 2px 0; -fx-border-color: #1a237e;");

                // Criar a tabela
                TableView<MetricaEstacaoModel> tableView = new TableView<>();
                tableView.setPrefHeight(150);

                TableColumn<MetricaEstacaoModel, String> colEstacaoNome = new TableColumn<>("Estação");
                colEstacaoNome.setCellValueFactory(new PropertyValueFactory<>("nomeEstacao"));
                colEstacaoNome.setPrefWidth(150);

                TableColumn<MetricaEstacaoModel, Integer> colCaminhoesAtendidos = new TableColumn<>("Caminhões Atendidos");
                colCaminhoesAtendidos.setCellValueFactory(new PropertyValueFactory<>("caminhoesAtendidos"));
                colCaminhoesAtendidos.setPrefWidth(150);
                colCaminhoesAtendidos.setStyle("-fx-alignment: CENTER;");

                TableColumn<MetricaEstacaoModel, Double> colTempoMedioEspera = new TableColumn<>("Tempo Médio de Espera");
                colTempoMedioEspera.setCellValueFactory(new PropertyValueFactory<>("tempoMedioEspera"));
                colTempoMedioEspera.setPrefWidth(180);
                colTempoMedioEspera.setStyle("-fx-alignment: CENTER;");
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

                TableColumn<MetricaEstacaoModel, Integer> colLixoTransferidoEstacao = new TableColumn<>("Lixo Transferido");
                colLixoTransferidoEstacao.setCellValueFactory(new PropertyValueFactory<>("lixoTransferido"));
                colLixoTransferidoEstacao.setPrefWidth(150);
                colLixoTransferidoEstacao.setStyle("-fx-alignment: CENTER_RIGHT;");
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

                tableView.getColumns().addAll(colEstacaoNome, colCaminhoesAtendidos, colTempoMedioEspera, colLixoTransferidoEstacao);

                // Adicionar dados à tabela
                ObservableList<MetricaEstacaoModel> metricasEstacaoList = FXCollections.observableArrayList();
                if (stats.getEstatisticasEstacoes() != null && stats.getEstatisticasEstacoes().tamanho() > 0) {
                    Lista<Estatisticas.EntradaEstacao> estacoesStats = stats.getEstatisticasEstacoes();
                    for (int i = 0; i < estacoesStats.tamanho(); i++) {
                        Estatisticas.EntradaEstacao estStat = estacoesStats.obter(i);
                        metricasEstacaoList.add(new MetricaEstacaoModel(
                                estStat.nomeEstacao,
                                estStat.caminhoesPequenosDescarregados,
                                stats.getTempoMedioEsperaPorEstacao(estStat.nomeEstacao),
                                stats.getLixoTransferidoPorEstacao(estStat.nomeEstacao)
                        ));
                    }
                } else {
                    metricasEstacaoList.add(new MetricaEstacaoModel("Sem dados", 0, 0.0, 0));
                }
                tableView.setItems(metricasEstacaoList);

                tableSection.getChildren().addAll(tableTitle, tableView);
                mainContainer.getChildren().add(tableSection);

                // ---- Seção 3: Principais Descobertas ----
                VBox discoverySection = new VBox(15);
                discoverySection.setPadding(new Insets(20));
                discoverySection.setStyle("-fx-border-color: #e0e0e0; -fx-padding: 20px; " +
                        "-fx-border-radius: 5px; -fx-background-radius: 5px; -fx-background-color: #f8f9fa;");

                Label discoveryTitle = new Label("Principais Descobertas");
                discoveryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                double eficienciaColeta = stats.calcularPercentualLixoColetado();

                Label lblEficiencia = new Label("Eficiência de Coleta: O sistema conseguiu coletar " +
                        String.format(brLocale, "%.2f%% do lixo gerado.", eficienciaColeta));
                lblEficiencia.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-line-spacing: 3px;");
                lblEficiencia.setWrapText(true);

                Label lblDistribuicao = new Label("Distribuição de Lixo: Análise da distribuição do lixo entre as zonas urbanas.");
                lblDistribuicao.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-line-spacing: 3px;");
                lblDistribuicao.setWrapText(true);

                Label lblGargalos = new Label("Gargalos no Sistema: O tempo médio de espera nas estações (" +
                        String.format(brLocale, "%.1f", stats.calcularTempoMedioEsperaFilaPequenos()) +
                        " minutos) pode indicar o nível de congestionamento nas estações de transferência.");
                lblGargalos.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-line-spacing: 3px;");
                lblGargalos.setWrapText(true);

                Label lblOtimizacao = new Label("Otimização da Frota: " +
                        String.format("O sistema %s durante a simulação.",
                                (stats.getCaminhoesGrandesAdicionados() > 0) ?
                                        "precisou adicionar " + stats.getCaminhoesGrandesAdicionados() +
                                                " caminhões grandes além da frota inicial" :
                                        "não precisou adicionar novos caminhões grandes à frota inicial"));
                lblOtimizacao.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333; -fx-line-spacing: 3px;");
                lblOtimizacao.setWrapText(true);

                discoverySection.getChildren().addAll(discoveryTitle, lblEficiencia, lblDistribuicao, lblGargalos, lblOtimizacao);
                mainContainer.getChildren().add(discoverySection);

                // ---- Seção 4: Conclusão ----
                VBox conclusionSection = new VBox(15);
                conclusionSection.setAlignment(Pos.CENTER);
                conclusionSection.setPadding(new Insets(25));
                conclusionSection.setStyle("-fx-background-color: #34495e; -fx-padding: 25px; " +
                        "-fx-border-radius: 8px; -fx-background-radius: 8px;");

                Label conclusionTitle = new Label("Conclusão");
                conclusionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

                Label conclusionText1 = new Label("A simulação demonstra que o sistema de coleta de lixo de Teresina, " +
                        String.format(brLocale, "com a configuração testada, apresentou uma eficiência de %.2f%% na coleta.", eficienciaColeta));
                conclusionText1.setStyle("-fx-font-size: 13px; -fx-text-fill: #ecf0f1; -fx-line-spacing: 4px; -fx-text-alignment: justify;");
                conclusionText1.setWrapText(true);

                int numCGNecessarios = stats.calcularEstimativaCaminhoesGrandesNecessarios();
                Label conclusionHighlight = new Label(String.format(brLocale, "%d Caminhões de %d Toneladas",
                        numCGNecessarios, CaminhaoGrande.CAPACIDADE_MAXIMA_KG / 1000));
                conclusionHighlight.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffffff; " +
                        "-fx-padding: 15px 0px; -fx-alignment: center; -fx-background-color: #2c3e50; " +
                        "-fx-background-radius: 5px; -fx-border-color: #46627f; -fx-border-width: 1px; -fx-border-radius: 5px;");

                Label conclusionText2 = new Label("Esta conclusão considera a quantidade total de lixo coletado diariamente nas cinco zonas e a capacidade de transporte dos caminhões grandes. " +
                        String.format(brLocale, "Com uma frota mínima de %d caminhões grandes, o sistema pode operar com eficiência.", numCGNecessarios));
                conclusionText2.setStyle("-fx-font-size: 13px; -fx-text-fill: #ecf0f1; -fx-line-spacing: 4px; -fx-text-alignment: justify;");
                conclusionText2.setWrapText(true);

                conclusionSection.getChildren().addAll(conclusionTitle, conclusionText1, conclusionHighlight, conclusionText2);
                mainContainer.getChildren().add(conclusionSection);

                scrollPane.setContent(mainContainer);

                // Definir o conteúdo na aba
                if (tabRelatorioFinal != null) {
                    tabRelatorioFinal.setContent(scrollPane);
                    tabPanePrincipal.getSelectionModel().select(tabRelatorioFinal);
                    adicionarLog("Relatório final gerado programaticamente com sucesso!");
                } else {
                    adicionarLog("ERRO: Aba de relatório final não encontrada.");
                }

            } catch (Exception e) {
                adicionarLog("ERRO: Falha ao gerar relatório programaticamente: " + e.getMessage());
                e.printStackTrace();
            }
        });
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