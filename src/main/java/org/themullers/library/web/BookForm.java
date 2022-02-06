package org.themullers.library.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.themullers.library.Book;
import org.themullers.library.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class BookForm extends Book {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

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
        for (var fieldName : new String[] {"title", "author", "author2", "author3", "series", "altTitle1", "altTitle2", "epubObjectKey", "mobiObjectKey", "audiobookObjectKey", "amazonId"}) {
            setToNullIfBlank(fieldName);
        }
        if (Utils.isNotBlank(publicationYearString)) {
            publicationYear = Integer.parseInt(publicationYearString);
        }
        if (Utils.isNotBlank(seriesSequenceString)) {
            seriesSequence = Integer.parseInt(seriesSequenceString);
        }
        if (Utils.isNotBlank(acquisitionDateString)) {
            acquisitionDate = DATE_FORMAT.parse(acquisitionDateString);
        }
        if (Utils.isNotBlank(newTags)) {
            var tags = Arrays.stream(newTags.split(",")).map(String::trim).collect(Collectors.toList());
        }
    }

    protected void setToNullIfBlank(String fieldName) {
        try {
            var field = Book.class.getDeclaredField(fieldName);
            var value = field.get(this);
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
}
