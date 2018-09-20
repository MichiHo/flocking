package uni.bsc.baproj;

import java.util.Collection;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Boid {
	public static DoubleProperty velScale = new SimpleDoubleProperty(1.0);
	public static DoubleProperty seperationRadius = new SimpleDoubleProperty(20.0);
	public static DoubleProperty radius = new SimpleDoubleProperty(50.0);
	public static DoubleProperty weightSeperation = new SimpleDoubleProperty(1.5);
	public static DoubleProperty weightCohesion = new SimpleDoubleProperty(1.0);
	public static DoubleProperty weightAlignment = new SimpleDoubleProperty(1.0);
	public static DoubleProperty weightBorder = new SimpleDoubleProperty(2.0);
	public static DoubleProperty maxVel = new SimpleDoubleProperty(5.0), 
			maxAccel = new SimpleDoubleProperty(0.1);
	
	public static Rectangle2D area = new Rectangle2D(0.0, 0.0, 500.0, 500.0);
	
	Group visual;
	Polygon polygon;
	
	Vec pos;
	Vec vel = new Vec(), acceleration = new Vec();
	
	public Boid(double x, double y) {
		visual = new Group();
		pos = new Vec(x,y);
		polygon = new Polygon(0.0,4.0,10.0,0.0,0.0,-4.0);
		polygon.setStroke(null);
		polygon.setFill(Color.GREEN);
		visual.getChildren().add(polygon);
		position();
	}
	
	public Group getVisual() {return visual;}
	
	public Vec getPos() {return pos;}
	
	public Vec getVel() {return vel;}
	
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
			if(dist < seperationRadius.get()) {
				// Richtung weg vom Boid, normiert und mit 1/abstand gewichtet
				sep = sep.add(pos.sub(b.pos).norm().div(dist));
			}
			
			// Alignment & Cohesion
			if(dist < radius.get()) {
				++count;
				posSum = posSum.add(b.getPos());
				velSum = velSum.add(b.getVel().div(dist));
			}
			
		}
		
		// sep tries to steer away from too close boids
		// norm it to maxVel
		if(!sep.isNull()) {
			sep = sep.norm().mult(maxVel.get()).sub(vel);
		}
		
		// velSum contains the dominant velocity direction after
		// norming it to maxVel
		if(!velSum.isNull()) {
			ali = velSum.norm().mult(maxVel.get()).sub(vel);
		}	
		
		// posSum.div(count) gives average position of swarm
		// subtract position for direction and norm to maxVel
		if(count > 0) {
			coh = posSum.div(count).sub(pos).norm().mult(maxVel.get()).sub(vel);
		}
		
		Vec border = new Vec();
		if(pos.x < area.getMinX()+seperationRadius.get()) {
			// left
			border.x = area.getMinX()+seperationRadius.get() - pos.x;
		} else if (pos.x > area.getMaxX()-seperationRadius.get()) {
			// Right
			border.x = area.getMaxX()-seperationRadius.get() - pos.x;
		}
		
		if(pos.y < area.getMinY()+seperationRadius.get()) {
			// left
			border.y = area.getMinY()+seperationRadius.get() - pos.y;
		} else if (pos.y > area.getMaxY()-seperationRadius.get()) {
			// Right
			border.y = area.getMaxY()-seperationRadius.get() - pos.y;
		}
		border = border.mult(3.0);
		
		// put more weight to seperation
		double weightSum = weightAlignment.get()+weightCohesion.get()
				+ weightSeperation.get()+weightBorder.get();
		
		if(weightSum>0.0) {
			acceleration = acceleration.add(sep.mult(weightSeperation.get()))
					.add(ali.mult(weightAlignment.get()))
					.add(coh.mult(weightCohesion.get()))
					.add(border.mult(weightBorder.get()))
					.div(weightSum);
		}
		
		// Scale Accel relative to FPS
		acceleration = acceleration.limit(maxAccel.get()).mult(timeFactor);
		
		vel = vel.add(acceleration).limit(maxVel.get());
		acceleration = new Vec();
		// Scale Vel relative to FPS
		pos = pos.add(vel.mult(velScale.get()*timeFactor));
		position();
	}
	
	
	public void position() {
		visual.relocate(pos.x,pos.y);
		visual.setRotate(Math.toDegrees(Math.atan2(vel.y, vel.x)));
	}
}
