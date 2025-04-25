# Simulador de Coleta de Lixo - Mockup Java

Este projeto oferece um **mockup didático** para a construção de um **simulador de coleta de lixo na cidade de Teresina**, baseado em programação orientada a objetos. Ele foi projetado para ser **expandido pelos alunos**, que deverão utilizar suas próprias **estruturas de dados implementadas do zero** (listas, filas, pilhas, ponteiros) para compor a lógica do simulador.

## Objetivo Pedagógico

Este mockup serve como ponto de partida para os estudantes desenvolverem:
- Modelagem de sistemas reais utilizando POO
- Uso de herança, polimorfismo e abstração
- Controle de tempo em simulações
- Serialização de estado para persistência
- Integração com estruturas de dados personalizadas

## Estrutura do Projeto

```
simulador/
├── Simulador.java
├── Main.java
├── zonas/
│   └── ZonaUrbana.java
├── caminhoes/
│   ├── CaminhaoPequeno.java
│   ├── CaminhaoPequenoPadrao.java
│   ├── CaminhaoGrande.java
│   └── CaminhaoGrandePadrao.java
├── estacoes/
│   ├── EstacaoTransferencia.java
│   └── EstacaoPadrao.java
└── README.md
```

## Componentes e Responsabilidades

### `Simulador.java`
- Controla o tempo da simulação (1 segundo = 1 minuto simulado)
- Oferece métodos para `iniciar()`, `pausar()`, `continuarSimulacao()`, `encerrar()`, `gravar()` e `carregar()`
- Utiliza `Timer` para simular o avanço do tempo

### `ZonaUrbana.java`
- Gera uma quantidade aleatória de lixo por minuto
- Permite que caminhões pequenos coletem lixo acumulado

### `CaminhaoPequeno` e `CaminhaoPequenoPadrao`
- Representam os caminhões que fazem a coleta nas zonas urbanas
- Possuem capacidade limitada e descarregam nas estações de transferência

### `CaminhaoGrande` e `CaminhaoGrandePadrao`
- Transportam o lixo acumulado das estações até o aterro sanitário
- Partem quando estiverem com a carga cheia (20 toneladas)

### `EstacaoTransferencia` e `EstacaoPadrao`
- Recebem lixo dos caminhões pequenos
- Armazenam o lixo até serem descarregadas pelos caminhões grandes

## Tarefas dos Alunos

Este mockup **não inclui as estruturas de dados** como listas, filas, pilhas e ponteiros. Estas devem ser **criadas pelos próprios alunos** para:

- Gerenciar a fila de caminhões nas estações
- Registrar histórico de coletas por zona
- Controlar a ordem de geração e coleta de lixo
- Organizar eventos de simulação (opcional)

## Instruções de Uso

1. Compile todos os arquivos com `javac`:
    ```bash
    javac simulador/**/*.java
    ```

2. Execute o programa de simulação:
    ```bash
    java simulador.Main
    ```

3. Expanda o código conforme as regras do problema proposto:
    - Adicione zonas urbanas e caminhões
    - Crie interações entre os objetos
    - Utilize estruturas de dados personalizadas

## Requisitos

- Java 8 ou superior
- Estruturas de dados implementadas pelo aluno

---

**Autor**: Professor Sekeff  
**Finalidade**: Uso educacional na disciplina de Estrutura de Dados