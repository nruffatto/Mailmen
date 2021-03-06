package game;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Player extends Movable implements KeyListener{
	public static final int NUMBER_OF_STATS = 2;
	public static final int SPEED_INDEX = 0;
	public static final int JUMP_INDEX = 1;
	public static final int NORMAL_INDEX = 0;
	public static final int HAS_PACKAGE_INDEX = 1;	
	private int packageState = NORMAL_INDEX;
	private int[][] stats = new int[2][NUMBER_OF_STATS];
	
	private static final int IDLE_INDEX = 0;
	private static final int RUN_INDEX = 1;
	private static final int PACKAGE_RUN_INDEX = 2;
	private static final int PACKAGE_IDLE_INDEX = 3;
	private static final int CROUCH_IDLE_INDEX = 4;
	private static final int CROUCH_RUN_INDEX = 5;
	private static final int DEFAULT_INDEX = 6;
	
	private int speed = 10;
	private int jumpingSpeed = 25;
	protected int playerState;
	private double crouchScale = 0.4;
	
	private int left;
	private int right;
	private int up;
	private int down;
	

	
	protected int playerNumber;
	
	private boolean leftKeyPressed = false;
	private boolean rightKeyPressed = false;
	
	private boolean crouchKeyPressed = false;
	protected boolean isCrouched = false;
	private boolean isJumping = false;

	private Animation[][] playerImages = { // Gets animations for both players
			{	new Animation("Sprites/mailman1idle_8_.png",138,135),
				new Animation("Sprites/mailman1run_8_.png",138,135),
				new Animation("Sprites/mailman1boxrun_16_.png",138,135),
				new Animation("Sprites/mailman1boxidle_8_.png",138,135),
				new Animation("Sprites/mailman1crouchidle_6_.png",138,135),
				new Animation("Sprites/mailman1crouchwalk_6_.png",138,135),  
				new Animation("Sprites/mailman1_1_.png",138,135)},
			{
				new Animation("Sprites/mailman2idle_8_.png",138,135),
				new Animation("Sprites/mailman2run_8_.png",138,135),
				new Animation("Sprites/mailman2boxrun_16_.png",138,135),
				new Animation("Sprites/mailman2boxidle_8_.png",138,135),
				new Animation("Sprites/mailman2crouchidle_6_.png",138,135),
				new Animation("Sprites/mailman2crouchwalk_6_.png",138,135), 
				new Animation("Sprites/mailman2_1_.png",138,135)
			}
	};
	

/* }>Key Codes<{
 * get with KeyEvent.VK_<whatever key you want> like VK_A, VK_W, or VK_S
 */
	
	public Player(int x, int y, int playerNumber) {
		super(x, y);
		HITBOX_WIDTH = 50;
		HITBOX_RATIO = 127.0 / 60.0;
		IMAGE_SCALE = HITBOX_WIDTH / 60.0;
		startPoint = new Point((int)(38 * IMAGE_SCALE),(int)(7 * IMAGE_SCALE));
		IMAGE_WIDTH = (int)(138 * IMAGE_SCALE);// * IMAGE_SCALE
		IMAGE_HEIGHT = (int)(135 * IMAGE_SCALE);
		rec = new Rectangle(x, y, HITBOX_WIDTH, (int)(HITBOX_WIDTH * HITBOX_RATIO));
		this.playerNumber = playerNumber;

		stats[NORMAL_INDEX][SPEED_INDEX] = 10;
		stats[HAS_PACKAGE_INDEX][SPEED_INDEX] = 5;
		stats[NORMAL_INDEX][JUMP_INDEX] = 25;
		stats[HAS_PACKAGE_INDEX][JUMP_INDEX] = 20;
	}
	
	public void getControls() {
		switch (playerNumber) {
			case 0:
				left = KeyEvent.VK_A;
				right = KeyEvent.VK_D;
				up = KeyEvent.VK_W;
				down = KeyEvent.VK_S;
				break;
			case 1: 
				left = KeyEvent.VK_LEFT;
				right = KeyEvent.VK_RIGHT;
				up = KeyEvent.VK_UP;
				down = KeyEvent.VK_DOWN;
				break;
			default: break;
		}
	}
	
	@Override
	public void update() {
		updateProperties();
		super.update();
	}
	
	public void updateProperties() { // Set player state and stats
		if(game.packages[0].holder == this) {
			hasPackage = true;
		}else {
			hasPackage = false;
		}
		if(hasPackage) {
			packageState = HAS_PACKAGE_INDEX;
			playerState = PACKAGE_IDLE_INDEX;
		}else {
			packageState = NORMAL_INDEX;
			if(!isCrouched) {
				playerState = IDLE_INDEX;
			}else {
				playerState = CROUCH_IDLE_INDEX;
			}
		}
		speed = stats[packageState][SPEED_INDEX];
		jumpingSpeed = stats[packageState][JUMP_INDEX];
		if(isJumping && !isInAir) {
			isInAir = true;
			velY = - jumpingSpeed;
		}
		if(crouchKeyPressed && !isCrouched) {
			if(!hasPackage) {
				crouch();
				playerState = CROUCH_IDLE_INDEX;
				isCrouched = true;
			}
		}else if(!crouchKeyPressed && isCrouched && !checkCollisionCrouch()) {
			unCrouch();
			playerState = IDLE_INDEX;
			isCrouched = false;
		}
		if(game.packages[0].holder == null && !isCrouched &&
				!this.isTouching(game.dogs[0])) {
			hasPackage = false;
			if(this.isTouching(game.packages[0]) || game.packages[0].isTouching(this)) {
				game.packages[0].setHolder(this);
				hasPackage = true;
			}
		}
		if(leftKeyPressed && !rightKeyPressed) {
			velX = -speed;
			if(!isCrouched) {
				if (hasPackage) {
					playerState = PACKAGE_RUN_INDEX;
				}else {
					playerState = RUN_INDEX;
				}
			}else {
				playerState = CROUCH_RUN_INDEX;
			}
			
		}else if(!leftKeyPressed && rightKeyPressed) {
			velX = speed;
			if(!isCrouched) {
				if (hasPackage) {
					playerState = PACKAGE_RUN_INDEX;
				}else {
					playerState = RUN_INDEX;
				}
			}else {
				playerState = CROUCH_RUN_INDEX;
			}
		}else {
			velX = 0;
		}
		
		switch(playerState) { // changes animation frame
			case 0: playerImages[playerNumber][IDLE_INDEX].nextFrame(); break;
			case 1: playerImages[playerNumber][RUN_INDEX].nextFrame(); break;
			case 2: playerImages[playerNumber][PACKAGE_RUN_INDEX].nextFrame(); break;
			case 3: playerImages[playerNumber][PACKAGE_IDLE_INDEX].nextFrame(); break;
			case 4: playerImages[playerNumber][CROUCH_IDLE_INDEX].nextFrame(); break;
			case 5: playerImages[playerNumber][CROUCH_RUN_INDEX].nextFrame(); break;
			default: break;
		}
	}
	
	private void crouch() {
		int crouchDist = (int)(rec.height - rec.height * crouchScale);
		rec.height -= crouchDist;
		rec.y += crouchDist;
		startPoint.y += crouchDist;
	}
	
	private void unCrouch() {
		int crouchDist = (int)(rec.height / crouchScale - rec.height);
		rec.height += crouchDist;
		rec.y -= crouchDist;
		startPoint.y -= crouchDist;
	}
	
	private boolean checkCollisionCrouch() {
		boolean collision = false;
		unCrouch();
		Point[] points = getPoints();
		Point[] pastPoints = getPastPoints();
		for(int i = 0; i < points.length; i ++) {
			if(game.map.contains(new Point(points[i].x / Screen.startingLength, points[i].y / Screen.startingLength))) {
				if(game.map.getBlock(points[i].x / Screen.startingLength, points[i].y / Screen.startingLength) != null) {
					collision = true;
					break;
				}
			}
		}
		crouch();
		return collision;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		getControls();
		if(e.getKeyCode() == left) {
				leftKeyPressed = true;
				isFacingLeft = true;
		}
		if(e.getKeyCode() == right) {
				rightKeyPressed = true;
				isFacingLeft = false;
		}
		if(e.getKeyCode() == up) {
			isJumping = true;
		}
		if(e.getKeyCode() == down) {
			crouchKeyPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		getControls();
		if(e.getKeyCode() == left) {
			leftKeyPressed = false;
		}
		if(e.getKeyCode() == right) {
			rightKeyPressed = false;
		}
		if(e.getKeyCode() == up) {
			isJumping = false;
		}
		if(e.getKeyCode() == down) {
			crouchKeyPressed = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public Image getImage() {
		switch(playerState) {
			case 0: return playerImages[playerNumber][IDLE_INDEX].getImage();
			case 1: return playerImages[playerNumber][RUN_INDEX].getImage();
			case 2: return playerImages[playerNumber][PACKAGE_RUN_INDEX].getImage();
			case 3: return playerImages[playerNumber][PACKAGE_IDLE_INDEX].getImage();
			case 4: return playerImages[playerNumber][CROUCH_IDLE_INDEX].getImage();
			case 5: return playerImages[playerNumber][CROUCH_RUN_INDEX].getImage();
			default: return playerImages[playerNumber][DEFAULT_INDEX].getImage();
		}
	}
	
	@Override
	public boolean isPlayer() {
		return true;
	}

}
