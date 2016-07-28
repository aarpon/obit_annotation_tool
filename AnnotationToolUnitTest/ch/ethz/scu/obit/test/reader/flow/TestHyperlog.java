package ch.ethz.scu.obit.test.reader.flow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ch.ethz.scu.obit.bdfacsdivafcs.readers.Hyperlog;

/**
 * Test flow cytometry readers and processors.
 * @author Aaron Ponti
 *
 */
public class TestHyperlog {

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
    private static boolean test(double[] x, double[] y_exp, double T, double W, 
            double M, double A, String testName, double tol) throws Exception {
        
        Hyperlog H = new Hyperlog(T, W, M, A);
        
        // Forward transform
        double[] y = H.transform(x);
        
        for (int i = 0; i < y.length; i++) {
            if (Math.abs(y[i] - y_exp[i]) > tol) {
                return false;
            }
        }

        // Inverse transform
        double[] z = H.inverseTransform(y);
        
        for (int i = 0; i < z.length; i++) {
            if (Math.abs(z[i] - x[i]) > tol) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Test various Hyperlog transforms
     * @throws Exception If the Hyperlog transform throws an Exception.
     */
    @Test
    public void testHyperlogTransform() throws Exception {

        double tol = 1e-6;
        double T, W, M, A;
        
        // Input array
        double[] x = {-10.0, -5.0, -1.0, 0.0, 0.3, 1.0, 3.0, 10.0, 100.0, 1000.0};

        // Test min, max and bounds

        // The minimum is 10.0 (with tolerance)
        double mn = Hyperlog.min(x);
        assertEquals(Math.abs(mn + 10.0) < tol, true);

        // The maximum is 1000.0 (with tolerance)
        double mx = Hyperlog.max(x);
        assertEquals(Math.abs(mx - 1000.0) < tol, true);

        // Min bound is -10 and max bound is 1000 
        double[] bds = Hyperlog.bounds(x);
        assertEquals(Math.abs(bds[0] + 10.0) < tol, true);
        assertEquals(Math.abs(bds[1] - 1000.0) < tol, true);

        // Test estimate parameters
        double[] bnds = Hyperlog.bounds(x);
        double[] params = Hyperlog.estimateParamHeuristic(bnds[0], bnds[1]);
        boolean testP1 = (Math.abs(params[0] - 1000.0) < tol) &&
                (Math.abs(params[1] - 0.3) < tol) &&
                (Math.abs(params[2] - 3.0) < tol) &&
                (Math.abs(params[3] - 1.0) < tol);
        assertEquals(testP1, true);

        double[] paramsA = Hyperlog.estimateParamHeuristic(x);
        boolean testP2 = (Math.abs(paramsA[0] - 1000.0) < tol) &&
                (Math.abs(paramsA[1] - 0.3) < tol) &&
                (Math.abs(paramsA[2] - 3.0) < tol) &&
                (Math.abs(paramsA[3] - 1.0) < tol);
        assertEquals(testP2, true);

        // Test transforms and inverse transforms

        // Test 1
        T = 1000.0; W = 1.0; M = 4.0; A = 0.0;
        double[] y1_exp = {0.083554, 0.155868, 0.229477, 0.250000, 0.256239, 
                0.270523, 0.309091, 0.416446, 0.731875, 1.000000};
        assertEquals(test(x, y1_exp, T, W, M, A, "Test 1", tol), true); 

        // Test 2
        T = 1000.0; W = 1.0; M = 4.0; A = 1.0;
        double[] y2_exp = {0.266843, 0.324695, 0.383581, 0.400000, 0.404991,
                0.416419, 0.447273, 0.533157, 0.7855, 1.000000};
        assertEquals(test(x, y2_exp, T, W, M, A, "Test 2", tol), true); 

        // Test 3
        T = 1000.0; W = 0.01; M = 4.0; A = 1.0;
        double[] y3_exp = {0.017447, 0.106439, 0.182593, 0.202000, 0.207833,
                0.221407, 0.259838, 0.386553, 0.774211, 1.000000}; 
        assertEquals(test(x, y3_exp, T, W, M, A, "Test 3", tol), true); 

        // Test 4
        T = Hyperlog.max(x); W = 0.01; M = 4.0; A = 1.0;
        double[] y4_exp = {0.017447, 0.106439, 0.182593, 0.202000, 0.207833,
                0.221407, 0.259838, 0.386553, 0.774211, 1.000000}; 
        assertEquals(test(x, y4_exp, T, W, M, A, "Test 4", tol), true); 

        // Test 5
        double[][] N = new double[10][2];
        N[2][0] = 5;
        N[7][1] = 2;
        double[] dmx = Hyperlog.max(N);
        boolean testM = (Math.abs(dmx[0] - 5.0) < tol) && (Math.abs(dmx[1] - 2.0) < tol); 
        assertEquals(testM, true);
    }
    
    /** 
     * Entry point
     * @param args Ignored.
     */   
    public static void main(String[] args) {

        Result result = JUnitCore.runClasses(TestHyperlog.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }

    }

}
