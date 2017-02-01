import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Command {

	List<Pattern> patterns;
	boolean hasLabel;
	private String commandName;
	private String[] examples;
	
	public Command(String commandName, Pattern pattern, boolean hasgoto, String[] examples) {
		setName(commandName);
		setExamples(examples);
		this.hasLabel = hasgoto;
		patterns = new ArrayList<Pattern>();
		patterns.add(pattern);
	}
	public Command(String commandName, List<Pattern> patterns, boolean hasgoto, String[] examples) {
		setName(commandName);
		setExamples(examples);
		this.hasLabel = hasgoto;
		this.patterns = patterns;
	}
	public Command(String commandName, String[] patterns, boolean hasgoto, String[] examples) {
		setName(commandName);
		setExamples(examples);
		this.hasLabel = hasgoto;
		this.patterns = new ArrayList<Pattern>();
		for (String p: patterns)
			this.patterns.add(Pattern.compile(p));
	}
	public Command(String commandName, String pattern, boolean hasgoto, String[] examples) {
		setName(commandName);
		setExamples(examples);
		this.hasLabel = hasgoto;
		patterns = new ArrayList<Pattern>();
		patterns.add(Pattern.compile(pattern));
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
	
	public boolean hasLabel() {
		return hasLabel;
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
	public boolean hasExamples() {
		return examples != null;
	}
}
