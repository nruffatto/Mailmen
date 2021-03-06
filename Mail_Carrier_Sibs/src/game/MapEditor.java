package game;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MapEditor extends TimerTask implements MouseListener, KeyListener, ActionListener, MouseWheelListener, MouseMotionListener, ItemListener {

	public static void main(String[] args) {
		MapEditor mE = new MapEditor();
	}
	
	public static final int DEFAULT_BLOCK_SIZE = 64;
	public static final int TIME_STEP = 50;
	public static final int ZOOM_STEP = 5;
	
	private Map map = new Map(12, 12);
	private Stack<Map> mapStack = new Stack<>();
	private EditorScreen screen = new EditorScreen(map);
	private JFrame editorFrame;
	private Container contentPane;
	private JPanel editorPanel;
	private JTextField fileField;
	private JButton openButton;
	private JButton saveButton;
	private JTextField mapWidthField;
	private JTextField mapHeightField;
	private JButton resizeButton;
	private JLabel messageLabel;
	private Timer timer = new Timer();
	
	private int editorPanelWidth = 200;
	
	public Point pos = new Point(0, 0);
	
	private int mouseX;
	private int mouseY;
	
	private boolean ctrlPressed = false; // keyCode is 17
	private boolean altPressed = false;	// keyCode is 18
	private boolean zPressed = false; // keyCode is 90
	private boolean rightClickPressed = false;
	private boolean leftClickPressed = false;
	
	private boolean canInsertBlocks = true;
	
	private int currentMovableIndex = -1;// `: 192, 1: 49, 2: 50, 3: 51, ...
	
	String currentFileName;
	
	private String[] imageFileNames = {
			"dirt/000","dirt/001","dirt/002","dirt/003",
			"grass/000","grass/001","grass/002","grass/003",
			"sidewalk/000","sidewalk/001","sidewalk/002","sidewalk/003","none",};
	private JComboBox imageComboBox;
	
	private Block currentBlock;
	
	public MapEditor() {
		editorFrame = new JFrame("Map Editor");
		editorFrame.setLocation(pos);
		editorFrame.setSize(map.getWidth() * DEFAULT_BLOCK_SIZE + editorPanelWidth,
				map.getHeight() * DEFAULT_BLOCK_SIZE + 30);
		editorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = editorFrame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBackground(new Color(224, 224, 224 ));
		editorFrame.setVisible(true);
		
		screen.setLayout(null);
		screen.setBackground(Color.BLUE);
		contentPane.add(screen, BorderLayout.CENTER);

		
		// Start of editorPanel stuff. As of now, we have to resize the jframe using the mouse for it to appear
		editorPanel = new JPanel();
		editorPanel.setBackground(Color.GRAY);
		
		openButton = new JButton("Open File");
		openButton.addActionListener(this);
		saveButton = new JButton("Save File");
		saveButton.addActionListener(this);
		
		fileField = new JTextField("file_name.txt");
		fileField.addActionListener(this);
		fileField.setBounds(0, 0, 100, 20);
		
		mapWidthField = new JTextField(map.getWidth() + "");
		mapWidthField.setBounds(0, 0, 100, 20);
		contentPane.add(editorPanel, BorderLayout.SOUTH);
		
		mapHeightField = new JTextField(map.getHeight() + "");
		mapHeightField.setBounds(0, 0, 100, 20);
		resizeButton = new JButton("Resize Map");
		resizeButton.addActionListener(this);
		
		messageLabel = new JLabel("________");
		
		imageComboBox = new JComboBox(imageFileNames);
		imageComboBox.addItemListener(this);
		
		
		editorPanel.add(messageLabel, BorderLayout.AFTER_LAST_LINE);
		editorPanel.add(fileField, BorderLayout.AFTER_LAST_LINE);
		editorPanel.add(openButton, BorderLayout.AFTER_LAST_LINE);
		editorPanel.add(saveButton, BorderLayout.AFTER_LAST_LINE);
		
		editorPanel.add(mapWidthField, BorderLayout.AFTER_LAST_LINE);
		editorPanel.add(mapHeightField, BorderLayout.AFTER_LAST_LINE);
		editorPanel.add(resizeButton, BorderLayout.AFTER_LAST_LINE);
		editorPanel.add(imageComboBox);
		// End of editorPanel Stuff

		editorFrame.addMouseListener(this);
		editorFrame.addMouseMotionListener(this);
		editorFrame.addMouseWheelListener(this);
		editorFrame.addKeyListener(this);
		editorFrame.setFocusable(true);
		
		currentBlock = new Block("blockimg/" + imageComboBox.getSelectedItem().toString() + ".png");
		
		timer.schedule(this, 0, TIME_STEP);
		storeMapInstance();
		messageLabel.setText("[" + screen.getMouseX() + ", " + screen.getMouseY() + "]");
	}
	
	private void storeMapInstance() { // adds a map to the map stack so that we can undo changes
		if(!mapStack.isEmpty()) {
		}
		if(mapStack.isEmpty() || (!mapStack.isEmpty() && !mapStack.peek().equals(map))) {
			mapStack.push(map.getCopy());
		}
	}
	
	private void undo() { // deletes most recent change 
		if(!mapStack.isEmpty()) {
			map = mapStack.pop().getCopy();
			screen.map = map;
		}
	}

	@Override
	public void run() { // the timer method
		screen.setMousePos(mouseX, mouseY);
		editorFrame.repaint();
	}
	
	public void clearMapStack() {
		while(!mapStack.isEmpty()) {
			mapStack.pop();
		}
	}
	
	private void openFile(String fileName) {
		map.clear();
		clearMapStack();
		messageLabel.setText("opening...");
		File f1 = new File("Maps/" + fileName);
		Scanner s1;
		int width;
		int height;
		int xCoord;
		int yCoord;
		String imageFileName;
		int numBlockPropertiesOfFile;
		boolean[] blockProperties = new boolean[Block.NUMBER_OF_PROPERTIES];
		try {
			s1 = new Scanner(f1);
			width = s1.nextInt();
			mapWidthField.setText(width + "");
			height = s1.nextInt();
			mapHeightField.setText(height + "");
			numBlockPropertiesOfFile = s1.nextInt();
			map.resize(width, height);
			boolean done = false;
			int counter = 0;
			while(!done) {
				String next = s1.next();
				if(next.equals("done")) {
					done = true;
				}else {
					map.insertSpawnPoint(Integer.parseInt(next), s1.nextInt(), counter);
				}
				counter ++;
			}
			while(s1.hasNextLine() && s1.hasNext()) {
				xCoord = s1.nextInt();
				yCoord = s1.nextInt();
				imageFileName = s1.next();
				for(int i = 0; i < numBlockPropertiesOfFile; i ++) {
					blockProperties[i] = s1.nextBoolean();
				}
				map.insertBlock(xCoord, yCoord, new Block(imageFileName, blockProperties));
			}
			messageLabel.setText("done opening!");
		} catch (FileNotFoundException e) {
			messageLabel.setText("File Not Found!");
			fileField.setText("file_name.txt");
		}
		storeMapInstance();
		if(screen.movables[0] != null) {
			screen.movables[0] = new Player(map.getSpawnPoint(0).x, map.getSpawnPoint(0).y, 0);
		}
		if(screen.movables[1] != null) {
			screen.movables[1] = new Player(map.getSpawnPoint(1).x, map.getSpawnPoint(1).y, 1);
		}
		if(screen.movables[2] != null) {
			screen.movables[2] = new Package(map.getSpawnPoint(2).x, map.getSpawnPoint(2).y);
		}
		if(screen.movables[3] != null) {
			screen.movables[3] = new Mailbox(map.getSpawnPoint(3).x, map.getSpawnPoint(3).y);
		}
		if(screen.movables[4] != null) {
			screen.movables[4] = new Dog(map.getSpawnPoint(4).x, map.getSpawnPoint(4).y, 200);
		}
	}
	
	public void saveFile(String fileName) {
		messageLabel.setText("saving...");
		int width = map.getWidth();
		int height = map.getHeight();
		int xCoord;
		int yCoord;
		String imageFileName;
		boolean[] blockProperties = new boolean[Block.NUMBER_OF_PROPERTIES];
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("Maps/" + fileName));
			writer.write(width + " " + height + " " + Block.NUMBER_OF_PROPERTIES);
			for(int i = 0; i < map.getSpawnPointsSize(); i ++) {
				if(map.getSpawnPoint(i) != null) {
					writer.write(" " + map.getSpawnPoint(i).x + " " + map.getSpawnPoint(i).y);
				}
			}
			writer.write(" done");
			writer.newLine();
			for(int i = 0; i < width; i ++) {
				for(int j = 0; j < height; j ++) {
					if(map.getBlock(i, j) != null) {
						writer.write(i + " " + j + " " + map.getBlock(i, j).getImageFileName() + " ");
						for(int k = 0; k < Block.NUMBER_OF_PROPERTIES; k ++) {
							if(map.getBlock(i, j).is(k)){
								writer.write("true ");
							}else {
								writer.write("false ");
							}
						}
						writer.newLine();
					}
				}
			}
			writer.flush();
			writer.close();
			messageLabel.setText("done saving!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
	private void insertBlock(int x, int y) { // inserts block or movable at mouse location, checks to make sure block can be inserted there
		x -= screen.pos.x;
		y -= screen.pos.y;
//		System.out.println(x + " " + y);
		if(x < map.getWidth() * screen.getBlockSize() && x > 0 &&
				y < map.getHeight() * screen.getBlockSize() && y > 0 &&
				canInsertBlocks) {
			if(currentMovableIndex >= 0) {
				x = (int)((double)x / screen.currentScale);
				y = (int)((double)y / screen.currentScale);
				map.insertSpawnPoint(x, y, currentMovableIndex);
				if(currentMovableIndex == 0) {
					screen.movables[currentMovableIndex] = new Player(x, y, 0);
				}else if(currentMovableIndex == 1) {
						screen.movables[currentMovableIndex] = new Player(x, y, 1);
				}else if(currentMovableIndex == 2) {
					screen.movables[currentMovableIndex] = new Package(x, y);
				}else if(currentMovableIndex == 3) {
					screen.movables[currentMovableIndex] = new Mailbox(x, y);
				}else if(currentMovableIndex == 4) {
					screen.movables[currentMovableIndex] = new Dog(x, y, 200);
				}
			}else {
				map.insertBlock(x / screen.getBlockSize(), 
						y / screen.getBlockSize(), 
						currentBlock.getCopy());
			}
		}
		storeMapInstance();
	}
	
	private void eraseBlock(int x, int y) { // erases block at mouse location if there is on
		x -= screen.pos.x;
		y -= screen.pos.y;
		if(x < map.getWidth() * screen.getBlockSize() && x > 0 &&
				y < map.getHeight() * screen.getBlockSize() && y > 0) {
			map.insertBlock(x / screen.getBlockSize(), 
					y / screen.getBlockSize(), 
					null);
		}
		storeMapInstance();
	}
	
	private void zoom(int zoomAmount) { // zooms into mouse location
		double newBlockSize = screen.getBlockSize() + ZOOM_STEP * zoomAmount;
		double scale = newBlockSize / screen.getBlockSize();
		screen.pos.x =(int) (- ((mouseX - screen.pos.x) * scale - mouseX));
		screen.pos.y =(int) (- ((mouseY - screen.pos.y) * scale - mouseY));
		screen.setBlockSize((int)newBlockSize);
	}

	@Override
	public void actionPerformed(ActionEvent e) { // handles buttons and fields, and comboboxes
		editorFrame.requestFocus();
		if(e.getSource().equals(openButton)) {
			openFile(fileField.getText());
		}else if(e.getSource().equals(saveButton)) {
			saveFile(fileField.getText());
		}else if(e.getSource().equals(resizeButton)) {
			int width = 0, height = 0;
			try {
				width = Integer.parseInt(mapWidthField.getText());
			} catch(NumberFormatException exception) {
				mapWidthField.setText("int please!");
			}
			try {
				height = Integer.parseInt(mapHeightField.getText());
			} catch(NumberFormatException exception) {
				mapHeightField.setText("int please!");
			}
			if(width > 0 && height > 0) {
				map.resize(width, height);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == 17) {
			ctrlPressed = true;
		}else if (e.getKeyCode() == 18) {
			altPressed = true;
		}else if(ctrlPressed && e.getKeyCode() == 90) {
			undo();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { // Handles movable assignments
		if(e.getKeyCode() == 17) {
			ctrlPressed = false;
		}else if (e.getKeyCode() == 18) {
			altPressed = false;
		}else if (e.getKeyCode() == 192) {
			currentMovableIndex = -1;
		}else if (e.getKeyCode() == 49) {
			currentMovableIndex = 0;
		}else if (e.getKeyCode() == 50) {
			currentMovableIndex = 1;
		}else if (e.getKeyCode() == 51) {
			currentMovableIndex = 2;
		}else if (e.getKeyCode() == 52) {
			currentMovableIndex = 3;
		}else if (e.getKeyCode() == 53) {
			currentMovableIndex = 4;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		messageLabel.setText("[" + screen.getMouseX() + ", " + screen.getMouseY() + "]"); // Displays coordinates of mouse
		if(leftClickPressed) {
			if(ctrlPressed) { // Pans the screen for <Ctrl> drag and inserts blocks otherwise
				canInsertBlocks = false;
				screen.pos.x += e.getX() - 7 - mouseX;
				screen.pos.y += e.getY() - 30 - mouseY;
				mouseX = e.getX() - 7;
				mouseY = e.getY() - 30;
			}else {
				insertBlock(e.getX() - 7, e.getY() - 30);
			}
		}
		else if(rightClickPressed) {
			if(ctrlPressed) {
			}else {
				eraseBlock(e.getX() - 7, e.getY() - 30);
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) { //Changes mouse location and displays coordinates on the screen
		messageLabel.setText("[" + screen.getMouseX() + ", " + screen.getMouseY() + "]");
		mouseX = e.getX() - 7;
		mouseY = e.getY() - 30;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		zoom(- e.getWheelRotation());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) { // updates relevant properties
		mouseX = e.getX() - 7;
		mouseY = e.getY() - 30;
		if(e.getButton() == MouseEvent.BUTTON1) {
			leftClickPressed = true;
		}
		else if(e.getButton() == MouseEvent.BUTTON3) {
			rightClickPressed = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) { // updates relevant properties
		if(e.getButton() == MouseEvent.BUTTON1) {
			leftClickPressed = false;
			insertBlock(e.getX() - 7, e.getY() - 30);
		}
		else if(e.getButton() == MouseEvent.BUTTON3) {
			rightClickPressed = false;
			eraseBlock(e.getX() - 7, e.getY() - 30);
		}
		canInsertBlocks = true;
	}

	@Override
	public void itemStateChanged(ItemEvent e) { // for switching between block images
		editorFrame.requestFocus();
		if(e.getSource() == imageComboBox) {
			if(imageComboBox.getSelectedItem().toString().equals("none")) {
				currentBlock.setImage("none");
			}else {
				currentBlock.setImage("blockimg/" + imageComboBox.getSelectedItem().toString() + ".png");
			}
		}
	}

}
