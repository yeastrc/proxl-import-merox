package org.yeastrc.proxl.xml.merox.parsed;

/**
 * Represents a position and a mass modification, to be associated as
 * part of a collection of parsed mods with a parsed peptide
 * @author Valued Customer
 *
 */
public class ParsedPeptideModification {
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	public boolean isMonolink() {
		return monolink;
	}
	public void setMonolink(boolean monolink) {
		this.monolink = monolink;
	}

	private int position;
	private double mass;
	private boolean monolink = false;
}
