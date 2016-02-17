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

//import javax.json.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.WindowManager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.OutOfMemoryError;
import java.net.URI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

// For landscape orientation fix (imports ActivityInfo)
import android.content.pm.ActivityInfo;
// For exif orientation tag
import android.media.ExifInterface;
import android.os.SystemClock;

// import android.Locale;
import java.util.Locale;

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
    public static final String SELECTED_COLOR_KEY = "SELECTED_COLOR";
    public static final String VIEW_ORIENTATION = "VIEW_ORIENTATION";
    public static final String SIMPLE_HEADER_KEY = "SIMPLE_HEADER";
    public static final String COUNTOK_EVAL_KEY = "COUNTOK_EVAL";

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
    private String viewOrientation = "any";

    private boolean simpleHeader = false;
    private boolean countOkEval = false;

    private String selectedLang = "en";

    private String strChosen = "Picked";
    private String strOf = "of";
    private String strProcessing = "Processing photos";
    private String strProcessingNote = "Processing may take a while";
    private String strChooseMax = "Max";
    private String strChooseMaxPhotos = "photos";
    private String strChooseNoteMax = "You can choose max";
    private String strChooseNoteMaxPhotos = "photos";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        selectedLang = Locale.getDefault().getLanguage();
        if(selectedLang.equals("cs"))
        {
            strChosen = "Vybráno";
            strOf = "z";
            strProcessing = "Zpracovávám fotografie";
            strProcessingNote = "Zpracování může chvilku trvat";
            strChooseMax = "Maximum";
            strChooseMaxPhotos = "fotek";
            strChooseNoteMax = "Můžete vybrat maximálně";
            strChooseNoteMaxPhotos = "fotek";
        }
        else if(selectedLang.equals("sk"))
        {
            strChosen = "Vybrané";
            strOf = "z";
            strProcessing = "Spracovávam fotografie";
            strProcessingNote = "Spracovanie môže chvíľku trvať";
            strChooseMax = "Maximum";
            strChooseMaxPhotos = "fotiek";
            strChooseNoteMax = "Môžete vybrať maximálne";
            strChooseNoteMaxPhotos = "fotiek";
        }

        // Log.d("ZETBOOK", "onCreate");
        super.onCreate(savedInstanceState);
        fakeR = new FakeR(this);

        // Fix orientation - call this before setContentView
        viewOrientation = getIntent().getStringExtra(VIEW_ORIENTATION);
        if(viewOrientation.equals("landscape"))
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else if(viewOrientation.equals("portrait"))
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(fakeR.getId("layout", "multiselectorgrid"));
        fileNames.clear();
        fileNamesRemoved.clear();

        selectedColor = getIntent().getIntExtra(SELECTED_COLOR_KEY, 0xffc70f3f);
        maxImages = getIntent().getIntExtra(MAX_IMAGES_KEY, NOLIMIT);
        desiredWidth = getIntent().getIntExtra(WIDTH_KEY, 0);
        desiredHeight = getIntent().getIntExtra(HEIGHT_KEY, 0);
        quality = getIntent().getIntExtra(QUALITY_KEY, 0);
        alreadySelectedFileNames = getIntent().getStringExtra(SELECTED_KEY);
        addImagesCount = getIntent().getIntExtra(ADD_IMAGES, 0);
        maxImageCount = maxImages;
        countOkEval = getIntent().getBooleanExtra(COUNTOK_EVAL_KEY, false);
        simpleHeader = getIntent().getBooleanExtra(SIMPLE_HEADER_KEY, false);

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
                maxImages--;
            }
        }

        updateAcceptButton();
        int count = addImagesCount + fileNames.size();
        int maxCount = addImagesCount + maxImageCount;
        updateHeaderText(simpleHeader, countOkEval, strChosen, count, strOf, maxCount);
        progress = new ProgressDialog(this);
        progress.setTitle(strProcessing);
        progress.setMessage(strProcessingNote);
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
            builder.setTitle(strChooseMax + " " + maxCount + " " + strChooseMaxPhotos);
            builder.setMessage(strChooseNoteMax + " " + maxCount + " " + strChooseNoteMaxPhotos + ".");
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
        updateHeaderText(simpleHeader, countOkEval, strChosen, count, strOf, maxCount);
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

    private void updateHeaderText(boolean simpleHeader, boolean countOkEval, String strChosen, int count, String strOf, int maxCount) {
        String text;
        TextView tv =((TextView) getActionBar().getCustomView().findViewById(fakeR.getId("id", "actionbar_status_textview")));
        if(simpleHeader){
            text = "" + count;
        }
        else {
            text = strChosen + " " + count + " " + strOf + " " + maxCount + "";
        }
        if(countOkEval){
            if(count >= 12 && count <= 60 && count % 4 == 0){
                tv.setBackgroundColor(0xe600cc33);
            }
            else {
                tv.setBackgroundColor(0x00ffffff);
            }
        }
        tv.setText(text);
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

            Log.d("ZETBOOK", "\n\n\n" + "doInBackground" + "\n");

            Log.d("ZETBOOK_MAIN", "PREVENT PHONE FROM SLEEPING");
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            Set<Entry<String, Integer>> fileNames = fileSets[0];
            ArrayList<String> al = new ArrayList<String>();
            ArrayList<String> alCleanup = new ArrayList<String>();
            try {
                Iterator<Entry<String, Integer>> i = fileNames.iterator();
                int imagesCount = fileNames.size();
                int imageSequence = 0;


                while(i.hasNext()) {

                    System.gc();
                    Bitmap bmp = null;

                    imageSequence++;

                    Entry<String, Integer> imageInfo = i.next();
                    File originalFile = new File(imageInfo.getKey());
                    String originalFileName = originalFile.getName(),
                           originalFilePath = originalFile.getAbsolutePath();

                    File tmpFile;


                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(originalFilePath, options);

                    // Get exif orientation and metadata
                    ExifInterface exif = new ExifInterface(originalFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    String origExifLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                           origExifLon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                           origExifDate = exif.getAttribute(ExifInterface.TAG_DATETIME);


                    int originalPhotoWidth = options.outWidth;
                    int originalPhotoHeight = options.outHeight;

                    // Swith width and height if rotated 90 or 270 deg
                    if(/*originalPhotoHeight > originalPhotoWidth && */(orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270))
                    {
                        originalPhotoHeight = options.outWidth;
                        originalPhotoWidth = options.outHeight;
                    }

                    // Recosntruct rotation
//                    boolean rotationSuccess = false;
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

                    float scale = calculateScale(originalPhotoWidth, originalPhotoHeight);
                    int finalWidth = (int)(originalPhotoWidth * scale);
                    int finalHeight = (int)(originalPhotoHeight * scale);

                    String largePhotoName = null;
                    String thumbnailName = null;
                    String miniPhotoName = null;
                    String originalPhotoName = Uri.fromFile(originalFile).toString();

                    Log.d("ZETBOOK", "parsing file " + imageSequence + " of " + imagesCount + " (" + originalFile + ")");

                    /**************************
                     * large photo
                     ************************/
                    Log.d("ZETBOOK", "- large photo (original: " + originalPhotoWidth + "x" + originalPhotoHeight + ", final: " + finalWidth + "x" + finalHeight + ")");
                    Log.d("ZETBOOK_MAIN", "photo " + imageSequence + " of " + imagesCount + " (original: " + options.outWidth + "x" + options.outHeight +  " rotate: " + rotate + "° modified:" + originalPhotoWidth + "x" + originalPhotoHeight + ", final: " + finalWidth + "x" + finalHeight + ")");


                    int inSampleSize = 1;
                    int maxSampleSize = calculateInSampleSize(originalPhotoWidth, originalPhotoHeight, finalWidth, finalHeight);

                    options = new BitmapFactory.Options();
                    options.inSampleSize = inSampleSize; // 1

                    try {
                        // create bitmap from file with bes sampling
                        while((inSampleSize <= maxSampleSize) && bmp == null) {

                            try {
                                bmp = tryToGetBitmap(originalFile, options);
                            }
                            catch (OutOfMemoryError e) {
                                // subsample
                                inSampleSize *= 2;

                                options = new BitmapFactory.Options();
                                options.inSampleSize = inSampleSize;

                                bmp = null;
                                System.gc();
                            }
                        }

                        if(bmp == null) {
                            Log.d("ZETBOOK", "** cannot create bitmap with any allowable sample size (max = " + maxSampleSize + ")");
                            throw new OutOfMemoryError("** cannot create bitmap anyway (out of memmory)");
                        }

                        // scale if needed
                        bmp = tryToScaleBitmapToPhoto(bmp);

                        // rotate if needed
//                        bmp = tryToRotateBitmap(bmp, rotate);
//                        rotationSuccess = true;

                        // store file
                        tmpFile = storeImage(bmp, originalFileName);

                        // ??
                        alCleanup.add(Uri.fromFile(tmpFile).toString());
                    }
                    catch (OutOfMemoryError e) {
                        Log.d("ZETBOOK", "** out of memory (trying processing bitmap)");

                        // store as original file
                        tmpFile = storeImage(originalFile);

                        // reset bmp
                        bmp = null;
                    }

                    // set same exif orientaton ans source
//                    if(!rotationSuccess) {
                        tmpFile = setExifOrientation(tmpFile, orientation);
//                    }

                    largePhotoName = Uri.fromFile(tmpFile).toString();



                    /**************************
                     * thumbnail 300x300
                     ************************/
                    Log.d("ZETBOOK", "- thumbnail photo (300x300)");

                    //bmp.recycle();
                    //bmp = null;
                    boolean newBitmap = (bmp == null);

                    try {
                        // create bitmap from file
                        if(newBitmap) {
                            options = new BitmapFactory.Options();
                            options.inSampleSize = calculateInSampleSize(originalPhotoWidth, originalPhotoHeight, 300, 300);

                            // get bitmap
                            bmp = tryToGetBitmap(originalFile, options);

                            // scale to 300x300
                            bmp = tryToScaleBitmapToThumbnail(bmp);

                            // rotate if needed
//                            bmp = tryToRotateBitmap(bmp, rotate);

                        }
                        else { // bitmap already exists

                            Log.d("ZETBOOK", "** bitmap already exists");

                            // only scale to 300x300
                            bmp = tryToScaleBitmapToThumbnail(bmp);
                        }

                        // store file
                        tmpFile = storeImage(bmp, originalFileName);

                        // ??
                        alCleanup.add(Uri.fromFile(tmpFile).toString());
                    }
                    catch (OutOfMemoryError e) {
                        Log.d("ZETBOOK", "** out of memory");

                        // store as original file
                        tmpFile = storeImage(originalFile);

                        // reset bmp
                        bmp = null;
                    }

                    // set same exif orientaton ans source
//                    if(!rotationSuccess) {
                        tmpFile = setExifOrientation(tmpFile, orientation);
//                    }

                    thumbnailName = Uri.fromFile(tmpFile).toString();




                    /**************************
                     * thumbnail 30x30
                     ************************/
                    Log.d("ZETBOOK", "- mini photo (30x30)");

                    //bmp.recycle();
                    //bmp = null;
                    newBitmap = (bmp == null);

                    try {
                        // create bitmap from file
                        if(newBitmap) {
                            options = new BitmapFactory.Options();
                            options.inSampleSize = calculateInSampleSize(originalPhotoWidth, originalPhotoHeight, 30, 30);

                            // get bitmap
                            bmp = tryToGetBitmap(originalFile, options);

                            // scale to 300x300
                            bmp = tryToScaleBitmapToMini(bmp);

                            // rotate if needed
//                            bmp = tryToRotateBitmap(bmp, rotate);
                        }
                        else { // bitmap already exists

                            Log.d("ZETBOOK", "** bitmap already exists");

                            // only scale to 30x30
                            bmp = tryToScaleBitmapToMini(bmp);
                        }

                        // store file
                        tmpFile = storeImage(bmp, originalFileName);

                        // ??
                        alCleanup.add(Uri.fromFile(tmpFile).toString());
                    }
                    catch (OutOfMemoryError e) {
                        Log.d("ZETBOOK", "** out of memory");

                        // store as original file
                        tmpFile = storeImage(originalFile);

                        // reset bmp
                        bmp = null;
                    }

                    // set same exif orientaton ans source
//                    if(!rotationSuccess) {
                        tmpFile = setExifOrientation(tmpFile, orientation);
//                    }

                    miniPhotoName = Uri.fromFile(tmpFile).toString();


//                    // rotation success
//                    if(rotationSuccess) {
//                        orientation = ExifInterface.ORIENTATION_NORMAL;
//                    }
//                    // rotation failed
//                    else {
                        // Change proportions since orientation is +-90deg what means the browser shows photo rotated and w and h must be switched
                        if(orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {

                            int tmpSize = originalPhotoWidth;
                            originalPhotoWidth = originalPhotoHeight;
                            originalPhotoHeight = tmpSize;

                            tmpSize = finalWidth;
                            finalWidth = finalHeight;
                            finalHeight = tmpSize;
                        }
//                    }

                    // returning json object serialized
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj.put("originalFilePath", originalFilePath);
                        jsonObj.put("originalPhotoName", originalPhotoName);
                        jsonObj.put("largePhotoName", largePhotoName);
                        jsonObj.put("thumbnailName", thumbnailName);
                        jsonObj.put("miniPhotoName", miniPhotoName);
                        jsonObj.put("originalPhotoWidth", originalPhotoWidth);
                        jsonObj.put("originalPhotoHeight", originalPhotoHeight);
                        jsonObj.put("finalWidth", finalWidth);
                        jsonObj.put("finalHeight", finalHeight);
                        jsonObj.put("originalRotation", rotate);
                        jsonObj.put("origExifDate", origExifDate);
                        jsonObj.put("origExifLon", origExifLon);
                        jsonObj.put("origExifLat", origExifLat);

                        Log.d("ZETBOOK", jsonObj.toString());
                    }
                    catch (JSONException e) {
                        Log.d("ZETBOOK", "** json serializing problem: " + e + " (file " + originalFileName + ")");
                    }

                    al.add(jsonObj.toString());


                    // old return string
                    //al.add(largePhotoName + "|" + originalPhotoWidth + "x" + originalPhotoHeight + "-" + finalWidth + "x" + finalHeight + "-" + orientation + ";" + thumbnailName + ";" + miniPhotoName + ";" + originalFilePath);

                    // free memory
                    bmp.recycle();
                    bmp = null;
                }



                return al;

            }
            catch(IOException e) {
                try {
                    asyncTaskError = e;
                    for (int i = 0; i < alCleanup.size(); i++) {
                        URI uri = new URI(alCleanup.get(i));
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


        private Bitmap tryToGetBitmap(File file, BitmapFactory.Options options) throws IOException, OutOfMemoryError {

            Log.d("ZETBOOK", "** trying to create bitmap (sample = " + options.inSampleSize + ")");

            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            if (bmp == null) {
                throw new IOException("The image file could not be opened.");
            }

            Log.d("ZETBOOK", "** bitmap created (sample = " + options.inSampleSize + ")");
            return bmp;
        }

        private File setExifOrientation(File file, int orientation) {

            Log.d("ZETBOOK", "** setting orientation to " + orientation);

            try {
                ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + orientation);
                exif.saveAttributes();
            } catch(Exception exception) {
                Log.d("ZETBOOK", "** setting orientation failed");
            }

            return file;
        }

        private Bitmap tryToRotateBitmap(Bitmap bmp, int rotate) {

            if(rotate != 0 && rotate != 90 && rotate != 180 && rotate != 270) {
                throw new IllegalArgumentException("rotate must be one of 0, 90, 180, 270 (not " + rotate + ")");
            }

            if (rotate != 0) {
                Log.d("ZETBOOK", "** rotating by " + rotate + " degrees");
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            }

            return bmp;
        }

        private Bitmap tryToScaleBitmapToPhoto(Bitmap bmp) {

            int width = bmp.getWidth(),
                height = bmp.getHeight(),
                maxSize;
            float scale = calculateScale(width, height);

            if(width > height) {
                maxSize = width;
            }
            else {
                maxSize = height;
            }

            return tryToScaleBitmap(bmp, (int)(maxSize * scale));
        }

        private Bitmap tryToScaleBitmapToThumbnail(Bitmap bmp) {
            return tryToScaleBitmap(bmp, 300);
        }

        private Bitmap tryToScaleBitmapToMini(Bitmap bmp) {
            return tryToScaleBitmap(bmp, 30);
        }

        private Bitmap tryToScaleBitmap(Bitmap bmp, int maxSize) {

            float scale;
            int width = bmp.getWidth(),
                height = bmp.getHeight(),
                newWidth,
                newHeight;

            if(width > height) {
                scale = (float)width / height;
                newWidth = (int)(maxSize * scale);
                newHeight = maxSize;
            }
            else {
                scale = (float)height / width;
                newWidth = maxSize;
                newHeight = (int)(maxSize * scale);
            }
            return tryToScaleBitmap(bmp, newWidth, newHeight);
        }

        private Bitmap tryToScaleBitmap(Bitmap bmp, int newWidth, int newHeight) {

            Log.d("ZETBOOK", "** scaling bitmap to size " + newWidth + "x" + newHeight);
            return Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);
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

            Log.d("ZETBOOK", "** saving file (from bitmap)");

            int index = fileName.lastIndexOf('.');
            // Add file name prefix here since Android crashes when filename is number
            String name = "tmp-" + fileName.substring(0, index);
            String ext = fileName.substring(index);
            File file = File.createTempFile(name, ext);
            Log.d("ZETBOOK", "** creatimg temp file: " +  file.getAbsolutePath());
            OutputStream outStream = new FileOutputStream(file);

            if (ext.compareToIgnoreCase(".png") == 0) {
                // Log.d("ZBOOK", "storeImage png" + fileName);
                bmp.compress(Bitmap.CompressFormat.PNG, quality, outStream);
            }
            else if(ext.compareToIgnoreCase(".jpg") == 0 || ext.compareToIgnoreCase(".jpeg") == 0) {
                // Log.d("ZBOOK", "storeImage jpg" + fileName);
                bmp.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
            }
            else {
                throw new IOException("Unsupported extension: " + ext);
            }

            outStream.flush();
            outStream.close();

            return file;
        }

        private File storeImage(File originalFile) throws IOException {

            Log.d("ZETBOOK", "** saving file (as copy of original)");

            String fileName = originalFile.getName();
            InputStream inStream = new FileInputStream(originalFile);


            int index = fileName.lastIndexOf('.');
            // Add file name prefix here since Android crashes when filename is number
            String name =  "tmp-" + fileName.substring(0, index);
            String ext = fileName.substring(index);
            File file = File.createTempFile(name, ext);

            OutputStream outStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096]; // Adjust if you want
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1)
            {
                outStream.write(buffer, 0, bytesRead);
            }


            outStream.flush();
            outStream.close();
            inStream.close();

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
        return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);
    }

    private int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / (inSampleSize)) > reqHeight && (halfWidth / (inSampleSize)) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private int calculateNextSampleSize(int sampleSize) {
        double logBaseTwo = (int)(Math.log(sampleSize) / Math.log(2));
        return (int)Math.pow(logBaseTwo + 1, 2);
    }


    private float calculateScale(int width, int height, int desiredWidth, int desiredHeight ) {
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

    private float calculateScale(int width, int height) {
        return calculateScale(width, height, this.desiredWidth, this.desiredHeight);
    }
}
