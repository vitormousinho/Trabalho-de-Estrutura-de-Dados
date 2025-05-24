import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

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
    private static final long serialVersionUID = 2L; // Mantido ou incrementado conforme necessidade

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
    private static final int INTERVALO_RELATORIO_MIN = 60; // Intervalo para imprimir relatório no console
    private static final int INTERVALO_TIMER_MS = 50; // Intervalo do timer da simulação (GUI pode precisar de < 1000)

    // --- Atributos da Simulação ---
    private transient Timer timer; // transient para não ser serializado
    private int tempoSimulado = 0; // Acumula minutos totais da simulação
    private boolean pausado = false;
    private transient Random random;
    private boolean proximaEstacaoEhA = true; // Para alternância entre estações
    private Estatisticas estatisticas = new Estatisticas(); // Deve ser serializável

    // Atributos para o sistema de distribuição
    private MapaUrbano mapaUrbano; // Deve ser serializável
    private DistribuicaoCaminhoes distribuicaoCaminhoes; // Deve ser serializável

    // Novos atributos para a garagem central e configurações de distribuição
    private GaragemCentral garagemCentral; // Deve ser serializável
    private boolean usarGaragemCentral = true;
    private boolean garantirDistribuicaoMinima = true;
    private int caminhoesPorZonaMinimo = 1;
    private int limiteViagensDiarias = 10; // Limite configurável

    // --- Listas Principais de Entidades (Devem ser serializáveis se a Lista for) ---
    private Lista<CaminhaoPequeno> todosOsCaminhoesPequenos;
    private Lista<CaminhaoGrande> listaCaminhoesGrandesDisponiveis;
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;

    // Contadores para caminhões grandes
    private int totalCaminhoesGrandesCriados = 0;
    private int caminhoesGrandesEmUso = 0;

    // Parâmetros configuráveis
    private int toleranciaCaminhoesGrandes = CaminhaoGrandePadrao.TOLERANCIA_ESPERA_PADRAO_MINUTOS;
    private int intervaloEstatisticas = INTERVALO_RELATORIO_MIN; // Para relatório no console

    public Simulador() {
        this.todosOsCaminhoesPequenos = new Lista<>();
        this.listaCaminhoesGrandesDisponiveis = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
        this.random = new Random(); // Será recriado se desserializado
        this.mapaUrbano = new MapaUrbano(); // Inicializa here
        // distribuidor e garagem são inicializados depois, com base nas zonas e config
    }

    public void inicializarGaragemCentral() {
        if (this.mapaUrbano == null) {
            this.mapaUrbano = new MapaUrbano();
        }
        this.garagemCentral = new GaragemCentral("Garagem Central", this.mapaUrbano);

        if (this.distribuicaoCaminhoes != null) {
            this.garagemCentral.setDistribuidor(this.distribuicaoCaminhoes);
            // As configurações de garantirDistribuicaoMinima e caminhoesPorZonaMinimo
            // já são passadas para o distribuidor quando ele é criado ou quando as
            // zonas são setadas, e o distribuidor as usa.
        }
        this.garagemCentral.setLimiteViagensDiarias(this.limiteViagensDiarias);

        if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno cp = todosOsCaminhoesPequenos.obter(i);
                if (cp.getStatus() == StatusCaminhao.OCIOSO) {
                    garagemCentral.adicionarCaminhao(cp);
                }
            }
        }
        System.out.println("Garagem Central inicializada.");
    }

    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) {
        this.todosOsCaminhoesPequenos = lista;
        if (this.todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < this.todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno cp = this.todosOsCaminhoesPequenos.obter(i);
                if (cp.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                    cp.setStatus(StatusCaminhao.OCIOSO);
                }
                cp.setTempoChegadaNaFila(-1);
                cp.setLimiteViagensDiarias(this.limiteViagensDiarias);
            }
        }
    }

    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) {
        this.listaCaminhoesGrandesDisponiveis = lista;
        if (lista != null) {
            this.totalCaminhoesGrandesCriados = lista.tamanho();
        } else {
            this.totalCaminhoesGrandesCriados = 0;
            this.listaCaminhoesGrandesDisponiveis = new Lista<>();
        }
    }

    public void setListaZonas(Lista<ZonaUrbana> lista) {
        this.listaZonas = lista;
        if (this.mapaUrbano != null && this.listaZonas != null && !this.listaZonas.estaVazia()) {
            this.distribuicaoCaminhoes = new DistribuicaoCaminhoes(this.mapaUrbano, this.listaZonas);
            // Aplicar configurações ao novo distribuidor
            this.distribuicaoCaminhoes.setGarantirDistribuicaoMinima(this.garantirDistribuicaoMinima);
            this.distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(this.caminhoesPorZonaMinimo);
            if(this.garagemCentral != null) { // Se garagem já existe, atualiza seu distribuidor
                this.garagemCentral.setDistribuidor(this.distribuicaoCaminhoes);
            }
        } else {
            this.distribuicaoCaminhoes = null; // Se não há zonas, não há distribuidor
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
        if (listaCaminhoesGrandesDisponiveis != null) {
            for (int i = 0; i < listaCaminhoesGrandesDisponiveis.tamanho(); i++) {
                listaCaminhoesGrandesDisponiveis.obter(i).setToleranciaEspera(tolerancia);
            }
        }
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

    public void setUsarGaragemCentral(boolean usarGaragemCentral) {
        this.usarGaragemCentral = usarGaragemCentral;
    }

    public void setGarantirDistribuicaoMinima(boolean garantirDistribuicaoMinima) {
        this.garantirDistribuicaoMinima = garantirDistribuicaoMinima;
        if (distribuicaoCaminhoes != null) {
            distribuicaoCaminhoes.setGarantirDistribuicaoMinima(garantirDistribuicaoMinima);
        }
    }

    public void setCaminhoesPorZonaMinimo(int caminhoesPorZonaMinimo) {
        if (caminhoesPorZonaMinimo < 1) {
            throw new IllegalArgumentException("O número mínimo de caminhões por zona deve ser pelo menos 1");
        }
        this.caminhoesPorZonaMinimo = caminhoesPorZonaMinimo;
        if (distribuicaoCaminhoes != null) {
            distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(caminhoesPorZonaMinimo);
        }
    }

    public void setLimiteViagensDiarias(int limiteViagensDiarias) {
        if (limiteViagensDiarias < 1) {
            throw new IllegalArgumentException("O limite de viagens diárias deve ser pelo menos 1");
        }
        this.limiteViagensDiarias = limiteViagensDiarias;
        if (garagemCentral != null) {
            garagemCentral.setLimiteViagensDiarias(limiteViagensDiarias);
        }
        // Atualiza para todos os caminhões existentes também, caso a garagem não os gerencie diretamente
        // ou para garantir consistência se a config da garagem mudar em tempo de execução.
        if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                todosOsCaminhoesPequenos.obter(i).setLimiteViagensDiarias(limiteViagensDiarias);
            }
        }
    }

    public void iniciar() {
        if (timer != null) {
            System.out.println("Simulação já iniciada. Use 'encerrar' primeiro para reiniciar.");
            return;
        }
        if (listaZonas == null || listaZonas.estaVazia() ||
                listaEstacoes == null || listaEstacoes.estaVazia() ||
                todosOsCaminhoesPequenos == null || todosOsCaminhoesPequenos.estaVazia()) {
            String erroMsg = "ERRO: Simulação não pode iniciar. Verifique se há:\n" +
                    " - Zonas configuradas: " + (listaZonas != null && !listaZonas.estaVazia()) + "\n" +
                    " - Estações configuradas: " + (listaEstacoes != null && !listaEstacoes.estaVazia()) + "\n" +
                    " - Caminhões Pequenos configurados: " + (todosOsCaminhoesPequenos != null && !todosOsCaminhoesPequenos.estaVazia());
            System.err.println(erroMsg);
            throw new IllegalStateException("Configuração incompleta para iniciar a simulação. Detalhes:\n" + erroMsg);
        }


        System.out.println("Iniciando Simulação...");
        pausado = false;
        tempoSimulado = 0;
        if (random == null) random = new Random();

        // Reinicializa o estado para uma nova simulação
        caminhoesGrandesEmUso = 0;
        estatisticas.resetar(); // Limpa estatísticas anteriores
        // Registrar o número inicial de caminhões grandes na frota (disponíveis + em uso se houvesse)
        // totalCaminhoesGrandesCriados já foi setado por setListaCaminhoesGrandes
        estatisticas.registrarTotalInicialCaminhoesGrandes(this.totalCaminhoesGrandesCriados);


        // Inicializa garagem central se estiver habilitada e não existir ainda
        // Ou reconfigura se já existir, para garantir que use o distribuidor correto
        if (this.usarGaragemCentral) {
            if(this.garagemCentral == null) inicializarGaragemCentral();
            else { // Garagem já existe, apenas atualiza o distribuidor e limite de viagens
                if(this.distribuicaoCaminhoes != null) this.garagemCentral.setDistribuidor(this.distribuicaoCaminhoes);
                this.garagemCentral.setLimiteViagensDiarias(this.limiteViagensDiarias);
            }
        }


        reiniciarViagensDiariasTodosCaminhoesPequenos();
        distribuirCaminhoesOciososParaColeta();

        timer = new Timer(true); // Daemon thread para não impedir o fechamento da aplicação
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) {
                    try {
                        int diaAnteriorSimulado = tempoSimulado / MINUTOS_EM_UM_DIA;
                        tempoSimulado = (tempoSimulado + 1);
                        int diaAtualSimulado = tempoSimulado / MINUTOS_EM_UM_DIA;

                        if (diaAtualSimulado > diaAnteriorSimulado) {
                            System.out.printf("\n--- TRANSIÇÃO PARA O DIA %d (Tempo Simulado Total: %d min) ---\n", diaAtualSimulado + 1, tempoSimulado);
                            reiniciarViagensDiariasTodosCaminhoesPequenos();
                            distribuirCaminhoesOciososParaColeta(); // Redistribui no início de cada dia
                        }
                        atualizarSimulacao();
                    } catch (Exception e) {
                        System.err.println("ERRO CRÍTICO NO LOOP DA SIMULAÇÃO! Tempo: " + formatarTempo(tempoSimulado));
                        e.printStackTrace();
                        pausar(); // Pausa para evitar erros contínuos
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
        pausado = true; // Garante que não haja mais atualizações
        // Gerar e salvar relatório textual ao encerrar
        try {
            String diretorioRelatorios = "Relatorios"; // Pasta para salvar os relatórios
            String nomeBaseArquivo = "relatorio_simulacao_" + System.currentTimeMillis() + ".txt";
            Path caminhoDiretorio = Paths.get(diretorioRelatorios);
            Path caminhoArquivo = Paths.get(diretorioRelatorios, nomeBaseArquivo);
            String nomeArquivoFinal = caminhoArquivo.toString();

            if (!Files.exists(caminhoDiretorio)) {
                try {
                    Files.createDirectories(caminhoDiretorio);
                    System.out.println("Diretório '" + diretorioRelatorios + "' criado com sucesso.");
                } catch (IOException e) {
                    System.err.println("Falha ao criar o diretório '" + diretorioRelatorios + "': " + e.getMessage());
                    System.err.println("O relatório será salvo no diretório atual da aplicação.");
                    nomeArquivoFinal = nomeBaseArquivo; // Fallback para o diretório atual
                } catch (SecurityException se) {
                    System.err.println("Erro de segurança ao tentar criar o diretório '" + diretorioRelatorios + "': " + se.getMessage());
                    System.err.println("O relatório será salvo no diretório atual da aplicação.");
                    nomeArquivoFinal = nomeBaseArquivo;
                }
            }

            System.out.println("\n" + estatisticas.gerarRelatorio()); // Imprime no console (que é a JTextArea na GUI)
            estatisticas.salvarRelatorio(nomeArquivoFinal); // Salva em arquivo no diretório

        } catch (IOException e) {
            System.err.println("Erro ao salvar relatório final: " + e.getMessage());
        } catch (SecurityException se) {
            System.err.println("Erro de segurança ao salvar relatório: " + se.getMessage());
            try { // Fallback para salvar no diretório atual em caso de erro de segurança
                String nomeBaseArquivoFallback = "relatorio_simulacao_" + System.currentTimeMillis() + ".txt";
                System.out.println("\n" + estatisticas.gerarRelatorio());
                estatisticas.salvarRelatorio(nomeBaseArquivoFallback);
                System.out.println("Relatório salvo no diretório atual após falha de segurança.");
            } catch (IOException ioe) {
                System.err.println("Erro ao salvar relatório no diretório atual após falha de segurança: " + ioe.getMessage());
            }
        }
        System.out.println("Simulação encerrada.");
    }


    private void distribuirCaminhoesOciososParaColeta() {
        if (listaZonas == null || listaZonas.estaVazia()) {
            //System.out.println("INFO: Nenhuma zona para distribuir caminhões.");
            return;
        }

        int distribuidos = 0;

        if (usarGaragemCentral && garagemCentral != null) {
            if (distribuicaoCaminhoes == null && this.listaZonas != null && !this.listaZonas.estaVazia()) {
                // Garante que o distribuidor existe se as zonas foram setadas depois da garagem
                this.distribuicaoCaminhoes = new DistribuicaoCaminhoes(this.mapaUrbano, this.listaZonas);
                this.distribuicaoCaminhoes.setGarantirDistribuicaoMinima(this.garantirDistribuicaoMinima);
                this.distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(this.caminhoesPorZonaMinimo);
                this.garagemCentral.setDistribuidor(this.distribuicaoCaminhoes);
            }
            distribuidos = garagemCentral.distribuirCaminhoesParaZonas(listaZonas, distribuicaoCaminhoes);
        }
        else if (distribuicaoCaminhoes != null) {
            distribuidos = distribuicaoCaminhoes.distribuirCaminhoes(todosOsCaminhoesPequenos, listaZonas);
        } else { // Fallback para distribuição aleatória se tudo mais falhar
            if (todosOsCaminhoesPequenos != null && !todosOsCaminhoesPequenos.estaVazia()) {
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
        }

        if (distribuidos > 0) {
            String metodo = usarGaragemCentral && garagemCentral != null ? "garagem central" :
                    (distribuicaoCaminhoes != null ? "distribuição inteligente" : "distribuição aleatória");
            System.out.println(distribuidos + " caminhões pequenos ociosos enviados para coleta usando " + metodo);
        } else if (todosOsCaminhoesPequenos != null && todosOsCaminhoesPequenos.tamanho() > 0) {
            //System.out.println("INFO: Nenhum caminhão ocioso para distribuir ou nenhuma zona precisando.");
        }
    }

    public void gravar(String caminhoArquivo) throws IOException {
        boolean estavaPausado = this.pausado;
        if (!estavaPausado && timer != null) pausar(); // Pausa antes de salvar se estava rodando

        System.out.println("Salvando estado da simulação em " + caminhoArquivo + "...");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(this);
            System.out.println("Simulação salva com sucesso.");
        } finally {
            // Retoma apenas se estava rodando e o timer ainda existe (não foi encerrado)
            if (!estavaPausado && timer != null) {
                continuarSimulacao();
            }
        }
    }

    public static Simulador carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        System.out.println("Carregando estado da simulação de " + caminhoArquivo + "...");
        Simulador sim = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo))) {
            sim = (Simulador) ois.readObject();
            // Recriar componentes transient
            sim.timer = null; // Timer será recriado se iniciar
            sim.pausado = true; // Simulação carregada fica pausada
            sim.random = new Random();
            // Garante que listas não sejam nulas pós-desserialização (caso de versões antigas)
            if (sim.todosOsCaminhoesPequenos == null) sim.todosOsCaminhoesPequenos = new Lista<>();
            if (sim.listaCaminhoesGrandesDisponiveis == null) sim.listaCaminhoesGrandesDisponiveis = new Lista<>();
            if (sim.listaZonas == null) sim.listaZonas = new Lista<>();
            if (sim.listaEstacoes == null) sim.listaEstacoes = new Lista<>();
            if (sim.estatisticas == null) sim.estatisticas = new Estatisticas();
            if (sim.mapaUrbano == null) sim.mapaUrbano = new MapaUrbano(); // Recria se nulo
            // Recria o distribuidor com base nas zonas carregadas
            if (sim.listaZonas != null && !sim.listaZonas.estaVazia()) {
                sim.distribuicaoCaminhoes = new DistribuicaoCaminhoes(sim.mapaUrbano, sim.listaZonas);
                sim.distribuicaoCaminhoes.setGarantirDistribuicaoMinima(sim.garantirDistribuicaoMinima);
                sim.distribuicaoCaminhoes.setCaminhoesPorZonaMinimo(sim.caminhoesPorZonaMinimo);
            }
            // Recria garagem se usarGaragemCentral é true
            if (sim.usarGaragemCentral) {
                sim.garagemCentral = new GaragemCentral("Garagem Central (Carregada)", sim.mapaUrbano);
                if(sim.distribuicaoCaminhoes != null) sim.garagemCentral.setDistribuidor(sim.distribuicaoCaminhoes);
                sim.garagemCentral.setLimiteViagensDiarias(sim.limiteViagensDiarias);
                // Adicionar caminhões ociosos à garagem recriada
                if (sim.todosOsCaminhoesPequenos != null) {
                    for (int i = 0; i < sim.todosOsCaminhoesPequenos.tamanho(); i++) {
                        CaminhaoPequeno cp = sim.todosOsCaminhoesPequenos.obter(i);
                        if (cp.getStatus() == StatusCaminhao.OCIOSO) {
                            sim.garagemCentral.adicionarCaminhao(cp);
                        }
                    }
                }
            }

            System.out.println("Simulação carregada. Está pausada. Tempo simulado: " + sim.formatarTempo(sim.tempoSimulado));
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
        int tempoNoDia = tempoSimulado % MINUTOS_EM_UM_DIA;
        if (isHorarioDePico(tempoNoDia)) {
            min = MIN_VIAGEM_PICO_MIN; max = MAX_VIAGEM_PICO_MIN;
        } else {
            min = MIN_VIAGEM_FORA_PICO_MIN; max = MAX_VIAGEM_FORA_PICO_MIN;
        }
        if (min >= max) return min;
        return random.nextInt(max - min + 1) + min;
    }

    public void adicionarCaminhaoGrande() {
        System.out.println("INFO: Adicionando um novo caminhão grande...");
        CaminhaoGrande novoCaminhao = new CaminhaoGrandePadrao(this.toleranciaCaminhoesGrandes);
        if (listaCaminhoesGrandesDisponiveis == null) listaCaminhoesGrandesDisponiveis = new Lista<>();
        listaCaminhoesGrandesDisponiveis.adicionar(novoCaminhao);
        totalCaminhoesGrandesCriados++;
        estatisticas.registrarNovoCaminhaoGrande();
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
                distribuicaoCaminhoes.incrementarTempoSemColeta(60); // Incrementa 60 minutos (1 hora)
            }
        }

        processarTodosCaminhoesPequenos();
        processarEstacoes();

        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);
        if (tempoNoDia % intervaloEstatisticas == 0 && this.tempoSimulado != 0) { // Evita relatório no tempo 0
            imprimirRelatorioHorario(); // Imprime relatório textual no console (log da GUI)
        }
    }

    private void reiniciarViagensDiariasTodosCaminhoesPequenos() {
        if (usarGaragemCentral && garagemCentral != null) {
            garagemCentral.reiniciarViagensDiarias();
        }
        // Mesmo se usar garagem, percorre todos para garantir que qualquer caminhão fora da garagem também seja resetado
        if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                // A garagem já deve ter tratado os seus. Aqui trata os que podem estar em zonas/estações.
                // O método reiniciarViagensDiarias do caminhão já verifica se estava INATIVO_LIMITE_VIAGENS.
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
                case VIAJANDO_ESTACAO:
                case RETORNANDO_ZONA:
                    processarCaminhaoEmViagem(caminhao);
                    break;
                case NA_FILA:
                    // O caminhão está na fila da estação. A estação o processará.
                    break;
                case DESCARREGANDO:
                    // Este status é gerenciado pela Estacao.
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
            caminhao.setStatus(StatusCaminhao.OCIOSO);
            return;
        }

        if (!caminhao.estaCheio()) {
            int lixoDisponivel = zona.getLixoAcumulado();
            if (lixoDisponivel > 0) {
                int lixoColetado = caminhao.coletar(lixoDisponivel); // coletar retorna o quanto coletou
                if (lixoColetado > 0) {
                    zona.coletarLixo(lixoColetado); // Remove da zona o que foi efetivamente coletado pelo caminhão
                    estatisticas.registrarColetaLixo(zona.getNome(), lixoColetado);

                    if (distribuicaoCaminhoes != null) {
                        distribuicaoCaminhoes.registrarColetaEmZona(zona.getNome());
                    }
                }
            }
        }

        if (caminhao.estaCheio()) {
            if (listaEstacoes == null || listaEstacoes.estaVazia()) {
                System.err.println("AVISO SIM: CP " + caminhao.getPlaca() + " está cheio, mas não há estações para descarregar!");
                return;
            }

            EstacaoTransferencia melhorEstacao = null;

            if (listaEstacoes.tamanho() >= 2) {
                // Temos pelo menos duas estações, vamos alternar entre elas
                int indiceEstacao = proximaEstacaoEhA ? 0 : 1;
                melhorEstacao = listaEstacoes.obter(indiceEstacao);
                proximaEstacaoEhA = !proximaEstacaoEhA; // Inverte para a próxima vez
            } else {
                // Se só tiver uma estação, usa ela
                melhorEstacao = listaEstacoes.obter(0);
            }

            if (melhorEstacao == null) { // Se não encontrou nenhuma estação (caso inesperado)
                System.err.println("ERRO SIM: Nenhuma estação disponível para CP " + caminhao.getPlaca() + " descarregar.");
                return;
            }

            String zonaOrigemNome = (caminhao.getZonaDeOrigemParaRetorno() != null) ? caminhao.getZonaDeOrigemParaRetorno().getNome() : "OrigemDesconhecida";
            int tempoViagemCalc = calcularTempoViagemPequeno();
            int tamanhoFila = (melhorEstacao instanceof EstacaoPadrao) ? ((EstacaoPadrao) melhorEstacao).getCaminhoesNaFila() : 0;

            System.out.printf("ENVIO EST: CP %s (%dkg) de Zona %s -> Est. %s (fila: %d). Viagem: %d min.%n",
                    caminhao.getPlaca(), caminhao.getCargaAtual(), zonaOrigemNome,
                    melhorEstacao.getNome(), tamanhoFila, tempoViagemCalc);

            caminhao.definirDestino(melhorEstacao); // Seta status para VIAJANDO_ESTACAO
            caminhao.definirTempoViagem(tempoViagemCalc);
            if (!caminhao.registrarViagem()) { // Se false, caminhão ficou INATIVO_LIMITE_VIAGENS
                System.out.println("INFO SIM: CP " + caminhao.getPlaca() + " tornou-se INATIVO ao registrar viagem para estação " + melhorEstacao.getNome());
                // Se usar garagem, o caminhão inativo poderia ser "recolhido" ou marcado para retornar
                if (usarGaragemCentral && garagemCentral != null && caminhao.getZonaAtual() != null) {
                    // garagemCentral.retornarCaminhaoDeZona(caminhao, caminhao.getZonaAtual());
                    // No entanto, ele está VIAJANDO, então não está "em uma zona" para ser retornado dela.
                    // A garagem o pegará quando ele voltar a estar OCIOSO após o dia.
                }
            }
        }
    }

    private void processarCaminhaoEmViagem(CaminhaoPequeno caminhao) {
        if (caminhao.processarViagem()) { // Viagem concluída
            StatusCaminhao statusAtualCaminhao = caminhao.getStatus();

            if (statusAtualCaminhao == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                System.out.println("INFO SIM: CP INATIVO " + caminhao.getPlaca() + " completou sua última viagem programada e permanece INATIVO.");
                return;
            }

            if (caminhao.getEstacaoDestino() != null && statusAtualCaminhao == StatusCaminhao.VIAJANDO_ESTACAO) {
                EstacaoTransferencia estacaoDest = caminhao.getEstacaoDestino();
                System.out.printf("CHEGADA EST: CP %s chegou à Estação %s.%n", caminhao.getPlaca(), estacaoDest.getNome());
                if (estacaoDest instanceof EstacaoPadrao) {
                    ((EstacaoPadrao) estacaoDest).receberCaminhaoPequeno(caminhao, this.tempoSimulado % MINUTOS_EM_UM_DIA); // Passa tempo para cálculo de espera
                    caminhao.setStatus(StatusCaminhao.NA_FILA);
                    estatisticas.registrarChegadaEstacao(estacaoDest.getNome());
                } else {
                    System.err.println("ERRO SIM: CP " + caminhao.getPlaca() + " chegou a est. não padrão " + estacaoDest.getNome() + ". OCIOSO.");
                    caminhao.setStatus(StatusCaminhao.OCIOSO);
                }
            }
            else if (caminhao.getZonaAtual() != null && statusAtualCaminhao == StatusCaminhao.RETORNANDO_ZONA) {
                ZonaUrbana zonaDest = caminhao.getZonaAtual();
                System.out.printf("RETORNO ZONA: CP %s retornou à Zona %s para coletar.%n", caminhao.getPlaca(), zonaDest.getNome());
                caminhao.setStatus(StatusCaminhao.COLETANDO); // Volta a coletar
            } else {
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
                // Processa a fila de caminhões pequenos na estação
                ResultadoProcessamentoFila resultado = estacaoPadrao.processarFila(this.tempoSimulado % MINUTOS_EM_UM_DIA); // Passa o tempo atual para cálculo de espera
                CaminhaoPequeno caminhaoProcessado = resultado.getCaminhaoProcessado();
                long tempoEsperaReal = resultado.getTempoDeEspera();

                if (caminhaoProcessado != null) { // Um caminhão foi descarregado
                    estatisticas.registrarAtendimentoCaminhaoPequeno(estacaoPadrao.getNome(), tempoEsperaReal);
                    ZonaUrbana zonaDeRetornoOriginal = caminhaoProcessado.getZonaDeOrigemParaRetorno();

                    if (caminhaoProcessado.getStatus() == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        System.out.println("INFO SIM: CP " + caminhaoProcessado.getPlaca() + " descarregou, mas já está INATIVO_LIMITE_VIAGENS.");
                        if (usarGaragemCentral && garagemCentral != null && zonaDeRetornoOriginal != null) {
                        }
                    } else if (zonaDeRetornoOriginal != null) {
                        caminhaoProcessado.setStatus(StatusCaminhao.RETORNANDO_ZONA);
                        caminhaoProcessado.definirDestino(zonaDeRetornoOriginal); // Define zonaAtual para retorno
                        caminhaoProcessado.definirTempoViagem(calcularTempoViagemPequeno());
                        System.out.printf("RETORNO PREP: CP %s de Est. %s -> Zona %s. Viagem: %d min.%n",
                                caminhaoProcessado.getPlaca(), estacaoPadrao.getNome(), zonaDeRetornoOriginal.getNome(), caminhaoProcessado.getTempoRestanteViagem());
                    } else {
                        System.err.println("AVISO CRÍTICO SIM: CP " + caminhaoProcessado.getPlaca() + " descarregou SEM zona de origem para retorno! Tornando OCIOSO.");
                        caminhaoProcessado.setStatus(StatusCaminhao.OCIOSO);
                        if (usarGaragemCentral && garagemCentral != null) { // Se ocioso, pode ir para garagem
                            garagemCentral.adicionarCaminhao(caminhaoProcessado);
                        }
                    }
                }

                CaminhaoGrande cgQuePartiu = estacaoPadrao.gerenciarTempoEsperaCaminhaoGrande();
                if (cgQuePartiu != null) {
                    int cargaTransportada = cgQuePartiu.getCargaAtual(); // Pega a carga ANTES de descarregar
                    cgQuePartiu.descarregar(); // Zera a carga do caminhão grande que partiu

                    estatisticas.registrarTransporteLixoCGPorEstacao(estacaoPadrao.getNome(), cargaTransportada);
                    estatisticas.registrarTransporteLixo(cargaTransportada);


                    if (listaCaminhoesGrandesDisponiveis == null) listaCaminhoesGrandesDisponiveis = new Lista<>();
                    listaCaminhoesGrandesDisponiveis.adicionar(cgQuePartiu); // Adiciona de volta à lista de disponíveis
                    caminhoesGrandesEmUso--;
                    System.out.println("INFO SIM: CG ("+cgQuePartiu+") retornou do aterro e está disponível. CGs Disp: " + listaCaminhoesGrandesDisponiveis.tamanho() + ", Em Uso: " + caminhoesGrandesEmUso);
                }

                // Verificar necessidade de alocar ou adicionar novo CG
                if (estacaoPadrao.precisaCaminhaoGrande()) { // Se a estação não tem CG e tem CPs na fila
                    atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                }
                // Adiciona novo CG à frota se o tempo de espera de CPs for muito alto
                if (estacaoPadrao.tempoEsperaExcedido()) {
                    System.out.println("ALERTA EST SIM: Tempo de espera de CPs excedido na Est. " + estacaoPadrao.getNome() + "! Adicionando novo CG à frota global.");
                    adicionarCaminhaoGrande(); // Adiciona à frota geral
                    if (estacaoPadrao.precisaCaminhaoGrande()) { // Re-verifica se AINDA precisa após adição
                        atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                    }
                }
            }
        }
    }


    private void atribuirCaminhaoGrandeParaEstacao(EstacaoPadrao estacao) {
        if (estacao == null || !estacao.precisaCaminhaoGrande()) return; // Já tem CG ou não precisa
        if (listaCaminhoesGrandesDisponiveis == null || listaCaminhoesGrandesDisponiveis.estaVazia()) {
            //System.out.println("INFO SIM: Est. " + estacao.getNome() + " precisa de CG, mas NENHUM está disponível no momento.");
            return;
        }

        CaminhaoGrande caminhao = listaCaminhoesGrandesDisponiveis.remover(0); // Pega o primeiro disponível
        if (estacao.atribuirCaminhaoGrande(caminhao)) {
            caminhoesGrandesEmUso++;
            System.out.printf("ALOCAÇÃO CG SIM: CG (%s) alocado para Est. %s. Em uso: %d. Disp: %d%n",
                    caminhao.toString(), estacao.getNome(), caminhoesGrandesEmUso, listaCaminhoesGrandesDisponiveis.tamanho());
        } else {
            // Isso não deveria acontecer se estacao.precisaCaminhaoGrande() retornou true
            System.err.println("ERRO SIM: Falha ao atribuir CG ("+caminhao+") à est. " + estacao.getNome() + " (talvez já tenha um inesperadamente?). Devolvendo à lista de disponíveis.");
            listaCaminhoesGrandesDisponiveis.adicionar(caminhao); // Adiciona de volta
        }
    }

    private void imprimirRelatorioHorario() {
        // Este método imprime no console (que é redirecionado para a área de log da GUI)
        System.out.println("\n========================================================================");
        int tempoNoDia = this.tempoSimulado % MINUTOS_EM_UM_DIA;
        System.out.printf("=== RELATÓRIO DE STATUS === Tempo: %s %s ===%n", formatarTempo(this.tempoSimulado), isHorarioDePico(tempoNoDia) ? "(PICO)" : "");
        System.out.println("------------------------------------------------------------------------");

        int cpColetando = 0, cpEmViagem = 0, cpNaFila = 0, cpOcioso = 0, cpInativoLimite = 0, cpDescarregando = 0;
        if (todosOsCaminhoesPequenos != null) {
            for (int i = 0; i < todosOsCaminhoesPequenos.tamanho(); i++) {
                CaminhaoPequeno cp = todosOsCaminhoesPequenos.obter(i);
                switch (cp.getStatus()) {
                    case COLETANDO: cpColetando++; break;
                    case VIAJANDO_ESTACAO: case RETORNANDO_ZONA: cpEmViagem++; break;
                    case NA_FILA: cpNaFila++; break;
                    case DESCARREGANDO: cpDescarregando++; break;
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
                if (distribuicaoCaminhoes != null) {
                    double score = distribuicaoCaminhoes.getScoreZona(zona.getNome()); // Usa o score final para display
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
    public int getTotalCaminhoesGrandes() { return totalCaminhoesGrandesCriados; } // Total que já existiu na frota
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