package org.yeastrc.proxl.xml.merox.main;

import org.yeastrc.proxl.xml.merox.builder.XMLBuilder;
import org.yeastrc.proxl.xml.merox.constants.Constants;
import org.yeastrc.proxl.xml.merox.objects.MeroxAnalysis;
import org.yeastrc.proxl.xml.merox.reader.MeroxAnalysisLoader;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

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

	@CommandLine.Option(names = { "-o", "--out-file" }, required = true, description = "The full path to the desired output proxl XML file.")
	private File outFile;

	@CommandLine.Option(names = { "-s", "--scan-filename" }, required = false, description = "The name of the scan file (e.g., mydata.mzML) used to search the data. Used to annotate PSMs with the name of the scan file, required if using Bibliospec to create a spectral library for Skyline.")
	private String scanFilename;

	@CommandLine.Option(names = { "-a", "--scan-number-adjust" }, required = false, description = "(Optional) Adjust the reported scan numbers in the Limelight XML by this amount. E.g. -a -1 would subtract 1 from each scan number.")
	private int scanNumberAdjust = 0;

	@CommandLine.Option(names = { "--preserve-peptide-order" }, required = false, description = "If present, the order of peptides reported by MeroX will be preserved in cross-links. Otherwise the converter may change the order to ensure all cross-links are reported using the same peptide string (e.g. PEPTIDE1--PEPTIDE2.")
	private boolean preservePeptideOrder = false;

	@CommandLine.Option(names = { "--15N-prefix" }, required = false, description = "(Optional) Protein names with this prefix are considered 15N labeled. E.g., 15N_")
	private String N15prefix;

	@CommandLine.Option(names = { "-v", "--verbose" }, required = false, description = "If present, complete error messages will be printed. Useful for debugging errors.")
	private boolean verboseRequested = false;

	/**
	 * Do the data conversion and save the XML file
	 * 
	 * @param resultsFile The results file
	 * @param fastaFile The FASTA file
	 * @param scanFilename The name of the scan filename
	 * @param outputFile The output file
	 * 
	 * @throws Exception If there is a problem
	 */
	private void convertData(File resultsFile, File fastaFile, String scanFilename, File outputFile, String N15prefix, int scanNumberAdjust, boolean preservePeptideOrder) throws Exception {
		
		MeroxAnalysisLoader loader = new MeroxAnalysisLoader();
		MeroxAnalysis analysis = loader.loadMeroXAnalysis(resultsFile, N15prefix, preservePeptideOrder);
		
		XMLBuilder builder = new XMLBuilder();
		builder.buildAndSaveXML(analysis, fastaFile, scanFilename, outputFile, N15prefix, scanNumberAdjust );
		
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

		try {
			MainProgram mp = new MainProgram();
			mp.convertData(zhrmFile, fastaFile, scanFilename, outFile, N15prefix, scanNumberAdjust, preservePeptideOrder);

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
