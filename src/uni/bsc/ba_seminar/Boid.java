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

public class Boid {
	public static DoubleProperty velScale = new SimpleDoubleProperty(1.0);
	public static DoubleProperty seperationRadius = new SimpleDoubleProperty(20.0);
	public static DoubleProperty radius = new SimpleDoubleProperty(50.0);
	public static DoubleProperty weightSeperation = new SimpleDoubleProperty(1.5);
	public static DoubleProperty weightCohesion = new SimpleDoubleProperty(1.0);
	public static DoubleProperty weightAlignment = new SimpleDoubleProperty(1.0);
	public static DoubleProperty weightBorder = new SimpleDoubleProperty(2.0);
	public static DoubleProperty weightAttractor = new SimpleDoubleProperty(0.0);
	public static DoubleProperty maxVel = new SimpleDoubleProperty(5.0), 
			maxAccel = new SimpleDoubleProperty(0.1);
	
	public static Rectangle2D borderArea = new Rectangle2D(0.0, 0.0, 500.0, 500.0), 
			finalArea = new Rectangle2D(0.0, 0.0, 500.0, 500.0);
	
	Group visual;
	Polygon polygon;
	Attractor attractor;
	
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
	
	public void setAttractor(Attractor attr) {
		attractor = attr;
	}
	
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
		if(pos.x < borderArea.getMinX()+seperationRadius.get()) {
			// left
			border.x = borderArea.getMinX()+seperationRadius.get() - pos.x;
		} else if (pos.x > borderArea.getMaxX()-seperationRadius.get()) {
			// Right
			border.x = borderArea.getMaxX()-seperationRadius.get() - pos.x;
		}
		
		if(pos.y < borderArea.getMinY()+seperationRadius.get()) {
			// left
			border.y = borderArea.getMinY()+seperationRadius.get() - pos.y;
		} else if (pos.y > borderArea.getMaxY()-seperationRadius.get()) {
			// Right
			border.y = borderArea.getMaxY()-seperationRadius.get() - pos.y;
		}
		border = border.mult(3.0);
		Vec att;
		double attWeight;
		if(attractor != null) {
			att = attractor.position().
					sub(pos).norm().mult(maxVel.get()).sub(vel);
			attWeight = weightAttractor.get();
		}else {
			att = new Vec();
			attWeight = 0.0;
		}
			
		// put more weight to seperation
		double weightSum = weightAlignment.get()+weightCohesion.get()
				+ weightSeperation.get()+weightBorder.get() + attWeight;
		
		if(weightSum>0.0) {
			acceleration = acceleration.add(sep.mult(weightSeperation.get()))
					.add(ali.mult(weightAlignment.get()))
					.add(coh.mult(weightCohesion.get()))
					.add(border.mult(weightBorder.get()))
					.add(att.mult(attWeight))
					.div(weightSum);
		}
		
		acceleration = acceleration.limit(maxAccel.get()).mult(velScale.get()*timeFactor);
		vel = vel.add(acceleration).limit(maxVel.get());
		pos = pos.add(vel.mult(velScale.get()*timeFactor));
		
		acceleration = new Vec();
	}
	
	public void recolor(Paint fill) {
		polygon.setFill(fill);
	}
	
	public void position() {
		if(!finalArea.contains(new Point2D(pos.x,pos.y))) {
			visual.setOpacity(0.5);
		} else {
			visual.setOpacity(1.0);
			visual.relocate(pos.x,pos.y);
			visual.setRotate(Math.toDegrees(Math.atan2(vel.y, vel.x)));
			
		}
	}
}
