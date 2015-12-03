/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jduagui;

import java.io.File;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;


/**
 *
 * @author Renegade Hackers: Luis
 */
public class openDialog {
    static File chooseDirectory(Window main, String path){
        DirectoryChooser diag = new DirectoryChooser();
        diag.setTitle("Choose Direcotry to Analyze");
        diag.setInitialDirectory(new File(path));
        File file = diag.showDialog(main);
        return file;
    }
}
