package org.yeastrc.proxl.xml.merox.reader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yeastrc.proxl.xml.merox.mods.MeroxStaticModification;
import org.yeastrc.proxl.xml.merox.mods.MeroxVariableModification;
import org.yeastrc.proxl.xml.merox.objects.LinkablePosition;
import org.yeastrc.proxl.xml.merox.objects.MeroxAminoAcid;
import org.yeastrc.proxl.xml.merox.objects.MeroxCrosslinker;
import org.yeastrc.proxl.xml.merox.objects.Molecule;
import org.yeastrc.proxl.xml.merox.utils.MassUtils;

/**
 * Read the properties out of a properties file in the results file of a MeroX analysis
 * @author mriffle
 *
 */
public class PropertiesReader {

	/**
	 * Get the cross-linker for this experiment as defined in the properties file
	 *
	 * @param propertiesFileContents InputStream containing config file
	 * @return The populated MeroxCrosslinker
	 * @throws Exception If there is a problem
	 */
	public MeroxCrosslinker getCrosslinkerFromProperties(byte[] propertiesFileContents, Map<String, Double> elementsDefinition) throws Exception {

		String linkerName = this.getCrosslinkerNameFromProperties( new ByteArrayInputStream(propertiesFileContents) );
		return this.getCrosslinkerForNameFromProperties( new ByteArrayInputStream(propertiesFileContents), linkerName, elementsDefinition);

	}

	/**
	 * Get the Merox name for the crosslinker used in this experiment which can
	 * then be used to determine cross-linker properties as defined in the
	 * properties file.
	 *
	 * Syntax:
	 * USEDCROSSLINKER=DSSO				//Cross-linker that is selected from the following list.
	 *
	 * @param is InputStream containing config file
	 * @return The name of the used crosslinker
	 * @throws Exception If there is a problem
	 */
	public String getCrosslinkerNameFromProperties( InputStream is ) throws Exception {

		Pattern p = Pattern.compile( "USEDCROSSLINKER=(.+)$" );

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				Matcher m = p.matcher(currentLine);
				if (m.matches()) {
					return m.group(1);
				}
			}

		}

		throw new Exception( "Unable to determine cross-linker from Merox properties." );
	}

	/**
	 * Get a cross-linker object populated with the properties of the supplied cross-linker as
	 * defined in the properties file.
	 *
	 * Syntax:
	 *         CROSSLINKER=DSBU
	 *         COMPOSITION=C9O3N2H12
	 *         COMPHEAVY=
	 *         SITE1=K{
	 *         SITE2=KSTY{
	 *         MAXIMUMDISTANCE=26.9
	 *         CNL=C4H7NO
	 *         DEADENDMOLECULE=H2O
	 *         REPORTERIONS
	 *         RBu;C9N2OH17
	 *         RBuUr;C10N2O2H15
	 *         MODSITE1
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         MODSITE2
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         END
	 *
	 * @param is InputStream containing config file
	 * @param name The name of the cross-linker
	 * @return The populated MeroxCrosslinker
	 * @throws Exception If there is a problem
	 */
	public MeroxCrosslinker getCrosslinkerForNameFromProperties(InputStream is, String name, Map<String, Double> elementsDefinition) throws Exception {

		Pattern p = Pattern.compile( "CROSSLINKER=(.+)$" );
		ArrayList<String> linkerDefinitionLines = new ArrayList<>(25 );
		boolean readingLinkerDefinition = false;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				Matcher m = p.matcher(currentLine);
				if (m.matches()) {

					if (m.group(1).equals(name)) {
						linkerDefinitionLines.add(currentLine);
						readingLinkerDefinition = true;
					}
				} else if (readingLinkerDefinition) {

					if (currentLine.equals("END")) {

						return getCrosslinkerFromStringArray(linkerDefinitionLines, elementsDefinition);
					} else {

						linkerDefinitionLines.add(currentLine);
					}
				}

			}

		}

		throw new Exception( "Unable to find cross-linker definition for linker: " + name );
	}

	/**
	 * Get the crosslinker defined by the supplied array of lines from the properties file
	 *
	 * Syntax:
	 *         CROSSLINKER=DSBU
	 *         COMPOSITION=C9O3N2H12
	 *         COMPHEAVY=
	 *         SITE1=K{
	 *         SITE2=KSTY{
	 *         MAXIMUMDISTANCE=26.9
	 *         CNL=C4H7NO
	 *         DEADENDMOLECULE=H2O
	 *         REPORTERIONS
	 *         RBu;C9N2OH17
	 *         RBuUr;C10N2O2H15
	 *         MODSITE1
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         MODSITE2
	 *         Bu;C4NOH7;1
	 *         BuUr;C5O2NH5;1
	 *         Pep;;0
	 *         END
	 *
	 * @param linkerDefinitionLines The lines from the param file for a single crosslinker definition
	 * @param elementsDefinition The parsed elements definition from the config file
	 * @return The populated MeroxCrosslinker
	 * @throws Exception If there is a problem
	 */
	public MeroxCrosslinker getCrosslinkerFromStringArray( ArrayList<String> linkerDefinitionLines, Map<String, Double> elementsDefinition) throws Exception {

		MeroxCrosslinker crosslinker = new MeroxCrosslinker();
		crosslinker.setCleavedMolecules(new HashSet<>() );

		boolean readingModSite = false;

		for( String line : linkerDefinitionLines ) {

			if( line.equals( "MODSITE1" ) ) {
				readingModSite = true;
			} else if( line.equals( "MODSITE2" ) ) {
				readingModSite = true;
			} else if( readingModSite ) {

				if( line.equals( "END" ) ) {
					readingModSite = false;
				} else {

					String[] subFields = line.split(";");

					if (subFields.length != 3) {
						throw new Exception("Unexpected syntax for MODSITE definition. Got " + line);
					}

					// only include non empty formulae
					if (subFields[1].length() > 0) {
						crosslinker.getCleavedMolecules().add(
								new Molecule(
										MassUtils.getMassFromFormula(subFields[1], elementsDefinition),
										subFields[1]
								)
						);
					}
				}
			} else {

				String[] fields = line.split("=");

				switch (fields[0]) {
					case "CROSSLINKER":
						crosslinker.setName(fields[1]);
						break;
					case "COMPOSITION":
						crosslinker.setFullLengthMolecule(
								new Molecule(
										MassUtils.getMassFromFormula(fields[1], elementsDefinition),
										fields[1]
								)
						);
						break;
					case "SITE1":
						crosslinker.setLinkablePosition1(getLinkablePositionForBindingRules(fields[1]));
						break;
					case "SITE2":
						crosslinker.setLinkablePosition2(getLinkablePositionForBindingRules(fields[1]));
						break;
					case "MAXIMUMDISTANCE":
						crosslinker.setSpacerArmLength(new BigDecimal(fields[1]));
						break;
				}
			}
		}

		return crosslinker;
	}

	/**
	 * Get the LinkablePosition defined by the binding rule using the syntax from the config file
	 * @param bindingRules The binding rule given as a string such as KR{
	 * @return The populated LinkablePosition
	 * @throws Exception If there is a problem
	 */
	public LinkablePosition getLinkablePositionForBindingRules(String bindingRules) throws Exception {
		LinkablePosition linkablePosition = new LinkablePosition();
		linkablePosition.setResidues(new HashSet<>());

		if(bindingRules == null || bindingRules.length() < 1) {
			throw new Exception("Got empty binding rules fro cross-linker definition.");
		}

		for (int i = 0; i < bindingRules.length(); i++) {

			String residue = String.valueOf(bindingRules.charAt(i));

			if(residue.equals("{")) {
				linkablePosition.setProteinNTerminus(true);
			} else if(residue.equals("}")) {
				linkablePosition.setProteinCTerminus(true);
			} else {
				linkablePosition.getResidues().add(residue.toUpperCase());
			}
		}

		return linkablePosition;
	}


	/**
	 * Load the elements in the ELEMENTS section
	 *
	 * @param is InputStream containing config file
	 * @return The parsed element definitions
	 * @throws Exception If there is a problem
	 */
	public HashMap<String, Double> getElementsFromPropertiesFileContents(InputStream is) throws Exception {

		HashMap<String, Double> elementsMap = new HashMap<>();
		boolean readingElements = false;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				if (currentLine.equals("ELEMENTS")) {
					readingElements = true;

				} else if (readingElements) {

					if (currentLine.equals("END")) {

						return elementsMap;
					} else {
						String[] fields = currentLine.split(";");
						elementsMap.put(fields[0], Double.valueOf(fields[1]));
					}
				}
			}

		}

		throw new Exception("Unable to find ELEMENTS section in parameters file.");
	}

	/**
	 * Load the elements in the AMINOACIDS section
	 *
	 * @param is InputStream containing config file
	 * @return The parsed amino acids
	 * @throws Exception If there is a problem
	 */
	public HashMap<String, MeroxAminoAcid> getAminoAcidsFromPropertiesFileContents(InputStream is) throws Exception {

		HashMap<String, MeroxAminoAcid> aminoAcidMap = new HashMap<>();
		boolean readingAminoAcids = false;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				if (currentLine.equals("AMINOACIDS")) {
					readingAminoAcids = true;

				} else if (readingAminoAcids) {

					if (currentLine.equals("END")) {

						return aminoAcidMap;
					} else {
						String[] fields = currentLine.split(";");
						MeroxAminoAcid meroxAminoAcid = new MeroxAminoAcid(fields[1], fields[0], fields[2]);
						aminoAcidMap.put(fields[1], meroxAminoAcid);
					}
				}
			}

		}

		throw new Exception("Unable to find AMINOACIDS section in parameters file.");
	}

	/**
	 * Load the elements in the VARMODIFICATION section
	 *
	 * @param is InputStream containing config file
	 * @return The parsed variable mods
	 * @throws Exception If there is a problem
	 */
	public HashMap<String, MeroxVariableModification> getVariableModsFromPropertiesFileContents(InputStream is) throws Exception {

		HashMap<String, MeroxVariableModification> modMap = new HashMap<>();
		boolean readingModInfo = false;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				if (currentLine.equals("VARMODIFICATION")) {
					readingModInfo = true;

				} else if (readingModInfo) {

					if (currentLine.equals("END")) {

						return modMap;
					} else {
						String[] fields = currentLine.split(";");

						MeroxVariableModification mod = new MeroxVariableModification();
						mod.setFrom(fields[0]);
						mod.setTo(fields[1]);
						mod.setMaxModifications(Integer.parseInt(fields[2]));

						modMap.put(fields[1], mod);
					}
				}
			}

		}

		throw new Exception("Unable to find VARMODIFICATION section in parameters file.");
	}

	/**
	 * Load the value for the DECOYIDENTIFIER key
	 *
	 * @param is InputStream containing config file
	 * @return The parsed decoyprefix
	 * @throws Exception If there is a problem
	 */
	public String getDecoyPrefixFromProperties(InputStream is) throws Exception {

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				if (currentLine.startsWith("DECOYIDENTIFIER")) {
					String[] fields = currentLine.split("=");
					return fields[1];
				}
			}

		}

		throw new Exception("Unable to find DECOYIDENTIFIER section in parameters file.");
	}

	/**
	 * Load the elements in the STATMODIFICATION section
	 *
	 * @param is InputStream containing config file
	 * @return The parsed static mods
	 * @throws Exception If there is a problem
	 */
	public HashMap<String, MeroxStaticModification> getStaticModsFromPropertiesFileContents(InputStream is) throws Exception {

		HashMap<String, MeroxStaticModification> modMap = new HashMap<>();
		boolean readingModInfo = false;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

			String currentLine;        // the line we're currently parsing
			while ((currentLine = br.readLine()) != null) {

				// remove comment and trim
				currentLine = removeComment(currentLine);

				if (currentLine.equals("STATMODIFICATION")) {
					readingModInfo = true;

				} else if (readingModInfo) {

					if (currentLine.equals("END")) {

						return modMap;
					} else {
						String[] fields = currentLine.split(";");

						MeroxStaticModification mod = new MeroxStaticModification();
						mod.setFrom(fields[0]);
						mod.setTo(fields[1]);

						modMap.put(fields[1], mod);
					}
				}
			}

		}

		throw new Exception("Unable to find STATMODIFICATION section in parameters file.");
	}

	public AnalysisProperties getAnalysisProperties(byte[] propertiesFileContents) throws Exception {

		AnalysisProperties ap = new AnalysisProperties();

		// load the elements
		ap.setElements(getElementsFromPropertiesFileContents(new ByteArrayInputStream(propertiesFileContents)));

		// load the aminoacids
		ap.setAminoAcids(getAminoAcidsFromPropertiesFileContents(new ByteArrayInputStream(propertiesFileContents)));

		// load the variable mods
		ap.setVariableMods(getVariableModsFromPropertiesFileContents(new ByteArrayInputStream(propertiesFileContents)));

		// load the static mods
		ap.setStaticMods(getStaticModsFromPropertiesFileContents(new ByteArrayInputStream(propertiesFileContents)));

		// load the cross-linker definition
		ap.setCrosslinker(getCrosslinkerFromProperties(propertiesFileContents, ap.getElements()));

		// load the decoy prefix string
		ap.setDecoyPrefix(getDecoyPrefixFromProperties(new ByteArrayInputStream(propertiesFileContents)));
		
		return ap;
	}

	/**
	 * Remove any comment from a line (e.g., "//" and all following characters) and then trim() the line
	 * to remove all white space on either end.
	 *
	 * @param line The string containing a line from the config file
	 * @return The comment-less and trimmed line
	 */
	private static String removeComment( String line ) {
		return line.replaceAll( "\\/\\/.+", "" ).trim();
	}

}
