package org.yeastrc.proxl.xml.merox.parsed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yeastrc.proxl.xml.merox.objects.MeroxAnalysis;
import org.yeastrc.proxl.xml.merox.objects.Result;

public class ParsedReportedPeptideUtils {

	/**
	 * Convert a collection of Result objects into a Map keyed on reported peptide string with the value
	 * being the ParsedReportedPeptide object
	 * 
	 * @param analysis
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ParsedReportedPeptide> collateResultsByReportedPeptideString(MeroxAnalysis analysis, String N15prefix ) throws Exception {
		Map<String, ParsedReportedPeptide> peptides = new HashMap<String, ParsedReportedPeptide>();
		
		for( Result result : analysis.getAnalysisResults() ) {

			ParsedReportedPeptide reportedPeptide = new ParsedReportedPeptide();
			ArrayList<ParsedPeptide> parsedPeptides = ParsedPeptideUtils.getParsePeptides(result, analysis.getAnalysisProperties(), N15prefix);

			reportedPeptide.setResults( new ArrayList<>() );
			reportedPeptide.setType( result.getPsmType() );
			reportedPeptide.setPeptides(parsedPeptides);

			if( peptides.containsKey( reportedPeptide.getReportedPeptideString() ) ) {
				reportedPeptide = peptides.get( reportedPeptide.getReportedPeptideString() );
			} else {
				peptides.put( reportedPeptide.getReportedPeptideString(), reportedPeptide );
			}
				
			reportedPeptide.getResults().add( result );								
		}
				
		return peptides;		
	}
	
}
