package org.yeastrc.proxl.xml.merox.reader;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;

/**
 * A line from the Results.csv file in the zipped merox results file.
 * @author Valued Customer
 *
 */
public class Result {	
	
	/**
	 * Get a "reported peptide" string that unique identifies the crosslinked
	 * pair of peptides in this result, including positions linked in those
	 * peptides and any variable mods in those peptides.
	 * 
	 * @return
	 */
	public String getReportedPeptideString() {
		if( this.getPsmType() == MeroxConstants.PSM_TYPE_CROSSLINK )
			return this.getPeptide1() + "(" + this.getPosition1String() + ")--" + this.getPeptide2() + "(" + this.getPosition2String() + ")";
		
		if( this.getPsmType() == MeroxConstants.PSM_TYPE_LOOPLINK )
			return this.getPeptide1() + "(" + this.getPosition1String() + "," + this.getPosition2String() + ")";
		
		return this.getPeptide1() + "(" + this.getPosition1String() + ")";
	}
	
	/**
	 * Get the type (crosslink, looplink, or monolink) for this psm. Looked up in
	 * MeroxConstants
	 * @return
	 */
	public int getPsmType() {
		if( this.getPeptide2().equals( "0" ) ) return MeroxConstants.PSM_TYPE_MONOLINK;
		if( this.getPeptide2().equals( "1" ) ) return MeroxConstants.PSM_TYPE_LOOPLINK;
		
		return MeroxConstants.PSM_TYPE_CROSSLINK;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public double getMoverz() {
		return moverz;
	}
	public void setMoverz(double moverz) {
		this.moverz = moverz;
	}
	public int getCharge() {
		return charge;
	}
	public void setCharge(int charge) {
		this.charge = charge;
	}
	public double getObservedMass() {
		return observedMass;
	}
	public void setObservedMass(double observedMass) {
		this.observedMass = observedMass;
	}
	public double getCandidateMass() {
		return candidateMass;
	}
	public void setCandidateMass(double candidateMass) {
		this.candidateMass = candidateMass;
	}
	public double getDeviation() {
		return deviation;
	}
	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}
	public String getPeptide1() {
		return peptide1;
	}
	public void setPeptide1(String peptide1) {
		this.peptide1 = peptide1;
	}
	public String getPeptide2() {
		return peptide2;
	}
	public void setPeptide2(String peptide2) {
		this.peptide2 = peptide2;
	}
	public int getScanNumber() {
		return scanNumber;
	}
	public void setScanNumber(int scanNumber) {
		this.scanNumber = scanNumber;
	}
	public MeroxCrosslinker getLinker() {
		return linker;
	}
	public void setLinker(MeroxCrosslinker linker) {
		this.linker = linker;
	}
	public String getPosition1String() {
		return position1String;
	}
	public void setPosition1String(String position1String) {
		this.position1String = position1String;
	}
	public String getPosition2String() {
		return position2String;
	}
	public void setPosition2String(String position2String) {
		this.position2String = position2String;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}



	private int score;
	private double moverz;
	private int charge;
	private double observedMass;
	private double candidateMass;
	private double deviation;
	private String peptide1;
	private String peptide2;
	private int scanNumber;
	private String position1String;
	private String position2String;
	private MeroxCrosslinker linker;
	private int rank;
	
}
