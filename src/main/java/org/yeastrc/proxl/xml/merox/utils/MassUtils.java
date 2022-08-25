package org.yeastrc.proxl.xml.merox.utils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yeastrc.proteomics.peptide.peptide.Peptide;
import org.yeastrc.proteomics.peptide.peptide.PeptideMassCalculator;
import org.yeastrc.proxl.xml.merox.mods.IMeroxModification;
import org.yeastrc.proxl.xml.merox.parsed.ParsedPeptideModification;
import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;
import org.yeastrc.proxl.xml.merox.reader.MeroxAminoAcid;

public class MassUtils {

	/**
	 * Calculate the mass from the supplied chemical formula (e.g. "C5H7NO3")
	 * using the masses for those atoms present in the merox config file
	 * @param formula
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	public static double getMassFromFormula( String formula, AnalysisProperties properties ) throws Exception {
		double mass = 0;
		
		
		String pattern = "([A-Z][a-z]*)(\\d*)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher( formula );

		while( m.find() ) {
			
			String atom = m.group( 1 );

			int number = 1;
			if( m.group( 2 ) != null && !m.group( 2 ).equals( "" ) )
				number = Integer.parseInt( m.group( 2 ) );
			
			if( !properties.getElements().containsKey( atom ) ) {
				throw new Exception( "No element: " + atom + " is defined in the merox config file." );
			}
			
			double atomicmass = properties.getElements().get( atom );
			mass += atomicmass * number;			
		}
		
		
		return mass;
	}
	
	/**
	 * Calculate the mass of the supplied static modification. The result is the difference of the mass associated
	 * with the "to" part of the mod defition and the "from" part of the mod definition. E.g., if "m" has a mass of 100
	 * and "M" has a mass of 99 (and "m" is the to and "M" is the from), the mass would be calculated as 100-99 or 1.
	 * @param mod
	 * @param properties
	 * @return
	 * @throws Exception If the no mass can be found in the properties files for the letter codes used in the "to" or "from"
	 * for the mod.
	 */
	public static double getMassForModification(IMeroxModification mod, AnalysisProperties properties ) throws Exception {
		
		double fromMass = 0;
		double toMass = 0;
		
		if( properties.getAminoAcids().containsKey( mod.getFrom() ) ) {
			MeroxAminoAcid aa = properties.getAminoAcids().get( mod.getFrom() );
			String formula = aa.getFormula();
			
			fromMass = MassUtils.getMassFromFormula( formula, properties );
		} else {
			throw new Exception( "Could not find a mass associated with amino acid: " + mod.getFrom() + " in the properties file." );
		}
		
		
		if( properties.getAminoAcids().containsKey( mod.getTo() ) ) {
			MeroxAminoAcid aa = properties.getAminoAcids().get( mod.getTo() );
			String formula = aa.getFormula();
			
			toMass = MassUtils.getMassFromFormula( formula, properties );
		} else {
			throw new Exception( "Could not find a mass associated with amino acid: " + mod.getFrom() + " in the properties file." );
		}
		
		return toMass - fromMass;
	}
	
	
	/**
	 * Calculate the mass of the supplied peptide sequence plus any mods that are passed in.
	 * 
	 * @param sequence
	 * @param mods
	 * @return
	 * @throws Exception
	 */
	public static double calculateNeutralMassOfPeptide( String sequence, Collection<ParsedPeptideModification> mods ) throws Exception {
		
		Peptide peptide = new Peptide( sequence );
		double mass = PeptideMassCalculator.getInstance().getMassForPeptide(peptide, org.yeastrc.proteomics.mass.MassUtils.MassType.MONOISOTOPIC);

		if( mods != null ) {
			for( ParsedPeptideModification mod : mods ) {
				mass += mod.getMass();
			}
		}
				
		return mass;
	}
	
}
