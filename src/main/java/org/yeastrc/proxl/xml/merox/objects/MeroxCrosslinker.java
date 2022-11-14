package org.yeastrc.proxl.xml.merox.objects;

import java.math.BigDecimal;
import java.util.Collection;

public class MeroxCrosslinker {

	private String name;
	private BigDecimal spacerArmLength;
	private Molecule fullLengthMolecule;
	private Collection<Molecule> cleavedMolecules;
	private Collection<BigDecimal> monolinkMonoisotopicMasses;
	private LinkablePosition linkablePosition1;
	private LinkablePosition linkablePosition2;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getSpacerArmLength() {
		return spacerArmLength;
	}

	public void setSpacerArmLength(BigDecimal spacerArmLength) {
		this.spacerArmLength = spacerArmLength;
	}

	public Molecule getFullLengthMolecule() {
		return fullLengthMolecule;
	}

	public void setFullLengthMolecule(Molecule fullLengthMolecule) {
		this.fullLengthMolecule = fullLengthMolecule;
	}

	public Collection<Molecule> getCleavedMolecules() {
		return cleavedMolecules;
	}

	public void setCleavedMolecules(Collection<Molecule> cleavedMolecules) {
		this.cleavedMolecules = cleavedMolecules;
	}

	public Collection<BigDecimal> getMonolinkMonoisotopicMasses() {
		return monolinkMonoisotopicMasses;
	}

	public void setMonolinkMonoisotopicMasses(Collection<BigDecimal> monolinkMonoisotopicMasses) {
		this.monolinkMonoisotopicMasses = monolinkMonoisotopicMasses;
	}

	public LinkablePosition getLinkablePosition1() {
		return linkablePosition1;
	}

	public void setLinkablePosition1(LinkablePosition linkablePosition1) {
		this.linkablePosition1 = linkablePosition1;
	}

	public LinkablePosition getLinkablePosition2() {
		return linkablePosition2;
	}

	public void setLinkablePosition2(LinkablePosition linkablePosition2) {
		this.linkablePosition2 = linkablePosition2;
	}
}
