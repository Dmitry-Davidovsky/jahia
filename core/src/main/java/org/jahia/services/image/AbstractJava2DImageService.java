/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.image;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Abstract Java2D common image operation implementations
 */
public abstract class AbstractJava2DImageService extends AbstractImageService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractJava2DImageService.class);

    public enum OperationType {
        RESIZE, CROP, ROTATE
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        InputStream is = null;
        try {
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                logger.warn("Unable to load image for node {}", node.getPath());
                return null;
            }
            return new BufferImage(node.getPath(), originalImage, mimeType);
        } catch (Exception e) {
            logger.error("Error opening image for node {}. Cause: {}", node.getPath(), e.getLocalizedMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("Error opening image for node " + node.getPath(), e);
            }
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public int getHeight(Image i) {
        BufferImage bufferImage = ((BufferImage)i);
        return bufferImage.getOriginalImage().getHeight();
    }

    public int getWidth(Image i) {
        BufferImage bufferImage = ((BufferImage)i);
        return bufferImage.getOriginalImage().getWidth();
    }

    public boolean cropImage(Image image, File outputFile, int top, int left, int width, int height) throws IOException {
        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        int clippingWidth = width;
        if (left + clippingWidth > originalImage.getWidth()) {
            clippingWidth = originalImage.getWidth() - left;
        }
        int clippingHeight = height;
        if (top + clippingHeight > originalImage.getHeight()) {
            clippingHeight = originalImage.getHeight() - top;
        }
        BufferedImage clipping = getDestImage(clippingWidth, clippingHeight, originalImage);
        Graphics2D area = getGraphics2D(clipping, OperationType.CROP);
        area.drawImage(originalImage, 0, 0, clippingWidth, clippingHeight, left, top, left + clippingWidth,
                top + clippingHeight, null);
        area.dispose();

        // Save destination image
        saveImageToFile(clipping, ((BufferImage)image).getMimeType(), outputFile);

        return true;
    }

    public boolean rotateImage(Image image, File outputFile, boolean clockwise) throws IOException {
        return rotateImage(image, outputFile, clockwise ? 90. : -90);
    }

    public boolean rotateImage(Image image, File outputFile, double degrees) throws IOException {
        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        BufferedImage dest = getDestImage(originalImage.getHeight(), originalImage.getWidth(), originalImage);
        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.ROTATE);

        double angle = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = originalImage.getWidth(), h = originalImage.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
        graphics2D.translate((neww - w) / 2, (newh - h) / 2);
        graphics2D.rotate(angle, w / (double)2, h / (double)2);
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            graphics2D.drawImage(originalImage, 0, 0, graphics2D.getBackground(), null);
        } else {
            graphics2D.drawImage(originalImage, 0, 0, null);
        }

        // Save destination image
        saveImageToFile(dest, ((BufferImage)image).getMimeType(), outputFile);
        return true;
    }

    public boolean resizeImage(Image image, File outputFile, int newWidth, int newHeight, ResizeType resizeType) throws IOException {

        BufferedImage originalImage = ((BufferImage) image).getOriginalImage();

        BufferedImage dest = resizeImage(originalImage, newWidth, newHeight, resizeType);

        // Save destination image
        saveImageToFile(dest, ((BufferImage) image).getMimeType(), outputFile);

        return true;
    }

    public BufferedImage resizeImage(BufferedImage image, int width, int newHeight, ResizeType resizeType) {
        ResizeCoords resizeCoords = getResizeCoords(resizeType, image.getWidth(), image.getHeight(), width, newHeight);
        if (ResizeType.ADJUST_SIZE.equals(resizeType)) {
            width = resizeCoords.getTargetWidth();
            newHeight = resizeCoords.getTargetHeight();
        }

        BufferedImage dest = getDestImage(width, newHeight, image);

        // Paint source image into the destination, scaling as needed
        Graphics2D graphics2D = getGraphics2D(dest, OperationType.RESIZE);

        graphics2D.drawImage(image,
                resizeCoords.getTargetStartPosX(), resizeCoords.getTargetStartPosY(),
                resizeCoords.getTargetStartPosX() + resizeCoords.getTargetWidth(), resizeCoords.getTargetStartPosY() + resizeCoords.getTargetHeight(),
                resizeCoords.getSourceStartPosX(), resizeCoords.getSourceStartPosY(),
                resizeCoords.getSourceStartPosX() + resizeCoords.getSourceWidth(), resizeCoords.getSourceStartPosY() + resizeCoords.getSourceHeight(),
                null);
        graphics2D.dispose();
        return dest;
    }

    protected abstract Graphics2D getGraphics2D(BufferedImage bufferedImage, OperationType operationType);

    protected boolean canRead(JCRNodeWrapper node) throws RepositoryException {
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        String mimeType = contentNode.getProperty(Constants.JCR_MIMETYPE).getString();

        Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersByMIMEType(mimeType);
        if (imageReaderIterator.hasNext()) {
            // now let's check if we can also write it.
            Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersByMIMEType(mimeType);
            if (imageWriterIterator.hasNext()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected BufferedImage getDestImage(int newWidth, int newHeight, BufferedImage originalImage) {
        BufferedImage dest;
        if (originalImage.getColorModel() instanceof IndexColorModel) {
            // dest = new BufferedImage(newWidth, newHeight, originalImage.getType(), (IndexColorModel) originalImage.getColorModel());
            dest = new BufferedImage(originalImage.getColorModel(), originalImage.getColorModel().createCompatibleWritableRaster(newWidth, newHeight), false, new Hashtable<Object, Object>());
        } else {
            dest = new BufferedImage(newWidth, newHeight, originalImage.getType());
        }
        return dest;
    }

    protected void saveImageToFile(BufferedImage dest, String mimeType, File destFile) throws IOException {
        Iterator<ImageWriter> suffixWriters = ImageIO.getImageWritersByMIMEType(mimeType);
        if (suffixWriters.hasNext()) {
            ImageWriter imageWriter = suffixWriters.next();
            ImageOutputStream imageOutputStream = new FileImageOutputStream(destFile);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(dest);
            imageOutputStream.close();
        } else {
            logger.warn("Couldn't find a writer for mime type : " + mimeType + "(" + this.getClass().getName() + ")");
        }
    }

}
