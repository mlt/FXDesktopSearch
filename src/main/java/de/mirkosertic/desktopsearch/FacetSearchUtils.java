/**
 * FreeDesktopSearch - A Search Engine for your Desktop
 * Copyright (C) 2013 Mirko Sertic
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.mirkosertic.desktopsearch;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.search.DocValuesRangeFilter;

final class FacetSearchUtils {

    private final static Pattern rangePattern =
            Pattern.compile("(?:(?<left>-?\\d+)(?<lefti><=?))?(?<dim>[^<=]+)(?:(?<righti><=?)(?<right>\\d+))?");

    private FacetSearchUtils() {
    }

    public static String encode(String aDimension, String aValue) {
        return aDimension+"="+aValue;
    }

    public static String encode(String aDimension, LongRange aValue) {
        String out = "";
        if (aValue.min != Long.MIN_VALUE)
            out = aValue.min + (aValue.minInclusive ? "<=" : "<");
        out += aDimension;
        if (aValue.max != Long.MAX_VALUE)
            out += (aValue.maxInclusive ? "<=" : "<") + aValue.max;
        return out;
    }

    public static void addToMap(String aDimensionCriteria, Map<String, Object> aDrilldownDimensions) {
        Matcher matcher = rangePattern.matcher(aDimensionCriteria);
        if (matcher.matches()) {
            String dim = matcher.group("dim");
            String leftString = matcher.group("left");
            String rightString = matcher.group("right");
            Long left = null;
            Long right = null;
            String leftiString = matcher.group("lefti");
            boolean lefti = null != leftiString && leftiString.equals("<=");
            String rightiString = matcher.group("righti");
            boolean righti = null != rightiString && rightiString.equals("<=");
            if (null != leftString)
                left = Long.valueOf(leftString);
            if (null != rightString)
                right = Long.valueOf(rightString);
            DocValuesRangeFilter<Long> f = DocValuesRangeFilter.newLongRange(dim, left, right, lefti, righti);
            aDrilldownDimensions.put(dim, f);
            return;
        }

        int p = aDimensionCriteria.indexOf("=");
        if (-1 != p) {
            aDrilldownDimensions.put(aDimensionCriteria.substring(0, p), aDimensionCriteria.substring(p + 1));
            return;
        }
    }
}
