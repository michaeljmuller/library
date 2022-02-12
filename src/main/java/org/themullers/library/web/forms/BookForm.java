package org.themullers.library.web.forms;

import org.themullers.library.Book;
import org.themullers.library.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This class receives the data submitted by the form on the "add book" and "edit book" pages.
 */
public class BookForm extends Book {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    String coverImage;
    String newTags;
    String publicationYearString;
    String seriesSequenceString;
    String acquisitionDateString;

    /**
     * Merge any information that's not in the base class into the base class.
     *
     * @throws ParseException thrown if a date entered in the form couldn't be parsed
     */
    public void merge() throws ParseException {

        // make sure we save nulls to the database instead of empty strings
        for (var fieldName : new String[] {"title", "author", "author2", "author3", "series", "altTitle1", "altTitle2", "epubObjectKey", "mobiObjectKey", "audiobookObjectKey", "amazonId"}) {
            setToNullIfBlank(fieldName);
        }

        // parse the publication year as an integer
        if (Utils.isNotBlank(publicationYearString)) {
            publicationYear = Integer.parseInt(publicationYearString);
        }

        // parse the series # as an integer
        if (Utils.isNotBlank(seriesSequenceString)) {
            seriesSequence = Integer.parseInt(seriesSequenceString);
        }

        // parse the acquisition date
        if (Utils.isNotBlank(acquisitionDateString)) {
            acquisitionDate = DATE_FORMAT.parse(acquisitionDateString);
        }

        // add any new tags to the list of tags
        if (Utils.isNotBlank(newTags)) {
            addTags(Arrays.stream(newTags.split(",")).map(String::trim).collect(Collectors.toList()));
        }
    }

    /**
     * Helper method to set a field of this object to null if it currently contains an empty (or blank) string.
     * Implemented using introspection.
     *
     * @param fieldName  the field to null out
     */
    protected void setToNullIfBlank(String fieldName) {
        try {
            // get the field from this object
            var field = Book.class.getDeclaredField(fieldName);

            // get the field's current value
            var value = field.get(this);

            // if the field is blank, set its value to null
            if (value instanceof String valueString && valueString.trim().length() == 0) {
                field.set(this, null);
            }
        }
        catch (NoSuchFieldException|IllegalAccessException x) {
            System.out.println(x.toString());
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

    // ACCESSOR METHODS

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
}
