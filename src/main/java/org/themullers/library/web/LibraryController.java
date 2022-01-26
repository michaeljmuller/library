package org.themullers.library.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.themullers.library.Asset;
import org.themullers.library.LibUtils;
import org.themullers.library.SpreadsheetService;
import org.themullers.library.Utils;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@RestController
public class LibraryController {

    LibraryDAO dao;
    LibraryOSAO osao;
    SpreadsheetService ss;

    @Autowired
    public LibraryController(LibraryDAO dao, LibraryOSAO osao, SpreadsheetService ss) {
        this.dao = dao;
        this.osao = osao;
        this.ss = ss;
    }

    /**
     * Handle a request to render the application's home page.
     * The home page displays the most recently added assets.
     *
     * @param page can be a number > 1 to display more (older) assets
     * @return information needed to render the home page
     */
    @GetMapping("/")
    public ModelAndView home(@RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        var mv = new LibraryModelAndView("home");

        // get the most recent assets
        int assetsPerPage = 6;
        var newReleases = dao.fetchNewestAssets(assetsPerPage, page * assetsPerPage);
        mv.addObject("page", page);
        mv.addObject("assets", newReleases);
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
     * @param author the author whose assets are to be rendered
     * @return information needed to render the page
     */
    @GetMapping("/author")
    public ModelAndView author(@RequestParam(name = "name") String author) {

        // get the assets, group them by series, and then pull out the stand-alone assets to be handled uniquely
        var assets = dao.fetchAssetsForAuthor(author);
        var groupedAssets = LibUtils.groupAssetsBySeries(assets);
        var standalone = groupedAssets.remove(LibUtils.STANDALONE);

        // build the model
        var mv = new LibraryModelAndView("author");
        mv.addObject("authorName", author);
        mv.addObject("groupedAssets", groupedAssets);
        mv.addObject("standalone", standalone);
        return mv;
    }

    /**
     * Handle a request to provide a cover image for an asset.
     *
     * @param book the s3 object id of the book whose cover should be rendered
     * @return information needed to render the page
     */
    @GetMapping(value = "/cover", produces = "image/jpeg")
    public byte[] cover(@RequestParam(name = "book") String book) throws IOException {
        return dao.fetchImageForEbook(book);
    }

    /**
     * Handle a request to render the page that allows the user to upload or download a spreadsheet
     * containing metadata for all the assets in the library.
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
     * Handle a request to download a spreadsheet containing metadata for each asset in the library.
     *
     * @return a spreadsheet
     * @throws IOException thrown if an unexpected error occurs generating the spreadsheet
     */
    @GetMapping(value = "/ss", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public byte[] downloadSpreadsheet() throws IOException {
        return ss.download();
    }

    /**
     * Handle a request to upload a spreadsheet containing metadata for each asset in the library.
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
    public void getEbook(@RequestParam(value = "id") int assetId, HttpServletResponse response) throws IOException {
        var id = dao.fetchEbookObjectKey(assetId);
        var obj = osao.readObject(id);
        LibUtils.writeS3ObjectToResponse(obj, response);
    }

    @GetMapping(value = "/audiobook", produces = "application/epub+zip")
    public void getAudiobook(@RequestParam(value = "id") int assetId, HttpServletResponse response) throws IOException {
        var id = dao.fetchAudiobookObjectKey(assetId);
        var obj = osao.readObject(id);
        LibUtils.writeS3ObjectToResponse(obj, response);
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

    @GetMapping("/forms/editbook/{id}")
    public ModelAndView displayEditBookForm(@PathVariable("id") int assetId) {

        var mv = new LibraryModelAndView("/edit-book-form");

        // fetch the asset to edit using the id from the URL
        var asset = new Asset(); asset.setId(assetId);
        asset.setTitle("the title of the book");
        asset.addTag("Humor");
        asset.addTag("First Contact");
        asset.setAcquisitionDate(new Date());
        //mv.addObject("asset", dao.fetchAsset(assetId));

        // get lists of object ids of each type that are not currently attached to any assets in the database
        var objIds = LibUtils.unattachedAssetObjectIds(dao, osao);
        var epubs = objIds.stream().filter(o -> o.toLowerCase().endsWith("epub")).collect(Collectors.toList());
        var audiobooks = objIds.stream().filter(o -> o.toLowerCase().endsWith("m4b")).collect(Collectors.toList());

        // if there is an epub already associated with this asset, add it to the list
        var epub = asset.getEbookS3ObjectKey();
        if (epub != null) {
            epubs.add(epub);
            Collections.sort(epubs);
        }

        // if there is an audiobook already attched to this asset, add it to the list
        var audiobook = asset.getAudiobookS3ObjectKey();
        if (audiobook != null) {
            audiobooks.add(audiobook);
            Collections.sort(audiobooks);
        }

        mv.addObject("asset", asset);
        mv.addObject("authorList", dao.fetchAllAuthors());
        mv.addObject("seriesList", dao.fetchAllSeries());
        mv.addObject("tagList", dao.fetchAllTags());
        mv.addObject("unattachedEpubs", epubs);
        mv.addObject("unattachedAudiobooks", audiobooks);
        return mv;
    }

    /**
     * Updates the metadata for the given asset.
     *
     * @param id  The asset id.
     * @return
     */
    @PostMapping("/metadata/book/{id}")
    public ModelAndView updateMetadata(@PathVariable("id") int id) {
        // TODO
        return null;
    }

}
