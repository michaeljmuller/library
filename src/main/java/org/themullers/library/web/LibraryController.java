package org.themullers.library.web;

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
import org.themullers.library.web.forms.BookForm;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * This is the main controller for the Library application; it handles requests to display web
 * pages after the user is authenticated.  API requests by JavaScript on those pages are handled
 * by the RestAPIController.  The PasswordResetController contains the implementation pages to
 * support the "I forgot my password" process.
 */
@RestController  // TODO: change this to a plain Controller (not REST) ?
public class LibraryController {

    private Logger logger = LoggerFactory.getLogger(LibraryController.class);

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
     * The home page displays some statistics, a search area, and a few recently added books.
     *
     * @return a view object containing the template needed to render the home page
     */
    @GetMapping("/")
    public ModelAndView home() {
        var mv = new LibraryModelAndView("home");

        mv.addObject("books", dao.fetchNewestBooks(6, 0));
        mv.addObject("titleCount", dao.countTitles());
        mv.addObject("audiobookCount", dao.countAudiobooks());
        mv.addObject("authorCount", dao.countAuthors());

        return mv;
    }

    /**
     * Handle a request to render the "authors" page.
     *
     * @return information needed to render the page
     */
    @GetMapping("/authors")
    public ModelAndView authors(@RequestParam(value="sortBy", required=false, defaultValue="first") String sortBy) {

        var authorInfoList = dao.getAuthorInfo();

        boolean sortByLast = sortBy.equalsIgnoreCase("last");

        if (sortByLast) {
            Collections.sort(authorInfoList, (a, b) -> {
                return a.getByLast().compareTo(b.getByLast());
            });
        }

        var map = authorInfoList.stream().collect(groupingBy(a -> {
            var name = sortByLast ? a.getByLast() : a.getName();
            return name.substring(0,1).toUpperCase();
        }));

        var mv = new LibraryModelAndView("authors");
        mv.addObject("authorInfoMap", map);
        mv.addObject("sortBy", sortByLast ? "last" : "first");
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
     * Handle a request to provide a cover image for a book.
     *
     * @param bookId the id of the book whose cover should be rendered
     * @return the binary contents of the image
     */
    @GetMapping(value = "/book/cover/{id}", produces = "image/jpeg")
    public byte[] cover(@PathVariable(name = "id") int bookId) throws IOException {
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

    /**
     * Display a page that lists all the tags that have been associated with books in the library.
     * @return  a view object containing a reference to the template that should be used to render the tags page
     */
    @GetMapping("/tags")
    public ModelAndView tags() {
        var mv = new LibraryModelAndView("tags");
        mv.addObject("tags", dao.getTags());
        return mv;
    }

    /**
     * Display a page with all the books the have the specified tag applied to them.
     * @param tag  the tag to use to filter the books returned
     * @return  a view object containing a reference to the template that should be used to render the tag page
     */
    @GetMapping("/tag/{tag}")
    public ModelAndView tag(@PathVariable("tag") String tag, @RequestParam(value="page", defaultValue="1") int page) {

        int booksPerPage = 50;

        // fetch one more book than the number we display on the page so know whether there's another page of results
        var taggedBooks = dao.fetchBooksWithTag(tag, booksPerPage+1, (page-1) * booksPerPage);

        // if there are more results after this page, adjust the count and throw out the last result
        // (it's really the first result of the next page)
        var numBooks = taggedBooks.size();
        var hasMore = numBooks > booksPerPage;
        if (hasMore) {
            numBooks--;
            taggedBooks.remove(numBooks);
        }

        // calculate the overall index of the first book on this page
        var firstBookNum = ((page-1) * booksPerPage) + 1;

        var mv = new LibraryModelAndView("tag");
        mv.addObject("tag", tag);
        mv.addObject("books", taggedBooks);
        mv.addObject("page", page);
        mv.addObject("nextPage", page+1);
        mv.addObject("firstBookNum", firstBookNum);
        mv.addObject("lastBookNum", firstBookNum + numBooks - 1);
        mv.addObject("hasMore", hasMore);
        return mv;
    }

    /**
     * Display the login page
     * @param error  whether this page is being re-displayed in response to a failed login
     * @param logout  whether this page is being displayed after a user logs out
     * @return  a view object containing a reference to the template that should be used to render the login page
     */
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

    /**
     * Downloads an EPUB.
     *
     * @param bookId  the id of the book whose EPUB should be downloaded
     * @param response  the http response object that the EPUB will be written to
     * @throws IOException  throws if an unexpected error occurs while downloading the EPUB
     */
    @GetMapping(value = "/book/epub/{id}", produces = "application/epub+zip")
    public void getEpub(@PathVariable(value = "id") int bookId, HttpServletResponse response) throws IOException {
        var id = dao.fetchEpubObjectKey(bookId);
        var obj = osao.readObject(id);
        libUtils.writeS3ObjectToResponse(obj, response);
    }

    /**
     * Downloads an MOBI.
     *
     * @param bookId  the id of the book whose MOBI should be downloaded
     * @param response  the http response object that the MOBI will be written to
     * @throws IOException  throws if an unexpected error occurs while downloading the MOBI
     */
    @GetMapping(value = "/book/mobi/{id}", produces = "application/epub+zip")
    public void getMobi(@PathVariable(value = "id") int bookId, HttpServletResponse response) throws IOException {
        var id = dao.fetchMobiObjectKey(bookId);
        var obj = osao.readObject(id);
        libUtils.writeS3ObjectToResponse(obj, response);
    }

    /**
     * Downloads an audiobook.
     *
     * @param bookId  the id of the book whose audiobook should be downloaded
     * @param response  the http response object that the audiobook will be written to
     * @throws IOException  throws if an unexpected error occurs while downloading the audiobook
     */
    @GetMapping(value = "/book/m4b/{id}", produces = "audio/mp4a-latm")
    public void getAudiobook(@PathVariable(value = "id") int bookId, HttpServletResponse response) throws IOException {
        var id = dao.fetchAudiobookObjectKey(bookId);
        var obj = osao.readObject(id);
        libUtils.writeS3ObjectToResponse(obj, response);
    }

    /**
     * Displays the form that allows a user to add a book to the library.
     * @param epubObjKey  the EPUB of the book that we're adding.
     * @param httpReferrer  the page that sent the user to this form (so we can send them back when done)
     * @return  a view object containing the template that should be used to render the "add book" page
     * @throws IOException  thrown if an IO error occurs while we're trying to render this page
     */
    @GetMapping("/addBook")
    public ModelAndView displayAddBookForm(@RequestParam("epub") String epubObjKey, @RequestHeader(value="Referer", required=false) String httpReferrer) throws IOException {

        // add the EPUB (and the associated MOBI) to a new book
        var book = new BookForm();
        book.setEpubObjectKey(epubObjKey);
        book.setMobiObjectKey(LibUtils.matchingMobi(epubObjKey));

        // populate the model
        var mv = new LibraryModelAndView("/edit-book-form");
        mv.addObject("referrer", httpReferrer);
        populateModelForEditBookPage(mv, book, false);

        return mv;
    }

    /**
     * Displays a form allowing the user to edit a book's metadata.
     * @param bookId  the id of the book to be edited
     * @param httpReferrer  the page that sent the user to this form (so we can send them back when done)
     * @return  a view object containing the template that should be used to render the "edit book" page
     * @throws IOException  thrown if an IO error occurs while we're trying to render this page
     */
    @GetMapping("/editBook/{id}")
    public ModelAndView editBookFormDisplay(@PathVariable("id") int bookId, @RequestHeader(value="Referer", required=false) String httpReferrer) throws IOException {

        // populate the model
        var mv = new LibraryModelAndView("/edit-book-form");
        mv.addObject("referrer", httpReferrer);
        populateModelForEditBookPage(mv, dao.fetchBook(bookId), true);

        return mv;
    }

    /**
     * Populate the model used to render an add/edit book page.
     * @param mv  the model/view object used to render the page
     * @param book  the java object with information about the book
     * @param isEdit  whether this model is for an edit operation (as opposed to an add operation)
     * @throws IOException  thrown if an IO error occurs while we're trying to populate the model
     */
    protected void populateModelForEditBookPage(ModelAndView mv, Book book, boolean isEdit) throws IOException {

        /*
        When the user is picking what assets to associate with this book, we want to exclude
        assets that are already attached to other books; we don't want to have the same book
        twice in the library.
         */

        // get lists of object ids of each type that are not currently attached to any books in the database
        var objIds = libUtils.fetchUnattachedObjectKeys();
        var epubs = objIds.stream().filter(o -> o.toLowerCase().endsWith("epub")).collect(Collectors.toList());
        var mobis = objIds.stream().filter(o -> o.toLowerCase().endsWith("mobi")).collect(Collectors.toList());
        var audiobooks = objIds.stream().filter(o -> o.toLowerCase().endsWith("m4b")).collect(Collectors.toList());

        // if we're editing a book, include as options the assets that are CURRENTLY attached to that book
        if (isEdit) {
            addToList(book.getEpubObjectKey(), epubs);
            addToList(book.getMobiObjectKey(), mobis);
            addToList(book.getAudiobookObjectKey(), audiobooks);
        }

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
        mv.addObject("formAction", isEdit ? "/api/book/" + book.getId() : "/api/book");
    }

    /**
     * Adds an asset to a list and then alphabetizes the list.
     * @param asset  the asset to add to the list
     * @param listOfAssets  the list of assets
     */
    protected void addToList(String asset, List<String> listOfAssets) {
        if (asset != null) {
            listOfAssets.add(asset);
            Collections.sort(listOfAssets);
        }
    }

    /**
     * Renders a page with detailed information about a book.
     * @param bookId  the id of the book to display.
     * @return  a view object containing the template that should be used to render the "book details" page
     */
    @GetMapping("/book/{id}")
    public ModelAndView showBookDetails(@PathVariable("id") int bookId) {
        var mv = new LibraryModelAndView("/book-details");
        mv.addObject("book", dao.fetchBook(bookId));
        return mv;
    }

    /**
     * Returns the binary content of an image from an EPUB.
     * @param epubObjKey  the S3 object key of the EPUB
     * @param imageFilename  the filename of the image in the EPUB
     * @return  the image binary
     * @throws FileNotFoundException  thrown if the requested image can't be found in the EPUB
     */
    @GetMapping("/epubImage")
    public Resource getImageFromBook(@RequestParam("epubObjKey") String epubObjKey, @RequestParam("file") String imageFilename) throws FileNotFoundException {
        return new InputStreamResource(new FileInputStream(bookImageCache.getEpubBookImageFromCache(epubObjKey, imageFilename)));
    }

    /**
     * Returns the binary content of an uploaded cover image.
     * @param bookId  the id of the book for which this image was uploaded
     * @param filename  the filename of the uploaded image
     * @return  the image binary
     * @throws FileNotFoundException  thrown if the requested image can't be found
     */
    @GetMapping(value="/uploadedImage")
    public Resource getUploadedImage(@RequestParam("bookId") int bookId, @RequestParam("file") String filename) throws FileNotFoundException {
        return new InputStreamResource(new FileInputStream(bookImageCache.getUploadedBookImageFromCache(bookId, filename)));
    }

    /**
     * Displays a page with information about assets that have been uploaded, but not yet added to the library.
     * @return  a view object containing the template that should be used to render the "new assets" page
     */
    @GetMapping("/admin/newAssets")
    public ModelAndView newAssets() {

        // get lists of object ids of each type that are not currently attached to any books in the database
        var objIds = libUtils.fetchUnattachedObjectKeys();
        var epubs = objIds.stream().filter(o -> o.toLowerCase().endsWith(".epub")).collect(Collectors.toList());
        var mobis = objIds.stream().filter(o -> o.toLowerCase().endsWith(".mobi")).collect(Collectors.toList());
        var audiobooks = objIds.stream().filter(o -> o.toLowerCase().endsWith(".m4b")).collect(Collectors.toList());

        // get a list of titles and authors formatted like this: title (author)
        var titlesAndAuthors = dao.fetchAllBooks().stream().map(b -> b.getTitle() + " (" + b.getAuthor() + ")").sorted().collect(Collectors.toList());

        var mv = new LibraryModelAndView("/new-assets");
        mv.addObject("epubs", epubs);
        mv.addObject("mobis", mobis);
        mv.addObject("audiobooks", audiobooks);
        mv.addObject("books", titlesAndAuthors);
        return mv;
    }

    /**
     * Display a page of the most recently-acquired books.
     * @param page  which page of books to display (bigger number for older books)
     * @return  a view object containing the template that should be used to render the "recent acquisitions" page
     */
    @GetMapping("/recents")
    public ModelAndView recents(@RequestParam(value="page", defaultValue="1") int page) {
        var mv = new LibraryModelAndView("/recent-acquisitions");

        int booksPerPage = 50;

        // fetch one more book than the number we display on the page so know whether there's another page of results
        var newReleases = dao.fetchNewestBooks(booksPerPage+1, (page-1) * booksPerPage);

        // if there are more results after this page, adjust the count and throw out the last result
        // (it's really the first result of the next page)
        var numBooks = newReleases.size();
        var hasMore = numBooks > booksPerPage;
        if (hasMore) {
            numBooks--;
            newReleases.remove(numBooks);
        }

        // calculate the index of the first book on this page
        var firstBookNum = ((page-1) * booksPerPage) + 1;

        mv.addObject("books", newReleases);
        mv.addObject("page", page);
        mv.addObject("nextPage", page+1);
        mv.addObject("firstBookNum", firstBookNum);
        mv.addObject("lastBookNum", firstBookNum + numBooks - 1);
        mv.addObject("firstBookDate", newReleases.get(0).getAcquisitionDate());
        mv.addObject("lastBookDate",  newReleases.get(numBooks-1).getAcquisitionDate());
        mv.addObject("hasMore", hasMore);

        return mv;
    }

    @GetMapping("/search")
    public ModelAndView search(@RequestParam(value= "for", required=false) String searchText) {
        var mv = new LibraryModelAndView("/search");

        if (!Utils.isBlank(searchText)) {
            mv.addObject("searchText", searchText);
            mv.addObject("titles", dao.searchTitles(searchText));
            mv.addObject("authors", dao.searchAuthors(searchText));
            mv.addObject("seriesMap", dao.searchSeries(searchText));
        }

        return mv;
    }
}
