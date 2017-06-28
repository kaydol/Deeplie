

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

public class Parser {
	
	private HashMap<Integer, String> textWithLines;
	private List<Node> trees;
	private List<Node> pool;
	
	private Syntax pscript = new Syntax();
	private HashSet<String> DefinedLabels = new HashSet<String>();
	private HashSet<String> Activated_QuestIDs = new HashSet<String>();
	private HashSet<String> Completed_QuestIDs = new HashSet<String>();
	private HashSet<String> Activated_Objectives = new LinkedHashSet<String>();
	private HashSet<String> Completed_Objectives = new HashSet<String>();
	private LinkedHashSet<Integer> Quest_Stages = new LinkedHashSet<Integer>();
	private LinkedHashSet<String> Quest_IDs = new LinkedHashSet<String>();
	private LinkedHashSet<String> MentionedNPCs = new LinkedHashSet<String>();
	
	private Pattern ValidSpeechName, ValidSpeechText, ValidCommand, ValidCondition, ValidLabel;
	
	public TreeMap<Integer, List<String>> Errors =  new TreeMap<Integer, List<String>>();
	public TreeMap<Integer, String> ToDos = new TreeMap<Integer, String>();
	
	public List<String> Log = new ArrayList<String>();
	
	public Parser() {
		
		ValidSpeechName = Pattern.compile("^([\\w\\s]+)( \\(\\w+\\))?"); // Name (emotion)
		ValidSpeechText = Pattern.compile("[^:#]+"); // blacklisted symbols
		ValidCommand = Pattern.compile("[\\w\\?\\s:<>=\\-]+"); // \w ? \s : <>=- 
		ValidCondition = Pattern.compile("[\\w\\?\\s:<>=\\-\\|^&]+");
		ValidLabel = Pattern.compile("^\\[\\w+\\]");
		
	}
	
	public void clearData() {
		Log.clear();
		Errors.clear();
		DefinedLabels.clear();
		Activated_QuestIDs.clear();
		Completed_QuestIDs.clear();
		Activated_Objectives.clear();
		Completed_Objectives.clear();
		Quest_Stages.clear();
		Quest_IDs.clear();
		MentionedNPCs.clear();
	}
	
	public static HashMap<Integer, String> addLinesToText (List<String> text) {
		
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		
		for (int i = 0; i < text.size(); ++i)
			result.put(i+1, text.get(i));
		
		return result;
	}
	
	
	public void readFromFile(String filename) throws IOException {
		
		File file = new File(filename);	
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null, "No such file in directory '" + filename + "'", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		List<String> text = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
		setText(text);
	}
	
	public void setText(List<String> text) {
		clearData();
		textWithLines = addLinesToText(text);
		buildTree();
		performAnalysis();
	}
	
	private void addError(int lineNumber, String description) {
		
		if (!Errors.containsKey(lineNumber))
			Errors.put(lineNumber, new ArrayList<String>());
		
		Errors.get(lineNumber).add(description);
	}
	
	private void performAnalysis() {
		String str;
		Matcher m;

		HashSet<String> npc_names = new HashSet<String>();

		// This message repeats a lot, so I put in a variable
		String errInappropriateSymbol = "Error: inappropriate symbol(s)";
		
		for (int lineNumber : textWithLines.keySet()) {
			
			str = textWithLines.get(lineNumber).trim();
			
			if (str.matches(".*#\\s*TODO.*"))
				ToDos.put(lineNumber, str);
			
			// Remove all comments from the line
			str = str.replaceAll("#.*", "").trim();
			
			// Saving the current label to allow aliasname only before the first label
			String currentLabel = "";
			if (str.matches(ValidLabel.pattern()))
				currentLabel = str;
			
			if (str.isEmpty())
				continue;
			
			if (str.startsWith("*") && !str.matches("\\*" + ValidCommand.pattern())) {
				
				if (str.matches("\\*\\s*runscript .+") || str.matches("\\*\\s*f .+") || str.matches("\\*\\s*playsound .+") || str.matches("\\*\\s*spawnmob .+")) {
					// These commands may have some very rare symbols in their arguments, and I don't want to 
					// add those very specific symbols into a common whitelist used by all other commands
					// Instead, those symbols will be treated by the regular expression of those commands 
				}
				else {
					addError(lineNumber, errInappropriateSymbol);
					continue;
				}
			}
			if (str.startsWith("?") && !str.matches("\\?" + ValidCondition.pattern())) {
				addError(lineNumber, errInappropriateSymbol);
				continue;
			}
			
			
			m = (Pattern.compile("\\* aliasname <([\\w]+)> <([\\w\\s]+)>")).matcher(str);
			if (m.find()) { 
				npc_names.add(m.group(1));
				npc_names.add(m.group(2));
				if (!currentLabel.isEmpty())
					addError(lineNumber, "Error: having 'aliasname' in the middle of the script");
				continue;
			}
			
			// " : "
			m = (Pattern.compile("\\s:\\s")).matcher(str);
			if (m.find()) { 
				addError(lineNumber, "Error: ':' shouldn't be surrounded by two spaces");
				continue;
			}
			
			// "Text:Text" instead of "Name: Text" or "Text :Label" i.e. : should always has at least space around
			// digits added to prevent shooting at <-1:10> alike constructions, used by hasitem? command
			m = (Pattern.compile("[^\\s\\d]:[^\\s\\d]")).matcher(str);
			if (m.find()) { 
				addError(lineNumber, "Error: at least one space must be present near ':'");
				continue;
			}
			
			// ">Text" instead of "> Text"
			m = (Pattern.compile("^\\s*>[^\\s]")).matcher(str);
			if (m.find()) { 
				addError(lineNumber, "Error: > should be followed by space");
				continue;
			}
			
			// "> Text" or "? condition" with missing :Label at the end
			m = (Pattern.compile("^>.*|^\\?.*")).matcher(str);
			if (m.find()) {
				if (!str.matches(".*:\\w+$")) {
					addError(lineNumber, "Error: missing label");
					continue;
				}
			}
			
			// Invalid symbols in Responses, applies the same rules as for NPC Speech
			if (str.startsWith(">")) {
				int beginIndex = str.lastIndexOf('|') + 1;
				if (beginIndex == 0)
					++beginIndex;
				int endIndex = str.lastIndexOf(':');
				if (!str.substring(beginIndex, endIndex).matches(ValidSpeechText.pattern())) {
					addError(lineNumber, "Error: inappropriate symbol(s) in Response");
					continue;
				}
			}
			
			// Checking multi-conditional lines: `? command1 args ([|&^] command2 args...) :Label`
			m = (Pattern.compile("^\\?(.*):\\w+$")).matcher(str);
			if (m.find()) {
				if (!parse(m.group(1), true, true, lineNumber))
					continue;
			}
			
			// Check code between pipelines in Optional Responses
			m = (Pattern.compile("(?<=\\|)(.*)(?=\\|)")).matcher(str);
			if (m.find() && str.startsWith(">")) {
				if (!parse(m.group(1), true, true, lineNumber))
					continue;
			}
			
			// Missed pipe in Optional Responses
			m = (Pattern.compile("\\|(.*\\|)?")).matcher(str);
			if (m.find() && str.startsWith(">") && m.group(1) == null) {
				addError(lineNumber, "Error: second pipe symbol is missing in the Optional Response");
				continue;
			}
			
			
			// Missing command after asterisk\?
			m = (Pattern.compile("^[?*](.+)?")).matcher(str);
			if (m.find() && m.group(1) == null) { 
				addError(lineNumber, "Error: missing command after asterisk\\questionmark");
				continue;
			}
			
			// More than 1 space after asterisk\?
			m = (Pattern.compile("^[?*]\\s{2,}")).matcher(str);
			if (m.find()) { 
				addError(lineNumber, "Error: more than 1 space after asterisk\\questionmark");
				continue;
			}
			
			// `*command` ~ missed space after asterisk
			m = (Pattern.compile("[?*](\\w+\\??)")).matcher(str);
			if (m.find()) {
				addError(lineNumber, "Error: missed space after asterisk\\questionmark in '" + m.group() + "'");
				continue;
			}
			
			// TODO this check most likely can be rewritten in a better way
			// no :Label for commands that use them, i.e. commands with ?:  "* commandname? <args> :Label"
			m = (Pattern.compile("\\* (\\w+\\??).*")).matcher(str);
			if (m.find()) {
				if (pscript.commandExists(m.group(1))) {
					Command c = pscript.findCommand(m.group(1));
					if (c.isConditional() && !str.matches(".*:\\w+$")) {
						addError(lineNumber, "Error: missing label for '" + m.group(1) + "'");
						continue;
					}
				} else {
					// unknown commands were already caught earlier
				}
			}
			
			
			// Unknown command OR invalid arguments
			m = (Pattern.compile("\\*(.+)?(?=:)|\\*(.+)?")).matcher(str);
			if (m.find()) {
				// such a spike was made because hasItem contains ':' in its argument and has a :label at the end 
				// this regexp either goes up to the latest ':' in the line or to the end of the line if none ':' was found
				String expression;
				// This 'if' block is used because of '|' operation in RegExp, it either returns groups 1-2 or 3-4
				if (m.group(1) != null) {
					expression = m.group(1);
				} else {
					expression = m.group(2);
				}
				
				if (!parse(expression, false, false, lineNumber))
					continue;
			}
			

			// uncapitalized BEGINNING and END in goto and :Labels
			m = (Pattern.compile("(?<=goto )[Ee][Nn][Dd]|(?<=:)[Ee][Nn][Dd]")).matcher(str);
			if (m.find() && !m.group().equals("END")) {
				addError(lineNumber, "Error: uncapitalized END");
				continue;
			}
			m = (Pattern.compile("(?<=goto )[Bb][Ee][Gg][Ii][Nn][Nn][Ii][Nn][Gg]|(?<=:)[Bb][Ee][Gg][Ii][Nn][Nn][Ii][Nn][Gg]")).matcher(str);
			if (m.find() && !m.group().equals("BEGINNING")) {
				addError(lineNumber, "Error: uncapitalized BEGINNING");
				continue;
			}
			
			
			// wrong capitalization in variables
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Nn][Aa][Mm][Ee]")).matcher(str);
			if (m.find() && !m.group().equals("$PLAYERNAME")) {
				addError(lineNumber, "Error: uncapitalized $PLAYERNAME");
				continue;
			}
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Gg][Ee][Nn][Dd][Ee][Rr]")).matcher(str);
			if (m.find() && !(m.group().equals("$PLAYERGENDER") || m.group().equals("$Playergender"))) {
				addError(lineNumber, "Error: use either $PLAYERGENDER or $Playergender");
				continue;
			}
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Rr][Aa][Cc][Ee]")).matcher(str);
			if (m.find() && !(m.group().equals("$PLAYERRACE") || m.group().equals("$Playerrace"))) {
				addError(lineNumber, "Error: use either $PLAYERRACE or $Playerrace");
				continue;
			}
			
			// Unknown NPC name
			boolean line_contains_npc_name = false;
			m = (Pattern.compile("^[^?*>].+(?=:)")).matcher(str);
			if (m.find()) {
				String npc_name = m.group().trim();
				String emotion = ""; 
				// TODO use this variable
				
				if (npc_name.matches(ValidSpeechName.pattern())) {
					m = (Pattern.compile("^([\\w\\s]+)( \\(\\w+\\))?")).matcher(npc_name);
					if (m.find()) {
						line_contains_npc_name = true;
						npc_name = m.group(1).trim();
						if (m.group(2) != null)
							emotion = m.group(2).trim();
						if (!npc_names.contains(npc_name)) {
							if (!MentionedNPCs.contains(npc_name)) {
								Log.add("Info: new NPC found '" + npc_name + "', first appearance at line " + lineNumber);
								MentionedNPCs.add(npc_name);
							} else {
								// This NPC wasn't mentioned by aliasname, but we already reported this name
							}
						}
					}	
				} else {
					addError(lineNumber, "Error: bad NPC name, or having colons (':') in speech");
					continue;
				}
				
			}
			
			// Checking NPC speech in NPCName: `speech`
			if (line_contains_npc_name) {
				m = (Pattern.compile("(?<=:).+")).matcher(str);
				if (m.find() && !m.group().matches(ValidSpeechText.pattern())) {
					addError(lineNumber, errInappropriateSymbol);
					continue;
				}
			}
			
			// suspicious words that passed all previous checks
			m = (Pattern.compile("^.+")).matcher(str);
			if (m.find()) {
				if (str.startsWith("*")) // Command or Single condition
					continue;
				if (str.startsWith("?")) // Multi-conditional statement
					continue;
				if (str.startsWith(">")) // Response
					continue;
				if (str.matches(ValidLabel.pattern())) // Label
					continue;
				// checking if this a part of NPC phrase: we have to go up to see if there was a speaking NPC
				boolean NPC = line_contains_npc_name;
				if (!line_contains_npc_name) {
					for (int j = lineNumber-1; j > 0; --j) {
						String s = textWithLines.get(j).trim();
						if (s.matches(ValidSpeechName.pattern() + ":.*")) { // NPCName (emotion): 
							NPC = true;
							break;
						}
						if (s.isEmpty() || s.matches(ValidSpeechText.pattern())) // part of the speech or comment
							continue;
						break;
					}
				}
				if (!NPC) {
					if (pscript.commandExists(m.group()))
						addError(lineNumber, "Error: perhaps an asterisk was missed");
					else
						addError(lineNumber, errInappropriateSymbol);
				}
			}
		}
		
		for (String task: Activated_Objectives) {
			if (!Completed_Objectives.contains(task)) 
				addError(-1, "Error: objective '" + task + "' was activated, but never was completed\\cancelled");			
		}
		for (String task: Completed_Objectives) {
			if (!Activated_Objectives.contains(task)) 
				addError(-1, "Error: objective '" + task + "' was completed\\cancelled, but never was activated");			
		}
		for (String quest: Activated_QuestIDs) {
			if (!Completed_QuestIDs.contains(quest)) 
				addError(-1, "Error: quest '" + quest + "' was activated, but was never completed");			
		}
		for (String quest: Completed_QuestIDs) {
			if (!Activated_QuestIDs.contains(quest)) 
				addError(-1, "Error: quest '" + quest + "' was completed, but was never activated");			
		}
		
		String temp = "";
		for (String s: Quest_IDs)
			temp += s + ", ";
		if (temp.length() == 0)
			temp = "<No QuestIDs found>  ";
		Log.add("Info: mentioned QuestIDs = " + temp.substring(0, temp.length() - 2));
		
		// TODO make a more comfortable way to show this information?
		temp = "";
		for (String s: Activated_Objectives)
			temp += s + ", ";
		if (temp.length() == 0)
			temp = "<Command activateobjective wasn't used>  ";
		Log.add("Info: mentioned Tasks = " + temp.substring(0, temp.length() - 2));
		
		temp = "";
		for (int k: Quest_Stages)
			temp += k + ", ";
		if (temp.length() == 0)
			temp = "<Command queststage wasn't used>  ";
		Log.add("Info: stages set by queststage command = " +  temp.substring(0, temp.length() - 2));
	}
	
	private void buildTree() {
		
		// Gathering all nodes in one pool
		String label = null;
		
		TreeMap<Integer, String> content = new TreeMap<Integer, String>();
		trees = new ArrayList<Node>();
		pool = new ArrayList<Node>();
		
		int currentline = 0; // used to detect if we reached the end of the file
		
		for (int i = 1; i < textWithLines.size() + 1; ++i) {
			String p = textWithLines.get(i);
			++currentline;
			// Removing comments in content of each node 
			p = p.replaceAll("#.*", "").trim();
			if (p.matches(ValidLabel.pattern()) || currentline == textWithLines.size()) {
				// reached another node
				if (label != null) {
					DefinedLabels.add(label);
					if (currentline == textWithLines.size())
						content.put(currentline, p);
					pool.add(new Node(content));
					if (currentline == textWithLines.size())
						break;
				} else {
					//shoots only once
					//content here contains the text above the first [label]
				}
				label = p;
				if (DefinedLabels.contains(label))
					addError(currentline, "Error: duplicate label name " + label);
				if (label.equals("[BEGINNING]"))
					addError(currentline, "Error: attempt to redefine [BEGINNING] label");
				if (label.equals("[END]"))
					addError(currentline, "Error: attempt to redefine [END] label");
				
				content = new TreeMap<Integer, String>();
				content.put(currentline, label);
			}
			content.put(currentline, p);
		}
		
		Log.add("Info: " + pool.size() + " labels have been found");
		if (pool.size() == 0) {
			Log.add("Info: no labels found, exiting...");
			return;
		}
		
		// Building a structured tree upon given pool
		for(Node node: pool) 
		{
			// Searching for labels in content of each node
			for(int i : node.getContent().keySet()) 
			{
				String s = node.getContent().get(i).trim();
				s = s.replaceAll("goto ", ":"); // this is for catching <* goto Label> commands
				if (s.matches(".+:\\w+$")) {
					// Most likely we reached a label here
					String[] arr = s.split(":");
					label = arr[arr.length - 1];
					String embracedLabel = "[" + label + "]";
					
					// Searching a child with the name=label
					Node child = null;
					for(int j = 0; j < pool.size(); ++j)
						if (pool.get(j).getLabel().equals(embracedLabel))
							child = pool.get(j);
					
					if (child == null) {
						// ERROR: no such label 
						if (!(label.equals("END") || label.equals("BEGINNING")))
							addError(i, "Error: no such label ':" + label + "'");
			
					} else {
						if (!node.getChildren().contains(child)) {
							child.addFather(node);
							node.addChild(child);
						}
					}
				}
			}
			
			// Searching for nodes with missing '* goto Label' at the and
			boolean found_reliable_exit_point = false;
			for(int i : node.getContent().keySet()) 
			{
				String line = node.getContent().get(i).trim();
				if (line.isEmpty())
					continue;
				// seeking for either goto or reliable dialogue answer that will 100% lead us out of the current node
				// dialogue answer must not contain conditional expression in order to be `reliable`, hehe
				if (line.matches("^\\*\\s*goto\\s+\\w+$|^\\s*>[^|]+:(\\w+)$")) 
				{
					found_reliable_exit_point = true;
					break;
				} 
			}
			if (!found_reliable_exit_point) {
				int lineNumber = node.getContent().firstKey();
				addError(lineNumber, "Error: no reliable exit point from node " + node.getLabel());
				break;
			}
			
			// Searching for areas with unreachable code in the current node, i.e. code after 'goto' command, or code after answer options
			boolean found_something = false;
			boolean found_goto_jump = false;
			
			for(int currentLine : node.getContent().keySet()) 
			{
				String line = node.getContent().get(currentLine).trim();
				
				if (line.isEmpty())
					continue;
				if (line.matches("^(\\* goto \\w+)|^>.+")) 
				{
					// No code will be executed after 'goto' jump
					if (found_goto_jump) {
						int unreachable_starts_at = currentLine; 
						addError(unreachable_starts_at, "Error: the code at line " + unreachable_starts_at + " and below will never be executed");
						break;
					}
					if (line.startsWith("*"))
						found_goto_jump = true;
					
					// But if we met a response, we still should be able to read 
					// other responses and don't give an error while reading them
					found_something = true;
				}
				else 
				{
					// We get here once we found a line that was neither a response nor goto command
					if (found_something) {
						int unreachable_starts_at = currentLine; 
						addError(unreachable_starts_at, "Error: the code at line " + unreachable_starts_at + " and below will never be executed");
						break;
					}
				}
			}
			
			
		}
		
		// Counting the number of trees
		for (Node node: pool) {
			if (node.getFathers().size() == 0)
				trees.add(node);
		}
		
		Log.add("Info: " + trees.size() + " trees have been found");
		
	}
	
	private boolean parse(String expression, boolean isConditional, boolean isMultiConditional, int lineNumber) {
		
		expression = expression.trim();
		
		// In case the expression was multi-conditional
		String[] expressions = expression.split("[&|^]");
		
		//if (multiconditional && expressions.length < 2) {
		//	MainWindow.pushToLog("Error: multi-conditional check must have at least 2 conditions at line " + linenumber);
		//	return false;
		//}
		if (!isMultiConditional && expressions.length > 1) {
			addError(lineNumber, "Error: only one command is allowed after the asterisk");
			return false;
		}
		
		for (String exp : expressions) {
			exp = exp.trim();
			String command = exp.split(" ")[0];
			if (pscript.commandExists(command)) 
			{
				Command c = pscript.findCommand(command);
				if (c.hasArguments() && exp.indexOf(' ') == -1) {
					addError(lineNumber, "Error: command '" + command + "' requires argument(s)");
					return false;
				}
				
				String args = "";
				if (exp.contains(" "))
					args = exp.substring(exp.indexOf(' '), exp.length()).trim();
					
				if (!c.isConditional() && isConditional) {
					addError(lineNumber, "Error: command '" + command + "' is not suitable for using in conditional response");
					return false;
				}
				if (!c.accepts(args)) {
					addError(lineNumber, "Error: invalid argument '" + args + "' for " + command + "'");
					return false;
				}
				
				// For checking if objectives and quests were completed or not
				Matcher m = (Pattern.compile("\\w+\\?? <?(" + Syntax.QuestID_pattern + ")>?(.*)")).matcher(expression);
				if (m.find()) {
					
					String FirstArgument = m.group(1); 	// usually QuestID, but not for all commands. Doesn't work for argument of hasitem, i.e. <x:y>
					String SecondArgument = ""; 		// can be TaskID or something else
					if (m.group(2) != null) {
						SecondArgument = m.group(2).trim();
					} 
					
					switch (c.getName()) {
						case "activateobjective": Activated_Objectives.add(SecondArgument); Quest_IDs.add(FirstArgument); break;
						case "completeobjective": Completed_Objectives.add(SecondArgument); Quest_IDs.add(FirstArgument); break;
						case "cancelobjective": Completed_Objectives.add(SecondArgument); Quest_IDs.add(FirstArgument); break;
						case "activatequest": Activated_QuestIDs.add(FirstArgument); Quest_IDs.add(FirstArgument); break;
						case "completequest": Completed_QuestIDs.add(FirstArgument); Quest_IDs.add(FirstArgument); break;
						case "queststage": 
							int stage = Integer.parseInt(SecondArgument);
							if (stage > 9000)
								Completed_QuestIDs.add(FirstArgument);
							Quest_Stages.add(stage);
							Quest_IDs.add(FirstArgument);
						break;
						case "queststage?": Quest_IDs.add(FirstArgument); break;
						case "objectivecomplete?": Quest_IDs.add(FirstArgument); break;
						case "questactive?": Quest_IDs.add(FirstArgument); break;
						case "activateKillObjective": Quest_IDs.add(FirstArgument); break;
					}
				}
			} 
			else {
				addError(lineNumber, "Error: unknown command '" + command + "'");
				return false;
			}
		}
		
		return true;
	}
	
	
	public List<Node> getTrees() {
		return trees;
	}

	public HashMap<Integer, String> getText() {
		return textWithLines;
	}

	public List<Node> getPool() {
		return pool;
	}

}
