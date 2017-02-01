import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;


public class AboutWindow extends JFrame {
	
	private static final long serialVersionUID = -1828611032197262558L;
	
	public AboutWindow() {
		super("About");	
		WindowAdapter exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                MainWindow.aboutWindow = null;
            }
        };
        addWindowListener(exitListener);
        
        
		//////////////////////
		//	Right side		//
		//////////////////////
		
		//Border inner = BorderFactory.createLineBorder(Color.GRAY);
		Border outer = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		
		JPanel background = new JPanel(new BorderLayout());
		background.setBorder(outer);

		JTextArea rightTextBox = new JTextArea();
		rightTextBox.setLineWrap(true);
		rightTextBox.setWrapStyleWord(true);
		JScrollPane scrollpane = new JScrollPane(rightTextBox);
		
		rightTextBox.setFont(new Font("Verdana", Font.PLAIN, 12));
		rightTextBox.setDisabledTextColor(Color.BLACK);
		rightTextBox.setEditable(false);
		
		pushToBox(rightTextBox, "This program was designed for helping writers and script implementators in their tedious work with pscript files.");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "It graphically represents the dialogue trees based on recognized goto and :label keywords. "
				+ "Red node is the start of each tree, it means it doesn't have any goto leading into itself, "
				+ "and that it gonna need a jump to them based on a 'queststage?' command in the beginning of the file.");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "___ Tips ___");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "~ You can drag & drop .pscript files into the program window to open them quickly");
		pushToBox(rightTextBox, "~ Use RightMouseButton and MouseWheel for navigating in the canvas");
		pushToBox(rightTextBox, "~ You can move trees by dragging red nodes with LeftMouseButton");
		pushToBox(rightTextBox, "~ pscript file is just a .txt file with .pscript instead of .txt");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "___ Things that shown as errors ___");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "~ uncapitalized $PLAYERNAME & ENDs in labels and gotos");
		pushToBox(rightTextBox, "~ QuestIDs with spaces\\punctuation in them");
		pushToBox(rightTextBox, "~ writing '>Text' instead of '> Text'");
		pushToBox(rightTextBox, "~ '> Text' with missing :label at the end of the line");
		pushToBox(rightTextBox, "~ having an asterisk in |...|");
		pushToBox(rightTextBox, "~ missed space\\too many spaces after asterisk");
		pushToBox(rightTextBox, "~ no :label after commands that use them, i.e. commands with ? mark");
		pushToBox(rightTextBox, "~ unknown command OR invalid arguments");
		pushToBox(rightTextBox, "~ having a :LabelName without having a [LabelName]");
		pushToBox(rightTextBox, "~ using commands without providing any arguments");
		pushToBox(rightTextBox, "~ missing spaces around ':', at least one space must be present");
		pushToBox(rightTextBox, "~ asterisks that ain't followed by commands");
		pushToBox(rightTextBox, "~ unknown NPC names (all names should be mentioned by aliasname commands)");
		pushToBox(rightTextBox, "~ commands with missed asterisks before them");
		pushToBox(rightTextBox, "~ weird unrecognized words that don't fit the context are marked as 'something wrong at line XXX', make sure the NPCs don't have emty lines in the middle of their dialogues!");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "___ Send your feedback ___");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "Please, send me your feedback and ideas about what errors should also be recognized, as well as undetected mistakes or other problems.");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "Before using the program make sure you're having the latest version.");
		
		//////////////////////
		//	Left side		//
		//////////////////////
		
		JLabel picLabel = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("DeeplieConfused.png"))));
		JPanel left = new JPanel(new BorderLayout());
		
		JTextArea leftTextBox = new JTextArea();
		
		leftTextBox.setLineWrap(true);
		leftTextBox.setWrapStyleWord(true);
		leftTextBox.setFont(new Font("Verdana", Font.PLAIN, 12));
		leftTextBox.setDisabledTextColor(Color.BLACK);
		leftTextBox.setEditable(false);
		leftTextBox.setOpaque(false);
		leftTextBox.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 15));
		
		left.add(picLabel, BorderLayout.NORTH);
		left.add(leftTextBox, BorderLayout.CENTER);
		
		pushToBox(leftTextBox, "LoE .pscript Visualiser “Deeplie”");
		pushToBox(leftTextBox, "Creator: Fess");
		pushToBox(leftTextBox, "Version: 1.10"); 
		pushToBox(leftTextBox, ""); 
		pushToBox(leftTextBox, "Poke me on Discord: Fess#2162");
		pushToBox(leftTextBox, "Script Implementation Department"); 
		
		JButton updateButton = new JButton("Download the latest version"); //Download the latest version
		updateButton.addActionListener(new ActionListener() {           
            public void actionPerformed(ActionEvent e) {
            	
				try {
        			openWebpage(new URL("https://drive.google.com/file/d/0B3t6UhN0j39wTkxVX3pSZEtOb3c/view?usp=sharing"));
        		} catch (MalformedURLException e1) {
        			e1.printStackTrace();
        		} catch (Exception e1) {
        			e1.printStackTrace();
        		}
            }       
        });
		
		JPanel buttHolder = new JPanel(new BorderLayout());
		buttHolder.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 15));
		buttHolder.add(updateButton);
		
		
		left.add(buttHolder, BorderLayout.SOUTH);
		
		//////////////////////
		//	Adding			//
		//////////////////////
		
		background.add(left, BorderLayout.WEST);
		background.add(scrollpane, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(background, BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(700, 500));
		pack();
		setLocationRelativeTo(Main.window);
		setVisible(true);
	}
	
	private static void pushToBox(JTextArea area, String str) {
		area.setText(area.getText() + str + System.lineSeparator());
	}
	
	public static void openWebpage(URL url) throws Exception {
		URI uri = url.toURI();
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	    	desktop.browse(uri);
	    }
	}
}
