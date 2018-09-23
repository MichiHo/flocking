package uni.bsc.ba_seminar;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import filter.Attractor;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Style;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
	public static DataModel data = new DataModel();
	
	private static ObservableList<String> configurations = FXCollections.observableArrayList();
	
	private double height = 700.0, width = 1000.0, menuWidth = 270.0;
	private Label fpsLabel, boidsLabel;
	private BooleanProperty paused = new SimpleBooleanProperty(true);
	private Pane canvas;
	private ComboBox<String> confChooser;
	private int cores;
	private double timeFactor;
	private Random randomX = new Random(), randomY = new Random();
	
	private Circle measureDot = new Circle(0.0, 0.0, 3.0, Color.BLACK);
	private TrailRenderer measureTrail = new TrailRenderer(300);
	private TrailRenderer positionTrail = new TrailRenderer(300);
	
	// For simulating lower-frequency measuring
	private int measureFrameCount = 0;
	private double measureTimeFactorAccum = 0.0;
	
	private Attractor attractorEight;
	
	private GHFilter ghfilter;
	
	private List<Boid> boids;
	@Override
	public void start(Stage primaryStage) throws Exception {
		ghfilter = new GHFilter(2);
		
		cores = Runtime.getRuntime().availableProcessors();
		System.out.println("cores:"+cores);
		primaryStage.setResizable(true);
		primaryStage.setHeight(height);
		primaryStage.setWidth(width);
		
		attractorEight = new Attractor(t ->  {
			return new Vec(2.0*Math.cos(t),Math.sin(t*2.0));
		});
		attractorEight.offset = new Vec(width*0.6,height/2.0);
		attractorEight.scale.set(200.0);
		attractorEight.speed.bind(data.attractorSpeedProperty());
		data.attractorForAllProperty().addListener((ob,o,n)->{
			for(Boid b:boids) b.setAttractor(n?attractorEight:null);
			if(!n && boids.size()>0)
				boids.get(0).setAttractor(attractorEight);
		});
		
		
		BorderPane root = new BorderPane();
		
		GridPane menuPane = new GridPane();
		menuPane.setPickOnBounds(true);
		
		menuPane.setHgap(10.0);
		int menuRow = 0;
		
		Button btnSaveConf = new Button("Save");
		menuPane.add(btnSaveConf, 0, menuRow);
		btnSaveConf.setOnAction(e->{
			if(confChooser.getValue().equals("<configuration>") || confChooser.getValue().isEmpty()) return;
			
			ObjectMapper mapper = new ObjectMapper();
			File f = new File("profiles/"+confChooser.getValue()+".json");
			try {
				f.createNewFile();
				mapper.writerWithDefaultPrettyPrinter().writeValue(f, data);
				//mapper.writeValue(f, data);
				if(!confChooser.getItems().contains(confChooser.getValue()))
					confChooser.getItems().add(confChooser.getValue());
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		
		Button btnDelConf = new Button("Delete");
		menuPane.add(btnDelConf, 1, menuRow);
		btnDelConf.setOnAction(e->{
			String choice = confChooser.getValue();
			if(choice.isEmpty()) return;
			if(confChooser.getItems().contains(choice)) {
				confChooser.getItems().remove(choice);
			}
			File f = new File("profiles/"+choice+".json");
			if(f.exists()) {
				f.delete();
			}
			confChooser.setValue("");
		});
		
		confChooser = new ComboBox<>(configurations);
		confChooser.setValue("<configuration>");
		confChooser.setPrefWidth(menuWidth);
		confChooser.setEditable(true);
		confChooser.valueProperty().addListener((c,o,n)-> {
			File f = new File("profiles/"+n+".json");
			if(f.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					mapper.readerForUpdating(data).readValue(f);
					//data = mapper.readValue(f, DataModel.class);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		menuPane.add(confChooser, 2, menuRow++,2,1);
		
		menuPane.add(new Separator(), 0, menuRow++,4,1);
		
		ToggleButton pauseButton = new ToggleButton("Pause");
		pauseButton.selectedProperty().bindBidirectional(paused);
		menuPane.add(pauseButton, 0, menuRow,1,1);
		
		Button resetButton = new Button("Kill Boids");
		resetButton.setOnAction(e -> {
			for(Boid b : boids) {
				canvas.getChildren().remove(b.getVisual());
			}
			boids.clear();
			boidsLabel.setText("Boids: 0");
		});
		menuPane.add(resetButton, 1, menuRow,1,1);
		
		boidsLabel = new Label("Boids: 0");
		menuPane.add(boidsLabel, 2,menuRow,1,1);
		
		fpsLabel = new Label();
		menuPane.add(fpsLabel,3,menuRow++,1,1);
		
		GridPane flockingMenu = new GridPane();
		TitledPane flockingMenuPane = new TitledPane("Flocking Setup", flockingMenu);
		menuPane.add(flockingMenuPane,0,menuRow++,4,1);
		
		flockingMenu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		flockingMenu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		int row = 0;
		
		
		flockingMenu.add(new Label("Velocity Scale:"), 0, row++);
		Slider slVelScale = new Slider(0.001, 1.0, 0.0);
		slVelScale.setPrefWidth(menuWidth);
		slVelScale.valueProperty().bindBidirectional(data.velScaleProperty());
		slVelScale.setShowTickMarks(true);
		slVelScale.setShowTickLabels(true);
		flockingMenu.add(slVelScale, 0, row++);
		
		flockingMenu.add(new Label("Velocity Maximum:"), 0, row++);
		Slider slMaxVel = new Slider(0.0, 20.0, 0.0);
		slMaxVel.valueProperty().bindBidirectional(data.maxVelProperty());
		slMaxVel.setShowTickMarks(true);
		slMaxVel.setShowTickLabels(true);
		flockingMenu.add(slMaxVel, 0, row++);
		
		flockingMenu.add(new Label("Acceleration Maximum:"), 0, row++);
		Slider slMaxAcc = new Slider(0.001, 1.0, 0.0);
		slMaxAcc.valueProperty().bindBidirectional(data.maxAccelProperty());
		slMaxAcc.setShowTickMarks(true);
		slMaxAcc.setShowTickLabels(true);
		flockingMenu.add(slMaxAcc, 0, row++);
		
		flockingMenu.add(new Separator(), 0, row++);
		flockingMenu.add(new Label("Min Radius:"), 0, row++);
		Slider slSepRad = new Slider(0.0, 100.0, 0.0);
		slSepRad.setShowTickMarks(true);
		slSepRad.setShowTickLabels(true);
		slSepRad.valueProperty().bindBidirectional(data.seperationRadiusProperty());
		flockingMenu.add(slSepRad, 0, row++);
		
		
		flockingMenu.add(new Label("Max Radius:"), 0, row++);
		Slider slCohRad = new Slider(1, 500.0, 0.0);
		slCohRad.valueProperty().bindBidirectional(data.radiusProperty());
		slCohRad.setShowTickMarks(true);
		slCohRad.setShowTickLabels(true);
		flockingMenu.add(slCohRad, 0, row++);

		flockingMenu.add(new Label("WEIGHTS"), 0, row++);
		flockingMenu.add(new Label("Seperation:"), 0, row++);
		Slider slSepW = new Slider(0.0, 2.0, 0.0);
		slSepW.valueProperty().bindBidirectional(data.weightSeperationProperty());
		slSepW.setShowTickMarks(true);
		flockingMenu.add(slSepW, 0, row++);
		
		flockingMenu.add(new Label("Cohesion:"), 0, row++);
		Slider slCohW = new Slider(0.0, 2.0, 0.0);
		slCohW.valueProperty().bindBidirectional(data.weightCohesionProperty());
		slCohW.setShowTickMarks(true);
		flockingMenu.add(slCohW, 0, row++);
		
		flockingMenu.add(new Label("Alignment:"), 0, row++);
		Slider slAliW = new Slider(0.0, 2.0, 0.0);
		slAliW.valueProperty().bindBidirectional(data.weightAlignmentProperty());
		slAliW.setShowTickMarks(true);
		flockingMenu.add(slAliW, 0, row++);
		
		flockingMenu.add(new Label("Border:"), 0, row++);
		Slider slBorW = new Slider(0.0, 2.0, 0.0);
		slBorW.valueProperty().bindBidirectional(data.weightBorderProperty());
		slBorW.setShowTickMarks(true);
		flockingMenu.add(slBorW, 0, row++);
		
		flockingMenu.add(new Label("Attractor:"), 0, row++);
		Slider slAttW = new Slider(0.0, 2.0, 0.0);
		slAttW.valueProperty().bindBidirectional(data.weightAttractorProperty());
		slAttW.setShowTickMarks(true);
		flockingMenu.add(slAttW, 0, row++);
		
		flockingMenu.add(new Separator(), 0, row++);
		flockingMenu.add(new Label("Border inset:"), 0, row++);
		Slider slBorder = new Slider(0.0, 200.0, data.borderProperty().get());
		slBorder.valueProperty().bindBidirectional(data.borderProperty());
		slBorder.setShowTickMarks(true);
		slBorder.setShowTickLabels(true);
		flockingMenu.add(slBorder, 0, row++);
		
		CheckBox btnBorderFlip = new CheckBox("Border flip");
		btnBorderFlip.selectedProperty().bindBidirectional(data.borderFlipProperty());
		flockingMenu.add(btnBorderFlip, 0, row++);

		CheckBox btnAttForAll = new CheckBox("Apply Attractor to all Boids");
		btnAttForAll.selectedProperty().bindBidirectional(data.attractorForAllProperty());
		flockingMenu.add(btnAttForAll, 0, row++);
		
		flockingMenu.add(new Label("Attractor Speed:"), 0, row++);
		Slider slAttSpeed = new Slider(0.0, 1.0, 0.0);
		slAttSpeed.valueProperty().bindBidirectional(data.attractorSpeedProperty());
		slAttSpeed.setShowTickMarks(true);
		slAttSpeed.setMajorTickUnit(0.1);
		slAttSpeed.setShowTickLabels(true);
		flockingMenu.add(slAttSpeed, 0, row++);
		
		GridPane filterMenu = new GridPane();
		filterMenu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		filterMenu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		TitledPane filterMenuPane = new TitledPane("Filter setup:", filterMenu);
		menuPane.add(filterMenuPane,0,menuRow++,4,1);
		row = 0;
		

		filterMenu.add(new Label("frames per measure:"), 0, row++);
		Slider slFramesPerMeasure = new Slider(1,240,1);
		slFramesPerMeasure.valueProperty().bindBidirectional(data.framesPerMeasureProperty());
		slFramesPerMeasure.setShowTickMarks(true);
		slFramesPerMeasure.setShowTickLabels(true);
		filterMenu.add(slFramesPerMeasure, 0, row++);
		
		filterMenu.add(new Label("Trail Length:"), 0, row++);
		Slider slTrailLength = new Slider(1,500,1);
		slTrailLength.valueProperty().bindBidirectional(data.globalTrailLengthProperty());
		slTrailLength.setShowTickMarks(true);
		slTrailLength.setShowTickLabels(true);
		filterMenu.add(slTrailLength, 0, row++);

		CheckBox btnShowMeasureTrail = new CheckBox("Measure Trail");
		btnShowMeasureTrail.selectedProperty().bindBidirectional(data.showMeasureTrailProperty());
		measureDot.visibleProperty().bind(data.showMeasureTrailProperty());
		measureTrail.visibleProperty().bind(data.showMeasureTrailProperty());
		filterMenu.add(btnShowMeasureTrail, 0, row++);
		
		CheckBox btnShowPosTrail = new CheckBox("Position Trail");
		btnShowPosTrail.selectedProperty().bindBidirectional(data.showPositionTrailProperty());
		positionTrail.visibleProperty().bind(data.showPositionTrailProperty());
		filterMenu.add(btnShowPosTrail, 0, row++);
		
		filterMenu.add(new Separator(), 0, row++);
		CheckBox ghactive = new CheckBox("GH FILTER");
		ghactive.selectedProperty().bindBidirectional(data.gh_activeProperty());
		filterMenu.add(ghactive, 0, row++);
		
		Label ghstatus = new Label();
		ghstatus.textProperty().bindBidirectional(ghfilter.status);
		filterMenu.add(ghstatus, 0, row++);
		filterMenu.add(new Label("g:"), 0, row++);
		Slider slFilterG = new Slider(0.0, 1.0, 0.1);
		slFilterG.setPrefWidth(menuWidth);
		slFilterG.valueProperty().bindBidirectional(data.gh_gProperty());
		slFilterG.setShowTickMarks(true);
		slFilterG.setShowTickLabels(true);
		filterMenu.add(slFilterG, 0, row++);
		
		filterMenu.add(new Label("h:"), 0, row++);
		Slider slFilterH = new Slider(0.0, 0.3, 0.1);
		slFilterH.valueProperty().bindBidirectional(data.gh_hProperty());
		slFilterH.setShowTickMarks(true);
		slFilterH.setShowTickLabels(true);
		filterMenu.add(slFilterH, 0, row++);
		
		filterMenu.add(new Label("Add Noise:"), 0, row++);
		Slider slNoise = new Slider(0.0, 100.0, 0.1);
		slNoise.valueProperty().bindBidirectional(data.noiseProperty());
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
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e->addBoid(e.getX(),e.getY()));
		
		boids = new ArrayList<>();
		
		canvasStack.getChildren().add(canvas);
		
		Pane filterVisuals = new Pane();
		filterVisuals.setPickOnBounds(false);
		filterVisuals.getChildren().add(ghfilter.getVisual());
		ghfilter.bindTrailLength(data.globalTrailLengthProperty());
		measureTrail.setStroke(new Color(0.0, 0.0, 0.0, 0.5));
		measureTrail.lengthProperty().bind(data.globalTrailLengthProperty());
		filterVisuals.getChildren().add(measureDot);
		filterVisuals.getChildren().add(measureTrail);
		positionTrail.setStroke(Color.GREEN);
		positionTrail.lengthProperty().bind(data.globalTrailLengthProperty());
		filterVisuals.getChildren().add(positionTrail);
		filterVisuals.getChildren().add(attractorEight.getVisual()); 	
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
		if(data.attractorForAllProperty().get() || boids.size()==1) {
			b.setAttractor(attractorEight);
		}
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
		
		Boid.borderArea = new Rectangle2D(data.borderProperty().get(),data.borderProperty().get(),
				canvas.getWidth()-2.0*data.borderProperty().get(), canvas.getHeight()-2.0*data.borderProperty().get());
		Boid.finalArea = new Rectangle2D(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
		
		attractorEight.timeStep(timeFactor);
		
		
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
			positionTrail.push(boids.get(0).getPos());
			if(measureFrameCount<data.framesPerMeasureProperty().get()) {
				measureFrameCount++;
				measureTimeFactorAccum+= deltaTime;
			} else {
				
				boids.get(0).getVisual().setScaleX(2.0);
				boids.get(0).getVisual().setScaleY(2.0);
				boids.get(0).recolor(Color.BLUE);
				Vector<Double> measure = new Vector<Double>();
				measure.add(boids.get(0).pos.x + data.noiseProperty().get()*(randomX.nextGaussian()));
				measure.add(boids.get(0).pos.y + data.noiseProperty().get()*(randomY.nextGaussian()));
				measureDot.setCenterX(measure.get(0));
				measureDot.setCenterY(measure.get(1));
				measureTrail.push(measure.get(0), measure.get(1));
				if(data.isGh_active()) ghfilter.step(measure, measureTimeFactorAccum); // oder deltaTime?
				
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
	
	public static void main(String[] args) throws IOException {
		
		File profilesFolder = new File("profiles");
		File[] names = profilesFolder.listFiles((dir,name)->{
			return name.endsWith(".json");
		});
		for(File f : names) {
			configurations.add(f.getName().substring(0, f.getName().length()-5));
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File("profiles/first.json"), data);
		Application.launch(args);
	}

}
