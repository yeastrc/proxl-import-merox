package org.yeastrc.proxl.xml.merox.constants;

public class LinkerConstants {

	/**
	 * These are the linkers proxl knows how to handle that merox knows how to handle, as of this writing. Logic for handling cross-linkers (binding rules) should
	 * be moved into the XML schema in the future.
	 */
	public static final String[] VALID_LINKERS = new String[]{ "dss", "bs3", "edc", "bs2", "dss.sty", "bs3.sty", "sulfo-smcc", "dsso", "tg" };
	
	
}
