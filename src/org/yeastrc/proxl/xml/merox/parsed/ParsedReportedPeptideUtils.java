package org.yeastrc.proxl.xml.merox.parsed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yeastrc.proxl.xml.merox.reader.MeroxAnalysis;
import org.yeastrc.proxl.xml.merox.reader.Result;

public class ParsedReportedPeptideUtils {

	/**
	 * Convert the results of the analysis (a map of scans to top-scoring PSMs) into a collection of reported peptides
	 * each with associated PSMs
	 * 
	 * @param analysis
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ParsedReportedPeptide> getParsedReportedPeptideFromResults( MeroxAnalysis analysis ) throws Exception {
		Map<String, ParsedReportedPeptide> peptides = new HashMap<String, ParsedReportedPeptide>();
		
		for( Result result : analysis.getAnalysisResults() ) {
				
			String reportedPeptideString = result.getReportedPeptideString();
				
			ParsedReportedPeptide reportedPeptide = null;
			if( peptides.containsKey( reportedPeptideString ) ) {
				reportedPeptide = peptides.get( reportedPeptideString );
			} else {
				reportedPeptide = new ParsedReportedPeptide();
				peptides.put( reportedPeptideString, reportedPeptide );
					
				reportedPeptide.setResults( new ArrayList<Result>() );
				reportedPeptide.setReportedPeptideString( reportedPeptideString );
				reportedPeptide.setType( result.getPsmType() );
					
				// associated the parsed peptides with this reported peptide
				reportedPeptide.setPeptides( ParsedPeptideUtils.getParsePeptides( result, analysis.getAnalysisProperties() ) );					
			}
				
			reportedPeptide.getResults().add( result );								
		}
				
		return peptides;		
	}
	
}
