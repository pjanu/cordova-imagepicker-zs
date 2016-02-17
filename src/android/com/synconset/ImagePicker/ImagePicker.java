/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";

	private CallbackContext callbackContext;
	private JSONObject params;

	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
			Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
			int max = 20;
			int add = 0;
			int desiredWidth = 0;
			int desiredHeight = 0;
			int quality = 100;
			String selectedFileNames = "";
			int selectedColor = 0;
			String orientation = "any";
			String customization = "";
			if (this.params.has("maximumImagesCount")) {
				max = this.params.getInt("maximumImagesCount");
			}
			if (this.params.has("addImagesCount")) {
				add = this.params.getInt("addImagesCount");
			}
			if (this.params.has("width")) {
				desiredWidth = this.params.getInt("width");
			}
			if (this.params.has("height")) {
				desiredWidth = this.params.getInt("height");
			}
			if (this.params.has("quality")) {
				quality = this.params.getInt("quality");
			}
			if (this.params.has("selected")) {
				selectedFileNames = this.params.getString("selected");
			}
			if (this.params.has("selectedColor")) {
				selectedColor = this.params.getInt("selectedColor");
			}
			if (this.params.has("orientation")) {
				orientation = this.params.getString("orientation");
			}
			if (this.params.has("customization")) {
				customization = this.params.getString("customization");
			}
			intent.putExtra("SELECTED_COLOR", selectedColor);
			intent.putExtra("MAX_IMAGES", max);
			intent.putExtra("ADD_IMAGES", add);
			intent.putExtra("WIDTH", desiredWidth);
			intent.putExtra("HEIGHT", desiredHeight);
			intent.putExtra("QUALITY", quality);
			intent.putExtra("SELECTED_KEY", selectedFileNames);
			intent.putExtra("VIEW_ORIENTATION", orientation);
			intent.putExtra("CUSTOMIZATION", customization);
			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
			}
		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			String type = data.getStringExtra("TYPE");
			JSONArray res = null;
			if(type != "CLEAR")
			{
				ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
				res = new JSONArray(fileNames);
			}
			else {
				res = new JSONArray();
			}
			String fileNamesRemoved = data.getStringExtra("REMOVEDFILENAMES");
			JSONObject gres = new JSONObject();
			try {
				gres.put("state", "ok");
				gres.put("removedFiles", fileNamesRemoved);
				gres.put("addedFiles", res);
			} catch (JSONException e) {
			    e.printStackTrace();
			}
			this.callbackContext.success(gres);
		}
		else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		}
		else if (resultCode == Activity.RESULT_CANCELED) {
			JSONObject gres = new JSONObject();
			try {
				gres.put("state", "cancelled");
			} catch (JSONException e) {
			    e.printStackTrace();
			}
			this.callbackContext.success(gres);
		}
		else {
			this.callbackContext.error("No images selected");
		}
	}
}