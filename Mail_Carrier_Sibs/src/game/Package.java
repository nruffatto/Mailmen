package game;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Package extends Movable implements MouseListener {
	
	public Movable holder; // The Movable holder of the Package
	public Movable lastHolder; // The last holder of the Package
	
	protected int packageThrows; // Number of times the Package has been thrown
	protected int packageDrops; // Number of times the Package has been drop
	protected boolean isDropped;

	public Package(int x, int y) {
		super(x, y);
		HITBOX_WIDTH = 50;
		HITBOX_RATIO = 56.0 / 69.0;
		IMAGE_SCALE = HITBOX_WIDTH / 60.0;
		startPoint = new Point((int)(0),(int)(0));
		IMAGE_WIDTH = (int)(69 * IMAGE_SCALE);// * IMAGE_SCALE
		IMAGE_HEIGHT = (int)(56 * IMAGE_SCALE);
		rec = new Rectangle(x, y, HITBOX_WIDTH, (int)(HITBOX_WIDTH * HITBOX_RATIO));
	}
	
	public void setHolder(Movable m) {
		if (lastHolder != null) {
			isDropped = false;
			lastHolder.hasPackage = false;
		}
		if(m != lastHolder) {
			holder = m;
			lastHolder = m;
			this.isVisible = false;
		}
	}
	
	public void removeHolder() {
		isDropped = true;
		holder.hasPackage = false;
		holder = null;
		this.isVisible = true;
	}
	
	public void throwPackage(int x, int y) {
	    double distanceX = ((x - game.screen.pos.x)/game.screen.currentScale - rec.getCenterX()); //Distance between Player x-coordinate and Mouse x-coordinate
	    double distanceY= ((y - game.screen.pos.y)/game.screen.currentScale - rec.getCenterY()); //Distance between Player y-coordinate and Mouse y-coordinate
	    velX = distanceX/15;
	    velY = (distanceY-225)/15;
		game.score += 5;
	}
	
	@Override
	public void update() {
		if(holder != null) {
			rec.setLocation(holder.rec.x, holder.rec.y);
			if (holder.isTouching(game.dogs[0]) && !holder.isMailbox()) { //Dog Drop
				removeHolder();
			}
		}else {
			super.update();
			if(!isTouching(lastHolder)) {
				lastHolder = null;
			}
		}
		if (this.isTouching(game.mailboxes[0])) { //Win condition
			setHolder(game.mailboxes[0]); 
			game.levelComplete();
		}
		if(!isInAir && wasInAir) { //Drop condition
			game.score += 20;
			packageDrops++;
			if (packageDrops > 3) {
				game.gameOver();
			}
		}
		wasInAir = isInAir;
	}
	
	@Override
	protected void checkCollisionY() {
		isInAir = true;
		Point[] points = getPoints();
		Point[] pastPoints = getPastPoints();
		for(int i = 0; i < points.length; i ++) {
			if(game.map.contains(new Point(points[i].x / Screen.startingLength, points[i].y / Screen.startingLength))) {
				if(game.map.getBlock(points[i].x / Screen.startingLength, points[i].y / Screen.startingLength) != null) {
					lastHolder = null;
					if(points[i].y > pastPoints[i].y) {//points[i].y > pastPoints[i].y
						rec.y -= points[i].y % Screen.startingLength + 1;
						velX = 0;
						isInAir = false;
//						System.out.println(points[i].y % Screen.startingLength + 1);
					}else if(points[i].y < pastPoints[i].y){//if(points[i].y < pastPoints[i].y)
						rec.y += Screen.startingLength - points[i].y % Screen.startingLength + 1;
//						System.out.println("2");
					}else {
//						System.out.println("same");
					}
					velY = 0;
					velX = 0;
					break;
				}
			}
		}
	}
	
	@Override
	public Image getImage() {
		File imageFile;
		if(packageDrops >= 3) {
			imageFile = new File("Sprites/package3.png");
		}else if(packageDrops >= 2) {
			imageFile = new File("Sprites/package2.png");
		}else if(packageDrops >= 1) {
			imageFile = new File("Sprites/package1.png");
		}else {
			imageFile = new File("Sprites/package.png");	
		}
		BufferedImage img;
		try {
			img = ImageIO.read(imageFile);
			return img;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
	public void mousePressed(MouseEvent e) {
		if(holder != null) {
			throwPackage(e.getX(), e.getY());
			removeHolder();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}
	
	@Override
	public boolean isPackage() {
		return true;
	}
	
}
