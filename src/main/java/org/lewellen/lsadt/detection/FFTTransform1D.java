package org.lewellen.lsadt.detection;

public class FFTTransform1D {
	public Complex[] Transform(Complex[] x) {
        if (x.length == 1)
            return x;

        Complex[] x_odd = new Complex[x.length >> 1];
        Complex[] x_even = new Complex[x.length >> 1];
        for (int i = 0; i < x.length >> 1; i++) {
            x_even[i] = x[2 * i];
            x_odd[i] = x[2 * i + 1];
        }

        Complex[] X_odd = Transform(x_odd);
        Complex[] X_even = Transform(x_even);

        Complex[] X = new Complex[x.length];
        for (int i = 0; i < X.length; i++)
            X[i] =
            	X_even[i % (x.length >> 1)].add(
        			(new Complex(0.0, -i * 2 * Math.PI / (double)x.length).exp()).multiply(
    					X_odd[i % (x.length >> 1)]
					)
    			);

        return X;		
	}
}
