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
	
	
	private static ParsedPeptide getMonolinkedParsedPeptide( String MeroXPeptide, String MeroXPosition, Result result, AnalysisProperties properties ) throws Exception {
		String nakedPeptide = "";
		Collection<ParsedPeptideModification> mods = new ArrayList<ParsedPeptideModification>();

		double staticModTotal = 0.0;	// total static mod mass added
		
		// merox peptide all being with either { or [ and end with } or ]. Remove the first and last characters
		MeroXPeptide = MeroXPeptide.substring(1, MeroXPeptide.length() - 1);
		
		
		for (int i = 0; i < MeroXPeptide.length(); i++){
		    String MeroXResidue = String.valueOf( MeroXPeptide.charAt(i) );
		    int peptidePosition = i + 1;

		    // is this a static mod?
		    if( properties.getStaticMods().containsKey( MeroXResidue ) ) {
		    	nakedPeptide += properties.getStaticMods().get( MeroXResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add to our static mod mass adjustment for later
		    	staticModTotal += MassUtils.getMassForModification( properties.getStaticMods().get( MeroXResidue ), properties );
		    }
		    
		    // is this a variable mod?
		    else if( properties.getVariableMods().containsKey( MeroXResidue ) ) {
		    	nakedPeptide += properties.getVariableMods().get( MeroXResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add this mod to the collection of mods for this peptide
		    	ParsedPeptideModification mod = new ParsedPeptideModification();
		    	mod.setPosition( peptidePosition );
		    	mod.setMass( properties.getVariableMods().get( MeroXResidue ).getMassShift( properties ) );
		    	
		    	mods.add( mod );
		    }
		    
		    // neither a variable or a static mod
		    else {
		    	nakedPeptide += MeroXResidue;
		    }
		    
		}
		
		// get calculated monoisotopic mass for peptide, including dynamic and static mods
		double massPreMonolink = MassUtils.calculateNeutralMassOfPeptide( nakedPeptide, mods );		
		massPreMonolink += staticModTotal;
		
		double MeroXCalculatedMass = result.getCandidateMass();

		// merox reports MH+ mass
		MeroXCalculatedMass -= AtomUtils.ATOM_HYDROGEN.getMass( org.yeastrc.proteomics.mass.MassUtils.MassType.MONOISOTOPIC );
		
		double massOfMonolink = MeroXCalculatedMass - massPreMonolink;
		BigDecimal roundedMassOfMonolink = new BigDecimal( massOfMonolink );
		roundedMassOfMonolink = roundedMassOfMonolink.setScale( 4, RoundingMode.HALF_UP );
		
		// handle the monolinked position as a variable mod
		MeroXPosition = MeroXPosition.substring( 1 );
		int position = Integer.parseInt( MeroXPosition );
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
	
	
	private static ParsedPeptide getLooplinkedParsedPeptide( String MeroXPeptide, String MeroXPosition1, String MeroXPosition2, AnalysisProperties properties ) throws Exception {
		String nakedPeptide = "";
		Collection<ParsedPeptideModification> mods = new ArrayList<ParsedPeptideModification>();
		
		// merox peptide all being with either { or [ and end with } or ]. Remove the first and last characters
		MeroXPeptide = MeroXPeptide.substring(1, MeroXPeptide.length() - 1);
		
		
		for (int i = 0; i < MeroXPeptide.length(); i++){
		    String MeroXResidue = String.valueOf( MeroXPeptide.charAt(i) );
		    int peptidePosition = i + 1;

		    // is this a static mod?
		    if( properties.getStaticMods().containsKey( MeroXResidue ) ) {
		    	nakedPeptide += properties.getStaticMods().get( MeroXResidue ).getFrom();		// use the unmodded code for this residue
		    }
		    
		    // is this a variable mod?
		    else if( properties.getVariableMods().containsKey( MeroXResidue ) ) {
		    	nakedPeptide += properties.getVariableMods().get( MeroXResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add this mod to the collection of mods for this peptide
		    	ParsedPeptideModification mod = new ParsedPeptideModification();
		    	mod.setPosition( peptidePosition );
		    	mod.setMass( properties.getVariableMods().get( MeroXResidue ).getMassShift( properties ) );
		    	
		    	mods.add( mod );
		    }
		    
		    // neither a variable or a static mod
		    else {
		    	nakedPeptide += MeroXResidue;
		    }
		    
		}
		
		// handle the positions
		MeroXPosition1 = MeroXPosition1.substring( 1 );
		int position1 = Integer.parseInt( MeroXPosition1 );
		if( position1 == 0 ) { position1 = 1; }
		
		MeroXPosition2 = MeroXPosition2.substring( 1 );
		int position2 = Integer.parseInt( MeroXPosition2 );
		if( position2 == 0 ) { position2 = 1; }
				
		ParsedPeptide peptide = new ParsedPeptide();
		peptide.setSequence( nakedPeptide );
		peptide.setMods( mods );
		peptide.setLinkedPosition1( position1 );
		peptide.setLinkedPosition2( position2 );
		
		return peptide;
	}
	
	private static ParsedPeptide getCrosslinkedParsedPeptide( String MeroXPeptide, String MeroXPosition, AnalysisProperties properties ) throws Exception {
		String nakedPeptide = "";
		Collection<ParsedPeptideModification> mods = new ArrayList<ParsedPeptideModification>();
		
		// merox peptide all being with either { or [ and end with } or ]. Remove the first and last characters
		MeroXPeptide = MeroXPeptide.substring(1, MeroXPeptide.length() - 1);
		
		
		for (int i = 0; i < MeroXPeptide.length(); i++){
		    String MeroXResidue = String.valueOf( MeroXPeptide.charAt(i) );
		    int peptidePosition = i + 1;

		    // is this a static mod?
		    if( properties.getStaticMods().containsKey( MeroXResidue ) ) {
		    	nakedPeptide += properties.getStaticMods().get( MeroXResidue ).getFrom();		// use the unmodded code for this residue
		    }
		    
		    // is this a variable mod?
		    else if( properties.getVariableMods().containsKey( MeroXResidue ) ) {
		    	nakedPeptide += properties.getVariableMods().get( MeroXResidue ).getFrom();		// use the unmodded code for this residue
		    	
		    	// add this mod to the collection of mods for this peptide
		    	ParsedPeptideModification mod = new ParsedPeptideModification();
		    	mod.setPosition( peptidePosition );
		    	mod.setMass( properties.getVariableMods().get( MeroXResidue ).getMassShift( properties ) );
		    	
		    	mods.add( mod );
		    }
		    
		    // neither a variable or a static mod
		    else {
		    	nakedPeptide += MeroXResidue;
		    }
		    
		}
		
		// handle the position
		MeroXPosition = MeroXPosition.substring( 1 );
		int position = Integer.parseInt( MeroXPosition );
		if( position == 0 ) { position = 1; }
		
				
		ParsedPeptide peptide = new ParsedPeptide();
		peptide.setSequence( nakedPeptide );
		peptide.setMods( mods );
		peptide.setLinkedPosition1( position );
		
		return peptide;
	}
	
	
	
}
