package uni.bsc.ba_seminar;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;


public class DataModel {
	private DoubleProperty noise = new SimpleDoubleProperty(0.0);
	private IntegerProperty globalTrailLength = new SimpleIntegerProperty(300);
	private DoubleProperty border = new SimpleDoubleProperty(60.0);
	private IntegerProperty framesPerMeasure = new SimpleIntegerProperty(1);
	private BooleanProperty attractorForAll = new SimpleBooleanProperty(false);
	
	private DoubleProperty velScale = new SimpleDoubleProperty(1.0);
	private DoubleProperty seperationRadius = new SimpleDoubleProperty(20.0);
	private DoubleProperty radius = new SimpleDoubleProperty(50.0);
	private DoubleProperty weightSeperation = new SimpleDoubleProperty(1.5);
	private DoubleProperty weightCohesion = new SimpleDoubleProperty(1.0);
	private DoubleProperty weightAlignment = new SimpleDoubleProperty(1.0);
	private DoubleProperty weightBorder = new SimpleDoubleProperty(2.0);
	private DoubleProperty weightAttractor = new SimpleDoubleProperty(0.0);
	private DoubleProperty maxVel = new SimpleDoubleProperty(5.0); 
	private DoubleProperty maxAccel = new SimpleDoubleProperty(0.1);
	
	private BooleanProperty gh_active = new SimpleBooleanProperty(true);
	private DoubleProperty gh_g = new SimpleDoubleProperty(0.1);
	private DoubleProperty gh_h = new SimpleDoubleProperty(0.0);
	
	
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
	
	
	
	
	
	
}
