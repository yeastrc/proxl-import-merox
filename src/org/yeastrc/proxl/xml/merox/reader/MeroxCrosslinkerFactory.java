package org.yeastrc.proxl.xml.merox.reader;

import java.util.ArrayList;
import java.util.HashSet;

public class MeroxCrosslinkerFactory {

    public static MeroxCrosslinker getMeroxCrosslinker( String linkerName ) {

        MeroxCrosslinker meroxCrosslinker = new MeroxCrosslinker();

        switch( linkerName ) {

            case "DSSO":

                meroxCrosslinker.setName("DSSO");

                meroxCrosslinker.setBindingRules(new ArrayList<>(2));
                meroxCrosslinker.getBindingRules().add("K{");
                meroxCrosslinker.getBindingRules().add("KSTY{");

                meroxCrosslinker.setFormula("C6O3SH6");

                meroxCrosslinker.setCleavedFormulae(new ArrayList<>(2));
                meroxCrosslinker.getCleavedFormulae().add("C3OH2");
                meroxCrosslinker.getCleavedFormulae().add("C3O2SH4");

                break;

        }

        return meroxCrosslinker;
    }

}
