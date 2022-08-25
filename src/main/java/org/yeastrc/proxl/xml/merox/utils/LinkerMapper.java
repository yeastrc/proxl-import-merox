package org.yeastrc.proxl.xml.merox.utils;

import java.util.HashMap;
import java.util.Map;

public class LinkerMapper {

	private static final Map<String, String> linkerMap = new HashMap<String, String>();

	static {
		linkerMap.put( "dss", "DSS/BS3" );
		linkerMap.put( "bs3", "DSS/BS3" );
		linkerMap.put( "dss.sty", "DSS/BS3" );
		linkerMap.put( "bs3.sty", "DSS/BS3" );
		linkerMap.put( "edc", "EDC" );
		linkerMap.put( "bs2", "BS2G" );
		linkerMap.put( "sulfo-smcc", "Sulfo-SMCC");
		linkerMap.put( "dsso", "DSSO" );
		linkerMap.put( "tg", "TG" );
	}

	public static Map<String, String> getLinkerMap() {
		return linkerMap;
	}

	/**
	 * Get the name of the merox crosslinker (found in the properties file) for the
	 * proxl-based crosslinker abbreviation.
	 * 
	 * @param proxlAbbr
	 * @return
	 * @throws Exception
	 */
	public static String getMeroXCrosslinkerName( String proxlAbbr ) throws Exception {
		
		if( linkerMap.containsKey( proxlAbbr ) )
			return linkerMap.get( proxlAbbr );
		
		throw new Exception( "Unsupported linker name: " + proxlAbbr );

	}
}
