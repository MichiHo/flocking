package uni.bsc.ba_seminar;

import filter.Attractor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;


public class DataModel {
	public static enum GHFilterMode {
		Normal(true),
		Benedict_Bordner(false);
		public final boolean hChoosable;
		private GHFilterMode(boolean h) {hChoosable = h;}
	}
	
	public static enum AttractorType {
		Eight(new Attractor(t ->  {
			return new Vec(2.0*Math.cos(t),Math.sin(t*2.0));
		})),
		Circle(new Attractor(t ->  {
			return new Vec(Math.cos(t),Math.sin(t));
		})),
		Line(new Attractor(t ->  {
			if(t<Math.PI)
				return new Vec(
						-1.0 + 2.0*t/Math.PI, 
						-1.0 + 2.0*t/Math.PI);
			else
				return new Vec(
						1.0 + 2.0*(Math.PI-t)/Math.PI, 
						1.0 + 2.0*(Math.PI-t)/Math.PI);
			
		}));
		
		public final Attractor attractor;
		private AttractorType(Attractor a) {
			attractor = a;
		}
	}
	
	// Boid Movement
	private BooleanProperty attractorForAll = new SimpleBooleanProperty(false);
	private DoubleProperty attractorSpeed = new SimpleDoubleProperty(0.2);
	private ObjectProperty<AttractorType> attractorType = new SimpleObjectProperty<>(AttractorType.Line);
	private DoubleProperty border = new SimpleDoubleProperty(60.0);
	private DoubleProperty velScale = new SimpleDoubleProperty(1.0);
	private DoubleProperty maxVel = new SimpleDoubleProperty(5.0); 
	private DoubleProperty maxAccel = new SimpleDoubleProperty(0.1);
	private BooleanProperty borderFlip = new SimpleBooleanProperty(true);
	
	// Flocking Parameters
	private DoubleProperty seperationRadius = new SimpleDoubleProperty(20.0);
	private DoubleProperty radius = new SimpleDoubleProperty(50.0);
	private DoubleProperty weightSeperation = new SimpleDoubleProperty(1.5);
	private DoubleProperty weightCohesion = new SimpleDoubleProperty(1.0);
	private DoubleProperty weightAlignment = new SimpleDoubleProperty(1.0);
	private DoubleProperty weightBorder = new SimpleDoubleProperty(2.0);
	private DoubleProperty weightAttractor = new SimpleDoubleProperty(0.0);
	
	// Measure-Simulation
	private DoubleProperty noise = new SimpleDoubleProperty(0.0);
	private IntegerProperty framesPerMeasure = new SimpleIntegerProperty(1);
	
	
	// G-H-Filter
	private ObjectProperty<GHFilterMode> gh_mode = new SimpleObjectProperty<>(GHFilterMode.Normal);
	private BooleanProperty gh_active = new SimpleBooleanProperty(true);
	private DoubleProperty gh_g = new SimpleDoubleProperty(0.1);
	private DoubleProperty gh_h = new SimpleDoubleProperty(0.0);
	
	private BooleanProperty kalman_active = new SimpleBooleanProperty(false);
	
	// Visuals
	private IntegerProperty globalTrailLength = new SimpleIntegerProperty(300);
	private BooleanProperty showPositionTrail = new SimpleBooleanProperty(false);
	private BooleanProperty showMeasureTrail = new SimpleBooleanProperty(false);
	private BooleanProperty showFilterTrail = new SimpleBooleanProperty(true);
	private BooleanProperty showMeasureNoise = new SimpleBooleanProperty(false);
	private BooleanProperty showAttractor = new SimpleBooleanProperty(true);
	
	public final DoubleProperty noiseProperty() {
		return this.noise;
	}
	
	public final double getNoise() {
		return this.noiseProperty().get();
	}
	
	public final void setNoise(final double noise) {
		this.noiseProperty().set(noise);
	}
	
	public final IntegerProperty globalTrailLengthProperty() {
		return this.globalTrailLength;
	}
	
	public final int getGlobalTrailLength() {
		return this.globalTrailLengthProperty().get();
	}
	
	public final void setGlobalTrailLength(final int globalTrailLength) {
		this.globalTrailLengthProperty().set(globalTrailLength);
	}
	
	public final DoubleProperty borderProperty() {
		return this.border;
	}
	
	public final double getBorder() {
		return this.borderProperty().get();
	}
	
	public final void setBorder(final double border) {
		this.borderProperty().set(border);
	}
	
	public final IntegerProperty framesPerMeasureProperty() {
		return this.framesPerMeasure;
	}
	
	public final int getFramesPerMeasure() {
		return this.framesPerMeasureProperty().get();
	}
	
	public final void setFramesPerMeasure(final int framesPerMeasure) {
		this.framesPerMeasureProperty().set(framesPerMeasure);
	}
	
	public final BooleanProperty attractorForAllProperty() {
		return this.attractorForAll;
	}
	
	public final boolean isAttractorForAll() {
		return this.attractorForAllProperty().get();
	}
	
	public final void setAttractorForAll(final boolean attractorForAll) {
		this.attractorForAllProperty().set(attractorForAll);
	}

	public final DoubleProperty velScaleProperty() {
		return this.velScale;
	}
	

	public final double getVelScale() {
		return this.velScaleProperty().get();
	}
	

	public final void setVelScale(final double velScale) {
		this.velScaleProperty().set(velScale);
	}
	

	public final DoubleProperty seperationRadiusProperty() {
		return this.seperationRadius;
	}
	

	public final double getSeperationRadius() {
		return this.seperationRadiusProperty().get();
	}
	

	public final void setSeperationRadius(final double seperationRadius) {
		this.seperationRadiusProperty().set(seperationRadius);
	}
	

	public final DoubleProperty radiusProperty() {
		return this.radius;
	}
	

	public final double getRadius() {
		return this.radiusProperty().get();
	}
	

	public final void setRadius(final double radius) {
		this.radiusProperty().set(radius);
	}
	

	public final DoubleProperty weightSeperationProperty() {
		return this.weightSeperation;
	}
	

	public final double getWeightSeperation() {
		return this.weightSeperationProperty().get();
	}
	

	public final void setWeightSeperation(final double weightSeperation) {
		this.weightSeperationProperty().set(weightSeperation);
	}
	

	public final DoubleProperty weightCohesionProperty() {
		return this.weightCohesion;
	}
	

	public final double getWeightCohesion() {
		return this.weightCohesionProperty().get();
	}
	

	public final void setWeightCohesion(final double weightCohesion) {
		this.weightCohesionProperty().set(weightCohesion);
	}
	

	public final DoubleProperty weightAlignmentProperty() {
		return this.weightAlignment;
	}
	

	public final double getWeightAlignment() {
		return this.weightAlignmentProperty().get();
	}
	

	public final void setWeightAlignment(final double weightAlignment) {
		this.weightAlignmentProperty().set(weightAlignment);
	}
	

	public final DoubleProperty weightBorderProperty() {
		return this.weightBorder;
	}
	

	public final double getWeightBorder() {
		return this.weightBorderProperty().get();
	}
	

	public final void setWeightBorder(final double weightBorder) {
		this.weightBorderProperty().set(weightBorder);
	}
	

	public final DoubleProperty weightAttractorProperty() {
		return this.weightAttractor;
	}
	

	public final double getWeightAttractor() {
		return this.weightAttractorProperty().get();
	}
	

	public final void setWeightAttractor(final double weightAttractor) {
		this.weightAttractorProperty().set(weightAttractor);
	}
	

	public final DoubleProperty maxVelProperty() {
		return this.maxVel;
	}
	

	public final double getMaxVel() {
		return this.maxVelProperty().get();
	}
	

	public final void setMaxVel(final double maxVel) {
		this.maxVelProperty().set(maxVel);
	}
	

	public final DoubleProperty maxAccelProperty() {
		return this.maxAccel;
	}
	

	public final double getMaxAccel() {
		return this.maxAccelProperty().get();
	}
	

	public final void setMaxAccel(final double maxAccel) {
		this.maxAccelProperty().set(maxAccel);
	}

	public final BooleanProperty gh_activeProperty() {
		return this.gh_active;
	}
	

	public final boolean isGh_active() {
		return this.gh_activeProperty().get();
	}
	

	public final void setGh_active(final boolean gh_active) {
		this.gh_activeProperty().set(gh_active);
	}
	

	public final DoubleProperty gh_gProperty() {
		return this.gh_g;
	}
	

	public final double getGh_g() {
		return this.gh_gProperty().get();
	}
	

	public final void setGh_g(final double gh_g) {
		this.gh_gProperty().set(gh_g);
	}
	

	public final DoubleProperty gh_hProperty() {
		return this.gh_h;
	}
	

	public final double getGh_h() {
		return this.gh_hProperty().get();
	}
	

	public final void setGh_h(final double gh_h) {
		this.gh_hProperty().set(gh_h);
	}

	public final DoubleProperty attractorSpeedProperty() {
		return this.attractorSpeed;
	}
	

	public final double getAttractorSpeed() {
		return this.attractorSpeedProperty().get();
	}
	

	public final void setAttractorSpeed(final double attractorSpeed) {
		this.attractorSpeedProperty().set(attractorSpeed);
	}

	public final BooleanProperty showMeasureTrailProperty() {
		return this.showMeasureTrail;
	}
	

	public final boolean isShowMeasureTrail() {
		return this.showMeasureTrailProperty().get();
	}
	

	public final void setShowMeasureTrail(final boolean showMeasureTrail) {
		this.showMeasureTrailProperty().set(showMeasureTrail);
	}
	

	public final BooleanProperty showPositionTrailProperty() {
		return this.showPositionTrail;
	}
	

	public final boolean isShowPositionTrail() {
		return this.showPositionTrailProperty().get();
	}
	

	public final void setShowPositionTrail(final boolean showPositionTrail) {
		this.showPositionTrailProperty().set(showPositionTrail);
	}

	public final BooleanProperty borderFlipProperty() {
		return this.borderFlip;
	}
	

	public final boolean isBorderFlip() {
		return this.borderFlipProperty().get();
	}
	

	public final void setBorderFlip(final boolean borderFlip) {
		this.borderFlipProperty().set(borderFlip);
	}

	public final ObjectProperty<GHFilterMode> gh_modeProperty() {
		return this.gh_mode;
	}
	

	public final GHFilterMode getGh_mode() {
		return this.gh_modeProperty().get();
	}
	

	public final void setGh_mode(final GHFilterMode gh_mode) {
		this.gh_modeProperty().set(gh_mode);
	}

	public final BooleanProperty showMeasureNoiseProperty() {
		return this.showMeasureNoise;
	}
	

	public final boolean isShowMeasureNoise() {
		return this.showMeasureNoiseProperty().get();
	}
	

	public final void setShowMeasureNoise(final boolean showMeasureNoise) {
		this.showMeasureNoiseProperty().set(showMeasureNoise);
	}

	public final ObjectProperty<AttractorType> attractorTypeProperty() {
		return this.attractorType;
	}
	

	public final AttractorType getAttractorType() {
		return this.attractorTypeProperty().get();
	}
	

	public final void setAttractorType(final AttractorType attractorType) {
		this.attractorTypeProperty().set(attractorType);
	}

	public final BooleanProperty showAttractorProperty() {
		return this.showAttractor;
	}
	

	public final boolean isShowAttractor() {
		return this.showAttractorProperty().get();
	}
	

	public final void setShowAttractor(final boolean showAttractor) {
		this.showAttractorProperty().set(showAttractor);
	}

	public final BooleanProperty showFilterTrailProperty() {
		return this.showFilterTrail;
	}
	

	public final boolean isShowFilterTrail() {
		return this.showFilterTrailProperty().get();
	}
	

	public final void setShowFilterTrail(final boolean showFilterTrail) {
		this.showFilterTrailProperty().set(showFilterTrail);
	}

	public final BooleanProperty kalman_activeProperty() {
		return this.kalman_active;
	}
	

	public final boolean isKalman_active() {
		return this.kalman_activeProperty().get();
	}
	

	public final void setKalman_active(final boolean kalman_active) {
		this.kalman_activeProperty().set(kalman_active);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
