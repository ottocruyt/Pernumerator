/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.android.pernumerator.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Pernumerator app.
 */
public final class ItemContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ItemContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "be.android.pernumerator";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://be.android.android.pernumerator/pernumerator/ is a valid path for
     * looking at item data. content://be.android.android.pernumerator/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_ITEMS = "items";

    /**
     * Inner class that defines constant values for the items database table.
     * Each entry in the table represents a single item.
     */
    public static final class ItemEntry implements BaseColumns {

        /** The content URI to access the item data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /** Name of database table for be.android.pernumerator */
        public final static String TABLE_NAME = "items";

        /**
         * Unique ID number for the item (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_NAME ="name";

        /**
         * Type of the item.
         *
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_TYPE = "type";

        /**
         * Owner of the item.
         *
         * The only possible values are {@link #OWNER_OTHER}, {@link #OWNER_OTTO},
         * {@link #OWNER_LINDE} or {@link #OWNER_BOTH}
         *
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_OWNER = "owner";

        /**
         * Possible values for the owner of the item.
         */
        public static final int OWNER_BOTH = 0;
        public static final int OWNER_OTTO = 1;
        public static final int OWNER_LINDE = 2;
        public static final int OWNER_OTHER = 3;

        /**
         * Weight of the item.
         *
         * Type: FLOAT
         */
        public final static String COLUMN_ITEM_WEIGHT = "weight";

        /**
         * Price of the item
         * Type: FLOAT
         *
         */
        public final static String COLUMN_ITEM_PRICE = "price";

        /**
         * Dimensions of the item
         * L:
         * W:
         * H:
         * Type: FLOAT
         *
         */

        public final static String COLUMN_ITEM_DIM_L = "length";
        public final static String COLUMN_ITEM_DIM_W = "width";
        public final static String COLUMN_ITEM_DIM_H = "height";

        /**
         *
         * Image of the item
         *
         * Type: BLOB
         *
         */
        public final static String COLUMN_ITEM_IMG = "image";

        /**
         *
         * barcode of the item
         *
         * Type: STRING (because no calc will be done on the number)
         *
         */
        public final static String COLUMN_ITEM_BARCODE = "barcode";




        /**
         * Returns whether or not the given gender is {@link #OWNER_BOTH}, {@link #OWNER_OTTO},
         * {@link #OWNER_LINDE} or {@link #OWNER_OTHER}.
         */
        public static boolean isValidOwner(int owner) {
            return owner == OWNER_BOTH || owner == OWNER_OTTO || owner == OWNER_LINDE || owner == OWNER_OTHER;
        }
    }

}

