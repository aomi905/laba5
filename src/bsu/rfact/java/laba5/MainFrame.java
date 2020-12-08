package bsu.rfact.java.laba5;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class MainFrame extends JFrame {
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;
    private JFileChooser fileChooser = null;
    private JMenuItem resetGraphicsMenuItem;
    private GraphicsDisplay display = new GraphicsDisplay();
    private boolean fileLoaded = false;
    private JMenuItem saveToTextMenuItem;
    private DecimalFormat formatter = new DecimalFormat("###.#####");
    private JCheckBoxMenuItem showAxisMenuItem,
            showMarkersMenuItem,
            showGridsMenuItem,
            showRotateMenuItem;

    public MainFrame() {
        super("Plotting function graphs based on prepared files");

        this.setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        this.setLocation((kit.getScreenSize().width - WIDTH) / 2,
                (kit.getScreenSize().height - HEIGHT) / 2);

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Open file") {
            public void actionPerformed(ActionEvent event) {
                if (MainFrame.this.fileChooser == null) {
                    MainFrame.this.fileChooser = new JFileChooser();
                    MainFrame.this.fileChooser.setCurrentDirectory(new File("."));
                }

                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    MainFrame.this.openGraphics(MainFrame.this.fileChooser.getSelectedFile());
                saveToTextMenuItem.setEnabled(true);
            }
        };
        fileMenu.add(openGraphicsAction);

        JMenu graphicsMenu = new JMenu("Graph");
        menuBar.add(graphicsMenu);
        Action showAxisAction = new AbstractAction("Show coordinate axes") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);

        Action showMarkersAction = new AbstractAction("Show point markers") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);

        Action showGridsAction = new AbstractAction("Show grid") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowGrid(showGridsMenuItem.isSelected());
            }
        };
        showGridsMenuItem = new JCheckBoxMenuItem(showGridsAction);
        graphicsMenu.add(showGridsMenuItem);
        showGridsMenuItem.setSelected(true);

        Action showRotateAction = new AbstractAction("Rotate 90 degree to the left") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowRotate(showRotateMenuItem.isSelected());
            }
        };
        showRotateMenuItem = new JCheckBoxMenuItem(showRotateAction);
        graphicsMenu.add(showRotateMenuItem);
        showRotateMenuItem.setSelected(false);

        graphicsMenu.addMenuListener(new GraphicsMenuListener());


        Action saveToTextAction = new AbstractAction("Сохранить в текстовый файл") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showSaveDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION){
                    saveToTextFile(fileChooser.getSelectedFile());
                }
            }
        };
        saveToTextMenuItem = fileMenu.add(saveToTextAction);
        saveToTextMenuItem.setEnabled(false);

        Action resetGraphicsAction = new AbstractAction("Отменить все изменения") {
            public void actionPerformed(ActionEvent event) {
                MainFrame.this.display.reset();
            }
        };
        this.resetGraphicsMenuItem = fileMenu.add(resetGraphicsAction);
        this.resetGraphicsMenuItem.setEnabled(false);
        this.getContentPane().add(this.display, "Center");
    }

    protected void openGraphics(File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            ArrayList graphicsData = new ArrayList(50);

            while(in.available() > 0) {
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData.add(new Double[]{x, y});
            }

            if (graphicsData.size() > 0) {
                this.fileLoaded = true;
                this.resetGraphicsMenuItem.setEnabled(true);
                this.display.displayGraphics(graphicsData);
            }

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(MainFrame.this,
                    "The specified file wasn't found", "Data loading error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(MainFrame.this,
                    "Error in reading point coordinates from file", "Data loading error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    private void saveToTextFile(File selectedFile) {
        try {
            FileWriter writer = new FileWriter(selectedFile);
            ArrayList<Double[]> originalData = display.getGraphicsData();
            Iterator iter = originalData.iterator();

            while(iter.hasNext()) {
                Double[] point = (Double[])iter.next();
                for(int i = 0; i < 2; i++){
                    writer.write(String.valueOf(formatter.format(point[i])));
                    int a = formatter.format(point[i]).toString().length();
                    for (int l = 1; l < (30 - a); l++) {
                        writer.write(" ");
                    }
                }
                writer.write("\n");
            }
            writer.flush();
        }catch (IOException e){
            System.out.println("File couldn't be created");
        }
    }


    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }

    private class GraphicsMenuListener implements MenuListener {
        @Override
        public void menuSelected(MenuEvent e) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            showGridsMenuItem.setEnabled(fileLoaded);
            showRotateMenuItem.setEnabled(fileLoaded);
        }

        @Override
        public void menuDeselected(MenuEvent e) {

        }

        @Override
        public void menuCanceled(MenuEvent e) {

        }
    }
}