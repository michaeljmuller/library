package org.themullers.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.themullers.library.db.LibraryDAO;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class handles HTTP requests from the user's web browser.
 */
@RestController
@SpringBootApplication
public class WebApplication {

    LibraryDAO dao;

    @Autowired
    public WebApplication(LibraryDAO dao) {
        this.dao = dao;
    }

    /**
     * This is the main entry point for the application.  Any arguments are passed
     * through to Spring Boot.
     * @param args  parameters provided on the command line when this application was launched
     */
    public static void main(String args[]) {
        SpringApplication.run(WebApplication.class, args);
    }

    /**
     * Handle a request to render the application's home page.
     * @return  information needed to render the home page
     */
    @GetMapping("/")
    public ModelAndView home() {
        return new LibraryModelAndView("home");
    }

    /**
     * Handle a request to render the "authors" page.
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
     * @param author the author whose assets are to be rendered
     * @return information needed to render the page
     */
    @GetMapping("/author")
    public ModelAndView author(@RequestParam(name="name") String author) {

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
     * @param book the s3 object id of the book whose cover should be rendered
     * @return  information needed to render the page
     */
    @GetMapping(value = "/cover", produces = "image/jpeg")
    public byte[] cover(@RequestParam(name="book") String book) throws IOException {
        try (var fos = new FileOutputStream("/Users/mmuller/Desktop/foo.jpeg")) {
            return dao.fetchImageForEbook(book);
        }
    }

    /**
     * Handle a request to render the page that allows the user to upload or download a spreadsheet
     * containing metadata for all the assets in the library.
     * @param msg  an optional message to be displayed indicating results from an earlier operation
     * @param status  an optional indicator of the success of an earlier operation; should be "success" or "failure"
     * @param stackDump  an optional stack trace with diagnostic information concerning the failure of an earlier operation
     * @return  information needed to render the page
     */
    @GetMapping("metadata")
    public ModelAndView metadata(@RequestParam(name="msg", required=false) String msg, @RequestParam(name="status", required=false) String status,  @ModelAttribute(name="stackDump") String stackDump) {
        var mv = new LibraryModelAndView("metadata");
        mv.addObject("msg", msg);
        mv.addObject("status", status);
        mv.addObject("stackDump", stackDump);
        return mv;
    }

    /**
     * Handle a request to download a spreadsheet containing metadata for each asset in the library.
     * @return  a spreadsheet
     * @throws IOException  thrown if an unexpected error occurs generating the spreadsheet
     */
    @GetMapping(value = "/ss", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public byte[] downloadSpreadsheet() throws IOException {
        return Spreadsheet.fromDatabase(dao);
    }

    /**
     * Handle a request to upload a spreadsheet containing metadata for each asset in the library.
     * @param file  the spreadsheet to upload
     * @param redirectAttributes  a Spring Boot object that allows this method to store information to be rendered in another web page
     * @return  a view object that instructs Spring Boot to redirect the user to the "metadata" page
     * @throws IOException  thrown if an unexpected error occurs processing the spreadsheet
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
                Spreadsheet.toDatabase(dao, bytes);
            }
            catch (Exception x) {
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
    public ModelAndView login(@RequestParam(value="error", required=false) String error, @RequestParam(value="logout", required=false) String logout) {
        var mv = new LibraryModelAndView("/niceLogin");
        if (logout != null) {
            mv.addObject("msg", "You have been logged out.");
        }
        if (error != null) {
            mv.addObject("msg", "Invalid login; please try again.");
        }
        return mv;
    }

    class LibraryModelAndView extends ModelAndView {
        public LibraryModelAndView(String view) {
            super(view);
            if (SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof User user) {
                addObject("userFirstName", user.getFirstName());
                addObject("userLastName", user.getLastName());
            }
        }
    }
}
