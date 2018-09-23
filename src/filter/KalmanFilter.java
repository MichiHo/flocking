package filter;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * Implementation of a Kalman-Filter operation on double values.
 * Sizes for state, observation and control vectors can be set individually.
 * 
 * @author Michael Hochmuth
 *
 */
public class KalmanFilter {

	private DoubleMatrix2D stateTransition;
	private DoubleMatrix2D observationModel;
	private DoubleMatrix2D controlModel;
	private DoubleMatrix2D processNoise;
	private DoubleMatrix2D stateCovariance;
	private DoubleMatrix1D state;
	
	private int stateSize, observationSize, controlSize;
	
	public KalmanFilter(int stateDimension) {
		this.stateSize = stateDimension;
		observationSize = stateDimension;
		controlSize = 0;
		
		state = new DenseDoubleMatrix1D(stateDimension);
		// Covariance = 0 means the initial state is assumed totally precise
		stateCovariance = DoubleFactory2D.dense.make(stateDimension, stateDimension);
		stateTransition = DoubleFactory2D.dense.identity(stateDimension);
		observationModel = DoubleFactory2D.dense.identity(stateDimension);
		processNoise = DoubleFactory2D.dense.make(stateDimension, stateDimension);
	}
	
	/**
	 * Get the current state (mean) of the filter
	 * @return
	 */
	public DoubleMatrix1D getState() {
		return state.copy();
	}
	
	/**
	 * Set the state of this filter. Use this method for initializing the Filter, if you
	 * wish some other start point than zero.
	 * @param state State Mean
	 * @param stateCovariance State Covariance Matrix
	 */
	public void setState(DoubleMatrix1D state, DoubleMatrix2D stateCovariance) {
		if(state.size()!=stateSize || stateCovariance.rows()!=stateSize
				|| stateCovariance.columns()!=stateSize)
			throw new IllegalArgumentException("State size must be upheld");
		
		this.state = state;
		this.stateCovariance = stateCovariance;
	}
	
	/**
	 * Set the inherent process noise to be added to the stateCovariance
	 * each timestep
	 * @param processNoise
	 */
	public void setProcessNoise(DoubleMatrix2D processNoise) {
		if(processNoise.rows()!=stateSize || processNoise.columns()!=stateSize) 
			throw new IllegalArgumentException("State size must be upheld");
		
		this.processNoise = processNoise;
	}
	
	/**
	 * Set the Model for transitioning states and covariances. Must be of quadratic of 
	 * same size as state.
	 * @param stateTransitionModel
	 */
	public void setStateTransitionModel(DoubleMatrix2D stateTransitionModel) {
		if(stateTransitionModel.rows()!=stateSize || stateTransitionModel.columns()!=stateSize) 
			throw new IllegalArgumentException("State size must be upheld");
		
		this.stateTransition = stateTransitionModel;
	}
	
	/**
	 * Set the observation model to transform observations to state-space in a way
	 * of state = model * observation. Therefore the row-count must be the stateDimension
	 * and the column-count redefines the observationDimension
	 * @param model Matrix
	 */
	public void setObservationModel(DoubleMatrix2D model) {
		if(model.columns()!=stateSize)
			throw new IllegalArgumentException("The observation model must have the "
					+ "same row-count as the state has dimensions");
		observationSize = model.rows();
		observationModel = model;
	}
	
	/**
	 * Perform one predict+update - step.
	 * @param observation Observation made. Must be of observationDimension size
	 * @param observationCovariance Covariance of the observation.
	 * @param control Control-information. Pass null to denote no control made
	 */
	public void step(DoubleMatrix1D observation, DoubleMatrix2D observationCovariance, 
			DoubleMatrix1D control) {
		if(observation.size()!=observationSize)
			throw new IllegalArgumentException("Illegal observation size. Got " + 
					observation.size() + " but expected "+observationSize);
		
		if(observationCovariance.rows()!=observationCovariance.columns() || 
				observationCovariance.rows()!=observationSize)
			throw new IllegalArgumentException("Illegal observation covariance size");
		
		if(control!=null && control.size()!=controlSize)
			throw new IllegalArgumentException("Illegal control size");
		
		// PREDICT
		// ... state mean
		state = stateTransition.zMult(state, null);	//x' = F * x
		if(control!=null && controlSize>0 && controlModel != null) {
			DoubleMatrix1D controlInfluence = DoubleFactory1D.dense.make(stateSize);
			controlModel.zMult(control, controlInfluence);
			state.assign(controlInfluence, (i,j)->{return i+j;});	//.. + B * u
		}
		
		// ... state covariance
		stateCovariance = stateCovariance.zMult(stateTransition, null,1.0,0.0,false,true); // P = P*F^T
		stateCovariance = stateTransition.zMult(stateCovariance, null); // P = F * (P * F^T)
		stateCovariance.assign(processNoise, (i,j)->{return i+j;}); // P = (F * (P * F^T)) + Q
		
		// UPDATE
		//System.out.println("obsM " + observationModel.rows() + "x"+observationModel.columns()+" , state "+state.size());
		DoubleMatrix1D innov = DoubleFactory1D.dense.make(2);
		observationModel.zMult(state, innov, 1.0, 0.0, false); // H * x'
		innov.assign(observation, (hx,z)->{return z - hx;}); // y = z - (H * x')
		
		DoubleMatrix2D covInnov = stateCovariance.zMult(observationModel, null,1.0,0.0,false,true); // P H^T
		covInnov = observationModel.zMult(covInnov, null, 1.0, 0.0, false,false); // H (P H^t)
		covInnov.assign(observationCovariance, (temp,r)-> {return r - temp;}); // S = R - (H (P H^T))
		
		Algebra a = new Algebra();
		
		DoubleMatrix2D kalmanGain = observationModel.zMult(
				a.inverse(covInnov), null, 1.0, 0.0, true,false); // H^T * S^-1
		kalmanGain = stateCovariance.zMult(kalmanGain, null, 1.0, 0.0, false,false); // K = P * (H^T * S^-1)
		
		state.assign(kalmanGain.zMult(innov, null), (i,j)->{return i+1;}); // x = x' + K * y
		
		DoubleMatrix2D ikh = kalmanGain.zMult(observationModel, null); // K * H
		ikh.assign(DoubleFactory2D.dense.identity(stateSize), (kh,i)->{return i - kh;}); // ikh = I - (K * H)
		
		stateCovariance = stateCovariance.zMult(ikh, null,1.0,0.0,false,true); // P' * ikh^T
		stateCovariance = ikh.zMult(stateCovariance, null); // ikh * (P * ikh^T)
		
		DoubleMatrix2D obsCovScaled = observationCovariance.zMult(
				kalmanGain, null,1.0,0.0,false,true); // R K^T
		obsCovScaled = kalmanGain.zMult(obsCovScaled, null); // K (R K^T)
		
		stateCovariance.assign(obsCovScaled,(i,j)->{return i + j;}); // P = ikh * (P * ikh^T) + K (R K^T)
	}
}
