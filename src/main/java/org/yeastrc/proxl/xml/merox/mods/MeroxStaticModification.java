package org.yeastrc.proxl.xml.merox.mods;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;
import org.yeastrc.proxl.xml.merox.utils.MassUtils;

public class MeroxStaticModification implements IMeroxModification {
	
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
	
	private String from;
	private String to;
	
}
