import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

import Estruturas.Lista;
import caminhoes.CaminhaoPequeno;
import caminhoes.CaminhaoGrande;
import caminhoes.StatusCaminhao;
import zonas.MapaUrbano;
import zonas.ZonaUrbana;
import estacoes.EstacaoTransferencia;
import estacoes.EstacaoPadrao;
import estacoes.ResultadoProcessamentoFila;
import caminhoes.CaminhaoGrandePadrao;

public class Simulador implements Serializable {
    private static final long serialVersionUID = 2L;

    // --- Constantes de Configuração de Tempo ---
    private static final int MINUTOS_EM_UM_DIA = 24 * 60;
    private static final int PICO_MANHA_INICIO_MIN = 7 * 60;
    private static final int PICO_MANHA_FIM_MIN = 9 * 60 - 1;
    private static final int PICO_TARDE_INICIO_MIN = 17 * 60;
    private static final int PICO_TARDE_FIM_MIN = 19 * 60 - 1;
    private static final int MIN_VIAGEM_FORA_PICO_MIN = 15;
    private static final int MAX_VIAGEM_FORA_PICO_MIN = 45;
    private static final int MIN_VIAGEM_PICO_MIN = 30;
    private static final int MAX_VIAGEM_PICO_MIN = 90;
    private static final int INTERVALO_RELATORIO_MIN = 60;
    private static final int INTERVALO_TIMER_MS = 50;

    // --- Atributos da Simulação ---
    private transient Timer timer;
    private int tempoSimulado = 0; // Acumula minutos totais da simulação
    private boolean pausado = false;
    private transient Random random;
    private Estatisticas estatisticas = new Estatisticas();

    // Atributos para o sistema de distribuição
    private MapaUrbano mapaUrbano;
    private DistribuicaoCaminhoes distribuicaoCaminhoes;

    // Novos atributos para a garagem central e configurações de distribuição
    private GaragemCentral garagemCentral;
    private boolean usarGaragemCentral = true; // Por padrão, usar garagem central
    private boolean garantirDistribuicaoMinima = true; // Garantir pelo menos um caminhão por zona
    private int caminhoesPorZonaMinimo = 1; // Mínimo de caminhões por zona
    private int limiteViagensDiarias = 10; // Limite configurável (usando o padrão da classe CaminhaoPequeno)

    // --- Listas Principais de Entidades ---
    private Lista<CaminhaoPequeno> todosOsCaminhoesPequenos;
    private Lista<CaminhaoGrande> listaCaminhoesGrandesDisponiveis;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;

    // Contadores para caminhões grandes
    private int totalCaminhoesGrandesCriados = 0; // Total de CGs que fazem/fizeram parte da frota
    private int caminhoesGrandesEmUso = 0;

    // Parâmetros configuráveis
    private int toleranciaCaminhoesGrandes = CaminhaoGrandePadrao.TOLERANCIA_ESPERA_PADRAO_MINUTOS;
    private int intervaloEstatisticas = INTERVALO_RELATORIO_MIN;

    public Simulador() {
        this.todosOsCaminhoesPequenos = new Lista<>();
        this.listaCaminhoesGrandesDisponiveis = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
        this.random = new Random();

        // Inicializa o mapa urbano
        this.mapaUrbano = new MapaUrbano();

        // Inicializa configurações da distribuição
        this.usarGaragemCentral = true;
        this.garantirDistribuicaoMinima = true;
        this.caminhoesPorZonaMinimo = 1;
        this.limiteViagensDiarias = 10; // Valor padrão, agora configurável
    }

    // --- Método para inicializar a garagem central ---
    public void inicializarGaragemCentral() {
        if (this.mapaUrbano == null) {
            this.mapaUrbano = new MapaUrbano();
        }
        this.garagemCentral = new GaragemCentral("Garagem Central", this.mapaUrbano);

        // Configura o distribuidor
        if (this.distribuicaoCaminhoes != null) {
            this.garagemCentral.setDistribuidor(this.distribuicaoCaminhoes);

            if (this.garantirDistribuicaoMinima) {
                this.distribuicaoCaminhoes.setGarantirDistribuicaoMinima(true);
                this.distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(this.caminhoesPorZonaMinimo);
            }
        }

        // Define o limite de viagens diárias
        this.garagemCentral.setLimiteViagensDiarias(this.limiteViagensDiarias);

        // Transfere caminhões ociosos para a garagem
        if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno cp = todosOsCaminhoesPequenos.obter(i);
                if (cp.getStatus() == StatusCaminhao.OCIOSO) {
                    garagemCentral.adicionarCaminhao(cp);
                }
            }
        }

        System.out.println("Garagem Central inicializada. Configurações: " +
                "Garantir distribuição mínima=" + garantirDistribuicaoMinima +
                ", Mínimo por zona=" + caminhoesPorZonaMinimo +
                ", Limite viagens=" + limiteViagensDiarias);
    }

    // --- Setters para Configuração ---
    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) {
        this.todosOsCaminhoesPequenos = lista;
        if (this.todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < this.todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno cp = this.todosOsCaminhoesPequenos.obter(i);
                // Ao setar a lista, garante que os caminhões estejam em um estado inicial consistente
                if (cp.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                    cp.setStatus(StatusCaminhao.OCIOSO);
                }
                cp.setTempoChegadaNaFila(-1); // Reseta informação de fila
                cp.setLimiteViagensDiarias(this.limiteViagensDiarias); // Define o limite configurado
            }
        }
    }

    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) {
        this.listaCaminhoesGrandesDisponiveis = lista;
        if (lista != null) {
            this.totalCaminhoesGrandesCriados = lista.tamanho(); // Define o número inicial de CGs na frota
        } else {
            this.totalCaminhoesGrandesCriados = 0;
            this.listaCaminhoesGrandesDisponiveis = new Lista<>(); // Garante que não seja nulo
        }
    }

    public void setListaZonas(Lista<ZonaUrbana> lista) {
        this.listaZonas = lista;

        // Inicializa o distribuidor de caminhões quando as zonas são definidas
        if (this.mapaUrbano != null && this.listaZonas != null && !this.listaZonas.estaVazia()) {
            this.distribuicaoCaminhoes = new DistribuicaoCaminhoes(this.mapaUrbano, this.listaZonas);

            // Configura o distribuidor com as novas configurações
            if (this.distribuicaoCaminhoes != null) {
                this.distribuicaoCaminhoes.setGarantirDistribuicaoMinima(this.garantirDistribuicaoMinima);
                this.distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(this.caminhoesPorZonaMinimo);
            }
        }
    }

    public void setListaEstacoes(Lista<EstacaoTransferencia> lista) {
        this.listaEstacoes = lista;
    }

    public void setToleranciaCaminhoesGrandes(int tolerancia) {
        if (tolerancia < 1) {
            throw new IllegalArgumentException("A tolerância deve ser pelo menos 1 minuto.");
        }
        this.toleranciaCaminhoesGrandes = tolerancia;
        // Atualiza para caminhões grandes disponíveis
        if (listaCaminhoesGrandesDisponiveis != null) {
            for (int i = 0; i < listaCaminhoesGrandesDisponiveis.tamanho(); i++) {
                listaCaminhoesGrandesDisponiveis.obter(i).setToleranciaEspera(tolerancia);
            }
        }
        // Atualiza para caminhões grandes já em uso nas estações
        if (listaEstacoes != null) {
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                    EstacaoPadrao estPadrao = (EstacaoPadrao) listaEstacoes.obter(i);
                    CaminhaoGrande cgEmUso = estPadrao.getCaminhaoGrandeAtual();
                    if (cgEmUso != null) {
                        cgEmUso.setToleranciaEspera(tolerancia);
                    }
                }
            }
        }
    }

    public void setIntervaloRelatorio(int intervalo) {
        if (intervalo < 1) { throw new IllegalArgumentException("O intervalo do relatório deve ser pelo menos 1 minuto.");}
        this.intervaloEstatisticas = intervalo;
    }

    // --- Setters para as novas configurações ---
    public void setUsarGaragemCentral(boolean usarGaragemCentral) {
        this.usarGaragemCentral = usarGaragemCentral;
    }

    public void setGarantirDistribuicaoMinima(boolean garantirDistribuicaoMinima) {
        this.garantirDistribuicaoMinima = garantirDistribuicaoMinima;

        // Atualiza a configuração no distribuidor, se existir
        if (distribuicaoCaminhoes != null) {
            distribuicaoCaminhoes.setGarantirDistribuicaoMinima(garantirDistribuicaoMinima);
        }
    }

    public void setCaminhoesPorZonaMinimo(int caminhoesPorZonaMinimo) {
        if (caminhoesPorZonaMinimo < 1) {
            throw new IllegalArgumentException("O número mínimo de caminhões por zona deve ser pelo menos 1");
        }
        this.caminhoesPorZonaMinimo = caminhoesPorZonaMinimo;

        // Atualiza a configuração no distribuidor, se existir
        if (distribuicaoCaminhoes != null) {
            distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(caminhoesPorZonaMinimo);
        }
    }

    public void setLimiteViagensDiarias(int limiteViagensDiarias) {
        if (limiteViagensDiarias < 1) {
            throw new IllegalArgumentException("O limite de viagens diárias deve ser pelo menos 1");
        }
        this.limiteViagensDiarias = limiteViagensDiarias;

        // Atualiza o limite na garagem central, se existir
        if (garagemCentral != null) {
            garagemCentral.setLimiteViagensDiarias(limiteViagensDiarias);
        }

        // Atualiza também todos os caminhões, caso a garagem não exista ou não esteja em uso
        if (!usarGaragemCentral && todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                todosOsCaminhoesPequenos.obter(i).setLimiteViagensDiarias(limiteViagensDiarias);
            }
        }
    }

    // --- Controle da Simulação ---
    public void iniciar() {
        if (timer != null) {
            System.out.println("Simulação já iniciada. Use 'encerrar' primeiro para reiniciar.");
            return;
        }
        if (listaZonas == null || listaZonas.estaVazia() ||
                listaEstacoes == null || listaEstacoes.estaVazia() ||
                todosOsCaminhoesPequenos == null || todosOsCaminhoesPequenos.estaVazia()) {
            System.err.println("ERRO: Simulação não pode iniciar sem Zonas, Estações e Caminhões Pequenos configurados!");
            return;
        }

        System.out.println("Iniciando Simulação...");
        pausado = false;
        tempoSimulado = 0; // Reseta o tempo total da simulação
        if (random == null) random = new Random();

        caminhoesGrandesEmUso = 0;
        estatisticas.resetar();
        // totalCaminhoesGrandesCriados já foi setado por setListaCaminhoesGrandes
        estatisticas.registrarTotalInicialCaminhoesGrandes(this.totalCaminhoesGrandesCriados);

        // Inicializa garagem central se estiver habilitada e não existir ainda
        if (this.usarGaragemCentral && this.garagemCentral == null) {
            inicializarGaragemCentral();
        }

        reiniciarViagensDiariasTodosCaminhoesPequenos(); // Garante que todos comecem o dia com viagens zeradas
        distribuirCaminhoesOciososParaColeta(); // Distribui os que estão ociosos

        timer = new Timer(true); // Daemon thread
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) {
                    try {
                        int diaAnteriorSimulado = tempoSimulado / MINUTOS_EM_UM_DIA;
                        tempoSimulado = (tempoSimulado + 1); // Avança 1 minuto
                        int diaAtualSimulado = tempoSimulado / MINUTOS_EM_UM_DIA;

                        if (diaAtualSimulado > diaAnteriorSimulado) { // Houve uma virada de dia
                            System.out.printf("\n--- TRANSIÇÃO PARA O DIA %d (Tempo Simulado Total: %d min) ---\n", diaAtualSimulado + 1, tempoSimulado);
                            reiniciarViagensDiariasTodosCaminhoesPequenos();
                            distribuirCaminhoesOciososParaColeta();
                        }
                        atualizarSimulacao();
                    } catch (Exception e) {
                        System.err.println("ERRO CRÍTICO NO LOOP DA SIMULAÇÃO! Tempo: " + formatarTempo(tempoSimulado));
                        e.printStackTrace();
                        pausar();
                    }
                }
            }
        }, 0, INTERVALO_TIMER_MS);
        System.out.println("Simulação iniciada. Atualização a cada " + INTERVALO_TIMER_MS + "ms.");
    }

    public void pausar() {
        if (timer == null) { System.out.println("Simulação não iniciada."); return; }
        if (!pausado) { System.out.println("Simulação pausada."); pausado = true; }
        else { System.out.println("Simulação já está pausada."); }
    }

    public void continuarSimulacao() {
        if(timer == null) { System.out.println("Simulação não iniciada. Use iniciar()."); return; }
        if (pausado) { System.out.println("Simulação retomada."); pausado = false; }
        else { System.out.println("Simulação já está em execução."); }
    }

    public void encerrar() {
        System.out.println("Encerrando simulação...");
        if (timer != null) { timer.cancel(); timer = null; }
        pausado = true;
        try {
            String nomeArquivo = "relatorio_simulacao_" + System.currentTimeMillis() + ".txt";
            System.out.println("\n" + estatisticas.gerarRelatorio()); // Imprime no console (que é a JTextArea)
            estatisticas.salvarRelatorio(nomeArquivo); // Salva em arquivo
        } catch (IOException e) { System.err.println("Erro ao salvar relatório final: " + e.getMessage()); }
        System.out.println("Simulação encerrada.");
    }

    private void distribuirCaminhoesOciososParaColeta() {
        if (listaZonas == null || listaZonas.estaVazia()) {
            return;
        }

        int distribuidos = 0;

        // Usa a garagem central se estiver configurada
        if (usarGaragemCentral && garagemCentral != null) {
            distribuidos = garagemCentral.distribuirCaminhoesParaZonas(listaZonas, distribuicaoCaminhoes);
        }
        // Caso contrário, usa o código antigo
        else if (distribuicaoCaminhoes != null) {
            // O código existente para usar o distribuidor
            distribuidos = distribuicaoCaminhoes.distribuirCaminhoes(todosOsCaminhoesPequenos, listaZonas);
        } else {
            // Código existente de distribuição aleatória
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno caminhao = todosOsCaminhoesPequenos.obter(i);
                if (caminhao.getStatus() == StatusCaminhao.OCIOSO) {
                    int idxZona = random.nextInt(listaZonas.tamanho());
                    ZonaUrbana zonaDestino = listaZonas.obter(idxZona);
                    caminhao.definirDestino(zonaDestino);
                    caminhao.setStatus(StatusCaminhao.COLETANDO);
                    distribuidos++;
                }
            }
        }

        if (distribuidos > 0) {
            String metodo = usarGaragemCentral ? "garagem central" :
                    (distribuicaoCaminhoes != null ? "distribuição inteligente" : "distribuição aleatória");
            System.out.println(distribuidos + " caminhões pequenos ociosos enviados para coleta usando " + metodo);
        }
    }

    public void gravar(String caminhoArquivo) throws IOException {
        boolean estavaPausado = this.pausado; pausar();
        System.out.println("Salvando estado da simulação em " + caminhoArquivo + "...");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(this); System.out.println("Simulação salva com sucesso.");
        } finally { if (!estavaPausado && timer != null) { continuarSimulacao(); } } // Só continua se timer ainda existe
    }

    public static Simulador carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        System.out.println("Carregando estado da simulação de " + caminhoArquivo + "...");
        Simulador sim = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo))) {
            sim = (Simulador) ois.readObject(); sim.timer = null; sim.pausado = true; sim.random = new Random();
            if (sim.todosOsCaminhoesPequenos == null) sim.todosOsCaminhoesPequenos = new Lista<>();
            if (sim.listaCaminhoesGrandesDisponiveis == null) sim.listaCaminhoesGrandesDisponiveis = new Lista<>();
            // Se houver outros atributos que precisam de inicialização pós-deserialização, adicione aqui.
            System.out.println("Simulação carregada. Está pausada.");
        }
        if (sim == null) { throw new IOException("Falha ao carregar Simulador: objeto nulo."); }
        return sim;
    }

    private boolean isHorarioDePico(int tempoAtualMinutosNoDia) {
        return (tempoAtualMinutosNoDia >= PICO_MANHA_INICIO_MIN && tempoAtualMinutosNoDia <= PICO_MANHA_FIM_MIN) ||
                (tempoAtualMinutosNoDia >= PICO_TARDE_INICIO_MIN && tempoAtualMinutosNoDia <= PICO_TARDE_FIM_MIN);
    }

    private int calcularTempoViagemPequeno() {
        int min, max;
        int tempoNoDia = tempoSimulado % MINUTOS_EM_UM_DIA; // Usa o tempo atual no dia para calcular o pico
        if (isHorarioDePico(tempoNoDia)) {
            min = MIN_VIAGEM_PICO_MIN; max = MAX_VIAGEM_PICO_MIN;
        } else {
            min = MIN_VIAGEM_FORA_PICO_MIN; max = MAX_VIAGEM_FORA_PICO_MIN;
        }
        if (min >= max) return min; // Evita erro com nextInt se min == max
        return random.nextInt(max - min + 1) + min;
    }

    public void adicionarCaminhaoGrande() {
        System.out.println("INFO: Adicionando um novo caminhão grande...");
        CaminhaoGrande novoCaminhao = new CaminhaoGrandePadrao(this.toleranciaCaminhoesGrandes);
        if (listaCaminhoesGrandesDisponiveis == null) listaCaminhoesGrandesDisponiveis = new Lista<>();
        listaCaminhoesGrandesDisponiveis.adicionar(novoCaminhao);
        totalCaminhoesGrandesCriados++; // Incrementa o contador da frota total
        estatisticas.registrarNovoCaminhaoGrande(); // Notifica estatísticas
        System.out.println("Novo caminhão grande adicionado! Total na Frota: " + totalCaminhoesGrandesCriados +
                ", Disponíveis: " + listaCaminhoesGrandesDisponiveis.tamanho());
    }

    private void atualizarSimulacao() {
        int tempoNoDia = this.tempoSimulado % MINUTOS_EM_UM_DIA;

        // Geração de Lixo (ocorre uma vez por hora, no início da hora)
        if (tempoNoDia % 60 == 0) {
            if (listaZonas != null) {
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
            // Atualiza o tempo sem coleta para o sistema de distribuição inteligente
            if (distribuicaoCaminhoes != null) {
                distribuicaoCaminhoes.incrementarTempoSemColeta(1); // 1 minuto por iteração
            }
        }

        processarTodosCaminhoesPequenos();
        processarEstacoes();

        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);
        if (tempoNoDia % intervaloEstatisticas == 0 && this.tempoSimulado != 0) { // Evita relatório no tempo 0
            imprimirRelatorioHorario();
        }
    }

    private void reiniciarViagensDiariasTodosCaminhoesPequenos() {
        // Se estiver usando garagem central, delega a ela
        if (usarGaragemCentral && garagemCentral != null) {
            garagemCentral.reiniciarViagensDiarias();
        }
        // Caso contrário, usa o código original
        else if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                todosOsCaminhoesPequenos.obter(i).reiniciarViagensDiarias();
            }
        }
    }

    private void processarTodosCaminhoesPequenos() {
        if (todosOsCaminhoesPequenos == null) return;

        for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
            CaminhaoPequeno caminhao = todosOsCaminhoesPequenos.obter(i);
            StatusCaminhao statusAtual = caminhao.getStatus();

            if (statusAtual == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                continue; // Será tratado na virada do dia
            }

            switch (statusAtual) {
                case OCIOSO:
                    // Será pego por distribuirCaminhoesOciososParaColeta() se necessário
                    break;
                case COLETANDO:
                    processarCaminhaoEmColeta(caminhao);
                    break;
                case VIAJANDO_ESTACAO: // Caminhão indo para estação
                case RETORNANDO_ZONA:  // Caminhão voltando da estação para zona
                    processarCaminhaoEmViagem(caminhao);
                    break;
                case NA_FILA:
                    // O caminhão está na fila da estação. A estação o processará.
                    break;
                case DESCARREGANDO:
                    // Este status é gerenciado pela Estacao enquanto o caminhão está nela.
                    // O Simulador não precisa agir diretamente sobre ele aqui.
                    break;
                default:
                    System.err.println("ALERTA SIM: Caminhão " + caminhao.getPlaca() + " em status desconhecido/não tratado: " + statusAtual);
                    break;
            }
        }
    }

    private void processarCaminhaoEmColeta(CaminhaoPequeno caminhao) {
        ZonaUrbana zona = caminhao.getZonaAtual();
        if (zona == null) {
            System.err.println("ERRO SIM CRÍTICO: CP " + caminhao.getPlaca() + " em COLETANDO mas zonaAtual é null! Definindo como OCIOSO.");
            caminhao.setStatus(StatusCaminhao.OCIOSO); // Medida de segurança
            return;
        }

        if (!caminhao.estaCheio()) {
            int lixoDisponivel = zona.getLixoAcumulado();
            if (lixoDisponivel > 0) {
                int lixoColetado = caminhao.coletar(lixoDisponivel);
                if (lixoColetado > 0) {
                    zona.coletarLixo(lixoColetado);
                    estatisticas.registrarColetaLixo(zona.getNome(), lixoColetado);

                    // Registra a coleta para o sistema de distribuição inteligente
                    if (distribuicaoCaminhoes != null) {
                        distribuicaoCaminhoes.registrarColetaEmZona(zona.getNome());
                    }
                }
            }
        }

        if (caminhao.estaCheio()) {
            if (listaEstacoes == null || listaEstacoes.estaVazia()) {
                System.err.println("AVISO SIM: CP " + caminhao.getPlaca() + " está cheio, mas não há estações para descarregar!");
                return; // Caminhão fica cheio na zona, não pode ir a lugar nenhum
            }

            EstacaoTransferencia melhorEstacao = null;
            int menorFila = Integer.MAX_VALUE;
            if (listaEstacoes != null) {
                for (int j = 0; j < listaEstacoes.tamanho(); j++) {
                    EstacaoTransferencia est = listaEstacoes.obter(j);
                    if (est instanceof EstacaoPadrao) {
                        int tamanhoFila = ((EstacaoPadrao) est).getCaminhoesNaFila();
                        if (tamanhoFila < menorFila) {
                            menorFila = tamanhoFila;
                            melhorEstacao = est;
                        }
                    }
                }
            }

            if (melhorEstacao == null) {
                System.err.println("ERRO SIM: Nenhuma EstacaoPadrao disponível para CP " + caminhao.getPlaca() + " descarregar.");
                return; // Não há para onde ir
            }

            String zonaOrigemNome = (caminhao.getZonaDeOrigemParaRetorno() != null) ? caminhao.getZonaDeOrigemParaRetorno().getNome() : "OrigemDesconhecida";
            int tempoViagemCalc = calcularTempoViagemPequeno();
            System.out.printf("ENVIO EST: CP %s (%dkg) de Zona %s -> Est. %s (fila: %d). Viagem: %d min.%n",
                    caminhao.getPlaca(), caminhao.getCargaAtual(), zonaOrigemNome,
                    melhorEstacao.getNome(), menorFila, tempoViagemCalc);

            caminhao.definirDestino(melhorEstacao); // Atualiza status para VIAJANDO_ESTACAO, estacaoDestino, e zera zonaAtual
            caminhao.definirTempoViagem(tempoViagemCalc);
            if (!caminhao.registrarViagem()) { // Se false, caminhão ficou INATIVO_LIMITE_VIAGENS
                System.out.println("INFO SIM: CP " + caminhao.getPlaca() + " tornou-se INATIVO ao registrar viagem para estação " + melhorEstacao.getNome());
            }
        }
    }

    private void processarCaminhaoEmViagem(CaminhaoPequeno caminhao) {
        if (caminhao.processarViagem()) { // Viagem concluída
            StatusCaminhao statusAtualCaminhao = caminhao.getStatus(); // Captura o status ATUAL (pode ser INATIVO_LIMITE_VIAGENS)

            // Se o caminhão se tornou INATIVO durante a viagem (ou ao registrar a viagem)
            if (statusAtualCaminhao == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                System.out.println("INFO SIM: CP INATIVO " + caminhao.getPlaca() + " completou sua última viagem programada e permanece INATIVO.");
                // Ele não fará a ação de chegada (entrar na fila ou coletar)
                // Será tratado na virada do dia.
                return;
            }

            // Se estava VIAJANDO_ESTACAO e NÃO está inativo por limite
            if (caminhao.getEstacaoDestino() != null && statusAtualCaminhao == StatusCaminhao.VIAJANDO_ESTACAO) {
                EstacaoTransferencia estacaoDest = caminhao.getEstacaoDestino();
                System.out.printf("CHEGADA EST: CP %s chegou à Estação %s.%n", caminhao.getPlaca(), estacaoDest.getNome());
                if (estacaoDest instanceof EstacaoPadrao) {
                    ((EstacaoPadrao) estacaoDest).receberCaminhaoPequeno(caminhao, this.tempoSimulado % MINUTOS_EM_UM_DIA);
                    caminhao.setStatus(StatusCaminhao.NA_FILA);
                    // caminhao.setEstacaoDestino(estacaoDest); // já setado
                    // caminhao.setZonaAtual(null); // JÁ É NULL desde definirDestino(estacao) - REMOVIDO DAQUI
                    estatisticas.registrarChegadaEstacao(estacaoDest.getNome());
                } else {
                    System.err.println("ERRO SIM: CP " + caminhao.getPlaca() + " chegou a est. não padrão " + estacaoDest.getNome() + ". OCIOSO.");
                    caminhao.setStatus(StatusCaminhao.OCIOSO);
                }
            }
            // Se estava RETORNANDO_ZONA e NÃO está inativo por limite
            else if (caminhao.getZonaAtual() != null && statusAtualCaminhao == StatusCaminhao.RETORNANDO_ZONA) {
                ZonaUrbana zonaDest = caminhao.getZonaAtual();
                System.out.printf("RETORNO ZONA: CP %s retornou à Zona %s para coletar.%n", caminhao.getPlaca(), zonaDest.getNome());
                caminhao.setStatus(StatusCaminhao.COLETANDO);
            } else {
                // Se chegou aqui, é um estado inesperado APÓS a viagem E NÃO é INATIVO_LIMITE_VIAGENS
                System.err.printf("ALERTA SIM: CP %s completou viagem mas estado final é inesperado: Status=%s, EstDest=%s, ZonaAtual=%s. OCIOSO.%n",
                        caminhao.getPlaca(), statusAtualCaminhao, caminhao.getEstacaoDestino(), caminhao.getZonaAtual());
                caminhao.setStatus(StatusCaminhao.OCIOSO);
            }
        }
    }

    private void processarEstacoes() {
        if (listaEstacoes == null) return;
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                EstacaoPadrao estacaoPadrao = (EstacaoPadrao) listaEstacoes.obter(i);
                ResultadoProcessamentoFila resultado = estacaoPadrao.processarFila(this.tempoSimulado % MINUTOS_EM_UM_DIA);
                CaminhaoPequeno caminhaoProcessado = resultado.getCaminhaoProcessado();
                long tempoEsperaReal = resultado.getTempoDeEspera();

                if (caminhaoProcessado != null) { // Um caminhão foi descarregado
                    estatisticas.registrarAtendimentoCaminhaoPequeno(estacaoPadrao.getNome(), tempoEsperaReal);
                    ZonaUrbana zonaDeRetornoOriginal = caminhaoProcessado.getZonaDeOrigemParaRetorno();

                    // Verifica se o caminhão está INATIVO_LIMITE_VIAGENS *após* descarregar
                    if (caminhaoProcessado.getStatus() == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        System.out.println("INFO SIM: CP " + caminhaoProcessado.getPlaca() + " descarregou, mas já está INATIVO_LIMITE_VIAGENS.");
                        // Não precisa mudar status, ele já está inativo.

                        // Se estiver usando garagem central, pode recolher o caminhão inativo
                        if (usarGaragemCentral && garagemCentral != null) {
                            if (zonaDeRetornoOriginal != null) {
                                garagemCentral.retornarCaminhaoDeZona(caminhaoProcessado, zonaDeRetornoOriginal);
                                System.out.println("INFO SIM: CP INATIVO " + caminhaoProcessado.getPlaca() +
                                        " retornou para a garagem central após descarregar na estação.");
                            }
                        }
                    } else if (zonaDeRetornoOriginal != null) {
                        caminhaoProcessado.setStatus(StatusCaminhao.RETORNANDO_ZONA);
                        caminhaoProcessado.definirDestino(zonaDeRetornoOriginal); // Define zonaAtual para retorno
                        caminhaoProcessado.definirTempoViagem(calcularTempoViagemPequeno());
                        System.out.printf("RETORNO PREP: CP %s de Est. %s -> Zona %s. Viagem: %d min.%n",
                                caminhaoProcessado.getPlaca(), estacaoPadrao.getNome(), zonaDeRetornoOriginal.getNome(), caminhaoProcessado.getTempoRestanteViagem()); // Usa GETTER
                    } else {
                        System.err.println("AVISO CRÍTICO SIM: CP " + caminhaoProcessado.getPlaca() + " descarregou SEM zona de origem para retorno! Tornando OCIOSO.");
                        caminhaoProcessado.setStatus(StatusCaminhao.OCIOSO);
                    }
                }

                // Gerenciar caminhão grande
                CaminhaoGrande cgQuePartiu = estacaoPadrao.gerenciarTempoEsperaCaminhaoGrande();
                if (cgQuePartiu != null) {
                    int cargaTransportada = cgQuePartiu.getCargaAtual(); // Pega a carga ANTES de descarregar
                    cgQuePartiu.descarregar(); // Zera a carga do caminhão grande
                    estatisticas.registrarTransporteLixo(cargaTransportada);

                    if (listaCaminhoesGrandesDisponiveis == null) listaCaminhoesGrandesDisponiveis = new Lista<>();
                    listaCaminhoesGrandesDisponiveis.adicionar(cgQuePartiu);
                    caminhoesGrandesEmUso--;
                    System.out.println("INFO SIM: CG ("+cgQuePartiu+") retornou do aterro. CGs Disp: " + listaCaminhoesGrandesDisponiveis.tamanho() + ", Em Uso: " + caminhoesGrandesEmUso);
                }

                // Verificar necessidade de alocar ou adicionar novo CG
                if (estacaoPadrao.precisaCaminhaoGrande()) {
                    atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                }
                if (estacaoPadrao.tempoEsperaExcedido()) {
                    System.out.println("ALERTA EST SIM: Tempo de espera de CPs excedido na Est. " + estacaoPadrao.getNome() + "! Adicionando novo CG à frota.");
                    adicionarCaminhaoGrande(); // Adiciona à frota geral
                    if (estacaoPadrao.precisaCaminhaoGrande()) { // Re-verifica se AINDA precisa após adição
                        atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                    }
                }
            }
        }
    }

    private void atribuirCaminhaoGrandeParaEstacao(EstacaoPadrao estacao) {
        if (estacao == null || !estacao.precisaCaminhaoGrande()) return;
        if (listaCaminhoesGrandesDisponiveis == null || listaCaminhoesGrandesDisponiveis.estaVazia()) {
            //System.out.println("INFO SIM: Est. " + estacao.getNome() + " precisa de CG, mas NENHUM está disponível."); // Log opcional
            return;
        }

        CaminhaoGrande caminhao = listaCaminhoesGrandesDisponiveis.remover(0);
        if (estacao.atribuirCaminhaoGrande(caminhao)) {
            caminhoesGrandesEmUso++;
            System.out.printf("ALOCAÇÃO CG SIM: CG alocado para Est. %s. Em uso: %d. Disp: %d%n",
                    estacao.getNome(), caminhoesGrandesEmUso, listaCaminhoesGrandesDisponiveis.tamanho());
        } else {
            System.err.println("ERRO SIM: Falha ao atribuir CG ("+caminhao+") à est. " + estacao.getNome() + " (talvez já tenha um?). Devolvendo à lista de disponíveis.");
            listaCaminhoesGrandesDisponiveis.adicionar(caminhao); // Adiciona de volta ao final ou início da lista
        }
    }

    private void imprimirRelatorioHorario() {
        System.out.println("\n========================================================================");
        int tempoNoDia = this.tempoSimulado % MINUTOS_EM_UM_DIA;
        System.out.printf("=== RELATÓRIO DE STATUS === Tempo: %s %s ===%n", formatarTempo(this.tempoSimulado), isHorarioDePico(tempoNoDia) ? "(PICO)" : "");
        System.out.println("------------------------------------------------------------------------");

        int cpColetando = 0, cpEmViagem = 0, cpNaFila = 0, cpOcioso = 0, cpInativoLimite = 0, cpDescarregando = 0; // cpDescarregando pode ser omitido se NA_FILA o cobre
        if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno cp = todosOsCaminhoesPequenos.obter(i);
                switch (cp.getStatus()) {
                    case COLETANDO: cpColetando++; break;
                    case VIAJANDO_ESTACAO: case RETORNANDO_ZONA: cpEmViagem++; break;
                    case NA_FILA: cpNaFila++; break;
                    case DESCARREGANDO: cpDescarregando++; break; // Estações podem ter caminhões descarregando que não estão mais "na fila"
                    case OCIOSO: cpOcioso++; break;
                    case INATIVO_LIMITE_VIAGENS: cpInativoLimite++; break;
                }
            }
        }
        int totalCP = todosOsCaminhoesPequenos != null ? todosOsCaminhoesPequenos.tamanho() : 0;
        System.out.printf("CPS: %d tot | Col: %d | Viag: %d | Fila: %d | Desc: %d | Ocio: %d | InatLim: %d%n",
                totalCP, cpColetando, cpEmViagem, cpNaFila, cpDescarregando, cpOcioso, cpInativoLimite);

        int cgsDisponiveis = (listaCaminhoesGrandesDisponiveis != null) ? listaCaminhoesGrandesDisponiveis.tamanho() : 0;
        System.out.printf("CGS: %d frota | %d em uso | %d disponíveis%n",
                totalCaminhoesGrandesCriados, caminhoesGrandesEmUso, cgsDisponiveis);
        System.out.println("------------------------------------------------------------------------");

        System.out.println("ZONAS (Lixo Acumulado):");
        int lixoTotalZonas = 0;
        if (listaZonas != null && !listaZonas.estaVazia()) {
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                lixoTotalZonas += zona.getLixoAcumulado();

                // Se temos o sistema de distribuição, mostra o score também
                if (distribuicaoCaminhoes != null) {
                    double score = distribuicaoCaminhoes.getScoreZona(zona.getNome());
                    System.out.printf("  - %-10s: %6d kg (Score: %.2f)%n", zona.getNome(), zona.getLixoAcumulado(), score);
                } else {
                    System.out.printf("  - %-10s: %6d kg%n", zona.getNome(), zona.getLixoAcumulado());
                }
            }
            System.out.printf("  Total nas Zonas: %d kg%n", lixoTotalZonas);
        } else { System.out.println("  (Nenhuma zona configurada)"); }
        System.out.println("------------------------------------------------------------------------");

        System.out.println("ESTAÇÕES:");
        if (listaEstacoes != null && !listaEstacoes.estaVazia()) {
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                    EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);
                    System.out.printf("  - Est. %-10s: %d CPs na fila | CG Presente: %s (Carga: %d kg)%n",
                            estacao.getNome(), estacao.getCaminhoesNaFila(),
                            estacao.temCaminhaoGrande() ? "Sim" : "Não", estacao.getCargaCaminhaoGrandeAtual());
                } else { System.out.printf("  - Est. %-10s: (Tipo não padrão)%n", listaEstacoes.obter(i).getNome());}
            }
        } else { System.out.println("  (Nenhuma estação configurada)");}

        // Adiciona informação da garagem central, se disponível
        if (usarGaragemCentral && garagemCentral != null) {
            System.out.println("------------------------------------------------------------------------");
            System.out.println("GARAGEM CENTRAL:");
            System.out.printf("  - Garagem %s: %d caminhões estacionados | Limite viagens: %d%n",
                    garagemCentral.getNome(), garagemCentral.getCaminhoesEstacionados(),
                    garagemCentral.getLimiteViagensDiariasPadrao());
        }

        System.out.println("========================================================================\n");
    }

    // --- Getters para UI e outros ---
    public Estatisticas getEstatisticas() { return estatisticas; }
    public int getTempoSimulado() { return tempoSimulado; }
    public boolean isPausado() { return pausado; }
    public Lista<ZonaUrbana> getListaZonas() { return listaZonas; }
    public Lista<EstacaoTransferencia> getListaEstacoes() { return listaEstacoes; }
    public Lista<CaminhaoPequeno> getTodosOsCaminhoesPequenos() { return todosOsCaminhoesPequenos; }
    public int getCaminhoesGrandesEmUso() { return caminhoesGrandesEmUso; }
    public int getCaminhoesGrandesDisponiveis() { return listaCaminhoesGrandesDisponiveis != null ? listaCaminhoesGrandesDisponiveis.tamanho() : 0; }
    public int getTotalCaminhoesGrandes() { return totalCaminhoesGrandesCriados; }
    public int getToleranciaCaminhoesGrandes() { return toleranciaCaminhoesGrandes; }
    public boolean isUsarGaragemCentral() { return usarGaragemCentral; }
    public boolean isGarantirDistribuicaoMinima() { return garantirDistribuicaoMinima; }
    public int getCaminhoesPorZonaMinimo() { return caminhoesPorZonaMinimo; }
    public int getLimiteViagensDiarias() { return limiteViagensDiarias; }
    public GaragemCentral getGaragemCentral() { return garagemCentral; }

    private String formatarTempo(int minutosTotais) {
        int dias = minutosTotais / MINUTOS_EM_UM_DIA;
        int minutosNoDia = minutosTotais % MINUTOS_EM_UM_DIA;
        int horas = minutosNoDia / 60;
        int minutos = minutosNoDia % 60;
        if (dias > 0) {
            return String.format("Dia %d, %02d:%02d (Total: %d min)", dias + 1, horas, minutos, minutosTotais);
        }
        return String.format("%02d:%02d (Total: %d min)", horas, minutos, minutosTotais);
    }
}