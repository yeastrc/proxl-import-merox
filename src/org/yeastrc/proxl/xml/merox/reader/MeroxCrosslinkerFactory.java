package org.yeastrc.proxl.xml.merox.reader;

import java.util.ArrayList;
import java.util.HashSet;

//USEDCROSSLINKER=DSSO				//Cross-linker that is selected from the following list.
//
//        CROSSLINKER=BS3/DSS-D0/D12
//        COMPOSITION=C8H10O2
//        COMPHEAVY=C8D12O2-H2
//        SITE1=K{
//        SITE2=KSTY{
//        MAXIMUMDISTANCE=23.8
//        RETENTIONDIFF=40
//        MODSITE1
//        Pep;;0
//        MODSITE2
//        Pep;;0
//        END
//
//        CROSSLINKER=DSBU
//        COMPOSITION=C9O3N2H12
//        COMPHEAVY=
//        SITE1=K{
//        SITE2=KSTY{
//        MAXIMUMDISTANCE=26.9
//        CNL=C4H7NO
//        DEADENDMOLECULE=H2O
//        REPORTERIONS
//        RBu;C9N2OH17
//        RBuUr;C10N2O2H15
//        MODSITE1
//        Bu;C4NOH7;1
//        BuUr;C5O2NH5;1
//        Pep;;0
//        MODSITE2
//        Bu;C4NOH7;1
//        BuUr;C5O2NH5;1
//        Pep;;0
//        END

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
