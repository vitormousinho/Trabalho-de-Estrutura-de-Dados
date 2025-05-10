// Arquivo: MainApp.java
// Pacote: (defina seu pacote, ex: com.meuprojeto.simulador)

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert; // <--- IMPORTAÇÃO ADICIONADA

// Certifique-se de que as classes Simulador e SimuladorController estão acessíveis
// import com.meuprojeto.simulador.Simulador; // Exemplo de importação
// import com.meuprojeto.simulador.SimuladorController; // Exemplo de importação

public class MainApp extends Application {

    private Simulador simulador; // Instância do seu simulador
    private SimuladorController controller;

    @Override
    public void init() throws Exception {
        super.init();
        // Inicialize o simulador aqui, talvez com configurações padrão
        // ou carregue configurações de um arquivo.
        // Por enquanto, vamos criar uma nova instância.
        simulador = new Simulador();
        // Você pode querer carregar configurações padrão aqui ou permitir
        // que o usuário configure através da UI antes de iniciar.
        // Para este exemplo, o controller irá pegar essa instância.
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Carrega o arquivo FXML
            // Certifique-se de que o caminho para "simulador_interface.fxml" está correto
            // Se estiver no mesmo pacote, apenas o nome do arquivo é suficiente.
            // Se estiver em uma pasta 'resources' que espelha a estrutura do pacote, use:
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/meuprojeto/simulador/simulador_interface.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("simulador_interface.fxml"));
            Parent root = loader.load();

            // Obtém o controller e passa a instância do simulador
            controller = loader.getController();
            controller.setSimulador(simulador);
            controller.setPrimaryStage(primaryStage); // Para diálogos de salvar/carregar
            controller.inicializarInterface(); // Método para configurar listeners e atualizações iniciais

            // Configura a cena
            Scene scene = new Scene(root, 1200, 800); // Ajuste o tamanho conforme necessário

            // Adiciona um arquivo CSS para estilização (opcional, mas recomendado)
            // Certifique-se de que o caminho para "styles.css" está correto.
            // Verifique se o arquivo styles.css está acessível e o caminho está correto.
            // Se styles.css estiver na mesma pasta que MainApp.java:
            // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            // Se estiver na raiz da pasta 'resources':
            // scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());


            primaryStage.setTitle("Simulador de Coleta de Lixo - Teresina (JavaFX)");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar o FXML ou iniciar a aplicação JavaFX:");
            e.printStackTrace();
            // Tratar o erro adequadamente (ex: mostrar um diálogo de erro)
            Alert alert = new Alert(Alert.AlertType.ERROR, "Falha ao carregar a interface: " + e.getMessage() + "\nVerifique se o arquivo FXML está no local correto e se o controller está definido.");
            alert.setHeaderText("Erro de Carregamento");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Erro inesperado ao iniciar a aplicação JavaFX:");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ocorreu um erro inesperado: " + e.getMessage());
            alert.setHeaderText("Erro Inesperado");
            alert.showAndWait();
        }
    }

    @Override
    public void stop() throws Exception {
        // Ações de limpeza ao fechar a aplicação, se necessário
        System.out.println("Aplicação JavaFX encerrando...");
        if (controller != null) {
            controller.encerrarTimerAtualizacao(); // Garante que o timer pare
        }
        if (simulador != null && simulador.getEstatisticas() != null && simulador.getTempoSimulado() > 0) {
            // Opcional: Gerar um relatório final automaticamente ao fechar, se a simulação rodou.
            // controller.exibirRelatorioFinal(); // Se quiser mostrar na UI
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}