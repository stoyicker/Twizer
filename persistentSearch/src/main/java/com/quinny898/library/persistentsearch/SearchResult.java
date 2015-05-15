package com.quinny898.library.persistentsearch;

import android.graphics.drawable.Drawable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SearchResult {
    public String mTitle;
    public Drawable mIcon;

    /**
     * Create a search result with text and an mIcon
     *
     * @param title {@link String} The mTitle
     * @param icon  {@link Drawable} The mIcon
     */
    public SearchResult(final String title, final Drawable icon) {
        this.mTitle = title;
        this.mIcon = icon;
    }

    /**
     * Return the title of the result
     */
    @Override
    public String toString() {
        return mTitle;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(mTitle)
//                .append(mIcon)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof SearchResult) {
            final SearchResult other = (SearchResult) obj;
            return new EqualsBuilder()
                    .append(mTitle, other.mTitle)
//                    .append(mIcon, other.mIcon)
                    .isEquals();
        } else {
            return Boolean.FALSE;
        }
    }
}