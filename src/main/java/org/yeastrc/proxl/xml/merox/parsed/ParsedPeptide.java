package org.yeastrc.proxl.xml.merox.parsed;

import org.apache.commons.lang3.StringUtils;
import org.yeastrc.proxl.xml.merox.utils.ModUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * A peptide parsed from a merox representation of a peptide.
 * It contains the unmodified peptide sequence, and a collection 
 * of associated variable modifications
 * @author Valued Customer
 *
 */
public class ParsedPeptide {

	public boolean equals( Object o ) {

		if( !( o instanceof ParsedPeptide) )
			return false;

		return this.toString().equals( ((ParsedPeptide)o).toString() );
	}

	public int hashCode() {
		return this.toString().hashCode();
	}
	public String toString() {

		String str = "";
		Map<Integer, Collection<Double>> modMap = ModUtils.convertModCollectionToMap(this.getMods());

		for( int i = 1; i <= this.getSequence().length(); i++ ) {
			String r = String.valueOf( this.getSequence().charAt( i - 1 ) );
			str += r;

			List<String> modsAtPosition = new ArrayList<String>();

			if( modMap.get( i ) != null ) {
				for( Double modMass : modMap.get( i ) ) {
					modsAtPosition.add( BigDecimal.valueOf(modMass).setScale( 2, BigDecimal.ROUND_HALF_UP ).toString() );
				}

				if( modsAtPosition.size() > 0 ) {

					// sort these strings on double values
					Collections.sort( modsAtPosition, new Comparator<String>() {
						public int compare(String s1, String s2) {
							return Double.valueOf( s1 ).compareTo( Double.valueOf( s2 ) );
						}
					});

					String modsString = StringUtils.join( modsAtPosition, "," );
					str += "[" + modsString + "]";
				}
			}
		}

		if(this.is15N) {
			str += "-15N";
		}

		return str;
	}

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

	public boolean isIs15N() {
		return is15N;
	}

	public void setIs15N(boolean is15N) {
		this.is15N = is15N;
	}

	private boolean is15N = false;
	private String sequence;
	private Collection<ParsedPeptideModification> mods;
	private Integer linkedPosition1;
	private Integer linkedPosition2;
	
}
