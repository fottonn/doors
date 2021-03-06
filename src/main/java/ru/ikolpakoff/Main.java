package ru.ikolpakoff;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.ikolpakoff.base.HibernateUtil;
import ru.ikolpakoff.controllers.MainWindowController;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Doors");
        primaryStage.setScene(new Scene(root, 560, 440));
        primaryStage.setMinHeight(460);
        primaryStage.setMinWidth(590);
        primaryStage.setMaxWidth(590);
        primaryStage.getIcons().add(new Image("/img/DoorIcon.png"));
        primaryStage.setOnCloseRequest(event -> HibernateUtil.sessionFactoryClose());
        primaryStage.show();
        ((MainWindowController)loader.getController()).setRootStage(primaryStage);


    }
}
