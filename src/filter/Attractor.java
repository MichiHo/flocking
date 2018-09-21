package filter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uni.bsc.ba_seminar.Vec;

public class Attractor {
	public AttractorCurve curve;
	public DoubleProperty scale = new SimpleDoubleProperty(1.0);
	public DoubleProperty speed = new SimpleDoubleProperty(0.04);
	public Vec offset = new Vec();
	
	private Group visual;
			
	
	private double t = 0.0, period = Math.PI*2.0;
	
	public Attractor(AttractorCurve curve) {
		this.curve = curve;
		visual = new Group();
		visual.getChildren().add(new Circle(5.0, new Color(0.0, 0.0, 0.0, 0.5)));
	}
	
	public void timeStep(double deltaTime) {
		t = (t+deltaTime*speed.get())%period;
		Vec v = position();
		visual.relocate(v.x, v.y);
	}
	
	public Vec position() {
		return curve.curve(t).mult(scale.get()).add(offset);
	}
	
	
	@FunctionalInterface
	public interface AttractorCurve {
		public Vec curve(double t);
	}
	
	public Group getVisual() {
		return visual;
	}
	
}
