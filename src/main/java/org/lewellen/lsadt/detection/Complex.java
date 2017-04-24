package org.lewellen.lsadt.detection;

import java.io.Serializable;

public class Complex implements Serializable {
	private static final long serialVersionUID = -2827190564820712996L;
	
	public static final Complex PositiveRealUnit;
    public static final Complex NegativeRealUnit;
    public static final Complex PositiveImaginaryUnit;
    public static final Complex NegativeImaginaryUnit;
    public static final Complex Zero;

    static {
        PositiveRealUnit = new Complex(1);
        NegativeRealUnit = new Complex(-1);
        PositiveImaginaryUnit = new Complex(0, 1);
        NegativeImaginaryUnit = new Complex(0, -1);
        Zero = new Complex(0, 0);
    }

    public final double Real;
    public final double Imaginary;

    public Complex() { 
    	Real = 0.0;
    	Imaginary = 0.0;
    }

    public Complex(double real) { 
    	Real = real;
    	Imaginary = 0.0;
    }

    public Complex(double real, double imaginary) {
        Real = real;
        Imaginary = imaginary;
    }

    public Complex add(Complex v) { 
        return new Complex(Real + v.Real, Imaginary + v.Imaginary);
    }

    public Complex exp() {
        // e^(a + bi)
        // = e^(a) e^(bi)
        // = e^(a) (cos(b) + i sin(b))

        return new Complex(
            Math.exp(Real) * Math.cos(Imaginary),
            Math.exp(Real) * Math.sin(Imaginary)
        );
    }
    
    public Complex multiply(Complex v) {
        // (a + bi) * (c * di)
        // = ac + adi + bic + bidi
        // = (ac - bd) + (ad + bc)i 

        return new Complex(Real * v.Real - Imaginary * v.Imaginary, Real * v.Imaginary + Imaginary * v.Real);
    }

    public Complex multiply(double a) {
        return new Complex(a * Real, a * Imaginary);
    }
    
    @Override
    public boolean equals(Object obj) {
    	if(obj == null)
    		return super.equals(obj);
    	
    	if(!(obj instanceof Complex))
    		return super.equals(obj);

    	Complex other = (Complex)obj;
    	
    	return Math.abs(other.Real - Real) < 1e-12 && Math.abs(other.Imaginary - Imaginary) < 1e-12;    	
    };

    @Override
    public String toString() {
        return String.format("%.2f + %.2fi", Real, Imaginary);
    }
}