package org.themullers.library.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.themullers.library.BookImageCache;
import org.themullers.library.LibUtils;
import org.themullers.library.Utils;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.web.forms.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Handle RESTful API requests.
 */
@RestController
public class RestAPIController {

    private static Logger logger = LoggerFactory.getLogger(RestAPIController.class);

    public final static String EXISTING_COVER_VALUE = "Existing Cover";

    LibraryDAO dao;
    LibUtils libUtils;
    BookImageCache bookImageCache;

    @Autowired
    public RestAPIController(LibraryDAO dao, LibUtils libUtils, BookImageCache bookImageCache) {
        this.dao = dao;
        this.libUtils = libUtils;
        this.bookImageCache = bookImageCache;
    }

    /**
     * Handle an API request to add a book to the library.
     * @param book  information from the form about the book to be added to the library
     * @return  a response object with status and error messages
     */
    @PostMapping(value="/api/book", produces="application/json;charset=UTF-8")
    public FormValidation addBook(@RequestBody BookForm book) {

        // if data in the form fields is valid
        var v = new BookFormValidation(book);
        if (v.isSuccess()) {
            try {
                // parse the form fields into the book object
                book.merge();

                // add the book to the library
                var bookId = dao.insertBook(book);

                // if the user picked a cover image for the book
                var coverImageFilename = book.getCoverImage();
                if (!Utils.isBlank(coverImageFilename)) {

                    // look for the requested cover image in the cache of uploaded images
                    var coverImageFile = bookImageCache.getUploadedBookImageFromCache(0, coverImageFilename);

                    // if the cover image wasn't found there, look for the image in the cache of images from the EPUB
                    if (coverImageFile == null) {
                        coverImageFile = bookImageCache.getEpubBookImageFromCache(book.getEpubObjectKey(), coverImageFilename);
                    }

                    // if we found a cover image, add it to the database for this book
                    if (coverImageFile != null) {
                        dao.insertCoverImage(bookId, coverImageFilename, libUtils.mimeTypeForFile(coverImageFilename), Files.readAllBytes(coverImageFile.toPath()));
                    }

                    // if we didn't find a cover image, don't do anything other than log an error
                    else {
                        logger.error("cover image was specified when adding a book, but now we can't find it in the cover image cache; filename = " + coverImageFilename);
                    }
                }
            }
            // if an exception is thrown, capture the reason and add it to the response for display to the user
            catch (Exception x) {
                v.addError("Exception thrown: " + x.getMessage());
                v.setSuccess(false);
                logger.info("exception while validating form data", x);
            }
        }
        return v;
    }

    /**
     * Handle a request to update a book in the library.
     * @param bookId  the id of the book to update
     * @param book  information from the form about the book to be updated
     * @return  a response object with status and error messages
     */
    @PutMapping(value="/api/book/{id}")
    public FormValidation editBook(@RequestBody BookForm book, @PathVariable("id") int bookId) {

        // update the form info with the book id from the URL
        book.setId(bookId);

        // if the information from the form doesn't contain any invalid fields
        var v = new BookFormValidation(book);
        if (v.isSuccess()) {
            try {
                // merge form fields like 'pub year string' into base class properties like 'pub year'
                book.merge();

                // update the book in the database
                dao.updateBook(book);
                dao.setTags(bookId, book.getTags());

                // update the cover image, if it's been changed
                var coverImageFilename = book.getCoverImage();
                if (!Utils.isBlank(coverImageFilename) && !EXISTING_COVER_VALUE.equalsIgnoreCase(coverImageFilename)) {
                    var coverImageFile = bookImageCache.getUploadedBookImageFromCache(0, coverImageFilename);
                    dao.insertCoverImage(bookId, coverImageFilename, libUtils.mimeTypeForFile(coverImageFilename), Files.readAllBytes(coverImageFile.toPath()));
                }
            }
            catch (Exception x) {
                v.addError("Exception thrown: " + x.getMessage());
                v.setSuccess(false);
                logger.info("exception while validating form data", x);
            }
        }
        return v;
    }

    /**
     * Update a MOBI by associating it with a book.
     * @param mobiForm  the information from a form on that page
     * @param mobi  the MOBI that should be associated with the book specified in the mobiForm
     * @return  a response object with status, any error message, and the index passed in the mobiForm
     */
    @PutMapping(value="/api/mobi/{mobi}")
    public FormValidation assignMobiToBook(@RequestBody MobiForm mobiForm, @PathVariable("mobi") String mobi) {

        var v = new MobiFormValidation(mobiForm.getMobiIndex());

        try {
            // parse the book info
            var pattern = Pattern.compile("(.*)\\ \\((.*)\\)");
            var matcher = pattern.matcher(mobiForm.getBookInfo());
            if (matcher.matches()) {

                // extract the title and author
                var title = matcher.group(1);
                var author = matcher.group(2);

                // if we can find the book, update it to use the specified MOBI
                var book = dao.fetchBook(title, author);
                if (book != null) {
                    book.setMobiObjectKey(mobi);
                    dao.updateBook(book);
                }

                // if we can't find the book, return a bad status with a message
                else {
                    v.fail("could not find that book in the database");
                }
            }

            // if the book info isn't in a valid format, return a bad status with a message
            else {
                v.fail("you need to specify a book in the format 'title (author)'");
            }
        }

        // if an exception is thrown, return a bad status with a message
        catch (Exception x) {
            v.fail("exception thrown while associating MOBI with book: " + x.getMessage());
        }

        return v;
    }

    /**
     * Upload the cover image for a book
     * @param file  the cover image
     * @param bookId  the id of the book
     * @throws IOException  thrown if an unexpected error occurs during file transfer
     */
    @PostMapping(value="/api/book/cover/{id}")
    public void receiveCoverImage(@RequestParam("file") MultipartFile file, @PathVariable("id") int bookId) throws IOException {
        bookImageCache.cacheUploadedCoverForBook(file.getOriginalFilename(), file.getInputStream(), bookId);
    }
}
