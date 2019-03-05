package org.yeastrc.proxl.xml.merox.reader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;

/**
 * Interact with the zipped StavroX analysis file to load the data
 * from the experiment into a MeroxAnalysis object
 * 
 * @author mriffle
 *
 */
public class MeroxAnalysisLoader {

	public MeroxAnalysis loadStavroxAnalysis(File dataFile ) throws Exception {
		MeroxAnalysis sa = new MeroxAnalysis();
		
		ZipFile zipFile = null;	
		InputStream is = null;
		
		try {
		
			zipFile = new ZipFile( dataFile.getAbsolutePath() );

			
			// load the properties file
			ZipEntry zipEntry = zipFile.getEntry( MeroxConstants.PROPERTIES_FILENAME );

			is = zipFile.getInputStream( zipEntry );
			
			PropertiesReader pr = new PropertiesReader();
			AnalysisProperties ap = pr.getAnalysisProperties( is );
			is.close();
			
			sa.setAnalysisProperties( ap );

			// save contents of properties file
			is = zipFile.getInputStream( zipEntry );
			sa.setPropertiesFileContents( IOUtils.toByteArray(is)  );
			
			// load the PSM data
			zipEntry = zipFile.getEntry( MeroxConstants.PSM_ANNOTATIONS_FILENAME );

			is = zipFile.getInputStream( zipEntry );
			
			ResultsReader rr = new ResultsReader();
			List<Result> analysisResults = rr.getAnalysisResults( is );
			is.close();
			
			sa.setAnalysisResults( analysisResults );
			
			// load the decoy data
			zipEntry = zipFile.getEntry( MeroxConstants.DECOY_FILENAME );
			if( zipEntry != null ) {
				is = zipFile.getInputStream( zipEntry );
				DecoyHandler handler = new DecoyHandler();
				handler.readDecoys( is );
				is.close();
				
				sa.setDecoyHandler( handler );
			} else {
				throw new Exception( "Decoy file was not found." );
			}	

			
		} finally {
			try { is.close(); }
			catch( Throwable t ) { ; }
			
			try { zipFile.close(); }
			catch( Throwable t ) { ; }
		}
		
		

		
		return sa;
		
	}
	
	
}
