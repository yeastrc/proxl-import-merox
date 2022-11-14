package org.yeastrc.proxl.xml.merox.utils;

public class IsotopeLabelUtils {

    /**
     * Return true if the supplied protein name is an isotope labeled protein
     *
     * @param proteinName
     * @param labelPrefix
     * @return
     */
    public static boolean isLabeldProtein(String proteinName, String labelPrefix) {
        return labelPrefix != null && (proteinName.startsWith(labelPrefix) || proteinName.startsWith(">" + labelPrefix));
    }

}
