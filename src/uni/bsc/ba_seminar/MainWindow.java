package uni.bsc.ba_seminar;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import filter.GHFilter;
import filter.TrailRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.Style;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MainWindow extends Application {
	private double height = 700.0, width = 1000.0, menuWidth = 270.0;
	private Label fpsLabel, boidsLabel;
	private BooleanProperty paused = new SimpleBooleanProperty(true);
	private DoubleProperty noise = new SimpleDoubleProperty(0.0);
	private IntegerProperty globalTrailLength = new SimpleIntegerProperty(300);
	private Pane canvas;
	private DoubleProperty border = new SimpleDoubleProperty(60.0);
	private int cores;
	private double timeFactor;
	
	private Circle measureDot = new Circle(0.0, 0.0, 3.0, Color.BLACK);
	private TrailRenderer measureTrail = new TrailRenderer(300);
	private IntegerProperty framesPerMeasure = new SimpleIntegerProperty(1);
	private int measureFrameCount = 0;
	private double measureTimeFactorAccum = 0.0;
	
	private GHFilter ghfilter;
	
	private List<Boid> boids;
	@Override
	public void start(Stage primaryStage) throws Exception {
		ghfilter = new GHFilter(2);
		
		cores = Runtime.getRuntime().availableProcessors();
		System.out.println("cores:"+cores);
		primaryStage.setResizable(false);
		primaryStage.setHeight(height);
		primaryStage.setWidth(width);
		
		
		BorderPane root = new BorderPane();
		
		GridPane menuPane = new GridPane();
		menuPane.setPickOnBounds(true);
		
		menuPane.setHgap(10.0);
		ToggleButton pauseButton = new ToggleButton("Pause");
		pauseButton.selectedProperty().bindBidirectional(paused);
		menuPane.add(pauseButton, 0, 0,1,1);
		
		Button resetButton = new Button("Kill Boids");
		resetButton.setOnAction(e -> {
			for(Boid b : boids) {
				canvas.getChildren().remove(b.getVisual());
			}
			boids.clear();
			boidsLabel.setText("Boids: 0");
		});
		menuPane.add(resetButton, 1, 0,1,1);
		
		boidsLabel = new Label("Boids: 0");
		menuPane.add(boidsLabel, 2,0,1,1);
		
		fpsLabel = new Label();
		menuPane.add(fpsLabel,3,0,1,1);
		
		GridPane flockingMenu = new GridPane();
		TitledPane flockingMenuPane = new TitledPane("Flocking Setup", flockingMenu);
		menuPane.add(flockingMenuPane,0,1,4,1);
		
		flockingMenu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		flockingMenu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		int row = 0;
		
		
		flockingMenu.add(new Label("Velocity Scale:"), 0, row++);
		Slider slVelScale = new Slider(0.001, 1.0, Boid.velScale.get());
		slVelScale.setPrefWidth(menuWidth);
		slVelScale.valueProperty().bindBidirectional(Boid.velScale);
		slVelScale.setShowTickMarks(true);
		slVelScale.setShowTickLabels(true);
		flockingMenu.add(slVelScale, 0, row++);
		
		flockingMenu.add(new Label("Velocity Maximum:"), 0, row++);
		Slider slMaxVel = new Slider(0.0, 20.0, Boid.maxVel.get());
		slMaxVel.valueProperty().bindBidirectional(Boid.maxVel);
		slMaxVel.setShowTickMarks(true);
		slMaxVel.setShowTickLabels(true);
		flockingMenu.add(slMaxVel, 0, row++);
		
		flockingMenu.add(new Label("Acceleration Maximum:"), 0, row++);
		Slider slMaxAcc = new Slider(0.001, 1.0, Boid.maxAccel.get());
		slMaxAcc.valueProperty().bindBidirectional(Boid.maxAccel);
		slMaxAcc.setShowTickMarks(true);
		slMaxAcc.setShowTickLabels(true);
		flockingMenu.add(slMaxAcc, 0, row++);
		
		flockingMenu.add(new Separator(), 0, row++);
		flockingMenu.add(new Label("Min Radius:"), 0, row++);
		Slider slSepRad = new Slider(0.0, 100.0, Boid.seperationRadius.get());
		slSepRad.setShowTickMarks(true);
		slSepRad.setShowTickLabels(true);
		slSepRad.valueProperty().bindBidirectional(Boid.seperationRadius);
		flockingMenu.add(slSepRad, 0, row++);
		
		
		flockingMenu.add(new Label("Max Radius:"), 0, row++);
		Slider slCohRad = new Slider(1, 500.0, Boid.radius.get());
		slCohRad.valueProperty().bindBidirectional(Boid.radius);
		slCohRad.setShowTickMarks(true);
		slCohRad.setShowTickLabels(true);
		flockingMenu.add(slCohRad, 0, row++);

		flockingMenu.add(new Label("WEIGHTS"), 0, row++);
		flockingMenu.add(new Label("Seperation:"), 0, row++);
		Slider slSepW = new Slider(0.0, 2.0, Boid.weightSeperation.get());
		slSepW.valueProperty().bindBidirectional(Boid.weightSeperation);
		slSepW.setShowTickMarks(true);
		flockingMenu.add(slSepW, 0, row++);
		
		flockingMenu.add(new Label("Cohesion:"), 0, row++);
		Slider slCohW = new Slider(0.0, 2.0, Boid.weightCohesion.get());
		slCohW.valueProperty().bindBidirectional(Boid.weightCohesion);
		slCohW.setShowTickMarks(true);
		flockingMenu.add(slCohW, 0, row++);
		
		flockingMenu.add(new Label("Alignment:"), 0, row++);
		Slider slAliW = new Slider(0.0, 2.0, Boid.weightAlignment.get());
		slAliW.valueProperty().bindBidirectional(Boid.weightAlignment);
		slAliW.setShowTickMarks(true);
		flockingMenu.add(slAliW, 0, row++);
		
		flockingMenu.add(new Label("Border:"), 0, row++);
		Slider slBorW = new Slider(0.0, 2.0, Boid.weightBorder.get());
		slBorW.valueProperty().bindBidirectional(Boid.weightBorder);
		slBorW.setShowTickMarks(true);
		flockingMenu.add(slBorW, 0, row++);
		
		flockingMenu.add(new Separator(), 0, row++);
		flockingMenu.add(new Label("Border inset:"), 0, row++);
		Slider slBorder = new Slider(0.0, 200.0, border.get());
		slBorder.valueProperty().bindBidirectional(border);
		slBorder.setShowTickMarks(true);
		slBorder.setShowTickLabels(true);
		flockingMenu.add(slBorder, 0, row++);

		
		
		
		
		GridPane filterMenu = new GridPane();
		filterMenu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		filterMenu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		TitledPane filterMenuPane = new TitledPane("Filter setup:", filterMenu);
		menuPane.add(filterMenuPane,0,2,4,1);
		row = 0;
		

		filterMenu.add(new Label("frames per measure:"), 0, row++);
		Slider slFramesPerMeasure = new Slider(1,240,1);
		slFramesPerMeasure.valueProperty().bindBidirectional(framesPerMeasure);
		slFramesPerMeasure.setShowTickMarks(true);
		slFramesPerMeasure.setShowTickLabels(true);
		filterMenu.add(slFramesPerMeasure, 0, row++);
		
		filterMenu.add(new Label("trail length:"), 0, row++);
		Slider slTrailLength = new Slider(1,500,1);
		slTrailLength.valueProperty().bindBidirectional(globalTrailLength);
		slTrailLength.setShowTickMarks(true);
		slTrailLength.setShowTickLabels(true);
		filterMenu.add(slTrailLength, 0, row++);

		CheckBox btnShowMeasureTrail = new CheckBox("Show measurement");
		measureDot.visibleProperty().bind(btnShowMeasureTrail.selectedProperty());
		measureTrail.visibleProperty().bind(btnShowMeasureTrail.selectedProperty());
		filterMenu.add(btnShowMeasureTrail, 0, row++);
		
		filterMenu.add(new Separator(), 0, row++);
		CheckBox ghactive = new CheckBox("GH FILTER");
		ghactive.selectedProperty().bindBidirectional(ghfilter.active);
		filterMenu.add(ghactive, 0, row++);
		
		Label ghstatus = new Label();
		ghstatus.textProperty().bindBidirectional(ghfilter.status);
		filterMenu.add(ghstatus, 0, row++);
		filterMenu.add(new Label("g:"), 0, row++);
		Slider slFilterG = new Slider(0.0, 1.0, 0.1);
		slFilterG.setPrefWidth(menuWidth);
		slFilterG.valueProperty().bindBidirectional(ghfilter.g);
		slFilterG.setShowTickMarks(true);
		slFilterG.setShowTickLabels(true);
		filterMenu.add(slFilterG, 0, row++);
		
		filterMenu.add(new Label("h:"), 0, row++);
		Slider slFilterH = new Slider(0.0, 0.3, 0.1);
		slFilterH.valueProperty().bindBidirectional(ghfilter.h);
		slFilterH.setShowTickMarks(true);
		slFilterH.setShowTickLabels(true);
		filterMenu.add(slFilterH, 0, row++);
		
		filterMenu.add(new Label("Add Noise:"), 0, row++);
		Slider slNoise = new Slider(0.0, 100.0, 0.1);
		slNoise.valueProperty().bindBidirectional(noise);
		slNoise.setShowTickMarks(true);
		slNoise.setShowTickLabels(true);
		filterMenu.add(slNoise, 0, row++);
		
		ScrollPane menuScroll = new ScrollPane(menuPane);
		menuScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		menuScroll.setPrefWidth(320.0);
		root.setLeft(menuScroll);
		
		StackPane canvasStack = new StackPane();
		
		canvas = new Pane();
		canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
			addBoid(e.getX(), e.getY());
		});
		
		
		boids = new ArrayList<>();
		
		canvasStack.getChildren().add(canvas);
		
		Pane filterVisuals = new Pane();
		filterVisuals.setPickOnBounds(false);
		filterVisuals.getChildren().add(ghfilter.getVisual());
		ghfilter.bindTrailLength(globalTrailLength);
		measureTrail.setStroke(new Color(0.0, 0.0, 0.0, 0.5));
		measureTrail.length.bind(globalTrailLength);
		filterVisuals.getChildren().add(measureDot);
		filterVisuals.getChildren().add(measureTrail);
		canvasStack.getChildren().add(filterVisuals);
		root.setCenter(canvasStack);
		
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
					fpsLabel.setText(String.format("FPS: %d", 1000000000/(deltaSum/fpsDisplayUpdate)));
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
	
	
	CyclicBarrier barrier;
	Thread threads[];
	BoidWorker workers[];
	Object nextCycleMutex = new Object();
	/**
	 * Next step.
	 * @param deltaTime Time since last Step in NANOSECONDS!
	 */
	public void physicsUpdate(long deltaTime) {
		timeFactor = deltaTime / 16666666.666667;
		
		Boid.borderArea = new Rectangle2D(border.get(),border.get(),
				canvas.getWidth()-2.0*border.get(), canvas.getHeight()-2.0*border.get());
		Boid.finalArea = new Rectangle2D(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
		
		
		
//		if(boids.size()>100) {
//			if(barrier==null) {
//				// This initializes the worker threads.
//				
//				threads = new Thread[cores];
//				workers = new BoidWorker[cores];
//				barrier = new CyclicBarrier(cores+1);
//				for(int core = 0; core < cores; ++core) {
//					workers[core] = new BoidWorker(core,cores);
//					threads[core] = new Thread(workers[core]);
//					threads[core].start();
//				}
//			}
//			// Make threads work out new Boid position
//			synchronized (nextCycleMutex) {
//				nextCycleMutex.notifyAll();
//				
//			}
//			
//			try {
//				barrier.await(); // Wait for them to finish
//			} catch (InterruptedException | BrokenBarrierException e) {
//				e.printStackTrace();
//			}
//			barrier.reset();	// Reset for next Cycle
//			
//		} else {
			// No thread option
			for(Boid b : boids) {
				b.update(boids, timeFactor);
			}
//		}
		for(Boid b : boids) {
			b.position();
			
			
		}
		if(boids.size()>0) {
			if(measureFrameCount<framesPerMeasure.get()) {
				measureFrameCount++;
				measureTimeFactorAccum+= deltaTime;
			} else {
				
				boids.get(0).getVisual().setScaleX(2.0);
				boids.get(0).getVisual().setScaleY(2.0);
				boids.get(0).recolor(Color.BLUE);
				Vector<Double> measure = new Vector<Double>();
				measure.add(boids.get(0).pos.x + noise.get()*(Math.random()-0.5));
				measure.add(boids.get(0).pos.y + noise.get()*(Math.random()-0.5));
				measureDot.setCenterX(measure.get(0));
				measureDot.setCenterY(measure.get(1));
				measureTrail.push(measure.get(0), measure.get(1));
				if(ghfilter.active.get()) ghfilter.step(measure, measureTimeFactorAccum); // oder deltaTime?
				
				measureFrameCount = 0;
				measureTimeFactorAccum = 0.0;
			}
		}
	}
	
	public class BoidWorker implements Runnable {
		int index, cores;
		public BoidWorker(int index, int cores) {
			this.index = index;
			this.cores = cores;
		}
		@Override
		public void run() {
			while(true) {
			try {
				synchronized (nextCycleMutex) {
					nextCycleMutex.wait();		// Wait for next cycle
					
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}				
			for(int b = index; b < boids.size(); b+=cores) {
				boids.get(b).update(boids, timeFactor);
			}
			try {
				barrier.await(); 	// Reach the barrier for painting
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}}
		
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
