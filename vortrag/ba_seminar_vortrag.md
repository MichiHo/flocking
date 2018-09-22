---
title: "Bachelor Seminar : Kalman Filter"
geometry: margin=1.0cm
papersize: a4
---

# Struktur

1.  Introduction
	*   Examples?
2.  g-h-Filter
3.  Kalman-Filter
4.  Outlook: Extended / Unscented Kalman Filter
5.  End.


-   Hidden Markov model?

# 1. Introduction

*   Concept of the presented filters: Combine inaccurate measures and worldmodel-based predictions to achieve better accuracy.
*   Use case: **Tracking** of real Objects 

# 2. G-H-Filter

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

## Disadvantages
-   Reacts poorly to more complex acceleration


# 3. Kalman-Filter

-   State Variables:
	-   **State** $\hat{x}_k \in \mathbb{R}^n$
		-   *z.B. Position, Geschwindigkeit*
	-   **Kovariance-Matrix** $P_k \in \mathbb{R}^{n\times n}$
-   Input:
	-   **Measurement / Sensor Reading** $z_k$ ( generally not of the same unit as $x_k$ )
	-   **Kovariance of Observation Noise** $R_k$ 
	-   **Control Vector** $u_k$ beschreibt den deterministischen & bekannten Einfluss auf den Zustand. 
		-	*z.B. Steuerung der Motoren*
	-	
-   World Model:
	-   The **Prediction-Matrix (State-transition model)** $F_k \in \mathbb{R}^{n\times n}$ 
		-   converts $\hat{x}_k$ to $\hat{x}_{k+1}$. This can be used to realize position & velocity as in the g-h-filter, but also *Acceleration* or rather derivations of any degree.
		-	The Prediction-Matrix also converts $P_k$ to $P_{k+1}$
	-	The **Control-Matrix (Control-input model)** $B_k$ converts the control-vector $u_k$ to the units of the state variable.
		-	*z.B. Voltagelevel on Engine leading to increased velocity*
	-	The **Sensor-Matrix(name?) (Observation model)** $H_k$ converts the state-variable to the units of the measurement-vector (in case the sensors use different units than the kalman filter's state model)
	-	The **Kalman-Gain** $\hat{K}_k$ scales the Residual in proportion to the prediction by evaluating the current sensor-accuracy

## Theory
-	Kalman Filter analogous to Hidden Markov Model but with **continous** hidden variables

## 3.n Measuring
