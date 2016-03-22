package ch.ethz.scu.obit.bdfacsdivafcs.readers;

import java.text.DecimalFormat;

/**
 * Hyperlog transform and inverse transform for flow cytometry data.
 * 
 * @author Aaron Ponti
 * 
 * Original author Rachel Finck (C++)
 * 
 * @see "http://www.mathworks.com/matlabcentral/fileexchange/45034-hyperlog-transformation"
 * @see "http://flowcyt.sourceforge.net/gating/latest.pdf (chapter 6.6)"
 * 
 * Parameters:
 * 
 * T: T ∈ R, T > 0 is the "top of scale" value.
 * 
 * W: W ∈ R, 0 < W ≤ M/2 is the number of decades in the approximately linear 
 *    region.
 * 
 * M: M ∈ R, M > 0 is the number of decades that the true logarithmic scale 
 *    approached at the high end of the Hyperlog scale would cover in the plot
 *    range.
 *    If omitted, M has a default value of 4.5.
 * 
 * A: A ∈ R, −W ≤ A ≤ M − 2W is the number of additional decades of negative
 *    data values to be included.
 *    If omitted, A has a default value of 0.0.
 */
public class Hyperlog {

	// Constants

	/**
	 * Default decades for the transform
	 */
	static public final double DEFAULT_DECADES = 4.5;

	/**
	 * Natural logarithm of 10. 
	 */
	static public final double LN_10 = Math.log(10.0);
	
	/**
	 * Length of the Taylor expansion
	 */
	static public final int TAYLOR_LENGTH = 16;
	
	/**
	 * Epsilon
	 */
	static public final double EPSILON = 1e-14;
	
	// Parameters
	private double T, W, M, A;
	private double a, b, c, f;
	private double w, x0, x1, x2;
	private double inverse_x0;

	private double xTaylor;
	private double[] taylor = new double[TAYLOR_LENGTH];

	private int bins;

    /**
     * Constructor.
     * 
     * Default values for M = 4.5, A = 0 and bins = 0.
     * 
     * @param T top of scale value.
     * @param W number of decades in the approximately linear region.
     * @throws Exception for invalid input arguments. 
     */
	public Hyperlog(double T, double W) throws Exception {
		
		this.T = T;
		this.M = DEFAULT_DECADES;
		this.W = W;
		this.A = 0;
		this.bins = 0;
		
		// Initialize
		initialize();
	}

    /**
     * Alternative constructor.
     * 
     * Default value bins = 0.
     *
     * @param T top of scale value.
     * @param M number of decades that the true logarithmic scale approached
     *          at the high end of the Hyperlog scale would cover in the plot
     *          range.
     * @param W the number of such decades in the approximately linear region.
     * @param A number of additional decades of negative data values to be 
     *          included.
     * @throws Exception for invalid input arguments. 
     */
	public Hyperlog(double T, double W, double M, double A) throws Exception {
		
		this.T = T;
		this.M = M;
		this.W = W;
		this.A = A;
		this.bins = 0;
		
		// Initialize
		initialize();
	}

    /**
     * Alternative constructor.
     * @param T top of scale value.
     * @param M number of decades that the true logarithmic scale approached
     *          at the high end of the Hyperlog scale would cover in the plot
     *          range.
     * @param W the number of such decades in the approximately linear region.
     * @param A number of additional decades of negative data values to be 
     *          included.
     * @param bins number of bins for the data
     * @throws Exception for invalid input arguments.
     */
	public Hyperlog(double T, double W, double M, double A, int bins) throws Exception {
		
		this.T = T;
		this.M = M;
		this.W = W;
		this.A = A;
		this.bins = bins;

		// Initialize
		initialize();
	}

	/**
	 * Hyperlog forward transform.
	 * @param x array of values to be transformed.
	 * @return array of transformed values.
	 */
	public double[] transform(double[] x) {

		double[] y = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			try {
				y[i] = scale(x[i]);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return y;
	}

    /**
	 * Hyperlog inverse transform
	 * 
	 * @param x array of values to be inverse-transformed.
	 * @return array of inverse-transformed values.
	 */
	public double[] inverseTransform(double[] x) {

		double[] y = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			try {
				y[i] = inverse(x[i]);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		return y;
	}
	 
	/**
	 * Initialize the Hyperlog parameters.
	 * @throws Exception if any of the parameters are invalid.
	 */
	private void initialize() throws Exception {
		
	    if (T <= 0) {
	        throw new Exception("IllegalParameter: T is not positive");
	    }
	    
	    if (W < 0){
	        throw new Exception("IllegalParameter: W is negative");
	    }
	    
	    if (W <= 0) {
	        throw new Exception("IllegalParameter: W is not positive");
	    }
	    
	    if (M <= 0) {
	        throw new Exception("IllegalParameter: M is not positive");
	    }
	    
	    if (2 * W > M) {
	    	throw new Exception("IllegalParameter: W is too large");
	    }
	    
	    if (-A > W || A + W > M - W) {
	       throw new Exception("IllegalParameter: A is too large");
	    }
	    
	    // If we're going to bin the data make sure that
	    // zero is on a bin boundary by adjusting A
	    if (bins > 0)
	    {
	        double zero = (W + A) / (M + A);
	        zero = Math.floor(zero * bins + 0.5) / bins;
	        A = (M * zero - W) / (1 - zero);
	    }

	    // Actual parameters
	    // Choose the data zero location and the width of the linearization 
	    // region to match the corresponding logicle scale
	    w = W / (M + A);
	    x2 = A / (M + A);
	    x1 = x2 + w;
	    x0 = x2 + 2 * w;
	    b = (M + A) * LN_10;
	    double e2bx0 = Math.exp(b * x0);
	    double c_a = e2bx0 / w;
	    double f_a = Math.exp(b * x1) + c_a * x1;

	    a = T / ((Math.exp(b) + c_a) - f_a);
	    c = c_a * a;
	    f = f_a * a;

	    // Use Taylor series near x1, i.e., data zero to
	    // avoid round off problems of formal definition
	    xTaylor = x1 + w / 4;

	    // Compute coefficients of the Taylor series
	    double coef = a * Math.exp(b * x1);
	    
	    // 16 is enough for full precision of typical scales
	    taylor = new double[TAYLOR_LENGTH];
	    for (int i = 0; i < TAYLOR_LENGTH; ++i)
	    {
	        coef *= b / (i + 1);
	        taylor[i] = coef;
	    }
	    taylor[0] += c; // hyperlog condition
	    inverse_x0 = inverse(x0);
	}
	
	/**
	 * Calculate the inverse.
	 * @param scale Data scale.
	 * @return the inverse.
	 */
	private double inverse(double scale) {

		// Reflect negative scale regions
	    boolean negative = scale < x1;
	    if (negative) {
	        scale = 2 * x1 - scale;
	    }
	    
	    double inverse;
	    if (scale < xTaylor) {
	        // Near x1, i.e., data zero use the series expansion
	        inverse = taylorSeries(scale);
	    } else {
	        // This formulation has better roundoff behavior
	        inverse = (a * Math.exp(b * scale) + c * scale) - f;
	    }
	    
	    // Handle scale for negative values
	    if (negative) {
	        return -inverse;
	    } else {
	        return inverse;
	    }
	}

	/**
	 * Scale
	 * @param value
	 * @return scaled value
	 * @throws Exception if no convergence.
	 */
	private double scale(double value) throws Exception {
	    
		// Handle true zero separately
	    if (value == 0) {
	        return x1;
	    }
	    
	    // Reflect negative values
	    boolean negative = value < 0;
	    if (negative) {
	        value = -value;
	    }
	    
	    // Initial guess at solution
	    double x;
	    if (value < inverse_x0) {

	    	// Use linear approximation in the quasi linear region
	        x = x1 + value * w / inverse_x0;
	        
	    } else {
	        
	    	// Otherwise use ordinary logarithm
	        x = Math.log(value / a) / b;
	    
	    }
	    
	    // Try for double precision unless in extended range
	    double tolerance = 3 * EPSILON;
	    if (x > 1) {
	        tolerance = 3 * x * EPSILON;
	    }

	    for (int i = 0; i < 10; ++i) {

	    	// Compute the function and its first two derivatives
	        double ae2bx = a * Math.exp(b * x);
	        //double ce2mdx = p->c / exp(p->d * x);
	        double y;
	        if (x < xTaylor) {
	            
	        	// Near zero use the Taylor series
	            y = taylorSeries(x) - value;
	        
	        } else {
	            
	        	// This formulation has better roundoff behavior
	            y = (ae2bx + c * x) - (f + value);
	        
	        }
	        
	        double abe2bx = b * ae2bx;
	        double dy = abe2bx + c;
	        double ddy = b * abe2bx;

	        // This is Halley's method with cubic convergence
	        double delta = y / (dy * (1 - y * ddy / (2 * dy * dy)));
	        x -= delta;

	        // if we've reached the desired precision we're done
	        if (Math.abs(delta) < tolerance) {

	        	// Handle negative arguments
	            if (negative) {
	                return 2 * x1 - x;
	            } else {
	                return x;
	            }
	        }
	    }

	    throw new Exception("DidNotConverge: scale() didn't converge");
	}

	/**
	 * Compute the slope of the bi-exponential.
	 * @param scale Data scale.
	 * @return slope of the bi-exponential.
	 */
	public double slope(double scale) {
	    
		// Reflect negative scale regions
	    if (scale < x1) {
	        scale = 2 * x1 - scale;
	    }
	    
	    // Compute the slope of the bi-exponential
	    return (a * b * Math.exp(b * scale) + c);
	}
	
	/**
	 * Calculate Taylor series.
	 * @param scale Data scale.
	 * @return Taylor series (sum).
	 */
	private double taylorSeries (double scale) {
	    
		// Taylor series is around x1
	    double x = scale - x1;
	    double sum = taylor[TAYLOR_LENGTH - 1] * x;
		for (int i = TAYLOR_LENGTH - 2; i >= 0; --i) {
			sum = (sum + taylor[i]) * x;
		}
		return sum;
	}
	
	/**
	 * Estimate parameters for the Hyperlog transform from the min and max
	 * values of the array to be transformed.
	 * @param mn min value of the data to be transformed.
	 * @param mx max value of the data to be transformed. 
	 * @return array of parameters [T, M, W, A] (see description above).
	 */
	public static double[] estimateParamHeuristic(double mn, double mx) {
		
        double T = mx;
        double M = Math.log10(T);
        double W = M/10;
        double A;
        
        if (mn > -10.0) {
        	A = 0;
        } else {
        	A = Math.log10(Math.abs(mn));
        }

        double[] params = {T, W, M, A};
        
        return params;
	}
	
	/**
	 * Estimate parameters for the Hyperlog transform from the min and max
	 * values of the array to be transformed.
	 * @param x array of values for which the parameters are to be estimated.
	 * @return array of parameters [T, M, W, A] (see description above).
	 */
	public static double[] estimateParamHeuristic(double[] x) {
		
		// Extact min and max values
		double[] bnds = Hyperlog.bounds(x);
		
		// Estimate the parameters
		return Hyperlog.estimateParamHeuristic(bnds[0], bnds[1]);
	}	
	/**
	 * Multiplies all values in the input array by a constant factor.
	 * 
	 * This is used to bring back the top of the scale of the transformed
	 * array to the same value. If x was the original linear scale that
	 * was transformed by the Hyperlog, f would be max(x). 
	 * 
	 * @param y Array of (transformed) values to be scaled.
	 * @param f Constant factor.
	 * @return Array of values each scaled by the factor f.
	 */
	public static double[] arrayMult(double[] y, double f) {
	
		double[] z = new double[y.length];
		for (int i = 0; i < y.length; i++) {
			z[i] = f * y[i];
		}
		return z;
	}
	
	/**
	 * Return the max value of the array.
	 * @param x Array of values.
	 * @return max value of the array.
	 */
	public static double max(double[] x) {
		double mx = x[0];
		for (int i = 1; i < x.length; i++) {
			if (x[i] > mx) {
				mx = x[i];
			}
		}
		return mx;
	}

	/**
	 * Return the min value of the array.
	 * @param x Array of values.
	 * @return min value of the array.
	 */
	public static double min(double[] x) {
		double mn = x[0];
		for (int i = 1; i < x.length; i++) {
			if (x[i] < mn) {
				mn = x[i];
			}
		}
		return mn;
	}

	/**
	 * Return the bounds (min and max value) of the array.
	 * @param x Array of values.
	 * @return array of [min, max] values of the array.
	 */
	public static double[] bounds(double[] x) {
		double mn = x[0];
		double mx = x[0];
		double[] extrema = {mn, mx}; 
		for (int i = 1; i < x.length; i++) {
			if (x[i] < mn) {
				mn = x[i];
			}
			if (x[i] > mx) {
				mx = x[i];
			}
		}
		extrema[0] = mn;
		extrema[1] = mx;
		return extrema;
	}
	
	
	/**
	 * Return the max value of a matrix per column.
	 * @param M Matrix of values.
	 * @return array of max values per column.
	 */
	public static double[] max(double[][] M) {
		double[] mx = {M[0][0], M[0][1]};
		for (int i = 1; i < M.length; i++) {
			if (M[i][0] > mx[0]) {
				mx[0] = M[i][0];
			}
			if (M[i][1] > mx[1]) {
				mx[1] = M[i][1];
			}
		}
		return mx;
	}

	/**
	 * Return the min value of a matrix per column.
	 * @param M Matrix of values.
	 * @return array of min values per column.
	 */
	public static double[] min(double[][] M) {
		double[] mn = {M[0][0], M[0][1]};
		for (int i = 1; i < M.length; i++) {
			if (M[i][0] < mn[0]) {
				mn[0] = M[i][0];
			}
			if (M[i][1] < mn[1]) {
				mn[1] = M[i][1];
			}
		}
		return mn;
	}

	/**
	 * Test routine
	 * @param x array of values to be transformed
	 * @param y_exp array of expected transformation values
	 * @param T top of scale
	 * @param W decades at the top of the scale
	 * @param M decades in the linear range
	 * @param A number of decades in the negative range
	 * @param testName name of the test
	 * @param tol tolerance of the test
	 * @throws Exception if the Hyperlog parameters are not valid.
	 */
	private static void test(double[] x, double[] y_exp, double T, double W, 
			double M, double A, String testName, double tol) throws Exception {
		
		Hyperlog H = new Hyperlog(T, W, M, A);
		
		double[] y = H.transform(x);

		System.out.print(testName + ": Forward transform => [");
		for (int i = 0; i < y.length; i++) {
			System.out.print(new DecimalFormat("#0.000000").format(y[i]) + " ");
			if (Math.abs(y[i] - y_exp[i]) > tol) {
				System.err.println(" => FAILED!");
				return;
			}
		}
		System.out.println("] => PASSED!");
		
		double[] z = H.inverseTransform(y);
		
		System.out.print(testName + ": Inverse transform => [");
		for (int i = 0; i < z.length; i++) {
			System.out.print(new DecimalFormat("#0.000000").format(z[i]) + " ");
			if (Math.abs(z[i] - x[i]) > tol) {
				System.err.println(" => FAILED!");
				return;
			}
		}
		System.out.println("] => PASSED!");
	}
	
	/** 
	 * Program entry point.
	 * @param args Program arguments
	 * @throws Exception if the transform fails.
	 */
	public static void main(String[] args) throws Exception {
		
		double tol = 1e-6;
		double T, W, M, A;
		
		// Input array
		double[] x = {-10.0, -5.0, -1.0, 0.0, 0.3, 1.0, 3.0, 10.0, 100.0, 1000.0};

		// Test min, max and bounds

		double mn = min(x);
		System.out.print("Min = " + mn);	
		if (Math.abs(mn + 10.0) > tol) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");
		
		double mx = max(x);
		System.out.print("Max = " + mx);	
		if (Math.abs(mx - 1000.0) > tol) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");
		
		double[] bds = bounds(x);
		System.out.print("Min bound = " + bds[0]);	
		if (Math.abs(bds[0] + 10.0) > tol) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");
		System.out.print("Max bound = " + bds[1]);	
		if (Math.abs(bds[1] - 1000.0) > tol) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");

		// Test estimate parameters
		double[] bnds = bounds(x);
		double[] params = estimateParamHeuristic(bnds[0], bnds[1]);
		System.out.print("Parameters from bounds: T =  " + params[0] + 
				", W = " + params[1] + ", M = " + params[2] +
				", A = " + params[3]);
		if ((Math.abs(params[0] - 1000.0) > tol) ||
				(Math.abs(params[1] - 0.3) > tol) ||
				(Math.abs(params[2] - 3.0) > tol) ||
				(Math.abs(params[3] - 1.0) > tol)) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");

		double[] paramsA = estimateParamHeuristic(x);
		System.out.print("Parameters from bounds: T =  " + paramsA[0] + 
				", W = " + paramsA[1] + ", M = " + paramsA[2] +
				", A = " + paramsA[3]);
		if ((Math.abs(paramsA[0] - 1000.0) > tol) ||
				(Math.abs(paramsA[1] - 0.3) > tol) ||
				(Math.abs(paramsA[2] - 3.0) > tol) ||
				(Math.abs(paramsA[3] - 1.0) > tol)) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");

		// Test transforms and inverse transforms

		// Test 1
		T = 1000.0; W = 1.0; M = 4.0; A = 0.0;
		double[] y1_exp = {0.083554, 0.155868, 0.229477, 0.250000, 0.256239, 
				0.270523, 0.309091, 0.416446, 0.731875, 1.000000};
		test(x, y1_exp, T, W, M, A, "Test 1", tol); 

		// Test 2
		T = 1000.0; W = 1.0; M = 4.0; A = 1.0;
		double[] y2_exp = {0.266843, 0.324695, 0.383581, 0.400000, 0.404991,
				0.416419, 0.447273, 0.533157, 0.7855, 1.000000};
		test(x, y2_exp, T, W, M, A, "Test 2", tol); 

		// Test 3
		T = 1000.0; W = 0.01; M = 4.0; A = 1.0;
		double[] y3_exp = {0.017447, 0.106439, 0.182593, 0.202000, 0.207833,
				0.221407, 0.259838, 0.386553, 0.774211, 1.000000}; 
		test(x, y3_exp, T, W, M, A, "Test 3", tol); 

		// Test 4
		T = max(x); W = 0.01; M = 4.0; A = 1.0;
		double[] y4_exp = {0.017447, 0.106439, 0.182593, 0.202000, 0.207833,
				0.221407, 0.259838, 0.386553, 0.774211, 1.000000}; 
		test(x, y4_exp, T, W, M, A, "Test 4", tol); 

		// Test 5
		double[][] N = new double[10][2];
		N[2][0] = 5;
		N[7][1] = 2;
		double[] dmx = max(N);
		System.out.print("max x = " + dmx[0] + "; max y = " + dmx[1]);
		if ((Math.abs(dmx[0] - 5.0) > tol) || (Math.abs(dmx[1] - 2.0) > tol)) {
			System.err.println(" => FAILED!");
			return;
		}
		System.out.println(" => PASSED!");
	}
}
