import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class Node {

	private String label;
	private TreeMap<Integer, String> content;
	private List<Node> fathers;
	private List<Node> children;
	private Point2D position;
	private int drawcount;
	
	public Node(TreeMap<Integer, String> content) {
		setLabel(content.firstEntry().getValue());
		setContent(content);
		children = new ArrayList<Node>();
		fathers = new ArrayList<Node>();
		drawcount = 0;
	}

	public void addChild(Node child) {
		children.add(child);
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public void addFather(Node father) {		
		fathers.add(father);
	}
	
	public List<Node> getFathers() {
		return fathers;
	}
	
	public Node findChild(String label) {
		
		if (this.label.equals(label))
			return this;
		
		for(Node p: children) {
			return p.findChild(label);
		}
		
		return null;
	}

	public boolean checkDrawCounter(int times) {
		if (drawcount < times) 
			return true;
		return false;
	}
	
	public void incDrawCounter() {
		++drawcount;
	}
	
	public void resetDrawCounter() {
		drawcount = 0;
	}
	
	//////////////////////////
	// Getters and Setters	//
	//////////////////////////
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public TreeMap<Integer, String> getContent() {
		return content;
	}

	public void setContent(TreeMap<Integer, String> content) {
		this.content = content;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
	}
	
}
