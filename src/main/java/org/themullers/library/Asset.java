package org.themullers.library;

import java.util.*;

/**
 * A simple Java class containing information about one of the library's assets.
 */
public class Asset {
    private int id;
    private String title;
    private String author;
    private String author2;
    private String author3;
    private Integer publicationYear;
    private String series;
    private Integer seriesSequence;
    private Date acquisitionDate;
    private String altTitle1;
    private String altTitle2;
    private String ebookS3ObjectKey;
    private String audiobookS3ObjectKey;
    private Set<String> tags = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor2() {
        return author2;
    }

    public void setAuthor2(String author2) {
        this.author2 = author2;
    }

    public String getAuthor3() {
        return author3;
    }

    public void setAuthor3(String author3) {
        this.author3 = author3;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public Integer getSeriesSequence() {
        return seriesSequence;
    }

    public void setSeriesSequence(Integer seriesSequence) {
        this.seriesSequence = seriesSequence;
    }

    public Date getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getAltTitle1() {
        return altTitle1;
    }

    public void setAltTitle1(String altTitle1) {
        this.altTitle1 = altTitle1;
    }

    public String getAltTitle2() {
        return altTitle2;
    }

    public void setAltTitle2(String altTitle2) {
        this.altTitle2 = altTitle2;
    }

    public String getEbookS3ObjectKey() {
        return ebookS3ObjectKey;
    }

    public void setEbookS3ObjectKey(String ebookS3ObjectKey) {
        this.ebookS3ObjectKey = ebookS3ObjectKey;
    }

    public String getAudiobookS3ObjectKey() {
        return audiobookS3ObjectKey;
    }

    public void setAudiobookS3ObjectKey(String audiobookS3ObjectKey) {
        this.audiobookS3ObjectKey = audiobookS3ObjectKey;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Asset other) {
            return Utils.objectsAreEqual(this.id, other.id) &&
                    Utils.objectsAreEqual(this.title, other.title) &&
                    Utils.objectsAreEqual(this.author, other.author) &&
                    Utils.objectsAreEqual(this.author2, other.author2) &&
                    Utils.objectsAreEqual(this.publicationYear, other.publicationYear) &&
                    Utils.objectsAreEqual(this.series, other.series) &&
                    Utils.objectsAreEqual(this.seriesSequence, other.seriesSequence) &&
                    Utils.objectsAreEqual(this.acquisitionDate, other.acquisitionDate) &&
                    Utils.objectsAreEqual(this.altTitle1, other.altTitle1) &&
                    Utils.objectsAreEqual(this.ebookS3ObjectKey, other.ebookS3ObjectKey) &&
                    Utils.objectsAreEqual(this.audiobookS3ObjectKey, other.audiobookS3ObjectKey) &&
                    Utils.objectsAreEqual(this.tags, other.tags); // note: set equals() ignores order https://docs.oracle.com/javase/6/docs/api/java/util/Set.html#equals(java.lang.Object)
        }
        else {
            return false;
        }
    }

}
