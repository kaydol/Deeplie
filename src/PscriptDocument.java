import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class PscriptDocument extends DefaultStyledDocument  {

	private static final long serialVersionUID = -1382953602866405059L;
	
	private DefaultStyledDocument doc;
	    private Element rootElement;
	    private MutableAttributeSet normal, keyword, comment, quote, searchQuery, searchQueryNoBackground;
	    private Set<String> keywords;

	    public PscriptDocument() {
	        doc = this;
	        rootElement = doc.getDefaultRootElement();
	        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

	        normal = new SimpleAttributeSet();
	        StyleConstants.setForeground(normal, Color.black);

	        comment = new SimpleAttributeSet();
	        StyleConstants.setForeground(comment, new Color(0, 128, 0));
	        StyleConstants.setBold(comment, true);
	        //StyleConstants.setItalic(comment, true);

	        keyword = new SimpleAttributeSet();
	        StyleConstants.setForeground(keyword, new Color(0, 64, 128));
	        //StyleConstants.setBold(keyword, true);
	        
	        quote = new SimpleAttributeSet();
	        StyleConstants.setForeground(quote, Color.red);

	        searchQuery = new SimpleAttributeSet();
	        StyleConstants.setBackground(searchQuery, new Color(155, 255, 155));
	        
	        searchQueryNoBackground = new SimpleAttributeSet();
	        StyleConstants.setBackground(searchQueryNoBackground, new Color(255, 255, 255));
	        
	        
	        keywords = new HashSet<String>();
	        Syntax pscript = new Syntax();
	        for (Command c : pscript.commands)
	        	keywords.add(c.getName());
	        
	        keywords.add("$Playerrace");
	        keywords.add("$PLAYERRACE");
	        keywords.add("$PLAYERNAME");
	        keywords.add("BEGINNING");
	        keywords.add("END");
	    }

	    /*
	     *  Override to apply syntax highlighting after the document has been updated
	     */
	    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException
	    {
	        super.insertString(offset, str, a);
	        processChangedLines(offset, str.length());
	    }

	    /*
	     *  Override to apply syntax highlighting after the document has been updated
	     */
	    public void remove(int offset, int length) throws BadLocationException
	    {
	    	super.remove(offset, length);
	    	processChangedLines(offset, 0);
	    }

	    /*
	     *  Determine how many lines have been changed,
	     *  then apply highlighting to each line
	     */
	    public void processChangedLines(int offset, int length) throws BadLocationException
	    {
	        String content = doc.getText(0, doc.getLength());

	        //  The lines affected by the latest document update

	        int startLine = rootElement.getElementIndex(offset);
	        int endLine = rootElement.getElementIndex(offset + length);

	        if (startLine > endLine)
	            startLine = endLine;

	        //  Do the actual highlighting

	        for (int i = startLine; i <= endLine; i++)
	        {
	            applyHighlighting(content, i);
	        }

	        
	    }

	    /*
	     *  Parse the line to determine the appropriate highlighting
	     */
	    private void applyHighlighting(String content, int line) throws BadLocationException
	    {
	        int startOffset = rootElement.getElement( line ).getStartOffset();
	        int endOffset = rootElement.getElement( line ).getEndOffset() - 1;

	        int lineLength = endOffset - startOffset;
	        int contentLength = content.length();

	        if (endOffset >= contentLength)
	            endOffset = contentLength - 1;

	        //  set normal attributes for the line
	        doc.setCharacterAttributes(startOffset, lineLength, normal, true);

	        //  check for single line comment
	        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
	        if ( (index > -1) && (index < endOffset) )
	        {
	            doc.setCharacterAttributes(index, endOffset - index + 1, comment, false);
	            endOffset = index - 1;
	        }

	        //  check for tokens
	        checkForTokens(content, startOffset, endOffset);
	    }

	    private void checkForTokens(String content, int startOffset, int endOffset)
	    {
	        while (startOffset <= endOffset)
	        {
	        	//  Extract and process the entire token
	        	if (isQuote( content.substring(startOffset, startOffset + 1)))
	                startOffset = getQuoteToken(content, startOffset, endOffset);
	            else
	                startOffset = getOtherToken(content, startOffset, endOffset);
	        }
	    }
	    
	    private boolean isQuote(String character)
	    {
	        String quoteDelimiters = "[";
	        
	        if (quoteDelimiters.indexOf(character) < 0)
	            return false;
	        else
	            return true;
	    }
	    
	    private int getQuoteToken(String content, int startOffset, int endOffset)
	    {
	        String quoteDelimiter = getQuoteEnding(content.substring(startOffset, startOffset + 1));
	        
	        int index;
	        int endOfQuote = startOffset;

	        index = content.indexOf(quoteDelimiter, endOfQuote + 1);
	        
	        if ( (index < 0) || (index > endOffset) ) 
	        	// didn't find the enclosing quote, highlighting the whole line instead
	        	endOfQuote = endOffset;
	        else 
	        	// found the enclosing quote
	        	endOfQuote = index; 
	        
	        doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);

	        return endOfQuote + 1;
	    }
	    
	    private int getOtherToken(String content, int startOffset, int endOffset)
	    {
	        int endOfToken = startOffset + 1;
	        if ( isDelimiter( content.substring(startOffset, startOffset + 1) ) )
	        	 return endOfToken;
	        
	        while ( endOfToken <= endOffset )
	        {
	        	if (isDelimiter(content.substring(endOfToken, endOfToken + 1)))
	                break;
	        	endOfToken++;
	        }

	        String token = content.substring(startOffset, endOfToken);
	        
	        if (isKeyword(token))
	        {
	            doc.setCharacterAttributes(startOffset, endOfToken - startOffset, keyword, false);
	        }

	        return endOfToken + 1;
	    }
	    
	    private String getSingleLineDelimiter()
	    {
	        return "#";
	    }

	    private boolean isKeyword(String token)
	    {
	        return keywords.contains(token);
	    }
	
	    private boolean isDelimiter(String character)
	    {
	        String operands = ";:{}()[]+-/%<=>!&|^~*,.";
	    	if (Character.isWhitespace(character.charAt(0)) ||  operands.indexOf(character) != -1)
	            return true;
	        else
	            return false;
	    }
	    
	    private String getQuoteEnding(String opening) {
	    	if (opening.equals("["))
	    		return "]";
	    	return opening;
	    }
	    
	    public void clearSearchQuery() {
	    	SwingUtilities.invokeLater(new Runnable() {
    		    public void run() {
    		    	try {
    		    		doc.setCharacterAttributes(0, doc.getLength(), searchQueryNoBackground, false);
    		    	} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    		    }
    		});
	    }
	    
	    public void highlightSearchQuery(String query) {
	    	try {
	    		String content = doc.getText(0, doc.getLength());
				int currentIndex = 0;
				
				while (content.indexOf(query, currentIndex) > -1) {
					currentIndex = content.indexOf(query, currentIndex);
					doc.setCharacterAttributes(currentIndex, query.length(), searchQuery, false);
					currentIndex += query.length();
				}
				
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	
	    }
	    
}
