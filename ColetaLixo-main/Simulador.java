import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

// Importe as classes necessárias
import Estruturas.Lista;
// import Estruturas.Fila; // Fila não é usada diretamente aqui
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.StatusCaminhao;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;
import caminhoes.CaminhaoGrandePadrao; // Import necessário

public class Simulador implements Serializable {
    private static final long serialVersionUID = 1L; // Para serialização

    // --- Constantes de Configuração de Tempo ---
    private static final int MINUTOS_EM_UM_DIA = 24 * 60;
    // Horário de Pico (exemplo: 7:00-8:59 e 17:00-18:59)
    private static final int PICO_MANHA_INICIO_MIN = 7 * 60;   // 420
    private static final int PICO_MANHA_FIM_MIN = 9 * 60 - 1; // 539
    private static final int PICO_TARDE_INICIO_MIN = 17 * 60;  // 1020
    private static final int PICO_TARDE_FIM_MIN = 19 * 60 - 1; // 1139
    // Tempos de Viagem (exemplo)
    private static final int MIN_VIAGEM_FORA_PICO_MIN = 15;
    private static final int MAX_VIAGEM_FORA_PICO_MIN = 45;
    private static final int MIN_VIAGEM_PICO_MIN = 30;
    private static final int MAX_VIAGEM_PICO_MIN = 90;
    // Intervalo padrão para relatório horário
    private static final int INTERVALO_RELATORIO_MIN = 60;
    // Intervalo de atualização do timer (em milissegundos reais)
    private static final int INTERVALO_TIMER_MS = 50; // 50ms = 20 atualizações/segundo real

    // --- Atributos da Simulação ---
    private transient Timer timer; // transient: não serializar o timer
    private int tempoSimulado = 0; // Em minutos simulados (0 a 1439)
    private boolean pausado = false;
    private transient Random random; // transient: inicializar após deserialização

    private Estatisticas estatisticas = new Estatisticas();

    // Listas gerenciadas pelo simulador
    private Lista<CaminhaoPequeno> listaCaminhoesPequenosOciosos;
    private Lista<CaminhaoGrande> listaCaminhoesGrandesDisponiveis;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;

    // Listas para acompanhar caminhões ativos
    private Lista<CaminhaoPequeno> caminhoesEmViagem = new Lista<>();
    private Lista<CaminhaoPequeno> caminhoesEmColeta = new Lista<>();

    // Contadores para caminhões grandes
    private int totalCaminhoesGrandesCriados = 0;
    private int caminhoesGrandesEmUso = 0;

    // Parâmetros configuráveis
    private int toleranciaCaminhoesGrandes = CaminhaoGrandePadrao.TOLERANCIA_ESPERA_PADRAO_MINUTOS;
    private int intervaloEstatisticas = INTERVALO_RELATORIO_MIN;


    /**
     * Construtor do Simulador.
     * Inicializa as listas e o gerador aleatório.
     */
    public Simulador() {
        this.listaCaminhoesPequenosOciosos = new Lista<>();
        this.listaCaminhoesGrandesDisponiveis = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
        this.random = new Random();
    }

    // --- Métodos Setters para configuração inicial ---
    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) { this.listaCaminhoesPequenosOciosos = lista; }
    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) {
        this.listaCaminhoesGrandesDisponiveis = lista;
        this.totalCaminhoesGrandesCriados = lista.tamanho();
    }
    public void setListaZonas(Lista<ZonaUrbana> lista) { this.listaZonas = lista; }
    public void setListaEstacoes(Lista<EstacaoTransferencia> lista) { this.listaEstacoes = lista; }

    public void setToleranciaCaminhoesGrandes(int tolerancia) {
        if (tolerancia < 1) {
            throw new IllegalArgumentException("A tolerância deve ser pelo menos 1 minuto.");
        }
        this.toleranciaCaminhoesGrandes = tolerancia;
        for (int i = 0; i < listaCaminhoesGrandesDisponiveis.tamanho(); i++) {
            CaminhaoGrande caminhao = listaCaminhoesGrandesDisponiveis.obter(i);
            caminhao.setToleranciaEspera(tolerancia);
        }
        // Nota: Caminhões já em uso nas estações não terão sua tolerância atualizada por este método.
        // Se for necessário, iterar nas estações e nos caminhões grandes alocados nelas.
        if (!listaEstacoes.estaVazia()) {
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                EstacaoTransferencia est = listaEstacoes.obter(i);
                if (est instanceof EstacaoPadrao) {
                    EstacaoPadrao estPadrao = (EstacaoPadrao) est;
                    // Se a EstacaoPadrao expusesse seu caminhaoGrandeAtual, poderíamos atualizar.
                    // Por ora, esta alteração afeta principalmente os caminhões recém-criados ou os disponíveis.
                }
            }
        }
    }

    public void setIntervaloRelatorio(int intervalo) {
        if (intervalo < 1) {
            throw new IllegalArgumentException("O intervalo do relatório deve ser pelo menos 1 minuto.");
        }
        this.intervaloEstatisticas = intervalo;
    }

    // --- Métodos de Controle da Simulação ---
    public void iniciar() {
        if (timer != null) {
            System.out.println("Simulação já iniciada. Use 'encerrar' primeiro para reiniciar.");
            return;
        }
        if (listaZonas.estaVazia() || listaEstacoes.estaVazia()) {
            System.err.println("ERRO: A simulação não pode iniciar sem Zonas e Estações configuradas!");
            return;
        }

        System.out.println("Iniciando Simulação...");
        pausado = false;
        tempoSimulado = 0;

        if (random == null) {
            random = new Random();
        }

        caminhoesEmViagem = new Lista<>();
        caminhoesEmColeta = new Lista<>();
        caminhoesGrandesEmUso = 0;

        estatisticas.resetar();
        estatisticas.registrarTotalInicialCaminhoesGrandes(this.listaCaminhoesGrandesDisponiveis.tamanho());

        distribuirCaminhoesPorZonas();

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) {
                    try {
                        tempoSimulado = (tempoSimulado + 1) % MINUTOS_EM_UM_DIA;
                        atualizarSimulacao();
                    } catch (Exception e) {
                        System.err.println("ERRO CRÍTICO NO LOOP DA SIMULAÇÃO! Tempo: " + formatarTempo(tempoSimulado));
                        e.printStackTrace();
                        pausar();
                    }
                }
            }
        }, 0, INTERVALO_TIMER_MS);

        System.out.println("Simulação iniciada. Tempo real de atualização: " + INTERVALO_TIMER_MS + "ms.");
    }

    private void distribuirCaminhoesPorZonas() {
        if (listaZonas.estaVazia()) {
            System.err.println("AVISO: Não há zonas para distribuir caminhões.");
            return;
        }
        if (listaCaminhoesPequenosOciosos.estaVazia()) {
            System.out.println("INFO: Nenhum caminhão pequeno ocioso para distribuir.");
            return;
        }

        System.out.println("Distribuindo " + listaCaminhoesPequenosOciosos.tamanho() + " caminhões pequenos pelas zonas...");

        while (!listaCaminhoesPequenosOciosos.estaVazia()) {
            CaminhaoPequeno caminhao = listaCaminhoesPequenosOciosos.remover(0);
            int idxZona = random.nextInt(listaZonas.tamanho());
            ZonaUrbana zonaDestino = listaZonas.obter(idxZona);

            caminhao.definirDestino(zonaDestino); // Isso agora também define zonaDeOrigemParaRetorno
            caminhao.setStatus(StatusCaminhao.COLETANDO);
            caminhoesEmColeta.adicionar(caminhao);
        }
        System.out.println("Distribuição inicial concluída. " + caminhoesEmColeta.tamanho() + " caminhões em coleta.");
    }

    public void pausar() {
        if (timer == null) {
            System.out.println("Simulação não iniciada.");
            return;
        }
        if (!pausado) {
            System.out.println("Simulação pausada.");
            pausado = true;
        } else {
            System.out.println("Simulação já está pausada.");
        }
    }

    public void continuarSimulacao() {
        if(timer == null) {
            System.out.println("Simulação não iniciada. Use iniciar().");
            return;
        }
        if (pausado) {
            System.out.println("Simulação retomada.");
            pausado = false;
        } else {
            System.out.println("Simulação já está em execução.");
        }
    }

    public void encerrar() {
        System.out.println("Encerrando simulação...");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        pausado = true;

        try {
            String nomeArquivo = "relatorio_simulacao_" + System.currentTimeMillis() + ".txt";
            System.out.println("\n" + estatisticas.gerarRelatorio());
            estatisticas.salvarRelatorio(nomeArquivo);
            // System.out.println("Relatório final salvo em " + nomeArquivo); // Mensagem já está em estatisticas.salvarRelatorio
        } catch (IOException e) {
            System.err.println("Erro ao salvar relatório final: " + e.getMessage());
        }
        System.out.println("Simulação encerrada.");
    }

    // --- Métodos de Persistência (Salvar/Carregar) ---
    public void gravar(String caminhoArquivo) throws IOException {
        boolean estavaPausado = this.pausado;
        pausar();

        System.out.println("Salvando estado da simulação em " + caminhoArquivo + "...");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(this);
            System.out.println("Simulação salva com sucesso.");
        } finally {
            if (!estavaPausado) {
                continuarSimulacao();
            }
        }
    }

    public static Simulador carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        System.out.println("Carregando estado da simulação de " + caminhoArquivo + "...");
        Simulador sim = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo))) {
            sim = (Simulador) ois.readObject();

            sim.timer = null;
            sim.pausado = true;
            sim.random = new Random();

            System.out.println("Simulação carregada. Está pausada. Use 'continuar' ou 'iniciar' (reinicia o tempo).");
        }
        if (sim == null) {
            throw new IOException("Falha ao carregar o objeto Simulador, objeto nulo após deserialização.");
        }
        if (sim.caminhoesEmViagem == null) sim.caminhoesEmViagem = new Lista<>();
        if (sim.caminhoesEmColeta == null) sim.caminhoesEmColeta = new Lista<>();
        if (sim.listaCaminhoesPequenosOciosos == null) sim.listaCaminhoesPequenosOciosos = new Lista<>();
        if (sim.listaCaminhoesGrandesDisponiveis == null) sim.listaCaminhoesGrandesDisponiveis = new Lista<>();

        return sim;
    }

    // --- Métodos Auxiliares Internos ---
    private boolean isHorarioDePico(int tempoAtualMinutos) {
        boolean manha = (tempoAtualMinutos >= PICO_MANHA_INICIO_MIN && tempoAtualMinutos <= PICO_MANHA_FIM_MIN);
        boolean tarde = (tempoAtualMinutos >= PICO_TARDE_INICIO_MIN && tempoAtualMinutos <= PICO_TARDE_FIM_MIN);
        return manha || tarde;
    }

    private int calcularTempoViagemPequeno() {
        int min, max;
        if (isHorarioDePico(this.tempoSimulado)) {
            min = MIN_VIAGEM_PICO_MIN;
            max = MAX_VIAGEM_PICO_MIN;
        } else {
            min = MIN_VIAGEM_FORA_PICO_MIN;
            max = MAX_VIAGEM_FORA_PICO_MIN;
        }
        if (min >= max) return min;
        return random.nextInt(max - min + 1) + min;
    }

    public void adicionarCaminhaoGrande() {
        System.out.println("INFO: Adicionando um novo caminhão grande...");
        CaminhaoGrande novoCaminhao = new CaminhaoGrandePadrao(this.toleranciaCaminhoesGrandes);
        listaCaminhoesGrandesDisponiveis.adicionar(novoCaminhao);
        totalCaminhoesGrandesCriados++;
        estatisticas.registrarNovoCaminhaoGrande();
        System.out.println("Novo caminhão grande adicionado! Total Criados: " + totalCaminhoesGrandesCriados +
                ", Disponíveis: " + listaCaminhoesGrandesDisponiveis.tamanho());
    }

    // --- LÓGICA PRINCIPAL DA SIMULAÇÃO ---
    private void atualizarSimulacao() {
        // 1. Início do Dia
        if (tempoSimulado == 0) {
            System.out.printf("\n--- INÍCIO DO NOVO DIA (Tempo: %s) ---\n", formatarTempo(tempoSimulado));
            reiniciarViagensDiariasCaminhoes();
        }

        // 2. Geração de Lixo (ocorre uma vez por hora, no início da hora)
        if (tempoSimulado % 60 == 0) { // A cada hora
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                int lixoAntes = zona.getLixoAcumulado();
                zona.gerarLixo(); // A mensagem de geração já está em ZonaUrbana
                int geradoNestaHora = zona.getLixoAcumulado() - lixoAntes;
                if (geradoNestaHora > 0) {
                    estatisticas.registrarGeracaoLixo(zona.getNome(), geradoNestaHora);
                }
            }
        }

        // 3. Processar Caminhões Pequenos
        processarCaminhoesEmColeta();
        processarCaminhoesEmViagem();

        // 4. Processar Estações de Transferência
        processarEstacoes();

        // 5. Atualizar Estatísticas Agregadas
        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);

        // 6. Relatório Periódico
        if (tempoSimulado % intervaloEstatisticas == 0 && tempoSimulado != 0) {
            imprimirRelatorioHorario();
        }
    }

    private void reiniciarViagensDiariasCaminhoes() {
        System.out.println("INFO: Reiniciando contadores de viagens diárias dos caminhões pequenos.");
        int countReiniciados = 0;
        // Itera sobre todas as listas que podem conter caminhões pequenos
        Lista<CaminhaoPequeno>[] listasDeCaminhoes = new Lista[]{
                caminhoesEmColeta, caminhoesEmViagem, listaCaminhoesPequenosOciosos
        };
        // E também os caminhões nas filas das estações
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                // Precisaria de um método para pegar os caminhões da fila da EstacaoPadrao
                // e iterar sobre eles. Para simplificar, vamos focar nas listas principais do simulador.
                // A alternativa é que CaminhaoPequeno.reiniciarViagensDiarias() seja chamado
                // quando o caminhão volta a ficar OCIOSO ou COLETANDO.
            }
        }

        for (Lista<CaminhaoPequeno> lista : listasDeCaminhoes) {
            for (int i = 0; i < lista.tamanho(); i++) {
                lista.obter(i).reiniciarViagensDiarias();
                countReiniciados++;
            }
        }
        // Para caminhões em filas de estações, eles serão reiniciados quando voltarem ao ciclo (ociosos/coletando)
        System.out.println("INFO: Contadores de " + countReiniciados + " caminhões pequenos (em listas principais) reiniciados.");
    }

    private void processarCaminhoesEmColeta() {
        for (int i = caminhoesEmColeta.tamanho() - 1; i >= 0; i--) {
            CaminhaoPequeno caminhao = caminhoesEmColeta.obter(i);
            ZonaUrbana zona = caminhao.getZonaAtual(); // zonaAtual deve estar definida aqui

            if (caminhao.getStatus() == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                continue;
            }

            if (zona != null && !caminhao.estaCheio()) {
                int lixoDisponivel = zona.getLixoAcumulado();
                if (lixoDisponivel > 0) {
                    int lixoRealmenteColetado = caminhao.coletar(lixoDisponivel); // Método já imprime log de erro se qtd <= 0
                    if (lixoRealmenteColetado > 0) {
                        zona.coletarLixo(lixoRealmenteColetado); // Remove da zona
                        estatisticas.registrarColetaLixo(zona.getNome(), lixoRealmenteColetado);
                        // System.out.printf("COLETA: CP %s coletou %dkg de %s. Carga: %d/%dkg%n",
                        // caminhao.getPlaca(), lixoRealmenteColetado, zona.getNome(),
                        // caminhao.getCargaAtual(), caminhao.getCapacidade());
                    }
                }
            }

            if (caminhao.estaCheio() && !listaEstacoes.estaVazia()) {
                EstacaoTransferencia melhorEstacao = null;
                int menorFila = Integer.MAX_VALUE;

                for (int j = 0; j < listaEstacoes.tamanho(); j++) {
                    EstacaoTransferencia estacaoAtual = listaEstacoes.obter(j);
                    if (estacaoAtual instanceof EstacaoPadrao) {
                        EstacaoPadrao estacaoPadrao = (EstacaoPadrao) estacaoAtual;
                        int tamanhoFila = estacaoPadrao.getCaminhoesNaFila();
                        if (tamanhoFila < menorFila) {
                            menorFila = tamanhoFila;
                            melhorEstacao = estacaoAtual;
                        }
                    }
                }

                if (melhorEstacao == null) {
                    System.err.println("ERRO CRÍTICO: Nenhuma estação do tipo PADRÃO encontrada para " + caminhao.getPlaca() + ". Caminhão ficará parado.");
                    continue;
                }

                caminhao.definirDestino(melhorEstacao); // Isso define status VIAJANDO_ESTACAO, zera zonaAtual e guarda zonaDeOrigem
                int tempoViagem = calcularTempoViagemPequeno();
                caminhao.definirTempoViagem(tempoViagem);

                System.out.printf("VIAGEM->EST: CP %s (%dkg) de %s para Est. %s (%d na fila). Tempo: %d min.%n",
                        caminhao.getPlaca(), caminhao.getCargaAtual(),
                        (caminhao.getZonaDeOrigemParaRetorno() != null ? caminhao.getZonaDeOrigemParaRetorno().getNome() : "Origem Desconhecida"), // Usa zona de origem para log
                        melhorEstacao.getNome(), menorFila, tempoViagem);

                caminhao.registrarViagem(); // Pode se tornar INATIVO aqui

                caminhoesEmViagem.adicionar(caminhao);
                caminhoesEmColeta.remover(i);

            } else if (caminhao.estaCheio() && listaEstacoes.estaVazia()) {
                System.err.println("AVISO: Caminhão " + caminhao.getPlaca() + " está cheio, mas não há estações para descarregar!");
            }
        }
    }

    private void processarCaminhoesEmViagem() {
        for (int i = caminhoesEmViagem.tamanho() - 1; i >= 0; i--) {
            CaminhaoPequeno caminhao = caminhoesEmViagem.obter(i);
            boolean viagemCompleta = caminhao.processarViagem();

            if (viagemCompleta) {
                StatusCaminhao statusAtual = caminhao.getStatus();
                EstacaoTransferencia estacaoDest = caminhao.getEstacaoDestino();
                ZonaUrbana zonaDestRetorno = caminhao.getZonaAtual(); // Usado se estiver RETORNANDO_ZONA

                if (statusAtual == StatusCaminhao.VIAJANDO_ESTACAO && estacaoDest != null) {
                    System.out.printf("CHEGADA EST: CP %s chegou à Estação %s.%n", caminhao.getPlaca(), estacaoDest.getNome());
                    if (estacaoDest instanceof EstacaoPadrao) {
                        ((EstacaoPadrao) estacaoDest).receberCaminhaoPequeno(caminhao);
                        caminhao.setStatus(StatusCaminhao.NA_FILA); // Estação gerencia a partir daqui
                        estatisticas.registrarChegadaEstacao(estacaoDest.getNome());
                        caminhoesEmViagem.remover(i);
                    } else {
                        System.err.println("ERRO: CP " + caminhao.getPlaca() + " chegou a uma estação desconhecida: " + estacaoDest.getNome() + ". Retornando aos ociosos.");
                        caminhao.setStatus(StatusCaminhao.OCIOSO);
                        listaCaminhoesPequenosOciosos.adicionar(caminhao);
                        caminhoesEmViagem.remover(i);
                    }
                } else if (statusAtual == StatusCaminhao.RETORNANDO_ZONA && zonaDestRetorno != null) {
                    System.out.printf("RETORNO ZONA: CP %s retornou à Zona %s para coletar.%n", caminhao.getPlaca(), zonaDestRetorno.getNome());
                    if (caminhao.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) { // Dupla checagem, pois registrarViagem pode inativar
                        caminhao.setStatus(StatusCaminhao.COLETANDO);
                        // caminhao.definirDestino(zonaDestRetorno); // zonaAtual já foi definida como destino de retorno
                        caminhoesEmColeta.adicionar(caminhao);
                        caminhoesEmViagem.remover(i);
                    } else {
                        System.out.println("INFO: CP " + caminhao.getPlaca() + " retornou à zona, mas está INATIVO. Indo para ociosos.");
                        listaCaminhoesPequenosOciosos.adicionar(caminhao); // Já está inativo
                        caminhoesEmViagem.remover(i);
                    }
                } else if (statusAtual == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                    System.out.println("INFO: CP inativo " + caminhao.getPlaca() + " completou sua última viagem. Indo para ociosos.");
                    listaCaminhoesPequenosOciosos.adicionar(caminhao);
                    caminhoesEmViagem.remover(i);
                } else {
                    System.err.printf("ERRO INESPERADO: CP %s completou viagem. Status: %s, EstDest: %s, ZonaDestRet: %s. Indo para ociosos.%n",
                            caminhao.getPlaca(), statusAtual, estacaoDest, zonaDestRetorno);
                    caminhao.setStatus(StatusCaminhao.OCIOSO);
                    listaCaminhoesPequenosOciosos.adicionar(caminhao);
                    caminhoesEmViagem.remover(i);
                }
            }
        }
    }

    private void processarEstacoes() {
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            EstacaoTransferencia estacao = listaEstacoes.obter(i);
            if (estacao instanceof EstacaoPadrao) {
                EstacaoPadrao estacaoPadrao = (EstacaoPadrao) estacao;
                CaminhaoPequeno caminhaoProcessado = estacaoPadrao.processarFila();

                if (caminhaoProcessado != null) { // Um caminhão pequeno foi descarregado
                    // TODO: Calcular tempo de espera real e passar para registrarAtendimentoCaminhaoPequeno
                    long tempoDeEsperaEstimado = 0; // Placeholder
                    estatisticas.registrarAtendimentoCaminhaoPequeno(estacao.getNome(), tempoDeEsperaEstimado);

                    // MODIFICADO: Usa getZonaDeOrigemParaRetorno()
                    ZonaUrbana zonaRetorno = caminhaoProcessado.getZonaDeOrigemParaRetorno();

                    if (caminhaoProcessado.getStatus() == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        System.out.println("INFO: CP " + caminhaoProcessado.getPlaca() + " descarregou, mas está INATIVO. Indo para ociosos.");
                        listaCaminhoesPequenosOciosos.adicionar(caminhaoProcessado);
                    } else if (zonaRetorno != null) {
                        caminhaoProcessado.setStatus(StatusCaminhao.RETORNANDO_ZONA);
                        caminhaoProcessado.definirDestino(zonaRetorno); // Define zonaAtual e zonaDeOrigem para esta nova fase
                        int tempoViagemRetorno = calcularTempoViagemPequeno();
                        caminhaoProcessado.definirTempoViagem(tempoViagemRetorno);

                        System.out.printf("RETORNO->ZONA: CP %s (%s) de Est. %s para Zona %s. Tempo: %d min.%n",
                                caminhaoProcessado.getPlaca(), caminhaoProcessado.getStatus(),
                                estacao.getNome(), zonaRetorno.getNome(), tempoViagemRetorno);
                        caminhoesEmViagem.adicionar(caminhaoProcessado);
                    } else {
                        // Este é o caso que queremos evitar/minimizar
                        System.err.println("AVISO: CP " + caminhaoProcessado.getPlaca() + " descarregou, mas não tem zona de ORIGEM para retorno definida. Indo para ociosos.");
                        caminhaoProcessado.setStatus(StatusCaminhao.OCIOSO);
                        listaCaminhoesPequenosOciosos.adicionar(caminhaoProcessado);
                    }
                }

                CaminhaoGrande caminhaoGrandeQuePartiu = estacaoPadrao.gerenciarTempoEsperaCaminhaoGrande();
                if (caminhaoGrandeQuePartiu != null) {
                    estatisticas.registrarTransporteLixo(caminhaoGrandeQuePartiu.getCargaAtual()); // Usa carga real transportada
                    // O método descarregar() em CaminhaoGrande zera a carga, então pegamos antes ou passamos como parâmetro.
                    // Em EstacaoPadrao, caminhaoQuePartiu.descarregar() é chamado após o log.
                    // Para ser preciso, registrarTransporteLixo deveria receber a carga que *foi* descarregada.
                    // Vamos assumir que getCargaAtual() antes de descarregar() está ok por enquanto.
                    listaCaminhoesGrandesDisponiveis.adicionar(caminhaoGrandeQuePartiu);
                    caminhoesGrandesEmUso--;
                    System.out.println("INFO: Caminhão grande retornou do aterro e está disponível. CGs Disp: " + listaCaminhoesGrandesDisponiveis.tamanho() + ", Em Uso: " + caminhoesGrandesEmUso);
                }

                if (estacaoPadrao.precisaCaminhaoGrande()) {
                    atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                }

                if (estacaoPadrao.tempoEsperaExcedido()) {
                    System.out.println("ALERTA! Tempo de espera de CPs excedido na Estação " + estacao.getNome() + "! Adicionando novo CG à frota.");
                    adicionarCaminhaoGrande(); // Adiciona um novo à frota GERAL
                    if (estacaoPadrao.precisaCaminhaoGrande()) { // Verifica novamente se AINDA precisa
                        atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                    }
                }
            }
        }
    }

    private void atribuirCaminhaoGrandeParaEstacao(EstacaoPadrao estacao) {
        if (estacao == null || !estacao.precisaCaminhaoGrande()) return; // Dupla checagem

        if (!listaCaminhoesGrandesDisponiveis.estaVazia()) {
            CaminhaoGrande caminhao = listaCaminhoesGrandesDisponiveis.remover(0);
            boolean sucesso = estacao.atribuirCaminhaoGrande(caminhao);
            if (sucesso) {
                caminhoesGrandesEmUso++;
                System.out.printf("ALOCAÇÃO CG: CG alocado para Est. %s. Em uso: %d. Disp: %d%n",
                        estacao.getNome(), caminhoesGrandesEmUso, listaCaminhoesGrandesDisponiveis.tamanho());
            } else {
                System.err.println("ERRO: Falha ao atribuir CG à est. " + estacao.getNome() + " (já tem um?). Devolvendo à lista.");
                listaCaminhoesGrandesDisponiveis.adicionar(caminhao); // Adiciona de volta no início ou fim
            }
        } else {
            System.out.println("INFO: Est. " + estacao.getNome() + " precisa de CG, mas NENHUM está disponível.");
        }
    }

    private void imprimirRelatorioHorario() {
        System.out.println("\n========================================================================");
        System.out.printf("=== RELATÓRIO DE STATUS === Tempo: %s %s ===%n", formatarTempo(tempoSimulado), isHorarioDePico(tempoSimulado) ? "(PICO)" : "");
        System.out.println("------------------------------------------------------------------------");

        int totalPequenos = caminhoesEmColeta.tamanho() + caminhoesEmViagem.tamanho() + listaCaminhoesPequenosOciosos.tamanho();
        // Contar caminhões na fila das estações para o total
        int pequenosNaFilaEstacoes = 0;
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                pequenosNaFilaEstacoes += ((EstacaoPadrao) listaEstacoes.obter(i)).getCaminhoesNaFila();
            }
        }
        totalPequenos += pequenosNaFilaEstacoes;

        System.out.printf("CPS: %d totais | %d coleta | %d viagem | %d fila est. | %d ociosos/inativos%n",
                totalPequenos,
                caminhoesEmColeta.tamanho(),
                caminhoesEmViagem.tamanho(),
                pequenosNaFilaEstacoes,
                listaCaminhoesPequenosOciosos.tamanho());

        System.out.printf("CGS: %d criados | %d em uso | %d disponíveis%n",
                totalCaminhoesGrandesCriados,
                caminhoesGrandesEmUso,
                listaCaminhoesGrandesDisponiveis.tamanho());
        System.out.println("------------------------------------------------------------------------");

        System.out.println("ZONAS (Lixo Acumulado):");
        int lixoTotalZonas = 0;
        if (!listaZonas.estaVazia()) {
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                lixoTotalZonas += zona.getLixoAcumulado();
                System.out.printf("  - %-10s: %6d kg%n", zona.getNome(), zona.getLixoAcumulado());
            }
            System.out.printf("  Total nas Zonas: %d kg%n", lixoTotalZonas);
        } else {
            System.out.println("  (Nenhuma zona configurada)");
        }
        System.out.println("------------------------------------------------------------------------");

        System.out.println("ESTAÇÕES:");
        if (!listaEstacoes.estaVazia()) {
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                    EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);
                    System.out.printf("  - Est. %-10s: %d CPs na fila | CG Presente: %s (Carga: %d kg)%n",
                            estacao.getNome(),
                            estacao.getCaminhoesNaFila(),
                            estacao.temCaminhaoGrande() ? "Sim" : "Não",
                            estacao.getCargaCaminhaoGrandeAtual());
                } else {
                    System.out.printf("  - Est. %-10s: (Tipo não padrão)%n", listaEstacoes.obter(i).getNome());
                }
            }
        } else {
            System.out.println("  (Nenhuma estação configurada)");
        }
        System.out.println("========================================================================\n");
    }

    // --- Getters para acesso externo (ex: interfaces) ---
    public Estatisticas getEstatisticas() { return estatisticas; }
    public int getTempoSimulado() { return tempoSimulado; }
    public int getTotalCaminhoesGrandes() { return totalCaminhoesGrandesCriados; }
    public int getCaminhoesGrandesEmUso() { return caminhoesGrandesEmUso; }
    public int getCaminhoesGrandesDisponiveis() { return listaCaminhoesGrandesDisponiveis.tamanho(); }
    public int getToleranciaCaminhoesGrandes() { return toleranciaCaminhoesGrandes; }
    public boolean isPausado() { return pausado; }
    public Lista<ZonaUrbana> getListaZonas() { return listaZonas; }
    public Lista<EstacaoTransferencia> getListaEstacoes() { return listaEstacoes; }

    private String formatarTempo(int minutosTotais) {
        int dias = minutosTotais / MINUTOS_EM_UM_DIA;
        int minutosNoDia = minutosTotais % MINUTOS_EM_UM_DIA;
        int horas = minutosNoDia / 60;
        int minutos = minutosNoDia % 60;
        if (dias > 0) {
            return String.format("Dia %d, %02d:%02d", dias + 1, horas, minutos);
        }
        return String.format("%02d:%02d", horas, minutos);
    }
}