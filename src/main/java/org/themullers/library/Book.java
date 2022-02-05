package org.themullers.library;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple Java class containing information about one of the library's books.
 */
public class Book {
    protected Integer id;
    protected String title;
    protected String author;
    protected String author2;
    protected String author3;
    protected Integer publicationYear;
    protected String series;
    protected Integer seriesSequence;
    protected Date acquisitionDate;
    protected String altTitle1;
    protected String altTitle2;
    protected String epubObjectKey;
    protected String mobiObjectKey;
    protected String audiobookObjectKey;
    protected String amazonId;

    private Set<String> tags = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getEpubObjectKey() {
        return epubObjectKey;
    }

    public void setEpubObjectKey(String epubObjectKey) {
        this.epubObjectKey = epubObjectKey;
    }

    public String getMobiObjectKey() {
        return mobiObjectKey;
    }

    public void setMobiObjectKey(String mobiObjectKey) {
        this.mobiObjectKey = mobiObjectKey;
    }

    public String getAudiobookObjectKey() {
        return audiobookObjectKey;
    }

    public void setAudiobookObjectKey(String audiobookObjectKey) {
        this.audiobookObjectKey = audiobookObjectKey;
    }

    public String getAmazonId() {
        return amazonId;
    }

    public void setAmazonId(String amazonId) {
        this.amazonId = amazonId;
    }

    public void addTag(String newTag) {
        tags.add(newTag);
    }

    public void addTags(Collection<String> newTags) {
        tags.addAll(newTags);
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Book other) {
            return Utils.objectsAreEqual(this.id, other.id) &&
                    Utils.objectsAreEqual(this.title, other.title) &&
                    Utils.objectsAreEqual(this.author, other.author) &&
                    Utils.objectsAreEqual(this.author2, other.author2) &&
                    Utils.objectsAreEqual(this.publicationYear, other.publicationYear) &&
                    Utils.objectsAreEqual(this.series, other.series) &&
                    Utils.objectsAreEqual(this.seriesSequence, other.seriesSequence) &&
                    Utils.objectsAreEqual(this.acquisitionDate, other.acquisitionDate) &&
                    Utils.objectsAreEqual(this.altTitle1, other.altTitle1) &&
                    Utils.objectsAreEqual(this.epubObjectKey, other.epubObjectKey) &&
                    Utils.objectsAreEqual(this.mobiObjectKey, other.mobiObjectKey) &&
                    Utils.objectsAreEqual(this.audiobookObjectKey, other.audiobookObjectKey) &&
                    Utils.objectsAreEqual(this.amazonId, other.amazonId) &&
                    Utils.objectsAreEqual(this.tags, other.tags); // note: set equals() ignores order https://docs.oracle.com/javase/6/docs/api/java/util/Set.html#equals(java.lang.Object)
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("id: ").append(id);
        sb.append("\ntitle: ").append(title);
        sb.append("\nauthor: ").append(author);
        sb.append("\nauthor 2: ").append(author2);
        sb.append("\nauthor 3: ").append(author3);
        sb.append("\npub year: ").append(publicationYear);
        sb.append("\nseries: ").append(series);
        sb.append("\nseries #: ").append(seriesSequence);
        sb.append("\nacq date: ").append(acquisitionDate);
        sb.append("\nalt title 1: ").append(altTitle1);
        sb.append("\nalt title 2: ").append(altTitle2);
        sb.append("\nepub obj key: ").append(epubObjectKey);
        sb.append("\nmobi obj key: ").append(mobiObjectKey);
        sb.append("\naudiobook obj key: ").append(audiobookObjectKey);
        sb.append("\nasin: ").append(amazonId);
        sb.append("\ntags: ").append(String.join(", ", tags));
        return sb.toString();
    }
}
