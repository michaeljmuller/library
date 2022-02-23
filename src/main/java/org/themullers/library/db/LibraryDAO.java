package org.themullers.library.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.themullers.library.AuthorInfo;
import org.themullers.library.Book;
import org.themullers.library.auth.pwreset.PasswordResetToken;
import org.themullers.library.User;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Database access methods.
 */
@Component
public class LibraryDAO {

    JdbcTemplate jt;

    @Autowired
    public LibraryDAO(JdbcTemplate jt) {
        this.jt = jt;
    }

    /**
     * An enumeration of all the columns in the user table.
     */
    enum USER_COLS {
        id,
        email,
        password,
        first_name,
        last_name,
    }

    /**
     * An enumeration of all the columns in the books table.
     */
    enum BOOK_COLS {
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
        epub_object_key,
        mobi_object_key,
        audiobook_object_key,
        asin,
    }

    public final static String BOOK_ORDER_TITLE = "title";
    public final static String BOOK_ORDER_AUTHOR = "author";
    public final static String BOOK_ORDER_PUB_YEAR_ASC = "pubYearAsc";
    public final static String BOOK_ORDER_ACQ_DATE_ASC = "acqDateAsc";
    public final static String BOOK_ORDER_PUB_YEAR_DESC = "pubYearDesc";
    public final static String BOOK_ORDER_ACQ_DATE_DESC = "acqDateDesc";

    // QUERY METHODS

    /**
     * Returns a list of EPUB object keys that are used by more than one book (this should not happen).
     * @return  a list of EPUB object keys
     */
    public List<String> epubsInMultipleBooks() {
        var sql = """
                select distinct epub_object_key from books
                where epub_object_key is not null
                group by epub_object_key
                having count(*) > 1
                """;
        return jt.queryForList(sql, String.class);
    }

    /**
     * Returns a list of MOBI object keys that are used by more than one book (this should not happen).
     * @return  a list of MOBI object keys
     */
    public List<String> mobisInMultipleBooks() {
        var sql = """
                select distinct mobi_object_key from books
                where mobi_object_key is not null
                group by mobi_object_key
                having count(*) > 1
                """;
        return jt.queryForList(sql, String.class);
    }

    /**
     * Returns a list of audiobook object keys that are used by more than one book (this should not happen).
     * @return  a list of audiobook object keys
     */
    public List<String> audiobooksInMultipleBooks() {
        var sql = """
                select distinct audiobook_object_key from books
                where audiobook_object_key is not null
                group by audiobook_object_key
                having count(*) > 1
                """;
        return jt.queryForList(sql, String.class);
    }

    /**
     * Look for books with the same EPUB object key as the given book.
     * @param book  a book object
     * @return  the number of books with the same EPUB object key as the given book.
     */
    public int numBooksWithSameEpubAsset(Book book) {
        return jt.queryForObject("select count(*) from books where epub_object_key = ? and id <> ?", Integer.class, book.getEpubObjectKey(), book.getId());
    }

    /**
     * Look for books with the same MOBI object key as the given book.
     * @param book  a book object
     * @return  the number of books with the same MOBI object key as the given book.
     */
    public int numBooksWithSameMobiAsset(Book book) {
        return jt.queryForObject("select count(*) from books where mobi_object_key = ? and id <> ?", Integer.class, book.getMobiObjectKey(), book.getId());
    }

    /**
     * Look for books with the same audiobook object key as the given book.
     * @param book  a book object
     * @return  the number of books with the same audiobook object key as the given book.
     */
    public int numBooksWithSameAudiobookAsset(Book book) {
        return jt.queryForObject("select count(*) from books where audiobook_object_key = ? and id <> ?", Integer.class, book.getAudiobookObjectKey(), book.getId());
    }

    /**
     * Fetch the books that have a certain tag.
     * @param tag  the tag filtering which books will be returned
     * @param limit  don't return more books than this threshold
     * @param offset  skip this number of books from the beginning of the results
     * @param order  one of the BOOK_ORDER_* values or null
     * @return  a list of books
     */
    public List<Book> fetchBooksWithTag(String tag, int limit, int offset, String order) {

        String sql = """
                select
                    %s, group_concat(distinct t.tag separator ',') as tags
                from (
                    select x.* from books x inner join tags y on x.id = y.book_id where y.tag = ?
                ) as a
                left outer join tags t on a.id = t.book_id
                group by a.id
                order by %s
                limit %s offset %s
                """;

        sql = String.format(sql, commaSeparated(BOOK_COLS.class, "a"), orderBySql(order), limit, offset);

        return jt.query(sql, LibraryDAO::mapBook, tag);
    }

    /**
     * Fetch all the audiobooks.
     * @param limit  don't return more books than this threshold
     * @param offset  skip this number of books from the beginning of the results
     * @param order  one of the BOOK_ORDER_* values or null
     * @return  a list of books
     */
    public List<Book> fetchAudiobooks(int limit, int offset, String order) {

        String sql = """
                select
                    %s, group_concat(distinct t.tag separator ',') as tags
                from books a
                left outer join tags t on a.id = t.book_id
                where a.audiobook_object_key is not null
                group by a.id
                order by %s
                limit %s offset %s
                """;

        sql = String.format(sql, commaSeparated(BOOK_COLS.class, "a"), orderBySql(order), limit, offset);

        return jt.query(sql, LibraryDAO::mapBook);
    }

    /**
     * Generate SQL to order a book query based on the BOOK_ORDER_* constant specified.
     * @param orderByConst  the desired order
     * @return  a SQL order by clause
     */
    protected String orderBySql(String orderByConst) {
        return switch (orderByConst) {
            case BOOK_ORDER_TITLE -> "a.title";
            case BOOK_ORDER_AUTHOR -> "a.author, a.series, a.series_sequence, a.pub_year";
            case BOOK_ORDER_PUB_YEAR_DESC -> "a.pub_year desc, a.title";
            case BOOK_ORDER_ACQ_DATE_DESC -> "a.acq_date desc, a.title";
            case BOOK_ORDER_PUB_YEAR_ASC -> "a.pub_year asc, a.title";
            case BOOK_ORDER_ACQ_DATE_ASC -> "a.acq_date asc, a.title";
            default -> "a.acq_date desc, a.title";
        };
    }

    /**
     * Get information about each author in the library: name, title count, and tags associated with that author's work.
     *
     * @return  a list of AuthorInfo objects
     */
    public List<AuthorInfo> getAuthorInfo() {
        String sql = """
                select t.author, c.num_books, t.tags from (
                  select b.author as author, group_concat(distinct t.tag separator ',') as tags
                      from books b left outer join tags t
                      on t.book_id = b.id group by b.author
                ) as t
                left outer join (
                  select author as author, count(*) as num_books from books a group by author
                ) as c on c.author = t.author
                order by t.author
                """;

        return jt.query(sql, new RowMapper<AuthorInfo>() {
            @Override
            public AuthorInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new AuthorInfo(rs.getString("author"), rs.getInt("num_books"), rs.getString("tags"));
            }
        });
    }

    /**
     * Count the number of titles in the library.
     * @return  the number of titles in the library
     */
    public int countTitles() {
        return jt.queryForObject("select count(*) from books", Integer.class);
    }

    /**
     * Count the number of authors in the library.
     * @return  the number of authors in the library
     */
    public int countAuthors() {
        return jt.queryForObject("select count(distinct author) from books", Integer.class);
    }

    /**
     * Count the number of audiobooks in the library.
     * @return  the number of audiobooks in the library
     */
    public int countAudiobooks() {
        return jt.queryForObject("select count(*) from books where books.audiobook_object_key is not null", Integer.class);
    }

    /**
     * Find books where the title contains certain text.
     * @param searchText  the text to search for
     * @return  a list of books
     */
    public List<Book> searchTitles(String searchText) {
        String sql = String.format("select %s, group_concat(t.tag separator ',') as tags from books a left outer join tags t on a.id = t.book_id where a.title like ? group by a.id", commaSeparated(BOOK_COLS.class, "a"));
        return jt.query(sql, LibraryDAO::mapBook, "%" + searchText + "%");
    }

    /**
     * Find authors matching certain text.
     * @param searchText  the text to search for
     * @return  a list of authors
     */
    public List<String> searchAuthors(String searchText) {
        var wildcard = "%" + searchText + "%";
        var authors = jt.queryForList("select distinct author from books where author like ?", String.class, wildcard);
        Collections.sort(authors);
        return authors;
    }

    /**
     * Find series matching certain text.
     * @param searchText  the text to search for
     * @return  a map of series referencing a list of authors that wrote books in the series
     */
    public Map<String, List<String>> searchSeries(String searchText) {
        return jt.query("select distinct series, author from books where series like ?", rs -> {

            // create a map of series -> list of authors
            var map = new HashMap<String, List<String>>();

            // for each result
            var hasMore = rs.first();
            while (hasMore) {

                // get the series and author
                var series = rs.getString("series");
                var author = rs.getString("author");

                // make a list for the authors of this series (if it's not already there)
                map.putIfAbsent(series, new LinkedList<String>());

                // add the author to the list for this series
                map.get(series).add(author);

                // move to the next result
                hasMore = rs.next();
            }

            return map;
        }, "%" + searchText + "%");
    }

    /**
     * Returns a boolean indicating whether a book has a cover image uploaded.
     * @param bookId  The book's id
     * @return  boolean indicating whether we have a cover for that book in the DB
     */
    public boolean hasCoverImage(int bookId) {
        return jt.queryForObject("select count(*) from cover_images where book_id = ?", Integer.class, bookId) > 0;
    }

    /**
     * Looks up a book with an EPUB having a certain object key.
     * @param epubObjectKey  the object key to search for
     * @return  the matching book's id
     */
    public Integer fetchBookIdForEpub(String epubObjectKey) {
        return jt.query("select id from books where epub_object_key = ?", rs -> {
            return rs.first() ? rs.getObject("id", Integer.class) : null;
        }, epubObjectKey);
    }

    /**
     * Returns the object key for the EPUB asset with the given book ID.
     * @param bookId  the database ID of the ebook
     * @return  the ebook's S3 object key
     */
    public String fetchEpubObjectKey(int bookId) {
        String sql = "select epub_object_key from books where id = ?";
        return jt.queryForObject(sql, String.class, bookId);
    }

    /**
     * Returns the object key for the MOBI asset with the given book ID.
     * @param bookId  the database ID of the ebook
     * @return  the ebook's object key
     */
    public String fetchMobiObjectKey(int bookId) {
        String sql = "select mobi_object_key from books where id = ?";
        return jt.queryForObject(sql, String.class, bookId);
    }

    /**
     * Returns the object key for the audiobook asset with the given book ID.
     * @param bookId  the database ID of the audiobook
     * @return  the audiobook's object key
     */
    public String fetchAudiobookObjectKey(int bookId) {
        String sql = "select audiobook_object_key from books where id = ?";
        return jt.queryForObject(sql, String.class, bookId);
    }

    /**
     * Returns information about the user with a given email address.
     * @param userId  The ID of the user to find.
     * @return  Information about the specified user.
     */
    public User fetchUser(int userId) {
        String sql = String.format("select %s from users where id = ?", commaSeparated(USER_COLS.class));
        var users = jt.query(sql, LibraryDAO::mapUser, userId);
        return users == null || users.size() == 0 ? null : users.get(0);
    }

    /**
     * Returns information about the user with a given email address.
     * @param email  The email address associated with the user to find.
     * @return  Information about the specified user.
     */
    public User fetchUser(String email) {
        String sql = String.format("select %s from users where email = ? limit 1", commaSeparated(USER_COLS.class));
        var users = jt.query(sql, LibraryDAO::mapUser, email);
        return users == null || users.size() == 0 ? null : users.get(0);
    }

    /**
     * Returns all the books in the database.
     * @return  list of books
     */
    public List<Book> fetchAllBooks() {
        String sql = String.format("select %s, group_concat(t.tag separator ',') as tags from books a left outer join tags t on a.id = t.book_id group by a.id", commaSeparated(BOOK_COLS.class, "a"));
        return jt.query(sql, LibraryDAO::mapBook);
    }

    /**
     * Returns the book matching a specified id
     * @param bookId  the id to use to find the book
     * @return  the matching book
     */
    public Book fetchBook(int bookId) {
        var sql = String.format("select %s, group_concat(t.tag separator ',') as tags from books a left outer join tags t on a.id = t.book_id where a.id = ? limit 1", commaSeparated(BOOK_COLS.class, "a"));
        var books = jt.query(sql, LibraryDAO::mapBook, bookId);
        return books == null || books.size() < 1 ? null : books.get(0);
    }

    /**
     * returns the book matching by title and author #1
     * @param title  the title of the book to find
     * @param author  the first author of the book to find
     * @return  the matching book
     */
    public Book fetchBook(String title, String author) {
        var sql = String.format("select %s, group_concat(t.tag separator ',') as tags from books a left outer join tags t on a.id = t.book_id where a.title=? and a.author=? limit 1", commaSeparated(BOOK_COLS.class, "a"));
        var books = jt.query(sql, LibraryDAO::mapBook, title, author);
        return books == null || books.size() < 1 ? null : books.get(0);
    }

    /**
     * Returns all the books written by a given author.
     * @param author  The name of an author whose books should be returned.
     * @return  list of books
     */
    public List<Book> fetchBooksForAuthor(String author) {
        String sql = String.format("select %s , group_concat(t.tag separator ',') as tags from books a left outer join tags t on a.id = t.book_id where a.author = ? group by a.id", commaSeparated(BOOK_COLS.class, "a"));
        return jt.query(sql, LibraryDAO::mapBook, author);
    }

    /**
     * Returns the most recently acquired books.  The result set is limited to a certain number of books
     * and the first few books are skipped.
     * @param limit  The number of books to return.
     * @param skip  The number of books to skip.
     * @return  a list of books
     */
    public List<Book> fetchNewestBooks(int limit, int skip) {
        var sql = String.format("select %s, group_concat(t.tag separator ',') as tags from books a left outer join tags t on a.id = t.book_id group by a.id order by acq_date desc limit %s offset %s", commaSeparated(BOOK_COLS.class, "a"), limit, skip);
        return jt.query(sql, LibraryDAO::mapBook);
    }

    /**
     * Creates a user object from a row of query result.
     * @param rs  the result set returned from the query
     * @param rowNum  the current row of the result set
     * @return  a user object
     * @throws SQLException  thrown if an unexpected error occurs fetching information from the result set
     */
    protected static User mapUser(ResultSet rs, int rowNum) throws SQLException {
        var user = new User();

        // read columns from the result set, write to properties of the object
        user.setId(rs.getInt(USER_COLS.id.toString()));
        user.setEmail(rs.getString(USER_COLS.email.toString()));
        user.setPassword(rs.getString(USER_COLS.password.toString()));
        user.setFirstName(rs.getString(USER_COLS.first_name.toString()));
        user.setLastName(rs.getString(USER_COLS.last_name.toString()));

        return user;
    }

    /**
     * Populate a new book object with information from the results of a database query.
     * This method implements a functional interface, making it easy to use with jdbctemplate.
     * @param rs  the query's result set
     * @param rowNum  number of the row being processed
     * @return  a new book object containing information from the result set
     * @throws SQLException  thrown when an unexpected error occurs processing the result set
     */
    protected static Book mapBook(ResultSet rs, int rowNum) throws SQLException {

        var book = new Book();

        // read columns from the result set, write to properties of the object
        book.setId(getIntOrNull(rs, withTableId(BOOK_COLS.id, "a")));
        book.setTitle(rs.getString(withTableId(BOOK_COLS.title, "a")));
        book.setAuthor(rs.getString(withTableId(BOOK_COLS.author, "a")));
        book.setAuthor2(rs.getString(withTableId(BOOK_COLS.author2, "a")));
        book.setAuthor3(rs.getString(withTableId(BOOK_COLS.author3, "a")));
        book.setPublicationYear(rs.getInt(withTableId(BOOK_COLS.pub_year, "a")));
        book.setSeries(rs.getString(withTableId(BOOK_COLS.series, "a")));
        book.setSeriesSequence(getIntOrNull(rs, withTableId(BOOK_COLS.series_sequence, "a")));
        book.setAcquisitionDate(rs.getDate(withTableId(BOOK_COLS.acq_date, "a")));
        book.setAltTitle1(rs.getString(withTableId(BOOK_COLS.alt_title1, "a")));
        book.setAltTitle2(rs.getString(withTableId(BOOK_COLS.alt_title2, "a")));
        book.setEpubObjectKey(rs.getString(withTableId(BOOK_COLS.epub_object_key, "a")));
        book.setMobiObjectKey(rs.getString(withTableId(BOOK_COLS.mobi_object_key, "a")));
        book.setAudiobookObjectKey(rs.getString(withTableId(BOOK_COLS.audiobook_object_key, "a")));
        book.setAmazonId(rs.getString(withTableId(BOOK_COLS.asin, "a")));

        // if there are any tags associated with this book
        // (handle tags differently because we joined them into the result set as CSV)
        var tags = rs.getString("tags");
        if (tags != null && tags.trim().length() > 0) {

            // parse the comma-separated list of tags
            for (var tag : tags.split(",")) {
                book.addTag(tag);
            }
        }

        return book;
    }

    /**
     * Gets an integer value from a result set.
     * @param rs  the result set from which to extract the integer value
     * @param col  which column of the result set to extract the value from
     * @return  an integer value or null if there was no value
     * @throws SQLException  thrown if we are unable to extract the value from the result set
     */
    protected static Integer getIntOrNull(ResultSet rs, String col) throws SQLException {
        var value = rs.getInt(col);
        return rs.wasNull() ? null : value;
    }

    /**
     * Returns the cover image for an ebook.
     * @param bookId  the id of the ebook whose image should be returned
     * @return  the cover image (or null if no cover image exists for this book)
     */
    public byte[] fetchImageForEbook(int bookId) {
        try {
            String sql = "select bits from cover_images where book_id = ?";
            Blob blob = jt.queryForObject(sql, Blob.class, bookId);
            return blob == null ? null : blob.getBytes(1, (int) blob.length());
        }
        catch (EmptyResultDataAccessException x) {
            return null;
        }
        catch (SQLException x) {
            throw new DataRetrievalFailureException("unable to get image binary", x);
        }
    }

    /**
     * Returns a list of all the authors with books in the library.
     * @return  a list of authors
     */
    public List<String> fetchAllAuthors() {
        return jt.queryForList("select distinct author from books where author is not null order by author", String.class);
    }

    /**
     * Returns a list of all the series with books in the library.
     * @return  a list of series
     */
    public List<String> fetchAllSeries() {
        return jt.queryForList("select distinct series from books where series is not null order by series", String.class);
    }

    /**
     * Returns a list of all the tags used in the library.
     * @return a list of tags
     */
    public List<String> fetchAllTags() {
        return jt.queryForList("select distinct tag from tags order by tag", String.class);
    }

    /**
     * Returns a list of all the EPUBs in the library.
     * @return  a list of EPUB object keys
     */
    public List<String> fetchAllEpubObjectKeys() {
        return jt.queryForList("select distinct epub_object_key from books where epub_object_key is not null order by epub_object_key", String.class);
    }

    /**
     * Returns a list of all the MOBIs in the library.
     * @return  a list of MOBI object keys
     */
    public List<String> fetchAllMobiObjectKeys() {
        return jt.queryForList("select distinct mobi_object_key from books where mobi_object_key is not null order by mobi_object_key", String.class);
    }

    /**
     * Returns a list of all the audiobooks in the library.
     * @return  a list of audiobook object keys
     */
    public List<String> fetchAllAudiobookObjectKeys() {
        return jt.queryForList("select distinct audiobook_object_key from books where audiobook_object_key is not null order by audiobook_object_key", String.class);
    }

    /**
     * Gets all the tags in the library and a count of the number of books associated with each tag.
     * @return  the tags and tag counts
     */
    public Map<String, Integer> getTags() {

        // create a map of tags to populate
        var map = new LinkedHashMap<String, Integer>();

        // get the number of books associated with each tag
        jt.query("select tag, count(*) as num_books from tags group by tag order by tag", row -> {
            map.put(row.getString(1), row.getInt(2));
        });

        return map;
    }

    /**
     * Returns the password reset token for the given user.
     * @param userId  the id of the user whose token we're looking for
     * @return  the user's password reset token
     */
    public PasswordResetToken fetchPasswordResetTokenForUser(int userId) {

        // search for password reset tokens for this user
        var tokens = jt.query("select token, creation_time from password_reset_tokens where user_id = ? order by creation_time desc limit 1", (rs, rowNum) -> {

            // build a password reset token from the result set
            var token = new PasswordResetToken();
            token.setUserId(userId);
            token.setToken(rs.getString("token"));
            token.setCreationTime(rs.getTimestamp("creation_time"));
            return token;

        }, userId);

        // return the token
        return tokens == null || tokens.size() <= 0 ? null : tokens.get(0);
    }

    // INSERT/UPDATE/DELETE METHODS

    /**
     * Delete a book from the library's database, along with any associated tags.
     * @param bookId  the primary key id of the book to delete
     */
    public void deleteBook(int bookId) {
        // TODO: update schema to use ON DELETE CASCADE
        jt.update("delete from cover_images where book_id = ?", bookId);
        jt.update("delete from tags where book_id = ?", bookId);
        jt.update("delete from books where id = ?", bookId);
    }

    /**
     * Inserts a book into the database.
     * @return  the id (primary key) of the new book row
     * @param book  the book to insert
     */
    public int insertBook(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        String sql = String.format("insert into books (%s) values (%s)", commaSeparated(BOOK_COLS.class), questionMarks(BOOK_COLS.class));
        jt.update(c -> {
            var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, book.getId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getAuthor2());
            ps.setString(5, book.getAuthor3());
            ps.setInt(6, book.getPublicationYear());
            ps.setString(7, book.getSeries());
            ps.setObject(8, book.getSeriesSequence());
            ps.setDate(9, toSqlDate(book.getAcquisitionDate()));
            ps.setString(10, book.getAltTitle1());
            ps.setString(11, book.getAltTitle2());
            ps.setString(12, book.getEpubObjectKey());
            ps.setString(13, book.getMobiObjectKey());
            ps.setString(14, book.getAudiobookObjectKey());
            ps.setString(15, book.getAmazonId());
            return ps;
        }, keyHolder);

        // get the id of the newly-created book row
        var bookId = keyHolder.getKey().intValue();

        // insert the book's tags
        for (var tag : book.getTags()) {
            jt.update("insert into tags(book_id, tag) values (?, ?)", bookId, tag);
        }

        return bookId;
    }

    /**
     * Convert a java util date to a sql date without throwing a null pointer exception.
     * @param date  a java util date object
     * @return  a sql date
     */
    protected java.sql.Date toSqlDate(Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }

    /**
     * Inserts a cover image into the database, deleting any previous cover images.
     * @param bookId  the id (foreign key) of the book whose cover should be inserted
     * @param filename  the cover image's filename
     * @param mimeType  the mime type of the cover image
     * @param bytes  the cover image
     */
    public void insertCoverImage(int bookId, String filename, String mimeType, byte[] bytes) {
        jt.update("delete from cover_images where book_id = ?", bookId);
        jt.update("insert into cover_images (book_id, filename, mime_type, bits) values (?, ?, ? , ?)", bookId, filename, mimeType, bytes);
    }

    /**
     * Sets the tags associated with an book.
     * @param bookId  the book whose tags are to be defined
     * @param tags  the tags to associate with the book
     */
    public void setTags(int bookId, Collection<String> tags) {
        jt.update("delete from tags where book_id = ?", bookId);
        for (var tag : tags) {
            jt.update("insert into tags(book_id, tag) values (?, ?)", bookId, tag);
        }
    }

    /**
     * Update information about a book.
     * @param book  an object with information about the book to be updated
     */
    public void updateBook(Book book) {
        var sql = String.format("update books set %s where id = ?", updateSql(BOOK_COLS.class));
        jt.update(sql, book.getId(), book.getTitle(), book.getAuthor(), book.getAuthor2(), book.getAuthor3(),
                book.getPublicationYear(), book.getSeries(), book.getSeriesSequence(), book.getAcquisitionDate(),
                book.getAltTitle1(), book.getAltTitle2(), book.getEpubObjectKey(), book.getMobiObjectKey(),
                book.getAudiobookObjectKey(), book.getAmazonId(), book.getId());
    }

    /**
     * Insert a row into the password reset token table.  Before doing so,
     * first delete any other tokens for this user.
     * @param userId  the id of the user for whom we're storing this token
     * @param token  the token value to save
     */
    public void storePasswordResetToken(int userId, String token) {
        jt.update("delete from password_reset_tokens where user_id = ?", userId);
        jt.update("insert into password_reset_tokens (user_id, token, creation_time) values (?, ?, ?)", userId, token, new Date());
    }

    /**
     * Delete any password reset tokens for a given user.
     * @param userId  the id of a user
     */
    public void deletePasswordResetToken(int userId) {
        jt.update("delete from password_reset_tokens where user_id = ?", userId);
    }

    /**
     * Set a user's password.
     *
     * @param userId  the id of a user
     * @param password  the user's new password
     */
    @Transactional
    public void setPassword(int userId, String password) {
        jt.update("update users set password = ? where id = ?", password, userId);
    }

    // HELPER METHODS BELOW HERE

    /**
     * Returns the enumeration names as a comma-separated string.
     * @param enumClass  The enumeration to convert
     * @return  a comma-separated string of enumeration values
     */
    protected static String commaSeparated(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString).collect(Collectors.joining(","));
    }

    /**
     * Returns a comma-separated string of question marks for use in SQL queries.
     * The number of question marks will match the number of values in the enumeration.
     * @param enumClass  The enumeration to evaluate.
     * @return a comma-separated string of question marks
     */
    protected static String questionMarks(Class<? extends Enum<?>> enumClass) {
        return String.join(",", Collections.nCopies(enumClass.getEnumConstants().length, "?"));
    }

    /**
     * Returns the enumeration names as a comma-separated string, using a provided prefix
     * delimited with a period. Using the prefix "a", for example, returns a string like
     * a.enum_one, a.enum_two, etc.
     * @param enumClass  The enumeration to evaluate
     * @param tableId  The prefix to prepend before each column name.
     * @return the column names
     */
    protected static String commaSeparated(Class<? extends Enum<?>> enumClass, String tableId) {
        return Arrays.stream(enumClass.getEnumConstants()).map(e -> tableId + "." + e.toString()).collect(Collectors.joining(","));
    }

    /**
     * Given an enumeration of columns, create the column assignment specification of an update
     * statement.  Given an enumeration of A,B,C for example, generate A=?, B=?, C=?.
     * @param enumClass  an enumeration of columns for a table
     * @return  the sql to assign values to the columns
     */
    protected static String updateSql(Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(e -> e + " = ?").collect(Collectors.joining(","));
    }

    /**
     * Pre-pends a table id to an enumeration value with a period delimiter.
     * BOOK_COL.id, for example, would yield "a.id" given a table id of "a".
     *
     * @param e  An enumeration value.
     * @param tableId  A table identifier
     * @return  The string value of the enumeration with the table id prepended.
     */
    protected static String withTableId(Enum<?> e, String tableId) {
        return tableId + "." + e.toString();
    }
}
