package org.yeastrc.proxl.xml.merox.main;

import org.yeastrc.proxl.xml.merox.builder.XMLBuilder;
import org.yeastrc.proxl.xml.merox.constants.Constants;
import org.yeastrc.proxl.xml.merox.reader.MeroxAnalysis;
import org.yeastrc.proxl.xml.merox.reader.MeroxAnalysisLoader;
import org.yeastrc.proxl.xml.merox.utils.LinkerMapper;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convert a MeroX data file to a ProXL XML file
 * @author mriffle
 *
 */
@CommandLine.Command(name = "java -jar " + Constants.CONVERSION_PROGRAM_NAME,
		mixinStandardHelpOptions = true,
		version = Constants.CONVERSION_PROGRAM_NAME + " " + Constants.CONVERSION_PROGRAM_VERSION,
		sortOptions = false,
		synopsisHeading = "%n",
		descriptionHeading = "%n@|bold,underline Description:|@%n%n",
		optionListHeading = "%n@|bold,underline Options:|@%n",
		description = "Convert the results of a MeroX analysis to a Proxl XML file suitable for import into Proxl.\n\n" +
				"More info at: " + Constants.CONVERSION_PROGRAM_URI
)
public class MainProgram implements Runnable {

	@CommandLine.Option(names = { "-r", "--zhrm-file" }, required = true, description = "The full path to the results file from the MeroX analysis.")
	private File zhrmFile;

	@CommandLine.Option(names = { "-f", "--fasta-file" }, required = true, description = "The full path to the FASTA file used in the MeroX analysis.")
	private File fastaFile;

	@CommandLine.Option(names = { "-l", "--linker" }, required = true, description = "The name of the cross-linker used. Valid linkers: bs2, bs3, bs2.sty (supports STY as linkable), bs3.sty, dss, dsso, edc, sulfo-smcc, tg (short for transglutaminase)")
	private String linkerName;

	@CommandLine.Option(names = { "-o", "--out-file" }, required = true, description = "The full path to the desired output proxl XML file.")
	private File outFile;

	@CommandLine.Option(names = { "-s", "--scan-filename" }, required = false, description = "The name of the scan file (e.g., mydata.mzML) used to search the data. Used to annotate PSMs with the name of the scan file, required if using Bibliospec to create a spectral library for Skyline.")
	private String scanFilename;

	@CommandLine.Option(names = { "-a", "--scan-number-adjustment" }, required = false, description = "(Optional, defaults to 1) When using mzML, MeroX outputs the index of the scan as opposed to its scan number. This causes a mismatch when Proxl attempts to use the scan number to match PSMs to scans. In testing, the scan numbers appears to always be 1 higher than the reported scan index when using mzML files. \"-a 1\" will adjust the scan numbers appropriately.")
	private int scanNumberAdjustment = 1;

	@CommandLine.Option(names = { "-v", "--verbose" }, required = false, description = "If present, complete error messages will be printed. Useful for debugging errors.")
	private boolean verboseRequested = false;

	/**
	 * Do the data conversion and save the XML file
	 * 
	 * @param resultsFile The results file
	 * @param linkerName The name of the cross-linker
	 * @param fastaFile The FASTA file
	 * @param scanFilename The name of the scan filename
	 * @param scanNumberAdjustment The amount to adjust the reported scan numbers by
	 * @param outputFile The output file
	 * 
	 * @throws Exception If there is a problem
	 */
	private void convertData(File resultsFile, String linkerName, File fastaFile, String scanFilename, int scanNumberAdjustment, File outputFile) throws Exception {
		
		MeroxAnalysisLoader loader = new MeroxAnalysisLoader();
		MeroxAnalysis analysis = loader.loadMeroXAnalysis( resultsFile );
		
		// ensure the linker they entered maps to a linker in the properties file
		String MeroXLinker = LinkerMapper.getMeroXCrosslinkerName( linkerName );
		if( !MeroXLinker.equals( analysis.getAnalysisProperties().getCrosslinker().getName() ) ) {
			String message = "Entered linker: " + linkerName + " does not map to linker in properties: " + analysis.getAnalysisProperties().getCrosslinker().getName();
			throw new Exception( message );
		}
		
		/*
		// test print out the all linkers and calculated masses from properties file
		for( MeroxCrosslinker linker : analysis.getAnalysisProperties().getCrosslinkers() ) {
			System.out.println( linker.getName() + " : " + linker.getFormula() + " : calculated mass: " + LinkerUtils.calculateLinkerMass( linker, analysis.getAnalysisProperties() ) );
		}
		*/
		
		XMLBuilder builder = new XMLBuilder();
		builder.buildAndSaveXML(analysis, linkerName, fastaFile, scanFilename, scanNumberAdjustment, outputFile );
		
	}

	private String[] args;
	
	public void run() {
		
		printStartup();

		if(!(zhrmFile.exists())) {
			System.err.println("Results file " + zhrmFile.getAbsolutePath() + " does not exist.");
			System.exit(1);
		}

		if(!(fastaFile.exists())) {
			System.err.println("FASTA file " + fastaFile.getAbsolutePath() + " does not exist.");
			System.exit(1);
		}

		if(!(LinkerMapper.getLinkerMap().containsKey(linkerName))) {
			List<String> validLinkers = new ArrayList<>(LinkerMapper.getLinkerMap().keySet());
			Collections.sort(validLinkers);

			System.err.println("'" + linkerName + "' is not a valid linker name. Must be one of: " +
					String.join(",", validLinkers));
			System.exit(1);
		}


		try {
			MainProgram mp = new MainProgram();
			mp.convertData(zhrmFile, linkerName, fastaFile, scanFilename, scanNumberAdjustment, outFile);

		} catch(Throwable t) {
			System.out.println( "\nError encountered:" );
			System.out.println( t.getMessage() );

			if(verboseRequested) {
				t.printStackTrace();
			}

			System.exit( 1 );
		}
		
		
	}

	public static void main( String[] args ) {

		MainProgram mp = new MainProgram();
		mp.args = args;

		CommandLine.run(mp, args);
	}
	
	public static void printStartup() {
		
		try( BufferedReader br = new BufferedReader( new InputStreamReader( MainProgram.class.getResourceAsStream( "startup.txt" ) ) ) ) {
			
			String line = null;
			while ( ( line = br.readLine() ) != null ) {
				line = line.replace( "#VERSION#", Constants.CONVERSION_PROGRAM_VERSION );
				System.out.println( line );
			}
			
		} catch ( Exception e ) {
			System.out.println( "Error printing help." );
		}
	}
	
	public static void printHelp() {
		
		try( BufferedReader br = new BufferedReader( new InputStreamReader( MainProgram.class.getResourceAsStream( "help.txt" ) ) ) ) {
			
			String line = null;
			while ( ( line = br.readLine() ) != null )
				System.out.println( line );				
			
		} catch ( Exception e ) {
			System.out.println( "Error printing help." );
		}
	}
	
}
