package org.pconrad.webapps.sparkjava;

/**
 * Temperature Conversion Class
 *
 * @author Phill Conrad
 */
public class TempConversion {

    public static double ctof(double fTemp) {
	return (fTemp * 9.0 / 5.0) + 32.0;
    }
}
