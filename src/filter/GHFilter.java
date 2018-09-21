package filter;

import java.util.List;
import java.util.Observable;
import java.util.Vector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class GHFilter {
	public DoubleProperty g = new SimpleDoubleProperty(0.1);
	public DoubleProperty h = new SimpleDoubleProperty(0.0);
	public StringProperty status = new SimpleStringProperty("init");
	public BooleanProperty active = new SimpleBooleanProperty(true);
	private int dimension;
	
	private Vector<Double> x,v;
	
	private Group visual;
	private TrailRenderer trail = new TrailRenderer(300);
	private Circle dot = new Circle(0.0,0.0,6.0, Color.RED);;
	
	public GHFilter(int dimension) {
		if(dimension<0) throw new IllegalArgumentException();
		
		this.dimension = dimension;
		x = new Vector<>(dimension);
		v = new Vector<>(dimension);
		
		for(int d = 0; d < dimension; ++d) {
			x.add(0.0);
			v.add(0.0);
		}
		
		visual = new Group();
		visual.visibleProperty().bind(active);
		visual.setPickOnBounds(false);
		trail.setStroke(Color.RED);
		trail.setStrokeWidth(7.0);
		visual.getChildren().add(trail);
		visual.getChildren().add(dot);
	}
	
	public void step(List<Double> measures, double timeFactor) {
		if(measures == null || measures.size() < dimension) return;
		
		Vector<Double> newX = new Vector<>(dimension);
		Vector<Double> newV = new Vector<>(dimension);
		
		double residual, x_k, v_k;
		for(int dim = 0; dim < dimension; ++dim) {
			x_k = x.get(dim) + timeFactor*v.get(dim);
			v_k = v.get(dim);
			
			residual = measures.get(dim) - x_k;

			newX.add(x_k + g.get()*residual);
			newV.add(v_k + h.get()*residual/timeFactor);
		}
		// Update 2D Visual
		dot.setCenterX(newX.get(0));
		dot.setCenterY(newX.get(1));
		trail.push(newX.get(0), newX.get(1));
				
				
		x = newX;
		v = newV;
		
		if(0.0 > 4.0 - 2.0*g.get() - h.get()) {
			status.set("Instabil");
		} else {
			status.set("Stabil?");
			
		}
		
	}
	
	public void bindTrailLength(ObservableValue<? extends Number> obs) {
		trail.length.bind(obs);
	}
	
	public Group getVisual() {
		return visual;
	}
	
}
