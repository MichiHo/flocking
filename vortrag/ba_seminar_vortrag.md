Bachelor Seminar : Kalman Filter
================================

## Struktur

1.  Einführung
	*   Standardbeispiele?
2.  g-h-Filter
3.  Kalman-Filter
4.  Ausblick: Extended / Unscented Kalman Filter
5.  Schluss


-   Hidden Markov model?

## 1. Einführung

*   Konzept der hier vorgestellten Filter: Ungenaue Messungen kombinieren mit Vorhersagen anhand des Weltmodells und des Vergangenen Zustandes (eventuell auch mit Steuerinformationen) um **genauere** Ergebnisse zu bekommen.

## 2. G-H-Filter

-   Zustandsvariablen:
	-   $\hat{x}_k$ entspricht der Position
	-   $\hat{v}_k$ entspricht der Geschwindigkeit
-   Eingaben:
	-   Messung $x_k$
-   Weltmodell: 
	-   $\hat{x}_k$ wird pro Zeitschritt $\Delta t$ als 
		$\hat{x}_k = \hat{x}_{k-1} + \Delta t \cdot \hat{v}_{k-1}$ upgedated
-   Bei Update-Schritt mit der ungenauen Messung $x_k$ wird zusätzlich mit dem **Residual** $\hat{r}_k = x_k - \hat{x}_k$ und den **Parametern $g$ und $h$** korrigiert:
	-   $\hat{x}_k = \hat{x}_{k-1} + \Delta t \cdot \hat{v}_{k-1} + g\cdot \hat{r}_k$
	-   $\hat{v}_k = \hat{v}_{k-1}  + \frac{h}{\Delta t}\cdot \hat{r}_k$

### Wahl von $g$ und $h$
Große Parameter reagieren gut auf Transienten aber auch auf Rauschen. 
Kleine Parameter filtern Rauschen aber auch Transienten raus.

Bei richtiger Wahl der Parameter, *filtert* der G-H-Algorithmus Rauschen heraus und erzeugt schönere Ergebnisse.

### Nachteile
-   Beschleunigung kein Teil des Modells
-   Sehr simpel


## 3. Kalman-Filter

-   Zustandsvariablen:
	-   **Zustand** $\hat{x}_k \in \mathbb{R}^n$
		-   *z.B. Position, Geschwindigkeit*
	-   **Kovarianzmatrix** $P_k \in \mathbb{R}^{n\times n}$
-   Eingaben:
	-   **Messung** $z_k$ ( i.Allg. nicht gleiche Einheiten wie $x_k$ )
	-   **Messungskovarianz** 
	-   **Störung** $u_k$ beschreibt den deterministischen & bekannten Einfluss auf den Zustand. 
		-	*z.B. Steuerung der Motoren*
	-	
-   Weltmodell:
	-   Die **Prediction-Matrix** $F_k \in \mathbb{R}^{n\times n}$ Ü
		-   Überführt 
		$\hat{x}_k$ in $\hat{x}_{k+1}$. Damit lässt sich insbesondere der gesamte g-h-Filter realisieren, aber auch *Beschleunigung* bzw Ableitungen beliebigen Grades.
		-	Die Prediction-Matrix überführt auch $P_k$ zu $P_{k+1}$
	-	Die **Störungs-Dynamik(name?)** $B_k$ überführt die Größe der Störung $u_k$ in die Einheiten des Zustandes.
		-	*z.B. Motorspannung beschleunigt etwas*
	-	Die **Sensor-Matrix(name?)** $H_k$ überführt die Zustandsvariablen in den entsprechenden Sensoroutput (falls Sensoren andere Einheiten / Skalen verwenden als das Modell)
	-	Der **Kalman-Gain** $\hat{K}_k$ skaliert das Residual im Verhältnis zur Prediction anhand beider Genauigkeiten (?)
