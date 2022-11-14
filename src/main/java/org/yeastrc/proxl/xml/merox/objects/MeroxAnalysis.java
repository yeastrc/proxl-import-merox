package org.yeastrc.proxl.xml.merox.objects;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;

/**
 * The data associated with a merox analysis, namely the
 * properties file and the PSMs and associated annotations
 * 
 * @author mriffle
 *
 */
public class MeroxAnalysis {

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	public AnalysisProperties getAnalysisProperties() {
		return analysisProperties;
	}

	public void setAnalysisProperties(AnalysisProperties analysisProperties) {
		this.analysisProperties = analysisProperties;
	}

	
	
	public List<Result> getAnalysisResults() {
		return analysisResults;
	}

	/**
	 * The returned data structure is a map where the keys are
	 * the scan number and the values are a list of results with the highest score for that scan.
	 * If multiple results have the highest score, they will all be returned.
	 * @param analysisResults
	 */
	public void setAnalysisResults(List<Result> analysisResults) {
		this.analysisResults = analysisResults;
	}

	public byte[] getPropertiesFileContents() {
		return propertiesFileContents;
	}

	public void setPropertiesFileContents(byte[] propertiesFileContents) {
		this.propertiesFileContents = propertiesFileContents;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private AnalysisProperties analysisProperties;
	private List<Result> analysisResults;
	private byte[] propertiesFileContents;
	private String version;
	
}
