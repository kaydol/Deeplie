

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	
	private List<String> text;
	private List<Node> trees;
	private List<Node> pool;
	
	public Parser(String filename) throws IOException {
		readFile(filename);
		performAnalysis();
		buildTree();
	}
	
	public void readFile(String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists())
			MainWindow.pushToLog("  Error: no such file in directory '" + filename + "'");
		text = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
		MainWindow.pushToLog("Info: finished reading file, " + text.size() + " lines total");
	}
	
	public void performAnalysis() {
		String str;
		Matcher m;
		
		Syntax psqript = new Syntax();
		List<String> npc_names = new ArrayList<String>();
		
		for (int i = 0; i < text.size(); ++i) {
			str = text.get(i).trim();
			
			if (str.isEmpty() || str.charAt(0) == '#')
				continue;
			
			m = (Pattern.compile("\\* aliasname <([\\w]+)> <([\\w\\s]+)>")).matcher(str);
			if (m.find()) { 
				npc_names.add(m.group(1));
				npc_names.add(m.group(2));
				continue;
			}
			
			
			// "Text:Text" instead of "Name: Text" or "Text :Label" i.e. : should always has at least space around
			// digits added to prevent shooting at <-1:10> alike constructions, used by hasitem? command
			m = (Pattern.compile("[^\\s\\d]:[^\\s\\d]")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("  Error: at least one space must be present near ':' at line " + (i+1));
				continue;
			}
			
			// ">Text" instead of "> Text"
			m = (Pattern.compile("^>[^\\s]")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("  Error: > should be followed by space at line " + (i+1));
				continue;
			}
			
			// "> Text" with missing label at the end
			m = (Pattern.compile("^>.*")).matcher(str);
			if (m.find()) {
				if (!str.matches(".*:\\w+$")) {
					MainWindow.pushToLog("  Error: missing label at line " + (i+1));
					continue;
				}
			}
			
			// missing command after asterisk
			m = (Pattern.compile("\\*(.+)?")).matcher(str);
			if (m.find() && m.group(1) == null) { 
				MainWindow.pushToLog("  Error: missing command after asterisk at line " + (i+1));
				continue;
			}
			
			// more than 1 space after asterisk 
			m = (Pattern.compile("\\*\\s{2,}")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("  Error: more than 1 space after asterisk at line " + (i+1));
				continue;
			}
			
			// asterisk in ||
			m = (Pattern.compile("\\|.*\\*.*\\|")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("  Error: no asterisk allowed in '" + m.group() + "' at line " + (i+1));
				continue;
			}
			// *command ~ missed space after asterisk
			m = (Pattern.compile("\\*(\\w+\\??)")).matcher(str);
			if (m.find()) {
				MainWindow.pushToLog("  Error: missed space after asterisk in '" + m.group() + "' at line " + (i+1));
				continue;
			}
					
			// no goto label for commands that use them, i.e. commands with ?:  "* commandname? args :label"
			m = (Pattern.compile("\\* (\\w+\\??) .*")).matcher(str);
			if (m.find()) {
				if (psqript.commandExists(m.group(1))) {
					Command c = psqript.findCommand(m.group(1));
					if (c.hasLabel && !str.matches(".*:\\w+$")) {
						MainWindow.pushToLog("  Error: missing label for '" + m.group(1) + "' at line " + (i+1));
						continue;
					}
				}
			}
			
			// unknown command OR invalid arguments
			m = (Pattern.compile("\\* (\\w+\\??)(.+)?(?=:)|\\* (\\w+\\??)(.+)?")).matcher(str);
			if (m.find()) {
				// such a spike was made because hasItem contains ':' in its argument and has a :label at the end 
				// this regexp either goes up to the latest ':' in the line or to the end of the line if none ':' was found
				String str_command, str_args;
				// This 'if' block is used because of '|' operation in RegExp, it either returns groups 1-2 or 3-4
				if (m.group(1) != null) {
					str_command = m.group(1);
					str_args = m.group(2);
				} else {
					str_command = m.group(3);
					str_args = m.group(4);
				}
				
				if (psqript.commandExists(str_command)) {
					Command c = psqript.findCommand(str_command);
					if (str_args == null) {
						MainWindow.pushToLog("  Error: no arguments for '" + str_command + "' at line " + (i+1));
						continue;
					}
					if (str_args.trim().isEmpty() || !c.accepts(str_args.trim())) {
						MainWindow.pushToLog("  Error: invalid argument '" + str_args.trim() + "' for " + str_command + " at line " + (i+1));
						continue;
					}
				} else {
					MainWindow.pushToLog("  Error: unknown command '" + str_command + "' at line " + (i+1));
					continue;
				}
			}
			
			// unknown command OR invalid arguments in optional responses |command args|
			m = (Pattern.compile("\\|(\\w+\\??)( [^\\|]+)?\\|")).matcher(str);
			if (m.find()) {
				if (psqript.commandExists(m.group(1))) {
					Command c = psqript.findCommand(m.group(1));
					if (m.group(2) == null) {
						MainWindow.pushToLog("  Error: no arguments for '" + m.group(1) + "' at line " + (i+1));
						continue;
					}
					if (m.group(2).trim().isEmpty() || !c.accepts(m.group(2).trim())) {
						MainWindow.pushToLog("  Error: invalid argument '" + m.group(2).trim() + "' for " + m.group(1) + " at line " + (i+1));
						continue;
					}
				} else {
					MainWindow.pushToLog("  Error: unknown command '" + m.group(1) + "' at line " + (i+1));
					continue;
				}
			}
			
			// uncapitalized END in goto and :Labels
			m = (Pattern.compile("(?<=goto )[Ee][Nn][Dd]|(?<=:)[Ee][Nn][Dd]")).matcher(str);
			if (m.find() && !m.group().equals("END")) {
				MainWindow.pushToLog("  Error: uncapitalized END at line " + (i+1));
				continue;
			}
			// wrong capitalization in variables
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Nn][Aa][Mm][Ee]")).matcher(str);
			if (m.find() && !m.group().equals("$PLAYERNAME")) {
				MainWindow.pushToLog("  Error: uncapitalized $PLAYERNAME at line " + (i+1));
				continue;
			}
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Gg][Ee][Nn][Dd][Ee][Rr]")).matcher(str);
			if (m.find() && !(m.group().equals("$PLAYERGENDER") || m.group().equals("$Playergender"))) {
				MainWindow.pushToLog("  Error: use either $PLAYERGENDER or $Playergender at line " + (i+1));
				continue;
			}
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Rr][Aa][Cc][Ee]")).matcher(str);
			if (m.find() && !(m.group().equals("$PLAYERRACE") || m.group().equals("$Playerrace"))) {
				MainWindow.pushToLog("  Error: use either $PLAYERRACE or $Playerrace at line " + (i+1));
				continue;
			}
			
			// unknown NPC name
			boolean line_contains_npc_name = false;
			m = (Pattern.compile("^([\\w\\s]+)( \\(\\w+\\))?:.+")).matcher(str);
			if (m.find()) {
				
				line_contains_npc_name = true;
				
				String npc_name = m.group(1);
				//String emotion_name = m.group(2);
				
				if (!npc_names.contains(npc_name)) {
					MainWindow.pushToLog("  Error: if '" + npc_name + "' is an NPC name, it should be mentioned by aliasname command, at line " + (i+1));
					continue;
				}
			}
			
			// suspicious words that passed all previous checks
			m = (Pattern.compile("^\\w+")).matcher(str);
			if (m.find()) {	
				// checking if this a part of NPC phrase: we have to go up to see if there was a speaking NPC
				boolean NPC = line_contains_npc_name;
				if (!line_contains_npc_name) {
					for (int j = i-1; j > 0; --j) {
						String s = text.get(j).trim();
						if (s.isEmpty())
							break;
						if (s.matches("\\[\\w+\\]"))
							break;
						if (s.matches("^[\\w\\s]+( \\(\\w+\\))?:.+")) {
							NPC = true;
							break;
						}
						if (s.matches("[\\w\\s;,\\(\\)\\.!\\?]+"))
							continue;
					}
				}
				if (!NPC) {
					if (psqript.commandExists(m.group()))
						MainWindow.pushToLog("  Error: perhaps an asterisk was missed at line " + (i+1));
					else
						MainWindow.pushToLog("  Error: something is wrong with line " + (i+1));
				}
			}
			
			
		}
		
	}
	
	public void buildTree() {
		
		// Gathering all nodes in one pool
		String label = null;
		List<String> content = new ArrayList<String>();
		trees = new ArrayList<Node>();
		pool = new ArrayList<Node>();
		
		int currentline = 0; // used to detect if we reached the end of the file
		for (String p: text) {
			++currentline;
			p = p.trim();
			if (p.matches("^\\[\\w+\\]") || currentline == text.size()) {
				// reached another node
				if (label != null) {
					pool.add(new Node(label, content));
				} else {
					//shoots only once
					//content here contains the text above the first [label]
				}
				label = p;
				content = new ArrayList<String>();
			}
			content.add(p);
		}
		
		MainWindow.pushToLog("Info: " + pool.size() + " labels have been found");
		if (pool.size() == 0) {
			MainWindow.pushToLog("Info: no labels found, exiting...");
			return;
		}
		
		// Building a structured tree upon given pool
		for(Node node: pool) {
			// Searching for labels in content of each node
			for(String s: node.getContent()) {
				if (s.trim().matches(".+\\s:\\w+$")) {
					// Most likely we reached a label here
					String[] arr = s.split(":");
					label = arr[arr.length - 1];
					String embracedLabel = "[" + label + "]";
					
					// Searching a child with the name=label
					Node child = null;
					for(int i = 0; i < pool.size(); ++i)
						if (pool.get(i).getLabel().equals(embracedLabel))
							child = pool.get(i);
					
					if (child == null) {
						// ERROR: no such label 
						int lineNumber = text.indexOf(s) + 1;
						if (!label.equals("END"))
							MainWindow.pushToLog("  Error: no such label ':" + label + "' at line " + lineNumber);
					} else {
						if (!node.getChildren().contains(child)) {
							child.addFather(node);
							node.addChild(child);
						}
					}
				}
			}
		}
		
		// Counting the number of trees
		for (Node node: pool) {
			if (node.getFathers().size() == 0)
				trees.add(node);
		}
		
		MainWindow.pushToLog("Info: " + trees.size() + " trees have been found");
		MainWindow.pushToLog("Info: parser finished its job");
		
	}
	
	public List<Node> getTrees() {
		return trees;
	}

	public List<String> getText() {
		return text;
	}

	public List<Node> getPool() {
		return pool;
	}

}
