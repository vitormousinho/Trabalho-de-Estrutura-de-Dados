import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert; // <--- IMPORTAÇÃO ADICIONADA

public class MainApp extends Application {

    private Simulador simulador; // Instância do seu simulador
    private SimuladorController controller;

    @Override
    public void init() throws Exception {
        super.init();
        simulador = new Simulador();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("simulador_interface.fxml"));
            Parent root = loader.load();

            controller = loader.getController();
            controller.setSimulador(simulador);
            controller.setPrimaryStage(primaryStage); // Para diálogos de salvar/carregar
            controller.inicializarInterface(); // Método para configurar listeners e atualizações iniciais

            // Configura a cena
            Scene scene = new Scene(root, 1200, 800); // Ajuste o tamanho conforme necessário

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
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}