package filter;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Describes a point following a curve given as
 * {@link AttractorCurve}-function. The function
 * is assumed periodic (by default to <code>2*PI</code>).
 * 
 * Has as internal state the current time-position and
 * is updated via {@link Attractor#timeStep(double)} given
 * the time-difference that passed. 
 * 
 * Retrieve the position any time via {@link Attractor#position()}.
 * 
 * @author Michael Hochmuth
 *
 */
public class Attractor {
	/** Curve used by this Attractor */
	public AttractorCurve curve;
	
	/** Scale multiplied to the curve's position before adding the offset */
	public final DoubleProperty scale = new SimpleDoubleProperty(1.0);
	
	/** Allows adjustment to the speed the curve is followed in time. */
	public final DoubleProperty speed = new SimpleDoubleProperty(0.04);
	
	/** Offset added to the curve's current position. */
	public Vector2D offset = Vector2D.ZERO;
	
	private Group visual;
	private double t = 0.0, period = Math.PI*2.0;
	
	/**
	 * Creates a new Attractor driving the given {@link AttractorCurve}
	 * function in the Range <code>[0,2*PI]</code> as time progresses.
	 * 
	 * @param curve Underlying curve to use.
	 */
	public Attractor(AttractorCurve curve) {
		this.curve = curve;
		visual = new Group();
		visual.getChildren().add(new Circle(5.0, new Color(0.0, 0.0, 0.0, 0.5)));
	}
	
	/**
	 * Advance the curves state by the given time-difference.
	 * The speed-value is taken into account, altering the period
	 * of the curve's position over time.
	 * 
	 * @param deltaTime
	 */
	public void timeStep(double deltaTime) {
		t = (t+deltaTime*speed.get()*0.05)%period;
		Vector2D v = position();
		visual.relocate(v.getX(), v.getY());
	}
	
	/**
	 * Get the current position of the attractor.
	 * @return A new Vec with the position.
	 */
	public Vector2D position() {
		return curve.curve(t).scalarMultiply(scale.get()).add(offset);
	}
	
	
	/**
	 * Simple function getting a time-value and
	 * returning the position of the curve at the
	 * given absolute time.
	 * 
	 * @author Michael Hochmuth
	 *
	 */
	@FunctionalInterface
	public interface AttractorCurve {
		public Vector2D curve(double t);
	}
	
	/**
	 * Get a visual representation of the Attractor.
	 * (a dot at it's current position, updating realtime).
	 * @return A Dot.
	 */
	public Group getVisual() {
		return visual;
	}
	
}
