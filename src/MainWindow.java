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
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainWindow extends JFrame {

	private static String log = "";
	private static final long serialVersionUID = -3880026026104218593L;
	private static JTextArea console;
	private static Canvas canvas;
	private static JTabbedPane tabbedPane;
	private static JScrollPane consoleScrollpane;
	
	public static File LastLoadedFile;
	public static AboutWindow aboutWindow;
	public static AdvancedWindow advancedWindow;
	public static CommandsWindow commandsWindow;
	public static Parser parser = new Parser();
	public static JTextArea errorDescription;
	
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("pscript file (.pscript .txt)", "pscript", "txt");
	
	public static String prefix_error = "   ";
	public static String prefix_example = "         ";
	public static String prefix_info = "";
	public static String prefix_terminating = "";
	public static String ProgramName = "LoE .pscript Visualiser “Deeplie”";
	public static Font menuFont = new Font("Verdana", Font.PLAIN, 12);
	public static Font consoleFont = new Font("Courier New", Font.PLAIN, 15);
	public static TextLineNumber EditorPane;
	
	private static boolean freshlyOpened = true; 
	private static boolean unsavedChanges = false;
	
	
	public MainWindow() {
		
		super(ProgramName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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
    	console.setFont(consoleFont);
    	console.setEditable(false);
    	consoleScrollpane = new JScrollPane(console);
    	//consoleScrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	consoleScrollpane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15), inner));
    	consoleScrollpane.setPreferredSize(new Dimension(400, 200));
    	
    	
    	//////////////////
    	//	Adding		//
    	//////////////////
    	
    	//JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasHolder, consoleScrollpane);
    	
    	JTextPane textPane = new JTextPane();
    	JScrollPane scrollPane = new JScrollPane(textPane);
    	EditorPane = new TextLineNumber(textPane);
    	scrollPane.setRowHeaderView( EditorPane );
    	scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	
    	JPanel EditorWrap = new JPanel(new BorderLayout());
    	EditorWrap.add(scrollPane, BorderLayout.CENTER);
		errorDescription = new JTextArea();
		errorDescription.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		errorDescription.setFont(MainWindow.consoleFont);
		errorDescription.setEditable(false);
		errorDescription.setOpaque(false);
		errorDescription.setRows(1);
		EditorWrap.add(errorDescription, BorderLayout.SOUTH);
		EditorWrap.setBorder(outer);
		
    	ImageIcon editorIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("application_edit.png")));
    	ImageIcon consoleIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("application_xp_terminal.png")));
    	ImageIcon canvasIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("canvas.png")));
    	
    	tabbedPane = new JTabbedPane();
    	tabbedPane.setFont(menuFont);
    	tabbedPane.addTab("Text Editor", editorIcon, EditorWrap);
    	tabbedPane.addTab("Canvas", canvasIcon, canvasHolder);
    	tabbedPane.addTab("Console", consoleIcon, consoleScrollpane);
    	background.add(tabbedPane, BorderLayout.CENTER);
    	
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
							parser.readFromFile(f.getAbsolutePath());
							updateState(true);
						}
						else 
							pushToLog(-1, "Terminating: file should have either .txt or .pscript extension");
					}
					if (files.size() > 1)
						pushToLog(-1, "Info: when you drag & drop more than 1 files, only one is processed");
					
				} catch (UnsupportedFlavorException | IOException  e) {
					pushToLog(-1, "Terminating: Make sure the file exists and uses UTF-8 encoding");
					e.printStackTrace();
				}
				return true; 
			}
    	});
    	
    	add(background);
        setJMenuBar(createMenuBar());
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("DeeplieConfused.png")));
        
		setPreferredSize(new Dimension(900, 600));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static void updateState(boolean reloadEditor) {
		log = "";
		pushToLog(-1, "Input file = " + LastLoadedFile);
		
		for (String s : parser.Log)
			pushToLog(-1, s);
		
		pushToLog(-1, "Lines total : " + parser.getText().keySet().size());
		pushToLog(-1, "Lines with errors : " + parser.Errors.keySet().size());
		
		pushToLog(-1, "");
		for (int i : parser.Errors.keySet()) {
			for (String s : parser.Errors.get(i))
				pushToLog(i, s);
		}
		pushToLog(-1, "");
		
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	consoleScrollpane.getVerticalScrollBar().setValue(0);
		    }
		});
		
		canvas.resetCanvas();
		canvas.setData(parser.getTrees(), parser.getPool());
		if (canvas.isShowing())
			canvas.repaintCanvas(); 
		
		if (reloadEditor) {
			EditorPane.loadText(MainWindow.parser.getText());
			Main.window.unsavedChanges(false);
		}
		
		freshlyOpened = false;
		
	}
	
	public static void pushToLog(int lineNumber, String msg) {
		
		if (msg.trim().startsWith("Terminating:"))
			msg = prefix_terminating + msg;
		if (msg.trim().startsWith("Error:"))
			msg = prefix_error + msg;
		if (msg.trim().startsWith("Info:"))
			msg = prefix_info + msg;
		if (msg.trim().startsWith("Valid"))
			msg = prefix_error + '|' + prefix_example + msg;
		
		if (lineNumber >= 0)
			msg += ", at line " + lineNumber;
		
		log += msg + System.lineSeparator();
		console.setText(log);
	}
	
	public static JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
         
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(menuFont);
        
        JMenuItem item_newFile = new JMenuItem("Create new file ");
        item_newFile.setFont(menuFont);
        fileMenu.add(item_newFile);
        item_newFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	LastLoadedFile = null;
	            	freshlyOpened = true;
	            	parser.clearData();
	            	EditorPane.clearData();
	            	updateState(false);
	            }           
	        });
        item_newFile.setAccelerator(KeyStroke.getKeyStroke("control N"));
        
        
        JMenuItem item_loadFile = new JMenuItem("Load .pscript file ");
        item_loadFile.setFont(menuFont);
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
	                			parser.readFromFile(LastLoadedFile.getAbsolutePath());
								updateState(true);
	                		} catch (IOException e1) {
	                			pushToLog(-1, "Terminating: Make sure the file exists and uses UTF-8 encoding");
	                			e1.printStackTrace();
	                		}
	                    } else
	                    	pushToLog(-1, "Terminating: Input file '" + file.getName() + "' does not exist");
	                }          
	            }           
	        });
        item_loadFile.setAccelerator(KeyStroke.getKeyStroke("control L"));
        
        JMenuItem item_reloadFile = new JMenuItem("Reload file from drive");
        item_reloadFile.setFont(menuFont);
        fileMenu.add(item_reloadFile);
        item_reloadFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (LastLoadedFile == null) {
	            		pushToLog(-1, prefix_terminating + "Terminating: You didn't load any files yet, nothing to reload");
	            		return;
	            	}
	            	if (LastLoadedFile.exists()) {
                		try {
                			parser.readFromFile(LastLoadedFile.getAbsolutePath());
							updateState(true);
                		} catch (IOException e1) {
                			pushToLog(-1, "Terminating: Make sure the file exists and uses UTF-8 encoding");
                			e1.printStackTrace();
                			return;
                		}
                    } else
                    	pushToLog(-1, "Terminating: Input file '" + LastLoadedFile.getName() + "' does not exist");      
	            }
	        });
        item_reloadFile.setAccelerator(KeyStroke.getKeyStroke("control R"));
        
        JMenuItem item_saveToFile = new JMenuItem("Save file to drive");
        item_saveToFile.setFont(MainWindow.menuFont);
        fileMenu.add(item_saveToFile);
        item_saveToFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (LastLoadedFile == null) {
	            		JFileChooser filecreate = new JFileChooser();
	            		filecreate.setCurrentDirectory(new File(System.getProperty("user.dir")));
	            		filecreate.setFileFilter(filter);
		            	int ret = filecreate.showDialog(null, "Create .pscript file");                
		                if (ret == JFileChooser.APPROVE_OPTION) {
		                    File file = filecreate.getSelectedFile();
		                    if (!file.exists()) {
	                			LastLoadedFile = file;
	                			updateState(true);
		                    } else
		                    	pushToLog(-1, "Terminating: File '" + file.getName() + "' already exists");
		                }
	            	}
	            	EditorPane.writeToFile();
	            	Main.window.unsavedChanges(false);		            
	            }
	        });
        item_saveToFile.setAccelerator(KeyStroke.getKeyStroke("control S"));
        
        JMenu advancedMenu = new JMenu("Advanced");
        advancedMenu.setFont(menuFont);
        
        JMenuItem item_openAdvanced = new JMenuItem("Advanced editing");
        item_openAdvanced.setFont(menuFont);
        advancedMenu.add(item_openAdvanced);
        item_openAdvanced.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (advancedWindow == null) {
	            		advancedWindow = new AdvancedWindow();	
	            		return;
	            	}
	            	advancedWindow.setVisible(true);
	            	advancedWindow.toFront(); 
	            }           
	        });
        
        JMenuItem item_openCommands = new JMenuItem("Supported commands");
        item_openCommands.setFont(menuFont);
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
        aboutMenu.setFont(menuFont);
	        JMenuItem item_openAbout = new JMenuItem("Open about");
	        item_openAbout.setFont(menuFont);
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
	
	public static void requestFocusInCanvas() {
		canvas.requestFocusInWindow();
	}
	
	public void unsavedChanges(boolean b) {
		if (LastLoadedFile == null) {
			Main.window.setTitle(ProgramName);
			return;
		}
		if (freshlyOpened) {
			Main.window.setTitle(ProgramName + " | " + LastLoadedFile.getName());
			return;
		} 
		if (unsavedChanges == b)
			return;
		
		unsavedChanges = b;
		
		if (b)
			setTitle(ProgramName + " | *" + LastLoadedFile.getName()); 	//  File has unsaved changes
		else 
			setTitle(ProgramName + " | " + LastLoadedFile.getName()); 	//  File has no unsaved changes
		
	}
}
