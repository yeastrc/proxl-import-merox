package org.yeastrc.proxl.xml.merox.mods;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;
import org.yeastrc.proxl.xml.merox.utils.MassUtils;

public class MeroxVariableModification implements IMeroxModification {
	
	@Override
	public double getMassShift( AnalysisProperties properties ) throws Exception {
		return MassUtils.getMassForModification( this, properties );
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this );
	}
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public int getMaxModifications() {
		return maxModifications;
	}
	public void setMaxModifications(int maxModifications) {
		this.maxModifications = maxModifications;
	}
	
	private String from;
	private String to;
	private int maxModifications;
	
}
