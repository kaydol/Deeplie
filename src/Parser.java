

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	
	private List<String> text;
	private List<Node> trees;
	private List<Node> pool;
	private Syntax pscript;
	
	private HashSet<String> Activated_QuestIDs;
	private HashSet<String> Completed_QuestIDs;
	private HashSet<String> Activated_Objectives;
	private HashSet<String> Completed_Objectives;
	private LinkedHashSet<Integer> Quest_Stages;
	private LinkedHashSet<String> Quest_IDs;
	
	private Pattern ValidSpeechName, ValidSpeechText, ValidCommand, ValidCondition, ValidLabel;
	
	public Parser(String filename) throws IOException {
		pscript = new Syntax();
		Activated_QuestIDs = new HashSet<String>();
		Completed_QuestIDs = new HashSet<String>();
		Activated_Objectives = new HashSet<String>();
		Completed_Objectives = new HashSet<String>();
		Quest_Stages = new LinkedHashSet<Integer>();
		Quest_IDs = new LinkedHashSet<String>();
		
		ValidSpeechName = Pattern.compile("^([\\w\\s]+)( \\(\\w+\\))?"); // Name (emotion)
		ValidSpeechText = Pattern.compile("[\\w\\s;,\\.!\\?\\$'\"\\-“”‘’&%…]+"); // NO ':' allowed!
		ValidCommand = Pattern.compile("[\\w\\?\\s:<>=\\-\\[\\]\\.]+"); // \w ? \s : <>=- [] .
		ValidCondition = Pattern.compile("[\\w\\?\\s:<>=\\-\\|^&]+");
		ValidLabel = Pattern.compile("^\\[\\w+\\]");
		
		readFile(filename);
		performAnalysis();
		buildTree();
	}
	
	public void readFile(String filename) throws IOException {
		
		File file = new File(filename);	
		if (!file.exists())
			MainWindow.pushToLog("Error: no such file in directory '" + filename + "'");
		
		text = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
		
		Activated_QuestIDs.clear();
		Completed_QuestIDs.clear();
		Activated_Objectives.clear();
		Completed_Objectives.clear();
		Quest_Stages.clear();
		Quest_IDs.clear();
		
		MainWindow.pushToLog("Info: finished reading file, " + text.size() + " lines total");
	}
	
	private void performAnalysis() {
		String str;
		Matcher m;

		HashSet<String> npc_names = new HashSet<String>();

		// This message repeats a lot, so I put in a variable
		String errInappropriateSymbol = "Error: inappropriate symbol(s) at line ";
		

		for (int i = 0; i < text.size(); ++i) {
			str = text.get(i).trim();
			
			// Saving the current label to allow aliasname only before the first label
			String currentLabel = "";
			if (str.matches(ValidLabel.pattern()))
				currentLabel = str;
			
			if (str.isEmpty() || str.startsWith("#"))
				continue;
			
			if (str.startsWith("*") && !str.matches("\\*" + ValidCommand.pattern())) {
				MainWindow.pushToLog(errInappropriateSymbol + (i+1));
				continue;
			}
			if (str.startsWith("?") && !str.matches("\\?" + ValidCondition.pattern())) {
				MainWindow.pushToLog(errInappropriateSymbol + (i+1));
				continue;
			}
			
			
			m = (Pattern.compile("\\* aliasname <([\\w]+)> <([\\w\\s]+)>")).matcher(str);
			if (m.find()) { 
				npc_names.add(m.group(1));
				npc_names.add(m.group(2));
				if (!currentLabel.isEmpty())
					MainWindow.pushToLog("Error: having 'aliasname' in the middle of the script, at line " + (i+1));
				continue;
			}
			
			// " : "
			m = (Pattern.compile("\\s:\\s")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("Error: ':' shouldn't be surrounded by two spaces at line " + (i+1));
				continue;
			}
			
			// "Text:Text" instead of "Name: Text" or "Text :Label" i.e. : should always has at least space around
			// digits added to prevent shooting at <-1:10> alike constructions, used by hasitem? command
			m = (Pattern.compile("[^\\s\\d]:[^\\s\\d]")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("Error: at least one space must be present near ':' at line " + (i+1));
				continue;
			}
			
			// ">Text" instead of "> Text"
			m = (Pattern.compile("^\\s*>[^\\s]")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("Error: > should be followed by space at line " + (i+1));
				continue;
			}
			
			// "> Text" or "? condition" with missing :Label at the end
			m = (Pattern.compile("^>.*|^\\?.*")).matcher(str);
			if (m.find()) {
				if (!str.matches(".*:\\w+$")) {
					MainWindow.pushToLog("Error: missing label at line " + (i+1));
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
					System.out.println(str.substring(beginIndex, endIndex));
					MainWindow.pushToLog("Error: inappropriate symbol(s) in Response at line " + (i+1));
					continue;
				}
			}
			
			// Checking multi-conditional lines: `? command1 args ([|&^] command2 args...) :Label`
			m = (Pattern.compile("^\\?(.*):\\w+$")).matcher(str);
			if (m.find()) {
				if (!parse(m.group(1), true, true, i+1))
					continue;
			}
			
			// Check code between pipelines in Optional Responses
			m = (Pattern.compile("(?<=\\|)(.*)(?=\\|)")).matcher(str);
			if (m.find() && str.startsWith(">")) {
				if (!parse(m.group(1), true, true, i+1))
					continue;
			}
			
			// Missed pipe in Optional Responses
			m = (Pattern.compile("\\|(.*\\|)?")).matcher(str);
			if (m.find() && str.startsWith(">") && m.group(1) == null) {
				MainWindow.pushToLog("Error: the second pipe symbol was missed in the Optional Response at line " + (i+1));
				continue;
			}
			
			
			// Missing command after asterisk\?
			m = (Pattern.compile("^[?*](.+)?")).matcher(str);
			if (m.find() && m.group(1) == null) { 
				MainWindow.pushToLog("Error: missing command after asterisk\\questionmark at line " + (i+1));
				continue;
			}
			
			// More than 1 space after asterisk\?
			m = (Pattern.compile("^[?*]\\s{2,}")).matcher(str);
			if (m.find()) { 
				MainWindow.pushToLog("Error: more than 1 space after asterisk\\questionmark at line " + (i+1));
				continue;
			}
			
			// `*command` ~ missed space after asterisk
			m = (Pattern.compile("[?*](\\w+\\??)")).matcher(str);
			if (m.find()) {
				MainWindow.pushToLog("Error: missed space after asterisk\\questionmark in '" + m.group() + "' at line " + (i+1));
				continue;
			}
			
			// TODO this check most likely can be rewritten in a better way
			// no :Label for commands that use them, i.e. commands with ?:  "* commandname? <args> :Label"
			m = (Pattern.compile("\\* (\\w+\\??).*")).matcher(str);
			if (m.find()) {
				if (pscript.commandExists(m.group(1))) {
					Command c = pscript.findCommand(m.group(1));
					if (c.isConditional() && !str.matches(".*:\\w+$")) {
						MainWindow.pushToLog("Error: missing label for '" + m.group(1) + "' at line " + (i+1));
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
				
				if (!parse(expression, false, false, i+1))
					continue;
			}
			

			// uncapitalized BEGINNING and END in goto and :Labels
			m = (Pattern.compile("(?<=goto )[Ee][Nn][Dd]|(?<=:)[Ee][Nn][Dd]")).matcher(str);
			if (m.find() && !m.group().equals("END")) {
				MainWindow.pushToLog("Error: uncapitalized END at line " + (i+1));
				continue;
			}
			m = (Pattern.compile("(?<=goto )[Bb][Ee][Gg][Ii][Nn][Nn][Ii][Nn][Gg]|(?<=:)[Bb][Ee][Gg][Ii][Nn][Nn][Ii][Nn][Gg]")).matcher(str);
			if (m.find() && !m.group().equals("BEGINNING")) {
				MainWindow.pushToLog("Error: uncapitalized BEGINNING at line " + (i+1));
				continue;
			}
			
			
			// wrong capitalization in variables
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Nn][Aa][Mm][Ee]")).matcher(str);
			if (m.find() && !m.group().equals("$PLAYERNAME")) {
				MainWindow.pushToLog("Error: uncapitalized $PLAYERNAME at line " + (i+1));
				continue;
			}
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Gg][Ee][Nn][Dd][Ee][Rr]")).matcher(str);
			if (m.find() && !(m.group().equals("$PLAYERGENDER") || m.group().equals("$Playergender"))) {
				MainWindow.pushToLog("Error: use either $PLAYERGENDER or $Playergender at line " + (i+1));
				continue;
			}
			m = (Pattern.compile("\\$[Pp][Ll][Aa][Yy][Ee][Rr][Rr][Aa][Cc][Ee]")).matcher(str);
			if (m.find() && !(m.group().equals("$PLAYERRACE") || m.group().equals("$Playerrace"))) {
				MainWindow.pushToLog("Error: use either $PLAYERRACE or $Playerrace at line " + (i+1));
				continue;
			}
			
			// Unknown NPC name
			boolean line_contains_npc_name = false;
			//m = (Pattern.compile("^([\\w\\s]+)( \\(\\w+\\))?:.+")).matcher(str);
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
							MainWindow.pushToLog("Error: if '" + npc_name + "' is an NPC name, it's good to mention it by aliasname command, at line " + (i+1));
							//continue;
						}
					}	
				} else {
					MainWindow.pushToLog("Error: using colons in speech is forbidden, at line " + (i+1));
					continue;
				}
				
			}
			
			// Checking NPC speech in NPCName: `speech`
			if (line_contains_npc_name) {
				m = (Pattern.compile("(?<=:).+")).matcher(str);
				if (m.find() && !m.group().matches(ValidSpeechText.pattern())) {
					MainWindow.pushToLog(errInappropriateSymbol + (i+1));
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
					for (int j = i-1; j > 0; --j) {
						String s = text.get(j).trim();
						if (s.matches(ValidSpeechName.pattern() + ":.*")) { // NPCName (emotion): 
							NPC = true;
							break;
						}
						if (s.isEmpty() || s.startsWith("#") || s.matches(ValidSpeechText.pattern())) // part of the speech or comment
							continue;
						break;
					}
				}
				if (!NPC) {
					if (pscript.commandExists(m.group()))
						MainWindow.pushToLog("Error: perhaps an asterisk was missed at line " + (i+1));
					else
						MainWindow.pushToLog(errInappropriateSymbol + (i+1));
				}
			}
		}
		

		for (String task: Activated_Objectives) {
			if (!Completed_Objectives.contains(task)) 
				MainWindow.pushToLog("Error: objective '" + task + "' was activated, but never was completed\\cancelled");			
		}
		for (String task: Completed_Objectives) {
			if (!Activated_Objectives.contains(task)) 
				MainWindow.pushToLog("Error: objective '" + task + "' was completed\\cancelled, but never was activated");			
		}
		for (String quest: Activated_QuestIDs) {
			if (!Completed_QuestIDs.contains(quest)) 
				MainWindow.pushToLog("Error: quest '" + quest + "' was activated, but was never completed");			
		}
		for (String quest: Completed_QuestIDs) {
			if (!Activated_QuestIDs.contains(quest)) 
				MainWindow.pushToLog("Error: quest '" + quest + "' was completed, but was never activated");			
		}
		
		String temp = "";
		for (String s: Quest_IDs)
			temp += s + ", ";
		if (temp.length() == 0)
			temp = "<No QuestIDs found>  ";
		MainWindow.pushToLog("Info: mentioned QuestIDs = " + temp.substring(0, temp.length() - 2));
		
		// TODO make a more comfortable way to show this information + change Activated_Objectives to LinkedHashSet to save the order of tasks
		//temp = "";
		//for (String s: Activated_Objectives)
		//	temp += s + ", ";
		//MainWindow.pushToLog("Info: mentioned Objectives = " + temp.substring(0, temp.length() - 2));
		
		temp = "";
		for (int k: Quest_Stages)
			temp += k + ", ";
		if (temp.length() == 0)
			temp = "<Command queststage wasn't used>  ";
		MainWindow.pushToLog("Info: stages set by queststage command = " +  temp.substring(0, temp.length() - 2));
		
				
	}
	
	private void buildTree() {
		
		// Gathering all nodes in one pool
		String label = null;
		HashSet<String> usedLabels = new HashSet<String>();
		
		List<String> content = new ArrayList<String>();
		trees = new ArrayList<Node>();
		pool = new ArrayList<Node>();
		
		int currentline = 0; // used to detect if we reached the end of the file
		for (String p: text) {
			++currentline;
			p = p.trim();
			if (p.matches(ValidLabel.pattern()) || currentline == text.size()) {
				// reached another node
				if (label != null) {
					usedLabels.add(label);
					if (currentline == text.size())
						content.add(p);
					pool.add(new Node(label, content));
					if (currentline == text.size())
						break;
				} else {
					//shoots only once
					//content here contains the text above the first [label]
				}
				label = p;
				if (usedLabels.contains(label))
					MainWindow.pushToLog("Error: duplicate label name " + label + " at line " + currentline);

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
				s = s.trim();
				s = s.replaceAll("goto ", ":"); // this is for catching <* goto Label> commands
				if (s.matches(".+:\\w+$")) {
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
						// sometimes lineNumber can glitch and be zero; 
						// this happens when Java fails to find that line in the 'text' array, and indexOf returns -1
						// Java fails because we search for a trimmed version, and text contains untrimmed strings
						if (!(label.equals("END") || label.equals("BEGINNING")))
							MainWindow.pushToLog("Error: no such label ':" + label + "' at line " + lineNumber);
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
			for(int i = node.getContent().size() - 1; i > 0; --i) {
				String line = node.getContent().get(i).trim();
				if (line.isEmpty() || line.startsWith("#")) 
					continue;
				// seeking for either goto or reliable dialogue answer that will 100% lead us out of the current node
				// dialogue answer must not contain conditional expression in order to be `reliable`, hehe
				if (line.matches("^\\* goto \\w+$|^> [^|]+:(\\w+)$")) 
				{
					found_reliable_exit_point = true;
					break;
				} 
			}
			if (!found_reliable_exit_point) {
				int linenumber = text.indexOf(node.getLabel()) + 1;
				MainWindow.pushToLog("Error: no reliable exit point at node " + node.getLabel() + " starting at line " + linenumber);
				break;
			}
			
			
			
			// Searching for areas with unreachable code in the current node, i.e. code after 'goto' command, or code after answer options
			boolean found_something = false;
			boolean found_goto_jump = false;
			for(int i = 0; i < node.getContent().size(); ++i) {
				String line = node.getContent().get(i).trim();
				if (line.isEmpty() || line.startsWith("#")) 
					continue;
				if (line.matches("^(\\* goto \\w+)|^>.+")) 
				{
					// No code will be executed after 'goto' jump
					if (found_goto_jump) {
						int unreachable_starts_at = text.indexOf(line) + 1;
						MainWindow.pushToLog("Error: the code at line " + unreachable_starts_at + " and below will never be executed");
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
						int unreachable_starts_at = text.indexOf(line) + 1;
						MainWindow.pushToLog("Error: the code at line " + unreachable_starts_at + " and below will never be executed");
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
		
		MainWindow.pushToLog("Info: " + trees.size() + " trees have been found");
		MainWindow.pushToLog("Info: parser finished its job");
		
	}
	
	private boolean parse(String expression, boolean isConditional, boolean isMultiConditional, int linenumber) {
		
		expression = expression.trim();
		
		// In case the expression was multi-conditional
		String[] expressions = expression.split("[&|^]");
		
		//if (multiconditional && expressions.length < 2) {
		//	MainWindow.pushToLog("Error: multi-conditional check must have at least 2 conditions at line " + linenumber);
		//	return false;
		//}
		if (!isMultiConditional && expressions.length > 1) {
			MainWindow.pushToLog("Error: only one command is allowed after the asterisk at line " + linenumber);
			return false;
		}
		
		for (String exp : expressions) {
			exp = exp.trim();
			String command = exp.split(" ")[0];
			if (pscript.commandExists(command)) 
			{
				Command c = pscript.findCommand(command);
				String args = exp.substring(exp.indexOf(' '), exp.length()).trim();
				if (!c.isConditional() && isConditional) {
					MainWindow.pushToLog("Error: command '" + command + "' is not suitable for using in conditional response, at line " + linenumber);
					return false;
				}
				if (!c.accepts(args)) {
					MainWindow.pushToLog("Error: invalid argument '" + args + "' for " + command + " at line " + linenumber);			
					if (c.hasExamples()) {
						MainWindow.pushToLog("Valid example:");
						for (String example : c.getExamples())
							MainWindow.pushToLog(MainWindow.prefix_error + '|' + MainWindow.prefix_example + example);
					}
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
				MainWindow.pushToLog("Error: unknown command '" + command + "' at line " + linenumber);
				return false;
			}
		}
		
		return true;
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
