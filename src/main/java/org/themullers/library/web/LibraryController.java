package org.themullers.library.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.themullers.library.*;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class LibraryController {

    private Logger logger = LoggerFactory.getLogger(LibraryController.class);

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    LibraryDAO dao;
    LibraryOSAO osao;
    SpreadsheetService ss;
    LibUtils libUtils;
    BookImageCache bookImageCache;

    @Autowired
    public LibraryController(LibraryDAO dao, LibraryOSAO osao, SpreadsheetService ss, LibUtils libUtils, BookImageCache bookImageCache) {
        this.dao = dao;
        this.osao = osao;
        this.ss = ss;
        this.libUtils = libUtils;
        this.bookImageCache = bookImageCache;
    }

    /**
     * Handle a request to render the application's home page.
     * The home page displays the most recently added books.
     *
     * @param page can be a number > 1 to display more (older) books
     * @return information needed to render the home page
     */
    @GetMapping("/")
    public ModelAndView home(@RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        var mv = new LibraryModelAndView("home");

        // get the most recent books
        int booksPerPage = 6;
        var newReleases = dao.fetchNewestBooks(booksPerPage, page * booksPerPage);
        mv.addObject("page", page);
        mv.addObject("books", newReleases);
        return mv;
    }

    /**
     * Handle a request to render the "authors" page.
     *
     * @return information needed to render the page
     */
    @GetMapping("/authors")
    public ModelAndView authors() {
        var mv = new LibraryModelAndView("authors");
        mv.addObject("authors", dao.fetchAllAuthors());
        return mv;
    }

    /**
     * Handle a request to render the "author" page.
     *
     * @param author the author whose books are to be rendered
     * @return information needed to render the page
     */
    @GetMapping("/author")
    public ModelAndView author(@RequestParam(name = "name") String author) {

        // get the books, group them by series, and then pull out the stand-alone books to be handled uniquely
        var books = dao.fetchBooksForAuthor(author);
        var groupedBooks = libUtils.groupBooksBySeries(books);
        var standalone = groupedBooks.remove(LibUtils.STANDALONE);

        // build the model
        var mv = new LibraryModelAndView("author");
        mv.addObject("authorName", author);
        mv.addObject("groupedBooks", groupedBooks);
        mv.addObject("standalone", standalone);
        return mv;
    }

    /**
     * Handle a request to provide a cover image for an book.
     *
     * @param bookId the id of the book whose cover should be rendered
     * @return the binary contents of the image
     */
    @GetMapping(value = "/cover", produces = "image/jpeg")
    public byte[] cover(@RequestParam(name = "book") int bookId) throws IOException {
        return dao.fetchImageForEbook(bookId);
    }

    /**
     * Handle a request to render the page that allows the user to upload or download a spreadsheet
     * containing metadata for all the books in the library.
     *
     * @param msg       an optional message to be displayed indicating results from an earlier operation
     * @param status    an optional indicator of the success of an earlier operation; should be "success" or "failure"
     * @param stackDump an optional stack trace with diagnostic information concerning the failure of an earlier operation
     * @return information needed to render the page
     */
    @GetMapping("metadata")
    public ModelAndView metadata(@RequestParam(name = "msg", required = false) String msg, @RequestParam(name = "status", required = false) String status, @ModelAttribute(name = "stackDump") String stackDump) {
        var mv = new LibraryModelAndView("metadata");
        mv.addObject("msg", msg);
        mv.addObject("status", status);
        mv.addObject("stackDump", stackDump);
        return mv;
    }

    /**
     * Handle a request to download a spreadsheet containing metadata for each book in the library.
     *
     * @return a spreadsheet
     * @throws IOException thrown if an unexpected error occurs generating the spreadsheet
     */
    @GetMapping(value = "/ss", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public byte[] downloadSpreadsheet() throws IOException {
        return ss.download();
    }

    /**
     * Handle a request to upload a spreadsheet containing metadata for each book in the library.
     *
     * @param file               the spreadsheet to upload
     * @param redirectAttributes a Spring Boot object that allows this method to store information to be rendered in another web page
     * @return a view object that instructs Spring Boot to redirect the user to the "metadata" page
     * @throws IOException thrown if an unexpected error occurs processing the spreadsheet
     */
    @PostMapping("/ss")
    public RedirectView uploadSpreadsheet(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {

        // get information about the uploaded file
        var bytes = file.getBytes();
        var filename = file.getOriginalFilename();

        // this is the default success message
        var success = true;
        var msg = String.format("Success! Processed file %s (%,d bytes).", filename, bytes.length);

        // if the user didn't select a file before pressing the upload button...
        if (bytes == null || bytes.length <= 0) {
            msg = "Please choose a file to upload.";
            success = false;
        }

        // if we actually got a file
        else {
            try {
                // import the contents from the file into the database
                ss.upload(bytes);
            } catch (Exception x) {
                success = false;
                msg = String.format("Unable to process spreadsheet -- %s: %s", x.getClass().getSimpleName(), x.getMessage());
                redirectAttributes.addFlashAttribute("stackDump", Utils.toString(x));
            }
        }

        // redirect the user back to the "metadata" page after the upload completes
        redirectAttributes.addAttribute("msg", msg);
        redirectAttributes.addAttribute("status", success ? "success" : "failure");
        return new RedirectView("/metadata");
    }

    @GetMapping("/tags")
    public ModelAndView tags() {
        var mv = new LibraryModelAndView("tags");
        mv.addObject("tags", dao.getTags());
        return mv;
    }

    @GetMapping("/login")
    public ModelAndView login(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "logout", required = false) String logout) {
        var mv = new LibraryModelAndView("/login");
        if (logout != null) {
            mv.addObject("msg", "You have been logged out.");
        }
        if (error != null) {
            mv.addObject("msg", "Invalid login; please try again.");
        }
        return mv;
    }

    @GetMapping(value = "/book", produces = "application/epub+zip")
    public void getEbook(@RequestParam(value = "bookId") int bookId, HttpServletResponse response) throws IOException {
        var id = dao.fetchEpubObjectKey(bookId);
        var obj = osao.readObject(id);
        libUtils.writeS3ObjectToResponse(obj, response);
    }

    @GetMapping(value = "/audiobook", produces = "application/epub+zip")
    public void getAudiobook(@RequestParam(value = "bookId") int bookId, HttpServletResponse response) throws IOException {
        var id = dao.fetchAudiobookObjectKey(bookId);
        var obj = osao.readObject(id);
        libUtils.writeS3ObjectToResponse(obj, response);
    }

    @GetMapping(value="/asset")
    public ModelAndView uploadAsset() {
        return new LibraryModelAndView("/upload-asset-form");
    }

    @PostMapping(value="/asset")
    public void receiveAssetBinary(@RequestParam("file") MultipartFile file) throws IOException {
        osao.uploadObject(file.getInputStream(), file.getSize(), file.getOriginalFilename());
    }

    @PostMapping(value="/uploadCover")
    public void receiveCoverImage(@RequestParam("file") MultipartFile file, @RequestParam("bookId") int bookId) throws IOException {
        bookImageCache.cacheUploadedCoverForBook(file.getOriginalFilename(), file.getInputStream(), bookId);
    }

    @GetMapping("/forms/editbook/{id}")
    public ModelAndView editBookFormDisplay(@PathVariable("id") int bookId) throws IOException {
        var mv = new LibraryModelAndView("/edit-book-form");
        populateEditBookPageModel(mv, dao.fetchBook(bookId), true);
        return mv;
    }

    @PostMapping("/forms/editbook/{id}")
    public ModelAndView editBookFormHandleSubmission(@PathVariable("id") int bookId, HttpServletRequest req) throws IOException {

        var formData = new EditBookFormData(req);
        var validation = formData.validate();

        var formBook = validation.book();
        formBook.setId(bookId);
        var errors = validation.errors();

        var dbBook = dao.fetchBook(bookId);
        var msg = formBook.equals(dbBook) ? "books match!" : "books don't match :(";
        errors.add(0, msg);

        // if there are any validation errors, display them on this page
        if (errors.size() > 0) {
            var mv = new LibraryModelAndView("/edit-book-form");
            populateEditBookPageModel(mv, formBook, true);
            mv.addObject("errors", errors);
            return mv;
        }
        else {
            throw new RuntimeException("Not yet implemented.");
        }
    }

    protected record EditBookFormValidation(List<String> errors, Book book) {
    }

    protected record EditBookFormData(HttpServletRequest request) implements FormData {

        public EditBookFormValidation validate() {
            var errors = new LinkedList<String>();
            var book = new Book();
            book.setTitle(getString("title"));
            book.setAltTitle1(getString("altTitle1"));
            book.setAltTitle2(getString("altTitle2"));
            book.setAuthor(getString("author"));
            book.setAuthor2(getString("author2"));
            book.setAuthor3(getString("author3"));
            handle(() -> book.setPublicationYear(getInteger("publicationYear")), errors, "Bad publication year");
            book.setSeries(getString("series"));
            handle(() ->book.setSeriesSequence(getInteger("seriesSequence")), errors, "Bad format series sequence number");
            handle(() -> book.setAcquisitionDate(getDate("acquisitionDate")), errors, "Bad format acquisition date");
            book.setTags(getTags());
            book.setEpubObjectKey(getString("epubObjectKey"));
            book.setMobiObjectKey(getString("mobiObjectKey"));
            book.setAudiobookObjectKey(getString("audiobookObjectKey"));
            book.setAmazonId(getString("asin"));
            return new EditBookFormValidation(errors, book);
        }

        /**
         * combine the tags from the radio boxes and the text field into one list
         */
        private List<String> getTags() {

            // get the tags from the check boxes (make a copy so we get a mutable list)
            var tags = new LinkedList<String>(getStrings("tags"));

            // if any additional tags were specified in the text field
            var newTagsCSV = getString("newTags");
            if (!Utils.isBlank(newTagsCSV)) {

                // for each new tag in the text area
                var newTagsArray = newTagsCSV.split(",");
                for (var newTag : newTagsArray) {

                    // clean any white space from the tag
                    var trimNewTag = newTag.trim();

                    // if the tag isn't a duplicate of one of the checkboxes, add it to the list
                    if (newTag.length() > 0 && !tags.contains(trimNewTag)) {
                        tags.add(trimNewTag);
                    }
                }
            }

            return tags;
        }

        private void handle(Runnable operation, List<String> errors, String msg) {
            try {
                operation.run();
            }
            catch (Exception x) {
                errors.add(msg + " -- " + x.getMessage());
            }
        }

        @Override
        public SimpleDateFormat dateFormat() {
            return DATE_FORMAT;
        }
    }

    protected void populateEditBookPageModel(ModelAndView mv, Book book, boolean isEdit) throws IOException {

        // get lists of object ids of each type that are not currently attached to any books in the database
        var objIds = libUtils.fetchUnattachedObjectIds();
        var epubs = objIds.stream().filter(o -> o.toLowerCase().endsWith("epub")).collect(Collectors.toList());
        var mobis = objIds.stream().filter(o -> o.toLowerCase().endsWith("mobi")).collect(Collectors.toList());
        var audiobooks = objIds.stream().filter(o -> o.toLowerCase().endsWith("m4b")).collect(Collectors.toList());

        // if there is an epub already associated with this book, add it to the list
        var epub = book.getEpubObjectKey();
        if (epub != null) {
            epubs.add(epub);
            Collections.sort(epubs);
        }

        // if there is an audiobook already attached to this book, add it to the list
        var audiobook = book.getAudiobookObjectKey();
        if (audiobook != null) {
            audiobooks.add(audiobook);
            Collections.sort(audiobooks);
        }

        mv.addObject("operation", "edit");
        mv.addObject("book", book);
        mv.addObject("authorList", dao.fetchAllAuthors());
        mv.addObject("seriesList", dao.fetchAllSeries());
        mv.addObject("tagList", dao.fetchAllTags());
        mv.addObject("unattachedEpubs", epubs);
        mv.addObject("unattachedMobis", mobis);
        mv.addObject("unattachedAudiobooks", audiobooks);
        mv.addObject("bookImages", bookImageCache.imagesFromBook(book.getEpubObjectKey()));
        mv.addObject("hasCoverImage", isEdit ? dao.hasCoverImage(book.getId()) : false);
        mv.addObject("operation", isEdit ? "edit" : "add");
        mv.addObject("formAction", isEdit ? "/forms/editbook/" + book.getId() : "/api/addBook");
    }

    /**
     * Gets candidate images for an ebook.
     */
    @GetMapping("/coverImages")
    public ModelAndView displayCoverImagesForBook(@RequestParam("epubObjId") String epubObjId) throws IOException {
        var mv = new LibraryModelAndView("/cover-images-div");
        mv.addObject("images", bookImageCache.imagesFromBook(epubObjId));
        mv.addObject("epubObjId", epubObjId);
        return mv;
    }

    @GetMapping("/epubImage")
    public Resource getImageFromBook(@RequestParam("objId") String epubObjId, @RequestParam("file") String filename) throws FileNotFoundException {
        return new InputStreamResource(new FileInputStream(bookImageCache.getBookImageFromCache(epubObjId, filename)));
    }

    @GetMapping(value="/uploadedImage")
    public Resource getUploadedImage(@RequestParam("bookId") int bookId, @RequestParam("file") String filename) throws FileNotFoundException {
        return new InputStreamResource(new FileInputStream(bookImageCache.getUploadedBookFromCache(bookId, filename)));
    }

    /**
     * Updates the metadata for the given book.
     *
     * @param id  The book id.
     * @return
     */
    @PostMapping("/metadata/book/{id}")
    public ModelAndView updateMetadata(@PathVariable("id") int id) {
        // TODO
        return null;
    }

    @GetMapping("/addBooks")
    public ModelAndView addBooks() {

        // get lists of object ids of each type that are not currently attached to any books in the database
        var objIds = libUtils.fetchUnattachedObjectIds();
        var epubs = objIds.stream().filter(o -> o.toLowerCase().endsWith("epub")).collect(Collectors.toList());

        var mv = new LibraryModelAndView("/add-books");
        mv.addObject("epubs", epubs);
        return mv;
    }

    @GetMapping("/addBook")
    public ModelAndView displayAddBookForm(@RequestParam("epub") String epubObjKey) throws IOException {

        var mv = new LibraryModelAndView("/edit-book-form");

        var mobiObjectKey = matchingMobi(epubObjKey);

        var book = new BookForm();
        book.setEpubObjectKey(epubObjKey);
        book.setMobiObjectKey(mobiObjectKey);
        populateEditBookPageModel(mv, book, false);

        return mv;
    }

    @PostMapping(value="/api/addBook", produces="application/json;charset=UTF-8")
    public FormValidation addBook(@RequestBody BookForm book) {
        var v = new FormValidation();
        try {
            v.checkIsBlank(book.getTitle(), "Missing title");
            v.checkIsBlank(book.getAuthor(), "Missing author");
            v.checkIsBlank(book.getPublicationYearString(), "Missing publication year");
            v.checkNotPattern(book.getPublicationYearString(), "\\d{4}", "Bad format publication year");
            v.check(Utils.isNotBlank(book.getSeries()) && Utils.isBlank(book.getSeriesSequenceString()), "Missing series # (required when series is provided)");
            v.checkNotPattern(book.getSeriesSequenceString(), "\\d+", "Bad format series #");
            v.checkIsBlank(book.getAcquisitionDateString(), "Missing acquisition date");
            v.checkNotPattern(book.getAcquisitionDateString(), "\\d{1,2}/\\d{1,2}/\\d{4}", "Acquisition date must be mm/dd/yyyy");
            v.checkIsBlank(book.getEpubObjectKey(), "No EPUB selected");

            if (v.isOkay()) {
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
        }
        catch (Exception x) {
            v.add("Exception thrown: " + x.getMessage());
        }
        return v;
    }

    public String matchingMobi(String epub) {
        String mobi = null;
        if (epub != null) {
            var lcEpub = epub.toLowerCase();
            if (lcEpub.endsWith(".epub")) {
                var base = lcEpub.substring(0, lcEpub.length() - 5);
                mobi = base + ".mobi";
            }
        }
        return mobi;
    }

    public static class FormValidation extends LinkedList<String> {
        public void checkNotPattern(String value, String regex, String message) {
            if (!Utils.isBlank(value) && !value.matches(regex)) {
                add(message);
            }
        }
        public void checkIsBlank(String value, String message) {
            if (Utils.isBlank(value)) {
                add(message);
            }
        }
        public void check(boolean condition, String message) {
            if (condition) {
                add(message);
            }
        }
        public boolean isOkay() {
            return size() == 0;
        }
    }

    public static class BookForm extends Book {
        String coverImage;
        String newTags;
        String publicationYearString;
        String seriesSequenceString;
        String acquisitionDateString;

        @JsonFormat(pattern="MM/dd/yyyy")
        public void setAcquisitionDate(Date acquisitionDate) {
            super.setAcquisitionDate(acquisitionDate);
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public String getNewTags() {
            return newTags;
        }

        public void setNewTags(String newTags) {
            this.newTags = newTags;
        }

        public String getPublicationYearString() {
            return publicationYearString;
        }

        public void setPublicationYearString(String publicationYearString) {
            this.publicationYearString = publicationYearString;
        }

        public String getSeriesSequenceString() {
            return seriesSequenceString;
        }

        public void setSeriesSequenceString(String seriesSequenceString) {
            this.seriesSequenceString = seriesSequenceString;
        }

        public String getAcquisitionDateString() {
            return acquisitionDateString;
        }

        public void setAcquisitionDateString(String acquisitionDateString) {
            this.acquisitionDateString = acquisitionDateString;
        }

        public void merge() throws ParseException {
            publicationYear = Integer.parseInt(publicationYearString);
            seriesSequence = Integer.parseInt(seriesSequenceString);
            acquisitionDate = DATE_FORMAT.parse(acquisitionDateString);
            if (!Utils.isBlank(newTags)) {
                var tags = Arrays.stream(newTags.split(",")).map(String::trim).collect(Collectors.toList());
            }
        }

        @Override
        public String toString() {
            var sb = new StringBuilder(super.toString());
            sb.append("\ncover img: ").append(coverImage);
            sb.append("\nnew tags: ").append(newTags);
            sb.append("\npublication year string: ").append(publicationYearString);
            sb.append("\nacquisition date string: ").append(acquisitionDateString);
            return sb.toString();
        }
    }
}
