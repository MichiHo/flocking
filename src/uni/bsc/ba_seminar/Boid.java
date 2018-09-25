package uni.bsc.ba_seminar;

import java.util.Collection;

import filter.Attractor;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;

/**
 * A single Boid for the simulation, with position and velocity.
 * The contained flocking simulation uses the parameters from the 
 * global {@link DataModel} in {@link MainWindow#data}.
 * @author Michael Hochmuth
 *
 */
public class Boid {
	
	
	public static Rectangle2D borderArea = new Rectangle2D(0.0, 0.0, 500.0, 500.0), 
			finalArea = new Rectangle2D(0.0, 0.0, 500.0, 500.0);
	public static DoubleProperty steeringRadius = new SimpleDoubleProperty(60.0);
			
	
	Group visual;
	Polygon polygon;
	public boolean useAttractor;
	
	Vec pos, nextPos;
	Vec vel,nextVel , acceleration = new Vec();
	
	public Boid(double x, double y) {
		visual = new Group();
		pos = new Vec(x,y);
		vel = new Vec();
		
		nextPos = pos;
		nextVel = vel;
		
		polygon = new Polygon(0.0,4.0,10.0,0.0,0.0,-4.0);
		polygon.setStroke(null);
		polygon.setFill(Color.GREEN);
		visual.getChildren().add(polygon);
		position();
	}
	
	public Group getVisual() {return visual;}
	
	public Vec getPos() {return pos;}
	
	public Vec getVel() {return vel;}
	
	/**
	 * Calculate updated position based on physics and flocking from the
	 * given boids. This doesn't apply the updated position to the boid yet,
	 * for that (and the visual update) call {@link Boid#position()}.
	 * @param boids
	 * @param timeFactor
	 */
	public void update(Collection<Boid> boids, double timeFactor) {
		
		// Seperation
		// Alignment
		// Cohesion
		
		Vec sep = new Vec();
		Vec ali = new Vec();
		Vec coh = new Vec();
		double dist;
		Vec posSum = new Vec(), velSum = new Vec();
		int count = 0;
		for(Boid b : boids) {
			dist = b.getPos().distance(getPos());
			if(dist<=0.00001) continue;
			
			// Seperation
			if(dist < MainWindow.data.getSeperationRadius()) {
				// Richtung weg vom Boid, normiert und mit 1/abstand gewichtet
				sep = sep.add(pos.sub(b.pos).norm().div(dist));
			}
			
			// Alignment & Cohesion
			if(dist < MainWindow.data.getRadius()) {
				++count;
				posSum = posSum.add(b.getPos());
				velSum = velSum.add(b.getVel().div(dist));
			}
			
		}
		
		// sep tries to steer away from too close boids
		// norm it to maxVel
		if(!sep.isNull()) {
			sep = sep.norm().mult(MainWindow.data.getMaxVel()).sub(vel);
		}
		
		// velSum contains the dominant velocity direction after
		// norming it to maxVel
		if(!velSum.isNull()) {
			ali = velSum.norm().mult(MainWindow.data.getMaxVel()).sub(vel);
		}	
		
		// posSum.div(count) gives average position of swarm
		// subtract position for direction and norm to maxVel
		if(count > 0) {
			coh = posSum.div(count).sub(pos).norm().mult(MainWindow.data.getMaxVel()).sub(vel);
		}
		
		Vec border = new Vec();
		if(pos.x < borderArea.getMinX()+MainWindow.data.getSeperationRadius()) {
			// left
			border.x = borderArea.getMinX()+MainWindow.data.getSeperationRadius() - pos.x;
		} else if (pos.x > borderArea.getMaxX()-MainWindow.data.getSeperationRadius()) {
			// Right
			border.x = borderArea.getMaxX()-MainWindow.data.getSeperationRadius() - pos.x;
		}
		
		if(pos.y < borderArea.getMinY()+MainWindow.data.getSeperationRadius()) {
			// left
			border.y = borderArea.getMinY()+MainWindow.data.getSeperationRadius() - pos.y;
		} else if (pos.y > borderArea.getMaxY()-MainWindow.data.getSeperationRadius()) {
			// Right
			border.y = borderArea.getMaxY()-MainWindow.data.getSeperationRadius() - pos.y;
		}
		border = border.mult(3.0);
		Vec att;
		double attWeight;
		if(useAttractor) {
			Attractor a = MainWindow.data.getAttractorType().attractor;
			double attrDist = a.position().distance(pos);
			double f = 1.0;
			if(attrDist< steeringRadius.get()) {
				f = attrDist / steeringRadius.get();
			}
			att = a.position().
					sub(pos).norm().mult(MainWindow.data.getMaxVel()*f).sub(vel);
			attWeight = MainWindow.data.getWeightAttractor();
		}else {
			att = new Vec();
			attWeight = 0.0;
		}
			
		// put more weight to seperation
		double weightSum = MainWindow.data.getWeightAlignment()+MainWindow.data.getWeightCohesion()
				+ MainWindow.data.getWeightSeperation()+MainWindow.data.getWeightBorder() + attWeight;
		
		if(weightSum>0.0) {
			acceleration = acceleration.add(sep.mult(MainWindow.data.getWeightSeperation()))
					.add(ali.mult(MainWindow.data.getWeightAlignment()))
					.add(coh.mult(MainWindow.data.getWeightCohesion()))
					.add(border.mult(MainWindow.data.getWeightBorder()))
					.add(att.mult(attWeight))
					.div(weightSum);
		}
		
		acceleration = acceleration.limit(MainWindow.data.getMaxAccel()).mult(MainWindow.data.getVelScale()*timeFactor);
		nextVel = vel.add(acceleration).limit(MainWindow.data.getMaxVel());
		nextPos = pos.add(nextVel.mult(MainWindow.data.getVelScale()*timeFactor));
		if(MainWindow.data.isBorderFlip()) {
			if(nextPos.x < 0) nextPos.x += finalArea.getWidth();
			if(nextPos.y < 0) nextPos.y += finalArea.getHeight();
			nextPos = new Vec(nextPos.x%finalArea.getWidth(), nextPos.y % finalArea.getHeight());
		}
		
		acceleration = new Vec();
	}
	
	public void position() {
		pos = nextPos;
		vel = nextVel;
		
		if(!finalArea.contains(new Point2D(pos.x,pos.y))) {
			visual.setOpacity(0.5);
		} else {
			visual.setOpacity(1.0);
			visual.relocate(pos.x,pos.y);
			visual.setRotate(Math.toDegrees(Math.atan2(vel.y, vel.x)));
			
		}
	}
	public void setFill(Paint fill) {
		polygon.setFill(fill);
	}
	
}
