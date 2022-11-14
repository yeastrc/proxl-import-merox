package org.yeastrc.proxl.xml.merox.parsed;

import java.util.ArrayList;
import java.util.Collection;

import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;
import org.yeastrc.proxl.xml.merox.objects.Result;

/**
 * In proxl, a "reported peptide" is essentially the string that uniquely identifies
 * a pair of linked proteins, including the positions at which they are respectively
 * linked and any variable mods in the peptides. This class associates the parsed
 * peptides (the actual sequence and mods of the two linked peptides) and any results
 * from the Results.csv file that describe this reported peptide (i.e., PSMs that map
 * to this reported peptide).
 * 
 * @author Valued Customer
 *
 */
public class ParsedReportedPeptide {

	public String toString() {

		if( this.getType() == MeroxConstants.PSM_TYPE_MONOLINK ) {
			return this.getPeptides().get(0).toString();
		} else if( this.getType() == MeroxConstants.PSM_TYPE_LOOPLINK ) {
			return this.getPeptides().get(0).toString() + "(" + this.getPeptides().get(0).getLinkedPosition1() + "," + this.getPeptides().get(0).getLinkedPosition2() + ")";
		} else if( this.getType() == MeroxConstants.PSM_TYPE_CROSSLINK ) {
			return this.getPeptides().get(0).toString() + "(" + this.getPeptides().get(0).getLinkedPosition1() + ")" + "-" +
					this.getPeptides().get(1).toString() + "(" + this.getPeptides().get(1).getLinkedPosition1() + ")";
		}

		return "Error: unknown peptide type";
	}

	@Override
	public int hashCode() {
		return this.getReportedPeptideString().hashCode();
	}
	
	@Override
	public boolean equals( Object o ) {
		if( o == null ) return false;
		if( !( o instanceof ParsedReportedPeptide ) ) return false;
		
		final ParsedReportedPeptide op = (ParsedReportedPeptide)o;
		return this.getReportedPeptideString().equals( op.getReportedPeptideString() );		
	}

	public String getReportedPeptideString() {
		return this.toString();
	}

	public ArrayList<ParsedPeptide> getPeptides() {
		return peptides;
	}
	public void setPeptides(ArrayList<ParsedPeptide> peptides) {
		this.peptides = peptides;
	}
	public Collection<Result> getResults() {
		return results;
	}
	public void setResults(Collection<Result> results) {
		this.results = results;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	private ArrayList<ParsedPeptide> peptides;
	private Collection<Result> results;
	private int type;
	
}
