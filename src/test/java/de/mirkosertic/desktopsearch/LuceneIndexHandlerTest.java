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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LuceneIndexHandlerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testAddition() throws IOException, URISyntaxException {
        URL resource = getClass().getResource("IMG_0653_resized.jpg");
        Path theFile = Paths.get(resource.toURI());
        ConfigurationManager configurationManager = new ConfigurationManager(folder.getRoot());
        Configuration aConfiguration = configurationManager.getConfiguration();
        ContentExtractor theExtractor = new ContentExtractor(aConfiguration);
        BasicFileAttributes theAttributes = Files.readAttributes(theFile, BasicFileAttributes.class);
        Content aContent = theExtractor.extractContentFrom(theFile, theAttributes);

        AnalyzerCache theCache = new AnalyzerCache(aConfiguration);
        LuceneIndexHandler luceneIndexHandler = new LuceneIndexHandler(aConfiguration, theCache, null, null);

        try {
            luceneIndexHandler.addToIndex(UUID.randomUUID().toString(), aContent);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        luceneIndexHandler.shutdown();
    }
}