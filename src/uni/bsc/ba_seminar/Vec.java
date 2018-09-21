package uni.bsc.ba_seminar;

/**
 * Utility class for 2D Vector
 * 
 * @author Gebhardt, Hochmuth, Kiriakidou
 * @version 2018/04/24
 */
public class Vec {
	

	public double x, y;
	private final double EPSILON = 0.00001;

	public Vec() {
		x = 0.0;
		y = 0.0;
	}
	
	/**
	 * A vector with the given components
	 * @param x 
	 * @param y
	 */
	public Vec(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * A vector as copy of another
	 * @param other Vector to copy from.
	 */
	public Vec(Vec other) {
		this.x=other.x;
		this.y=other.y;
	}

	/**
	 * Checks, if this vector and the given vector describe the same vector.
	 * @param other The other vector to compare.
	 * @return True, iff all components are equal.
	 */
	public boolean equals(Vec other) {
		return x==other.x && y==other.y;
	}


	/**
	 * Checks, if the two vectors are parallel (or antiparallel)
	 * @param other The Vector to check with this.
	 * @return true if the Scalar Product of this & other is 1
	 */
	public boolean parallels(Vec other) {
		return Math.abs(Math.abs(sProd(other))- length()*other.length()) < EPSILON;
	}

	/**
	 * Checks if this Vector is a Nullvector
	 * @return True if x == y == 0.0
	 */
	public boolean isNull() {
		return x==0.0 && y==0.0;
	}

	/**
	 * Return the sum of this vector and v. 
	 * @param v The vector to add
	 * @return The sum
	 */
	public Vec add(Vec v) {
		return new Vec(x+v.x,y+v.y);
	}

	/**
	 * Return the difference between this vector and v
	 * @param v The vector to subtract
	 * @return The difference
	 */
	public Vec sub(Vec v) {
		return new Vec(x-v.x,y-v.y);
	}

	/**
	 * Return the product of this vector and a scalar
	 * @param scalar The scalar value to multiply with
	 * @return The scaled vector
	 */
	public Vec mult(double scalar) {
		return new Vec(x*scalar,y*scalar);
	}
	
	public Vec div(double scalar) {
		scalar = 1.0 / scalar;
		return new Vec(x*scalar, y*scalar);
	}
	
	/**
	 * Returns a Vec in this direction no longer than length
	 * @param length
	 * @return
	 */
	public Vec limit(double length) {
		if(length < 0.0) throw new ArithmeticException("Length must be positive");
		double l = length();
		if(l> length)
			return div(l).mult(length);
		else
			return copy();
	}
	
	/**
	 * Returns a copy of this Vec
	 * @return
	 */
	public Vec copy() {return new Vec(this);}

	/**
	 * Return the scalar product of this vector and vec
	 * @param vec The vector to multiply this with
	 * @return The dot product
	 */
	public double sProd(Vec vec) {
		return x*vec.x + y*vec.y;
	}

	/**
	 * Return the length i.e. norm of this vector
	 * @return The length.
	 */
	public double length() {
		return Math.sqrt(x*x+y*y);
	}
	
	/**
	 * Return the length i.e. norm of this vector
	 * @return The length.
	 */
	public double sqrLength() {
		return x*x+y*y;
	}
	
	public double sqrDistance(Vec other) {
		double xx = other.x - x;
		double yy = other.y - y;
		return xx*xx + yy*yy;
	}
	
	public double distance(Vec other) {
		return Math.sqrt(sqrDistance(other));
	}

	/**
	 * Return a normed version of this vector. Returns a new Nullvector
	 * if this is a null-vector.
	 * @return A vector with same direction as this but length 1
	 */
	public Vec norm() {
		if(isNull())
			return new Vec();
		return this.mult(1/this.length());
	}
	
	/**
	 * A method to create a vector that is orthogonal to this.
	 * @return a orthogonal vector.
	 */
	public Vec orthVec() {
		
		return new Vec(this.y,this.x*(-1));
	}
}
