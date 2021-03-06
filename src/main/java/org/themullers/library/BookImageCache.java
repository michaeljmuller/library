package org.themullers.library;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

// NOTE: to resize -- https://www.baeldung.com/java-resize-image

/**
 * A cache of book images, stored on the file system either after being uploaded
 * or extracted from an EPUB.
 */
@Service
public class BookImageCache {

    protected static List<String> IMAGE_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "gif", "svg");

    LibraryOSAO osao;
    File imageCacheDir;

    public BookImageCache(LibraryDAO dao, LibraryOSAO osao, @Value("${book.image.cache.dir}") File imageCacheDir) {
        this.osao = osao;
        this.imageCacheDir = imageCacheDir;
    }

    /**
     * Extracts all the images from a book and returns a list of the filenames of the images
     * or uses information from a cache on the file system if the images have been previously
     * extracted.
     *
     * @param epubObjId The key for this book's epub in the object store.
     * @return A list of image filenames.
     * @throws IOException Throws when an unexpected error occurs extracting the images
     */
    public List<String> imagesFromBook(String epubObjId) throws IOException {
        var images = new LinkedList<String>();

        // if an epub was provided
        if (!Utils.isBlank(epubObjId)) {

            // if there is a cache entry for the images from this book
            File imageDir = new File(imageCacheDir, epubObjId);
            if (imageDir.exists()) {

                // walk the cache directory and add each image file's name to the list
                for (var file : imageDir.listFiles()) {
                    var filename = file.getName();
                    if (isImage(filename)) {
                        images.add(filename);
                    }
                }
            }

            // if there is not a cache entry for the images from this book
            else {

                // make a directory in which we will cache the images in this book
                imageDir.mkdir();

                // unzip the epub and inspect each file within
                for (var ebookEntry : Utils.unzip(osao.readObject(epubObjId).getObjectContent()).entrySet()) {

                    // if the file is an image
                    var pathWithinZip = ebookEntry.getKey();
                    if (isImage(pathWithinZip)) {

                        var imageBytes = ebookEntry.getValue();
                        var filename = filename(pathWithinZip);

                        try {
                            var dimensions = getImageDimensions(imageBytes);

                            // if the image is smaller than 50 pixels in either dimension, it's too small for a cover image; skip it
                            if (dimensions.w < 50 || dimensions.h < 50) {
                                continue;
                            }

                            filename = prependDimensionsToFilename(dimensions, filename);
                        }
                        catch (Exception x) {
                            // smother; we don't REALLY need the dimensions
                        }

                        // save the image to the cache directory
                        var imageFile = new File(imageDir, filename);
                        try (var fos = new FileOutputStream(imageFile)) {
                            fos.write(ebookEntry.getValue());
                        }

                        // add the image's filename to the list
                        images.add(filename);
                    }
                }
            }
        }

        return images;
    }

    /**
     * Add an uploaded cover image to the cache.
     * @param filename  a filename for the uploaded image
     * @param is  the binary content of the uploaded image
     * @param bookId  the id of the book that this image is a cover for
     * @throws IOException  thrown if an unexpected error occurred while uploading the cover image
     */
    public void cacheUploadedCoverForBook(String filename, InputStream is, int bookId) throws IOException {

        // make a subdirectory for uploaded covers for this book (if it doesn't already exist)
        File imageDir = new File(imageCacheDir, Integer.toString(bookId));
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }

        // write the upload into the cache
        try (var os = new FileOutputStream(new File(imageDir, filename))) {
            is.transferTo(os);
        }
    }

    /**
     * Finds a file in the filesystem cache of cover images extracted from EPUBs.
     * @param epubObjKey  the object key for the EPUB whose cover image we're looking for
     * @param imageFilename  the filename of the image to return
     * @return  a file with the cover image (or null if not found)
     */
    public File getEpubBookImageFromCache(String epubObjKey, String imageFilename) {
        var bookDir = new File(imageCacheDir, epubObjKey);
        if (!bookDir.exists() || !bookDir.isDirectory()) {
            return null;
        }
        return new File(bookDir, imageFilename);
    }

    /**
     * Finds a file in the filesystem cache of cover images uploaded for a book.
     * @param bookId  the id of the book whose cover we're searching for
     * @param imageFilename  filename of the cover image we're looking for
     * @return  a file with the cover image (or null if not found)
     */
    public File getUploadedBookImageFromCache(int bookId, String imageFilename) {
        var bookDir = new File(imageCacheDir, Integer.toString(bookId));
        if (!bookDir.exists() || !bookDir.isDirectory()) {
            return null;
        }
        return new File(bookDir, imageFilename);
    }

    // HELPER METHODS

    // get the filename (the last element) of a path
    protected String filename(String pathString) {
        return Path.of(pathString).getFileName().toString();
    }

    // is this file an image?
    protected boolean isImage(String filename) {
        return IMAGE_EXTENSIONS.contains(Utils.getExtension(filename));
    }

    // calculate an alternate filename that has some dimensions pre-pended to the filename
    protected String prependDimensionsToFilename(Dimensions dimensions, String originalFilename) {
        return String.format("%dx%d-%s", dimensions.w, dimensions.h, originalFilename);
    }

    // calculate the dimensions for an image
    protected Dimensions getImageDimensions(byte[] imageBytes) throws IOException {
        try (var is = new ByteArrayInputStream(imageBytes)) {
            var img = ImageIO.read(is);
            return new Dimensions(img.getWidth(), img.getHeight());
        }
    }

    // structure to represent image dimensions
    protected record Dimensions(int w, int h) {
    }
}