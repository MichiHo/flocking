package filter;

import java.util.Vector;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import uni.bsc.ba_seminar.Vec;

public class TrailRenderer extends Group {
	private int index = 0, length;
	private double width = 1.0;
	private Color stroke = Color.BLACK;
	
	private Vector<Vec> trail;
	
	public TrailRenderer(int length) {
		this.length = length;
		trail = new Vector<>(length);
		for(int i = 0; i < length; ++i) {
			trail.add(new Vec(0.0,0.0));
		}
		
	}
	
	public void setStroke(Color stroke) {
		this.stroke = stroke;
		redraw();
	}
	
	public void setStrokeWidth(double width) {
		this.width = width;
	}
	
	
	private void redraw() {
		getChildren().clear();
		
		for(int i = 0; i < length-1; ++i) {
			Line line = new Line(
					trail.get((i+index)%length).x,
					trail.get((i+index)%length).y,
					trail.get((i+index+1)%length).x,
					trail.get((i+index+1)%length).y);
			line.setStroke(stroke.deriveColor(0.0, 1.0, 1.0, ((i+1.0)/length)+0.1));
			line.setStrokeWidth(width);
			getChildren().add(line);
		}
		
	}
	
	public void push(double x, double y) {
		trail.set(index, new Vec(x,y));
		index = (index+1)%length;
		redraw();
	}
}
