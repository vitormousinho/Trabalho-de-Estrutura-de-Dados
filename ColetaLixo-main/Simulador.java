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
    private Lista<CaminhaoPequeno> listaCaminhoesPequenosOciosos; // Renomeado para clareza
    private Lista<CaminhaoGrande> listaCaminhoesGrandesDisponiveis; // Renomeado para clareza
    private Lista<ZonaUrbana> listaZonas;
    private Lista<EstacaoTransferencia> listaEstacoes;

    // Listas para acompanhar caminhões ativos
    private Lista<CaminhaoPequeno> caminhoesEmViagem = new Lista<>();
    private Lista<CaminhaoPequeno> caminhoesEmColeta = new Lista<>();
    // Não precisamos mais da lista original de pequenos, pois eles são movidos para coleta/ociosos

    // Contadores para caminhões grandes
    private int totalCaminhoesGrandesCriados = 0; // Conta todos os que já existiram
    private int caminhoesGrandesEmUso = 0; // Quantos estão nas estações

    // Parâmetros configuráveis
    private int toleranciaCaminhoesGrandes = CaminhaoGrandePadrao.TOLERANCIA_ESPERA_PADRAO_MINUTOS; // Usa constante
    private int intervaloEstatisticas = INTERVALO_RELATORIO_MIN; // Usa constante


    /**
     * Construtor do Simulador.
     * Inicializa as listas e o gerador aleatório.
     */
    public Simulador() {
        this.listaCaminhoesPequenosOciosos = new Lista<>();
        this.listaCaminhoesGrandesDisponiveis = new Lista<>();
        this.listaZonas = new Lista<>();
        this.listaEstacoes = new Lista<>();
        this.random = new Random(); // Inicializa o gerador
    }

    // --- Métodos Setters para configuração inicial ---
    /** Define a lista inicial de caminhões pequenos (todos começam ociosos). */
    public void setListaCaminhoesPequenos(Lista<CaminhaoPequeno> lista) { this.listaCaminhoesPequenosOciosos = lista; }
    /** Define a lista inicial de caminhões grandes (todos começam disponíveis). */
    public void setListaCaminhoesGrandes(Lista<CaminhaoGrande> lista) {
        this.listaCaminhoesGrandesDisponiveis = lista;
        this.totalCaminhoesGrandesCriados = lista.tamanho(); // Inicializa contador
    }
    /** Define a lista de zonas urbanas. */
    public void setListaZonas(Lista<ZonaUrbana> lista) { this.listaZonas = lista; }
    /** Define a lista de estações de transferência. */
    public void setListaEstacoes(Lista<EstacaoTransferencia> lista) { this.listaEstacoes = lista; }

    /** Define a tolerância de espera padrão para novos caminhões grandes e atualiza os existentes. */
    public void setToleranciaCaminhoesGrandes(int tolerancia) {
        if (tolerancia < 1) {
            throw new IllegalArgumentException("A tolerância deve ser pelo menos 1 minuto.");
        }
        this.toleranciaCaminhoesGrandes = tolerancia;

        // Atualiza a tolerância em todos os caminhões grandes DISPONÍVEIS
        for (int i = 0; i < listaCaminhoesGrandesDisponiveis.tamanho(); i++) {
            CaminhaoGrande caminhao = listaCaminhoesGrandesDisponiveis.obter(i);
            caminhao.setToleranciaEspera(tolerancia);
        }
        // Nota: Caminhões grandes que já estão em uso nas estações NÃO terão sua tolerância
        // atualizada por este método para evitar inconsistências na espera atual.
        // Se for necessário atualizar todos, seria preciso iterar nas estações também.
    }

    /** Define o intervalo (em minutos simulados) para impressão do relatório horário. */
    public void setIntervaloRelatorio(int intervalo) {
        if (intervalo < 1) {
            throw new IllegalArgumentException("O intervalo do relatório deve ser pelo menos 1 minuto.");
        }
        this.intervaloEstatisticas = intervalo;
    }

    // --- Métodos de Controle da Simulação ---

    /**
     * Inicia a execução da simulação.
     * Configura o estado inicial, distribui caminhões e agenda a tarefa de atualização.
     */
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
        tempoSimulado = 0; // Reinicia o tempo

        // Garante que o Random existe (caso tenha sido carregado de um save)
        if (random == null) {
            random = new Random();
        }

        // Reinicializa listas de estado e contadores
        caminhoesEmViagem = new Lista<>();
        caminhoesEmColeta = new Lista<>();
        caminhoesGrandesEmUso = 0;
        // totalCaminhoesGrandesCriados é mantido se for um reinício, ou zerado se desejado. Vamos zerar estatísticas.
        estatisticas.resetar();
        estatisticas.registrarTotalInicialCaminhoesGrandes(this.listaCaminhoesGrandesDisponiveis.tamanho());

        // Prepara caminhões pequenos: move todos para coleta inicial
        distribuirCaminhoesPorZonas();

        // Iniciar timer para atualização periódica
        timer = new Timer(true); // true = daemon thread (não impede JVM de fechar)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!pausado) {
                    try {
                        tempoSimulado = (tempoSimulado + 1) % MINUTOS_EM_UM_DIA; // Avança 1 minuto e faz ciclo diário
                        atualizarSimulacao();
                    } catch (Exception e) {
                        System.err.println("ERRO CRÍTICO NO LOOP DA SIMULAÇÃO! Tempo: " + tempoSimulado);
                        e.printStackTrace();
                        pausar(); // Pausa para evitar mais erros
                    }
                }
            }
        }, 0, INTERVALO_TIMER_MS); // Atualiza a cada X ms reais

        System.out.println("Simulação iniciada. Tempo real de atualização: " + INTERVALO_TIMER_MS + "ms.");
    }

    /**
     * Distribui os caminhões pequenos ociosos pelas zonas para iniciar a coleta.
     * Move os caminhões da lista de ociosos para a lista de coleta.
     */
    private void distribuirCaminhoesPorZonas() {
        if (listaZonas.estaVazia()) { // Verificação de robustez
            System.err.println("AVISO: Não há zonas para distribuir caminhões.");
            return;
        }
        if (listaCaminhoesPequenosOciosos.estaVazia()) {
            System.out.println("INFO: Nenhum caminhão pequeno ocioso para distribuir.");
            return;
        }

        System.out.println("Distribuindo " + listaCaminhoesPequenosOciosos.tamanho() + " caminhões pequenos pelas zonas...");

        // Itera pela lista de ociosos e move para coleta
        while (!listaCaminhoesPequenosOciosos.estaVazia()) {
            CaminhaoPequeno caminhao = listaCaminhoesPequenosOciosos.remover(0); // Remove o primeiro ocioso

            // Escolhe uma zona aleatória para iniciar
            int idxZona = random.nextInt(listaZonas.tamanho());
            ZonaUrbana zonaDestino = listaZonas.obter(idxZona);

            // Define estado e destino
            caminhao.setStatus(StatusCaminhao.COLETANDO);
            caminhao.definirDestino(zonaDestino); // Define a zona atual

            // Adiciona à lista de coleta
            caminhoesEmColeta.adicionar(caminhao);
            // System.out.println(" -> Caminhão " + caminhao.getPlaca() + " iniciando coleta na zona " + zonaDestino.getNome());
        }
        System.out.println("Distribuição inicial concluída. " + caminhoesEmColeta.tamanho() + " caminhões em coleta.");
    }

    /** Pausa a execução da simulação. */
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

    /** Continua a execução de uma simulação pausada. */
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

    /** Encerra a simulação, cancela o timer e gera o relatório final. */
    public void encerrar() {
        System.out.println("Encerrando simulação...");
        if (timer != null) {
            timer.cancel();
            timer = null; // Permite reiniciar depois
        }
        pausado = true; // Garante que não haja mais atualizações

        // Gerar relatório final
        try {
            String nomeArquivo = "relatorio_simulacao_" + System.currentTimeMillis() + ".txt";
            System.out.println("\n" + estatisticas.gerarRelatorio()); // Imprime no console
            estatisticas.salvarRelatorio(nomeArquivo);
            System.out.println("Relatório final salvo em " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar relatório final: " + e.getMessage());
        }
        System.out.println("Simulação encerrada.");
    }

    // --- Métodos de Persistência (Salvar/Carregar) ---

    /** Salva o estado atual do simulador em um arquivo. */
    public void gravar(String caminhoArquivo) throws IOException {
        // Pausa antes de salvar para garantir estado consistente
        boolean estavaPausado = this.pausado;
        pausar();

        System.out.println("Salvando estado da simulação em " + caminhoArquivo + "...");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(this); // Salva o objeto Simulador atual
            System.out.println("Simulação salva com sucesso.");
        } finally {
            // Retoma o estado anterior da pausa
            if (!estavaPausado) {
                continuarSimulacao();
            }
        }
    }

    /** Carrega o estado do simulador de um arquivo. */
    public static Simulador carregar(String caminhoArquivo) throws IOException, ClassNotFoundException {
        System.out.println("Carregando estado da simulação de " + caminhoArquivo + "...");
        Simulador sim = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo))) {
            sim = (Simulador) ois.readObject();

            // Re-inicializa atributos transient
            sim.timer = null; // Timer não é salvo, precisa ser recriado
            sim.pausado = true; // Carrega sempre pausado
            sim.random = new Random(); // Recria o gerador aleatório

            System.out.println("Simulação carregada. Está pausada. Use 'continuar' ou 'iniciar' (reinicia o tempo).");
        }
        if (sim == null) {
            throw new IOException("Falha ao carregar o objeto Simulador, objeto nulo após deserialização.");
        }
        // Garante que as listas transientes sejam inicializadas se forem nulas após a carga
        if (sim.caminhoesEmViagem == null) sim.caminhoesEmViagem = new Lista<>();
        if (sim.caminhoesEmColeta == null) sim.caminhoesEmColeta = new Lista<>();
        if (sim.listaCaminhoesPequenosOciosos == null) sim.listaCaminhoesPequenosOciosos = new Lista<>();
        if (sim.listaCaminhoesGrandesDisponiveis == null) sim.listaCaminhoesGrandesDisponiveis = new Lista<>();

        return sim;
    }

    // --- Métodos Auxiliares Internos ---

    /** Verifica se o tempo atual está dentro de um período de pico. */
    private boolean isHorarioDePico(int tempoAtualMinutos) {
        boolean manha = (tempoAtualMinutos >= PICO_MANHA_INICIO_MIN && tempoAtualMinutos <= PICO_MANHA_FIM_MIN);
        boolean tarde = (tempoAtualMinutos >= PICO_TARDE_INICIO_MIN && tempoAtualMinutos <= PICO_TARDE_FIM_MIN);
        return manha || tarde;
    }

    /** Calcula um tempo de viagem aleatório para caminhão pequeno baseado no horário. */
    private int calcularTempoViagemPequeno() {
        int min, max;
        if (isHorarioDePico(this.tempoSimulado)) {
            min = MIN_VIAGEM_PICO_MIN;
            max = MAX_VIAGEM_PICO_MIN;
        } else {
            min = MIN_VIAGEM_FORA_PICO_MIN;
            max = MAX_VIAGEM_FORA_PICO_MIN;
        }
        // Garante min <= max e calcula valor aleatório no intervalo
        if (min >= max) return min;
        // random.nextInt(N) gera de 0 a N-1. Então N = max - min + 1
        return random.nextInt(max - min + 1) + min;
    }

    /** Adiciona um novo caminhão grande à lista de disponíveis. */
    public void adicionarCaminhaoGrande() {
        System.out.println("INFO: Adicionando um novo caminhão grande...");
        CaminhaoGrande novoCaminhao = new CaminhaoGrandePadrao(this.toleranciaCaminhoesGrandes);
        listaCaminhoesGrandesDisponiveis.adicionar(novoCaminhao);
        totalCaminhoesGrandesCriados++;
        estatisticas.registrarNovoCaminhaoGrande(); // Registra na estatística
        System.out.println("Novo caminhão grande adicionado! Total Criados: " + totalCaminhoesGrandesCriados +
                ", Disponíveis: " + listaCaminhoesGrandesDisponiveis.tamanho());
    }

    // --- LÓGICA PRINCIPAL DA SIMULAÇÃO (Chamada a cada passo de tempo) ---

    /** Atualiza o estado de todos os componentes da simulação para o tempo atual. */
    private void atualizarSimulacao() {
        int hora = tempoSimulado / 60;
        int minuto = tempoSimulado % 60;

        // Log de tempo menos verboso (a cada 10 minutos, por exemplo)
        // if (tempoSimulado % 10 == 0) {
        //    System.out.printf("Tempo: %02d:%02d %s%n", hora, minuto, isHorarioDePico(tempoSimulado) ? "(PICO)" : "");
        // }

        // 1. Início do Dia (tempoSimulado == 0)
        if (tempoSimulado == 0) {
            System.out.printf("\n--- INÍCIO DO NOVO DIA (Tempo: %02d:%02d) ---\n", hora, minuto);
            reiniciarViagensDiariasCaminhoes();
        }

        // 2. Geração de Lixo (ocorre uma vez por hora, no início da hora)
        if (minuto == 0) {
            // System.out.println("--- Hora " + hora + ": Gerando lixo nas zonas ---"); // Log opcional
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                int lixoAntes = zona.getLixoAcumulado();
                zona.gerarLixo();
                int geradoNestaHora = zona.getLixoAcumulado() - lixoAntes;
                if (geradoNestaHora > 0) { // Só registra se gerou algo
                    estatisticas.registrarGeracaoLixo(zona.getNome(), geradoNestaHora);
                    // System.out.println("   " + zona.getNome() + ": Gerou " + geradoNestaHora + "kg. Total agora: " + zona.getLixoAcumulado() + "kg."); // Log detalhado opcional
                }
            }
        }

        // 3. Processar Caminhões Pequenos
        processarCaminhoesEmColeta(); // Tentam coletar e decidem viajar para estação
        processarCaminhoesEmViagem(); // Avançam na viagem e chegam aos destinos

        // 4. Processar Estações de Transferência
        processarEstacoes();

        // 5. Atualizar Estatísticas Agregadas
        estatisticas.atualizarMaxCaminhoesGrandesEmUso(caminhoesGrandesEmUso);

        // 6. Relatório Periódico (a cada 'intervaloEstatisticas' minutos)
        if (tempoSimulado % intervaloEstatisticas == 0 && tempoSimulado != 0) {
            imprimirRelatorioHorario(hora, minuto);
        }
    }

    /** Reinicia os contadores de viagens diárias de todos os caminhões pequenos. */
    private void reiniciarViagensDiariasCaminhoes() {
        System.out.println("INFO: Reiniciando contadores de viagens diárias dos caminhões pequenos.");
        int countReiniciados = 0;
        // Reinicia os caminhões em coleta
        for (int i = 0; i < caminhoesEmColeta.tamanho(); i++) {
            caminhoesEmColeta.obter(i).reiniciarViagensDiarias();
            countReiniciados++;
        }
        // Reinicia os caminhões em viagem
        for (int i = 0; i < caminhoesEmViagem.tamanho(); i++) {
            caminhoesEmViagem.obter(i).reiniciarViagensDiarias();
            countReiniciados++;
        }
        // Reinicia os caminhões ociosos (se houver)
        for (int i = 0; i < listaCaminhoesPequenosOciosos.tamanho(); i++) {
            listaCaminhoesPequenosOciosos.obter(i).reiniciarViagensDiarias();
            countReiniciados++;
        }
        System.out.println("INFO: Contadores de " + countReiniciados + " caminhões pequenos reiniciados.");
    }

    /** Processa os caminhões que estão atualmente coletando lixo nas zonas. */
    private void processarCaminhoesEmColeta() {
        if (listaEstacoes.estaVazia() && !caminhoesEmColeta.estaVazia()) {
            // Se não há estações, os caminhões em coleta não podem descarregar quando cheios.
            // Poderíamos imprimir um aviso aqui ou apenas deixar como está (eles ficarão cheios).
            // System.err.println("AVISO: Caminhões em coleta não têm estações para descarregar.");
        }

        // Itera de trás para frente para facilitar remoção
        for (int i = caminhoesEmColeta.tamanho() - 1; i >= 0; i--) {
            CaminhaoPequeno caminhao = caminhoesEmColeta.obter(i);
            ZonaUrbana zona = caminhao.getZonaAtual();

            // Verifica se o caminhão está ativo (não INATIVO_LIMITE_VIAGENS)
            if (caminhao.getStatus() == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                continue; // Pula caminhões inativos
            }

            // Se está na zona e não está cheio, tenta coletar
            if (zona != null && !caminhao.estaCheio()) {
                int lixoDisponivel = zona.getLixoAcumulado();
                if (lixoDisponivel > 0) {
                    // Usa o método coletar que retorna a quantidade efetivamente coletada
                    int lixoRealmenteColetado = caminhao.coletar(lixoDisponivel);

                    if (lixoRealmenteColetado > 0) {
                        // Remove da zona o que foi efetivamente coletado
                        zona.coletarLixo(lixoRealmenteColetado);
                        estatisticas.registrarColetaLixo(zona.getNome(), lixoRealmenteColetado);

                        // Log mais detalhado opcional
                        // System.out.printf("COLETA: Caminhão %s coletou %dkg da zona %s. Carga: %d/%d kg%n",
                        //        caminhao.getPlaca(), lixoRealmenteColetado, zona.getNome(),
                        //        caminhao.getCargaAtual(), caminhao.getCapacidade());
                    }
                }
            } // Fim da tentativa de coleta

            // Se o caminhão ficou cheio APÓS a coleta (ou já estava), direciona para estação
            // E verifica se há estações disponíveis
            if (caminhao.estaCheio() && !listaEstacoes.estaVazia()) {

                // --- Lógica de escolha da estação com menor fila ---
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
                    } else {
                        System.err.println("AVISO: Tipo de estação não suportado para contagem de fila: " + estacaoAtual.getNome());
                    }
                }

                if (melhorEstacao == null) {
                    System.err.println("ERRO: Não foi possível encontrar uma estação PADRÃO válida para o caminhão " + caminhao.getPlaca());
                    continue; // Pula para o próximo caminhão
                }
                // --- Fim da lógica de escolha ---

                // Configura o caminhão para viagem
                caminhao.definirDestino(melhorEstacao); // Define status VIAJANDO_ESTACAO e destino
                int tempoViagem = calcularTempoViagemPequeno();
                caminhao.definirTempoViagem(tempoViagem);

                System.out.printf("VIAGEM->EST: Caminhão %s cheio (%dkg). Indo para Estação %s (%d na fila). Tempo: %d min.%n",
                        caminhao.getPlaca(), caminhao.getCargaAtual(), melhorEstacao.getNome(), menorFila, tempoViagem);

                // Registra a viagem (e verifica limite)
                caminhao.registrarViagem(); // O status pode mudar para INATIVO aqui

                // Move o caminhão da lista de coleta para a lista de viagem
                caminhoesEmViagem.adicionar(caminhao);
                caminhoesEmColeta.remover(i); // Remove o caminhão da coleta (índice i ainda é válido devido ao loop reverso)

            } else if (caminhao.estaCheio() && listaEstacoes.estaVazia()) {
                System.err.println("AVISO: Caminhão " + caminhao.getPlaca() + " está cheio, mas não há estações para descarregar!");
                // O caminhão ficará parado na lista de coleta, cheio.
            }
        } // Fim do loop for (caminhões em coleta)
    }

    /** Processa os caminhões que estão atualmente em viagem (para estação ou retornando para zona). */
    private void processarCaminhoesEmViagem() {
        // Itera de trás para frente para facilitar remoção
        for (int i = caminhoesEmViagem.tamanho() - 1; i >= 0; i--) {
            CaminhaoPequeno caminhao = caminhoesEmViagem.obter(i);

            // Processa um passo de tempo da viagem
            boolean viagemCompleta = caminhao.processarViagem();

            if (viagemCompleta) {
                // Viagem terminou, verificar o destino e o status
                StatusCaminhao statusAtual = caminhao.getStatus();
                EstacaoTransferencia estacaoDest = caminhao.getEstacaoDestino();
                ZonaUrbana zonaDest = caminhao.getZonaAtual(); // Usado para retorno

                // Chegou à Estação?
                if (statusAtual == StatusCaminhao.VIAJANDO_ESTACAO && estacaoDest != null) {
                    System.out.printf("CHEGADA EST: Caminhão %s chegou à Estação %s.%n",
                            caminhao.getPlaca(), estacaoDest.getNome());

                    // Se a estação for do tipo que recebe caminhões (EstacaoPadrao)
                    if (estacaoDest instanceof EstacaoPadrao) {
                        EstacaoPadrao estacaoPadrao = (EstacaoPadrao) estacaoDest;
                        estacaoPadrao.receberCaminhaoPequeno(caminhao); // Adiciona à fila da estação
                        caminhao.setStatus(StatusCaminhao.NA_FILA);
                        estatisticas.registrarChegadaEstacao(estacaoDest.getNome()); // Registra chegada

                        // Remove da lista de viagem (caminhão agora é gerenciado pela estação)
                        caminhoesEmViagem.remover(i);
                    } else {
                        System.err.println("ERRO: Caminhão " + caminhao.getPlaca() + " chegou a uma estação que não sabe recebê-lo: " + estacaoDest.getNome());
                        // O caminhão ficará "preso" no limbo. Melhorar tratamento?
                        caminhoesEmViagem.remover(i); // Remove da viagem mesmo assim
                        listaCaminhoesPequenosOciosos.adicionar(caminhao); // Devolve aos ociosos? Ou um status de ERRO?
                        caminhao.setStatus(StatusCaminhao.OCIOSO); // Ou um novo status ERRO_ROTA
                    }
                }
                // Chegou de volta à Zona?
                else if (statusAtual == StatusCaminhao.RETORNANDO_ZONA && zonaDest != null) {
                    System.out.printf("RETORNO ZONA: Caminhão %s retornou à Zona %s para coletar.%n",
                            caminhao.getPlaca(), zonaDest.getNome());

                    // Se o caminhão ainda está ativo (não atingiu limite de viagens no meio do retorno)
                    if (caminhao.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        caminhao.setStatus(StatusCaminhao.COLETANDO);
                        // Move de volta para a lista de coleta
                        caminhoesEmColeta.adicionar(caminhao);
                        caminhoesEmViagem.remover(i);
                    } else {
                        System.out.println("INFO: Caminhão " + caminhao.getPlaca() + " retornou à zona, mas está inativo pelo limite de viagens.");
                        // Mantém inativo, remove da viagem e adiciona aos ociosos (ou uma lista de inativos)
                        listaCaminhoesPequenosOciosos.adicionar(caminhao); // Coloca nos ociosos por enquanto
                        caminhoesEmViagem.remover(i);
                    }
                }
                // Caminhão estava inativo e terminou a viagem? (Ex: ficou inativo ao registrar a última viagem)
                else if (statusAtual == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                    System.out.println("INFO: Caminhão inativo " + caminhao.getPlaca() + " completou sua última viagem.");
                    // Já está inativo, remove da viagem e move para ociosos (ele ficará inativo até a virada do dia)
                    listaCaminhoesPequenosOciosos.adicionar(caminhao);
                    caminhoesEmViagem.remover(i);
                }
                // Caso inesperado
                else {
                    System.err.printf("ERRO: Caminhão %s completou viagem em estado/destino inesperado: Status=%s, Estacao=%s, Zona=%s%n",
                            caminhao.getPlaca(), statusAtual, estacaoDest, zonaDest);
                    // Tratar como erro: remover da viagem e colocar nos ociosos
                    caminhao.setStatus(StatusCaminhao.OCIOSO); // Ou status de ERRO
                    listaCaminhoesPequenosOciosos.adicionar(caminhao);
                    caminhoesEmViagem.remover(i);
                }
            } // Fim if(viagemCompleta)
        } // Fim do loop for (caminhões em viagem)
    }

    /** Processa a lógica interna de cada estação de transferência. */
    private void processarEstacoes() {
        for (int i = 0; i < listaEstacoes.tamanho(); i++) {
            EstacaoTransferencia estacao = listaEstacoes.obter(i);

            // Verifica se é uma EstacaoPadrao para chamar métodos específicos
            if (estacao instanceof EstacaoPadrao) {
                EstacaoPadrao estacaoPadrao = (EstacaoPadrao) estacao;

                // 1. Processar a fila de caminhões pequenos (descarregar para o grande)
                CaminhaoPequeno caminhaoProcessado = estacaoPadrao.processarFila(); // Retorna o caminhão se processado

                // Se um caminhão pequeno foi processado (descarregou)
                if (caminhaoProcessado != null) {
                    estatisticas.registrarAtendimentoCaminhaoPequeno(estacao.getNome(), 0); // TODO: Calcular tempo de espera real

                    // Decidir o que fazer com o caminhão pequeno vazio
                    // Ele deve retornar para a sua zona original ou uma nova?
                    // Por simplicidade, vamos mandá-lo de volta para a ÚLTIMA zona onde coletou
                    ZonaUrbana zonaRetorno = caminhaoProcessado.getZonaAtual(); // Pega a zona ANTES de ir para estação

                    if (zonaRetorno != null && caminhaoProcessado.getStatus() != StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        // Define retorno para a zona
                        caminhaoProcessado.setStatus(StatusCaminhao.RETORNANDO_ZONA);
                        // Define o destino (a zona para onde retornar) - necessário para processarCaminhoesEmViagem
                        caminhaoProcessado.definirDestino(zonaRetorno);
                        int tempoViagemRetorno = calcularTempoViagemPequeno();
                        caminhaoProcessado.definirTempoViagem(tempoViagemRetorno);

                        System.out.printf("RETORNO->ZONA: Caminhão %s descarregou em %s. Retornando para %s. Tempo: %d min.%n",
                                caminhaoProcessado.getPlaca(), estacao.getNome(), zonaRetorno.getNome(), tempoViagemRetorno);

                        // Adiciona à lista de viagem para processar o retorno
                        caminhoesEmViagem.adicionar(caminhaoProcessado);

                    } else if (caminhaoProcessado.getStatus() == StatusCaminhao.INATIVO_LIMITE_VIAGENS) {
                        System.out.println("INFO: Caminhão " + caminhaoProcessado.getPlaca() + " descarregou, mas está inativo. Indo para ociosos.");
                        listaCaminhoesPequenosOciosos.adicionar(caminhaoProcessado);
                    }
                    else {
                        System.err.println("AVISO: Caminhão " + caminhaoProcessado.getPlaca() + " descarregou, mas não tem zona de retorno definida. Indo para ociosos.");
                        caminhaoProcessado.setStatus(StatusCaminhao.OCIOSO);
                        listaCaminhoesPequenosOciosos.adicionar(caminhaoProcessado);
                    }
                }

                // 2. Gerenciar tempo de espera do caminhão grande atual
                CaminhaoGrande caminhaoGrandeQuePartiu = estacaoPadrao.gerenciarTempoEsperaCaminhaoGrande();
                if (caminhaoGrandeQuePartiu != null) {
                    // Caminhão grande partiu para o aterro
                    estatisticas.registrarTransporteLixo(caminhaoGrandeQuePartiu.getCapacidadeMaxima()); // Aproximação da carga transportada
                    // Devolve o caminhão grande à lista de disponíveis
                    listaCaminhoesGrandesDisponiveis.adicionar(caminhaoGrandeQuePartiu);
                    caminhoesGrandesEmUso--;
                    System.out.println("INFO: Caminhão grande retornou do aterro e está disponível. Disponíveis: " + listaCaminhoesGrandesDisponiveis.tamanho());
                }

                // 3. Verificar se precisa de um caminhão grande (estação está sem e tem fila)
                if (estacaoPadrao.precisaCaminhaoGrande()) {
                    // System.out.println("INFO: Estação " + estacao.getNome() + " precisa de um caminhão grande."); // Log opcional
                    atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                }

                // 4. Verificar se o tempo de espera dos pequenos foi excedido (aciona adição de MAIS um grande)
                // Esta lógica precisa ser bem definida: adicionar um novo caminhão GRANDE à frota
                // se a espera dos PEQUENOS estiver muito longa? Ou apenas garantir que UM grande seja alocado?
                // O requisito 6 diz: "Se o tempo máximo de espera dos caminhões pequenos nas estações for excedido,
                // a simulação aciona a inclusão de um novo caminhão grande."
                // Isso implica AUMENTAR a frota total.
                if (estacaoPadrao.tempoEsperaExcedido()) {
                    System.out.println("ALERTA! Tempo de espera de caminhões pequenos excedido na Estação " + estacao.getNome() + "!");
                    // Adiciona um NOVO caminhão grande à frota geral
                    adicionarCaminhaoGrande();
                    // Tenta atribuir um caminhão grande imediatamente (pode ser o recém-criado ou outro disponível)
                    if (estacaoPadrao.precisaCaminhaoGrande()) { // Verifica novamente se ainda precisa após adicionar
                        atribuirCaminhaoGrandeParaEstacao(estacaoPadrao);
                    }
                }

            } // Fim if (estacao instanceof EstacaoPadrao)
        } // Fim loop for (estações)
    }


    /** Tenta atribuir um caminhão grande disponível para uma estação que precisa. */
    private void atribuirCaminhaoGrandeParaEstacao(EstacaoPadrao estacao) {
        if (estacao == null) return;

        if (!listaCaminhoesGrandesDisponiveis.estaVazia()) {
            // Remove o primeiro caminhão grande disponível da lista
            CaminhaoGrande caminhao = listaCaminhoesGrandesDisponiveis.remover(0);
            // Atribui à estação
            boolean sucesso = estacao.atribuirCaminhaoGrande(caminhao);

            if (sucesso) {
                caminhoesGrandesEmUso++;
                System.out.printf("ALOCAÇÃO CG: Caminhão grande alocado para Estação %s. Em uso: %d. Disponíveis: %d%n",
                        estacao.getNome(), caminhoesGrandesEmUso, listaCaminhoesGrandesDisponiveis.tamanho());
            } else {
                // Se a atribuição falhar (ex: estação já tem um), devolve para a lista
                System.err.println("ERRO: Falha ao atribuir caminhão grande à estação " + estacao.getNome() + ". Devolvendo à lista.");
                listaCaminhoesGrandesDisponiveis.adicionar(caminhao); // Adiciona de volta
            }
        } else {
            // Não há caminhões grandes disponíveis no momento
            System.out.println("INFO: Estação " + estacao.getNome() + " precisa de caminhão grande, mas NENHUM está disponível agora.");
            // A lógica de tempo excedido pode adicionar um novo posteriormente.
        }
    }

    /** Imprime um relatório de status no console em intervalos regulares. */
    private void imprimirRelatorioHorario(int hora, int minuto) {
        System.out.println("\n========================================================================");
        System.out.printf("=== RELATÓRIO DE STATUS === Tempo: %02d:%02d %s ===%n", hora, minuto, isHorarioDePico(tempoSimulado) ? "(PICO)" : "");
        System.out.println("------------------------------------------------------------------------");

        // Status Caminhões Pequenos
        int totalPequenos = caminhoesEmColeta.tamanho() + caminhoesEmViagem.tamanho() + listaCaminhoesPequenosOciosos.tamanho();
        System.out.printf("CAMINHÕES PEQUENOS: %d totais | %d em coleta | %d em viagem | %d ociosos/inativos%n",
                totalPequenos,
                caminhoesEmColeta.tamanho(),
                caminhoesEmViagem.tamanho(),
                listaCaminhoesPequenosOciosos.tamanho());

        // Status Caminhões Grandes
        System.out.printf("CAMINHÕES GRANDES: %d criados | %d em uso | %d disponíveis%n",
                totalCaminhoesGrandesCriados,
                caminhoesGrandesEmUso,
                listaCaminhoesGrandesDisponiveis.tamanho());
        System.out.println("------------------------------------------------------------------------");

        // Status Zonas
        System.out.println("STATUS DAS ZONAS (Lixo Acumulado):");
        int lixoTotalZonas = 0;
        if (!listaZonas.estaVazia()) {
            for (int i = 0; i < listaZonas.tamanho(); i++) {
                ZonaUrbana zona = listaZonas.obter(i);
                int lixo = zona.getLixoAcumulado();
                lixoTotalZonas += lixo;
                System.out.printf("  - %-10s: %6d kg%n", zona.getNome(), lixo);
            }
            System.out.printf("  Total nas Zonas: %d kg%n", lixoTotalZonas);
        } else {
            System.out.println("  (Nenhuma zona configurada)");
        }
        System.out.println("------------------------------------------------------------------------");

        // Status Estações
        System.out.println("STATUS DAS ESTAÇÕES:");
        if (!listaEstacoes.estaVazia()) {
            for (int i = 0; i < listaEstacoes.tamanho(); i++) {
                if (listaEstacoes.obter(i) instanceof EstacaoPadrao) {
                    EstacaoPadrao estacao = (EstacaoPadrao) listaEstacoes.obter(i);
                    System.out.printf("  - Estação %-10s: %d caminhões na fila | CG Presente: %s (Carga: %d kg)%n",
                            estacao.getNome(),
                            estacao.getCaminhoesNaFila(),
                            estacao.temCaminhaoGrande() ? "Sim" : "Não",
                            estacao.getCargaCaminhaoGrandeAtual());
                } else {
                    System.out.printf("  - Estação %-10s: (Tipo não padrão)%n", listaEstacoes.obter(i).getNome());
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
    public Lista<ZonaUrbana> getListaZonas() { return listaZonas; } // Expor com cuidado
    public Lista<EstacaoTransferencia> getListaEstacoes() { return listaEstacoes; } // Expor com cuidado
}