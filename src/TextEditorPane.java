import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


public class TextEditorPane extends JPanel {
	
	private static final long serialVersionUID = -198635163898972790L;
	private JTextPane icons, lineNumbers, content;
	private JScrollPane iconsScrollPane, lineNumbersScrollPane, contentScrollPane;
	private JTextArea errorDescription;
	private int lines = 0;
	private Font font = new Font("Verdana", Font.PLAIN, 13);
	private ImageIcon errorIcon = new ImageIcon(new ImageIcon("attention.png").getImage().getScaledInstance(17, 17, Image.SCALE_DEFAULT));
	
	public TextEditorPane() {
		
		super(new BorderLayout());

		Border outer = BorderFactory.createEmptyBorder(15, 15, 15, 15);
    	setBorder(outer);
    	
    	icons = new JTextPane();
    	icons.setEditable(false);
    	icons.setFont(font);
    	icons.setOpaque(false);
    	icons.setFocusable(false);
    	iconsScrollPane = new JScrollPane(icons);
    	iconsScrollPane.setBorder(null);
    	iconsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	iconsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER); 
    	iconsScrollPane.setPreferredSize(new Dimension(20, 0)); // TODO use the length of the maximum line number, calculate it in type EventHandler
		
		lineNumbers = new JTextPane();
		lineNumbers.setEditable(false);
		lineNumbers.setFont(font);
		lineNumbers.setOpaque(false);
		lineNumbers.setFocusable(false);
		lineNumbersScrollPane = new JScrollPane(lineNumbers);
		lineNumbersScrollPane.setBorder(null);
		lineNumbersScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		lineNumbersScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER); 
		lineNumbersScrollPane.setPreferredSize(new Dimension(45, 0)); // TODO use the length of the maximum line number, calculate it in type EventHandler
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(iconsScrollPane, BorderLayout.WEST);
		panel.add(lineNumbersScrollPane, BorderLayout.CENTER);
		add(panel, BorderLayout.WEST);
		
		content = new JTextPane();
		content.setFont(font);
		content.setContentType("text/plain");
		content.setDocument(new PscriptDocument());
		
		JPanel lineWrappingFix = new JPanel(new BorderLayout());
		lineWrappingFix.add(content);
		
		contentScrollPane = new JScrollPane(lineWrappingFix);
		contentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		contentScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		contentScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				iconsScrollPane.getVerticalScrollBar().setValue(e.getValue());
				lineNumbersScrollPane.getVerticalScrollBar().setValue(e.getValue());
			}
			
		});
		
		DefaultHighlighter contentHL = new DefaultHighlighter();
		DefaultHighlightPainter contentPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		content.setHighlighter(contentHL);
		
		DefaultHighlighter lineNumbersHL = new DefaultHighlighter();
		DefaultHighlightPainter lineNumbersPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
		lineNumbers.setHighlighter(lineNumbersHL);
		
		content.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				int dot = e.getDot();
			  	try {
					
					int line = getLineOfOffset(content, dot);
					//int positionInLine = dot - getLineStartOffset(content, line);
					
					StyledDocument doc = content.getStyledDocument();
					Element Line = doc.getDefaultRootElement().getElement(line);
					Element NumberLine = lineNumbers.getStyledDocument().getDefaultRootElement().getElement(line);
					//int length = Line.getEndOffset() - Line.getStartOffset();
					
					contentHL.removeAllHighlights();
					lineNumbersHL.removeAllHighlights();
					
					if (Line != null) {
						contentHL.addHighlight(Line.getStartOffset(), Line.getEndOffset(), contentPainter);
						String errorSum = "";
						
						// line + 1 is because we store lines from 0, but we count them from 1 in JTextPane
						if (MainWindow.parser.Errors.containsKey(line + 1))
							for (String errorMsg : MainWindow.parser.Errors.get(line + 1))
								errorSum += errorMsg + " ";
						
						errorDescription.setText(errorSum);
					}
					if (NumberLine != null)
						lineNumbersHL.addHighlight(NumberLine.getStartOffset(), NumberLine.getEndOffset(), lineNumbersPainter);
					
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			  	
			  	iconsScrollPane.getVerticalScrollBar().setValue(contentScrollPane.getVerticalScrollBar().getValue());
			  	lineNumbersScrollPane.getVerticalScrollBar().setValue(contentScrollPane.getVerticalScrollBar().getValue());
				
			}
			
			
			
		});
		
		
		add(contentScrollPane, BorderLayout.CENTER);
		
		StyledDocument doc = content.getStyledDocument();
		doc.addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				// An attribute was changed
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				StyledDocument doc = content.getStyledDocument();
				int linesAfterUpdate = doc.getDefaultRootElement().getElementCount();
				if (linesAfterUpdate != lines)
					setLineNumbers(linesAfterUpdate);
				((MainWindow) content.getTopLevelAncestor()).unsavedChanges(true);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				StyledDocument doc = content.getStyledDocument();
				int linesAfterUpdate = doc.getDefaultRootElement().getElementCount();
				if (linesAfterUpdate != lines)
					setLineNumbers(linesAfterUpdate);
				((MainWindow) content.getTopLevelAncestor()).unsavedChanges(true);
			}
		});
		
		errorDescription = new JTextArea();
		errorDescription.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
		errorDescription.setFont(MainWindow.consoleFont);
		errorDescription.setEditable(false);
		errorDescription.setOpaque(false);
		errorDescription.setRows(1);
		add(errorDescription, BorderLayout.SOUTH);
	}
	
	public void loadText(List<String> text) {
		
		int scrollBarPos = contentScrollPane.getVerticalScrollBar().getValue();
		content.setText("");
		
		StyledDocument doc = content.getStyledDocument();
	    try {
        	for (int i = 0; i < text.size(); i++) {
        		doc.insertString(doc.getLength(), text.get(i) + "\n", null);
        	}
        } 
	    catch (BadLocationException e) {
        	e.printStackTrace();
        }
	    
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	contentScrollPane.getVerticalScrollBar().setValue(scrollBarPos);
		    }
		});
	}
	
	public void writeToFile() {
		
		List<String> lastExport = new ArrayList<String>();
		for (String s : content.getText().split("\n"))
			lastExport.add(s);
		
		try {
			Files.write(MainWindow.LastLoadedFile.toPath(), lastExport, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MainWindow.parser.setText(lastExport);
		MainWindow.updateState(false);
		
		setLineNumbers(lines); // TODO I use this just to update pics
	}

	
	
	private void setLineNumbers(int count) {
		
		icons.setText("");
		
		StyledDocument doc_icons = icons.getStyledDocument();
		StyledDocument doc_numbers = lineNumbers.getStyledDocument();
		
		Style icon = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setIcon(icon, errorIcon);
        
		Style rightAlign = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setAlignment(rightAlign, StyleConstants.ALIGN_RIGHT);
		
		
		try {
			for (int i = 1; i < count + 1; ++i) {
				if (MainWindow.parser.Errors.containsKey(i))
					doc_icons.insertString(doc_icons.getLength(), "\n", icon);
				else
					doc_icons.insertString(doc_icons.getLength(), "\n", null);
			}
			
			if (count > lines) {
				// some lines were added
				while (count > lines)
					doc_numbers.insertString(doc_numbers.getLength(), addLeftSpaces(count, ++lines) + '\n', null);
			
			} else {
				// some lines were removed
				// TODO find a better way to remove text from JTextPane
				lineNumbers.setText("");
				for (int i = 1; i < count + 1; ++i) 
					doc_numbers.insertString(doc_numbers.getLength(), addLeftSpaces(count, i) + '\n', null);
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		lines = count;
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	iconsScrollPane.getVerticalScrollBar().setValue(contentScrollPane.getVerticalScrollBar().getValue());
				lineNumbersScrollPane.getVerticalScrollBar().setValue(contentScrollPane.getVerticalScrollBar().getValue());
		    }
		});
	}
	
	private static String addLeftSpaces(int maximum, int current) {

		int maxLength = 4;
		int len = Integer.toString(current).length();
		String spaces = "";
		
		int i = 0;
		while (len + i++ <= maxLength)
			spaces += " ";
		
		return spaces + Integer.toString(current);
	}
	
	// ###############

	static int getLineOfOffset(JTextComponent comp, int offset) throws BadLocationException {
	    Document doc = comp.getDocument();
	    if (offset < 0) {
	        throw new BadLocationException("Can't translate offset to line", -1);
	    } else if (offset > doc.getLength()) {
	        throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
	    } else {
	        Element map = doc.getDefaultRootElement();
	        return map.getElementIndex(offset);
	    }
	}

	static int getLineStartOffset(JTextComponent comp, int line) throws BadLocationException {
	    Element map = comp.getDocument().getDefaultRootElement();
	    if (line < 0) {
	        throw new BadLocationException("Negative line", -1);
	    } else if (line >= map.getElementCount()) {
	        throw new BadLocationException("No such line", comp.getDocument().getLength() + 1);
	    } else {
	        Element lineElem = map.getElement(line);
	        return lineElem.getStartOffset();
	    }
	}
}
