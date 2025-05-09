import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class TesteJavaFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Cria um rótulo com o texto "Olá, JavaFX!"
        Label helloLabel = new Label("Olá, JavaFX!");

        // Cria um painel StackPane para organizar o rótulo no centro
        StackPane root = new StackPane();
        root.getChildren().add(helloLabel);

        // Cria a cena, definindo o painel raiz e as dimensões da janela
        Scene scene = new Scene(root, 300, 200);

        // Define o título da janela
        primaryStage.setTitle("Teste JavaFX");
        // Define a cena na janela principal
        primaryStage.setScene(scene);
        // Exibe a janela
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Lança a aplicação JavaFX
        launch(args);
    }
}