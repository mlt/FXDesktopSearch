/**
 * FreeDesktopSearch - A Search Engine for your Desktop
 * Copyright (C) 2013 Mirko Sertic
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.mirkosertic.desktopsearch;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

class ContentExtractor {

    private static final Logger LOGGER = Logger.getLogger(ContentExtractor.class);

    private final Tika tika;
    private final DateTimeFormatter[] formats = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy"),
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss") // Date/Time in jpegs
            // DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ss") // date in jpegs, same as ISO_LOCAL_DATE_TIME ?
            };
    private final Configuration configuration;

    public ContentExtractor(Configuration aConfiguration) {
        configuration = aConfiguration;
        tika = new Tika();
    }

    private String harmonizeMetaDataName(String aName) {
        int p = aName.indexOf(":");
        if (p>0) {
            aName = aName.substring(p+1);
        }

        String theReplacement = configuration.getMetaDataNameReplacement().get(aName);
        if (theReplacement != null) {
            return theReplacement;
        }

        return aName;
    }

    private ZonedDateTime parseDate(String aString) {
        for (DateTimeFormatter f : formats) {
            try {
                ZonedDateTime date = ZonedDateTime.parse(aString, f);
                // System.out.printf("%s parsed\n", aString);
                return date;
            } catch (DateTimeParseException exc) {
            }
        }
        return null;
    }

    public Content extractContentFrom(Path aFile, BasicFileAttributes aBasicFileAttributes) {
        try {
            Metadata theMetaData = new Metadata();

            String theStringData;
            // Files under 10 Meg are read into memory as a whole
            if (aBasicFileAttributes.size() < 1024 * 1024 * 4) {
                byte[] theData = Files.readAllBytes(aFile);
                theStringData = tika.parseToString(new ByteArrayInputStream(theData), theMetaData);
            } else {
                try (InputStream theStream = Files.newInputStream(aFile, StandardOpenOption.READ)) {
                    theStringData = tika.parseToString(new BufferedInputStream(theStream), theMetaData);
                }
            }

            LanguageIdentifier theLanguageIdentifier = new LanguageIdentifier(theStringData);

            FileTime theFileTime = aBasicFileAttributes.lastModifiedTime();
            SupportedLanguage theLanguage = SupportedLanguage.getDefault();
            try {
                theLanguage = SupportedLanguage.valueOf(theLanguageIdentifier.getLanguage());
                if (!configuration.getEnabledLanguages().contains(theLanguage)) {
                    theLanguage = SupportedLanguage.getDefault();
                }
            } catch (Exception e) {
                LOGGER.info("Language "+theLanguageIdentifier.getLanguage()+" was detected, but is not supported");
            }
            Content theContent = new Content(aFile.toString(), theStringData, aBasicFileAttributes.size(), theFileTime.toMillis(), theLanguage,
                    theMetaData.get(Metadata.CONTENT_TYPE).compareTo("application/octet-stream") != 0);
            for (String theName : theMetaData.names()) {

                String theMetaDataValue = theMetaData.get(theName);

                if (theName.equals("Focal Length 35")) {
                    Long len = NumberFormat.getIntegerInstance().parse(theMetaDataValue).longValue();
                    theContent.addMetaData(harmonizeMetaDataName(theName.toLowerCase()), len);
                    continue;
                }

                // Try to detect if this is a date
                ZonedDateTime date = parseDate(theMetaDataValue);
                if (null != date) {
                    theContent.addMetaData(harmonizeMetaDataName(theName.toLowerCase()), date);
                    continue;
                }

                theContent.addMetaData(harmonizeMetaDataName(theName.toLowerCase()), theMetaDataValue);
//                        System.out.printf("%s is not parsable!\n", theMetaDataValue);
            }

            String theFileName = aFile.getFileName().toString();
            int p = theFileName.lastIndexOf(".");
            if (p > 0) {
                String theExtension = theFileName.substring(p + 1);
                theContent.addMetaData(IndexFields.EXTENSION, theExtension.toLowerCase());
            }

            return theContent;
        } catch (Exception e) {
            LOGGER.error("Error extracting content of " + aFile, e);
        }

        return null;
    }

    public boolean supportsFile(String aFilename) {
        for (SupportedDocumentType theType : configuration.getEnabledDocumentTypes()) {
            if (theType.supports(aFilename)) {
                return true;
            }
        }
        return false;
    }
}