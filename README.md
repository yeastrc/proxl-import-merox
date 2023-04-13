MeroX 2 to Proxl XML Converter
=============================

Use this program to convert the results of a MeroX 2 (https://www.stavrox.com/) analysis to Proxl XML suitable for import into the proxl web application.

How To Run
-------------
1. Download the [latest release](https://github.com/yeastrc/proxl-import-merox/releases).
2. Run the program ``java -jar merox2ProxlXML.jar`` with no arguments to see the possible parameters.
3. Run the program, e.g., ``java -jar merox2ProxlXML.jar -r ./results.zhrm -f ./yeast.fa -o results.xml``

In the above example, ``results.xml`` will be created and be suitable for import into ProXL.

For more information on importing data into Proxl, please see the [Proxl Import Documentation](http://proxl-web-app.readthedocs.io/en/latest/using/upload_data.html).

Note about 15N labelled data
-----------------------------
For Proxl to correctly handle 15N-labelled data, the FASTA file entries for 15N labeled proteins must all begin with the same prefix. For example, the non-labeled
alpha tubulin FASTA entry may begin with ">alpha_tubulin", whereas 15N alpha tubulin entry would begin with ">15N_alpha_tublin".
All entries for 15N labelled proteins would have to begin with "15N_". This "15N_" prefix can be whatever you like, but it
must be consistent and uniquely identify 15N labelled FASTA protein entries.

Then when running this converter, this "15N" prefix must be passed in using the `--15N-prefix=` parameter. For example:
``java -jar merox2ProxlXML.jar -r ./results.zhrm -f ./yeast.fa --15N-prefix=15N_ -o results.xml``.

More Information About Proxl
-----------------------------
For more information about Proxl, visit http://proxl-ms.org/.

Command line documentation
---------------------------

```
java -jar merox2ProxlXML.jar [-hvV] [--preserve-peptide-order]
                             [--15N-prefix=<N15prefix>] [-a=<scanNumberAdjust>]
                             -f=<fastaFile> -o=<outFile> -r=<zhrmFile>
                             [-s=<scanFilename>]

Description:

Convert the results of a MeroX analysis to a Proxl XML file suitable for import
into Proxl.

More info at: https://github.com/yeastrc/proxl-import-merox

Options:
  -r, --zhrm-file=<zhrmFile> The full path to the results file from the MeroX
                               analysis.
  -f, --fasta-file=<fastaFile>
                             The full path to the FASTA file used in the MeroX
                               analysis.
  -o, --out-file=<outFile>   The full path to the desired output proxl XML file.
  -s, --scan-filename=<scanFilename>
                             The name of the scan file (e.g., mydata.mzML) used to
                               search the data. Used to annotate PSMs with the name
                               of the scan file, required if using Bibliospec to
                               create a spectral library for Skyline.
  -a, --scan-number-adjust=<scanNumberAdjust>
                             (Optional) Adjust the reported scan numbers in the
                               Limelight XML by this amount. E.g. -a -1 would
                               subtract 1 from each scan number.
      --preserve-peptide-order
                             If present, the order of peptides reported by MeroX
                               will be preserved in cross-links. Otherwise the
                               converter may change the order to ensure all
                               cross-links are reported using the same peptide
                               string (e.g. PEPTIDE1--PEPTIDE2.
      --15N-prefix=<N15prefix>
                             (Optional) Protein names with this prefix are
                               considered 15N labeled. E.g., 15N_
  -v, --verbose              If present, complete error messages will be printed.
                               Useful for debugging errors.
  -h, --help                 Show this help message and exit.
  -V, --version              Print version information and exit.
```
