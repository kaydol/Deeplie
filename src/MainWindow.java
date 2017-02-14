import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainWindow extends JFrame {

	private static String log = "";
	private static final long serialVersionUID = -3880026026104218593L;
	private static JTextArea console;
	private static Canvas canvas;
	
	public static File LastLoadedFile;
	public static AboutWindow aboutWindow;
	public static AdvancedWindow advancedWindow;
	public static CommandsWindow commandsWindow;
	private static FileNameExtensionFilter filter;
	
	public static String prefix_error = "   ";
	public static String prefix_example = "         ";
	public static String prefix_info = "";
	public static String prefix_terminating = "";
	
	public static String ProgramName = "LoE .pscript Visualiser “Deeplie”";
	
	public MainWindow() {
		
		super(ProgramName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		filter = new FileNameExtensionFilter("pscript file (.pscript .txt)", "pscript", "txt");
		
    	//////////////////
    	//	Content		//
    	//////////////////
		
		Border inner = BorderFactory.createLineBorder(Color.GRAY);
    	Border outer = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    	
    	JPanel background = new JPanel(new BorderLayout());

    	canvas = new Canvas();
    	JPanel canvasHolder = new JPanel(new BorderLayout());
    	canvasHolder.add(canvas);
    	canvasHolder.setBorder(BorderFactory.createCompoundBorder(outer, inner));
    	canvasHolder.setPreferredSize(new Dimension(400, 400));
    	
    	console = new JTextArea();
    	console.setRows(15);
    	console.setLineWrap(true);
    	console.setWrapStyleWord(true);
    	console.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    	console.setFont(new Font("Verdana", Font.PLAIN, 13));
    	JScrollPane consoleScrollpane = new JScrollPane(console);
    	//consoleScrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	consoleScrollpane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15), inner));
    	consoleScrollpane.setPreferredSize(new Dimension(400, 200));
    	
    	
    	//////////////////
    	//	Adding		//
    	//////////////////
    	
    	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasHolder, consoleScrollpane);
    	background.add(splitPane, BorderLayout.CENTER);
    	
    	//background.add(canvasHolder, BorderLayout.CENTER);
    	//background.add(consoleScrollpane, BorderLayout.EAST);
		
    	add(background);
    	
    	JPanel glass = (JPanel) getGlassPane();
	    glass.setVisible(true);
    	glass.setTransferHandler ( new TransferHandler() {
			private static final long serialVersionUID = -3143990072563075139L;
			public int getSourceActions ( JComponent c ) { return TransferHandler.COPY; }
			public boolean canImport ( TransferSupport support ) { return true; }
			public boolean importData(JComponent comp, Transferable t) { 
				try 
				{
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() > 0) {
						File f = files.get(0);
						if (filter.accept(f)) {
							LastLoadedFile = f;
							processFile(f.getAbsolutePath());
						}
						else 
							pushToLog("Terminating: file should have either .txt or .pscript extension");
					}
					if (files.size() > 1)
						pushToLog("Info: when you drag&drop more than 1 files, only one is processed");
					
				} catch (UnsupportedFlavorException | IOException  e) {
					pushToLog("Terminating: Make sure the file exists and uses UTF-8 encoding");
					e.printStackTrace();
				}
				return true; 
			}
    	});
        setJMenuBar(createMenuBar());
    	
        
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("DeeplieConfused.png")));
        
		setPreferredSize(new Dimension(850, 500));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static void processFile(String filename) throws IOException {
		log = "";
		canvas.resetCanvas();
		pushToLog("Input file = " + filename);
		Parser parser = new Parser(filename);
		canvas.setData(parser.getTrees(), parser.getPool());
		canvas.repaintCanvas();
		
		Main.window.setTitle(ProgramName + " | " + LastLoadedFile.getName());
	}
	
	public static void pushToLog(String msg) {
		
		if (msg.trim().startsWith("Terminating:"))
			msg = prefix_terminating + msg;
		if (msg.trim().startsWith("Error:"))
			msg = prefix_error + msg;
		if (msg.trim().startsWith("Info:"))
			msg = prefix_info + msg;
		if (msg.trim().startsWith("Valid"))
			msg = prefix_error + '|' + prefix_example + msg;
		
		log += msg + System.lineSeparator();
		console.setText(log);
	}
	
	
	public static JMenuBar createMenuBar() {
		Font font = new Font("Verdana", Font.PLAIN, 12);

        JMenuBar menuBar = new JMenuBar();
         
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(font);
        
        JMenuItem item_loadFile = new JMenuItem("Load .pscript file");
        item_loadFile.setFont(font);
        fileMenu.add(item_loadFile);
        item_loadFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	JFileChooser fileopen = new JFileChooser();
	            	fileopen.setCurrentDirectory(new File(System.getProperty("user.dir")));
	            	fileopen.setFileFilter(filter);
	            	int ret = fileopen.showDialog(null, "Load .pscript file");                
	                if (ret == JFileChooser.APPROVE_OPTION) {
	                    File file = fileopen.getSelectedFile();
	                    if (file.exists()) {
	                		try {
	                			LastLoadedFile = file;
	                			processFile(file.getAbsolutePath());
	                		} catch (IOException e1) {
	                			pushToLog("Terminating: Make sure the file exists and uses UTF-8 encoding");
	                			e1.printStackTrace();
	                		}
	                    } else
	                    	pushToLog("Terminating: Input file '" + file.getName() + "' does not exist");
	                }          
	            }           
	        });
        JMenuItem item_reloadFile = new JMenuItem("Reload file");
        item_reloadFile.setFont(font);
        fileMenu.add(item_reloadFile);
        item_reloadFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (LastLoadedFile == null) {
	            		pushToLog(prefix_terminating + "Terminating: You didn't load any files yet, nothing to reload");
	            		return;
	            	}
	            	if (LastLoadedFile.exists()) {
                		try {
                			processFile(LastLoadedFile.getAbsolutePath());
                		} catch (IOException e1) {
                			pushToLog("Terminating: Make sure the file exists and uses UTF-8 encoding");
                			e1.printStackTrace();
                			return;
                		}
                    } else
                    	pushToLog("Terminating: Input file '" + LastLoadedFile.getName() + "' does not exist");      
	            }
	        });
	       
        
        JMenu advancedMenu = new JMenu("Advanced");
        advancedMenu.setFont(font);
        
        JMenuItem item_openAdvanced = new JMenuItem("Advanced editing");
        item_openAdvanced.setFont(font);
        advancedMenu.add(item_openAdvanced);
        item_openAdvanced.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (LastLoadedFile == null) {
	            		pushToLog("Terminating: You didn't load any files yet, nothing to edit");
	            		return;
	            	}
	            	if (advancedWindow == null) {
	            		advancedWindow = new AdvancedWindow();	
	            		return;
	            	}
	            	advancedWindow.setVisible(true);
	            	advancedWindow.toFront(); 
	            }           
	        });
        
        JMenuItem item_openCommands = new JMenuItem("Supported commands");
        item_openCommands.setFont(font);
        advancedMenu.add(item_openCommands);
        item_openCommands.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (commandsWindow == null) {
	            		commandsWindow = new CommandsWindow();	
	            		return;
	            	}
	            	commandsWindow.setVisible(true);
	            	commandsWindow.toFront(); 
	            }           
	        });
        
        
        JMenu aboutMenu = new JMenu("About");
        aboutMenu.setFont(font);
	        JMenuItem item_openAbout = new JMenuItem("Open about");
	        item_openAbout.setFont(font);
	        aboutMenu.add(item_openAbout);
	        item_openAbout.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (aboutWindow == null) {
	            		aboutWindow = new AboutWindow();
	            		return;
	            	}
	            	aboutWindow.setVisible(true);
	            	aboutWindow.toFront();
	            }           
	        });
        
        menuBar.add(fileMenu); 
        menuBar.add(advancedMenu); 
        menuBar.add(aboutMenu);
        
        return menuBar;
	}
	
	
}
