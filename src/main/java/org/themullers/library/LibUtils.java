package org.themullers.library;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

}
