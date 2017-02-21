import java.util.ArrayList;
import java.util.List;

public class Syntax {

	List<Command> commands;
	public static String QuestID_pattern = "\\w+"; 
	
	public Syntax() {
		
		commands = new ArrayList<Command>();
		
		commands.add(new Command("goto", "\\w+", new String[] {"* goto LabelName"}));
		commands.add(new Command("hasitem?", new String[] {"<-1:\\d+>", "<\\d+(:\\d+)?>"},new String[] {"* hasitem? <ItemID> :LabelName", "* hasitem? <ItemID:amount> :LabelName"}));
		commands.add(new Command("hasitemequipped?", "<\\d(, \\d+){4}>", new String[] {"* hasitemequipped? <ItemID> :LabelName", "* hasitemequipped? <ItemID, ItemID, ItemID, ItemID, ItemID> :LabelName"}));
		
		//commands.add(new Command("hascutiemark?", "<\\d+>", true, new String[] {"* hascutiemark? <TalentMarkID> :LabelName"}));
		
		commands.add(new Command("takeitem", new String[] {"-1 \\d+", "\\d+ \\d+"}, new String[] {"* takeitem ItemID amount"}));
		commands.add(new Command("giveitem", new String[] {"-1 \\d+", "\\d+ \\d+"}, new String[] {"* giveitem ItemID amount"}));
		commands.add(new Command("givexp", "(<\\w+(, \\w+)*> )?\\d+", new String[] {"* givexp 100", "* givexp <Combat> 100", "* givexp <Cooking, Mining, ...> 100"}));
		
		commands.add(new Command("queststage", "<" + QuestID_pattern + "> [1-9](\\d+)?", new String[] {"* queststage <QuestID> [1-9999]"}));
		commands.add(new Command("queststage?", "<" + QuestID_pattern + "> [=><] \\d+", new String[] {"* queststage? <QuestID> [=><] [1-9999]"}));
		
		commands.add(new Command("activateobjective", "<" + QuestID_pattern + "> [\\w\\s]+", new String[] {"* activateobjective <QuestID> ObjectiveID"}));
		commands.add(new Command("completeobjective", "<" + QuestID_pattern + "> [\\w\\s]+", new String[] {"* completeobjective <QuestID> ObjectiveID"}));
		commands.add(new Command("cancelobjective", "<" + QuestID_pattern + "> [\\w\\s]+", new String[] {"* cancelobjective <QuestID> ObjectiveID"}));
		
		commands.add(new Command("objectivecomplete?", "<" + QuestID_pattern + "> <[\\w\\s]+>", new String[] {"* objectivecomplete? <QuestID> <ObjectiveID> :LabelName"}));
		
		commands.add(new Command("questcomplete?", "<" + QuestID_pattern + ">", new String[] {"* questcomplete? <QuestID> :LabelName"}));
		commands.add(new Command("questactive?", "<" + QuestID_pattern + ">", new String[] {"* questactive? <QuestID> :LabelName"}));
		commands.add(new Command("completequest", QuestID_pattern, new String[] {"* completequest QuestID"}));
		commands.add(new Command("activatequest", "<" + QuestID_pattern + ">", new String[] {"* activatequest <QuestID>"}));
		
		commands.add(new Command("giveSkill", "\\d+ \\d+", new String[] {"* giveSkill SkillID LevelOfSkill", "* giveSkill 2 0"}));
		commands.add(new Command("aliasname", "<\\w+> <[\\w\\s]+>", new String[] {"* aliasname <ShortName> <Long Name>","* aliasname <Chaser> <Snuggle Chaser>", "* aliasname <SC> <Snuggle Chaser>", "* aliasname <Snug> <Snuggle Chaser>"}));
		
		commands.add(new Command("runscript", "(\\.\\.\\/)?(\\w+\\/)*(" + QuestID_pattern + "\\.pscript)( goto \\[\\w+\\])?", new String[] {"* runscript QuestID.pscript", "* runscript ../CK_Library/QuestID.pscript", "* runscript ../QuestID.pscript goto [Label]"}));
		commands.add(new Command("activateKillObjective", "<" + QuestID_pattern + "> <[\\w\\s]+> <\\w+> <\\w+> <\\d+> <\\w+>", null));
		
		commands.add(new Command("israce?", new String[] {"<Pegasus>", "<Unicorn>", "<Earth>"}, new String[] {"* israce? <[Pegasus|Unicorn|Earth]> :LabelName"}));
		commands.add(new Command("isage?", new String[] {"<Colt>", "<Stallion>", "<Filly>", "<Mare>"}, new String[] {"* isage? <[Colt|Stallion|Filly|Mare]> :LabelName"}));
		
		commands.add(new Command("f", new String[] {"ChangeRoom\\(\\w+\\)", "PlaySound\\(.+\\)", "TeleportTo\\(.+\\)"}, new String[] {"* f ChangeRoom(Cantermore)", "* f PlaySound(SFX/Level_Up_TM_SFX)", "* f TeleportTo(position->\"-323, 45, -22.9\")", "* f TeleportTo(npcs->Wellington)"}));
		
		commands.add(new Command("playsound", new String[] {"(\\w+\\/)*(\\w+)"}, new String[] {"* playsound Music/All/Battle_Boss", "* playsound SFX/Chicken_Pain03"}));
		
		
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
