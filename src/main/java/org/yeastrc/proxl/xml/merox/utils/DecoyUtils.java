package org.yeastrc.proxl.xml.merox.utils;

import org.yeastrc.proxl.xml.merox.objects.Result;
import org.yeastrc.proxl.xml.merox.reader.AnalysisProperties;

public class DecoyUtils {

    /**
     * If either of the proteins contained in this result maps to a decoy protein return true
     *
     * @param result
     * @param analysisProperties
     * @return
     */
    public static boolean isDecoyResult(Result result, AnalysisProperties analysisProperties) {
        return isDecoyProteinName(result.getProteins1(), analysisProperties) ||
                isDecoyProteinName(result.getProteins2(), analysisProperties);
    }

    /**
     * If the protein name is a decoy (begins with the decoy prefix) return true
     *
     * @param proteinName
     * @param analysisProperties
     * @return
     */
    public static boolean isDecoyProteinName(String proteinName, AnalysisProperties analysisProperties) {
        if(analysisProperties.getDecoyPrefix() == null || analysisProperties.getDecoyPrefix().length() < 1)
            return false;

        if(proteinName == null)
            return false;

        return proteinName.startsWith(analysisProperties.getDecoyPrefix());
    }

}
