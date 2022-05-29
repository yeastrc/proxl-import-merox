package org.yeastrc.proxl.xml.merox.parsed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;

import org.yeastrc.proteomics.peptide.atom.AtomUtils;
import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;
import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;
import org.yeastrc.proxl.xml.merox.reader.Result;
import org.yeastrc.proxl.xml.merox.utils.MassUtils;

public class ParsedPeptideUtils {

	/**
	 * Take a merox result and return the two parsed peptides from the described crosslink.
	 * 
	 * @param result
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	public static Collection<ParsedPeptide> getParsePeptides( Result result, AnalysisProperties properties ) throws Exception {
		
		Collection<ParsedPeptide> parsedPeptides = new ArrayList<ParsedPeptide>();
		
		if( result.getPsmType() == MeroxConstants.PSM_TYPE_CROSSLINK ) {
			parsedPeptides.add( getCrosslinkedParsedPeptide( result.getPeptide1(), result.getPosition1String(), properties ) );
			parsedPeptides.add( getCrosslinkedParsedPeptide( result.getPeptide2(), result.getPosition2String(), properties ) );		
		}
		
		else if( result.getPsmType() == MeroxConstants.PSM_TYPE_LOOPLINK ) {
			parsedPeptides.add( getLooplinkedParsedPeptide( result.getPeptide1(), result.getPosition1String(), result.getPosition2String(), properties ) );
		}
		
		else {
			parsedPeptides.add( getMonolinkedParsedPeptide( result.getPeptide1(), result.getPosition1String(), result, properties ) );
		}
		
		
		return parsedPeptides;
	}
	
	
	private static ParsedPeptide getMonolinkedParsedPeptide( String stavroxPeptide, String stavroxPosition, Result result, AnalysisProperties properties ) throws Exception {
		String nakedPeptide = "";
		Collection<ParsedPeptideModification> mods = new ArrayList<ParsedPeptideModification>();

		double staticModTotal = 0.0;	// total static mod mass added
		
		// merox peptide all being with either { or [ and end with } or ]. Remove the first and last characters
		stavroxPeptide = stavroxPeptide.substring(1, stavroxPeptide.length() - 1);
		
		
		for (int i = 0; i < stavroxPeptide.length(); i++){
		    String stavroxResidue = String.valueOf( stavroxPeptide.charAt(i) );
		    int peptidePosition = i + 1;

		    // is this a static mod?
		    if( properties.getStaticMods().containsKey( stavroxResidue ) ) {
		    	nakedPeptide += properties.getStaticMods().get( stavroxResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add to our static mod mass adjustment for later
		    	staticModTotal += MassUtils.getMassForModification( properties.getStaticMods().get( stavroxResidue ), properties );
		    }
		    
		    // is this a variable mod?
		    else if( properties.getVariableMods().containsKey( stavroxResidue ) ) {
		    	nakedPeptide += properties.getVariableMods().get( stavroxResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add this mod to the collection of mods for this peptide
		    	ParsedPeptideModification mod = new ParsedPeptideModification();
		    	mod.setPosition( peptidePosition );
		    	mod.setMass( properties.getVariableMods().get( stavroxResidue ).getMassShift( properties ) );
		    	
		    	mods.add( mod );
		    }
		    
		    // neither a variable or a static mod
		    else {
		    	nakedPeptide += stavroxResidue;
		    }
		    
		}
		
		// get calculated monoisotopic mass for peptide, including dynamic and static mods
		double massPreMonolink = MassUtils.calculateNeutralMassOfPeptide( nakedPeptide, mods );		
		massPreMonolink += staticModTotal;
		
		double stavroxCalculatedMass = result.getCandidateMass();

		// merox reports MH+ mass
		stavroxCalculatedMass -= AtomUtils.ATOM_HYDROGEN.getMass( org.yeastrc.proteomics.mass.MassUtils.MASS_TYPE_MONOISOTOPIC );
		
		double massOfMonolink = stavroxCalculatedMass - massPreMonolink;
		BigDecimal roundedMassOfMonolink = new BigDecimal( massOfMonolink );
		roundedMassOfMonolink = roundedMassOfMonolink.setScale( 4, RoundingMode.HALF_UP );
		
		// handle the monolinked position as a variable mod
		stavroxPosition = stavroxPosition.substring( 1 );
		int position = Integer.parseInt( stavroxPosition );
		if( position == 0 ) { position = 1; }
		
		ParsedPeptideModification mod = new ParsedPeptideModification();
		//mod.setMass( result.getLinker().getMass( properties ) );
		mod.setMass( roundedMassOfMonolink.doubleValue() );
		mod.setMonolink( true );
		mod.setPosition( position );
		
		
		
		mods.add( mod );
				
		ParsedPeptide peptide = new ParsedPeptide();
		peptide.setSequence( nakedPeptide );
		peptide.setMods( mods );
		
		return peptide;
	}
	
	
	private static ParsedPeptide getLooplinkedParsedPeptide( String stavroxPeptide, String stavroxPosition1, String stavroxPosition2, AnalysisProperties properties ) throws Exception {
		String nakedPeptide = "";
		Collection<ParsedPeptideModification> mods = new ArrayList<ParsedPeptideModification>();
		
		// merox peptide all being with either { or [ and end with } or ]. Remove the first and last characters
		stavroxPeptide = stavroxPeptide.substring(1, stavroxPeptide.length() - 1);
		
		
		for (int i = 0; i < stavroxPeptide.length(); i++){
		    String stavroxResidue = String.valueOf( stavroxPeptide.charAt(i) );
		    int peptidePosition = i + 1;

		    // is this a static mod?
		    if( properties.getStaticMods().containsKey( stavroxResidue ) ) {
		    	nakedPeptide += properties.getStaticMods().get( stavroxResidue ).getFrom();		// use the unmodded code for this residue
		    }
		    
		    // is this a variable mod?
		    else if( properties.getVariableMods().containsKey( stavroxResidue ) ) {
		    	nakedPeptide += properties.getVariableMods().get( stavroxResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add this mod to the collection of mods for this peptide
		    	ParsedPeptideModification mod = new ParsedPeptideModification();
		    	mod.setPosition( peptidePosition );
		    	mod.setMass( properties.getVariableMods().get( stavroxResidue ).getMassShift( properties ) );
		    	
		    	mods.add( mod );
		    }
		    
		    // neither a variable or a static mod
		    else {
		    	nakedPeptide += stavroxResidue;
		    }
		    
		}
		
		// handle the positions
		stavroxPosition1 = stavroxPosition1.substring( 1 );
		int position1 = Integer.parseInt( stavroxPosition1 );
		if( position1 == 0 ) { position1 = 1; }
		
		stavroxPosition2 = stavroxPosition2.substring( 1 );
		int position2 = Integer.parseInt( stavroxPosition2 );
		if( position2 == 0 ) { position2 = 1; }
				
		ParsedPeptide peptide = new ParsedPeptide();
		peptide.setSequence( nakedPeptide );
		peptide.setMods( mods );
		peptide.setLinkedPosition1( position1 );
		peptide.setLinkedPosition2( position2 );
		
		return peptide;
	}
	
	private static ParsedPeptide getCrosslinkedParsedPeptide( String stavroxPeptide, String stavroxPosition, AnalysisProperties properties ) throws Exception {
		String nakedPeptide = "";
		Collection<ParsedPeptideModification> mods = new ArrayList<ParsedPeptideModification>();
		
		// merox peptide all being with either { or [ and end with } or ]. Remove the first and last characters
		stavroxPeptide = stavroxPeptide.substring(1, stavroxPeptide.length() - 1);
		
		
		for (int i = 0; i < stavroxPeptide.length(); i++){
		    String stavroxResidue = String.valueOf( stavroxPeptide.charAt(i) );
		    int peptidePosition = i + 1;

		    // is this a static mod?
		    if( properties.getStaticMods().containsKey( stavroxResidue ) ) {
		    	nakedPeptide += properties.getStaticMods().get( stavroxResidue ).getFrom();		// use the unmodded code for this residue
		    }
		    
		    // is this a variable mod?
		    else if( properties.getVariableMods().containsKey( stavroxResidue ) ) {
		    	nakedPeptide += properties.getVariableMods().get( stavroxResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add this mod to the collection of mods for this peptide
		    	ParsedPeptideModification mod = new ParsedPeptideModification();
		    	mod.setPosition( peptidePosition );
		    	mod.setMass( properties.getVariableMods().get( stavroxResidue ).getMassShift( properties ) );
		    	
		    	mods.add( mod );
		    }
		    
		    // neither a variable or a static mod
		    else {
		    	nakedPeptide += stavroxResidue;
		    }
		    
		}
		
		// handle the position
		stavroxPosition = stavroxPosition.substring( 1 );
		int position = Integer.parseInt( stavroxPosition );
		if( position == 0 ) { position = 1; }
		
				
		ParsedPeptide peptide = new ParsedPeptide();
		peptide.setSequence( nakedPeptide );
		peptide.setMods( mods );
		peptide.setLinkedPosition1( position );
		
		return peptide;
	}
	
	
	
}
