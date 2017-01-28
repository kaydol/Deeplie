import java.util.ArrayList;
import java.util.List;

public class Syntax {

	List<Command> commands;
	private String QuestID_pattern = "\\w+"; 
	
	public Syntax() {
		
		commands = new ArrayList<Command>();
		
		commands.add(new Command("goto", "\\w+", false));
		commands.add(new Command("hasitem?", new String[] {"<-1:\\d+>", "<\\d+(:\\d+)?>"}, true));
		commands.add(new Command("hasitemequipped?", "<\\d(, \\d+){4}>", true));
		commands.add(new Command("takeitem", new String[] {"-1 \\d+", "\\d+ \\d+"}, false));
		commands.add(new Command("giveitem", new String[] {"-1 \\d+", "\\d+ \\d+"}, false));
		commands.add(new Command("givexp", "<\\w+(, \\w+)*> \\d+", false));
		
		commands.add(new Command("queststage", "<" + QuestID_pattern + "> \\d+", false));
		
		commands.add(new Command("queststage?", "<" + QuestID_pattern + "> [=><] \\d+", true));
		commands.add(new Command("activateobjective", "<" + QuestID_pattern + "> [\\w\\s]+", false));
		commands.add(new Command("completeobjective", "<" + QuestID_pattern + "> [\\w\\s]+", false));
		commands.add(new Command("cancelobjective", "<" + QuestID_pattern + "> [\\w\\s]+", false));
		
		commands.add(new Command("objectivecomplete?", "<" + QuestID_pattern + "> <[\\w\\s]+>", true));
		
		commands.add(new Command("questcomplete?", "<" + QuestID_pattern + ">", true));
		commands.add(new Command("questactive?", "<" + QuestID_pattern + ">", true));
		commands.add(new Command("completequest", QuestID_pattern, false));
		commands.add(new Command("activatequest", "<" + QuestID_pattern + ">", false));
		
		commands.add(new Command("giveSkill", "\\d+ \\d+", false));
		commands.add(new Command("aliasname", "<\\w+> <[\\w\\s]+>", false));
		commands.add(new Command("runscript", "(\\.\\.\\/)?(\\w+\\/)*([\\w\\s]+\\.pscript)", false));
		commands.add(new Command("activateKillObjective", "<" + QuestID_pattern + "> <[\\w\\s]+> <\\w+> <\\w+> <\\d+> <\\w+>", false));
		commands.add(new Command("israce", new String[] {"<Pegasus>", "<Unicorn>", "<Earth>"}, false));
		
	}
	
	public boolean commandExists(String name) {
		for (Command c: commands) {
			if (c.getName().equals(name))
				return true;
		}
		return false;
	}
	
	public Command findCommand(String name) {
		for (Command c: commands) {
			if (c.getName().equals(name))
				return c;
		}
		return null;
	}
}
