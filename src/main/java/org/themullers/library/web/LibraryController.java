package org.themullers.library.web;

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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class LibraryController {

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
    public void receiveAssetBinary(@RequestParam("file") MultipartFile file) throws FileNotFoundException, IOException {
        var size = file.getSize();
        osao.uploadObject(file.getInputStream(), file.getSize(), file.getOriginalFilename());
    }

    @PostMapping(value="/uploadCover")
    public void receiveCoverImage(@RequestParam("file") MultipartFile file, @RequestParam("bookId") int bookId) throws IOException {
        bookImageCache.cacheUploadedCoverForBook(file.getOriginalFilename(), file.getInputStream(), bookId);
    }

    @GetMapping("/forms/editbook/{id}")
    public ModelAndView editBookFormDisplay(@PathVariable("id") int bookId) throws IOException {
        var mv = new LibraryModelAndView("/edit-book-form");
        populateEditBookPageModel(mv, dao.fetchBook(bookId));
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
            populateEditBookPageModel(mv, formBook);
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
        static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

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
            return new EditBookFormValidation(errors, book);
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
            return dateFormat;
        }
    }

    protected void populateEditBookPageModel(ModelAndView mv, Book book) throws IOException {

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

        mv.addObject("book", book);
        mv.addObject("authorList", dao.fetchAllAuthors());
        mv.addObject("seriesList", dao.fetchAllSeries());
        mv.addObject("tagList", dao.fetchAllTags());
        mv.addObject("unattachedEpubs", epubs);
        mv.addObject("unattachedMobis", mobis);
        mv.addObject("unattachedAudiobooks", audiobooks);
        mv.addObject("bookImages", bookImageCache.imagesFromBook(book.getEpubObjectKey()));
        mv.addObject("hasCoverImage", dao.hasCoverImage(book.getId()));
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
}
