package ch.ethz.scu.obit.test.os;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ch.ethz.scu.obit.common.utils.QueryOS;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

public class TestOSQuery {

	@Test
	public void testGetHostname() {

		boolean found;

		try {
			String hostname = QueryOS.getHostName();
			System.out.println("Retrieved hostname: " + hostname);
			found = true;
		} catch (UnknownHostException e) {
			found = false;
		}
		assertEquals(found, true);
	}

	/** 
	 * Entry point
	 * @param args Ignored.
	 */
	public static void main(String[] args) {

		Result result = JUnitCore.runClasses(TestOSQuery.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}
}
