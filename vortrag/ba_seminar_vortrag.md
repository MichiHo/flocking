---
title: "Bachelor Seminar : Kalman Filter"
geometry: margin=1.0cm
papersize: a4
---

# Struktur

1.  Introduction
	*   Examples?
2.  g-h-Filter
3.  HMM
4.  Kalman-Filter
5.  Outlook: Extended / Unscented Kalman Filter
6.  End.


-   Hidden Markov model?

# Introduction

*   Concept of the presented filters: Combine inaccurate measures and worldmodel-based predictions to achieve better accuracy.
*   Use case: **Tracking** of real Objects 

# G-H-Filter

-   State variables:
	-   $\hat{x}_k$ like position
	-   $\hat{v}_k$ like velocity
-   Input:
	-   Measures $x_k$
-   World Model: 
	-   $\hat{x}_k$ is being predicted each time interval $\Delta t$ as 
		$\hat{x}_k = \hat{x}_{k-1} + \Delta t \cdot \hat{v}_{k-1}$
-   The Update-step with assumedly inaccurate measure $x_k$ uses the **Residual** $\hat{r}_k = x_k - \hat{x}_k$ scaled by **Parameters $g$ and $h$** to correct predictions:
	-   $\hat{x}_k = \hat{x}_{k-1} + \Delta t \cdot \hat{v}_{k-1} + g\cdot \hat{r}_k$
	-   $\hat{v}_k = \hat{v}_{k-1}  + \frac{h}{\Delta t}\cdot \hat{r}_k$

## Notes
-	G-H-Filter is also used to *predict*: after update comes next predict already, with time-constant used.
	-	many sensors (radar, etc) measure in constant intervals.

## Choice of $g$ and $h$
Big Parameters match transients but also emphasize noise.
Small Parameters reduce noise but might lead to divergence from real position.

Big values for $g$ that aren't corrected with big $h$ values can push the filter into Resonance and make the velocity increase unreasonably fast.

If chosen well, the algorithm *filters* Measurement-Noise and leads to smoother and more accurate results in tracking

The **Benedict-Bordner-Filter** is a g-h-filter with $h$ chosen relative to $g$ as $h = \frac{g^2}{2-g}$.

## Disadvantages
-   Reacts poorly to more complex acceleration


# Kalman-Filter
*	Kalman Filter represents state as Propability Distributions with mean $x$ and (co)variance $P$
*	abstracts state transition to any linear operation 
*	adds **control** vector & model
*	adds **observation-model** - doesn't assume observations of same unit as state
*	$g$ and $h$ are now time-dependent and functions of (co)variances of both measurement and prior state
	-> **kalman-gain** scales each component of the innovation individually

## 1-D example
In 1-D with state estimate $(x_{k | k-1},Var(x_{k | k-1}))$ comes measurement $(y_k,Var(y_k))$.

Weigh both with the **inverse Variance** (higher Var -> lower Precision) to recieve:

$$x_{k|k} = \frac{Var^{-1}(x_{k|k-1})x_{k|k-1} + Var^{-1}(y_{k})y_{k}}
{Var^{-1}(x_{k|k-1}) + Var^{-1}(y_{k})}$$

## Model

-   State Variables:
	-   **State** $\hat{x}_k \in \mathbb{R}^n$
		-   *z.B. Position, Geschwindigkeit*
	-   **Kovariance-Matrix** $P_k \in \mathbb{R}^{n\times n}$
-   Input:
	-   **Measurement / Sensor Reading** $z_k$ ( generally not of the same unit as $x_k$ )
	-   **Covariance of Observation Noise** $R_k$ 
	-   **Control Vector** $u_k$ : deterministic and known influence on the state 
		-	*e.g. motor control voltage*
	-	
-   World Model:
	-   The **Prediction-Matrix (State-transition model)** $F_k \in \mathbb{R}^{n\times n}$ 
		-   converts $\hat{x}_k$ to $\hat{x}_{k+1}$. This can be used to realize position & velocity as in the g-h-filter, but also *Acceleration* or rather derivations of any degree.
		-	The Prediction-Matrix also converts $P_k$ to $P_{k+1}$
	-	The **Control-Matrix (Control-input model)** $B_k$ converts the control-vector $u_k$ to the units of the state variable.
		-	*z.B. Voltagelevel on Engine leading to increased velocity*
	-	The **Observation model** $H_k$ converts the state-variable to the units of the measurement-vector (in case the sensors use different units than the kalman filter's state model)
	-	**Covariance of Process Noise** $Q_k$
	-	The **Kalman-Gain** $\hat{K}_k$ scales the Residual in proportion to the prediction by evaluating the current sensor-accuracy

## Notes
-	Kalman Filter is a common **Sensor Fusion** Algorithm
-	Kalman Filter analogous to Hidden Markov Model but with **continous** hidden variables
-	Kalman Filter is optimal linear filter, assumed
	1.	the model perfectly matches the real system
	2.	the entering noise is white (uncorrelated) 
	3.	the covariances of the noise are exactly known

# HMM

## Markov Model (probability theory)

-	stochastic model for randomly changing systems
-	**markov property** : future state(s) depend only on current
## Markov Chain
for fully observable systems

= sequence of random variables $x \in S$ with **countable** and often finite State-Space $S$

Transitions between states are given per Timestep (or globally, if *time-homogeneous*)

-	If S is finite -> directed graph, nodes in $S$, edges the probability
-	or Transition Matrix
example: PageRank algorithm by Google

## Hidden Markov Model
for partially observable systems!

-	Not the state variables $x \in S$ are observable, just **output tokens** $z$ that depend on the state
	-	Each state $x$ has probability-dist over possible tokens $p(x | z)$
	-	sequence of tokens gives only **some** information.
-	Instead of hidden state variable, the probability for each state is stored

-	can be represented as simple **Dynamic Bayesian Network** (Transition probabilities from one *explicit* time to the next timestep).

examples 
-	reinforcement learning
-	pattern recognition (speech, handwriting, gestures)
-	bioinformatics