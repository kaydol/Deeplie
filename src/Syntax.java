import java.util.ArrayList;
import java.util.List;

public class Syntax {

	List<Command> commands;
	private String QuestID_pattern = "\\w+"; 
	
	public Syntax() {
		
		commands = new ArrayList<Command>();
		
		commands.add(new Command("goto", "\\w+", false, new String[] {"* goto LabelName"}));
		commands.add(new Command("hasitem?", new String[] {"<-1:\\d+>", "<\\d+(:\\d+)?>"}, true, new String[] {"* hasitem? <ItemID> :LabelName", "* hasitem? <ItemID:amount> :LabelName"}));
		commands.add(new Command("hasitemequipped?", "<\\d(, \\d+){4}>", true, new String[] {"* hasitemequipped? <ItemID> :LabelName", "* hasitemequipped? <ItemID, ItemID, ItemID, ItemID, ItemID> :LabelName"}));
		
		commands.add(new Command("hascutiemark?", "<\\d+>", true, new String[] {"* hascutiemark? <TalentMarkID> :LabelName"}));
		
		commands.add(new Command("takeitem", new String[] {"-1 \\d+", "\\d+ \\d+"}, false, new String[] {"* takeitem ItemID amount #Removes a specific item from the player’s inventory"}));
		commands.add(new Command("giveitem", new String[] {"-1 \\d+", "\\d+ \\d+"}, false, new String[] {"* giveitem ItemID amount #Adds a specific item to the player’s inventory"}));
		commands.add(new Command("givexp", "(<\\w+(, \\w+)*> )?\\d+", false, new String[] {"* givexp 100 #Gives XP for all talent marks", "* givexp <Combat> 100 #Gives XP for a specific talent mark", "* givexp <Cooking, Mining> 100 #Talentmarks could be combined by commas"}));
		
		commands.add(new Command("queststage", "<" + QuestID_pattern + "> [1-9](\\d+)?", false, new String[] {"* queststage <QuestID> [1-9999]"}));
		commands.add(new Command("queststage?", "<" + QuestID_pattern + "> [=><] \\d+", true, new String[] {"* queststage? <QuestID> [=><] [1-9999]"}));
		
		commands.add(new Command("activateobjective", "<" + QuestID_pattern + "> [\\w\\s]+", false, new String[] {"* activateobjective <QuestID> ObjectiveID"}));
		commands.add(new Command("completeobjective", "<" + QuestID_pattern + "> [\\w\\s]+", false, new String[] {"* completeobjective <QuestID> ObjectiveID"}));
		commands.add(new Command("cancelobjective", "<" + QuestID_pattern + "> [\\w\\s]+", false, new String[] {"* cancelobjective <QuestID> ObjectiveID"}));
		
		commands.add(new Command("objectivecomplete?", "<" + QuestID_pattern + "> <[\\w\\s]+>", true, new String[] {"* objectivecomplete? <QuestID> <ObjectiveID> :LabelName"}));
		
		commands.add(new Command("questcomplete?", "<" + QuestID_pattern + ">", true, new String[] {"* questcomplete? <QuestID> :LabelName"}));
		commands.add(new Command("questactive?", "<" + QuestID_pattern + ">", true, new String[] {"* questactive? <QuestID> :LabelName"}));
		commands.add(new Command("completequest", QuestID_pattern, false, new String[] {"* completequest QuestID"}));
		commands.add(new Command("activatequest", "<" + QuestID_pattern + ">", false, new String[] {"* activatequest <QuestID>"}));
		
		commands.add(new Command("giveSkill", "\\d+ \\d+", false, new String[] {"* giveSkill SkillID LevelOfSkill", "* giveSkill 2 0"}));
		commands.add(new Command("aliasname", "<\\w+> <[\\w\\s]+>", false, new String[] {"* aliasname <ShortName> <Long Name>","* aliasname <Chaser> <Snuggle Chaser>", "* aliasname <SC> <Snuggle Chaser>", "* aliasname <Snug> <Snuggle Chaser>"}));
		
		commands.add(new Command("runscript", "(\\.\\.\\/)?(\\w+\\/)*([\\w\\s]+\\.pscript)", false, null));
		commands.add(new Command("activateKillObjective", "<" + QuestID_pattern + "> <[\\w\\s]+> <\\w+> <\\w+> <\\d+> <\\w+>", false, null));
		
		commands.add(new Command("israce?", new String[] {"<Pegasus>", "<Unicorn>", "<Earth>"}, true, new String[] {"* israce? <[Pegasus|Unicorn|Earth]> :LabelName"}));
		commands.add(new Command("isage?", new String[] {"<Colt>", "<Stallion>", "<Filly>", "<Mare>"}, true, new String[] {"* isage? <[Colt|Stallion|Filly|Mare]> :LabelName"}));
		
		
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
