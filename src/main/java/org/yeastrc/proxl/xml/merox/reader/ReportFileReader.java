package org.yeastrc.proxl.xml.merox.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReportFileReader {

    public static String getMeroXVersion(InputStream reportFileInputStream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(reportFileInputStream, StandardCharsets.ISO_8859_1))) {

            String currentLine;        // the line we're currently parsing
            while ((currentLine = br.readLine()) != null) {
                currentLine = currentLine.trim();

                if (currentLine.startsWith("MeroX-Version:")) {
                    String[] fields = currentLine.split("\\s+");
                    if (fields.length == 2) {
                        return fields[1];
                    }
                }
            }
        }

        return "2 (exact version unknown)";
    }
}
