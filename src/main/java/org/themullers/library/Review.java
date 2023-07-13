package org.themullers.library;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Date;

public class Review {
    User user;
    String review;
    String spoilers;
    String privateNotes;
    Integer rating;
    boolean recommended;
    Date createDate;
    Date modifyDate;

    public boolean hasSpoilers() {
        return spoilers != null && spoilers.trim().length() > 0;
    }

    public boolean hasPrivateNotes() {
        return privateNotes != null && privateNotes.trim().length() > 0;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReview() {
        return review;
    }

    public String getReviewHtml() {
        return markdownToHtml(review);
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getSpoilers() {
        return spoilers;
    }

    public String getSpoilersHtml() {
        return markdownToHtml(spoilers);
    }

    public void setSpoilers(String spoilers) {
        this.spoilers = spoilers;
    }

    public String getPrivateNotes() {
        return privateNotes;
    }

    public String getPrivateNotesHtml() {
        return markdownToHtml(privateNotes);
    }

    public void setPrivateNotes(String privateNotes) {
        this.privateNotes = privateNotes;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    protected String markdownToHtml(String md) {
        var mdParser = Parser.builder().build();
        var doc = mdParser.parse(md);
        var htmlRenderer = HtmlRenderer.builder().build();
        return htmlRenderer.render(doc);
    }
}
