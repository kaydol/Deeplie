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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
	public static ReplaceWindow replaceWindow;
	public static CommandsWindow commandsWindow;
	public static Parser parser = new Parser();
	public static JTextArea errorDescription;
	
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("pscript file (.pscript .txt)", "pscript", "txt");
	private static String[] supportedEncodings = new String[] {"UTF-8", "US-ASCII", "ISO-8859-1"};
	
	private static String prefix_error = "   ";
	private static String prefix_info = "";
	private static String ProgramName = "LoE .pscript Visualiser “Deeplie”";
	private static Font menuFont = new Font("Verdana", Font.PLAIN, 12);
	private static Font consoleFont = new Font("Courier New", Font.PLAIN, 15);
	public static TextLineNumber EditorPane;
	
	private static boolean freshlyOpened = true; 
	private static boolean unsavedChanges = false;
	private static JMenuItem item_undo, item_redo;
	
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
    	
    	JTextPane textPane = new JTextPane();
    	JScrollPane scrollPane = new JScrollPane(textPane);
    	EditorPane = new TextLineNumber(textPane);
    	EditorPane.setEditorFont(consoleFont);
    	scrollPane.setRowHeaderView(EditorPane);
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
		
		//  Icons for tabs
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
						if (filter.accept(f))
							tryToReadFromFile(f, supportedEncodings);
						else 
							JOptionPane.showMessageDialog(null, "File should have either .txt or .pscript extension", "Terminated", JOptionPane.ERROR_MESSAGE);
					}
					if (files.size() > 1)
						JOptionPane.showMessageDialog(null, "When you drag & drop multiple files at once, only the first one is processed", "Some files weren't processed", JOptionPane.INFORMATION_MESSAGE);
        
				} catch (UnsupportedFlavorException | IOException  e) {
					JOptionPane.showMessageDialog(null, "Can't read the file", "Terminated", JOptionPane.ERROR_MESSAGE);
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
		
		//  Loading introduction example
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	try {
					parser.readFromStream(getClass().getResourceAsStream("/Introduction"));
					LastLoadedFile = null;
					updateState(true);
				}
		    	catch (IOException e) {}
		    }
		});
		
		
		
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
		
		if (msg.trim().startsWith("Error:"))
			msg = prefix_error + msg;
		if (msg.trim().startsWith("Info:"))
			msg = prefix_info + msg;
		
		if (lineNumber >= 0)
			msg += ", at line " + lineNumber;
		
		log += msg + System.lineSeparator();
		console.setText(log);
	}
	
	private JMenuBar createMenuBar() {
		
		JMenuBar menuBar = new JMenuBar();
		
		//  Icons for actions
		ImageIcon undoIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("undo.png")));
    	ImageIcon redoIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("redo.png")));
    	ImageIcon saveIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("save.png")));
    	ImageIcon reloadIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("reload.png")));
    	ImageIcon createNewIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("new.png")));
    	ImageIcon loadIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("load.png")));
    	ImageIcon quickFixIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("quick_fix.png")));
    	ImageIcon aboutIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("about.png")));
    	ImageIcon supportedCommandsIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("hint.png")));
    	
    	
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(menuFont);
        
        JMenuItem item_newFile = new JMenuItem("Create new file ");
        item_newFile.setFont(menuFont);
        item_newFile.setIcon(createNewIcon);
        fileMenu.add(item_newFile);
        item_newFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	freshlyOpened = true;
	            	parser.clearData();
	            	EditorPane.clearData();
	            	LastLoadedFile = null;
	            	updateState(false);
	            	unsavedChanges(false);
	            }           
	        });
        item_newFile.setAccelerator(KeyStroke.getKeyStroke("control N"));
        
        
        JMenuItem item_loadFile = new JMenuItem("Open .pscript file ");
        item_loadFile.setIcon(loadIcon);
        item_loadFile.setFont(menuFont);
        fileMenu.add(item_loadFile);
        item_loadFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	JFileChooser fileopen = new JFileChooser();
	            	fileopen.setCurrentDirectory(new File(System.getProperty("user.dir")));
	            	fileopen.setFileFilter(filter);
	            	int ret = fileopen.showDialog(null, "Open .pscript file");                
	                if (ret == JFileChooser.APPROVE_OPTION) {
	                    File file = fileopen.getSelectedFile();
	                    if (file.exists()) {
	                    	tryToReadFromFile(file, supportedEncodings);
	                    } else
	                    	JOptionPane.showMessageDialog(null, "Input file '" + file.getName() + "' does not exist", "Terminated", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	        });
        item_loadFile.setAccelerator(KeyStroke.getKeyStroke("control L"));
        
        JMenuItem item_reloadFile = new JMenuItem("Reload file from drive ");
        item_reloadFile.setIcon(reloadIcon);
        item_reloadFile.setFont(menuFont);
        fileMenu.add(item_reloadFile);
        item_reloadFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (LastLoadedFile == null) {
	            		JOptionPane.showMessageDialog(null, "Can't reload the file :" + System.lineSeparator() + "First you need to Open a file, or Save the current one", "CTRL+R", JOptionPane.INFORMATION_MESSAGE);
	            		return;
	            	}
	            	if (LastLoadedFile.exists()) {
	            		int response = JOptionPane.YES_OPTION;
	            		if (unsavedChanges)
	            			response = JOptionPane.showConfirmDialog(null, "Reload the file from hard drive?" + System.lineSeparator() + "You will lose all unsaved changes.", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	            		if (response == JOptionPane.YES_OPTION)
	            			tryToReadFromFile(LastLoadedFile, supportedEncodings);
                	    else
                	    	return;
	            		
                    } else
                    	JOptionPane.showMessageDialog(null, "Input file '" + LastLoadedFile.getName() + "' does not exist", "Terminated", JOptionPane.ERROR_MESSAGE);
	            }
	        });
        item_reloadFile.setAccelerator(KeyStroke.getKeyStroke("control R"));
        
        
        JMenuItem item_saveToFile = new JMenuItem("Save file to drive");
        item_saveToFile.setIcon(saveIcon);
        item_saveToFile.setFont(MainWindow.menuFont);
        fileMenu.add(item_saveToFile);
        item_saveToFile.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (LastLoadedFile == null) {
	            		JFileChooser filecreate = new JFileChooser();
	            		filecreate.setSelectedFile(new File("New.pscript"));
	            		filecreate.setCurrentDirectory(new File(System.getProperty("user.dir")));
	            		filecreate.setFileFilter(filter);
		            	int ret = filecreate.showDialog(null, "Create .pscript file");                
		                if (ret == JFileChooser.APPROVE_OPTION) {
		                    File file = filecreate.getSelectedFile();
		                    //  if the file with the given name doesn't exist, create it
		                    if (!file.exists()) {
		                    	//  this block creates the file or throws an error
	                			try {
									file.createNewFile();
									LastLoadedFile = file;
									updateState(true);
								}
	                			catch (IOException e1) {
	                				//  throw an error if the file wasn't created
	                				LastLoadedFile = null;
	                				JOptionPane.showMessageDialog(null, "File '" + file.getName() + "' wasn't created", "Terminated", JOptionPane.ERROR_MESSAGE);
								}
		                    } else {
		                    	//  if the file with given name already exist, we ask if we should rewrite that file
		                    	int response = JOptionPane.showConfirmDialog(null, "File '" + file.getName() + "' already exists." + System.lineSeparator() + "Rewrite the file? All information in that file will be lost", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			            		if (response == JOptionPane.YES_OPTION) {
			            			//  this block creates the file or throws an error
		                			try {
										file.createNewFile();
										LastLoadedFile = file;
										updateState(true);
									}
		                			catch (IOException e1) {
		                				//  throw an error if the file wasn't created
		                				LastLoadedFile = null;
		                				JOptionPane.showMessageDialog(null, "File '" + file.getName() + "' wasn't created", "Terminated", JOptionPane.ERROR_MESSAGE);
									}
		                	    	
		                	    } else
		                	    	return;
		                    }
		                    
		                }
	            	}
	            	EditorPane.writeToFile();
	            	Main.window.unsavedChanges(false);		            
	            }
	        });
        item_saveToFile.setAccelerator(KeyStroke.getKeyStroke("control S"));
        
        JMenu advancedMenu = new JMenu("Edit");
        advancedMenu.setFont(menuFont);
        
        item_undo = new JMenuItem(EditorPane.undoAction);
        item_undo.setIcon(undoIcon);
        item_undo.setFont(menuFont);
        item_undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        advancedMenu.add(item_undo);
        
        item_redo = new JMenuItem(EditorPane.redoAction);
        item_redo.setIcon(redoIcon);
        item_redo.setFont(menuFont);
        item_redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        advancedMenu.add(item_redo);
        
        JMenuItem item_openAdvanced = new JMenuItem("Quick fix...");
        item_openAdvanced.setIcon(quickFixIcon);
        item_openAdvanced.setFont(menuFont);
        advancedMenu.add(item_openAdvanced);
        item_openAdvanced.addActionListener(new ActionListener() {           
	            public void actionPerformed(ActionEvent e) {
	            	if (replaceWindow == null) {
	            		replaceWindow = new ReplaceWindow();	
	            		return;
	            	}
	            	replaceWindow.setVisible(true);
	            	replaceWindow.toFront();
	            }           
	        });
        
        JMenu aboutMenu = new JMenu("About");
        aboutMenu.setFont(menuFont);
        JMenuItem item_openAbout = new JMenuItem("Open about");
        item_openAbout.setIcon(aboutIcon);
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
        JMenuItem item_openCommands = new JMenuItem("Supported commands");
        item_openCommands.setIcon(supportedCommandsIcon);
        item_openCommands.setFont(menuFont);
        aboutMenu.add(item_openCommands);
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
	
    public static void refreshControls() {
    	item_undo.setEnabled(EditorPane.undoManager.canUndo());
    	item_redo.setEnabled(EditorPane.undoManager.canRedo());
    }
    
    private static void tryToReadFromFile(File file, String[] encodings) {

    	boolean successful_read = false;
    	for (String encoding : encodings) {
    		try {
    			//  Attempting to read the file using given encoding
    			parser.readFromFile(file.getAbsolutePath(), encoding);
    			LastLoadedFile = file;
    			updateState(true);
    			successful_read = true;
    			if (encoding != "UTF-8") 
    				JOptionPane.showMessageDialog(null, 
    						"File is not in UTF-8 encoding, but contains special symbols." + System.lineSeparator() + 
    						"Those symbols might be displayed incorrectly (and appear as squares)." + System.lineSeparator() + System.lineSeparator() +
    						"To avoid broken symbols just copy all text from the file and paste it" + System.lineSeparator() +
    						"directly into Deeplie's Text Editor, then save the file via CTRL+S." + System.lineSeparator() +
    						"This will change the encoding to recommended UTF-8."
    					, "Your encoding is " + encoding, JOptionPane.INFORMATION_MESSAGE);
    			break;
    		}
    		catch (IOException e) {}
    	}
    	
    	if (!successful_read)
    		JOptionPane.showMessageDialog(null, "The encoding of given file is not supported and can't be read", "Terminated", JOptionPane.ERROR_MESSAGE);
    		
    }
}
