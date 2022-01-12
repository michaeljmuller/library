package org.themullers.library.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.themullers.library.Asset;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A library of database access methods.
 */
@Component
public class LibraryDAO {

    JdbcTemplate jt;

    @Autowired
    public LibraryDAO(JdbcTemplate jt) {
        this.jt = jt;
    }

    /**
     * an enumeration of all the columns in the assests table
     */
    enum ASSET_COLS {
        id,
        title,
        author,
        author2,
        author3,
        pub_year,
        series,
        series_sequence,
        acq_date,
        alt_title1,
        alt_title2,
        ebook_s3_object_key,
        audiobook_s3_object_key;

        /**
         * Returns the column names as a comma-sepated string.
         * @return the column names
         */
        public static String commaSeparated() {
            return Arrays.stream(values()).map(ASSET_COLS::toString).collect(Collectors.joining(", "));
        }

        /**
         * Returns the column names as a comma-separated string, using a provided prefix.
         * Using the prefix "a", for example, returns a string like a.column_one, a.column_two, etc.
         * @param tableId  The prefix to prepend before each column name.
         * @return the column names
         */
        public static String commaSeparated(String tableId) {
            return Arrays.stream(values()).map(c -> c.toStringWithTableId(tableId)).collect(Collectors.joining(", "));
        }

        /**
         * Converts the column enum to a string, pre-pending an identifier for a table reverenced in a query.
         * @param tableId  The prefix to prepend before the column name
         * @return  the column name
         */
        public String toStringWithTableId(String tableId) {
            return tableId + "." + toString();
        }

        /**
         * Returns a comma-separated string of question marks for use in SQL queries.
         * The number of question marks will match the number of columns.
         * @return a comma-separated string of question marks
         */
        public static String questionMarks() {
            return String.join(", ", Collections.nCopies(ASSET_COLS.values().length, "?"));
        }
    };

    /**
     * Returns all the assets in the database.
     * @return  list of assets
     */
    public List<Asset> fetchAllAssets() {
        String sql = String.format("select %s, group_concat(t.tag separator ',') as tags from assets a inner join tags t on a.id = t.asset_id group by a.id", ASSET_COLS.commaSeparated("a"));
        return jt.query(sql, LibraryDAO::mapAsset);
    }

    /**
     * Returns all the assets written by a given author.
     * @param author  The name of an author whose assets should be returned.
     * @return  list of assets
     */
    public List<Asset> fetchAssetsForAuthor(String author) {
        String sql = String.format("select %s , group_concat(t.tag separator ',') as tags from assets a inner join tags t on a.id = t.asset_id where a.author = ? group by a.id", ASSET_COLS.commaSeparated("a"));
        System.out.println(sql);
        return jt.query(sql, LibraryDAO::mapAsset, author);
    }

    /**
     * Returns the most recently acquired assets.  The result set is limited to a certain number of assets
     * and the first few assets are skipped.
     * @param limit  The number of assets to return.
     * @param skip  The number of assets to skip.
     * @return  a list of assets
     */
    public List<Asset> fetchNewestAssets(int limit, int skip) {
        var sql = String.format("select %s, group_concat(t.tag separator ',') as tags from assets a inner join tags t on a.id = t.asset_id group by a.id order by acq_date desc limit %s offset %s", ASSET_COLS.commaSeparated("a"), limit, skip);
        return jt.query(sql, LibraryDAO::mapAsset);
    }

    /**
     * Populate a new asset object with information from the results of a database query.
     * This method implements a functional interface, making it easy to use with jdbctemplate.
     * @param rs  the query's result set
     * @param rowNum  number of the row being processed
     * @return  a new asset object containing information from the result set
     * @throws SQLException  thrown when an unexpected error occurs processing the result set
     */
    protected static Asset mapAsset(ResultSet rs, int rowNum) throws SQLException {

        var asset = new Asset();

        // read columns from the result set, write to properties of the object
        asset.setId(rs.getInt(ASSET_COLS.id.toStringWithTableId("a")));
        asset.setTitle(rs.getString(ASSET_COLS.title.toStringWithTableId("a")));
        asset.setAuthor(rs.getString(ASSET_COLS.author.toStringWithTableId("a")));
        asset.setAuthor2(rs.getString(ASSET_COLS.author2.toStringWithTableId("a")));
        asset.setAuthor3(rs.getString(ASSET_COLS.author3.toStringWithTableId("a")));
        asset.setPublicationYear(rs.getInt(ASSET_COLS.pub_year.toStringWithTableId("a")));
        asset.setSeries(rs.getString(ASSET_COLS.series.toStringWithTableId("a")));
        asset.setSeriesSequence(rs.getInt(ASSET_COLS.series_sequence.toStringWithTableId("a")));
        asset.setAcquisitionDate(rs.getDate(ASSET_COLS.acq_date.toStringWithTableId("a")));
        asset.setAltTitle1(rs.getString(ASSET_COLS.alt_title1.toStringWithTableId("a")));
        asset.setAltTitle2(rs.getString(ASSET_COLS.alt_title2.toStringWithTableId("a")));
        asset.setEbookS3ObjectKey(rs.getString(ASSET_COLS.ebook_s3_object_key.toStringWithTableId("a")));
        asset.setAudiobookS3ObjectKey(rs.getString(ASSET_COLS.audiobook_s3_object_key.toStringWithTableId("a")));

        // handle tags differently because we joined them into the result set as CSV
        for (var tag : rs.getString("tags").split(",")) {
            asset.addTag(tag);
        }

        return asset;
    }

    /**
     * Returns the cover image for an ebook.
     * @param ebookS3ObjectKey  the s3 object id of the ebook whose image should be returned
     * @return  the cover image (or null if no cover image exists for this book)
     */
    public byte[] fetchImageForEbook(String ebookS3ObjectKey) {
        try {
            String sql = "select bits from cover_images where ebook_s3_object_key = ?";
            Blob bits = jt.queryForObject(sql, Blob.class, ebookS3ObjectKey);
            return bits.getBytes(1, (int) bits.length());
        }
        catch (EmptyResultDataAccessException x) {
            return null;
        }
        catch (SQLException x) {
            throw new DataRetrievalFailureException("unable to get image binary", x);
        }
    }

    /**
     * Returns a list of all the authors with assets in the library.
     * @return  a list of authors
     */
    public List<String> fetchAllAuthors() {
        String sql = "select distinct author from assets order by author";
        return jt.queryForList(sql, String.class);
    }

    /**
     * Indicates whether a cover image exists for a given ebook.
     * @param s3key  The s3 object key for an ebook
     * @return  true if the cover image is found, false otherwise
     */
    public boolean coverImageExists(String s3key) {
        var count = jt.queryForObject("select count(*) from cover_images where ebook_s3_object_key = ?", Integer.class, s3key);
        return count > 0;
    }

    /**
     * Gets all the tags in the library and a count of the number of assets associated with each tag.
     * @return  the tags and tag counts
     */
    public Map<String, Integer> getTags() {

        // create a map of tags to populate
        var map = new LinkedHashMap<String, Integer>();

        // get the number of assets associated with each tag
        jt.query("select tag, count(*) as num_assets from tags group by tag order by tag", row -> {
            map.put(row.getString(1), row.getInt(2));
        });

        return map;
    }

    /**
     * Inserts an asset into the database.
     * @param asset  the asset to insert
     */
    @Transactional
    public void insertAsset(Asset asset) {
        String sql = String.format("insert into assets (%s) values (%s)", ASSET_COLS.commaSeparated(), ASSET_COLS.questionMarks());
        jt.update(sql, null, asset.getTitle(), asset.getAuthor(), asset.getAuthor2(), asset.getAuthor3(), asset.getPublicationYear(), asset.getSeries(), asset.getSeriesSequence(), asset.getAcquisitionDate(), asset.getAltTitle1(), asset.getAltTitle2(), asset.getEbookS3ObjectKey(), asset.getAudiobookS3ObjectKey());
        // TODO: tags
    }

    /**
     * Inserts a cover image into the database.
     * @param s3key  the s3 object key for the book whose cover should be inserted
     * @param filename  the cover image's filename
     * @param mimeType  the mime type of the cover image
     * @param bytes  the cover image
     */
    @Transactional
    public void insertCoverImage(String s3key, String filename, String mimeType, byte[] bytes) {
        jt.update("insert into cover_images (ebook_s3_object_key, filename, mime_type, bits) values (?, ?, ? , ?)", s3key, filename, mimeType, bytes);
    }

    /**
     * Sets the tags associated with an asset.
     * @param assetId  the asset whose tags are to be defined
     * @param tags  the tags to associate with the asset
     */
    @Transactional
    public void setTags(int assetId, Collection<String> tags) {
        jt.update("delete from tags where asset_id = ?", assetId);
        for (var tag : tags) {
            jt.update("insert into tags(asset_id, tag) values (?, ?)", assetId, tag);
        }
    }
}
