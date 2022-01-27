package org.themullers.library;

import com.amazonaws.services.s3.model.S3Object;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LibUtils {

    public final static String STANDALONE = "Standalone";

    /**
     * Groups a list of books by the series those books are associated with;
     * books from Scalzi's "Old Man's War" series would be grouped separately
     * from books in his "Interdependency" series.
     *
     * The returned data structure's elements (the keys and values of the map)
     * are ordered chronologically by publication date; the books within that series
     * (the values) are ordered by their publication date and the series (the keys)
     * are ordered by the publication date of the earliest book in the series.
     *
     * If the book is not in a series (if getSeries() returns null), the book
     * will be grouped as STANDALONE.
     *
     * @param books The books to organize
     * @return  A map of books grouped by series.
     */
    public static Map<String, List<Book>> groupBooksBySeries(List<Book> books) {

        // start by sorting the provided books by their publication year
        books.sort((Book a, Book b) -> a.getPublicationYear() - b.getPublicationYear());

        // create a map to hold the groupings
        var groupedBooks = new LinkedHashMap<String, List<Book>>();

        // for each book
        for (var book : books) {

            // get the series (or use STANDALONE as the series if there isn't one)
            var series = Utils.ifNull(book.getSeries(), STANDALONE);

            // if this is the first book we've encountered for a series, create an entry with an empty list
            groupedBooks.putIfAbsent(series, new LinkedList<>());

            // add the book to the list of books for this series
            groupedBooks.get(series).add(book);
        }

        return groupedBooks;
    }

    public static Map<Integer, Book> indexBooksById(List<Book> books) {
        return books.stream().collect(Collectors.toMap(Book::getId, Function.identity()));
    }

    /**
     * determine a file's mime type based on its extension
     *
     * @param filename a filename
     * @return the mime type
     */
    public static String mimeTypeForFile(String filename) {
        if (filename.toLowerCase().endsWith(".epub")) {
            return "application/epub+zip";
        } else if (filename.toLowerCase().endsWith(".m4b")) {
            return "audio/mp4a-latm";
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
    public static void writeS3ObjectToResponse(S3Object obj, HttpServletResponse response) throws IOException {

        // escape any quotation marks in the filename with a backslash
        var filename = obj.getKey();
        var escapedFilename = filename.replace("\"", "\\\"");

        // build the content disposition
        var disposition = String.format("attachment; filename=\"%s\"", escapedFilename);

        // convert the content length from the object metadata into an int as required by the servlet response object
        var contentLength = Math.toIntExact(obj.getObjectMetadata().getContentLength());

        // figure out a mime type based on the file's extension
        var mimeType = LibUtils.mimeTypeForFile(filename);

        // write the S3 object info to the response
        response.setContentLength(contentLength);
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", disposition);
        obj.getObjectContent().transferTo(response.getOutputStream());
        response.flushBuffer();
    }

    public static List<String> unattachedObjectIds(LibraryDAO dao, LibraryOSAO osao) {

        var unattachedObjectIds = new LinkedList<String>();

        // get a list of the object IDs that are attached to books in the database
        var attachedEpubs = dao.fetchAllEpubObjectIds();
        var attachedMobis = dao.fetchAllMobiObjectIds();
        var attachedAudiobooks = dao.fetchAllAudiobookObjectIds();

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
}
