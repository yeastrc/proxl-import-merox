package org.yeastrc.proxl.xml.merox.utils;

import org.yeastrc.proxl.xml.merox.parsed.ParsedPeptideModification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModUtils {

    public static Map<Integer, Collection<Double>> convertModCollectionToMap(Collection<ParsedPeptideModification> modCollection) {
        Map<Integer, Collection<Double>> modMap = new HashMap<>();

        if(modCollection == null || modCollection.size() < 1) {
            return modMap;
        }

        for(ParsedPeptideModification parsedMod : modCollection) {
            if(!modMap.containsKey(parsedMod.getPosition())) {
                modMap.put(parsedMod.getPosition(), new ArrayList<>());
            }

            modMap.get(parsedMod.getPosition()).add(parsedMod.getMass());
        }

        return modMap;
    }

}
