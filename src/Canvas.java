
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;


public class Canvas extends JPanel {

	private static final long serialVersionUID = -1354251777507926593L;
	private BufferedImage savedImage;
	private List<Node> trees, pool;
	
	public int drawLimit = 2;
	private double startAngle = 0;
	private double spray = Math.PI; // defines the cone of each branch
	private double r = 50; // the length of each edge
	private double nodesize = 10; 
	
	private double scale;
	private boolean draggingRMB = false, draggingLMB = false;
	private Point mouseLastPoint;
	private int shiftX, shiftY, shX, shY;
	
	private Node selectedNode = null;
	
	public Canvas () {
		trees = new ArrayList<Node>();
		pool = new ArrayList<Node>();
		resetCanvas();
		addHandlers();
	}	
	
	public void resetCanvas() {
		scale = 1;
		shX = shiftX = (int) 0;
		shY = shiftY = (int) 0;
	}
	
    private void createBackGround() {
        if (savedImage == null)
            return;
        Graphics2D g = (Graphics2D) savedImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, savedImage.getWidth(), savedImage.getHeight());
        drawTrees(g);
    }
	
    public void paint(Graphics g) {
        if (g != null) {
            int W = getWidth();
            int H = getHeight();
            if ((W > 0) && (H > 0)) {    
            	savedImage = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            	createBackGround();
            	g.drawImage(savedImage, 0, 0, null);
            }
        }
    }
    
    public void drawTrees(Graphics g) {

    	for (Node root: trees) {
    		for (Node node: pool)
    			node.resetDrawCounter();
    		
    		if (root.getPosition() == null) {
	    		root.setPosition(new Point2D.Double(100 + trees.indexOf(root) * 300, 50));
    		}
    		drawNode(g, root, angleMod(startAngle));
    	}
    	
    }
    
    private void drawNode(Graphics g, Node node, double angle) {    	

    	int childrenCount = node.getChildren().size();
    	double step = spray / (double) (childrenCount  + 1);
    	//angle -= angle - spray / 2 + step;
    	
    	//if (childrenCount != 1) 
    		angle = angleMod(angle - spray / 2 + step);
    	Point2D position = node.getPosition();
    	
    	//////////////////////////////
    	//	Drawing current node	//
    	//////////////////////////////
    	
    	int x = (int) Math.round((position.getX() - nodesize / 2 - shiftX) * scale);
		int y = (int) Math.round((position.getY() - nodesize / 2 - shiftY) * scale);
		int size = (int) Math.round(nodesize * scale);
		
		Font font = new Font("Verdana", Font.TRUETYPE_FONT, 12);
		
		if (node.getFathers().size() == 0)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLACK);
		
		if (!node.checkDrawCounter(drawLimit)) {
			// this branch already was drawn, mark it as duplicate
			g.setColor(Color.GRAY);
			g.drawRect(x, y, size, size);
		
		} else {
			
			if (node.getChildren().isEmpty())
				g.fillRect(x, y, size, size);
			else
				g.drawRoundRect(x, y, size, size, size, size);
		}
		
		if (node.equals(selectedNode)) {
			g.drawRoundRect(x - size / 2, y - size / 2, size * 2, size * 2, size * 2, size * 2);
		}
		
		
		
		g.setFont(font);
		g.drawString(node.getLabel(), x + 5, y - 5);
		
    	//////////////////////////////
    	//	Drawing children		//
    	//////////////////////////////
    	
		if (!node.checkDrawCounter(drawLimit)) {
			// if the branch is a duplicate, we don't draw any children
			return;
		}
		
		
    	for (Node child: node.getChildren()) {

    			child.incDrawCounter();
    			Point2D pos = new Point2D.Double(position.getX() + r * Math.sin(angle), position.getY() + r * Math.cos(angle));
    			
    			int x1 = (int) Math.round((position.getX() - shiftX) * scale);
    			int y1 = (int) Math.round((position.getY() - shiftY) * scale);
    			int x2 = (int) Math.round((pos.getX() - shiftX) * scale);
    			int y2 = (int) Math.round((pos.getY() - shiftY) * scale);
    			
    			g.setColor(Color.BLACK);
    			g.drawLine(x1, y1, x2, y2);
    			
    			child.setPosition(pos);
    			drawNode(g, child, angle);
    			angle = angleMod(angle + step);
    	}
    }
    
	public void setData(List<Node> roots, List<Node> nodes) {
		trees = roots;
		pool = nodes;
	}
	
	
	//////////////////
	//	Handlers	//
	//////////////////

	public void addHandlers () {
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					draggingLMB = true;
					mouseLastPoint = e.getPoint();
					
					double x = e.getX() / scale + shiftX;
					double y = e.getY() / scale + shiftY;
					
					selectedNode = findNodeByCoordinate(new Point2D.Double(x, y));
					if(selectedNode != null) {
						repaintCanvas();
						Main.window.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					draggingRMB = true;
					mouseLastPoint = e.getPoint();
					Main.window.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));	
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					draggingLMB = false;
					selectedNode = null;
					repaintCanvas();
					Main.window.setCursor(Cursor.getDefaultCursor());	
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					draggingRMB = false;
					shX += (mouseLastPoint.x - e.getPoint().x) * (1 / scale);
					shY += (mouseLastPoint.y - e.getPoint().y) * (1 / scale);
					Main.window.setCursor(Cursor.getDefaultCursor());				
				}
			}
		});

    	addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (draggingLMB) {

					if (selectedNode != null) {
						double x = e.getX() / scale + shiftX;
						double y = e.getY() / scale + shiftY;
						selectedNode.setPosition(new Point2D.Double(x, y));
					}
					repaintCanvas();
				}
				if (draggingRMB) {
					shiftX = (int) Math.round(shX + (mouseLastPoint.x - e.getPoint().x) * (1 / scale));
					shiftY = (int) Math.round(shY + (mouseLastPoint.y - e.getPoint().y) * (1 / scale));
					repaintCanvas();
				}
			}
		});
    	
    	addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int rot = e.getWheelRotation();
				if (rot < 0) 
					scale += 0.2 * scale;
				else
					scale -= 0.2 * scale;
				shiftX = (int) Math.round(shX + (1 / scale));
				shiftY = (int) Math.round(shY + (1 / scale));
				repaintCanvas();
			}
		});
    	
	}
	
	public void repaintCanvas() {
    	createBackGround();
    	Graphics g = this.getGraphics();
    	g.drawImage(savedImage, 0, 0, null);
	}
	
	private double angleMod(double angle) {
		while (angle < 0)
			angle += Math.PI * 2;
		angle %= Math.PI * 2;
		return angle;
	}
	
	public Node findNodeByCoordinate(Point2D e) {
		for (Node node: trees) {
			Point2D pos = node.getPosition();
			if (pos.distance(e) < nodesize)	{
				//System.out.println("Node selected: " + node.getLabel());
				return node;
			}
		}
		return null;
	}
}
