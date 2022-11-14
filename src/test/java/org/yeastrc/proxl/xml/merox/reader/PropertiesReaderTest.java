package org.yeastrc.proxl.xml.merox.reader;

import org.junit.Before;
import org.junit.Test;
import java.io.InputStream;
import static org.junit.Assert.*;

public class PropertiesReaderTest {

    private static InputStream propertiesInputStream;

    @Before
    public static void setUpBefore() {
        propertiesInputStream = PropertiesReaderTest.class.getResourceAsStream("/example_data/properties.mxf");
    }

    @Test
    public void getCrosslinkerFromProperties() {
    }

    @Test
    public void getCrosslinkerNameFromProperties() {
    }

    @Test
    public void getCrosslinkerForNameFromProperties() {
    }

    @Test
    public void getCrosslinkerFromStringArray() {
    }

    @Test
    public void getAnalysisProperties() {
    }
}