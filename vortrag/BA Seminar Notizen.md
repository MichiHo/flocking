BA Seminar Notizen
==================


# Implementierng beispiele
Flocking
Steering Behaviours

The coding train steering flocking combine YT

# In den Vortrag reinnehmen
Markov model 


# Kalman Filter

## Varianten
Kommen mit Nichtlinearitäten klar:

-	Erweitertes Kalman-Filter (EKF) 
-	Unscented Kalman-Filter (UKF)

Wiki: "Inzwischen verwendet man eher mit anderen Verfahren"??

-	https://de.wikipedia.org/wiki/Sequenzielle_Monte-Carlo-Methode
-	Näherungsverfahren : Quadratur-Filter, Gaußsummenfilter, Projektionsfilter

## Implementierungen
-	" Wurzelimplementierung nach Potter et al. " (-> Cholesky Zerlegung?)
-	Bierman-Thornton-UD-Algorithmus


## Begriffe

-	Transfer-Function - maps input to output of system. in DSP mostly graphs frequency response
-	Recursive Filter - uses >=1 of its outputs as input
	-	Infinite Impulse Response - Recursive filters theoretically have infinite response.
		But Quantization makes it finite anyway
.	Biquad Filter