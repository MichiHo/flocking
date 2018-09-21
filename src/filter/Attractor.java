package filter;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.bsc.ba_seminar.Vec;

public class Attractor {
	public AttractorCurve curve;
	public DoubleProperty scale = new SimpleDoubleProperty(1.0);
	public DoubleProperty speed = new SimpleDoubleProperty(0.04);
	public Vec offset = new Vec();
			
	
	private double t = 0.0, period = Math.PI*2.0;
	
	public Attractor(AttractorCurve curve) {
		this.curve = curve;
		
	}
	
	public void timeStep(double deltaTime) {
		t = (t+deltaTime*speed.get())%period;
	}
	
	public Vec position() {
		return curve.curve(t).mult(scale.get()).add(offset);
	}
	
	
	@FunctionalInterface
	public interface AttractorCurve {
		public Vec curve(double t);
	}
	
}
