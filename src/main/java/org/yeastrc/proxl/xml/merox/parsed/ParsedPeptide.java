package org.yeastrc.proxl.xml.merox.parsed;

import java.util.Collection;

/**
 * A peptide parsed from a merox representation of a peptide.
 * It contains the unmodified peptide sequence, and a collection 
 * of associated variable modifications
 * @author Valued Customer
 *
 */
public class ParsedPeptide {
	
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public Collection<ParsedPeptideModification> getMods() {
		return mods;
	}
	public void setMods(Collection<ParsedPeptideModification> mods) {
		this.mods = mods;
	}
	public Integer getLinkedPosition1() {
		return linkedPosition1;
	}
	public void setLinkedPosition1(Integer linkedPosition1) {
		this.linkedPosition1 = linkedPosition1;
	}
	public Integer getLinkedPosition2() {
		return linkedPosition2;
	}
	public void setLinkedPosition2(Integer linkedPosition2) {
		this.linkedPosition2 = linkedPosition2;
	}



	private String sequence;
	private Collection<ParsedPeptideModification> mods;
	private Integer linkedPosition1;
	private Integer linkedPosition2;
	
}
