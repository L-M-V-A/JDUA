/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jduagui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import de.engehausen.treemap.ILabelProvider;
import de.engehausen.treemap.IRectangle;
import de.engehausen.treemap.ISelectionChangeListener;
import de.engehausen.treemap.ITreeModel;
import de.engehausen.treemap.examples.FileInfo;
import de.engehausen.treemap.examples.FileModel;
import de.engehausen.treemap.examples.Messages;
import de.engehausen.treemap.examples.swing.FileViewer;
import de.engehausen.treemap.examples.swing.FilterDialog;
import de.engehausen.treemap.impl.SquarifiedLayout;
import de.engehausen.treemap.swing.TreeMap;
import de.engehausen.treemap.swing.impl.CushionRectangleRendererEx;
/**
 *
 * @author Renegade Hackers: Luis
 */
public class FileTree extends FileViewer{

    protected final TreeMap<FileInfo> treeMap;
    protected final FilterDialog colorDialog;
    final JPanel container;
    protected final JLabel selectionTitle;
    public FileTree(int length, int width, File root){
        container = new JPanel();
        selectionTitle = new JLabel(" ");
        colorDialog = new FilterDialog(this, Messages.getString("fv.colors"));
        treeMap = new TreeMap<>();
        treeMap.setRectangleRenderer(new CushionRectangleRendererEx<>(200));
        treeMap.addSelectionChangeListener(this);
        treeMap.setLabelProvider(this);
        treeMap.setColorProvider(colorDialog);
        treeMap.setTreeMapLayout(new SquarifiedLayout<>(10));
        final Dimension mind = new Dimension(0, 0);
        final Dimension maxd = new Dimension(width, length);
        treeMap.setMinimumSize(mind);
        treeMap.setMaximumSize(maxd);
        container.setLayout(new BorderLayout());
        container.add(treeMap, BorderLayout.CENTER);
        container.add(selectionTitle, BorderLayout.SOUTH);
        pack();
        createTree(root);
        container.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        final Object src = event.getSource();
        if (src.equals(chooseMenu)) {
            //chooseDirectory();
        } else if (src.equals(colorsMenu)) {
            //colorDialog.setVisible(true);
        } else if (src.equals(exitMenu)) {
            dispose();
        }
    }

    @Override
    public void selectionChanged(ITreeModel<IRectangle<FileInfo>> model, IRectangle<FileInfo> rectangle, String text) {
        if (text != null) {
            selectionTitle.setText(text);
            final FileInfo info = rectangle.getNode();
            treeMap.setToolTipText(info.getSize()>0?info.getSizeAsString():null);
        }
    }

    @Override
    public String getLabel(ITreeModel<IRectangle<FileInfo>> model, IRectangle<FileInfo> rectangle) {
        return rectangle.getNode().getName();
    }
    
    @Override
    public void setVisible(final boolean flag) {
    }
    
    public static void main(final String[] args) { }
    
    public JPanel getContent(){
        return container;
    }
    public void destroy(){
        dispose();
    }
    
    private void createTree(File root){
        treeMap.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        treeMap.setTreeModel(FileModel.createFileModel(root.getAbsolutePath()));
        treeMap.repaint();
    }
}
