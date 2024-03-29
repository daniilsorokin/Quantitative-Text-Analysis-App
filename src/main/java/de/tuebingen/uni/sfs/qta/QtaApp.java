package de.tuebingen.uni.sfs.qta;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * @author Daniil Sorokin<daniil.sorokin@uni-tuebingen.de>
 */
public class QtaApp extends JFrame implements ActionListener
{
    
    public static final String LOGGER_NAME = Logger.GLOBAL_LOGGER_NAME + "." + QtaApp.class.getName() + ".Logger";
    private static final Logger logger = Logger.getLogger(LOGGER_NAME);
    private static final String LOGGER_ENCODING = "UTF-8";
    
    public static void main( String[] args )
    {
        try {
            FileHandler fh = new FileHandler("log.xml");
            fh.setEncoding(LOGGER_ENCODING);
            fh.setLevel(Level.ALL);
            logger.addHandler(fh);            
        } catch (IOException ex) {
            Logger.getLogger(QtaApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(QtaApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        QtaApp app = new QtaApp();
        app.initGUIComponents();
        app.setVisible(true);
        try {
            File conf = new File("qta.conf");
            if (conf.exists()) {
                readParametersFromFile(conf);
            } else {
                String altLocation = QtaApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                altLocation = altLocation.substring(0, altLocation.lastIndexOf("/")) + "/qta.conf";
                conf = new File(altLocation);
                if (conf.exists()) readParametersFromFile(conf);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error while reading config file. Proceed with default settings.");
            logger.log(Level.SEVERE, null, ex);
        }
        TreeTaggerResource.INSTANCE.getClass();
    }

    private static void readParametersFromFile(File file) throws IOException, FileNotFoundException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(file);
        props.load(fis);
        if (props.containsKey("treetagger.location")){
            String ttFolder = props.getProperty("treetagger.location");
            System.setProperty("treetagger.home", ttFolder);
        }
    }
    
    private JTable resultsTable;
    private JButton btnBrowse, btnSave;
    private JFileChooser fcInput, fcOutput;
    private FileFilter ffInputTXT, ffInputDOCX, ffOutputCSV, ffOutputXLSX;
    
    private void initGUIComponents() {
        this.setResizable(true);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(600, 200));
        this.setTitle("QTA");
        this.setLocationRelativeTo(null);
        
        fcInput = new JFileChooser();
        fcInput.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ffInputTXT = new FileNameExtensionFilter("Simple text (*.txt)", "txt");
        ffInputDOCX = new FileNameExtensionFilter("Microsoft Word 2007 and later (*.docx)", "docx");
        fcInput.addChoosableFileFilter(ffInputTXT);
        fcInput.addChoosableFileFilter(ffInputDOCX);
        fcInput.setAcceptAllFileFilterUsed(false);
        
        fcOutput = new JFileChooser();
        fcOutput.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ffOutputCSV = new FileNameExtensionFilter("Comma separated values (*.csv)", "csv");
        ffOutputXLSX = new FileNameExtensionFilter("Microsoft Excel 2007 and later (*.xlsx)", "xlsx");
        fcOutput.addChoosableFileFilter(ffOutputCSV);
        fcOutput.addChoosableFileFilter(ffOutputXLSX);
        fcOutput.setAcceptAllFileFilterUsed(false);
        
        // the main panel
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());
        
        
        JPanel fileSelectionPane = new JPanel();
        fileSelectionPane.setLayout(new FlowLayout(FlowLayout.LEADING));
        
        btnBrowse = new JButton("Open file and analyse");
        fileSelectionPane.add(btnBrowse);
        btnBrowse.addActionListener(this);
       
        btnSave = new JButton("Save table to file");
        btnSave.addActionListener(this);
        btnSave.setEnabled(false);
        fileSelectionPane.add(btnSave);
        
        fileSelectionPane.add(new Box.Filler(null, null, null));
                
        JScrollPane tableScrollPanel = new JScrollPane();
        resultsTable = new JTable();
        QtaTableModel tableModel = new QtaTableModel( new String [] {"Word lemma", "Part of speech", "Frequency", "Normalized frequency"}, 0 );
        TableRowSorter<QtaTableModel> sorter = new TableRowSorter<QtaTableModel>(tableModel);
        resultsTable.setModel(tableModel);
        resultsTable.setRowSorter(sorter);
        sorter.toggleSortOrder(3); 
        sorter.toggleSortOrder(3); //Reverse order       

        tableScrollPanel.setViewportView(resultsTable);
        contentPane.add(fileSelectionPane, BorderLayout.NORTH);
        contentPane.add(tableScrollPanel, BorderLayout.CENTER);
        this.pack();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBrowse){
            fcInput.setFileFilter(ffInputTXT);
            int returnVal = fcInput.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String filePath = fcInput.getSelectedFile().getAbsolutePath();                
                try {
                    String text = "";
                    if (fcInput.getFileFilter() == ffInputDOCX)
                        text = IOUtils.getTextFromFile(filePath, SupportedFileTypes.DOCX);
                    else if (fcInput.getFileFilter() == ffInputTXT)
                        text = IOUtils.getTextFromFile(filePath, SupportedFileTypes.TXT);
                    
                    HashMap<Word,Integer> frequencyTable = QTAnalyser.INSTANCE.computeFrequencyList(text);
                    HashMap<Word,Double> normFrequencyTable = QTAnalyser.INSTANCE.computeNormalizedFrequency(frequencyTable);
                    QtaTableModel tableModel = (QtaTableModel) resultsTable.getModel();
                    tableModel.setRowCount(0);
                    for (Word word : frequencyTable.keySet()) {
                        tableModel.addRow(new Object[] {
                            word.getLemma(),
                            word.getPos(),
                            frequencyTable.get(word),
                            normFrequencyTable.get(word)
                        });
                    }
                    btnSave.setEnabled(true);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Can't open the selected file.");
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        } else if (e.getSource() == btnSave){
            fcOutput.setFileFilter(ffOutputCSV);
            int returnVal = fcOutput.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String saveTo = fcOutput.getSelectedFile().getAbsolutePath();
                try {
                    if (fcOutput.getFileFilter() == ffOutputCSV)
                        IOUtils.saveTModelToCSV(saveTo, resultsTable);
                    else if (fcOutput.getFileFilter() == ffOutputXLSX)
                        IOUtils.saveTModelToXlsx(saveTo, resultsTable);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Can't save to the selected file.");
                    logger.log(Level.SEVERE, null, ex);
                }
            }            
        }
    }    
    
    private class QtaTableModel extends DefaultTableModel {
        
        public QtaTableModel(String[] colnames, int numRows){
            super(colnames, numRows);
        }
        
        @Override
        public Class getColumnClass(int column){
            switch(column){
                case 2:
                    return Integer.class;
                case 3:
                    return Double.class;
                case 0:
                case 1:
                default:
                    return String.class;
            }
        }
    }
}
