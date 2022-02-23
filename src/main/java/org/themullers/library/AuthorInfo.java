package org.themullers.library;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Information about an author (returned by DAO)
 */
public class AuthorInfo {
    String name;
    int numTitles;
    List<String> tags;
    String byLast;

    // constructor
    public AuthorInfo(String name, int numTitles, String tags) {

        this.name = name.trim();
        this.numTitles = numTitles;

        // break the tags from CSV into list
        this.tags = new LinkedList<String>();
        if (tags != null) {
            this.tags = Arrays.stream(tags.split(",")).map(String::trim).toList();
        }

        // if the name has no space, then it's the same when sorting by last name
        var lastSpaceInName = name.lastIndexOf(" ");
        if (lastSpaceInName <= 0) {
            this.byLast = name;
        }

        // if the name has a space, parse it into last-name first so we can sort that way
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
