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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class JPEGPreviewGenerator implements PreviewGenerator, PreviewConstants {

    private static final Logger LOGGER = Logger.getLogger(JPEGPreviewGenerator.class);

    @Override
    public synchronized Preview createPreviewFor(File aFile) {
        try {
            BufferedImage theImage = ImageIO.read(aFile);
            return new Preview(ImageUtils.rescale(theImage, THUMB_WIDTH, THUMB_HEIGHT, ImageUtils.RescaleMethod.RESIZE_FIT_BOTH_DIMENSIONS));
        } catch (Exception e) {
            LOGGER.error("Error creating preview for " + aFile, e);
            return null;
        }
    }

    @Override
    public boolean supportsFile(File aFile) {
        String ext = aFile.getName();
        int p = ext.indexOf(".");
        if (p>0)
            ext = ext.substring(p+1);
        // TODO check if ImageIO ignores extension and rely on Tika type
        // also consider using JAI for TIFF etc
        return ext.toLowerCase().matches("jpe?g|png|w?bmp|gif");
    }
}