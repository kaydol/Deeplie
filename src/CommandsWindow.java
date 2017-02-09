import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CommandsWindow extends JFrame {

	private static final long serialVersionUID = 2934003021566753943L;

	public CommandsWindow() {
		
		super("Supported commands");
		WindowAdapter exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                MainWindow.advancedWindow = null;
            }
        };
        addWindowListener(exitListener);
		
		Border inner = BorderFactory.createLineBorder(Color.GRAY);
    	Border outer = BorderFactory.createEmptyBorder(15, 15, 15, 15);
		JPanel background = new JPanel(new BorderLayout());
		Font font = new Font("Verdana", Font.PLAIN, 12);
		
		////////////
		
		JTextArea commandDesc = new JTextArea();
		commandDesc.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		commandDesc.setLineWrap(true);
		commandDesc.setWrapStyleWord(true);
		commandDesc.setFont(new Font("Verdana", Font.PLAIN, 12));
		commandDesc.setDisabledTextColor(Color.BLACK);
		commandDesc.setEditable(false);
		
		JScrollPane commandDescPane = new JScrollPane(commandDesc);
		commandDescPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0), inner));
		
		background.add(commandDescPane, BorderLayout.CENTER);
		
		////////////

		DefaultListModel<String> listModel = new DefaultListModel<String>();
		JList<String> list = new JList<String>(listModel);
		list.setFont(font);
		Syntax syntax = new Syntax();
		for (Command c: syntax.commands) {
			listModel.addElement(c.getName());
		}
		
		list.addListSelectionListener(new ListSelectionListener () {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String text = "Examples:" + System.lineSeparator();
				String[] desc = syntax.commands.get(list.getSelectedIndex()).getExamples();
				for (String p : desc) {
					text += MainWindow.prefix_error + p + System.lineSeparator();
				}
				commandDesc.setText(text);
				
			}
		});
		
		list.setSelectedIndex(0);
		
		list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		background.setBorder(outer);
		background.add(new JScrollPane(list), BorderLayout.WEST);
		
		////////////
		
		add(background);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("DeeplieConfused.png")));
		setPreferredSize(new Dimension(850, 400));
		pack();
		setLocationRelativeTo(Main.window);
		setVisible(true);
	}
	
	
	
}
