# Simulador de Coleta de Lixo Urbano

Este projeto é um simulador de um sistema de coleta de lixo urbano, desenvolvido como parte de um trabalho na disciplina de Estrutura de Dados. O objetivo é modelar e simular a logística envolvida na coleta de lixo em diferentes zonas de uma cidade, utilizando diferentes tipos de caminhões e estações de processamento.

## Sumário

- [Visão Geral](#visão-geral)
- [Funcionalidades Principais (Inferidas)](#funcionalidades-principais-inferidas)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Como Executar (Sugestão)](#como-executar-sugestão)
- [Documentação Adicional](#documentação-adicional)
- [Relatórios de Simulação](#relatórios-de-simulação)
- [Autores](#autores)
- [Como Contribuir](#como-contribuir)
- [Licença](#licença)

## Visão Geral

O simulador permite configurar e executar cenários de coleta de lixo, acompanhando o desempenho de caminhões, o acúmulo de lixo em zonas urbanas e a eficiência das estações de coleta e transferência. O sistema utiliza estruturas de dados customizadas para gerenciar as entidades e os eventos da simulação.

## Funcionalidades Principais (Inferidas)

* **Simulação Detalhada:** Modela o processo de coleta de lixo ao longo do tempo.
* **Gerenciamento de Frota:**
    * Diferentes tipos de caminhões (e.g., `CaminhaoPequeno.java`, `CaminhaoGrande.java`).
    * Controle de status e movimentação dos caminhões (`StatusCaminhao.java`).
    * Garagem central para os caminhões (`GaragemCentral.java`).
    * Lógica de distribuição de caminhões (`DistribuicaoCaminhoes.java`).
* **Mapeamento Urbano:**
    * Representação de um mapa urbano (`Mapa/MapaUrbano.java`).
    * Divisão em zonas urbanas com características próprias (`zonas/ZonaUrbana.java`, `zonas/ScoreZona.java`).
* **Estações de Coleta e Transferência:**
    * Estações padrão para descarte (`estacoes/EstacaoPadrao.java`).
    * Estações de transferência para otimização logística (`estacoes/EstacaoTransferencia.java`).
    * Processamento de filas de caminhões nas estações (`estacoes/ResultadoProcessamentoFila.java`).
* **Estruturas de Dados:**
    * Implementações próprias de Fila (`Estruturas/Fila.java`) e Lista (`Estruturas/Lista.java`) para gerenciar os elementos da simulação.
* **Análise e Relatórios:**
    * Coleta de estatísticas de simulação (`Estatisticas.java`, `MetricaEstacaoModel.java`).
    * Geração de relatórios textuais e possivelmente visuais sobre o desempenho da simulação (pasta `Relatorios/`, `RelatorioFinalController.java`).
* **Interface Gráfica:**
    * Interface de usuário para interagir com o simulador, configurar parâmetros e visualizar resultados (inferido de `MainApp.java`, `SimuladorController.java` e ficheiros `.css`).

## Estrutura do Projeto

O projeto está organizado nos seguintes pacotes principais (localizados em `ColetaLixo-main/`):

* `caminhoes/`: Contém as classes relacionadas aos diferentes tipos de caminhões e seu comportamento.
* `estacoes/`: Define as estações de coleta e de transferência de lixo.
* `Estruturas/`: Inclui implementações customizadas de estruturas de dados como filas e listas.
* `Mapa/`: Responsável pela representação do mapa urbano e seus componentes.
* `zonas/`: Modela as diferentes zonas urbanas da cidade.
* `resources/`: Contém recursos como folhas de estilo (`styles.css`).
* Raiz (`ColetaLixo-main/`): Classes principais como `MainApp.java` (ponto de entrada da aplicação), `Simulador.java` (lógica central da simulação), `SimuladorController.java` (controlador da interface principal) e `RelatorioFinalController.java` (controlador da tela de relatório).

A pasta `Relatorios/` armazena os resultados das simulações executadas.

## Tecnologias Utilizadas

* **Java:** Linguagem de programação principal.
* **JavaFX:** Framework para a criação da interface gráfica do usuário (inferido pela estrutura e nomes de ficheiros como `MainApp.java` e `*Controller.java`).

## Como Executar (Sugestão)

Para executar este projeto, você precisará de:

1.  **JDK (Java Development Kit)** instalado (e.g., versão 8, 11 ou superior).
2.  **JavaFX SDK** configurado no seu ambiente de desenvolvimento, caso não esteja incluído na sua distribuição do JDK.

**Passos gerais para execução:**

1.  **Clone o repositório** (ou extraia os ficheiros do projeto).
2.  **Abra o projeto em uma IDE Java** que suporte JavaFX (e.g., IntelliJ IDEA, Eclipse, NetBeans).
    * A estrutura com a pasta `out/` sugere que o projeto pode ter sido desenvolvido no IntelliJ IDEA.
3.  **Configure o SDK do JavaFX** nas configurações do projeto na sua IDE, se necessário (especialmente para Java 11+).
4.  **Compile o projeto.**
5.  **Execute a classe principal:** `MainApp.java` (localizada em `ColetaLixo-main/MainApp.java`).

## Documentação Adicional

O projeto inclui os seguintes documentos que fornecem mais detalhes sobre a concepção, modelagem e funcionamento do simulador:

* `Artigo_Ed.pdf`: Artigo detalhando os aspetos teóricos e práticos do trabalho.
* `documentacaoSimuladorDeColeta.pdf`: Documentação específica do simulador, suas funcionalidades e como utilizá-lo.

Recomenda-se a leitura destes documentos para uma compreensão completa do projeto.

## Relatórios de Simulação

A pasta `Relatorios/` contém ficheiros de texto (`.txt`) e possivelmente HTML (`.html`) que são gerados ao final de cada simulação. Estes relatórios detalham as estatísticas e os resultados obtidos, como:

* Quantidade de lixo coletado.
* Tempo de operação dos caminhões.
* Níveis de ocupação das estações.
* Custos operacionais (se modelado).
* Outras métricas de desempenho relevantes.

## Autores

* *[Alexandre Medeiros Cavalcante e Vitor Daniel Alves Mousinho]*

## Como Contribuir

Se desejar contribuir para este projeto, por favor, siga estas etapas:

1.  Faça um fork do projeto.
2.  Crie uma branch para sua feature (`git checkout -b feature/NovaFuncionalidade`).
3.  Commit suas alterações (`git commit -am 'Adiciona NovaFuncionalidade'`).
4.  Push para a branch (`git push origin feature/NovaFuncionalidade`).
5.  Abra um Pull Request.

## Licença

Este projeto não possui uma licença especifica.

---
