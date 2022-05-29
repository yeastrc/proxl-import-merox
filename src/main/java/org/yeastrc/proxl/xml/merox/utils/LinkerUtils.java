package org.yeastrc.proxl.xml.merox.utils;

import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;
import org.yeastrc.proxl.xml.merox.reader.MeroxCrosslinker;

public class LinkerUtils {

	/**
	 * Calculate the mass of the supplied crosslinker using its atomic formula, using the masses
	 * provided for those atoms in the merox properties file.
	 * 
	 * @param linker
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	public static double calculateLinkerMass(MeroxCrosslinker linker, AnalysisProperties properties ) throws Exception {
		boolean negative = false;
		
		String formula = linker.getFormula();
		if( formula.startsWith( "-" ) ) {
			negative = true;
			formula = formula.substring( 1 );			
		}

		double mass = MassUtils.getMassFromFormula( formula, properties );
		if( negative ) { mass = mass * -1; }
		
		return mass;
	}
	
}
