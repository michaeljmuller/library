package org.themullers.library;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.stereotype.Component;
import org.themullers.library.db.LibraryDAO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Spreadsheet {

    public static final String SHEET_NAME = "Media Assets";

    // this is m/d/yy according to https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/BuiltinFormats.html
    public static final short EXCEL_BUILTIN_MDY_FORMAT = 0xe;

    public static void toDatabase(LibraryDAO dao, byte[] spreadsheet) throws IOException {

        // get all the assets from the database, indexed by asset id
        var dbAssetList = dao.fetchAllAssets();
        var dbAssetMap = dbAssetList.stream().collect(Collectors.toMap(Asset::getId, Function.identity()));

        try (var bais = new ByteArrayInputStream(spreadsheet)) {
            var wb = new XSSFWorkbook(bais);
            var sheet = wb.getSheet(SHEET_NAME);
            for (var poiRow : sheet) {

                var row = new AssetRow((XSSFRow) poiRow);

                // skip the header row
                if (row.isHeader()) {
                    continue;
                }

                // if the asset in this row matches the asset with the same ID from the database, no changes are necessary
                var ssAsset = createAssetFromSpreadsheet(row);
                var dbAsset = dbAssetMap.get(ssAsset.getId());
                if (ssAsset.equals(dbAsset)) {
                    System.out.println(String.format("assets match, no update necessary: id %d, title %s", ssAsset.getId(), ssAsset.getEbookS3ObjectKey()));
                }

                // the assets don't match; some inserting/updating is required
                else {
                    // if the asset doesn't exist in the database then insert it
                    if (dbAsset == null) {
                        System.out.println(String.format("new asset, inserting: title %s", ssAsset.getEbookS3ObjectKey()));
                        //dao.insertAsset(ssAsset);
                    }

                    // the asset does exist; something needs to be updated
                    else {

                        // start by seeing if the tags differ
                        var ssTags = ssAsset.getTags();
                        var dbTags = dbAsset.getTags();
                        if (!Utils.objectsAreEqual(ssTags, dbTags)) {
                            System.out.println(String.format("assets tags differ, setting new tags: id %d, title %s", ssAsset.getId(), ssAsset.getEbookS3ObjectKey()));
                            //dao.setTags(ssAsset.getId(), ssTags);
                            dbAsset.setTags(ssTags);
                        }

                        // now that we've updated the tags, do we need to update the asset itself?
                        if (!ssAsset.equals(dbAsset)) {
                            System.out.println(String.format("assets metadata differs, updating: id %d, title %s", ssAsset.getId(), ssAsset.getEbookS3ObjectKey()));
                            //dao.updateAsset(ssAsset);
                        }
                    }
                }
            }

        }
    }

    protected static Asset createAssetFromSpreadsheet(AssetRow row) {

        var asset = new Asset();
        asset.setId(row.getIntValue(Column.DBID));
        asset.setTitle(row.getStringValue(Column.TITLE));
        asset.setAuthor(row.getStringValue(Column.AUTHOR));
        asset.setAuthor2(row.getStringValue(Column.AUTHOR2));
        asset.setAuthor3(row.getStringValue(Column.AUTHOR3));
        asset.setPublicationYear(row.getIntValue(Column.PUB_YEAR));
        String series = row.getStringValue(Column.SERIES);
        if (series != null && series.trim().length() > 0) {
            asset.setSeries(series);
            asset.setSeriesSequence(row.getIntValue(Column.SERIES_SEQUENCE));
        }
        asset.setAcquisitionDate(row.getDateValue(Column.ACQ_DATE));
        asset.setAltTitle1(row.getStringValue(Column.ALT_TITLE1));
        asset.setAltTitle2(row.getStringValue(Column.ALT_TITLE2));
        asset.setEbookS3ObjectKey(row.getStringValue(Column.EBOOK_S3_OBJ_KEY));
        asset.setAudiobookS3ObjectKey(row.getStringValue(Column.AUDIOBOOK_S3_OBJ_KEY));

        // split the comma-separated tags and add them to the asset's list individually
        String tagCSV = row.getStringValue(Column.TAGS);
        if (tagCSV != null && tagCSV.trim().length() > 0) {
            var tagArray = tagCSV.split(",");
            for (var tag : tagArray) {
                asset.addTag(tag.trim());
            }
        }

        return asset;
    }

    public static byte[] fromDatabase(LibraryDAO dao) throws IOException {
        var wb = XSSFWorkbookFactory.createWorkbook();
        var sheet = wb.createSheet(SHEET_NAME);

        // make the text larger for my geriatric eyeballs
        sheet.setZoom(150);

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

        // for each asset in the database
        int rowNum = 1;
        for (var asset : dao.fetchAllAssets()) {

            var row = new AssetRow(sheet.createRow(rowNum));
            row.setValue(Column.DBID, asset.getId());
            row.setValue(Column.TITLE, asset.getTitle());
            row.setValue(Column.AUTHOR, asset.getAuthor());
            row.setValue(Column.AUTHOR2, asset.getAuthor2());
            row.setValue(Column.AUTHOR3, asset.getAuthor3());
            row.setValue(Column.PUB_YEAR, asset.getPublicationYear());
            var series = asset.getSeries();
            if (series != null && series.trim().length() > 0) {
                row.setValue(Column.SERIES, asset.getSeries());
                row.setValue(Column.SERIES_SEQUENCE, asset.getSeriesSequence());
            }
            row.setValue(Column.ACQ_DATE, asset.getAcquisitionDate());
            row.setValue(Column.ALT_TITLE1, asset.getAltTitle1());
            row.setValue(Column.ALT_TITLE2, asset.getAltTitle2());
            row.setValue(Column.EBOOK_S3_OBJ_KEY, asset.getEbookS3ObjectKey());
            row.setValue(Column.AUDIOBOOK_S3_OBJ_KEY, asset.getAudiobookS3ObjectKey());
            // TODO: populate the tags

            rowNum++;
        }

        // resize the columns to fit the content
        for (var column : Column.values()) {
            sheet.autoSizeColumn(column.getNumber());
        }

        // freeze the header row
        sheet.createFreezePane(0,1);

        return bytesForWorkbook(wb);
    }

    static byte[] bytesForWorkbook(XSSFWorkbook wb) throws IOException {

        byte bytes[];

        try (var bos = new ByteArrayOutputStream()) {
            wb.write(bos);
            bytes = bos.toByteArray();
        }

        return bytes;
    }

    static class AssetRow {
        private XSSFRow row;
        public AssetRow(XSSFRow row) {
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
            return cell == null ? null : cell.getStringCellValue();
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
        EBOOK_S3_OBJ_KEY(11, "Ebook Object Key"),
        AUDIOBOOK_S3_OBJ_KEY(12, "Audiobook Object Key"),
        TAGS(13, "Tags");

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
