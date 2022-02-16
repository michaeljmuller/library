package org.themullers.library;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AuthorInfo {
    String name;
    int numTitles;
    List<String> tags;
    String byLast;

    public AuthorInfo(String name, int numTitles, String tags) {
        this.name = name.trim();
        this.numTitles = numTitles;
        this.tags = new LinkedList<String>();
        if (tags != null) {
            this.tags = Arrays.stream(tags.split(",")).map(String::trim).toList();
        }
        var lastSpaceInName = name.lastIndexOf(" ");
        if (lastSpaceInName <= 0) {
            this.byLast = name;
        }
        else {
            var lastName = name.substring(lastSpaceInName+1);
            var firstPartOfName = name.substring(0,lastSpaceInName);
            this.byLast = lastName + ", " + firstPartOfName;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumTitles() {
        return numTitles;
    }

    public void setNumTitles(int numTitles) {
        this.numTitles = numTitles;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getByLast() {
        return byLast;
    }

    public void setByLast(String byLast) {
        this.byLast = byLast;
    }
}
