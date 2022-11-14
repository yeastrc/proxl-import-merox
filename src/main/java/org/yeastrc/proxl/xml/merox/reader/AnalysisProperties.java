package org.yeastrc.proxl.xml.merox.reader;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.mods.MeroxStaticModification;
import org.yeastrc.proxl.xml.merox.mods.MeroxVariableModification;
import org.yeastrc.proxl.xml.merox.objects.MeroxAminoAcid;
import org.yeastrc.proxl.xml.merox.objects.MeroxCrosslinker;

/**
 * The properties associated with a merox analysis, as parsed from the
 * properties.mxf file in the merox results zip file.
 * 
 * @author mriffle
 *
 */
public class AnalysisProperties {

	public Map<String, Double> getElements() {
		return elements;
	}

	public void setElements(Map<String, Double> elements) {
		this.elements = elements;
	}

	public Map<String, MeroxAminoAcid> getAminoAcids() {
		return aminoAcids;
	}

	public void setAminoAcids(Map<String, MeroxAminoAcid> aminoAcids) {
		this.aminoAcids = aminoAcids;
	}

	public MeroxCrosslinker getCrosslinker() {
		return crosslinker;
	}

	public void setCrosslinker(MeroxCrosslinker crosslinker) {
		this.crosslinker = crosslinker;
	}

	public Map<String, MeroxVariableModification> getVariableMods() {
		return variableMods;
	}

	public void setVariableMods(Map<String, MeroxVariableModification> variableMods) {
		this.variableMods = variableMods;
	}

	public Map<String, MeroxStaticModification> getStaticMods() {
		return staticMods;
	}

	public void setStaticMods(Map<String, MeroxStaticModification> staticMods) {
		this.staticMods = staticMods;
	}

	public String getDecoyPrefix() {
		return decoyPrefix;
	}

	public void setDecoyPrefix(String decoyPrefix) {
		this.decoyPrefix = decoyPrefix;
	}

	private Map<String, Double> elements;							// keyed by the letter code of the element
	private Map<String, MeroxAminoAcid> aminoAcids;				// keyed by the letter code of the amino acid
	private MeroxCrosslinker crosslinker;
	private Map<String, MeroxVariableModification> variableMods;	// keyed by the "to" letter for the modification
	private Map<String, MeroxStaticModification> staticMods;		// keyed by the "to" letter for the modification
	private String decoyPrefix;
	
}
