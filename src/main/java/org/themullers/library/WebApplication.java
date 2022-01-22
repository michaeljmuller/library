package org.themullers.library;

import com.amazonaws.services.s3.model.S3Object;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.themullers.library.auth.PasswordGenerator;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.email.EmailSender;
import org.themullers.library.email.EmailTemplate;
import org.themullers.library.email.EmailTemplateProcessor;
import org.themullers.library.s3.LibraryOSAO;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * This class handles HTTP requests from the user's web browser.
 */
@RestController
@SpringBootApplication
@Configuration
public class WebApplication {

    LibraryDAO dao;
    LibraryOSAO osao;
    SpreadsheetProcessor spreadsheetProcessor;
    EmailSender emailSender;
    EmailTemplateProcessor emailTemplateProcessor;
    PasswordGenerator pwGenerator;
    String applicationBaseURL;

    protected final static int TOKEN_LIFESPAN_MINUTES = 15;


    @Autowired
    public WebApplication(LibraryDAO dao, LibraryOSAO osao, SpreadsheetProcessor spreadsheetProcessor, EmailSender emailSender, EmailTemplateProcessor emailTemplateProcessor, PasswordGenerator pwGenerator, @Value("${application.base.url}") String applicationBaseURL) {
        this.dao = dao;
        this.osao = osao;
        this.spreadsheetProcessor = spreadsheetProcessor;
        this.emailSender = emailSender;
        this.emailTemplateProcessor = emailTemplateProcessor;
        this.pwGenerator = pwGenerator;
        this.applicationBaseURL = applicationBaseURL;
    }

    /**
     * This is the main entry point for the application.  Any arguments are passed
     * through to Spring Boot.
     *
     * @param args parameters provided on the command line when this application was launched
     */
    public static void main(String args[]) {
        SpringApplication.run(WebApplication.class, args);
    }

    /**
     * Handle a request to render the application's home page.
     * The home page displays the most recently added assets.
     *
     * @return information needed to render the home page
     * @page can be a number > 1 to display more (older) assets
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
        return spreadsheetProcessor.download();
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
                spreadsheetProcessor.upload(bytes);
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

    @GetMapping("/pw-reset")
    public ModelAndView displayPasswordResetForm(@RequestParam(value="msg", required=false) String msg) {
        var mv = new ModelAndView("password-reset");
        mv.addObject("msg", msg);
        return mv;
    }

    @PostMapping("/pw-reset")
    public ModelAndView processPasswordResetForm(@RequestParam("email") String email) throws TemplateException, MessagingException, IOException {

        // check to make sure the user provided something that looks like a valid email
        if (!isValidFormatEmail(email)) {
            var mv = new LibraryModelAndView("/password-reset");
            mv.addObject("msg", "Please enter a valid format email");
            return mv;
        }

        sendPasswordResetEmail(email);
        return new ModelAndView("redirect:/pw-reset-email-sent");
    }

    @GetMapping("/pw-reset-email-sent")
    public ModelAndView checkEmailNotification() {
        return new ModelAndView("/check-email");
    }

    @GetMapping("/pw-reset-process-token")
    public ModelAndView processPasswordResetToken(@RequestParam("userid") int userId, @RequestParam("token") String token) {
        var tokenInfo = dao.fetchPasswordResetTokenForUser(userId);
        var user = dao.fetchUser(userId);

        // check for bad data
        if (tokenInfo == null || user == null || user.getId() != userId || !token.equals(tokenInfo.getToken())) {
            var mv = new LibraryModelAndView("redirect:/pw-reset");
            mv.addObject("msg", "Your password reset failed (bad data).  Try again?");
            return mv;
        }

        // check for expired token
        var tokenTimeMillis = tokenInfo.getCreationTime().getTime();
        var tokenAgeMillis = System.currentTimeMillis() - tokenTimeMillis;
        var tokenLifespanMillis = TOKEN_LIFESPAN_MINUTES * 60000;
        if (tokenAgeMillis > tokenLifespanMillis) {
            var mv = new LibraryModelAndView("redirect:/pw-reset");
            mv.addObject("msg", "Your password reset failed (bad data).  Try again?");
            return mv;
        }

        // reset the user's password
        var password = pwGenerator.generate(10, true, false, true, false);
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        var encryptedPw = encoder.encode(password);
        dao.setPassword(userId, encryptedPw);

        // display the confirmation page
        var mv = new LibraryModelAndView("/password-reset-complete");
        mv.addObject("password", password);
        return mv;
    }

    protected boolean isValidFormatEmail(String email) {
        // from https://www.baeldung.com/java-email-validation-regex
        var emailRegex = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@"
                + "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
        var pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    protected void sendPasswordResetEmail(String emailAddress) throws IOException, TemplateException, MessagingException {

        // fetch the user account associated with this email
        var user = dao.fetchUser(emailAddress);
        if (user == null) {

            // if there is no user account associated with this email, just quietly do nothing
            return;
        }

        // generate a simple 6-character token
        var token = pwGenerator.generate(6, false, true, true, false);

        // save this password reset token
        dao.storePasswordResetToken(user.getId(), token);

        // calculate token issue time and expiration times
        var issueDate = new Date();
        var expirationDate = new Date(issueDate.getTime() + (TOKEN_LIFESPAN_MINUTES * 60000));

        // collect information needed to render the "reset password" email
        var model = new HashMap<String, Object>();
        model.put("user", user);
        model.put("token", token);
        model.put("tokenIssueDateTime", issueDate);
        model.put("tokenExpirationDateTime", expirationDate);
        model.put("tokenLifespan", TOKEN_LIFESPAN_MINUTES);
        model.put("applicationBaseURL", applicationBaseURL);

        // render and send the "reset password" email
        var email = emailTemplateProcessor.process(EmailTemplate.RESET_PASSWORD, model);
        email.setTo(emailAddress);
        emailSender.sendEmail(email);
    }

    @GetMapping(value = "/book", produces = "application/epub+zip")
    public void getEbook(@RequestParam(value = "id") int assetId, HttpServletResponse response) throws IOException {
        var id = dao.fetchEbookObjectKey(assetId);
        var obj = osao.readObject(id);
        writeS3ObjectToResponse(obj, response);
    }

    @GetMapping(value = "/audiobook", produces = "application/epub+zip")
    public void getAudiobook(@RequestParam(value = "id") int assetId, HttpServletResponse response) throws IOException {
        var id = dao.fetchAudiobookObjectKey(assetId);
        var obj = osao.readObject(id);
        writeS3ObjectToResponse(obj, response);
    }

    /**
     * Writes an S3 object to an HTTP response object
     *
     * @param obj      the S3 object to write
     * @param response the HTTP response object to write to
     * @throws IOException thrown if an unexpected error occurs writing to the response
     */
    protected void writeS3ObjectToResponse(S3Object obj, HttpServletResponse response) throws IOException {

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
     * determine a file's mime type based on its extension
     *
     * @param filename a filename
     * @return the mime type
     */
    public String mimeTypeForFile(String filename) {
        if (filename.toLowerCase().endsWith(".epub")) {
            return "application/epub+zip";
        } else if (filename.toLowerCase().endsWith(".m4b")) {
            return "audio/mp4a-latm";
        }

        throw new RuntimeException("can't determine mime type for file " + filename);
    }

    class LibraryModelAndView extends ModelAndView {
        public LibraryModelAndView(String view) {
            super(view);

            var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof User user) {
                addObject("user", user);
            }
        }
    }
}
