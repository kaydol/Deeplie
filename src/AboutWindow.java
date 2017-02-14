import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
		rightTextBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		rightTextBox.setLineWrap(true);
		rightTextBox.setWrapStyleWord(true);
		JScrollPane scrollpane = new JScrollPane(rightTextBox);
		
		rightTextBox.setFont(new Font("Verdana", Font.PLAIN, 12));
		rightTextBox.setDisabledTextColor(Color.BLACK);
		rightTextBox.setEditable(false);
		
		pushToBox(rightTextBox, "This program was designed for helping writers and script implementers in their tedious work with pscript files.");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "It graphically represents all dialogue trees and shows mistakes that need to be fixed in order for game server to execute them right.");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "___ Tips ___");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "~ Hit the `Documentation` button on the left and read what Deeplie has to say");
		pushToBox(rightTextBox, "~ You can drag & drop .pscript files into the program window to open them quickly");
		pushToBox(rightTextBox, "~ Use RightMouseButton and MouseWheel for navigating in the canvas");
		pushToBox(rightTextBox, "~ You can move trees by dragging red nodes with LeftMouseButton");
		pushToBox(rightTextBox, "~ You can move the slider between canvas and text area to enlarge the preferable area");
		pushToBox(rightTextBox, "~ If you made some changes in the file, click `File > Reload file` button to quickly update the state");
		pushToBox(rightTextBox, "~ You can auto-correct some of the common mistakes in `Advanced > Advanced editing` menu.");
		
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "___ Send your feedback ___");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "My Discord is on the left, see `Creator` line. Please, send me your feedback and ideas about what errors should also be recognized, as well as undetected mistakes or other problems.");
		pushToBox(rightTextBox, "");
		pushToBox(rightTextBox, "Before using the program, make sure you're having the latest version. Also, make sure the file has the `UTF-8 (without BOM)` encoding.");
		
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
		pushToBox(leftTextBox, "Creator: Fess#2162");
		pushToBox(leftTextBox, "Version: 1.18"); 
		pushToBox(leftTextBox, ""); 
		pushToBox(leftTextBox, "LoE Script Implementation DEP"); 
		
		
		JButton docsButton = new JButton("Documentation"); //Download the latest version
		docsButton.addActionListener(new ActionListener() {           
            public void actionPerformed(ActionEvent e) {
            	
				try {
        			openWebpage(new URL("https://docs.google.com/document/d/1Lr4vJJkKFg45KI_iaNwiHohFhZ-xJHtX3U2vz3U1rrQ/edit?usp=sharing"));
        		} catch (MalformedURLException e1) {
        			e1.printStackTrace();
        		} catch (Exception e1) {
        			e1.printStackTrace();
        		}
            }       
        });
		
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
		
		
		JPanel docsButtHolder = new JPanel(new BorderLayout());
		docsButtHolder.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 15));
		docsButtHolder.add(docsButton);
		
		JPanel updButtHolder = new JPanel(new BorderLayout());
		updButtHolder.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 15));
		updButtHolder.add(updateButton);
		
		
		JPanel buttsBackground = new JPanel(new GridLayout(2, 1)); 
		buttsBackground.add(docsButtHolder);
		buttsBackground.add(updButtHolder);
		
		
		
		
		left.add(buttsBackground, BorderLayout.SOUTH);
		
		
		//////////////////////
		//	Adding			//
		//////////////////////
		
		background.add(left, BorderLayout.WEST);
		background.add(scrollpane, BorderLayout.CENTER);
		
		
		setLayout(new BorderLayout());
		add(background, BorderLayout.CENTER);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("DeeplieConfused.png")));
		
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
