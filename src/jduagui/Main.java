/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jduagui;

import java.io.File;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import static jduagui.Controller.treeMap;

/**
 *
 * @author Renegade Hackers: Luis
 */
public class Main extends Application {
    public enum OS{ Windows, OSX, UNIX  }
    public static OS env;
    public static String defaultRoot;
    public static String rootPath;
    public static String[] runtimeArgs;
    private final Image Icon = 
        new Image(getClass().getResourceAsStream("/16x16/apps/disk-usage-analyzer.png"));
    private final ImageView graphic = 
        new ImageView(new Image(getClass().getResourceAsStream("/96x96/apps/disk-usage-analyzer.png")));
    
    
    @Override
    public void start(Stage stage) throws Exception {
        rootPath = null;
        if(env == OS.UNIX){
            Dialog popup = getDialog();
            Optional<Pair<Pair<String, String>, Pair<String, String>>> result ;
            result= popup.showAndWait();
            if(!result.isPresent())
                System.exit(1);
            else
                result.ifPresent(runArgs -> {
                        runtimeArgs[0] = runArgs.getKey().getKey();
                        runtimeArgs[1] = runArgs.getKey().getValue();
                        runtimeArgs[2] = runArgs.getValue().getKey();
                        runtimeArgs[3] = runArgs.getValue().getValue();
                    }
                );
        }
        File file = openDialog.chooseDirectory(stage, defaultRoot);
        if(file != null)
            rootPath = file.getAbsolutePath();
        FXMLLoader loader = new FXMLLoader();
        
        
        Parent root = loader.load(getClass().getResource("JDUA.fxml"));
        
        Scene scene = new Scene(root);
        stage.setTitle("JDUA");
        stage.getIcons().add(Icon);
        stage.setScene(scene);
        stage.setOnCloseRequest(e ->{
            treeMap.destroy();
            Platform.exit();
        });
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("starting program");
        runtimeArgs = new String[4];
        getOS();
        switch(env){
            case Windows:   
                defaultRoot = "C:\\"; 
                runtimeArgs[0] = "cmd /c start cmd /k";
                runtimeArgs[1] = "cd <path>";
                runtimeArgs[2] = "explorer.exe";
                runtimeArgs[3] = "<path>";
                break;
            case OSX:       
                defaultRoot = "/";
                runtimeArgs[0] = "open -a Terminal.app";
                runtimeArgs[1] = "<path>";
                runtimeArgs[2] = "open -a Finder.app";
                runtimeArgs[3] = "<path>";
                break;
            case UNIX:      
                defaultRoot = "/";
                break;
        }
        launch(args);
    }

    public static void getOS(){
        String os = System.getProperty("os.name");
        if(os.contains("Windows") || os.contains("windows"))
            env = OS.Windows;
        else if(os.contains("OS X") || os.contains("Mac"))
            env = OS.OSX;
        else
            env = OS.UNIX;
    }
    
    public Dialog getDialog(){
        Dialog<Pair<Pair<String, String>, Pair<String, String>>> dialog = new Dialog<>();
        dialog.setGraphic(graphic);
        dialog.setTitle("User Arguments");
        String header = "For the following commands, in the argument\nportion include <path>";
        header += "to allow the runtime to\nexecute the proper commands. Cancelling will\n";
        header += "terminate the program.";
        dialog.setHeaderText(header);
        ButtonType confirmation = new ButtonType("Confirm", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmation, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.getColumnConstraints().add(new ColumnConstraints(150));
        grid.getColumnConstraints().add(new ColumnConstraints(200));

        grid.setPadding(new Insets(20, 20, 20, 25));
        TextField term = new TextField();
        term.setText("x-terminal-emulator");
        TextField termArg = new TextField();
        termArg.setText("--working-directory=<path>");
        TextField man = new TextField();
        man.setText("nautilus");
        TextField manArg = new TextField();
        manArg.setText("<path>");

        grid.add(new Label("Terminal Emulator:"), 0, 0);
        grid.add(term, 1, 0);
        grid.add(new Label("Terminal Argument:"), 0, 1);
        grid.add(termArg, 1, 1);
        grid.add(new Label("File Manager:"), 0, 2);
        grid.add(man, 1, 2);
        grid.add(new Label("Manager Argument:"), 0, 3);
        grid.add(manArg, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> term.requestFocus());
        dialog.setResultConverter((ButtonType dialogButton) -> {
            if (dialogButton == confirmation) {
                Pair<String, String> terminal = new Pair<>(term.getText(), termArg.getText());
                Pair<String, String> manager = new Pair<>(man.getText(), manArg.getText());
                return new Pair<>(terminal, manager);
            }
            return null;
        });
        
        Stage stage = (Stage)dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(Icon);
  
        return dialog;
    }
    
}