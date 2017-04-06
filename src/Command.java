import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Command {

	List<Pattern> patterns;
	private boolean isConditional;
	private String commandName;
	private String[] examples;
	
	public Command(String commandName, String pattern, String[] examples) {
		this(commandName, Pattern.compile(pattern), examples);
	}
	public Command(String commandName, String[] patterns, String[] examples) {
		this(commandName, toPatternList(patterns), examples);
	}
	public Command(String commandName, Pattern pattern, String[] examples) {
		this(commandName, toPatternList(pattern), examples);
	}
	public Command(String commandName, List<Pattern> patterns, String[] examples) {
		setName(commandName);
		setExamples(examples);
		setConditionality(commandName);
		setPatterns(patterns);
	}
	
	private static List<Pattern> toPatternList(String[] patterns) {
		List<Pattern> pats = new ArrayList<Pattern>();
		for (String p: patterns)
			pats.add(Pattern.compile(p));
		return pats;
	}
	private static List<Pattern> toPatternList(Pattern pattern) {
		List<Pattern> pats = new ArrayList<Pattern>();
		pats.add(pattern);
		return pats;
	}
	
	public void addPattern(Pattern p) {
		patterns.add(p);
	}
	public void addPattern(String s) {
		patterns.add(Pattern.compile(s));
	}
	
	boolean accepts(String argument) {
		for (Pattern p: patterns) {
			if (argument.matches(p.toString()))
				return true;
		}
		return false;
	}
	public String getName() {
		return commandName;
	}
	public void setName(String commandName) {
		this.commandName = commandName;
	}
	
	public boolean isConditional() {
		return isConditional;
	}
	private void setConditionality(String commandName) {
		isConditional = commandName.endsWith("?");
	}
	
	public String[] getExamples() {
		if (examples == null)
			return new String[] {"This command has no provided examples."};
		else
			return examples;
	}
	public void setExamples(String[] examples) {
		this.examples = examples;
	}
	public void setPatterns(List<Pattern> patterns) {
		this.patterns = patterns;
	}
	public boolean hasArguments() {
		return patterns != null;
	}
	public boolean hasExamples() {
		return examples != null;
	}
}
