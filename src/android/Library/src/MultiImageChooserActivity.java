/*
 * Copyright (c) 2012, David Erosa
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following  conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following  disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,  BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT  SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR  BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDIN G NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH  DAMAGE
 *
 * Code modified by Andrew Stephan for Sync OnSet
 *
 */

package com.synconset;

import java.net.URI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.synconset.FakeR;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

// For landscape orientation fix (imports ActivityInfo)
import android.content.pm.ActivityInfo;
// For exif orientation tag
import android.media.ExifInterface;


public class MultiImageChooserActivity extends Activity implements OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ImagePicker";

    public static final int NOLIMIT = -1;
    public static final String MAX_IMAGES_KEY = "MAX_IMAGES";
    public static final String WIDTH_KEY = "WIDTH";
    public static final String HEIGHT_KEY = "HEIGHT";
    public static final String QUALITY_KEY = "QUALITY";
    public static final String SELECTED_KEY = "SELECTED_KEY";
    public static final String ADD_IMAGES = "ADD_IMAGES";

    private ImageAdapter ia;

    private Cursor imagecursor, actualimagecursor;
    private int image_column_index, image_column_orientation, actual_image_column_index, orientation_column_index;
    private int colWidth;

    private static final int CURSORLOADER_THUMBS = 0;
    private static final int CURSORLOADER_REAL = 1;

    private Map<String, Integer> fileNames = new HashMap<String, Integer>();
    private Map<String, Integer> fileNamesRemoved = new HashMap<String, Integer>();

    private SparseBooleanArray checkStatus = new SparseBooleanArray();

    private int maxImages;
    private int maxImageCount;
    private int addImagesCount;

    private int desiredWidth;
    private int desiredHeight;
    private int quality;

    private GridView gridView;

    private final ImageFetcher fetcher = new ImageFetcher();

    private int selectedColor = 0xffc70f3f;
    private boolean shouldRequestThumb = true;

    private FakeR fakeR;

    private ProgressDialog progress;

    private boolean lockPicker = false;
    private String alreadySelectedFileNames = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.d("ZETBOOK", "onCreate");
        super.onCreate(savedInstanceState);
        fakeR = new FakeR(this);
        // Fix orientation to landscape - call this before setContentView
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(fakeR.getId("layout", "multiselectorgrid"));
        fileNames.clear();
        fileNamesRemoved.clear();

        maxImages = getIntent().getIntExtra(MAX_IMAGES_KEY, NOLIMIT);
        desiredWidth = getIntent().getIntExtra(WIDTH_KEY, 0);
        desiredHeight = getIntent().getIntExtra(HEIGHT_KEY, 0);
        quality = getIntent().getIntExtra(QUALITY_KEY, 0);
        alreadySelectedFileNames = getIntent().getStringExtra(SELECTED_KEY);
        addImagesCount = getIntent().getIntExtra(ADD_IMAGES, 0);
        maxImageCount = maxImages;

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        colWidth = width / 4;

        gridView = (GridView) findViewById(fakeR.getId("id", "gridview"));
        gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(new OnScrollListener() {
            private int lastFirstItem = 0;
            private long timestamp = System.currentTimeMillis();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    shouldRequestThumb = true;
                    ia.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                float dt = System.currentTimeMillis() - timestamp;
                if (firstVisibleItem != lastFirstItem) {
                    double speed = 1 / dt * 1000;
                    lastFirstItem = firstVisibleItem;
                    timestamp = System.currentTimeMillis();

                    // Limit if we go faster than a page a second
                    shouldRequestThumb = speed < visibleItemCount;
                }
            }
        });

        ia = new ImageAdapter(this);
        gridView.setAdapter(ia);

        LoaderManager.enableDebugLogging(false);
        getLoaderManager().initLoader(CURSORLOADER_THUMBS, null, this);
        getLoaderManager().initLoader(CURSORLOADER_REAL, null, this);
        setupHeader();

        // Some preselected files
        if(alreadySelectedFileNames.length() > 0) {
            String[] list = alreadySelectedFileNames.split(";");
            for(String item : list)
            {
                // Push and ignore rotation - rotation is checked separately before resize
                fileNames.put(item, 0);
            }
        }

        updateAcceptButton();
        int count = addImagesCount + fileNames.size();
        int maxCount = addImagesCount + maxImageCount;
        updateHeaderText("Vybráno " + count + " z " + maxCount + "");
        progress = new ProgressDialog(this);
        progress.setTitle("Zpracovávám fotografie");
        progress.setMessage("Zpracování může chvilku trvat");
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        // Log.d("ZETBOOK", "onItemClick");

        if (lockPicker == true) {
            return;
        }
        String name = getImageName(position);
        int rotation = getImageRotation(position);

        if (name == null) {
            return;
        }

        // boolean selected = isSelected(name);
        // if(selected)
        // {
        //     return;
        // }

        boolean isChecked = !isChecked(position);

        if (maxImages == 0 && isChecked) {
            isChecked = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            int maxCount = addImagesCount + maxImageCount;
            builder.setTitle("Maximum " + maxCount + " fotek");
            builder.setMessage("Můžete vybrat maximálně " + maxCount + " fotek.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (isChecked) {
            fileNames.put(name, new Integer(rotation));
            fileNamesRemoved.remove(name);
            if (maxImageCount == 1) {
                this.selectClicked(null);
            } else {
                maxImages--;
                ImageView imageView = (ImageView)view;
                if (android.os.Build.VERSION.SDK_INT>=16) {
                  imageView.setImageAlpha(160);
                } else {
                  imageView.setAlpha(160);
                }
                view.setBackgroundColor(selectedColor);
            }
        } else {
            fileNames.remove(name);
            fileNamesRemoved.put(name, 0);
            maxImages++;
            ImageView imageView = (ImageView)view;
            if (android.os.Build.VERSION.SDK_INT>=16) {
                imageView.setImageAlpha(255);
            } else {
                imageView.setAlpha(255);
            }
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        checkStatus.put(position, isChecked);
        updateAcceptButton();
        int count = addImagesCount + fileNames.size();
        int maxCount = addImagesCount + maxImageCount;
        updateHeaderText("Vybráno " + count + " z " + maxCount + "");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int cursorID, Bundle arg1) {
        // Log.d("ZETBOOK", "onCreateLoader");
        CursorLoader cl = null;

        ArrayList<String> img = new ArrayList<String>();
        switch (cursorID) {

        case CURSORLOADER_THUMBS:
            img.add(MediaStore.Images.Media._ID);
            img.add(MediaStore.Images.Media.ORIENTATION);
            break;
        case CURSORLOADER_REAL:
            img.add(MediaStore.Images.Thumbnails.DATA);
            img.add(MediaStore.Images.Media.ORIENTATION);
            break;
        default:
            break;
        }

        cl = new CursorLoader(MultiImageChooserActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                img.toArray(new String[img.size()]), null, null, "DATE_MODIFIED DESC");

        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Log.d("ZETBOOK", "onLoadFinished");
        if (cursor == null) {
            // NULL cursor. This usually means there's no image database yet....
            return;
        }

        switch (loader.getId()) {
            case CURSORLOADER_THUMBS:
                imagecursor = cursor;
                image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
                image_column_orientation = imagecursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                ia.notifyDataSetChanged();
                break;
            case CURSORLOADER_REAL:
                actualimagecursor = cursor;
                String[] columns = actualimagecursor.getColumnNames();
                actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                orientation_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION);
                break;
            default:
                break;
        }

        gridView.invalidateViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Log.d("ZETBOOK", "onLoaderReset");
        if (loader.getId() == CURSORLOADER_THUMBS) {
            imagecursor = null;
        } else if (loader.getId() == CURSORLOADER_REAL) {
            actualimagecursor = null;
        }
    }

    public void cancelClicked(View ignored) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void selectClicked(View ignored) {
        lockPicker = true;
        ((TextView) getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_done_textview"))).setEnabled(false);
        getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_done")).setEnabled(false);
        progress.show();
        Intent data = new Intent();
        if (fileNames.isEmpty()) {
            Bundle res = new Bundle();
            res.putString("REMOVEDFILENAMES", fileNamesRemoved.toString());
            res.putString("TYPE", "CLEAR");
            data.putExtras(res);
            this.setResult(RESULT_OK, data);
            progress.dismiss();
            finish();
        } else {
            new ResizeImagesTask().execute(fileNames.entrySet());
        }
    }


    /*********************
     * Helper Methods
     ********************/
    private void updateAcceptButton() {
        boolean enabled = fileNames.size() != 0;
        // Enable also when some removed and new selection is empty
        if(fileNamesRemoved.size() != 0)
        {
            enabled = true;
        }
        ((TextView) getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_done_textview")))
            .setEnabled(enabled);
        getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_done")).setEnabled(enabled);
    }

    private void updateHeaderText(String text) {
        ((TextView) getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_status_textview")))
            .setText(text);
    }

    private void setupHeader() {
        // From Roman Nkk's code
        // https://plus.google.com/113735310430199015092/posts/R49wVvcDoEW
        // Inflate a "Done/Discard" custom action bar view
        /*
         * Copyright 2013 The Android Open Source Project
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *     http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext().getSystemService(
                LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(fakeR.getId("layout", "actionbar_custom_view_done_discard"), null);
        customActionBarView.findViewById(fakeR.getId("id", "actionbar_done")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Done"
                selectClicked(null);
            }
        });
        customActionBarView.findViewById(fakeR.getId("id", "actionbar_discard")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private String getImageName(int position) {
        actualimagecursor.moveToPosition(position);
        String name = null;

        try {
            name = actualimagecursor.getString(actual_image_column_index);
        } catch (Exception e) {
            return null;
        }
        return name;
    }

    private int getImageRotation(int position) {
        actualimagecursor.moveToPosition(position);
        int rotation = 0;

        try {
            rotation = actualimagecursor.getInt(orientation_column_index);
        } catch (Exception e) {
            return rotation;
        }
        return rotation;
    }

    public boolean isChecked(int position) {
        boolean ret = checkStatus.get(position);
        return ret;
    }

    public boolean isSelected(String fileName) {
        if(alreadySelectedFileNames.length() == 0)
        {
            return false;
        }
        boolean ret = alreadySelectedFileNames.contains(fileName);
        return ret;
    }

    public int getSelectedCount() {
        if(alreadySelectedFileNames.length() == 0)
        {
            return 0;
        }
        int ret = alreadySelectedFileNames.length() - alreadySelectedFileNames.replace(";", "").length() + 1;
        return ret;
    }


    /*********************
    * Nested Classes
    ********************/
    private class SquareImageView extends ImageView {
        public SquareImageView(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }


    private class ImageAdapter extends BaseAdapter {
        private final Bitmap mPlaceHolderBitmap;

        public ImageAdapter(Context c) {
            Bitmap tmpHolderBitmap = BitmapFactory.decodeResource(getResources(), fakeR.getId("drawable", "loading_icon"));
            mPlaceHolderBitmap = Bitmap.createScaledBitmap(tmpHolderBitmap, colWidth, colWidth, false);
            if (tmpHolderBitmap != mPlaceHolderBitmap) {
                tmpHolderBitmap.recycle();
                tmpHolderBitmap = null;
            }
        }

        public int getCount() {
            if (imagecursor != null) {
                return imagecursor.getCount();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int pos, View convertView, ViewGroup parent) {

            if (convertView == null) {
                ImageView temp = new SquareImageView(MultiImageChooserActivity.this);
                temp.setScaleType(ImageView.ScaleType.CENTER_CROP);
                convertView = (View)temp;
            }

            ImageView imageView = (ImageView)convertView;
            imageView.setImageBitmap(null);

            final int position = pos;

            if (!imagecursor.moveToPosition(position)) {
                return imageView;
            }

            if (image_column_index == -1) {
                return imageView;
            }

            //  Needed for proper getImageName (otherwise randomly fails)
            boolean selected = false;
            String name = null;
            if (actualimagecursor != null ) {
                name = getImageName(pos);
                selected = isSelected(name);
            }

            final int id = imagecursor.getInt(image_column_index);
            final int rotate = imagecursor.getInt(image_column_orientation);

            // Consider selected file as checked
            if(selected)
            {
                checkStatus.put(pos, true);
            }

            if (isChecked(pos)) {
                if (android.os.Build.VERSION.SDK_INT>=16) {
                  imageView.setImageAlpha(160);
                } else {
                  imageView.setAlpha(160);
                }
                imageView.setBackgroundColor(selectedColor);
            } else {
                if (android.os.Build.VERSION.SDK_INT>=16) {
                  imageView.setImageAlpha(255);
                } else {
                  imageView.setAlpha(255);
                }
                imageView.setBackgroundColor(Color.TRANSPARENT);
            }

            if (shouldRequestThumb) {
                fetcher.fetch(Integer.valueOf(id), imageView, colWidth, rotate);
            }

            return imageView;
        }
    }


    private class ResizeImagesTask extends AsyncTask<Set<Entry<String, Integer>>, Void, ArrayList<String>> {
        private Exception asyncTaskError = null;

        @Override
        protected ArrayList<String> doInBackground(Set<Entry<String, Integer>>... fileSets) {
            Set<Entry<String, Integer>> fileNames = fileSets[0];
            ArrayList<String> al = new ArrayList<String>();
            try {
                Iterator<Entry<String, Integer>> i = fileNames.iterator();
                Bitmap bmp;
                Bitmap bmp2;
                Bitmap bmp3;
                while(i.hasNext()) {
                    Entry<String, Integer> imageInfo = i.next();
                    File file = new File(imageInfo.getKey());
                    String originalFilename = file.getAbsolutePath();
                    // int rotate = imageInfo.getValue().intValue();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                    // Get exif orientation
                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                    int width = options.outWidth;
                    int height = options.outHeight;

                    // Swith width and height if rotated 90 or 270 deg
                    if(orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270)
                    {
                        height = options.outWidth;
                        width = options.outHeight;
                    }

                    // Recosntruct rotation
                    int rotate = 0;
                    if(orientation == ExifInterface.ORIENTATION_ROTATE_90)
                    {
                        rotate = 90;
                    }
                    if(orientation == ExifInterface.ORIENTATION_ROTATE_180)
                    {
                        rotate = 180;
                    }
                    if(orientation == ExifInterface.ORIENTATION_ROTATE_270)
                    {
                        rotate = 270;
                    }

                    // Log.d("ZETBOOK", orientation + "-" + rotate);

                    float scale = calculateScale(width, height);
                    int finalWidth = (int)(width * scale);
                    int finalHeight = (int)(height * scale);

                    if (scale < 1) {
                        int inSampleSize = calculateInSampleSize(options, finalWidth, finalHeight);
                        options = new BitmapFactory.Options();
                        options.inSampleSize = inSampleSize;
                        try {
                            bmp = this.tryToGetBitmap(file, options, rotate, true);
                        } catch (OutOfMemoryError e) {
                            options.inSampleSize = calculateNextSampleSize(options.inSampleSize);
                            try {
                                bmp = this.tryToGetBitmap(file, options, rotate, false);
                            } catch (OutOfMemoryError e2) {
                                throw new IOException("Unable to load image into memory.");
                            }
                        }
                    } else {
                        try {
                            bmp = this.tryToGetBitmap(file, null, rotate, false);
                        } catch(OutOfMemoryError e) {
                            options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            try {
                                bmp = this.tryToGetBitmap(file, options, rotate, false);
                            } catch(OutOfMemoryError e2) {
                                options = new BitmapFactory.Options();
                                options.inSampleSize = 4;
                                try {
                                    bmp = this.tryToGetBitmap(file, options, rotate, false);
                                } catch (OutOfMemoryError e3) {
                                    throw new IOException("Unable to load image into memory.");
                                }
                            }
                        }
                    }
                    file = this.storeImage(bmp, file.getName());
                    bmp = null;
                    System.gc();

                    // Create one more file - thumbnail here
                    File file2 = new File(imageInfo.getKey());
                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inSampleSize = 1;
                    options2.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(file2.getAbsolutePath(), options2);
                    int width2 = options2.outWidth;
                    int height2 = options2.outHeight;
                    float scale2 = calculateScale(width2, height2);
                    if (scale2 < 1) {
                        int finalWidth2 = 300;//(int)(width2 * scale2);
                        int finalHeight2 = 300;//(int)(height2 * scale2);
                        int inSampleSize = calculateInSampleSize(options2, finalWidth2, finalHeight2);
                        options2 = new BitmapFactory.Options();
                        options2.inSampleSize = inSampleSize;
                        try {
                            bmp2 = this.tryToGetBitmap(file2, options2, rotate, true);
                        } catch (OutOfMemoryError e) {
                            options2.inSampleSize = calculateNextSampleSize(options2.inSampleSize);
                            try {
                                bmp2 = this.tryToGetBitmap(file2, options2, rotate, false);
                            } catch (OutOfMemoryError e2) {
                                throw new IOException("Unable to load image into memory.");
                            }
                        }
                    } else {
                        try {
                            bmp2 = this.tryToGetBitmap(file2, null, rotate, false);
                        } catch(OutOfMemoryError e) {
                            options2 = new BitmapFactory.Options();
                            options2.inSampleSize = 2;
                            try {
                                bmp2 = this.tryToGetBitmap(file2, options2, rotate, false);
                            } catch(OutOfMemoryError e2) {
                                options2 = new BitmapFactory.Options();
                                options2.inSampleSize = 4;
                                try {
                                    bmp2 = this.tryToGetBitmap(file2, options2, rotate, false);
                                } catch (OutOfMemoryError e3) {
                                    throw new IOException("Unable to load image into memory.");
                                }
                            }
                        }
                    }
                    file2 = this.storeImage(bmp2, file2.getName());
                    bmp2 = null;
                    System.gc();

                    // Create one more file - thumbnail here
                    File file3 = new File(imageInfo.getKey());
                    BitmapFactory.Options options3 = new BitmapFactory.Options();
                    options3.inSampleSize = 1;
                    options3.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(file3.getAbsolutePath(), options3);
                    int width3 = options3.outWidth;
                    int height3 = options3.outHeight;
                    float scale3 = calculateScale(width3, height3);
                    if (scale3 < 1) {
                        int finalWidth3 = 30;//(int)(width3 * scale3);
                        int finalHeight3 = 30;//(int)(height3 * scale3);
                        int inSampleSize = calculateInSampleSize(options3, finalWidth3, finalHeight3);
                        options3 = new BitmapFactory.Options();
                        options3.inSampleSize = inSampleSize;
                        try {
                            bmp3 = this.tryToGetBitmap(file3, options3, rotate, true);
                        } catch (OutOfMemoryError e) {
                            options3.inSampleSize = calculateNextSampleSize(options3.inSampleSize);
                            try {
                                bmp3 = this.tryToGetBitmap(file3, options3, rotate, false);
                            } catch (OutOfMemoryError e2) {
                                throw new IOException("Unable to load image into memory.");
                            }
                        }
                    } else {
                        try {
                            bmp3 = this.tryToGetBitmap(file3, null, rotate, false);
                        } catch(OutOfMemoryError e) {
                            options3 = new BitmapFactory.Options();
                            options3.inSampleSize = 3;
                            try {
                                bmp3 = this.tryToGetBitmap(file3, options3, rotate, false);
                            } catch(OutOfMemoryError e2) {
                                options3 = new BitmapFactory.Options();
                                options3.inSampleSize = 4;
                                try {
                                    bmp3 = this.tryToGetBitmap(file3, options3, rotate, false);
                                } catch (OutOfMemoryError e3) {
                                    throw new IOException("Unable to load image into memory.");
                                }
                            }
                        }
                    }
                    file3 = this.storeImage(bmp3, file3.getName());
                    bmp3 = null;
                    System.gc();

                    // Return all files together
                    al.add(Uri.fromFile(file).toString()+"|"+width+"x"+height+"-"+finalWidth+"x"+finalHeight+"-"+orientation+";"+Uri.fromFile(file2).toString()+";"+Uri.fromFile(file3).toString()+";"+originalFilename);
                }
                return al;
            } catch(IOException e) {
                try {
                    asyncTaskError = e;
                    for (int i = 0; i < al.size(); i++) {
                        URI uri = new URI(al.get(i));
                        File file = new File(uri);
                        file.delete();
                    }
                } catch(Exception exception) {
                    // the finally does what we want to do
                } finally {
                    return new ArrayList<String>();
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> al) {
            Intent data = new Intent();

            if (asyncTaskError != null) {
                Bundle res = new Bundle();
                res.putString("ERRORMESSAGE", asyncTaskError.getMessage());
                res.putString("REMOVEDFILENAMES", fileNamesRemoved.toString());
                data.putExtras(res);
                setResult(RESULT_CANCELED, data);
            } else if (al.size() > 0) {
                Bundle res = new Bundle();
                res.putStringArrayList("MULTIPLEFILENAMES", al);
                if (imagecursor != null) {
                    res.putInt("TOTALFILES", imagecursor.getCount());
                }
                res.putString("REMOVEDFILENAMES", fileNamesRemoved.toString());
                data.putExtras(res);
                setResult(RESULT_OK, data);
            } else {
                Bundle res = new Bundle();
                res.putString("REMOVEDFILENAMES", fileNamesRemoved.toString());
                data.putExtras(res);
                setResult(RESULT_CANCELED, data);
            }

            progress.dismiss();
            finish();
        }

        private Bitmap tryToGetBitmap(File file, BitmapFactory.Options options, int rotate, boolean shouldScale) throws IOException, OutOfMemoryError {
            Bitmap bmp;
            if (options == null) {
                bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            } else {
                bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            }
            if (bmp == null) {
                throw new IOException("The image file could not be opened.");
            }
            if (options != null && shouldScale) {
                float scale = calculateScale(options.outWidth, options.outHeight);
                bmp = this.getResizedBitmap(bmp, scale);
            }
            if (rotate != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            }
            return bmp;
        }

        /*
        * The following functions are originally from
        * https://github.com/raananw/PhoneGap-Image-Resizer
        *
        * They have been modified by Andrew Stephan for Sync OnSet
        *
        * The software is open source, MIT Licensed.
        * Copyright (C) 2012, webXells GmbH All Rights Reserved.
        */
        private File storeImage(Bitmap bmp, String fileName) throws IOException {
            int index = fileName.lastIndexOf('.');
            String name = fileName.substring(0, index);
            String ext = fileName.substring(index);
            File file = File.createTempFile(name, ext);
            OutputStream outStream = new FileOutputStream(file);
            if (ext.compareToIgnoreCase(".png") == 0) {
                bmp.compress(Bitmap.CompressFormat.PNG, quality, outStream);
            } else {
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
            }
            outStream.flush();
            outStream.close();
            return file;
        }

        private Bitmap getResizedBitmap(Bitmap bm, float factor) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(factor, factor);
            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
            return resizedBitmap;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private int calculateNextSampleSize(int sampleSize) {
        double logBaseTwo = (int)(Math.log(sampleSize) / Math.log(2));
        return (int)Math.pow(logBaseTwo + 1, 2);
    }

    private float calculateScale(int width, int height) {
        float widthScale = 1.0f;
        float heightScale = 1.0f;
        float scale = 1.0f;
        if (desiredWidth > 0 || desiredHeight > 0) {
            if (desiredHeight == 0 && desiredWidth < width) {
                scale = (float)desiredWidth/width;
            } else if (desiredWidth == 0 && desiredHeight < height) {
                scale = (float)desiredHeight/height;
            } else {
                if (desiredWidth > 0 && desiredWidth < width) {
                    widthScale = (float)desiredWidth/width;
                }
                if (desiredHeight > 0 && desiredHeight < height) {
                    heightScale = (float)desiredHeight/height;
                }
                if (widthScale < heightScale) {
                    scale = widthScale;
                } else {
                    scale = heightScale;
                }
            }
        }

        return scale;
    }
}
