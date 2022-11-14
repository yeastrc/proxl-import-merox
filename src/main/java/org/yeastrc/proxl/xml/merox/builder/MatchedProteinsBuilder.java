package org.yeastrc.proxl.xml.merox.builder;

import java.io.File;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.yeastrc.proteomics.fasta.FASTAEntry;
import org.yeastrc.proteomics.fasta.FASTAFileParser;
import org.yeastrc.proteomics.fasta.FASTAFileParserFactory;
import org.yeastrc.proteomics.fasta.FASTAHeader;
import org.yeastrc.proxl.xml.merox.parsed.ParsedPeptide;
import org.yeastrc.proxl.xml.merox.utils.IsotopeLabelUtils;
import org.yeastrc.proxl_import.api.xml_dto.MatchedProteins;
import org.yeastrc.proxl_import.api.xml_dto.Peptide;
import org.yeastrc.proxl_import.api.xml_dto.Peptides;
import org.yeastrc.proxl_import.api.xml_dto.Protein;
import org.yeastrc.proxl_import.api.xml_dto.ProteinAnnotation;
import org.yeastrc.proxl_import.api.xml_dto.ProxlInput;
import org.yeastrc.proxl_import.api.xml_dto.ReportedPeptide;
import org.yeastrc.proxl_import.api.xml_dto.ReportedPeptides;
import org.yeastrc.taxonomy.main.GetTaxonomyId;

/**
 * Build the MatchedProteins section of the ProXL XML docs. This is done by finding all proteins in the FASTA
 * file that contains any of the peptide sequences found in the experiment. 
 * 
 * This is generalized enough to be usable by any pipeline
 * 
 * @author mriffle
 */
public class MatchedProteinsBuilder {

	public static MatchedProteinsBuilder getInstance() { return new MatchedProteinsBuilder(); }
	
	/**
	 * Add all target proteins from the FASTA file that contain any of the peptides found in the experiment
	 * to the proxl xml document in the matched proteins sectioni.
	 * 
	 * @param proxlInputRoot
	 * @param fastaFile
	 * @param decoyPrefix
	 * @throws Exception
	 */
	public void buildMatchedProteins(ProxlInput proxlInputRoot, File fastaFile, String decoyPrefix, Collection<ParsedPeptide> distinctPeptides, String N15prefix ) throws Exception {

		// the proteins we've found
		Map<String, Collection<FastaProteinAnnotation>> proteins = getProteins( distinctPeptides, fastaFile, decoyPrefix );
		
		// create the XML and add to root element
		buildAndAddMatchedProteinsToXML( proxlInputRoot, proteins, N15prefix );
		
	}
	
	/**
	 * Do the work of building the matched peptides element and adding to proxl xml root
	 * 
	 * @param proxlInputRoot
	 * @param proteins
	 * @throws Exception
	 */
	private void buildAndAddMatchedProteinsToXML( ProxlInput proxlInputRoot, Map<String, Collection<FastaProteinAnnotation>> proteins, String N15prefix ) throws Exception {
		
		MatchedProteins xmlMatchedProteins = new MatchedProteins();
		proxlInputRoot.setMatchedProteins( xmlMatchedProteins );
		
		for( String sequence : proteins.keySet() ) {
			
			if( proteins.get( sequence ).isEmpty() ) continue;
			
			Protein xmlProtein = new Protein();
        	xmlMatchedProteins.getProtein().add( xmlProtein );
        	
        	xmlProtein.setSequence( sequence );
        	        	
        	for( FastaProteinAnnotation anno : proteins.get( sequence ) ) {
        		ProteinAnnotation xmlProteinAnnotation = new ProteinAnnotation();
        		xmlProtein.getProteinAnnotation().add( xmlProteinAnnotation );
        		
        		xmlProteinAnnotation.setName( anno.getName() );
        		
        		if( anno.getDescription() != null )
        			xmlProteinAnnotation.setDescription( anno.getDescription() );
        			
        		if( anno.getTaxonomyId() != null )
        			xmlProteinAnnotation.setNcbiTaxonomyId( new BigInteger( anno.getTaxonomyId().toString() ) );

				if(IsotopeLabelUtils.isLabeldProtein(anno.getName(), N15prefix)) {
					Protein.ProteinIsotopeLabels xProteinIsotopeLabels = new Protein.ProteinIsotopeLabels();
					xmlProtein.setProteinIsotopeLabels(xProteinIsotopeLabels);

					Protein.ProteinIsotopeLabels.ProteinIsotopeLabel xProteinIsotopeLabel = new Protein.ProteinIsotopeLabels.ProteinIsotopeLabel();
					xProteinIsotopeLabel.setLabel("15N");
					xProteinIsotopeLabels.setProteinIsotopeLabel(xProteinIsotopeLabel);
				}
        	}
		}
	}
	

	/**
	 * Get a map of the distinct target protein sequences mapped to a collection of target annotations for that sequence
	 * from the given fasta file, where the sequence contains any of the supplied peptide sequences
	 * 
	 * @param distinctPeptides
	 * @param fastaFile
	 * @param decoyPrefix
	 * @return
	 * @throws Exception
	 */
	private Map<String, Collection<FastaProteinAnnotation>> getProteins( Collection<ParsedPeptide> distinctPeptides, File fastaFile, String decoyPrefix ) throws Exception {
		
		Map<String, Collection<FastaProteinAnnotation>> proteinAnnotations = new HashMap<>();

		try ( FASTAFileParser parser = FASTAFileParserFactory.getInstance().getFASTAFileParser(fastaFile) ) {

			for ( FASTAEntry entry = parser.getNextEntry(); entry != null; entry = parser.getNextEntry() ) {

				if( isDecoyFastaEntry( entry, decoyPrefix ) )
					continue;
				
				for( FASTAHeader header : entry.getHeaders() ) {
					
					if( !proteinAnnotations.containsKey( entry.getSequence() ) )
						proteinAnnotations.put( entry.getSequence(), new HashSet<FastaProteinAnnotation>() );
					
					FastaProteinAnnotation anno = new FastaProteinAnnotation();
					anno.setName( header.getName() );
					anno.setDescription( header.getDescription() );
					
            		Integer taxId = GetTaxonomyId.getInstance().getTaxonomyId( header.getName(), header.getDescription() );
            		if( taxId != null )
            			anno.setTaxonomyId( taxId );
            		
					proteinAnnotations.get( entry.getSequence() ).add( anno );
				}
			}
		}
		return proteinAnnotations;
	}
	
	/**
	 * Return true if the supplied FASTA entry is a decoy entry. False otherwise.
	 * An entry is considered a decoy if any of the supplied decoy identifiers are present
	 * in any of the FASTA names
	 * 
	 * @param entry
	 * @param decoyPrefix
	 * @return
	 */
	private boolean isDecoyFastaEntry( FASTAEntry entry, String decoyPrefix ) {

		for( FASTAHeader header : entry.getHeaders() ) {
			if( header.getName().startsWith( decoyPrefix ) )
				return true;

		}

		return false;
	}
	
	
	/**
	 * Get all distinct peptides from a proxlxml doc's reported peptide section
	 * 
	 * @param proxlInputRoot
	 * @return
	 * @throws Exception
	 */
	private Collection<String> getDistinctPeptides( ProxlInput proxlInputRoot ) throws Exception {
		
		Collection<String> allPeptideSequences = new HashSet<>();
		
		ReportedPeptides reportedPeptides = proxlInputRoot.getReportedPeptides();
		
		if ( reportedPeptides != null ) {

			List<ReportedPeptide> reportedPeptideList = reportedPeptides.getReportedPeptide();
			
			if ( reportedPeptideList != null && ( ! reportedPeptideList.isEmpty() ) ) {
				
				for ( ReportedPeptide reportedPeptide : reportedPeptideList ) {
					
					if ( reportedPeptides != null ) {

						Peptides peptidesProxlXML = reportedPeptide.getPeptides();
						List<Peptide> peptideProxlXMLList = peptidesProxlXML.getPeptide();

						if ( peptideProxlXMLList != null && ( ! peptideProxlXMLList.isEmpty() ) ) {
							
							for ( Peptide peptideProxlXML : peptideProxlXMLList ) {
								
								allPeptideSequences.add( peptideProxlXML.getSequence() );
							}
						}
					}
				}
			}
		}
		
		
		return allPeptideSequences;
	}
	
	/**
	 * An annotation for a protein in a Fasta file
	 * 
	 * @author mriffle
	 *
	 */
	private class FastaProteinAnnotation {
		
		public int hashCode() {
			
			String hashString = this.getName();
			
			if( this.getDescription() != null )
				hashString += this.getDescription();
			
			if( this.getTaxonomyId() != null )
				hashString += this.getTaxonomyId().intValue();
			
			return hashString.hashCode();
		}
		
		/**
		 * Return true if name, description and taxonomy are all the same, false otherwise
		 */
		public boolean equals( Object o ) {
			try {
				
				FastaProteinAnnotation otherAnno = (FastaProteinAnnotation)o;
				
				if( !this.getName().equals( otherAnno.getName() ) )
					return false;
				
				
				if( this.getDescription() == null ) {
					if( otherAnno.getDescription() != null )
						return false;
				} else {
					if( otherAnno.getDescription() == null )
						return false;
				}
				
				if( !this.getDescription().equals( otherAnno.getDescription() ) )
					return false;
				
				
				if( this.getTaxonomyId() == null ) {
					if( otherAnno.getTaxonomyId() != null )
						return false;
				} else {
					if( otherAnno.getTaxonomyId() == null )
						return false;
				}
				
				if( !this.getTaxonomyId().equals( otherAnno.getTaxonomyId() ) )
					return false;
				
				
				return true;
				
			} catch( Exception e ) {
				return false;
			}
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public Integer getTaxonomyId() {
			return taxonomyId;
		}
		public void setTaxonomyId(Integer taxonomyId) {
			this.taxonomyId = taxonomyId;
		}

		private String name;
		private String description;
		private Integer taxonomyId;
		
	}
	
}
