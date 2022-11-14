package org.yeastrc.proxl.xml.merox.reader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.yeastrc.proxl.xml.merox.constants.MeroxConstants;
import org.yeastrc.proxl.xml.merox.objects.MeroxAnalysis;
import org.yeastrc.proxl.xml.merox.objects.Result;

/**
 * Interact with the zipped MeroX analysis file to load the data
 * from the experiment into a MeroxAnalysis object
 * 
 * @author mriffle
 *
 */
public class MeroxAnalysisLoader {

	public MeroxAnalysis loadMeroXAnalysis(File dataFile, String N15prefix) throws Exception {
		MeroxAnalysis sa = new MeroxAnalysis();
		
		ZipFile zipFile = null;	
		InputStream is = null;
		
		try {
		
			zipFile = new ZipFile( dataFile.getAbsolutePath() );

			
			// load the properties file
			ZipEntry zipEntry = zipFile.getEntry( MeroxConstants.PROPERTIES_FILENAME );

			// save contents of properties file
			is = zipFile.getInputStream(zipEntry);
			sa.setPropertiesFileContents(IOUtils.toByteArray(is));
			is.close();

			PropertiesReader pr = new PropertiesReader();
			AnalysisProperties ap = pr.getAnalysisProperties(sa.getPropertiesFileContents());
			sa.setAnalysisProperties( ap );

			// attempt to get the version
			zipEntry = zipFile.getEntry( MeroxConstants.REPORT_FILENAME );
			try {
				sa.setVersion(ReportFileReader.getMeroXVersion(zipFile.getInputStream(zipEntry)));
			} catch(Exception e) {
				sa.setVersion("2 (exact version unknown");
			}

			// load the PSM data
			zipEntry = zipFile.getEntry( MeroxConstants.PSM_ANNOTATIONS_FILENAME );

			is = zipFile.getInputStream( zipEntry );
			
			ResultsReader rr = new ResultsReader();
			List<Result> analysisResults = rr.getAnalysisResults( is, N15prefix );
			is.close();
			
			sa.setAnalysisResults( analysisResults );

			
		} finally {
			try { is.close(); }
			catch( Throwable t ) { ; }
			
			try { zipFile.close(); }
			catch( Throwable t ) { ; }
		}
		
		

		
		return sa;
		
	}
	
	
}
