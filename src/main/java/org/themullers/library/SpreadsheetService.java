package org.themullers.library;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.stereotype.Service;
import org.themullers.library.db.LibraryDAO;
import org.themullers.library.s3.LibraryOSAO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SpreadsheetService {

    protected LibraryDAO dao;
    protected LibraryOSAO osao;

    public SpreadsheetService(LibraryDAO dao, LibraryOSAO osao) {
        this.dao = dao;
        this.osao = osao;
    }

    public static final String SHEET_NAME = "Media Assets";
    public static final String SHEET_NEW_AUDIOBOOKS = "New Audiobooks";

    // this is m/d/yy according to https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/BuiltinFormats.html
    public static final short EXCEL_BUILTIN_MDY_FORMAT = 0xe;

    public void upload(byte[] spreadsheet) throws IOException {

        // get all the books from the database, indexed by book id
        var dbBookList = dao.fetchAllBooks();
        var dbBookMap = dbBookList.stream().collect(Collectors.toMap(Book::getId, Function.identity()));

        try (var bais = new ByteArrayInputStream(spreadsheet)) {

            var wb = new XSSFWorkbook(bais);
            var sheet = wb.getSheet(SHEET_NAME);
            for (var poiRow : sheet) {

                System.out.println("row " + poiRow.getRowNum());

                var row = new BookRow((XSSFRow) poiRow);

                // skip the header row and blank rows
                if (row.isHeader() || row.isBlank()) {
                    System.out.println("skipping");
                    continue;
                }

                // if the book in this row matches the book with the same ID from the database, no changes are necessary
                var ssBook = createBookFromSpreadsheet(row);
                var dbBook = dbBookMap.get(ssBook.getId());
                if (ssBook.equals(dbBook)) {
                    System.out.println(String.format("books match, no update necessary: id %d, title %s", ssBook.getId(), ssBook.getEpubObjectKey()));
                }

                // the books don't match; some inserting/updating is required
                else {
                    // if the book doesn't exist in the database then insert it
                    if (dbBook == null) {
                        System.out.println(String.format("new book, inserting: title %s", ssBook.getEpubObjectKey()));
                        dao.insertBook(ssBook);
                    }

                    // the book does exist; something needs to be updated
                    else {

                        // start by seeing if the tags differ
                        var ssTags = ssBook.getTags();
                        var dbTags = dbBook.getTags();
                        if (!Utils.objectsAreEqual(ssTags, dbTags)) {
                            System.out.println(String.format("book tags differ, setting new tags: id %d, title %s", ssBook.getId(), ssBook.getEpubObjectKey()));
                            dao.setTags(ssBook.getId(), ssTags);
                            dbBook.setTags(ssTags);
                        }

                        // now that we've updated the tags, do we need to update the book itself?
                        if (!ssBook.equals(dbBook)) {
                            System.out.println(String.format("book metadata differs, updating: id %d, title %s", ssBook.getId(), ssBook.getEpubObjectKey()));
                            dao.updateBook(ssBook);
                        }
                    }
                }
            }

        }
    }

    protected static Book createBookFromSpreadsheet(BookRow row) {

        var book = new Book();
        book.setId(row.getIntValue(Column.DBID));
        book.setTitle(row.getStringValue(Column.TITLE));
        book.setAuthor(row.getStringValue(Column.AUTHOR));
        book.setAuthor2(row.getStringValue(Column.AUTHOR2));
        book.setAuthor3(row.getStringValue(Column.AUTHOR3));
        book.setPublicationYear(row.getIntValue(Column.PUB_YEAR));
        String series = row.getStringValue(Column.SERIES);
        if (series != null && series.trim().length() > 0) {
            book.setSeries(series);
            book.setSeriesSequence(row.getIntValue(Column.SERIES_SEQUENCE));
        }
        book.setAcquisitionDate(row.getDateValue(Column.ACQ_DATE));
        book.setAltTitle1(row.getStringValue(Column.ALT_TITLE1));
        book.setAltTitle2(row.getStringValue(Column.ALT_TITLE2));
        book.setEpubObjectKey(row.getStringValue(Column.EPUB_OBJ_KEY));
        book.setMobiObjectKey(row.getStringValue(Column.MOBI_OBJ_KEY));
        book.setAudiobookObjectKey(row.getStringValue(Column.AUDIOBOOK_OBJ_KEY));
        book.setAmazonId(row.getStringValue(Column.ASIN));

        // split the comma-separated tags and add them to the book's list individually
        String tagCSV = row.getStringValue(Column.TAGS);
        if (tagCSV != null && tagCSV.trim().length() > 0) {
            var tagArray = tagCSV.split(",");
            for (var tag : tagArray) {
                book.addTag(tag.trim());
            }
        }

        return book;
    }

    public byte[] download() throws IOException {
        var books = dao.fetchAllBooks();
        var objects = osao.listObjects();
        var dbEbooks = new LinkedList<String>();
        var dbAudiobooks = new LinkedList<String>();

        var wb = XSSFWorkbookFactory.createWorkbook();
        var sheet = wb.createSheet(SHEET_NAME);
        var audioSheet = wb.createSheet(SHEET_NEW_AUDIOBOOKS);

        // make the text larger for my geriatric eyeballs
        sheet.setZoom(150);
        audioSheet.setZoom(150);

        // create a header row
        var header = sheet.createRow(0);
        for (var column : Column.values()) {
            header.createCell(column.getNumber()).setCellValue(column.getHeader());
        }

        // create a bold font
        var newFont = wb.createFont();
        newFont.setBold(true);

        // make each header cell bold
        for (var column : Column.values()) {
            var cell = header.getCell(column.getNumber());
            var style = cell.getCellStyle().copy();
            style.setFont(newFont);
            cell.setCellStyle(style);
        }

        // write each book that we found in the database to the spreadsheet
        int rowNum = 1;
        for (var book : books) {
            writeBookToSpreadsheet(book, sheet, rowNum++);
            addToList(book.getEpubObjectKey(), dbEbooks);
            addToList(book.getAudiobookObjectKey(), dbAudiobooks);
        }

        // resize the columns to fit the content
        for (var column : Column.values()) {
            sheet.autoSizeColumn(column.getNumber());
        }

        // freeze the header row
        sheet.createFreezePane(0,1);

        return bytesForWorkbook(wb);
    }

    protected void addToList(String item, List<String> list) {
        if (item != null && !list.contains(item)) {
            list.add(item);
        }
    }

    protected void writeBookToSpreadsheet(Book book, XSSFSheet sheet, int rowNum) {
        var row = new BookRow(sheet.createRow(rowNum));
        row.setValue(Column.DBID, book.getId());
        row.setValue(Column.TITLE, book.getTitle());
        row.setValue(Column.AUTHOR, book.getAuthor());
        row.setValue(Column.AUTHOR2, book.getAuthor2());
        row.setValue(Column.AUTHOR3, book.getAuthor3());
        var pubYear = book.getPublicationYear();
        if (pubYear != null) {
            row.setValue(Column.PUB_YEAR, pubYear);
        }
        var series = book.getSeries();
        if (series != null && series.trim().length() > 0) {
            row.setValue(Column.SERIES, book.getSeries());
            row.setValue(Column.SERIES_SEQUENCE, book.getSeriesSequence());
        }
        row.setValue(Column.ACQ_DATE, book.getAcquisitionDate());
        row.setValue(Column.ALT_TITLE1, book.getAltTitle1());
        row.setValue(Column.ALT_TITLE2, book.getAltTitle2());
        row.setValue(Column.EPUB_OBJ_KEY, book.getEpubObjectKey());
        row.setValue(Column.MOBI_OBJ_KEY, book.getMobiObjectKey());
        row.setValue(Column.AUDIOBOOK_OBJ_KEY, book.getAudiobookObjectKey());
        var tags = book.getTags();
        if (tags != null) {
            row.setValue(Column.TAGS, String.join(", ", book.getTags()));
        }
        row.setValue(Column.ASIN, book.getAmazonId());
    }

    static byte[] bytesForWorkbook(XSSFWorkbook wb) throws IOException {

        byte bytes[];

        try (var bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            bytes = bos.toByteArray();
        }

        return bytes;
    }

    static class BookRow {
        private XSSFRow row;
        public BookRow(XSSFRow row) {
            this.row = row;
        }

        protected int getIntValue(Column column) {
            var cell = row.getCell(column.getNumber());
            return cell == null || cell.getCellType() != CellType.NUMERIC ? 0 : (int) cell.getNumericCellValue();
        }

        protected void setValue(Column column, int value) {
            row.createCell(column.getNumber()).setCellValue(value);
        }

        protected String getStringValue(Column column) {
            var cell = row.getCell(column.getNumber());
            if (cell == null) {
                return null;
            }
            var stringValue = cell.getStringCellValue();
            if (stringValue == null || stringValue.trim().length() == 0) {
                return null;
            }
            return stringValue;
        }

        protected void setValue(Column column, String value) {
            row.createCell(column.getNumber()).setCellValue(value);
        }

        protected Date getDateValue(Column column) {
            var cell = row.getCell(column.getNumber());
            return cell == null ? null : cell.getDateCellValue();
        }

        protected void setValue(Column column, Date value) {
            var cell = row.createCell(column.getNumber());
            cell.setCellValue(value);

            // format the cell to display the date correctly
            var style = cell.getCellStyle().copy();
            style.setDataFormat(EXCEL_BUILTIN_MDY_FORMAT);
            cell.setCellStyle(style);
        }

        protected boolean isHeader() {
            return row.getRowNum() == 0;
        }

        // from https://stackoverflow.com/a/28451783
        protected boolean isBlank() {
            if (row == null) {
                return true;
            }

            // get the first and last cell numbers; these are zero-based indices
            var firstCellNum = row.getFirstCellNum();
            var lastCellNum = row.getLastCellNum(); // this is the last cell index PLUS ONE

            // if there are no cells, the row is blank
            if (lastCellNum <= 0) {
                return true;
            }

            // for each cell
            for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                Cell cell = row.getCell(cellNum);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    var cellStr = cell.toString();
                    // if we found something in the cell that con be converted to a non-zero length string, then the cell is not blank
                    if (!Utils.isBlank(cellStr)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public enum Column {
        DBID(0, "dbid"),
        TITLE(1, "Title"),
        AUTHOR(2, "Author"),
        AUTHOR2(3, "Author 2"),
        AUTHOR3(4, "Author 3"),
        PUB_YEAR(5, "Pub Year"),
        SERIES(6, "Series"),
        SERIES_SEQUENCE(7, "Num"),
        ACQ_DATE(8, "Acq Date"),
        ALT_TITLE1(9, "Alt Title 1"),
        ALT_TITLE2(10, "Alt Title 2"),
        EPUB_OBJ_KEY(11, "Epub Object Key"),
        MOBI_OBJ_KEY(12, "Mobi Object Key"),
        AUDIOBOOK_OBJ_KEY(13, "Audiobook Object Key"),
        TAGS(14, "Tags"),
        ASIN(15, "ASIN");

        private int number;
        private String header;

        Column(int number, String header) {
            this.number = number;
            this.header = header;
        }

        public int getNumber() {
            return number;
        }

        public String getHeader() {
            return header;
        }
    }
}
