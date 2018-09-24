package uni.bsc.ba_seminar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cern.colt.matrix.DoubleFactory2D;
import filter.GHFilter;
import filter.TrailRenderer;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

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
import javafx.scene.Node;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import uni.bsc.ba_seminar.DataModel.AttractorType;
import uni.bsc.ba_seminar.DataModel.GHFilterMode;

public class MainWindow extends Application {
	public static DataModel data = new DataModel();
	
	private static ObservableList<String> configurations = FXCollections.observableArrayList();
	private BooleanProperty keepFirstBoid = new SimpleBooleanProperty(true);
	
	private double height = 700.0, width = 1000.0, menuWidth = 330.0;
	private Label fpsLabel, boidsLabel;
	private BooleanProperty paused = new SimpleBooleanProperty(true);
	private Pane canvas;
	private ComboBox<String> confChooser;
	private int cores;
	private double timeFactor;
	private long nanosPerFrame = 16666667L, nanoAccum = 0L;
	private Random randomX = new Random(), randomY = new Random();
	
	private Circle measureDot = new Circle(0.0, 0.0, 3.0, Color.BLACK);
	private TrailRenderer measureTrail = new TrailRenderer(300);
	private Circle measureNoiseCircle;
	private TrailRenderer positionTrail = new TrailRenderer(300);
	private Circle kalmanDot = new Circle(0.0, 0.0, 3.0, Color.CYAN);
	private TrailRenderer kalmanTrail = new TrailRenderer(300);
	private Line kalmanPredict = new Line();
	private GridPane menuPane;
	
	// For simulating lower-frequency measuring
	private int measureFrameCount = 0;
	private double measureTimeFactorAccum = 0.0;
	
	private GHFilter ghfilter;
	private KalmanFilter kalmanfilter;
	RealMatrix stateTransition = new Array2DRowRealMatrix(new double [][]{
		{1.0,0.0,1.0,0.0},		// x = x + t*dx
		{0.0,1.0,0.0,1.0},		// y = y + t*dy
		{0.0,0.0,1.0,0.0},		// dx = dx
		{0.0,0.0,0.0,1.0}});
	
	RealMatrix stateTransitionAcc = new Array2DRowRealMatrix(new double [][]{
		{1.0,0.0,1.0,0.0,0.5,0.0},		// x = x + t*dx
		{0.0,1.0,0.0,1.0,0.0,0.5},		// y = y + t*dy
		{0.0,0.0,1.0,0.0,1.0,0.0},		
		{0.0,0.0,0.0,1.0,0.0,1.0},		
		{0.0,0.0,0.0,0.0,1.0,0.0},		
		{0.0,0.0,0.0,0.0,0.0,1.0}});
	
	RealMatrix processNoise = new Array2DRowRealMatrix(new double [][]{
		{0.1,0.0,0.0,0.0},		// x = x + t*dx
		{0.0,0.1,0.0,0.0},		// y = y + t*dy
		{0.0,0.0,0.1,0.0},		
		{0.0,0.0,0.0,0.1}});
	
	RealMatrix processNoise6 = new Array2DRowRealMatrix(new double [][]{
		{0.1,0.0,0.0,0.0,0.0,0.0},		// x = x + t*dx
		{0.0,0.1,0.0,0.0,0.0,0.0},		// y = y + t*dy
		{0.0,0.0,0.1,0.0,0.0,0.0},		
		{0.0,0.0,0.0,0.1,0.0,0.0},		
		{0.0,0.0,0.0,0.0,0.1,0.0},		
		{0.0,0.0,0.0,0.0,0.0,0.1}});
	
	RealMatrix observationModel = new Array2DRowRealMatrix(new double [][]{
		{1.0,0.0,0.0,0.0},
		{0.0,1.0,0.0,0.0}});
	
	RealMatrix observationNoise = new Array2DRowRealMatrix(new double [][]{
		{0.0,0.0},
		{0.0,0.0}});
	
	
	private List<Boid> boids;
	@Override
	public void start(Stage primaryStage) throws Exception {
		ghfilter = new GHFilter(2);
		kalmanfilter = new KalmanFilter(new ProcessModel() {
			
			@Override
			public RealMatrix getStateTransitionMatrix() {
				return stateTransition;
			}
			
			@Override
			public RealMatrix getProcessNoise() {
				return processNoise;
			}
			
			@Override
			public RealVector getInitialStateEstimate() {
				return null;
			}
			
			@Override
			public RealMatrix getInitialErrorCovariance() {
				return null;
			}
			
			@Override
			public RealMatrix getControlMatrix() {
				return null;
			}
		}, new MeasurementModel() {
			
			@Override
			public RealMatrix getMeasurementNoise() {
				return observationNoise;
			}
			
			@Override
			public RealMatrix getMeasurementMatrix() {
				return observationModel;
			}
		});
//		kalmanfilter = new KalmanFilter(4);
//		kalmanfilter.setObservationModel(DoubleFactory2D.dense.make(
//				new double [][]{
//					{1.0,0.0,0.0,0.0},	// apply x
//					{0.0,1.0,0.0,0.0}}	// apply y
//					));
//		kalmanfilter.setStateTransitionModel(DoubleFactory2D.dense.make(
//				new double [][]{
//					{1.0,0.0,1.0,0.0},		// x = x + t*dx
//					{0.0,1.0,0.0,1.0},		// y = y + t*dy
//					{0.0,0.0,1.0,0.0},		// dx = dx
//					{0.0,0.0,0.0,1.0}} ));	// dy = dy
//		kalmanfilter.setProcessNoise(DoubleFactory2D.dense.make(
//				new double [][]{
//					{1.0, 0.2, 0.0, 0.0},		// x = x + t*dx
//					{0.2, 1.0, 0.0, 0.0},		// y = y + t*dy
//					{0.0, 0.0, 1.0, 0.0},		// dx = dx
//					{0.0, 0.0, 0.0, 1.0}} ));	// dy = dy
//		
		cores = Runtime.getRuntime().availableProcessors();
		primaryStage.setResizable(true);
		primaryStage.setHeight(height);
		primaryStage.setWidth(width);
		
		for(AttractorType t : AttractorType.values()) {
			t.attractor.offset = new Vec(width*0.6,height/2.0);
			t.attractor.scale.set(200.0);
			t.attractor.speed.bind(data.attractorSpeedProperty());
			t.attractor.getVisual().visibleProperty().bind(data.showAttractorProperty());
		}
		AttractorType.Line.attractor.scale.set(300.0);
		data.attractorForAllProperty().addListener((ob,o,n)->{
			for(Boid b:boids) b.useAttractor = n;
			if(boids.size()>0)
				boids.get(0).useAttractor = true;
		});
		
		
		BorderPane root = new BorderPane();
		
		menuPane = new GridPane();
		menuPane.setPickOnBounds(true);
		
		menuPane.setHgap(10.0);
		int menuRow = 0;
		
		Button btnSaveConf = new Button("Save");
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
		confChooser.setPrefWidth(200.0);
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
		menuPane.add(new HBox(5.0, btnSaveConf,btnDelConf,confChooser), 0, menuRow++);
		
		menuPane.add(new Separator(), 0, menuRow++);
		
		ToggleButton pauseButton = new ToggleButton("Pause");
		pauseButton.selectedProperty().bindBidirectional(paused);
		
		Button resetButton = new Button("Kill Boids");
		resetButton.setOnAction(e -> {
			for(int i = boids.size()-1 ; i >= (keepFirstBoid.get()?1:0) ;--i) {

				canvas.getChildren().remove(boids.get(i).getVisual());
				boids.remove(i);
			}
			boidsLabel.setText("Boids: " + boids.size());
		});
		
		CheckBox btnKeepFirstBoid = new CheckBox("Keep #1");
		btnKeepFirstBoid.selectedProperty().bindBidirectional(keepFirstBoid);
		
		boidsLabel = new Label("Boids: 0");
		fpsLabel = new Label();
		menuPane.add(new HBox(5.0,pauseButton,resetButton,btnKeepFirstBoid,boidsLabel,fpsLabel),0,menuRow++);
		
		GridPane flockingMenu = new GridPane();
		TitledPane flockingMenuPane = new TitledPane("Flocking Setup", flockingMenu);
		menuPane.add(flockingMenuPane,0,menuRow++);
		
		flockingMenu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		flockingMenu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		int row = 0;
		
		
		flockingMenu.add(new Label("Velocity Scale:"), 0, row++);
		Slider slVelScale = new Slider(0.0, 1.0, 0.0);
		slVelScale.setPrefWidth(menuWidth);
		slVelScale.valueProperty().bindBidirectional(data.velScaleProperty());
		slVelScale.setShowTickMarks(true);
		slVelScale.setShowTickLabels(true);
		slVelScale.setMajorTickUnit(0.1);
		flockingMenu.add(slVelScale, 0, row++);
		
		flockingMenu.add(new Label("Velocity Maximum:"), 0, row++);
		Slider slMaxVel = new Slider(0.0, 20.0, 0.0);
		slMaxVel.valueProperty().bindBidirectional(data.maxVelProperty());
		slMaxVel.setShowTickMarks(true);
		slMaxVel.setShowTickLabels(true);
		slMaxVel.setMajorTickUnit(2.0);
		flockingMenu.add(slMaxVel, 0, row++);
		
		flockingMenu.add(new Label("Acceleration Maximum:"), 0, row++);
		Slider slMaxAcc = new Slider(0.0, 1.0, 0.0);
		slMaxAcc.valueProperty().bindBidirectional(data.maxAccelProperty());
		slMaxAcc.setShowTickMarks(true);
		slMaxAcc.setShowTickLabels(true);
		slMaxAcc.setMajorTickUnit(0.1);
		flockingMenu.add(slMaxAcc, 0, row++);
		
		flockingMenu.add(caption("Flocking Parameters"), 0, row++);
		flockingMenu.add(new Label("Min Radius:"), 0, row++);
		Slider slSepRad = new Slider(0.0, 200.0, 0.0);
		slSepRad.setShowTickMarks(true);
		slSepRad.setShowTickLabels(true);
		slSepRad.setMajorTickUnit(25.0);
		slSepRad.valueProperty().bindBidirectional(data.seperationRadiusProperty());
		flockingMenu.add(slSepRad, 0, row++);
		
		
		flockingMenu.add(new Label("Max Radius:"), 0, row++);
		Slider slCohRad = new Slider(0.0, 500.0, 0.0);
		slCohRad.valueProperty().bindBidirectional(data.radiusProperty());
		slCohRad.setShowTickMarks(true);
		slCohRad.setShowTickLabels(true);
		slCohRad.setMajorTickUnit(100.0);
		flockingMenu.add(slCohRad, 0, row++);

		flockingMenu.add(caption("Flocking Weights"), 0, row++);
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
		
		flockingMenu.add(caption("Attractor"), 0, row++);

		ComboBox<AttractorType> chooseAttractor = new ComboBox<>();
		chooseAttractor.getItems().setAll(DataModel.AttractorType.values());
		chooseAttractor.valueProperty().bindBidirectional(data.attractorTypeProperty());
		flockingMenu.add(chooseAttractor, 0, row++);
		
		flockingMenu.add(new Label("Attractor Speed:"), 0, row++);
		Slider slAttSpeed = new Slider(0.0, 1.0, 0.0);
		slAttSpeed.valueProperty().bindBidirectional(data.attractorSpeedProperty());
		slAttSpeed.setShowTickMarks(true);
		slAttSpeed.setMajorTickUnit(0.1);
		slAttSpeed.setShowTickLabels(true);
		flockingMenu.add(slAttSpeed, 0, row++);
		
		CheckBox btnAttForAll = new CheckBox("Apply Attractor to all Boids");
		btnAttForAll.selectedProperty().bindBidirectional(data.attractorForAllProperty());
		flockingMenu.add(btnAttForAll, 0, row++);
		
		CheckBox btnShowAtt = new CheckBox("Show Attractor position");
		btnShowAtt.selectedProperty().bindBidirectional(data.showAttractorProperty());
		flockingMenu.add(btnShowAtt, 0, row++);
		
		GridPane filterMenu = new GridPane();
		filterMenu.setBackground(new Background(new BackgroundFill(
				Color.WHITE, new CornerRadii(8.0), Insets.EMPTY)));
		filterMenu.setBorder(new Border(new BorderStroke(Color.hsb(0.0, 0.0, 0.9), 
				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(7.0))));
		
		TitledPane filterMenuPane = new TitledPane("Filter setup:", filterMenu);
		menuPane.add(filterMenuPane,0,menuRow++);
		row = 0;
		

		filterMenu.add(caption("Measurement Simulation"), 0, row++);

		filterMenu.add(new Label("Frames per Measure:"), 0, row++);
		Slider slFramesPerMeasure = new Slider(1,240,1);
		slFramesPerMeasure.valueProperty().bindBidirectional(data.framesPerMeasureProperty());
		slFramesPerMeasure.setShowTickMarks(true);
		slFramesPerMeasure.setShowTickLabels(true);
		filterMenu.add(slFramesPerMeasure, 0, row++);
		
		filterMenu.add(new Label("Measure Noise:"), 0, row++);
		Slider slNoise = new Slider(0.0, 100.0, 0.1);
		slNoise.valueProperty().bindBidirectional(data.noiseProperty());
		slNoise.setShowTickMarks(true);
		slNoise.setShowTickLabels(true);
		filterMenu.add(slNoise, 0, row++);
		
		int stopCount = 10;
		List<Stop> stops = new Vector<>(10);
		double coeff = 1.0 / Math.sqrt(2.0*Math.PI);
		for(int stop = 0; stop < stopCount; ++stop) {
			double r = 4.0* ((double)stop/(double)stopCount);
			stops.add(new Stop(r/4.0, new Color(0.0,0.0,0.0,
					coeff*Math.exp(-0.5 * r * r))));
		}
		RadialGradient measureNoiseGaussian = new RadialGradient(
				0.0, 0.0, 0.5,0.5, 0.5, true, CycleMethod.NO_CYCLE, 
				stops);
		
		measureNoiseCircle = new Circle(2.0,measureNoiseGaussian);
		measureNoiseCircle.radiusProperty().bind(data.noiseProperty().multiply(4.0));
		measureNoiseCircle.visibleProperty().bind(data.showMeasureNoiseProperty());
		CheckBox btnShowNoise = new CheckBox("Show Noise Distribution");
		btnShowNoise.selectedProperty().bindBidirectional(data.showMeasureNoiseProperty());
		filterMenu.add(btnShowNoise, 0, row++);
		

		filterMenu.add(caption("Trails"), 0, row++);
		
		filterMenu.add(new Label("Trail Length:"), 0, row++);
		Slider slTrailLength = new Slider(1,500,1);
		slTrailLength.valueProperty().bindBidirectional(data.globalTrailLengthProperty());
		slTrailLength.setShowTickMarks(true);
		slTrailLength.setShowTickLabels(true);
		filterMenu.add(slTrailLength, 0, row++);

		CheckBox btnShowPosTrail = new CheckBox("Position Trail");
		btnShowPosTrail.selectedProperty().bindBidirectional(data.showPositionTrailProperty());
		positionTrail.visibleProperty().bind(data.showPositionTrailProperty());
		filterMenu.add(btnShowPosTrail, 0, row++);
		
		CheckBox btnShowMeasureTrail = new CheckBox("Measure Trail");
		btnShowMeasureTrail.selectedProperty().bindBidirectional(data.showMeasureTrailProperty());
		measureDot.visibleProperty().bind(data.showMeasureTrailProperty());
		measureTrail.visibleProperty().bind(data.showMeasureTrailProperty());
		filterMenu.add(btnShowMeasureTrail, 0, row++);
		
		CheckBox btnShowFilterTrails = new CheckBox("Filter Trail");
		btnShowFilterTrails.selectedProperty().bindBidirectional(data.showFilterTrailProperty());
		measureDot.visibleProperty().bind(data.showMeasureTrailProperty());
		measureTrail.visibleProperty().bind(data.showMeasureTrailProperty());
		filterMenu.add(btnShowFilterTrails, 0, row++);
		
		
		filterMenu.add(caption("GH Filter"), 0, row++);
		CheckBox ghactive = new CheckBox("Active");
		ghactive.selectedProperty().bindBidirectional(data.gh_activeProperty());
		filterMenu.add(ghactive, 0, row++);
		
		ComboBox<GHFilterMode> comboGHMode = new ComboBox<>();
		comboGHMode.getItems().setAll(GHFilterMode.values());
		comboGHMode.valueProperty().bindBidirectional(data.gh_modeProperty());
		filterMenu.add(comboGHMode, 0, row++);
		
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
		data.gh_modeProperty().addListener((c,o,n)->slFilterH.setDisable(!n.hChoosable));	
		slFilterH.valueProperty().bindBidirectional(data.gh_hProperty());
		slFilterH.setShowTickMarks(true);
		slFilterH.setShowTickLabels(true);
		filterMenu.add(slFilterH, 0, row++);
		
		
		filterMenu.add(caption("Kalman Filter"), 0, row++);
		CheckBox btnKalmanActive = new CheckBox("Active");
		btnKalmanActive.selectedProperty().bindBidirectional(data.kalman_activeProperty());
		filterMenu.add(btnKalmanActive, 0, row++);
		
		Button btnKalmanSet = new Button("Set to Boid");
		btnKalmanSet.setOnAction(e->{
			if(boids.size()<1) return;
			Boid b = boids.get(0);
			RealMatrix m = observationNoise;
			
			observationNoise = new Array2DRowRealMatrix(new double [][]{
				{0.0,0.0},
				{0.0,0.0}});
			kalmanfilter.correct(new double[] 
					{b.getPos().x, b.getPos().y});
			observationNoise = m;
			
//			kalmanfilter.setState(DoubleFactory1D.dense.make(new double[] 
//					{b.getPos().x, b.getPos().y,
//					 b.getVel().x, b.getVel().y}), 
//					DoubleFactory2D.dense.make(
//							new double [][]{
//								{0.0,0.0,0.0,0.0},	// apply x
//								{0.0,0.0,0.0,0.0},	// apply y
//								{0.0,0.0,0.0,0.0},
//								{0.0,0.0,0.0,0.0}} ));
		});
		filterMenu.add(btnKalmanSet, 0, row++);
		
		
		ScrollPane menuScroll = new ScrollPane(menuPane);
		menuScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		menuScroll.setPrefWidth(menuWidth + 50.0);
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
		positionTrail.setStroke(Color.GRAY);
		positionTrail.setStrokeWidth(4.0);
		positionTrail.lengthProperty().bind(data.globalTrailLengthProperty().multiply(2.0));
		filterVisuals.getChildren().add(positionTrail);
		
		measureTrail.setStroke(new Color(0.0, 0.0, 0.0, 0.3));
		measureTrail.lengthProperty().bind(data.globalTrailLengthProperty().multiply(0.3));
		filterVisuals.getChildren().add(measureDot);
		filterVisuals.getChildren().add(measureTrail);
		filterVisuals.getChildren().add(measureNoiseCircle);
		
		ghfilter.bindTrailLength(data.globalTrailLengthProperty());
		filterVisuals.getChildren().add(ghfilter.getVisual());
		
		kalmanTrail.lengthProperty().bind(data.globalTrailLengthProperty());
		kalmanTrail.setStroke(Color.GREEN);
		kalmanTrail.setStrokeWidth(3.0);
		kalmanTrail.visibleProperty().bind(data.kalman_activeProperty());
		kalmanPredict.visibleProperty().bind(data.kalman_activeProperty());
		kalmanPredict.setStroke(Color.GREEN);
		kalmanPredict.getStrokeDashArray().addAll(3.0,5.0);
		kalmanPredict.setStrokeWidth(3.0);
		kalmanDot.visibleProperty().bind(data.kalman_activeProperty());
//		filterVisuals.getChildren().add(kalmanDot);
		filterVisuals.getChildren().add(kalmanTrail);
		filterVisuals.getChildren().add(kalmanPredict);
		data.attractorTypeProperty().addListener((c,o,n)->{
			filterVisuals.getChildren().remove(o.attractor.getVisual());
			filterVisuals.getChildren().add(n.attractor.getVisual());
		});
		filterVisuals.getChildren().add(data.getAttractorType().attractor.getVisual()); 
		filterVisuals.setMouseTransparent(true);
		canvasStack.getChildren().add(filterVisuals);
		root.setCenter(canvasStack);
		
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
		
		startAnimation();
	}
	
	private Node caption(String text) {
		Label l = new Label(text);
		l.setStyle("-fx-text-alignment: center; font-weight: bolder");
		Separator s = new Separator();
		s.setPadding(new Insets(4.0, 0.0, 1.0, 0.0));
		return new BorderPane(l, s, null, null, null);
	}
	
	public void addBoid(double x, double y) {
		Boid b = new Boid(x,y);
		boids.add(b);
		canvas.getChildren().add(b.getVisual());
		b.position();
		boidsLabel.setText("Boids: "+boids.size());
		if(data.attractorForAllProperty().get() || boids.size()==1) {
			b.useAttractor = true;
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
	
	
//	CyclicBarrier barrier;
//	Thread threads[];
//	BoidWorker workers[];
//	Object nextCycleMutex = new Object();
	/**
	 * Next step.
	 * @param deltaTime Time since last Step in NANOSECONDS!
	 */
	public void physicsUpdate(long deltaTime) {
		
		nanoAccum += deltaTime;
		while(nanoAccum > nanosPerFrame) {
			nanoAccum -= nanosPerFrame;
			timeFactor = 1.0;
		
			Boid.borderArea = new Rectangle2D(data.borderProperty().get(),data.borderProperty().get(),
					canvas.getWidth()-2.0*data.borderProperty().get(), canvas.getHeight()-2.0*data.borderProperty().get());
			Boid.finalArea = new Rectangle2D(0.0, 0.0, canvas.getWidth(), canvas.getHeight());
			
			data.getAttractorType().attractor.timeStep(timeFactor);
		
		
			for(Boid b : boids) {
				b.update(boids, timeFactor);
			}
			for(Boid b : boids) {
				b.position();
				
				
			}
			if(boids.size()>0) {
				positionTrail.push(boids.get(0).getPos());
				if(measureFrameCount<data.framesPerMeasureProperty().get()) {
					measureFrameCount++;
					measureTimeFactorAccum+= deltaTime;
				} else {
					
					boids.get(0).getVisual().setScaleX(3.0);
					boids.get(0).getVisual().setScaleY(3.0);
					boids.get(0).recolor(Color.BLUE);
					measureNoiseCircle.setCenterX(boids.get(0).getPos().x);
					measureNoiseCircle.setCenterY(boids.get(0).getPos().y);
					Vector<Double> measure = new Vector<Double>();
					measure.add(boids.get(0).pos.x + data.noiseProperty().get()*(randomX.nextGaussian()));
					measure.add(boids.get(0).pos.y + data.noiseProperty().get()*(randomY.nextGaussian()));
					measureDot.setCenterX(measure.get(0));
					measureDot.setCenterY(measure.get(1));
					measureTrail.push(measure.get(0), measure.get(1));
					if(data.isGh_active()) ghfilter.step(measure, measureTimeFactorAccum); // oder deltaTime?
					
					if(data.isKalman_active()) {
//						kalmanfilter.step(DoubleFactory1D.dense.make(new double[] 
//								{measure.get(0), measure.get(1)}),
//								DoubleFactory2D.dense.make(new double [][]{
//									{data.noiseProperty().get()*data.noiseProperty().get(), 0.0},
//									{0.0, data.noiseProperty().get()*data.noiseProperty().get()}} ),null);
						
//						observationNoise = new Array2DRowRealMatrix(new double [][]{
//								{data.noiseProperty().get()*data.noiseProperty().get(), 0.0},
//								{0.0, data.noiseProperty().get()*data.noiseProperty().get()}});
						observationNoise = new Array2DRowRealMatrix(new double [][]{
							{data.noiseProperty().get(), 0.0},
							{0.0, data.noiseProperty().get()}});
						
						kalmanfilter.correct(new double[] {measure.get(0), measure.get(1)});
						double [] result = kalmanfilter.getStateEstimation();
						
						Vec kalmanPos = new Vec(result[0],result[1]);
						kalmanDot.setCenterX(kalmanPos.x);
						kalmanDot.setCenterY(kalmanPos.y);
						kalmanTrail.push(kalmanPos);
						
						kalmanfilter.predict();
						double [] predict = kalmanfilter.getStateEstimation();
						kalmanPredict.setStartX(result[0]);
						kalmanPredict.setStartY(result[1]);
						kalmanPredict.setEndX(predict[0]);
						kalmanPredict.setEndY(predict[1]);
					}
					
					measureFrameCount = 0;
					measureTimeFactorAccum = 0.0;
				}
			}
		}
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
//			// No thread option
//			for(Boid b : boids) {
//				b.update(boids, timeFactor);
//			}
//		}
	}
	
//	public class BoidWorker implements Runnable {
//		int index, cores;
//		public BoidWorker(int index, int cores) {
//			this.index = index;
//			this.cores = cores;
//		}
//		@Override
//		public void run() {
//			while(true) {
//			try {
//				synchronized (nextCycleMutex) {
//					nextCycleMutex.wait();		// Wait for next cycle
//					
//				}
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}				
//			for(int b = index; b < boids.size(); b+=cores) {
//				boids.get(b).update(boids, timeFactor);
//			}
//			try {
//				barrier.await(); 	// Reach the barrier for painting
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (BrokenBarrierException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
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
