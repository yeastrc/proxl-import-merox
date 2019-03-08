package org.yeastrc.proxl.xml.merox.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yeastrc.proxl.xml.merox.mods.MeroxStaticModification;
import org.yeastrc.proxl.xml.merox.mods.MeroxVariableModification;

/**
 * Read the properties out of a properties file in the results file of a StavroX analysis
 * @author mriffle
 *
 */
public class PropertiesReader {

	private static final int NONE				 = -1;
	private static final int SETTINGS            = 0;
	private static final int ELEMENTS	 		 = 1;
	private static final int IONTYPES	 		 = 2;
	private static final int AMINOACIDS 		 = 3;
	private static final int CROSSLINKER 		 = 4;
	private static final int PROTEASE			 = 5;
	private static final int VARMODIFICATION	 = 6;
	private static final int STATMODIFICATION	 = 7;	
	private static final int UNKNOWN			 = 8;
	private static final int REPORTERIONS		 = 9;

	/**
	 * Get the cross-linker for this experiment as defined in the properties file
	 *
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public MeroxCrosslinker getCrosslinkerFromProperties( InputStream is ) throws Exception {

		String linkerName = this.getCrosslinkerNameFromProperties( is );
		return this.getCrosslinkerForNameFromProperties( is, linkerName );

	}

	/**
	 * Get the Merox name for the crosslinker used in this experiment which can
	 * then be used to determine cross-linker properties as defined in the
	 * properties file.
	 *
	 * Syntax:
	 * USEDCROSSLINKER=DSSO				//Cross-linker that is selected from the following list.
	 *
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public String getCrosslinkerNameFromProperties( InputStream is ) throws Exception {

		Pattern p = Pattern.compile( "USEDCROSSLINKER=(.+)$" );

		BufferedReader br = null;
		try {

			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment( currentLine );

				Matcher m = p.matcher( currentLine );
				if( m.matches() ) {
					return m.group(1);
				}
			}

		} finally {
			br.close();
		}

		throw new Exception( "Unable to determine cross-linker from Merox properties." );
	}

	/**
	 * Get a cross-linker object populated with the properties of the supplied cross-linker as
	 * defined in the properties file.
	 *
	 * Syntax:
	 *         CROSSLINKER=DSBU
	 *         COMPOSITION=C9O3N2H12
	 *         COMPHEAVY=
	 *         SITE1=K{
	 *         SITE2=KSTY{
	 *         MAXIMUMDISTANCE=26.9
	 *         CNL=C4H7NO
	 *         DEADENDMOLECULE=H2O
	 *         REPORTERIONS
	 *         RBu;C9N2OH17
	 *         RBuUr;C10N2O2H15
	 *         MODSITE1
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         MODSITE2
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         END
	 *
	 * @param is
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public MeroxCrosslinker getCrosslinkerForNameFromProperties( InputStream is, String name ) throws Exception {

		Pattern p = Pattern.compile( "CROSSLINKER=(.+)$" );
		ArrayList<String> linkerDefinitionLines = new ArrayList<>(25 );
		boolean readingLinkerDefinition = false;

		BufferedReader br = null;
		try {

			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment( currentLine );

				Matcher m = p.matcher( currentLine );
				if( m.matches() ) {

					if( m.group( 1 ).equals( name ) ) {
						 linkerDefinitionLines.add( currentLine );
						 readingLinkerDefinition = true;
						 continue;
					}
				}

				else if( readingLinkerDefinition ) {

					if( currentLine.equals( "END" ) ) {

						return getCrosslinkerFromStringArray( linkerDefinitionLines );
					} else {

						linkerDefinitionLines.add( currentLine );
					}
				}

			}

		} finally {
			br.close();
		}

		throw new Exception( "Unable to find cross-linker definition for linker: " + name );
	}

	/**
	 * Get the crosslinker defined by the supplied array of lines from the properties file
	 *
	 * Syntax:
	 *         CROSSLINKER=DSBU
	 *         COMPOSITION=C9O3N2H12
	 *         COMPHEAVY=
	 *         SITE1=K{
	 *         SITE2=KSTY{
	 *         MAXIMUMDISTANCE=26.9
	 *         CNL=C4H7NO
	 *         DEADENDMOLECULE=H2O
	 *         REPORTERIONS
	 *         RBu;C9N2OH17
	 *         RBuUr;C10N2O2H15
	 *         MODSITE1
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         MODSITE2
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         END
	 *
	 * @param linkerDefinitionLines
	 * @return
	 */
	public MeroxCrosslinker getCrosslinkerFromStringArray( ArrayList<String> linkerDefinitionLines ) throws Exception {

		MeroxCrosslinker crosslinker = new MeroxCrosslinker();
		crosslinker.setCleavedFormulae( new HashSet<String>() );
		crosslinker.setBindingRules( new ArrayList<String>() );

		boolean readingModSite = false;

		for( String line : linkerDefinitionLines ) {

			if( line.equals( "MODSITE1" ) ) {
				readingModSite = true;
			} else if( line.equals( "MODSITE2" ) ) {
				readingModSite = true;
			} else if( readingModSite ) {

				if( line.equals( "END" ) ) {
					readingModSite = false;
				} else {

					String[] subFields = line.split(";");

					if (subFields.length != 3) {
						throw new Exception("Unexpected syntax for MODSITE defiition. Got " + subFields);
					}

					// only include non empty formulae
					if (subFields[1].length() > 0) {
						crosslinker.getCleavedFormulae().add(subFields[1]);
					}
				}
			} else {

				String[] fields = line.split("=");

				if (fields[0].equals("CROSSLINKER")) {
					crosslinker.setName(fields[1]);
				} else if (fields[0].equals("COMPOSITION")) {
					crosslinker.setFormula(fields[1]);
				} else if (fields[0].equals("SITE1")) {
					crosslinker.getBindingRules().add(fields[1]);
				} else if (fields[0].equals("SITE2")) {
					crosslinker.getBindingRules().add(fields[1]);
				}
			}
		}

		return crosslinker;
	}


	public AnalysisProperties getAnalysisProperties( InputStream is ) throws Exception {

		AnalysisProperties ap = new AnalysisProperties();
		
		ap.setAnalysisSettings( new HashMap< String, String >() );
		ap.setAminoAcids( new HashMap<String, MeroxAminoAcid>() );
		ap.setElements( new HashMap<String, Double>() );
		ap.setIonTypes( new HashMap<String, String>() );
		ap.setProteaseLines( new ArrayList<MeroxProteaseLine>() );
		ap.setStaticMods( new HashMap<String, MeroxStaticModification>() );
		ap.setVariableMods( new HashMap<String, MeroxVariableModification>() );

		// separately get the cross-linker defiition
		ap.setCrosslinker( getCrosslinkerFromProperties( is ) );
		
		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
			
			String currentLine;		// the line we're currently parsing
			int mode = NONE;
			while( ( currentLine = br.readLine() ) != null ) {

				System.out.println( "reading line: " + currentLine );

				currentLine = removeComment( currentLine );

				System.out.println( "removed comment: " + currentLine );



				if( currentLine.equals( "END" ) ) {

					if( mode == ELEMENTS ) {
						mode = SETTINGS;
						continue;
					}

					mode = NONE;
					continue;
				}
				
				
				// find the new mode
				if( mode == NONE ) {
					if( currentLine.startsWith( "ELEMENTS" ) )
						mode = ELEMENTS;
					else if( currentLine.startsWith( "IONTYPES" ) )
						mode = IONTYPES;
					else if( currentLine.startsWith( "AMINOACIDS" ) )
						mode = AMINOACIDS;
					else if( currentLine.startsWith( "PROTEASE" ) )
						mode = PROTEASE;
					else if( currentLine.startsWith( "VARMODIFICATION" ) )
						mode = VARMODIFICATION;
					else if( currentLine.startsWith( "STATMODIFICATION" ) )
						mode = STATMODIFICATION;
					
					// special case, section header includes data
					else if( currentLine.startsWith( "CROSSLINKER=" ) ) {
						mode = CROSSLINKER;
					}
					
					else if( currentLine.startsWith( "REPORTERIONS" ) )
						mode = REPORTERIONS;
					
					else {
						
						System.err.println( "INFO: Got unknown header in properties file: " + currentLine );
						mode = UNKNOWN;						
					}
					
					continue;
				}
				
				
				// handle processing the various sections of the properties file
				
				if( mode == UNKNOWN ) {
					continue;
				}
				
				else if( mode == SETTINGS ) {
					
					// for some reason, the settings at the beginning of this file do not have a header line or an END line
					if( currentLine.startsWith( "ELEMENTS" ) ) {
						mode = ELEMENTS;
						continue;
					}

					// for some reason, the settings at the beginning of this file do not have a header line or an END line
					if( currentLine.startsWith( "IONTYPES" ) ) {
						mode = IONTYPES;
						continue;
					}
					
					String fields[] = currentLine.split( "=" );
					ap.getAnalysisSettings().put( fields[ 0 ],  fields[ 1 ] );
				}
				
				else if( mode == ELEMENTS ) {
					String fields[] = currentLine.split( ";" );
					ap.getElements().put( fields[ 0 ], Double.parseDouble( fields[ 1 ] ) );
				}
				
				else if( mode == IONTYPES ) {
					String fields[] = currentLine.split( ";" );
					ap.getIonTypes().put( fields[ 0 ],  fields[ 1 ] );
				}
				
				else if( mode == AMINOACIDS ) {
					String fields[] = currentLine.split( ";" );
					
					MeroxAminoAcid aa = new MeroxAminoAcid( fields[ 1 ], fields[ 0 ], fields[ 2 ] );
					
					ap.getAminoAcids().put( fields[ 1 ], aa );
				}
				
				
				else if( mode == CROSSLINKER ) {

				}
				
				else if( mode == PROTEASE ) {
					String fields[] = currentLine.split( ";" );

					MeroxProteaseLine spl = new MeroxProteaseLine();
					spl.setSite( fields[ 0 ] );
					spl.setMissedCleavages( fields[ 1 ] );
					spl.setBlockedBy( fields [ 2 ] );
					
					ap.getProteaseLines().add( spl );					
				}
				
				else if( mode == VARMODIFICATION ) {
					String fields[] = currentLine.split( ";" );

					MeroxVariableModification mod = new MeroxVariableModification();
					mod.setFrom( fields[ 0 ] );
					mod.setTo( fields[ 1 ] );
					mod.setMaxModifications( Integer.parseInt( fields [ 2 ] ) );
					
					ap.getVariableMods().put( fields[ 1 ], mod );					
				}
				
				else if( mode == STATMODIFICATION ) {
					String fields[] = currentLine.split( ";" );

					MeroxStaticModification mod = new MeroxStaticModification();
					mod.setFrom( fields [ 0 ] );
					mod.setTo( fields [ 1 ] );
					
					ap.getStaticMods().put( fields[ 1 ], mod );					
				}
				
				else if( mode == REPORTERIONS ) {
					continue;	// nothing to do for this.				
				}
				
			}
			
		} finally {
			try { br.close(); }
			catch( Throwable t ) { ; }
		}
		
		
		return ap;
	}

	private static String removeComment( String line ) {
		return line.replaceAll( "\\/\\/.+", "" ).trim();

	}

}
