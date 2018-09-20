package uni.bsc.baproj;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.Style;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class MainWindow extends Application {
	private double height = 700.0, width = 1000.0;
	private Label fpsLabel, boidsLabel;
	private BooleanProperty paused = new SimpleBooleanProperty(true);
	private Pane canvas;
	private DoubleProperty border = new SimpleDoubleProperty(60.0);
	
	private List<Boid> boids;
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setResizable(false);
		primaryStage.setHeight(height);
		primaryStage.setWidth(width);
		
		
		BorderPane root = new BorderPane();
		
		GridPane menu = new GridPane();
		menu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		menu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		int row = 0;
		
		ToggleButton pauseButton = new ToggleButton("Pause");
		pauseButton.setPrefWidth(300.0);
		pauseButton.selectedProperty().bindBidirectional(paused);
		menu.add(pauseButton, 0, row++);
		
		menu.add(new Label("Velocity Scale:"), 0, row++);
		Slider slVelScale = new Slider(0.001, 1.0, Boid.velScale.get());
		slVelScale.valueProperty().bindBidirectional(Boid.velScale);
		slVelScale.setShowTickMarks(true);
		slVelScale.setShowTickLabels(true);
		menu.add(slVelScale, 0, row++);
		
		menu.add(new Label("Velocity Maximum:"), 0, row++);
		Slider slMaxVel = new Slider(0.0, 20.0, Boid.maxVel.get());
		slMaxVel.valueProperty().bindBidirectional(Boid.maxVel);
		slMaxVel.setShowTickMarks(true);
		slMaxVel.setShowTickLabels(true);
		menu.add(slMaxVel, 0, row++);
		
		menu.add(new Label("Acceleration Maximum:"), 0, row++);
		Slider slMaxAcc = new Slider(0.001, 1.0, Boid.maxAccel.get());
		slMaxAcc.valueProperty().bindBidirectional(Boid.maxAccel);
		slMaxAcc.setShowTickMarks(true);
		slMaxAcc.setShowTickLabels(true);
		menu.add(slMaxAcc, 0, row++);
		
		menu.add(new Separator(), 0, row++);
		menu.add(new Label("Min Radius:"), 0, row++);
		Slider slSepRad = new Slider(0.0, 100.0, Boid.seperationRadius.get());
		slSepRad.setShowTickMarks(true);
		slSepRad.setShowTickLabels(true);
		slSepRad.valueProperty().bindBidirectional(Boid.seperationRadius);
		menu.add(slSepRad, 0, row++);
		
		
		menu.add(new Label("Max Radius:"), 0, row++);
		Slider slCohRad = new Slider(1, 500.0, Boid.radius.get());
		slCohRad.valueProperty().bindBidirectional(Boid.radius);
		slCohRad.setShowTickMarks(true);
		slCohRad.setShowTickLabels(true);
		menu.add(slCohRad, 0, row++);

		menu.add(new Label("WEIGHTS"), 0, row++);
		menu.add(new Label("Seperation:"), 0, row++);
		Slider slSepW = new Slider(0.0, 2.0, Boid.weightSeperation.get());
		slSepW.valueProperty().bindBidirectional(Boid.weightSeperation);
		slSepW.setShowTickMarks(true);
		menu.add(slSepW, 0, row++);
		
		menu.add(new Label("Cohesion:"), 0, row++);
		Slider slCohW = new Slider(0.0, 2.0, Boid.weightCohesion.get());
		slCohW.valueProperty().bindBidirectional(Boid.weightCohesion);
		slCohW.setShowTickMarks(true);
		menu.add(slCohW, 0, row++);
		
		menu.add(new Label("Alignment:"), 0, row++);
		Slider slAliW = new Slider(0.0, 2.0, Boid.weightAlignment.get());
		slAliW.valueProperty().bindBidirectional(Boid.weightAlignment);
		slAliW.setShowTickMarks(true);
		menu.add(slAliW, 0, row++);
		
		menu.add(new Label("Border:"), 0, row++);
		Slider slBorW = new Slider(0.0, 2.0, Boid.weightBorder.get());
		slBorW.valueProperty().bindBidirectional(Boid.weightBorder);
		slBorW.setShowTickMarks(true);
		menu.add(slBorW, 0, row++);
		
		menu.add(new Separator(), 0, row++);
		menu.add(new Label("Border inset:"), 0, row++);
		Slider slBorder = new Slider(0.0, 200.0, border.get());
		slBorder.valueProperty().bindBidirectional(border);
		slBorder.setShowTickMarks(true);
		slBorder.setShowTickLabels(true);
		menu.add(slBorder, 0, row++);

		
		boidsLabel = new Label("Boids: 0");
		menu.add(boidsLabel, 0, row++);
		
		fpsLabel = new Label();
		menu.add(fpsLabel,0,row++);
		root.setLeft(menu);
		
		canvas = new Pane();
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			addBoid(e.getX(), e.getY());
		});
		
		
		boids = new ArrayList<>();
		root.setCenter(canvas);
		
		primaryStage.setScene(new Scene(root));
		primaryStage.show();

		startAnimation();
	}
	
	public void addBoid(double x, double y) {
		Boid b = new Boid(x,y);
		boids.add(b);
		canvas.getChildren().add(b.getVisual());
		b.position();
		boidsLabel.setText("Boids: "+boids.size());
		System.out.println("n ("+x+"|"+y+")");
	}
	
	public void startAnimation() {

		AnimationTimer timer = new AnimationTimer() {
			long lastTime = System.nanoTime();
			long deltaSum = 0;
			int deltasPos = 1;
			int fpsDisplayUpdate = 15;
			@Override
			public void handle(long time) {
				deltaSum += time-lastTime;
				if(deltasPos == fpsDisplayUpdate) {
					deltasPos = 1;
					fpsLabel.setText(String.format("%d", 1000000000/(deltaSum/fpsDisplayUpdate)));
					deltaSum = 0;
				}
				deltasPos++;

				long delta = ((time-lastTime));
				if(delta>0) {
					if(!paused.get())
						physicsUpdate(delta);
					
					lastTime = time;
				}
			}
		};
		timer.start();
	}
	
	/**
	 * Next step.
	 * @param deltaTime Time since last Step in NANOSECONDS!
	 */
	public void physicsUpdate(long deltaTime) {
		double shouldBeOneForSixtyFPS = deltaTime / 16666666.666667;
		
		Boid.area = new Rectangle2D(border.get(),border.get(),
				canvas.getWidth()-2.0*border.get(), canvas.getHeight()-2.0*border.get());
		
		for(Boid b : boids) {
			b.update(boids, shouldBeOneForSixtyFPS);
		}
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
