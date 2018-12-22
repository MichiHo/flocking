package application;

import java.util.Collection;

import filter.Attractor;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * A single Boid for the simulation, with position and velocity.
 * The contained flocking simulation uses the parameters from the 
 * global {@link DataModel} in {@link MainWindow#data}.
 * @author Michael Hochmuth
 *
 */
public class Boid {
	/** Used for Attractor-Following */
	public static DoubleProperty steeringRadius = new SimpleDoubleProperty(60.0);
			
	// Painting
	Group visual;
	Polygon polygon;
	
	// Simulation
	/** If true, this Boid will also follow the Attractor */
	public boolean useAttractor;
	Vector2D pos, nextPos;
	Vector2D vel,nextVel , acceleration = Vector2D.ZERO;
	
	/** A new boid at the given position with zero velocity. */
	public Boid(double x, double y) {
		visual = new Group();
		pos = new Vector2D(x,y);
		vel = Vector2D.ZERO;
		
		nextPos = pos;
		nextVel = vel;
		
		polygon = new Polygon(0.0,4.0,10.0,0.0,0.0,-4.0);
		polygon.setStroke(null);
		polygon.setFill(Color.GREEN);
		visual.getChildren().add(polygon);
		update();
	}
	
	/** The JavaFX Visualization of this Boid */
	public Group getVisual() {return visual;}
	
	/** The position of this boid */
	public Vector2D getPos() {return pos;}
	
	/** The velocity of this boid */
	public Vector2D getVel() {return vel;}
	
	/**
	 * Calculate updated position based on physics and flocking from the
	 * given boids. This doesn't apply the updated position to the boid yet,
	 * for that (and the visual update) call {@link Boid#update()}.
	 * @param boids			All Boids in the scene
	 * @param timeFactor	1.0 for the fixed physics Framerate
	 */
	public void simulate(Collection<Boid> boids, double timeFactor) {
		
		// Seperation
		// Alignment
		// Cohesion
		
		Vector2D sep = Vector2D.ZERO;
		Vector2D ali = Vector2D.ZERO;
		Vector2D coh = Vector2D.ZERO;
		double dist;
		Vector2D posSum = Vector2D.ZERO, velSum = Vector2D.ZERO;
		int count = 0;
		for(Boid b : boids) {
			dist = b.getPos().distance(getPos());
			if(dist<=0.00001) continue;
			
			// Seperation
			if(dist < MainWindow.data.getSeperationRadius()) {
				// Richtung weg vom Boid, normiert und mit 1/abstand gewichtet
				sep = sep.add(pos.subtract(b.pos).normalize().scalarMultiply(1.0/dist));
			}
			
			// Alignment & Cohesion
			if(dist < MainWindow.data.getRadius()) {
				++count;
				posSum = posSum.add(b.getPos());
				velSum = velSum.add(b.getVel().scalarMultiply(1.0/dist));
			}
			
		}
		
		// sep tries to steer away from too close boids
		// norm it to maxVel
		if(sep.getNormSq()!=0.0) {
			sep = sep.normalize()
					.scalarMultiply(MainWindow.data.getMaxVel())
					.subtract(vel);
		}
		
		// velSum contains the dominant velocity direction after
		// norming it to maxVel
		if(velSum.getNormSq()!=0.0) {
			ali = velSum.normalize()
					.scalarMultiply(MainWindow.data.getMaxVel())
					.subtract(vel);
		}	
		
		// posSum.div(count) gives average position of swarm
		// subtract position for direction and norm to maxVel
		if(count > 0) {
			coh = posSum.scalarMultiply(1.0/count)
					.subtract(pos)
					.normalize()
					.scalarMultiply(MainWindow.data.getMaxVel())
					.subtract(vel);
		}
		
		Vector2D border = Vector2D.ZERO;
		if(pos.getX() < MainWindow.borderArea.getMinX()+MainWindow.data.getSeperationRadius()) {
			// left
			border = new Vector2D(
					MainWindow.borderArea.getMinX()+MainWindow.data.getSeperationRadius() 
					- pos.getX(), border.getY());
		} else if (pos.getX() > MainWindow.borderArea.getMaxX()-MainWindow.data.getSeperationRadius()) {
			// Right
			border = new Vector2D(
					MainWindow.borderArea.getMaxX()-MainWindow.data.getSeperationRadius() 
					- pos.getX(), border.getY());
		}
		
		if(pos.getY() < MainWindow.borderArea.getMinY()+MainWindow.data.getSeperationRadius()) {
			// left
			border = new Vector2D(border.getX(), 
					MainWindow.borderArea.getMinY()+MainWindow.data.getSeperationRadius() 
					- pos.getY());
		} else if (pos.getY() > MainWindow.borderArea.getMaxY()-MainWindow.data.getSeperationRadius()) {
			// Right
			border = new Vector2D(border.getX(),
					MainWindow.borderArea.getMaxY()-MainWindow.data.getSeperationRadius() 
					- pos.getY());
		}
		border = border.scalarMultiply(3.0);
		Vector2D att;
		double attWeight;
		if(useAttractor) {
			Attractor a = MainWindow.data.getAttractorType().attractor;
			double attrDist = a.position().distance(pos);
			double f = 1.0;
			if(attrDist< steeringRadius.get()) {
				f = attrDist / steeringRadius.get();
			}
			att = a.position()
					.subtract(pos)
					.normalize()
					.scalarMultiply(MainWindow.data.getMaxVel()*f)
					.subtract(vel);
			attWeight = MainWindow.data.getWeightAttractor();
		}else {
			att = Vector2D.ZERO;
			attWeight = 0.0;
		}
			
		// put more weight to seperation
		double weightSum = MainWindow.data.getWeightAlignment()+MainWindow.data.getWeightCohesion()
				+ MainWindow.data.getWeightSeperation()+MainWindow.data.getWeightBorder() + attWeight;
		
		if(weightSum>0.0) {
			acceleration = acceleration.add(sep.scalarMultiply(MainWindow.data.getWeightSeperation()))
					.add(ali.scalarMultiply(MainWindow.data.getWeightAlignment()))
					.add(coh.scalarMultiply(MainWindow.data.getWeightCohesion()))
					.add(border.scalarMultiply(MainWindow.data.getWeightBorder()))
					.add(att.scalarMultiply(attWeight))
					.scalarMultiply(1.0/weightSum);
		}
		
		// Limit accel to maxAccel and scale it with velScale*time
		acceleration = limit(acceleration,MainWindow.data.getMaxAccel())
				.scalarMultiply(MainWindow.data.getVelScale()*timeFactor);
		
		// add accel to vel and limit vel to maxVel
		nextVel = limit(vel.add(acceleration),MainWindow.data.getMaxVel());
		
		// add vel to pos
		nextPos = pos.add(nextVel.scalarMultiply(
				MainWindow.data.getVelScale()*timeFactor));
		
		if(MainWindow.data.isBorderFlip()) {
			if(nextPos.getX() < 0) 
				nextPos = new Vector2D(
						nextPos.getX() + MainWindow.finalArea.getWidth(),nextPos.getY());
			
			if(nextPos.getY() < 0) 
				nextPos = new Vector2D(
						nextPos.getX(),nextPos.getY() + MainWindow.finalArea.getHeight());
			nextPos = new Vector2D(nextPos.getX()%MainWindow.finalArea.getWidth(), nextPos.getY() % MainWindow.finalArea.getHeight());
		}
		
		acceleration = Vector2D.ZERO;
	}
	
	/** Apply the results of the physics-step */
	public void update() {
		pos = nextPos;
		vel = nextVel;
		
		if(!MainWindow.finalArea.contains(new Point2D(pos.getX(),pos.getY()))) {
			visual.setOpacity(0.5);
		} else {
			visual.setOpacity(1.0);
			visual.relocate(pos.getX(),pos.getY());
			visual.setRotate(Math.toDegrees(Math.atan2(vel.getY(), vel.getX())));
			
		}
	}
	
	/** Set the Fill used to paint this Boid */
	public void setFill(Paint fill) {
		polygon.setFill(fill);
	}
	
	/**
	 * Shorten input to the length of maxMagnitude if it is bigger.
	 * 
	 * @return 	input, if it is shorter than maxMagnitude, or a scaled version
	 * 			that has exactly this length
	 */
	private Vector2D limit(Vector2D input, double maxMagnitude) {
		double m = input.getNorm();
		if(m>maxMagnitude)
			return input.scalarMultiply(maxMagnitude/m);
		else
			return input;
	}
	
}
