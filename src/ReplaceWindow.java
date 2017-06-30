
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class ReplaceWindow extends JFrame {

	private static final long serialVersionUID = 2715564227665762808L;

	public ReplaceWindow() {
		
		super("Quick fix");
		WindowAdapter exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                MainWindow.replaceWindow = null;
            }
        };
        addWindowListener(exitListener);
		
		//Border inner = BorderFactory.createLineBorder(Color.GRAY);
    	Border outer = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		JPanel background = new JPanel(new GridLayout(11,1));
		background.setBorder(outer);
		
		JCheckBox box_doubleSpace = new JCheckBox("Put 2 spaces after the end of each sentence and 1 after commas");
		JCheckBox box_asteriskSpace = new JCheckBox("Put exactly 1 space after each asterisk & questionmark");
		JCheckBox box_speechSpace = new JCheckBox("Put a space before Speech in NPCNAME:Speech");
		JCheckBox box_replySpace = new JCheckBox("Put 1 space after each > symbol in Responses");
		JCheckBox box_capitalize = new JCheckBox("Captialize $PLAYERNAME, BEGINNING & END in labels and gotos");
		JCheckBox box_quotes = new JCheckBox("Replace every ' (apostrophe) with ’");
		JCheckBox box_dots1 = new JCheckBox("Replace each 3 dots (...) with 1 triple dot (…)");
		JCheckBox box_dots2 = new JCheckBox("Replace each triple dot (…) with 3 dots (...)");
		
		Font font = new Font("Verdana", Font.PLAIN, 12);
		box_doubleSpace.setFont(font);
		box_asteriskSpace.setFont(font);
		box_speechSpace.setFont(font);
		box_replySpace.setFont(font);
		box_capitalize.setFont(font);
		box_quotes.setFont(font);
		box_dots1.setFont(font);
		box_dots2.setFont(font);
		
		
		JButton commitButton = new JButton("Commit changes to Text Editor");
		commitButton.addActionListener(new ActionListener() {           
            public void actionPerformed(ActionEvent e) {
            
            	List<String> text = MainWindow.EditorPane.getEditorContent();
				
            	for (int i = 0; i < text.size(); ++i) {
            		String s = text.get(i).trim();
            		if (s.isEmpty())
            			continue;
            		
            		if (box_doubleSpace.isSelected() && s.charAt(0) != '*') {
            			s = s.replaceAll("([!?.])([^\\s\\.])", "$1  $2");
            			s = s.replaceAll(",([^\\s])", ",  $1");
            		}
            		if (box_asteriskSpace.isSelected()) {
            			s = s.replaceAll("\\*([^\\s])", "* $1"); // putting one space if none found
            			s = s.replaceAll("\\* \\s+(.+)", "* $1"); // deleting excess spaces if more than 1 space found
            			s = s.replaceAll("\\?([^\\s])", "? $1"); // putting one space if none found
            			s = s.replaceAll("\\? \\s+(.+)", "? $1"); // deleting excess spaces if more than 1 space found
            		}
            		if (box_speechSpace.isSelected())
            			s = s.replaceAll("^(\\w+):([^\\s])", "$1: $2");
            		if (box_replySpace.isSelected())
            			s = s.replaceAll("^>([^\\s])", "> $1");
            		if (box_capitalize.isSelected()) {
            			s = s.replaceAll("(?<=goto )[Bb][Ee][Gg][Ii][Nn][Nn][Ii][Nn][Gg]|(?<=:)[Bb][Ee][Gg][Ii][Nn][Nn][Ii][Nn][Gg]", "BEGINNING");
            			s = s.replaceAll("(?<=goto )[Ee][Nn][Dd]|(?<=:)[Ee][Nn][Dd]", "END");
            			s = s.replaceAll("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Nn][Aa][Mm][Ee]", "\\$PLAYERNAME");
            		}
            		if (box_quotes.isSelected())
            			s = s.replaceAll("'", "’");
            		if (box_dots1.isSelected())
            			s = s.replaceAll("\\.\\.\\.", "…");
            		if (box_dots2.isSelected())
            			s = s.replaceAll("…", "...");
            		
            		text.set(i, s);
            	}
            	
            	MainWindow.EditorPane.loadText(text);
            	Main.window.unsavedChanges(true);
            	
            }           
        });
		
		background.add(new JLabel("Select what you want to fix :"));
		background.add(box_doubleSpace);
		background.add(box_asteriskSpace);
		background.add(box_speechSpace);
		background.add(box_replySpace);
		background.add(box_capitalize);
		background.add(box_quotes);
		background.add(box_dots1);
		background.add(box_dots2);
		background.add(new JLabel(""));
		
		
		JPanel buttHolder = new JPanel(new BorderLayout());
		buttHolder.setBorder(BorderFactory.createEmptyBorder(0, 80, 0, 80));
		buttHolder.add(commitButton, BorderLayout.CENTER);
		
		background.add(buttHolder);
		
		add(background);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("DeeplieConfused.png")));
		setPreferredSize(new Dimension(480, 360));
		pack();
		setLocationRelativeTo(Main.window);
		
		setVisible(true);
	}
	
}
