package org.themullers.library.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.themullers.library.BookImageCache;
import org.themullers.library.LibUtils;
import org.themullers.library.Utils;
import org.themullers.library.db.LibraryDAO;

import java.nio.file.Files;
import java.util.regex.Pattern;

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

    @PostMapping(value="/api/book", produces="application/json;charset=UTF-8")
    public FormValidation addBook(@RequestBody BookForm book) {
        var v = new BookFormValidation(book);
        if (v.isSuccess()) {
            try {
                book.merge();
                var bookId = dao.insertBook(book);
                var coverImageFilename = book.getCoverImage();
                if (!Utils.isBlank(coverImageFilename)) {
                    var coverImageFile = bookImageCache.getUploadedBookFromCache(0, coverImageFilename);
                    if (coverImageFile == null) {
                        coverImageFile = bookImageCache.getBookImageFromCache(book.getEpubObjectKey(), coverImageFilename);
                    }
                    if (coverImageFile == null) {
                        logger.error("cover image was specified when adding a book, but now we can't find it in the cover image cache; filename = " + coverImageFilename);
                    }
                    else {
                        dao.insertCoverImage(bookId, coverImageFilename, libUtils.mimeTypeForFile(coverImageFilename), Files.readAllBytes(coverImageFile.toPath()));
                    }
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

    @PutMapping(value="/api/book/{id}")
    public FormValidation editBook(@RequestBody BookForm book, @PathVariable("id") int bookId) {
        book.setId(bookId);
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
                    var coverImageFile = bookImageCache.getUploadedBookFromCache(0, coverImageFilename);
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

    @PutMapping(value="/api/mobi/{mobi}")
    public FormValidation assignMobiToBook(@RequestBody MobiForm mobiForm, @PathVariable("mobi") String mobi) {

        var v = new MobiFormValidation(mobiForm.getMobiIndex());

        try {
            var pattern = Pattern.compile("(.*)\\ \\((.*)\\)");
            var matcher = pattern.matcher(mobiForm.bookInfo);
            var title = matcher.group(1);
            var author = matcher.group(2);

            var book = dao.fetchBook(title, author);
            if (book == null) {
                v.addError("could not find that book in the database");
                v.setSuccess(false);
            }
            else {
                book.setMobiObjectKey(mobi);
                dao.updateBook(book);
            }
        }
        catch (Exception x) {
            v.addError("exception thrown while associating MOBI with book: " + x.getMessage());
            v.setSuccess(false);
        }

        return v;
    }

    protected static class MobiForm {
        String bookInfo;
        String mobiIndex;

        public String getBookInfo() {
            return bookInfo;
        }

        public void setBookInfo(String bookInfo) {
            this.bookInfo = bookInfo;
        }

        public String getMobiIndex() {
            return mobiIndex;
        }

        public void setMobiIndex(String mobiIndex) {
            this.mobiIndex = mobiIndex;
        }
    }

    protected static class MobiFormValidation extends FormValidation {

        String mobiIndex;

        public MobiFormValidation(String mobiIndex) {
            this.mobiIndex = mobiIndex;
        }

        public String getMobiIndex() {
            return mobiIndex;
        }

        public void setMobiIndex(String mobiIndex) {
            this.mobiIndex = mobiIndex;
        }
    }

    protected static class BookFormValidation extends FormValidation {
        BookForm book;

        public BookFormValidation(BookForm book) {
            this.book = book;
            validate();
        }

        protected void validate() {
            addErrorIfBlank(book.getTitle(), "Missing title");
            addErrorIfBlank(book.getAuthor(), "Missing author");
            addErrorIfBlank(book.getPublicationYearString(), "Missing publication year");
            addErrorIfNotMatch(book.getPublicationYearString(), "\\d{4}", "Bad format publication year");
            addErrorIf(Utils.isNotBlank(book.getSeries()) && Utils.isBlank(book.getSeriesSequenceString()), "Missing series # (required when series is provided)");
            addErrorIfNotMatch(book.getSeriesSequenceString(), "\\d+", "Bad format series #");
            addErrorIfBlank(book.getAcquisitionDateString(), "Missing acquisition date");
            addErrorIfNotMatch(book.getAcquisitionDateString(), "\\d{1,2}/\\d{1,2}/\\d{4}", "Acquisition date must be mm/dd/yyyy");
            addErrorIfBlank(book.getEpubObjectKey(), "No EPUB selected");

            setSuccess(errorMessages.size() == 0);
        }
    }
}
