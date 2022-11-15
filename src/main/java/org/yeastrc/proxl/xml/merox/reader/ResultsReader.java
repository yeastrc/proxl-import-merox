package org.yeastrc.proxl.xml.merox.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;
import org.yeastrc.proxl.xml.merox.objects.MeroxCrosslinker;
import org.yeastrc.proxl.xml.merox.objects.Result;
import org.yeastrc.proxl.xml.merox.utils.DecoyUtils;

/**
 * Read the PSM-level results from Results.csv in the zipped results file
 * @author mriffle
 *
 */
public class ResultsReader {

	/**
	 * Get the results from Results.csv. The returned data structure is a map where the keys are
	 * the scan number and the values are a list of results with the highest score for that scan.
	 * If multiple results have the highest score, they will all be returned.
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public List<Result> getAnalysisResults(InputStream is, AnalysisProperties analysisProperties ) throws Exception {
		
		/**
		 * This map maps scan numbers to a map of scores found for PSMs for that scan that are linked to the results with that score.
		 * 
		 * Scan Number => {
		 * 					Score1 => {
		 * 								Result1
		 * 								Result2
		 * 							 }
		 * 					Score2 => {
		 * 								Result3
		 * 								Result4
		 * 							 }
		 * 
		 * 				  }
		 * 
		 * Used to calculate ranks for those results, where the highest scoring result for a given scan
		 * is given rank 1, second highest, rank 2 and so on. All results that have the same score are
		 * given the same rank. Results with the next highest score are given a rank of the rank of
		 * the results w/ the next best score + the number of results with that rank.
		 * 
		 * E.g., if 5 PSMs are tied for rank 1, any PSM with the next highest score would be rank 6.
		 * 
		 */
		Map<Integer, Map<Integer, List<Result>>> results = new HashMap<Integer, Map<Integer, List<Result>>>();
		
		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
			
			String currentLine;		// the line we're currently parsing
			while( ( currentLine = br.readLine() ) != null ) {
				
				String[] fields = currentLine.split( ";" );
				Result result = new Result();

				result.setScore( Integer.parseInt( fields[ 0 ] ) );
				result.setMoverz( Double.parseDouble( fields[ 1 ] ) );
				result.setCharge( Integer.parseInt( fields[ 2 ] ) );
				result.setObservedMass( Double.parseDouble( fields[ 3 ] ) );
				result.setCandidateMass( Double.parseDouble( fields [ 4 ] ) );
				result.setDeviation( Double.parseDouble( fields [ 5 ] ) );
				result.setRetentionTimeSeconds(Double.parseDouble(fields[18]));
				result.setqValue(Double.parseDouble(fields[24]));

				result.setPeptide1(fields[6]);
				result.setPeptide2(fields[10]);

				result.setProteins1(fields[7]);
				result.setProteins2(fields[11]);

				result.setScanNumber( getScanNumberFromScanField( fields[ 14 ] ) );

				result.setPosition1String( fields[ 20 ] );
				result.setPosition2String( fields[ 21 ] );

				// Skip this result if it's a decoy hit
				if(DecoyUtils.isDecoyResult(result, analysisProperties)) {
					continue;
				}
				
				/* to ensure consistency about the order of peptides 1 and 2 for a PSM
				 * this is required so that a calculated reported peptide string for a result
				 * always unique identifies the same pair of linked peptides and positions and
				 * because we do not know if merox is consistent about order of peptides in
				 * identified peptide pairs
				 * we will ensure peptide1 is alphabetically less than peptide 2 */
				if( result.getPsmType() == MeroxConstants.PSM_TYPE_CROSSLINK ) {
					if( result.getPeptide1().compareTo( result.getPeptide2() ) > 0 ) {
						
						// need to swap them
						String tmpPeptide = result.getPeptide1();
						String tmpPosition = result.getPosition1String();
						
						result.setPeptide1( result.getPeptide2() );
						result.setPosition1String( result.getPosition2String() );
						
						result.setPeptide2( tmpPeptide );
						result.setPosition2String( tmpPosition );
					} else if( result.getPeptide1().equals( result.getPeptide2() ) ) {
						
						// same peptide sequences, ensure position strings are alphabetized
						if( result.getPosition1String().compareTo( result.getPosition2String() ) > 0 ) {
							String tmpPosition = result.getPosition1String();
							
							result.setPosition1String( result.getPosition2String() );
							result.setPosition2String( tmpPosition );
						}
					}
				} else if( result.getPsmType() == MeroxConstants.PSM_TYPE_LOOPLINK ) {
					if( result.getPosition1String().compareTo( result.getPosition2String() ) > 0 ) {
						String tmpPosition = result.getPosition1String();
						
						result.setPosition1String( result.getPosition2String() );
						result.setPosition2String( tmpPosition );
					}
				}
				
				Map<Integer, List<Result>> scoreMap = null;
				if( !results.containsKey( result.getScanNumber() ) )
					results.put( result.getScanNumber(), new HashMap<Integer, List<Result>>() );
				
				// a map of scores to a list of results that have that score (for this scan number)
				scoreMap = results.get( result.getScanNumber() );
				
				if( !scoreMap.containsKey( result.getScore() ) )
					scoreMap.put( result.getScore(), new ArrayList<Result>() );
				
				scoreMap.get( result.getScore() ).add( result );
			}
			
		} finally {
			try { br.close(); }
			catch( Throwable t ) { ; }
		}
		

		// go over the list of lists, set ranks and create returned object
		List<Result> returnedResults = new ArrayList<Result>();
		
		for( int scanNumber : results.keySet() ) {
			
			// get a descending, sorted list of scores
			ArrayList<Integer> scores = new ArrayList<Integer>( results.get( scanNumber ).keySet() );			
			Collections.sort( scores, Collections.reverseOrder() );
			
			int rank = 1;
			for( int score : scores ) {
				
				for( Result result : results.get( scanNumber ).get( score ) ) {
					
					result.setRank( rank );
					returnedResults.add( result );
				}
				
				// increment the rank to use on the next iteration
				rank += results.get( scanNumber ).get( score ).size();
			}
		}
		
		
		
		return returnedResults;
		
	}

	/**
	 * MeroX can report the scan number in different ways, depending the file format of the scan file. Attempt
	 * to get the scan number based on ways I've thus far encountered.
	 * 
	 * @param scanNumberField
	 * @return
	 * @throws Exception
	 */
	private int getScanNumberFromScanField( String scanNumberField ) throws Exception {
		
		Integer scanNumber = null;
		
		// check if the scan number is just reported as an integer
		{
			try {
				scanNumber = Integer.parseInt( scanNumberField );
			} catch( Exception e ) {
				;
			}
			
			if( scanNumber != null ) { return scanNumber; }

		}

		{
			String[] fields = scanNumberField.split("~");
			if(fields.length == 2) {
				try {
					return Integer.parseInt( fields[ 0 ] );
				} catch( Exception e ) {
					;
				}
			}
		}
		
		// check if scan number is reported as this syntax: "Scan 13190 (rt=14.026) [QE_RH_RZtrisnRP_xlink_01.raw]"
		{
			String[] fields = scanNumberField.split( " " );
			if( fields.length > 1 && fields[ 0 ].equals( "Scan" ) ) {
				try {
					scanNumber = Integer.parseInt( fields[ 1 ] );
				} catch( Exception e ) {
					;
				}
				
				if( scanNumber != null ) { return scanNumber; }
			}
		}
		
		
		// check if scan number is reported as this syntax: "Q_2013_1010_RJ_07.17522.17522.3"
		{
			Pattern r = Pattern.compile( "^(.+)\\.(\\d+)\\.\\d+\\.\\d+$" );
			Matcher m = r.matcher( scanNumberField );
			
			if( m.matches() ) {
				
				try {
					scanNumber = Integer.parseInt( m.group( 2 ) );
				} catch( Exception e ) {
					;
				}
				
				if( scanNumber != null ) { return scanNumber; }

			}
		}
		
		// check if scan number is reported as this syntax: "NS=sn9870"
		{
			Pattern r = Pattern.compile( "^NS=sn(\\d+)$" );
			Matcher m = r.matcher( scanNumberField );
			
			if( m.matches() ) {
				
				try {
					scanNumber = Integer.parseInt( m.group( 1 ) );
				} catch( Exception e ) {
					;
				}
				
				if( scanNumber != null ) { return scanNumber; }

			}
		}
		
		// check if scan number is reported as this syntax:
		// QE_HF_27042018_PR957_ANH_Sample_4.4015.4015.4 File:"QE_HF_27042018_PR957_A NH_Sample_4.raw", NativeID:"controllerType=0 controllerNumber=1 scan=4015"
		{
			Pattern r = Pattern.compile( "^.*scan=(\\d+).*$" );
			Matcher m = r.matcher( scanNumberField );
			
			if( m.matches() ) {
				
				try {
					scanNumber = Integer.parseInt( m.group( 1 ) );
				} catch( Exception e ) {
					;
				}
				
				if( scanNumber != null ) { return scanNumber; }

			}
		}
		
		
		throw new Exception( "Unable to get scan number from the scan number field: " + scanNumberField );		
	}
	
}
