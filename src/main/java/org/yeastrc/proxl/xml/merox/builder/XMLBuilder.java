package org.yeastrc.proxl.xml.merox.builder;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.yeastrc.proxl.xml.merox.annotations.PSMAnnotationTypes;
import org.yeastrc.proxl.xml.merox.annotations.PSMDefaultVisibleAnnotationTypes;
import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;
import org.yeastrc.proxl.xml.merox.mods.MeroxStaticModification;
import org.yeastrc.proxl.xml.merox.objects.*;
import org.yeastrc.proxl.xml.merox.parsed.ParsedPeptide;
import org.yeastrc.proxl.xml.merox.parsed.ParsedPeptideModification;
import org.yeastrc.proxl.xml.merox.parsed.ParsedReportedPeptide;
import org.yeastrc.proxl.xml.merox.parsed.ParsedReportedPeptideUtils;
import org.yeastrc.proxl.xml.merox.utils.NumberUtils;
import org.yeastrc.proxl_import.api.xml_dto.*;
import org.yeastrc.proxl_import.api.xml_dto.SearchProgram.PsmAnnotationTypes;
import org.yeastrc.proxl_import.create_import_file_from_java_objects.main.CreateImportFileFromJavaObjectsMain;

/**
 * Take the results of a MeroX analysis and build the ProXL XML
 * @author mriffle
 *
 */
public class XMLBuilder {

	public void buildAndSaveXML(MeroxAnalysis analysis, File fastaFile, String scanFilename, File outputFile, String N15prefix) throws Exception {
		
		ProxlInput proxlInputRoot = new ProxlInput();
		proxlInputRoot.setFastaFilename(fastaFile.getName());
		
		SearchProgramInfo searchProgramInfo = new SearchProgramInfo();
		proxlInputRoot.setSearchProgramInfo( searchProgramInfo );
		
		SearchPrograms searchPrograms = new SearchPrograms();
		searchProgramInfo.setSearchPrograms( searchPrograms );
		
		SearchProgram searchProgram = new SearchProgram();
		searchPrograms.getSearchProgram().add( searchProgram );
		
		searchProgram.setName( MeroxConstants.SEARCH_PROGRAM_NAME );
		searchProgram.setVersion( analysis.getVersion() );
		searchProgram.setDisplayName( MeroxConstants.SEARCH_PROGRAM_NAME );
		
		
		//
		// Define the annotation types present in MeroX data
		//
		PsmAnnotationTypes psmAnnotationTypes = new PsmAnnotationTypes();
		searchProgram.setPsmAnnotationTypes( psmAnnotationTypes );
		
		FilterablePsmAnnotationTypes filterablePsmAnnotationTypes = new FilterablePsmAnnotationTypes();
		psmAnnotationTypes.setFilterablePsmAnnotationTypes( filterablePsmAnnotationTypes );
		filterablePsmAnnotationTypes.getFilterablePsmAnnotationType().addAll( PSMAnnotationTypes.getFilterablePsmAnnotationTypes() );
		
		DescriptivePsmAnnotationTypes descriptivePsmAnnotationTypes = new DescriptivePsmAnnotationTypes();
		psmAnnotationTypes.setDescriptivePsmAnnotationTypes( descriptivePsmAnnotationTypes );
		descriptivePsmAnnotationTypes.getDescriptivePsmAnnotationType().addAll( PSMAnnotationTypes.getDescriptivePsmAnnotationTypes() );
		
		//
		// Define which annotation types are visible by default
		//
		DefaultVisibleAnnotations xmlDefaultVisibleAnnotations = new DefaultVisibleAnnotations();
		searchProgramInfo.setDefaultVisibleAnnotations( xmlDefaultVisibleAnnotations );
		
		VisiblePsmAnnotations xmlVisiblePsmAnnotations = new VisiblePsmAnnotations();
		xmlDefaultVisibleAnnotations.setVisiblePsmAnnotations( xmlVisiblePsmAnnotations );

		xmlVisiblePsmAnnotations.getSearchAnnotation().addAll( PSMDefaultVisibleAnnotationTypes.getDefaultVisibleAnnotationTypes() );

		
		
		//
		// Define the linker information
		//
		Linkers linkers = new Linkers();
		proxlInputRoot.setLinkers( linkers );

		Linker linker = new Linker();
		linkers.getLinker().add( linker );

		MeroxCrosslinker meroxCrosslinker = analysis.getAnalysisProperties().getCrosslinker();

		linker.setName(meroxCrosslinker.getName());

		if(meroxCrosslinker.getSpacerArmLength() != null)
			linker.setSpacerArmLength(meroxCrosslinker.getSpacerArmLength());

		// add any monolink masses
		if(meroxCrosslinker.getMonolinkMonoisotopicMasses() != null && meroxCrosslinker.getMonolinkMonoisotopicMasses().size() > 0) {
			MonolinkMasses xMonolinkMasses = new MonolinkMasses();
			linker.setMonolinkMasses(xMonolinkMasses);

			for(BigDecimal mass : meroxCrosslinker.getMonolinkMonoisotopicMasses()) {
				MonolinkMass xMonolinkMass = new MonolinkMass();
				xMonolinkMass.setMass(mass);

				xMonolinkMasses.getMonolinkMass().add(xMonolinkMass);
			}
		}

		// add crosslink mass
		CrosslinkMasses masses = new CrosslinkMasses();
		linker.setCrosslinkMasses( masses );

		CrosslinkMass xlinkMass = new CrosslinkMass();
		linker.getCrosslinkMasses().getCrosslinkMass().add( xlinkMass );
		xlinkMass.setMass( NumberUtils.getRoundedBigDecimal(meroxCrosslinker.getFullLengthMolecule().getMonoisotopicMass(), 6));
		xlinkMass.setChemicalFormula(meroxCrosslinker.getFullLengthMolecule().getFormula());

		// add any cleavable masses
		if(meroxCrosslinker.getCleavedMolecules() != null && meroxCrosslinker.getCleavedMolecules().size() > 0) {
			for(Molecule molecule : meroxCrosslinker.getCleavedMolecules()) {
				CleavedCrosslinkMass xCleavedCrosslinkMass = new CleavedCrosslinkMass();
				linker.getCrosslinkMasses().getCleavedCrosslinkMass().add(xCleavedCrosslinkMass);

				xCleavedCrosslinkMass.setMass(NumberUtils.getRoundedBigDecimal(molecule.getMonoisotopicMass(), 6));
				xCleavedCrosslinkMass.setChemicalFormula(molecule.getFormula());
			}
		}


		// add linked ends
		if(meroxCrosslinker.getLinkablePosition1() == null || meroxCrosslinker.getLinkablePosition2() == null) {
			throw new Exception("Did not get two linkable ends...");
		}

		LinkedEnds xLinkedEnds = new LinkedEnds();
		linker.setLinkedEnds(xLinkedEnds);

		for(int endNumber : new int[]{0,1}) {
			LinkedEnd xLinkedEnd = new LinkedEnd();
			xLinkedEnds.getLinkedEnd().add(xLinkedEnd);

			LinkablePosition linkablePosition;

			if(endNumber == 1)
				linkablePosition = meroxCrosslinker.getLinkablePosition1();
			else
				linkablePosition = meroxCrosslinker.getLinkablePosition2();

			if(linkablePosition.getResidues() != null && linkablePosition.getResidues().size() > 0) {
				Residues xResidues = new Residues();
				xLinkedEnd.setResidues(xResidues);

				for(String residue : linkablePosition.getResidues()) {
					xResidues.getResidue().add(residue);
				}
			}

			if(linkablePosition.isProteinNTerminus() || linkablePosition.isProteinCTerminus()) {
				ProteinTermini xProteinTermini = new ProteinTermini();
				xLinkedEnd.setProteinTermini(xProteinTermini);

				if(linkablePosition.isProteinNTerminus()) {
					ProteinTerminus xProteinTerminus = new ProteinTerminus();
					xProteinTermini.getProteinTerminus().add(xProteinTerminus);
					xProteinTerminus.setTerminusEnd(ProteinTerminusDesignation.N);
					xProteinTerminus.setDistanceFromTerminus(BigInteger.ZERO);

					xProteinTerminus = new ProteinTerminus();
					xProteinTermini.getProteinTerminus().add(xProteinTerminus);
					xProteinTerminus.setTerminusEnd(ProteinTerminusDesignation.N);
					xProteinTerminus.setDistanceFromTerminus(BigInteger.ONE);
				}

				if(linkablePosition.isProteinCTerminus()) {
					ProteinTerminus xProteinTerminus = new ProteinTerminus();
					xProteinTermini.getProteinTerminus().add(xProteinTerminus);
					xProteinTerminus.setTerminusEnd(ProteinTerminusDesignation.C);
					xProteinTerminus.setDistanceFromTerminus(BigInteger.ZERO);
				}
			}
		}

		//
		// Define the static mods
		//
		
		if( analysis.getAnalysisProperties().getStaticMods() != null &&
				analysis.getAnalysisProperties().getStaticMods().size() > 0 ) {
			
			StaticModifications smods = new StaticModifications();
			proxlInputRoot.setStaticModifications( smods );
			
			for( String smodsTo : analysis.getAnalysisProperties().getStaticMods().keySet() ) {
				
				MeroxStaticModification MeroXSmod = analysis.getAnalysisProperties().getStaticMods().get( smodsTo );
				StaticModification xmlSmod = new StaticModification();
				
				xmlSmod.setAminoAcid( MeroXSmod.getFrom() );
				xmlSmod.setMassChange( NumberUtils.getRoundedBigDecimal( MeroXSmod.getMassShift( analysis.getAnalysisProperties()), 6 ) );
				
				smods.getStaticModification().add( xmlSmod );
			}	
		}
		
		//
		// Add decoy labels
		//
		DecoyLabels xmlDecoyLabels = new DecoyLabels();
		proxlInputRoot.setDecoyLabels( xmlDecoyLabels );

		DecoyLabel xmlDecoyLabel = new DecoyLabel();
		xmlDecoyLabels.getDecoyLabel().add( xmlDecoyLabel );
		xmlDecoyLabel.setPrefix(analysis.getAnalysisProperties().getDecoyPrefix());
		
		//
		// Define the peptide and PSM data
		//
		ReportedPeptides reportedPeptides = new ReportedPeptides();
		proxlInputRoot.setReportedPeptides( reportedPeptides );
		
		Map<String, ParsedReportedPeptide> reportedPeptidesAndResults = ParsedReportedPeptideUtils.collateResultsByReportedPeptideString( analysis, N15prefix );
		Collection<ParsedPeptide> distinctPeptides = new HashSet<>();

		for( String reportedPeptideString : reportedPeptidesAndResults.keySet() ) {
			
			ReportedPeptide xmlReportedPeptide = new ReportedPeptide();
			reportedPeptides.getReportedPeptide().add( xmlReportedPeptide );
			
			xmlReportedPeptide.setReportedPeptideString( reportedPeptideString );
			
			if( reportedPeptidesAndResults.get( reportedPeptideString ).getType() == MeroxConstants.PSM_TYPE_CROSSLINK )
				xmlReportedPeptide.setType( LinkType.CROSSLINK );
			else if( reportedPeptidesAndResults.get( reportedPeptideString ).getType() == MeroxConstants.PSM_TYPE_LOOPLINK )
				xmlReportedPeptide.setType( LinkType.LOOPLINK );
			else
				xmlReportedPeptide.setType( LinkType.UNLINKED );	// monolinked peptide with no cross- or loop-link are considered unlinked (monolinks are considered mods)
			
			Peptides xmlPeptides = new Peptides();
			xmlReportedPeptide.setPeptides( xmlPeptides );
			
			// add in the parsed peptides
			for( ParsedPeptide peptide : reportedPeptidesAndResults.get( reportedPeptideString ).getPeptides() ) {

				distinctPeptides.add(peptide);

				Peptide xmlPeptide = new Peptide();
				xmlPeptides.getPeptide().add( xmlPeptide );
				
				xmlPeptide.setSequence( peptide.getSequence() );
				
				if( peptide.getMods() != null && peptide.getMods().size() > 0 ) {
					
					Modifications xmlModifications = new Modifications();
					xmlPeptide.setModifications( xmlModifications );
					
					for( ParsedPeptideModification mod : peptide.getMods() ) {
						Modification xmlModification = new Modification();
						xmlModifications.getModification().add( xmlModification );
						
						xmlModification.setMass( NumberUtils.getRoundedBigDecimal( mod.getMass(), 6 ) );
						xmlModification.setPosition( new BigInteger( String.valueOf( mod.getPosition() ) ) );
						xmlModification.setIsMonolink( mod.isMonolink() );
					}
				}
				
				// add in the linked position in this peptide
				if( reportedPeptidesAndResults.get( reportedPeptideString ).getType() != MeroxConstants.PSM_TYPE_MONOLINK ) {	// monolinks have no linked positions, they're mods
					LinkedPositions xmlLinkedPositions = new LinkedPositions();
					xmlPeptide.setLinkedPositions( xmlLinkedPositions );
					
					LinkedPosition xmlLinkedPosition = new LinkedPosition();
					xmlLinkedPositions.getLinkedPosition().add( xmlLinkedPosition );
					xmlLinkedPosition.setPosition( new BigInteger( String.valueOf( peptide.getLinkedPosition1() ) ) );

					// looplinked PSMs are linked in two places in the same peptide
					if( reportedPeptidesAndResults.get( reportedPeptideString ).getType() == MeroxConstants.PSM_TYPE_LOOPLINK ) {
						xmlLinkedPosition = new LinkedPosition();
						xmlLinkedPositions.getLinkedPosition().add( xmlLinkedPosition );
						xmlLinkedPosition.setPosition( new BigInteger( String.valueOf( peptide.getLinkedPosition2() ) ) );
					}
				}

				// add in isotope labels
				if(peptide.isIs15N()) {

					Peptide.PeptideIsotopeLabels xPeptideIsotopeLabels = new Peptide.PeptideIsotopeLabels();
					xmlPeptide.setPeptideIsotopeLabels(xPeptideIsotopeLabels);

					Peptide.PeptideIsotopeLabels.PeptideIsotopeLabel xPeptideIsotopeLabel = new Peptide.PeptideIsotopeLabels.PeptideIsotopeLabel();
					xPeptideIsotopeLabel.setLabel("15N");
					xPeptideIsotopeLabels.setPeptideIsotopeLabel(xPeptideIsotopeLabel);
				}

			}
			
			// add in the PSMs and annotations
			Psms xmlPsms = new Psms();
			xmlReportedPeptide.setPsms( xmlPsms );
			
			// iterate over all PSMs
			for( Result result : reportedPeptidesAndResults.get( reportedPeptideString ).getResults() ) {
				Psm xmlPsm = new Psm();
				xmlPsms.getPsm().add( xmlPsm );
				
				if( scanFilename != null && scanFilename.length() > 0 )
					xmlPsm.setScanFileName( scanFilename );
				
				xmlPsm.setScanNumber( new BigInteger( String.valueOf( result.getScanNumber() ) ) );
				xmlPsm.setPrecursorCharge( new BigInteger( String.valueOf( result.getCharge() ) ) );
				xmlPsm.setPrecursorRetentionTime(NumberUtils.getRoundedBigDecimal(result.getRetentionTimeSeconds(), 4));

				try {
					xmlPsm.setPrecursorMZ(NumberUtils.getRoundedBigDecimal(result.getMoverz(), 6 ));
				} catch( Exception e ) {
					xmlPsm.setPrecursorMZ(BigDecimal.valueOf(result.getMoverz()));
				}

				if( result.getPsmType() != MeroxConstants.PSM_TYPE_MONOLINK )
					xmlPsm.setLinkerMass( NumberUtils.getRoundedBigDecimal(analysis.getAnalysisProperties().getCrosslinker().getFullLengthMolecule().getMonoisotopicMass(), 6));
				
				// add in the filterable PSM annotations (e.g., score)
				FilterablePsmAnnotations xmlFilterablePsmAnnotations = new FilterablePsmAnnotations();
				xmlPsm.setFilterablePsmAnnotations( xmlFilterablePsmAnnotations );
				
				// handle score
				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );
					
					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_SCORE );
					xmlFilterablePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( new BigDecimal(result.getScore()));
				}

				
				// handle rank
				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );
					
					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_RANK );
					xmlFilterablePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue( new BigDecimal( result.getRank() ) );				
				}
				
				// handle q-value
				{
					FilterablePsmAnnotation xmlFilterablePsmAnnotation = new FilterablePsmAnnotation();
					xmlFilterablePsmAnnotations.getFilterablePsmAnnotation().add( xmlFilterablePsmAnnotation );
					
					xmlFilterablePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_Q_VALUE );
					xmlFilterablePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					xmlFilterablePsmAnnotation.setValue(NumberUtils.getRoundedBigDecimal(result.getqValue(), 6));
				}
				
				
				// add in the non-filterable descriptive annotations (e.g., calculated mass)
				DescriptivePsmAnnotations xmlDescriptivePsmAnnotations = new DescriptivePsmAnnotations();
				xmlPsm.setDescriptivePsmAnnotations( xmlDescriptivePsmAnnotations );

				{
					// handle m/z
					DescriptivePsmAnnotation xmlDescriptivePsmAnnotation = new DescriptivePsmAnnotation();
					xmlDescriptivePsmAnnotations.getDescriptivePsmAnnotation().add( xmlDescriptivePsmAnnotation );
					
					xmlDescriptivePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_MOVERZ );
					xmlDescriptivePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					
					// try to limit this value to the chosen number of decimal places
					try {
						xmlDescriptivePsmAnnotation.setValue( NumberUtils.getRoundedBigDecimal(result.getMoverz(), 4).toString() );
					} catch( Exception e ) {
						xmlDescriptivePsmAnnotation.setValue( String.valueOf( result.getMoverz() ) );
					}
				}
				
				{
					// handle observed mass
					DescriptivePsmAnnotation xmlDescriptivePsmAnnotation = new DescriptivePsmAnnotation();
					xmlDescriptivePsmAnnotations.getDescriptivePsmAnnotation().add( xmlDescriptivePsmAnnotation );
					
					xmlDescriptivePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_OBSERVED_MASS );
					xmlDescriptivePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					
					// try to limit this value to the chosen number of decimal places
					try {
						xmlDescriptivePsmAnnotation.setValue( NumberUtils.getRoundedBigDecimal(result.getObservedMass(), 4).toString() );
					} catch( Exception e ) {
						xmlDescriptivePsmAnnotation.setValue( String.valueOf( result.getObservedMass() ) );
					}
					
				}
				
				{
					// handle candidate mass
					DescriptivePsmAnnotation xmlDescriptivePsmAnnotation = new DescriptivePsmAnnotation();
					xmlDescriptivePsmAnnotations.getDescriptivePsmAnnotation().add( xmlDescriptivePsmAnnotation );
					
					xmlDescriptivePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_CANDIDATE_MASS );
					xmlDescriptivePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					
					// try to limit this value to the chosen number of decimal places
					try {
						xmlDescriptivePsmAnnotation.setValue( NumberUtils.getRoundedBigDecimal(result.getCandidateMass(), 4).toString() );
					} catch( Exception e ) {
						xmlDescriptivePsmAnnotation.setValue( String.valueOf( result.getCandidateMass() ) );
					}
				}
				
				{
					// handle mass deviation
					DescriptivePsmAnnotation xmlDescriptivePsmAnnotation = new DescriptivePsmAnnotation();
					xmlDescriptivePsmAnnotations.getDescriptivePsmAnnotation().add( xmlDescriptivePsmAnnotation );
					
					xmlDescriptivePsmAnnotation.setAnnotationName( PSMAnnotationTypes.ANNOTATION_TYPE_DEVIATION );
					xmlDescriptivePsmAnnotation.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
					
					// try to limit this value to the chosen number of decimal places
					try {
						xmlDescriptivePsmAnnotation.setValue( NumberUtils.getRoundedBigDecimal(result.getDeviation(), 4).toString() );
					} catch( Exception e ) {
						xmlDescriptivePsmAnnotation.setValue( String.valueOf( result.getDeviation() ) );
					}
					
				}
				
				
			}
			
		}
		
		// add in matched proteins
		MatchedProteinsBuilder.getInstance().buildMatchedProteins( proxlInputRoot, fastaFile, analysis.getAnalysisProperties().getDecoyPrefix(), distinctPeptides, N15prefix );
		
		
		// add in config file
		ConfigurationFiles xmlConfigurationFiles = new ConfigurationFiles();
		proxlInputRoot.setConfigurationFiles( xmlConfigurationFiles );
		
		ConfigurationFile xmlConfigurationFile = new ConfigurationFile();
		xmlConfigurationFiles.getConfigurationFile().add( xmlConfigurationFile );
		
		xmlConfigurationFile.setSearchProgram( MeroxConstants.SEARCH_PROGRAM_NAME );
		xmlConfigurationFile.setFileName( MeroxConstants.PROPERTIES_FILENAME );
		xmlConfigurationFile.setFileContent( analysis.getPropertiesFileContents() );
		
		
		CreateImportFileFromJavaObjectsMain.getInstance().createImportFileFromJavaObjectsMain(outputFile, proxlInputRoot);
		
	}
	
}
