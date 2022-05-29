package org.yeastrc.proxl.xml.merox.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecoyHandler {

	public DecoyHandler() { }
	
	public void readDecoys( InputStream is ) throws Exception {

		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
			
			String currentLine;		// the line we're currently parsing
			while( ( currentLine = br.readLine() ) != null ) {
								
				String[] fields = currentLine.split( ";" );

				Double targetScore = null;
				Double decoyScore = null;
				
				try { targetScore = Double.parseDouble( fields[ 0 ] ); }
				catch( Exception e ) { ; }
				
				try { decoyScore = Double.parseDouble( fields[ 1 ] ); }
				catch( Exception e ) { ; }
				
				if( targetScore != null && targetScore > 0 ) {
					this.reverseSortedTargetScores.add( (int)Math.round( targetScore ) );
				}
				
				if( decoyScore != null && decoyScore > 0 ) {
					this.reverseSortedDecoyScores.add( (int)Math.round( decoyScore ) );
				}
			}
			
		} finally {
			try { br.close(); }
			catch( Throwable t ) { ; }
		}
		
		
		Collections.sort( reverseSortedDecoyScores, Collections.reverseOrder() );
		Collections.sort( reverseSortedTargetScores, Collections.reverseOrder() );
				
	}


	/**
	 * Get the FDR associated with a given score
	 * @param score
	 * @return
	 */
	public double getFDR( int score ) {
		
		int decoyCount = 0;
		int targetCount = 0;
		
		for( int testScore : reverseSortedDecoyScores ) {
			if( testScore < score ) break;
			decoyCount++;
		}
		
		for( int testScore : reverseSortedTargetScores ) {
			if( testScore < score ) break;
			targetCount++;
		}
		
		if( decoyCount == 0 ) return 0;
				
		return (double)decoyCount / (double)targetCount;
	}
	
	
	private List<Integer> reverseSortedDecoyScores = new ArrayList<Integer>();
	private List<Integer> reverseSortedTargetScores = new ArrayList<Integer>();
}
