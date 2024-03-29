package org.themullers.library;

import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Component;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Helper functions used in a variety of places in the implementation of the library.
 */
@Component
public class LibUtils {

    public static final Map<String, String> MIME_TYPES = Map.of(
            "epub", "application/epub+zip",
            "mobi", "application/x-mobipocket-ebook",
            "m4b", "audio/mp4a-latm",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "svg", "image/svg+xml"
    );

    public final static String STANDALONE = "Standalone";

    LibraryDAO dao;
    LibraryOSAO osao;

    public LibUtils(LibraryDAO dao, LibraryOSAO osao) {
        this.dao = dao;
        this.osao = osao;
    }

    /**
     * Groups a list of books by the series those books are associated with;
     * books from Scalzi's "Old Man's War" series would be grouped separately
     * from books in his "Interdependency" series.
     *
     * The returned data structure's elements (the keys and values of the map)
     * are ordered chronologically by publication date; the books within that series
     * (the values) are ordered by their publication date and the series (the keys)
     * are ordered by the series sequence number (or by publication date for the
     * standalone books).
     *
     * If the book is not in a series (if getSeries() returns null), the book
     * will be grouped as STANDALONE.
     *
     * @param books The books to organize
     * @return  A map of books grouped by series.
     */
    public Map<String, List<Book>> groupBooksBySeries(List<Book> books) {

        // sort all the books by publication year; this ensures that the series
        // will be added in order from oldest to newest
        books.sort((a, b) -> a.getPublicationYear() - b.getPublicationYear());

        // create a map to hold the groupings
        var groupedBooks = new LinkedHashMap<String, List<Book>>();

        // group the books by series
        for (var book : books) {

            // get the series (or use STANDALONE as the series if there isn't one)
            var series = Utils.ifNull(book.getSeries(), STANDALONE);

            // if this is the first book we've encountered for a series, create an entry with an empty list
            groupedBooks.putIfAbsent(series, new LinkedList<>());

            // add the book to the list of books for this series
            groupedBooks.get(series).add(book);
        }

        // sort the books within each series
        for (var series : groupedBooks.entrySet()) {
            var seriesName = series.getKey();
            var seriesBooks = series.getValue();

            // sort the standalone books by publication year
            if (STANDALONE.equals(seriesName)) {
                seriesBooks.sort((a, b) -> a.getPublicationYear() - b.getPublicationYear());
            }
            // sort the series by the series sequence number
            else {
                seriesBooks.sort((a,b) -> a.getSeriesSequence() - b.getSeriesSequence());
            }
        }

        return groupedBooks;
    }

    /**
     * Determine a file's mime type based on its extension.
     *
     * @param filename a filename
     * @return the mime type
     */
    public String mimeTypeForFile(String filename) {

        var ext = Utils.getExtension(filename);
        if (ext != null) {
            var mimeType =  MIME_TYPES.get(ext);
            if (mimeType != null) {
                return mimeType;
            }
        }

        throw new RuntimeException("can't determine mime type for file " + filename);
    }

    /**
     * Writes an S3 object to an HTTP response object
     *
     * @param obj      the S3 object to write
     * @param response the HTTP response object to write to
     * @throws IOException thrown if an unexpected error occurs writing to the response
     */
    public void writeS3ObjectToResponse(S3Object obj, HttpServletResponse response) throws IOException {

        // escape any quotation marks in the filename with a backslash
        var filename = obj.getKey();
        var escapedFilename = filename.replace("\"", "\\\"");

        // build the content disposition
        var disposition = String.format("attachment; filename=\"%s\"", escapedFilename);

        // convert the content length from the object metadata into an int as required by the servlet response object
        var contentLength = Math.toIntExact(obj.getObjectMetadata().getContentLength());

        // figure out a mime type based on the file's extension
        var mimeType = mimeTypeForFile(filename);

        // write the S3 object info to the response
        response.setContentLength(contentLength);
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", disposition);
        obj.getObjectContent().transferTo(response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Find object keys for assets that are not currently associated with a book.
     * @return  a list of object keys
     */
    public List<String> fetchUnattachedObjectKeys() {

        var unattachedObjectIds = new LinkedList<String>();

        // get a list of the object IDs that are attached to books in the database
        var attachedEpubs = dao.fetchAllEpubObjectKeys();
        var attachedMobis = dao.fetchAllMobiObjectKeys();
        var attachedAudiobooks = dao.fetchAllAudiobookObjectKeys();

        // for each asset in the object store
        for (var objId : osao.listObjects()) {

            // if that asset's id is not associated with any books in the database
            if (!attachedEpubs.contains(objId) && !attachedMobis.contains(objId) && !attachedAudiobooks.contains(objId)) {

                // add it to the list of unattached object ids
                unattachedObjectIds.add(objId);
            }
        }

        return unattachedObjectIds;
    }

    /**
     * Given an EPUB object key, calculate what the key would be for a MOBI for the same book.
     * @param epub  the object key for an EPUB
     * @return  the object key for a MOBI that may or may not exist (or null if a MOBI key could not be calculated)
     */
    public static String matchingMobi(String epub) {

        String mobi = null;

        // if an epub key was passed in
        if (epub != null) {

            // if the epub ended as expected with ".epub"
            var lcEpub = epub.toLowerCase();
            if (lcEpub.endsWith(".epub")) {

                // find the same filename, but ending in ".mobi"
                var base = epub.substring(0, lcEpub.length() - 5);
                mobi = base + ".mobi";
            }
        }
        return mobi;
    }
}
