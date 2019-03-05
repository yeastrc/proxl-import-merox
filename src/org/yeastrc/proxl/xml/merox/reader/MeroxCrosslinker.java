package org.yeastrc.proxl.xml.merox.reader;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.utils.LinkerUtils;

public class MeroxCrosslinker {

	/**
	 * Get the mass of this crosslinker, using the masses for elements found
	 * in a given analysis properties file.
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	public double getMass( AnalysisProperties properties ) throws Exception {
		return LinkerUtils.calculateLinkerMass( this, properties );
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public List<String> getBindingRules() {
		return bindingRules;
	}
	public void setBindingRules(List<String> bindingRules) {
		this.bindingRules = bindingRules;
	}

	private String name;
	private String formula;
	private List<String> bindingRules;	
}
