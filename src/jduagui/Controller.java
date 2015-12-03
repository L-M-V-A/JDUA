/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jduagui;


import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.net.URL;
import java.util.*;
import java.math.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import static jduagui.Controller.storageCache;
import static jduagui.Main.defaultRoot;
import static jduagui.Main.rootPath;
import static jduagui.Main.runtimeArgs;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Renegade Hackers: Luis
 */
public class Controller implements Initializable {
//  FXML IDs
/*============================================================================*/    
    @FXML public SplitPane verticalSplit;
    @FXML public GridPane swingPane;
    @FXML public TreeTableView directoryTable;
    @FXML public TableView extensionTable;
    @FXML public ImageView terminalLaunch;
    @FXML public ImageView managerLaunch;
    @FXML public ImageView openButton;
    @FXML public ImageView toggleTreeMap;
    @FXML public ImageView refreshButton;
    @FXML public SwingNode swingNode;
    @FXML public MenuItem editCopy;
    @FXML public MenuItem helpAbout;
    @FXML public TextField statusIndicator;
 
//  Terminal Button Action Handlers
/*============================================================================*/    
    @FXML public void onTerminalClick(){
        System.out.println("launching terminal");
        System.out.println(runtimeArgs[0] + ' ' + runtimeArgs[1]);
        command = runtimeArgs[0] + ' ' + runtimeArgs[1];
        command = command.replace("<path>", selectedPath);
        System.out.println(command);
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML public void onTerminalEnter()
    {   turnOn(termHighlight);  }
    @FXML public void onTerminalExit()
    {   turnOff(termHighlight);    }
    
    
//  File Manager Button Action Handlers
/*============================================================================*/    
    @FXML public void onManagerClick(){
        System.out.println("launching FileManager");
        System.out.println(runtimeArgs[2] + ' ' + runtimeArgs[3]);
        command = runtimeArgs[2] + ' ' + runtimeArgs[3];
        command = command.replace("<path>", selectedPath);
        System.out.println(command);
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML public void onManagerEnter()
    {   turnOn(managerHighlight);  }
    @FXML public void onManagerExit()
    {   turnOff(managerHighlight);    }
    
    
//  Open Button Action Handlers
/*============================================================================*/   
    @FXML public void onOpenClick(){
        statusIndicator.setText("Choosing Directory.");
        File file;
        if(rootPath != null)
            file = openDialog.chooseDirectory(stage, rootPath);
        else
            file = openDialog.chooseDirectory(stage, defaultRoot);
        
        String previousRoot = rootPath;
        
        if(file != null)
            rootPath = file.getAbsolutePath();  
        else
            return;
        
        statusIndicator.setText("Analyzing Directory.");
        setVisible(false);
        if(previousRoot != null)
            reset();
        init();
        setVisible(true);
    }
    
    @FXML public void onOpenEnter()
    {   turnOn(openHighlight);  }
    @FXML public void onOpenExit()
    {   turnOff(openHighlight); }
    
    
//  Toggle Button Action Handlers
/*============================================================================*/   
    @FXML public void onToggleClick(){
        System.out.println("Toggling TreeMap");
        if(swingNode.isVisible()){
            swingNode.setVisible(false);
            verticalSplit.setDividerPositions(1.0);
        }else if(!swingNode.isVisible()){
            swingNode.setVisible(true);
            verticalSplit.setDividerPositions(0.6);
        }
    }
    
    @FXML public void onToggleEnter()
    {   turnOn(toggleHighlight);    }
    @FXML public void onToggleExit()
    {   turnOff(toggleHighlight);   }
    
    
//  Refresh Button Action Handlers
/*============================================================================*/
    @FXML public void onRefreshClick(){
        if(rootPath != null)
            statusIndicator.setText("Refreshing.");
            setVisible(false);
            reset();
            init();
            setVisible(true);
    }
    
    @FXML public void onRefreshEnter()
    {   turnOn(refreshHighlight);   }
    @FXML public void onRefreshExit()
    {   turnOff(refreshHighlight);  }
    
    
    
//  Quit Menu Item Action Handler
/*============================================================================*/    
    @FXML public void onQuit(){   
        reset();
        System.exit(1);
    }    

//  Copy Menu Item Action Handler
/*============================================================================*/    
    @FXML public void onCopy()
    {   
        content.putString(selectedPath);
        clipboard.setContent(content); 
    }

//  About Menu Item Action Handler
/*============================================================================*/    
    @FXML public void onAbout()
    {   System.out.println("clicked about");    }

    
// Global Constants & Variables
/*============================================================================*/
    //Owner stage
    //private Stage popup;
    private Stage stage;
    public static FileTree treeMap;
    
    //Effects for buttons
    final static InnerShadow termHighlight = new InnerShadow(
       BlurType.ONE_PASS_BOX, Color.AZURE, 0.0, 0.0, 0.0, 0.0
    );
    final static InnerShadow openHighlight = new InnerShadow(
       BlurType.ONE_PASS_BOX, Color.AZURE, 0.0, 0.0, 0.0, 0.0
    );
    final static InnerShadow refreshHighlight = new InnerShadow(
       BlurType.ONE_PASS_BOX, Color.AZURE, 0.0, 0.0, 0.0, 0.0
    );
    final static InnerShadow toggleHighlight = new InnerShadow(
       BlurType.ONE_PASS_BOX, Color.AZURE, 0.0, 0.0, 0.0, 0.0
    );
    
    final static InnerShadow managerHighlight = new InnerShadow(
       BlurType.ONE_PASS_BOX, Color.AZURE, 0.0, 0.0, 0.0, 0.0
    );
    
    //Constants
    final static double lightOn = 80.0;
    final static double lightOff = 0.0;
    final static long TB = (long)Math.pow(2,40);
    final static long GB = (long)Math.pow(2,30);
    final static long MB = (long)Math.pow(2,20);
    final static long KB = (long)Math.pow(2,10);

    
    //Maps for Data Caching
    public static final Map<String, Long> storageCache = new HashMap<>();
    private final Map<String, Long> subDirs = new HashMap<>();
    private final Map<String, Long> subFiles = new HashMap<>();
    private final Map<String, Extension> extensions = new HashMap<>();
    
    //List for Table Input
    ObservableList<Map.Entry<String, Extension>> tableContent;
    
    //String to Identify Currently Selected TreeTable Row 
    //and also for TreeMap if Feature Implemented
    private String selectedPath = rootPath;
    
    
    private ChangeListener listener = setListener();
    
    //Clipboard
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();
    
    //Runtime comand container
    private String command;
    
    //private boolean first = true;
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        termHighlight.setHeight(0.0);
        termHighlight.setWidth(0.0);

        openHighlight.setHeight(0.0);
        openHighlight.setWidth(0.0);

        refreshHighlight.setHeight(0.0);
        refreshHighlight.setWidth(0.0);

        toggleHighlight.setHeight(0.0);
        toggleHighlight.setWidth(0.0);

        managerHighlight.setHeight(0.0);
        managerHighlight.setWidth(0.0);

        toggleTreeMap.setEffect(toggleHighlight);
        refreshButton.setEffect(refreshHighlight);
        openButton.setEffect(openHighlight);
        terminalLaunch.setEffect(termHighlight);
        managerLaunch.setEffect(managerHighlight);

        if(rootPath != null){
            statusIndicator.setText("Analyzing Directory.");
            init();
        }else{  statusIndicator.setText("Waiting for Directory.");  } 
    }
    
    private void init(){
        try{
            directoryTable.setRoot(buildFileTree(rootPath));
            directoryTable.getSelectionModel().selectedItemProperty().addListener(listener);
            directoryTable.getColumns().setAll(setNameColumn(), setSizeColumn());
            directoryTable.getColumns().addAll(setPercentColumn(), setItemColumn());
            directoryTable.getColumns().addAll(setDirColumn(), setFileColumn());
            FileItem.getExtensions(rootPath, extensions);
            tableContent = FXCollections.observableArrayList(extensions.entrySet());
            extensionTable.setItems(tableContent);
            extensionTable.getColumns().addAll(extNameColumn(), extCountColumn(), extSizeColumn());
            statusIndicator.setText("Done.");

            
            int width = (int)swingPane.getMinWidth() + (int)swingPane.getMaxWidth();
            int length = (int)swingPane.getMinHeight() + (int)swingPane.getMaxHeight();

            File root = (File)directoryTable.getRoot().getValue();
            treeMap = new FileTree(length, width, root);
            swingNode.setContent(treeMap.getContent());
            
        }catch(Exception e){
            System.out.println("error occured: " + e);
            e.printStackTrace();
        }
    }
    
    private void reset(){
        directoryTable.getColumns().removeAll(
            setNameColumn(), setSizeColumn(), setPercentColumn(), setItemColumn(), setDirColumn(), setFileColumn()
        ); 
        directoryTable.getSelectionModel().selectedItemProperty().removeListener(listener);
        extensionTable.getColumns().clear();
        directoryTable.getRoot().getChildren().clear();
        directoryTable.setRoot(null);
        extensionTable.getItems().clear();
        tableContent.clear();
        swingNode.setContent(null);
        treeMap.destroy();
        extensions.clear();
        storageCache.clear();
        subDirs.clear();
        subFiles.clear();
    }
    
    private void turnOn(InnerShadow img){
        img.setHeight(lightOn);
        img.setWidth(lightOn);
    }
    
    private void turnOff(InnerShadow img){
        img.setHeight(lightOff);
        img.setWidth(lightOff);
    }
    
    private void setVisible(boolean bool){
        directoryTable.setVisible(bool);
        extensionTable.setVisible(bool);
        swingNode.setVisible(bool);
    }
    
    private ChangeListener setListener(){
        return new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
               TreeItem<File> selectedItem = (TreeItem<File>)newValue;
               if(selectedItem.getValue().getAbsolutePath().equals(rootPath))
                   selectedPath = rootPath;
               else if(selectedItem.getValue().isDirectory())
                   selectedPath = selectedItem.getValue().getAbsolutePath();
               else if(selectedItem.getValue().isFile())
                   selectedPath = selectedItem.getValue().getParent();
            }   
        };
    }
    
    private TreeItem<File> buildFileTree(String path) throws IOException {
        File f = new File(path);
        TreeItem<File> root = createNode(f);
        root.setExpanded(true);
        root.setGraphic(new ImageView(
            new Image(getClass().getResourceAsStream(
                    "/16x16/apps/gnome-disks.png"))
        )); 
        return root;
    }
    
    private TreeItem<File> createNode(final File f) {
        return new TreeItem<File>(f){
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;
            
            @Override
             public ObservableList<TreeItem<File>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildChildren(this));
		}
                return super.getChildren();
            }
             
            @Override
            public boolean isLeaf() {
                if(isFirstTimeLeaf){
                    isFirstTimeLeaf = false;
                    File f = (File)getValue();
                    isLeaf = f.isFile();
                }  
                return isLeaf;
            }
            
            private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
                File f = TreeItem.getValue();
                if(f != null && f.isDirectory() && (!f.getAbsolutePath().equals("/proc/"))){
                    File[] files = f.listFiles();
                    if(files != null){
                        ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                        for(File childFile : files){
                            children.add(createNode(childFile));
                        }
                        return children;
                    }
                }
                return FXCollections.emptyObservableList();
            }
        };
    }
 
//  Name Column Creation
/*============================================================================*/
    private TreeTableColumn setNameColumn(){
        TreeTableColumn<File, String> col = new TreeTableColumn<>("Name");
        col.setCellValueFactory( (CellDataFeatures<File, String> p) ->{
            File f = p.getValue().getValue();
            if( f.getPath().equals(rootPath) )
                return new ReadOnlyStringWrapper( f.getPath());
            else
                return new ReadOnlyStringWrapper( f.getName()); 
        });
        return col;
    }

//  Size Column Creation
/*============================================================================*/
    private TreeTableColumn setSizeColumn(){
        TreeTableColumn<File, String> col = new TreeTableColumn<>("Size");
        col.setCellValueFactory(
            (CellDataFeatures<File, String> p) -> {
                ReadOnlyStringWrapper wrap;
                File file = p.getValue().getValue();
                String curPath = file.getPath();
                long size = file.length();
                if(file.isDirectory()){
                    if(storageCache.containsKey(curPath)){
                        wrap = new ReadOnlyStringWrapper( getByteSize(storageCache.get(curPath)) );
                    }else{
                        try{
                            size = FileItem.getSize(curPath, subDirs, subFiles);
                            storageCache.put(curPath, size);
                            wrap = new ReadOnlyStringWrapper(getByteSize(size));
                        }catch(IOException e){
                            storageCache.put(curPath, new Long(0));
                            wrap = new ReadOnlyStringWrapper(getByteSize(0));
                        }
                    }
                }else if(file.isFile()){
                    storageCache.put(curPath, size);
                    wrap = new ReadOnlyStringWrapper(getByteSize(size));
                }else{
                    storageCache.put(curPath, new Long(0));
                    wrap = new ReadOnlyStringWrapper(getByteSize(0));
                }
                return wrap;
            }
        );
        col.setComparator(new sizeComparator());
        return col;
    }
    
    class sizeComparator implements Comparator<String>{
        final String delim = "[ ]+";
        @Override
        public int compare(String sz1, String sz2)
        {
            String[] str1 = sz1.split(delim);
            String[] str2 = sz2.split(delim);
            if(str1[1].equals(str2[1])){
                Double lst = Double.valueOf(str1[0]);
                Double rst = Double.valueOf(str2[0]);
                return lst.compareTo(rst);
            }
            else{
                Integer lsf = suffixWeight(str1[1]);
                Integer rsf = suffixWeight(str2[1]);
                return lsf.compareTo(rsf);
            }
        }
        public int suffixWeight(String suffix)
        {
            switch(suffix){
                case "B": 	return 0;
                case "KB": 	return 1;
                case "MB": 	return 2;
                case "GB": 	return 3;
                case "TB": 	return 4;
                default: 	return -1;
            }

        }
    }

//  Percent Column Creation
/*============================================================================*/
    private TreeTableColumn setPercentColumn(){
        TreeTableColumn<File, String> col = new TreeTableColumn<>("~Percent");
        col.setCellValueFactory( (CellDataFeatures<File, String> p) -> {
            ReadOnlyStringWrapper wrap;
            File file = p.getValue().getValue();
            String curPath = file.getPath();
            if(curPath.equals(rootPath)){
                wrap = new ReadOnlyStringWrapper("100.00 %");
            }else{
                if( (storageCache.get(curPath) != null) && (storageCache.get(curPath) > 0) ){
                    double percentage = (double)storageCache.get(curPath);
                    percentage /= storageCache.get(file.getParent());
                    if(percentage == 1.0)	wrap = new ReadOnlyStringWrapper("100.00 %");
                    else{
                        percentage *= 100.0;
                        wrap = new ReadOnlyStringWrapper(twoDecimals(new BigDecimal(percentage), " %"));
                    }
                }else{
                    wrap = new ReadOnlyStringWrapper("0.00 %");
                }
            }
            return wrap;
        });
        col.setComparator(new percentComparator());
        return col;
    }
    
    class percentComparator implements Comparator<String>
	{
            final String delim = "[ ]+";
            @Override
            public int compare(String sz1, String sz2)
            {
                String[] str1 = sz1.split(delim);
                String[] str2 = sz2.split(delim);
                Double lpr = Double.valueOf(str1[0]);
                Double rpr = Double.valueOf(str2[0]);
                return lpr.compareTo(rpr);
            }
	}

//  Item Column Creation
/*============================================================================*/
    
    private TreeTableColumn setItemColumn(){
        TreeTableColumn<File, String> col  = new TreeTableColumn<>("Items");
        col.setCellValueFactory(
            (CellDataFeatures<File, String> p) ->{
                File file = p.getValue().getValue();
                String curPath = file.getPath();
                if(file.isDirectory()){
                    return new ReadOnlyStringWrapper("" + (subDirs.get(curPath) + subFiles.get(curPath)));
                }else   return new ReadOnlyStringWrapper("");
            }
        );
        col.setComparator(new longComparator());
        return col;
    }
    
//  Directory Column Creation
/*============================================================================*/   
    private TreeTableColumn setDirColumn(){
        TreeTableColumn<File, String> col  = new TreeTableColumn<>("Subdirectories");
        col.setCellValueFactory(
            (CellDataFeatures<File, String> p) ->{
                File file = p.getValue().getValue();
                String curPath = file.getPath();
                if(file.isDirectory()){
                    return new ReadOnlyStringWrapper("" + subDirs.get(curPath));
                }else   return new ReadOnlyStringWrapper("");
            }
        );
        col.setComparator(new longComparator());
        return col;
    }
//  File Column Creation
/*============================================================================*/    
    private TreeTableColumn setFileColumn(){
        TreeTableColumn<File, String> col  = new TreeTableColumn<>("Files");
        col.setCellValueFactory(
            (CellDataFeatures<File, String> p) ->{
                File file = p.getValue().getValue();
                String curPath = file.getPath();
                if(file.isDirectory()){
                    return new ReadOnlyStringWrapper("" + subFiles.get(curPath));
                }else   return new ReadOnlyStringWrapper("");
            }
        );
        col.setComparator(new longComparator());
        return col;
    }
    
//  Long Comparator
/*============================================================================*/
    class longComparator implements Comparator<String> {
        @Override
        public int compare(String sz1, String sz2) {
            Long lhs = new Long(0), rhs = new Long(0);
            if(!sz1.isEmpty())
                lhs = Long.valueOf(sz1);

            if(!sz2.isEmpty())
                rhs = Long.valueOf(sz2);

            return lhs.compareTo(rhs);
        }
    }
    
//  Cell Creation Assistants
/*============================================================================*/    
    private String getByteSize(long size){
        double total;
        String suffix;
        if(size < KB){
            suffix = " B";
            total = (double)size;
	}else if(size < MB){
            suffix = " KB";
            total = (double)size / KB;
        }else if(size < GB){
            suffix = " MB";
            total = (double)size / MB;
        }else if(size < TB){
            suffix = " GB";
            total = (double)size / GB;
        }else{
            suffix = " TB";
            total = (double)size / TB; 
        }
		
        return twoDecimals(new BigDecimal(total), suffix);
    }
    
    private String twoDecimals(BigDecimal d, String suffix){
        String decimal = d.toPlainString();
        int pos = decimal.indexOf('.') + 3;
        if(pos > decimal.length())  pos = decimal.length();
        return decimal.substring(0, pos) + suffix;
    }
    
    
//  Extension Name Column Creation
/*============================================================================*/    
    private TableColumn extNameColumn(){       
        TableColumn<Map.Entry<String, Extension>, String> col = new TableColumn<>("Extension");
        col.setCellValueFactory(
            (TableColumn.CellDataFeatures<Map.Entry<String, Extension>, String> p) -> 
                new ReadOnlyStringWrapper("." + p.getValue().getKey())
        );
        
        return col;
    }
  
//  Extension Total Size Column Creation
/*============================================================================*/     
    private TableColumn extSizeColumn(){       
        TableColumn<Map.Entry<String, Extension>, String> col = new TableColumn<>("Size Total");
        col.setCellValueFactory(
            (TableColumn.CellDataFeatures<Map.Entry<String, Extension>, String> p) ->{ 
                return new ReadOnlyStringWrapper(
                    getByteSize(p.getValue().getValue().getSize())
                );
            }
        );
        col.setComparator(new sizeComparator());
        return col;
    }
    
//  Extension Total Count Column Creation
/*============================================================================*/     
    private TableColumn extCountColumn(){       
        TableColumn<Map.Entry<String, Extension>, String> col = new TableColumn<>("Items");
        col.setCellValueFactory(
            (TableColumn.CellDataFeatures<Map.Entry<String, Extension>, String> p) ->{ 
                return new ReadOnlyStringWrapper(
                    "" + p.getValue().getValue().getCount()
                );
            }
        );
        col.setComparator(new longComparator());
        return col;
    }
    
    
    
    
    
    private JPanel addSwingNode(){
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JLabel n = new JLabel("testing SwingNode north");
        JLabel s = new JLabel("testing SwingNode south");
        JLabel w = new JLabel("testing SwingNode west");
        JLabel e = new JLabel("testing SwingNode east");
        JLabel c = new JLabel("testing SwingNode center");
        p.add(c, BorderLayout.CENTER);
        p.add(n, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        p.add(w, BorderLayout.WEST);
        p.add(e, BorderLayout.EAST);
        return p;
    }
    
}

class FileItem{
    final static String noExt = "<no extension>";
    public static long getSize(String startPath, Map<String, Long> dirs, Map<String, Long> files) throws IOException{
        final AtomicLong size = new AtomicLong(0);
        final AtomicLong subdirs = new AtomicLong(0);
        final AtomicLong fs = new AtomicLong(0);
        final File f = new File(startPath);
        final String str = "";
        Path path = Paths.get(startPath);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                subdirs.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fs.incrementAndGet();
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                fs.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }
        });
        if(subdirs.decrementAndGet() == -1)
            subdirs.incrementAndGet();

        if(f.isDirectory()){
            dirs.put(startPath, subdirs.get());
            files.put(startPath, fs.get());
        }
        return size.get();
    }
    
    public static void getExtensions(String startPath, Map<String, Extension> exts) throws IOException{
        final AtomicReference<String> extension = new AtomicReference<>("");
        final File f = new File(startPath);
        final String str = "";
        Path path = Paths.get(startPath);

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                storageCache.put(file.toAbsolutePath().toString(), attrs.size());
                extension.set(
                        FilenameUtils.getExtension(file.toAbsolutePath().toString())
                );
                
                if(extension.get().equals(str)){
                    if(exts.containsKey(noExt)){
                        exts.get(noExt).countIncrement();
                        exts.get(noExt).increaseSize(attrs.size());
                    }
                    else{
                        exts.put(noExt, new Extension(new AtomicLong(1), new AtomicLong(attrs.size())));
                    }
                }
                else{

                    if(exts.containsKey(extension.get())){
                        exts.get(extension.get()).countIncrement();
                        exts.get(extension.get()).increaseSize(attrs.size());
                    }
                    else{
                        exts.put(extension.get(), new Extension(new AtomicLong(1), new AtomicLong(attrs.size())));
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

class Extension{
    final private AtomicLong count, size;
    public Extension(){
        this.count = new AtomicLong(0);
        this.size = new AtomicLong(0);
    }
    public Extension(AtomicLong cnt, AtomicLong sz){
        this.count = cnt;
        this.size = sz;
    }
    
    public void countIncrement(){
        this.count.incrementAndGet();
    }
    
    public void increaseSize(long sz){
        this.size.addAndGet(sz);
    }
    
    public long getCount(){
        return this.count.get();
    }
    
    public long getSize(){
        return this.size.get();
    }
}
