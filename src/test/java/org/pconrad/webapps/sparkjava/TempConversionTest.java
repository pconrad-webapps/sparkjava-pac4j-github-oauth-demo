package org.pconrad.webapps.sparkjava;

import static org.junit.Assert.assertEquals;
import static org.pconrad.webapps.sparkjava.TempConversion.ctof;

import org.junit.Test;

public class TempConversionTest {

    @Test
    public void ctof_test_20() { assertEquals(68.0, ctof(20.0), 0.01); }
    
    @Test
    public void ctof_test_m40() { assertEquals(-40.0, ctof(-40.0), 0.01); }

    @Test
    public void ctof_test_100() { assertEquals(212.0, ctof(100.0), 0.01); }
    
    @Test
    public void ctof_test_m11() { assertEquals(12.2, ctof(-11.0), 0.01); }
    
    @Test
    public void ctof_test_0() { assertEquals(32.0, ctof(0.0), 0.01); }


}
