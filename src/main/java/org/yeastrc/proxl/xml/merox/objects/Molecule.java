package org.yeastrc.proxl.xml.merox.objects;

import java.util.Objects;

public class Molecule {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Molecule molecule = (Molecule) o;
        return formula.equals(molecule.formula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula);
    }

    public Molecule(Double monoisotopicMass, String formula) {
        this.monoisotopicMass = monoisotopicMass;
        this.formula = formula;
    }

    public Double getMonoisotopicMass() {
        return monoisotopicMass;
    }

    public String getFormula() {
        return formula;
    }

    private Double monoisotopicMass;
    private String formula;
}
