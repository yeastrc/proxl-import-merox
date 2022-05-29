package org.yeastrc.proxl.xml.merox.reader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.mods.MeroxStaticModification;
import org.yeastrc.proxl.xml.merox.mods.MeroxVariableModification;

/**
 * The properties associated with a merox analysis, as parsed from the
 * properties.ssf file in the merox results zip file.
 * 
 * @author mriffle
 *
 */
public class AnalysisProperties {
	

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	public Map<String, String> getAnalysisSettings() {
		return analysisSettings;
	}
	public void setAnalysisSettings(Map<String, String> analysisSettings) {
		this.analysisSettings = analysisSettings;
	}
	public Map<String, Double> getElements() {
		return elements;
	}
	public void setElements(Map<String, Double> elements) {
		this.elements = elements;
	}
	public Map<String, String> getIonTypes() {
		return ionTypes;
	}
	public void setIonTypes(Map<String, String> ionTypes) {
		this.ionTypes = ionTypes;
	}
	public Map<String, MeroxAminoAcid> getAminoAcids() {
		return aminoAcids;
	}
	public void setAminoAcids(Map<String, MeroxAminoAcid> aminoAcids) {
		this.aminoAcids = aminoAcids;
	}
	public Collection<MeroxProteaseLine> getProteaseLines() {
		return proteaseLines;
	}
	public void setProteaseLines(Collection<MeroxProteaseLine> proteaseLines) {
		this.proteaseLines = proteaseLines;
	}
	public Map<String, MeroxVariableModification> getVariableMods() {
		return variableMods;
	}
	public void setVariableMods(
			Map<String, MeroxVariableModification> variableMods) {
		this.variableMods = variableMods;
	}
	public Map<String, MeroxStaticModification> getStaticMods() {
		return staticMods;
	}
	public void setStaticMods(Map<String, MeroxStaticModification> staticMods) {
		this.staticMods = staticMods;
	}
	public int getCrosslinkerIndex() {
		return crosslinkerIndex;
	}

	public void setCrosslinkerIndex(int crosslinkerIndex) {
		this.crosslinkerIndex = crosslinkerIndex;
	}

	public MeroxCrosslinker getCrosslinker() {
		return crosslinker;
	}

	public void setCrosslinker(MeroxCrosslinker crosslinker) {
		this.crosslinker = crosslinker;
	}

	private Map<String, String> analysisSettings;					// keyed by the setting name
	private Map<String, Double> elements;							// keyed by the letter code of the element
	private Map<String, String> ionTypes;							// keyed by the first field of the ion type line
	private Map<String, MeroxAminoAcid> aminoAcids;				// keyed by the letter code of the amino acid
	private int crosslinkerIndex;
	private MeroxCrosslinker crosslinker;
	private Collection<MeroxProteaseLine> proteaseLines;
	private Map<String, MeroxVariableModification> variableMods;	// keyed by the "to" letter for the modification
	private Map<String, MeroxStaticModification> staticMods;		// keyed by the "to" letter for the modification
	
}
