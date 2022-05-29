package org.yeastrc.proxl.xml.merox.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

	public static final int NUMBER_DECIMAL_PLACES = 4;

	/**
	 * Get a big decimal rounded out to NUMBER_DECIMAL_PLACES places
	 * from the supplied double.
	 * 
	 * @param value
	 * @return
	 */
	public static BigDecimal getRoundedBigDecimal( double value ) {
		BigDecimal bd = new BigDecimal( value );
		bd = bd.setScale( NUMBER_DECIMAL_PLACES, RoundingMode.HALF_UP );
		
		return bd;
	}
	
}
