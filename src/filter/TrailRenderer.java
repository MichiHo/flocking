package filter;

import java.util.Vector;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Paints a line as Trail of some 2D-Moving Object.
 * After push() has been invoked, the TrailRenderer
 * shows as Line from the current position to the past
 * <em>length</em>. Is realized with a Ring-buffer, which
 * leads to strange artifacts when changing the length 
 * (that disappear shortly after however).
 * 
 * @author Michael Hochmuth
 *
 */
public class TrailRenderer extends Group{
	private int index = 0;
	/**
	 * The number of Points this TrailRenderer remembers and draws.
	 */
	private IntegerProperty length = new SimpleIntegerProperty(0);
	private double width = 1.0;
	private Color stroke = Color.BLACK;
	
	private Vector<Vector2D> trail;
	
	/**
	 * Create a trail-renderer that keeps the given count
	 * of samples and paints them decreasing in opacity.
	 * 
	 * @param length Number of samples to keep
	 */
	public TrailRenderer(int length) {
		this.length.set(length);
		trail = new Vector<>(length);
		for(int i = 0; i < length; ++i) {
			trail.add(Vector2D.ZERO);
		}
		this.length.addListener((ob,o,n)-> {
			for(int i = trail.size(); i < n.intValue(); ++i) {
				trail.add(Vector2D.ZERO);
			}
			for(int i = o.intValue()-1; i >=n.intValue(); --i) {
				trail.set(i, Vector2D.ZERO);
			}
		});

		setPickOnBounds(false);
	}
	
	/**
	 * Set color for this TrailRenderer to display in.
	 * @param color 
	 */
	public void setStroke(Color color) {
		this.stroke = color;
		redraw();
	}
	
	/**
	 * Set width of the stroke this TrailRenderer is
	 * made of
	 * @param width
	 */
	public void setStrokeWidth(double width) {
		this.width = width;
	}
	
	
	
	
	private void redraw() {
		getChildren().clear();
		int length = this.length.get();
		for(int i = 0; i < length-1; ++i) {
			if(trail.get((i+index)%length).getX()<0 || trail.get((i+index+1)%length).getX() < 0) continue;
			Line line = new Line(
					trail.get((i+index)%length).getX(),
					trail.get((i+index)%length).getY(),
					trail.get((i+index+1)%length).getX(),
					trail.get((i+index+1)%length).getY());
			line.setPickOnBounds(false);
			line.setStroke(stroke.deriveColor(0.0, 1.0, 1.0, ((i+1.0)/length)+0.1));
			line.setStrokeWidth(width);
			getChildren().add(line);
		}
		
	}
	
	/**
	 * Update Trail with a new position. The oldest position
	 * (before <em>length</em> push operations) is discarded instead.
	 * @param pos Vec with the position
	 */
	public void push(Vector2D pos) {
		trail.set(index, pos);
		index = (index+1)%length.get();
		redraw();
	}
	
	/**
	 * Update Trail with a new position. The oldest position
	 * (before <em>length</em> push operations) is discarded instead.
	 * @param x X-Position
	 * @param y Y-Position
	 */
	public void push(double x, double y) {
		push(new Vector2D(x,y));
	}

	public final IntegerProperty lengthProperty() {
		return this.length;
	}
	

	public final int getLength() {
		return this.lengthProperty().get();
	}
	

	public final void setLength(final int length) {
		this.lengthProperty().set(length);
	}
	
}
