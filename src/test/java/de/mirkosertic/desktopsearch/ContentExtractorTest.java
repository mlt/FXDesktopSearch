/**
 * FreeDesktopSearch - A Search Engine for your Desktop
 * Copyright (C) 2013 Mirko Sertic
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see
 * <http://www.gnu.org/licenses/>.
 */
package de.mirkosertic.desktopsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.mirkosertic.desktopsearch.Content.KeyValuePair;

public class ContentExtractorTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testParse() throws IOException, URISyntaxException {
        // personal photo
        URL resource = getClass().getResource("IMG_0653_resized.jpg");
        Path theFile = Paths.get(resource.toURI());
        ConfigurationManager configurationManager = new ConfigurationManager(folder.getRoot());
        Configuration theConfiguration = configurationManager.getConfiguration();
        ContentExtractor theExtractor = new ContentExtractor(theConfiguration);
        BasicFileAttributes theAttributes = Files.readAttributes(theFile, BasicFileAttributes.class);
        Content theContent = theExtractor.extractContentFrom(theFile, theAttributes);

        // assertTrue(theContent.isKnown());
        int count = theContent.getMetadata().reduce(0, (a, theEntry) -> {
            switch(theEntry.key) {
            case "creation-date":
            case "last-modified": {
                assertTrue(theEntry.value instanceof ZonedDateTime);
                assertEquals(theEntry.value, ZonedDateTime.of(LocalDateTime.of(2014, 02, 20, 22, 30, 18), ZoneId.systemDefault()));
//                assertEquals(theEntry.value, ZonedDateTime.of(LocalDateTime.of(2015, 05, 26, 16, 37, 10), ZoneId.systemDefault()));
                a += 1;
                break;
            }
            case "focal-length-35":
                assertEquals(theEntry.value, "35mm"); //"31mm");
                a += 1;
                break;
            case "image-height":
                a += 1;
                assertEquals(theEntry.value, "484 pixels");
                break;
            }
            return a;
        }, Integer::sum);
        assertEquals(15, count);
    }
}