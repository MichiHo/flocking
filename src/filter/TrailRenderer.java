package filter;

import java.util.Vector;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import uni.bsc.ba_seminar.Vec;

/**
 * Paints a line as Trail of some 2D-Moving Object.
 * After push() has been invoked, the TrailRenderer
 * shows as Line from the current position to the past
 * <em>length</em>.
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
	
	private Vector<Vec> trail;
	
	public TrailRenderer(int length) {
		this.length.set(length);
		trail = new Vector<>(length);
		for(int i = 0; i < length; ++i) {
			trail.add(new Vec(0.0,0.0));
		}
		this.length.addListener((ob,o,n)-> {
			for(int i = trail.size(); i < n.intValue(); ++i) {
				trail.add(new Vec());
			}
			for(int i = o.intValue()-1; i >=n.intValue(); --i) {
				trail.set(i, new Vec());
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
			if(trail.get((i+index)%length).x<0 || trail.get((i+index+1)%length).x < 0) continue;
			Line line = new Line(
					trail.get((i+index)%length).x,
					trail.get((i+index)%length).y,
					trail.get((i+index+1)%length).x,
					trail.get((i+index+1)%length).y);
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
	public void push(Vec pos) {
		push(pos.x,pos.y);
	}
	
	/**
	 * Update Trail with a new position. The oldest position
	 * (before <em>length</em> push operations) is discarded instead.
	 * @param x X-Position
	 * @param y Y-Position
	 */
	public void push(double x, double y) {
		trail.set(index, new Vec(x,y));
		index = (index+1)%length.get();
		redraw();
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
