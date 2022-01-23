package org.themullers.library;

import com.amazonaws.services.s3.model.S3Object;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LibUtils {

    public final static String STANDALONE = "Standalone";

    /**
     * Groups a list of assets by the series those assets are associated with;
     * assets from Scalzi's "Old Man's War" series would be grouped separately
     * from assets in his "Interdependency" series.
     *
     * The returned data structure's elements (the keys and values of the map)
     * are ordered chronologically by publication date; the assets within that series
     * (the values) are ordered by their publication date and the series (the keys)
     * are ordered by the publication date of the earliest book in the series.
     *
     * If the asset is not in a series (if getSeries() returns null), the asset
     * will be grouped as STANDALONE.
     *
     * @param assets The assets to organize
     * @return  A map of assets grouped by series.
     */
    public static Map<String, List<Asset>> groupAssetsBySeries(List<Asset> assets) {

        // start by sorting the provided assets by their publication year
        assets.sort((Asset a, Asset b) -> a.getPublicationYear() - b.getPublicationYear());

        // create a map to hold the groupings
        var groupedAssets = new LinkedHashMap<String, List<Asset>>();

        // for each asset
        for (var asset : assets) {

            // get the series (or use STANDALONE as the series if there isn't one)
            var series = Utils.ifNull(asset.getSeries(), STANDALONE);

            // if this is the first asset we've encountered for a series, create an entry with an empty list
            groupedAssets.putIfAbsent(series, new LinkedList<>());

            // add the asset to the list of assets for this series
            groupedAssets.get(series).add(asset);
        }

        return groupedAssets;
    }

    public static Map<Integer, Asset> indexAssetsById(List<Asset> assets) {
        return assets.stream().collect(Collectors.toMap(Asset::getId, Function.identity()));
    }

    /**
     * determine a file's mime type based on its extension
     *
     * @param filename a filename
     * @return the mime type
     */
    public static String mimeTypeForFile(String filename) {
        if (filename.toLowerCase().endsWith(".epub")) {
            return "application/epub+zip";
        } else if (filename.toLowerCase().endsWith(".m4b")) {
            return "audio/mp4a-latm";
        }

        throw new RuntimeException("can't determine mime type for file " + filename);
    }


    /**
     * Writes an S3 object to an HTTP response object
     *
     * @param obj      the S3 object to write
     * @param response the HTTP response object to write to
     * @throws IOException thrown if an unexpected error occurs writing to the response
     */
    public static void writeS3ObjectToResponse(S3Object obj, HttpServletResponse response) throws IOException {

        // escape any quotation marks in the filename with a backslash
        var filename = obj.getKey();
        var escapedFilename = filename.replace("\"", "\\\"");

        // build the content disposition
        var disposition = String.format("attachment; filename=\"%s\"", escapedFilename);

        // convert the content length from the object metadata into an int as required by the servlet response object
        var contentLength = Math.toIntExact(obj.getObjectMetadata().getContentLength());

        // figure out a mime type based on the file's extension
        var mimeType = LibUtils.mimeTypeForFile(filename);

        // write the S3 object info to the response
        response.setContentLength(contentLength);
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", disposition);
        obj.getObjectContent().transferTo(response.getOutputStream());
        response.flushBuffer();
    }
}
