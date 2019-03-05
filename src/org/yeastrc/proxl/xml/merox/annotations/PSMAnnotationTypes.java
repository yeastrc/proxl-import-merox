package org.yeastrc.proxl.xml.merox.annotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.proxl_import.api.xml_dto.DescriptivePsmAnnotationType;
import org.yeastrc.proxl_import.api.xml_dto.FilterDirectionType;
import org.yeastrc.proxl_import.api.xml_dto.FilterablePsmAnnotationType;

public class PSMAnnotationTypes {

	public static final String ANNOTATION_TYPE_SCORE = "score";
	public static final String ANNOTATION_TYPE_MOVERZ = "m/z";
	public static final String ANNOTATION_TYPE_OBSERVED_MASS = "obs. mass";
	public static final String ANNOTATION_TYPE_CANDIDATE_MASS = "cand. mass";
	public static final String ANNOTATION_TYPE_DEVIATION = "deviation";
	public static final String ANNOTATION_TYPE_SCAN_NUMBER = "scan num.";
	public static final String ANNOTATION_TYPE_RANK = "rank";
	public static final String ANNOTATION_TYPE_FDR = "FDR";
	
	/**
	 * Get the list of filterable PSM annotation types in StavroX data
	 * @return
	 */
	public static List<FilterablePsmAnnotationType> getFilterablePsmAnnotationTypes() {
		List<FilterablePsmAnnotationType> types = new ArrayList<FilterablePsmAnnotationType>();

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_SCORE );
			type.setDescription( "StavroX Score" );
			type.setDefaultFilterValue( new BigDecimal( "50" ) );
			type.setDefaultFilter( false );
			type.setFilterDirection( FilterDirectionType.ABOVE );
			
			types.add( type );
		}

		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_FDR );
			type.setDescription( "False discovery rate associated with this PSM's score" );
			type.setDefaultFilterValue( new BigDecimal( "0.01" ) );
			type.setDefaultFilter( true );
			type.setFilterDirection( FilterDirectionType.BELOW );
			
			types.add( type );
		}
		
		{
			FilterablePsmAnnotationType type = new FilterablePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_RANK );
			type.setDescription( "Rank of PSM for scan" );
			type.setDefaultFilterValue( new BigDecimal( "1" ) );
			type.setDefaultFilter( true );
			type.setFilterDirection( FilterDirectionType.BELOW );
			
			types.add( type );
		}
		
		return types;
	}
	
	/**
	 * Get the list of descriptive (non-filterable) PSM annotation types in StavroX data
	 * @return
	 */
	public static List<DescriptivePsmAnnotationType> getDescriptivePsmAnnotationTypes() {
		List<DescriptivePsmAnnotationType> types = new ArrayList<DescriptivePsmAnnotationType>();
		
		{
			DescriptivePsmAnnotationType type = new DescriptivePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_MOVERZ );
			type.setDescription( type.getName() );
			
			types.add( type );
		}
		
		{
			DescriptivePsmAnnotationType type = new DescriptivePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_OBSERVED_MASS );
			type.setDescription( type.getName() );
			
			types.add( type );
		}

		{
			DescriptivePsmAnnotationType type = new DescriptivePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_CANDIDATE_MASS );
			type.setDescription( type.getName() );
			
			types.add( type );
		}
		
		{
			DescriptivePsmAnnotationType type = new DescriptivePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_DEVIATION );
			type.setDescription( type.getName() );
			
			types.add( type );
		}
		
		{
			DescriptivePsmAnnotationType type = new DescriptivePsmAnnotationType();
			type.setName( ANNOTATION_TYPE_SCAN_NUMBER );
			type.setDescription( type.getName() );
			
			types.add( type );
		}
		
		return types;		
	}
	
}
