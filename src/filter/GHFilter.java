package filter;

import java.util.List;
import java.util.Vector;

import application.DataModel;
import application.MainWindow;
import application.DataModel.GHFilterMode;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * G-H-Filter implenentation using n-dimensional state and
 * velocity vectors. The parameters of this filter are taken from
 * the global {@link DataModel} from the {@link MainWindow}.
 * <br>
 * Also supplies a visualization of the filter performance with
 * a {@link TrailRenderer}
 * 
 * @author Michael Hochmuth
 *
 */
public class GHFilter {
	public StringProperty status = new SimpleStringProperty("init");
	private int dimension;
	
	// State Vectors
	private Vector<Double> x,v;
	
	// Visualization
	private Group visual;
	private TrailRenderer trail = new TrailRenderer(300);
	
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
		visual.visibleProperty().bind(MainWindow.data.gh_activeProperty());
		visual.setPickOnBounds(false);
		trail.setStroke(Color.RED);
		trail.setStrokeWidth(3.0);
		trail.visibleProperty().bind(MainWindow.data.showFilterTrailProperty());
		visual.getChildren().add(trail);
	}
	
	public void step(List<Double> measures, double timeFactor) {
		if(measures == null || measures.size() < dimension) return;
		
		Vector<Double> newX = new Vector<>(dimension);
		Vector<Double> newV = new Vector<>(dimension);
		
		double residual, x_k, v_k, g = MainWindow.data.getGh_g(), h = MainWindow.data.getGh_h();
		
		if(MainWindow.data.getGh_mode() == GHFilterMode.Benedict_Bordner && g < 2.0)
			h = g*g/(2.0-g);
		
		for(int dim = 0; dim < dimension; ++dim) {
			x_k = x.get(dim) + timeFactor*v.get(dim);
			v_k = v.get(dim);
			
			residual = measures.get(dim) - x_k;
			
			newX.add(x_k + g*residual);
			newV.add(v_k + h*residual/timeFactor);
		}
		// Update 2D Visual
		trail.push(newX.get(0), newX.get(1));
				
		x = newX;
		v = newV;
		
		if(0.0 > 4.0 - 2.0*MainWindow.data.getGh_g() - MainWindow.data.getGh_h()) {
			status.set("Instabil");
		} else {
			status.set("Stabil?");
			
		}
		
	}
	
	public void bindTrailLength(ObservableValue<? extends Number> obs) {
		trail.lengthProperty().bind(obs);
	}
	
	public Group getVisual() {
		return visual;
	}
	
}
