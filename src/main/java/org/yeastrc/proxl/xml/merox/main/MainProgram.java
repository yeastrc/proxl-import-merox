package org.yeastrc.proxl.xml.merox.main;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.yeastrc.proxl.xml.merox.builder.XMLBuilder;
import org.yeastrc.proxl.xml.merox.constants.ConverterConstants;
import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;
import org.yeastrc.proxl.xml.merox.reader.MeroxAnalysis;
import org.yeastrc.proxl.xml.merox.reader.MeroxAnalysisLoader;
import org.yeastrc.proxl.xml.merox.utils.LinkerMapper;

/**
 * Convert a StavroX data file to a ProXL XML file
 * @author mriffle
 *
 */
public class MainProgram {

	/**
	 * Do the data conversion and save the XML file
	 * 
	 * @param filename The path of the results file
	 * @param linkerName The name of the cross-linker
	 * @param fastaFilename The name of the FASTA file
	 * @param scanFilename The name of the scan filename
	 * @param scanNumberAdjustment The amount to adjust the reported scan numbers by
	 * @param outputFilename The path of the output file
	 * 
	 * @throws Exception If there is a problem
	 */
	private void convertData( String filename, String linkerName, String fastaFilename, String scanFilename, int scanNumberAdjustment, String outputFilename, BigDecimal importCutoff ) throws Exception {
		
		File file = new File( filename );
		MeroxAnalysisLoader loader = new MeroxAnalysisLoader();
		
		MeroxAnalysis analysis = loader.loadStavroxAnalysis( file );
		
		// ensure the linker they entered maps to a linker in the properties file
		String stavroxLinker = LinkerMapper.getStavroxCrosslinkerName( linkerName );
		if( !stavroxLinker.equals( analysis.getAnalysisProperties().getCrosslinker().getName() ) ) {
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
		builder.buildAndSaveXML(analysis, linkerName, fastaFilename, scanFilename, scanNumberAdjustment, new File( outputFilename ), importCutoff );
		
	}
	
	
	public static void main(String[] args ) throws Exception {
		
		printStartup();
		
		if( args.length == 0 ) {
			printHelp();
			System.exit( 0 );
		}
		
		CmdLineParser cmdLineParser = new CmdLineParser();
        
		CmdLineParser.Option resultsFilenameOpt = cmdLineParser.addStringOption( 'r', "results" );	
		CmdLineParser.Option linkerOpt = cmdLineParser.addStringOption( 'l', "linker" );	
		CmdLineParser.Option fastaOpt = cmdLineParser.addStringOption( 'f', "fasta_file" );	
		CmdLineParser.Option scanFileWithPathCommandLineOpt = cmdLineParser.addStringOption( 's', "scan_file" );
		CmdLineParser.Option outputFilenameOpt = cmdLineParser.addStringOption( 'o', "output_file" );
		CmdLineParser.Option scanNumberAdjustmentOpt = cmdLineParser.addIntegerOption( 'a', "scan_adjust" );
		CmdLineParser.Option importFilterCutoffOpt = cmdLineParser.addStringOption( 'i', "import_cutoff" );

        // parse command line options
        try { cmdLineParser.parse(args); }
        catch (IllegalOptionValueException e) {
        	printHelp();
            System.exit( 1 );
        }
        catch (UnknownOptionException e) {
           printHelp();
           System.exit( 1 );
        }
		
        
        String linkerName = (String)cmdLineParser.getOptionValue( linkerOpt );
        String resultsFilename = (String)cmdLineParser.getOptionValue( resultsFilenameOpt );
        String fastaFilename = (String)cmdLineParser.getOptionValue( fastaOpt );
        String scanFilename = (String)cmdLineParser.getOptionValue( scanFileWithPathCommandLineOpt );
        String outputFilename = (String)cmdLineParser.getOptionValue( outputFilenameOpt );
        
        
        BigDecimal importCutoff = null;
        String importCutoffString = (String)cmdLineParser.getOptionValue( importFilterCutoffOpt );
        
        if( importCutoffString != null ) {
	        try { importCutoff = new BigDecimal( importCutoffString ); }
	        catch( Exception e ) {
	        	System.err.println( "Expected a number for the import cutoff filter, got: " + importCutoffString );
	        	System.exit( 1 );
	        }
        }
        
        if( importCutoff == null )
        	importCutoff = new BigDecimal( MeroxConstants.DEFAULT_IMPORT_CUTOFF );

        Integer scanNumberAdjustment = (Integer)cmdLineParser.getOptionValue( scanNumberAdjustmentOpt );
        if( scanNumberAdjustment == null ) scanNumberAdjustment = 0;
		
		MainProgram mp = new MainProgram();
		mp.convertData( resultsFilename, linkerName, fastaFilename, scanFilename, scanNumberAdjustment, outputFilename, importCutoff );
		
		
		
	}
	
	public static void printStartup() {
		
		try( BufferedReader br = new BufferedReader( new InputStreamReader( MainProgram.class.getResourceAsStream( "startup.txt" ) ) ) ) {
			
			String line = null;
			while ( ( line = br.readLine() ) != null ) {
				line = line.replace( "#VERSION#", ConverterConstants._CONVERTER_VERSION );
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
