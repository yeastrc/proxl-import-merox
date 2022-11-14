package org.yeastrc.proxl.xml.merox.utils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MassUtilsTest {

    private Map<String, Double> elementsDefinition;


    @Before
    public void setUp() {
        elementsDefinition = new HashMap<>();

        elementsDefinition.put("A", 1.0);
        elementsDefinition.put("B", 1.25);
        elementsDefinition.put("C", 3.75);
        elementsDefinition.put("D", 9.0);
        elementsDefinition.put("Ax", 12.25);
        elementsDefinition.put("Zh", 20.5);
    }

    @Test(expected = Exception.class)
    public void getMassFromFormulaUsingElements_EmptyFormula() throws Exception {
        String formula = "";
        MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void getMassFromFormulaUsingElements_NullFormula() throws Exception {
        String formula = null;
        MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void getMassFromFormulaUsingElements_NullElements() throws Exception {
        String formula = "AB6Ax";
        MassUtils.getMassFromFormulaUsingElements(formula, null);
    }

    @Test(expected = Exception.class)
    public void getMassFromFormulaUsingElements_InvalidElement() throws Exception {
        String formula = "AB6ZzAx";
        MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
    }

    @Test(expected = Exception.class)
    public void getMassFromFormulaUsingElements_MultipleNegatives() throws Exception {
        String formula = "AB6-Zz-Ax";
        MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
    }

    @Test
    public void getMassFromFormulaUsingElements_OneSingleLetterElement() throws Exception {
        String formula = "B";
        double mass = MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
        assertEquals(1.25, mass, 0.0001);
    }

    @Test
    public void getMassFromFormulaUsingElements_NegativeOneSingleLetterElement() throws Exception {
        String formula = "-B";
        double mass = MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
        assertEquals(-1.25, mass, 0.0001);
    }

    @Test
    public void getMassFromFormulaUsingElements_NegatingAtom() throws Exception {
        String formula = "B-B";
        double mass = MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
        assertEquals(0, mass, 0.0001);
    }

    @Test
    public void getMassFromFormulaUsingElements_MultipleAtoms() throws Exception {
        String formula = "AB3Ax2C";
        double mass = MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
        assertEquals(33.0, mass, 0.0001);
    }

    @Test
    public void getMassFromFormulaUsingElements_NegativeMultipleAtoms() throws Exception {
        String formula = "-A2B3Ax2C";
        double mass = MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
        assertEquals(-34.0, mass, 0.0001);
    }

    @Test
    public void getMassFromFormulaUsingElements_PositiveAndNegativeMultipleAtoms() throws Exception {
        String formula = "A2B3Ax2C-ZhD3";
        double mass = MassUtils.getMassFromFormulaUsingElements(formula, elementsDefinition);
        assertEquals(-13.5, mass, 0.0001);
    }
}