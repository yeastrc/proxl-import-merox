package org.yeastrc.proxl.xml.merox.objects;

import java.util.Collection;

public class LinkablePosition {

    public Collection<String> getResidues() {
        return residues;
    }

    public void setResidues(Collection<String> residues) {
        this.residues = residues;
    }

    public boolean isProteinNTerminus() {
        return proteinNTerminus;
    }

    public void setProteinNTerminus(boolean proteinNTerminus) {
        this.proteinNTerminus = proteinNTerminus;
    }

    public boolean isProteinCTerminus() {
        return proteinCTerminus;
    }

    public void setProteinCTerminus(boolean proteinCTerminus) {
        this.proteinCTerminus = proteinCTerminus;
    }

    private Collection<String> residues;
    private boolean proteinNTerminus;
    private boolean proteinCTerminus;

}
