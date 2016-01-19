package liza.com.rateinstructor;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class MainActivity extends ListActivity implements InstructorDialogFragment.InstructorDialogListener{

    private static String TAG_DEBUG = MainActivity.class.getSimpleName();

    private String urlJsonArray1 = "http://bismarck.sdsu.edu/rateme/list";
    private ProgressDialog pDialog;
    HttpClient httpclient;
    DBAdapter instructorDb;
    // ArrayList for ListView
    ArrayList<Instructor> instructorList;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if(actionBar!=null)actionBar.setSubtitle(R.string.subTitle_main);

        instructorDb = new DBAdapter(this);//instance of instructor database

        //---setting up list view essentials--
        instructorList = new ArrayList<>();
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setSelector(R.color.accent_material_dark);

        // ------Showing progress dialog-------
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        //-----------Check network connection-----------//
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {/*network connected*/

            //-----------Loading request from cache/new network call--------------//
            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry cachedData = cache.get(urlJsonArray1);
            if (cachedData != null) {//data present in cache
                try {

                    JSONArray jA_Again = new JSONArray(new String(cachedData.data, "UTF-8"));
                    //--Extract JSON String and display--//
                    extractJson_DisplayList(jA_Again);

                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {/* Cached response doesn't exists. Make network call
                     making urlJsonArray1 request*/
                makeJsonArrayRequest();
            }
        } else {/*network not connected*/

            /*-------Alert user to connect to network for full functionality of app----------*/
            /* Create an instance of the dialog fragment and show message for reading from db*/
            /*Proceed--->read from dataBase ; Quit--->app quits completely.*/

            InstructorDialogFragment alertDialog = InstructorDialogFragment.newInstance("3");
            alertDialog.show(getFragmentManager(), "DB_ALERT");

        }
    }

    /**
     * Method to make json array request where jA_Response starts with [
     * */
    private void makeJsonArrayRequest() {

        showP_Dialog();

        JsonArrayRequest req = new JsonArrayRequest(urlJsonArray1,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jA_Response) {

                        //--Extract JSON String and display--//
                        extractJson_DisplayList(jA_Response);

                        hideP_Dialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG_DEBUG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                hideP_Dialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req);
    }

    /**
     * Method to extract JSON Strings, store to arrayList and display using ListAdapter
     * */
    private void extractJson_DisplayList(JSONArray jA_Data) {
        if(jA_Data != null) {
            try {
                instructorDb.open();
				
                //---- loop through each json object------
                for (int i = 0; i < jA_Data.length(); i++) {
                    JSONObject instructorOb = jA_Data.getJSONObject(i);
                    String firstName = instructorOb.getString(Instructor.KEY_FIRST_NAME);
                    String lastName = instructorOb.getString(Instructor.KEY_LAST_NAME);
                    long id = Long.valueOf(instructorOb.getString(Instructor.KEY_INSTRUCTOR_ID));

                    Instructor instructor = new Instructor(id,firstName,lastName);

                    instructorList.add(instructor);

                    //---update full name in database if exists otherwise insert new---
                    if(!instructorDb.updateFullName(instructor)){
                        //---add full name to database---
                        instructorDb.insertProfile(instructor);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            instructorDb.close();

            ListAdapter adapter = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_list_item_1, instructorList);
            setListAdapter(adapter);
        }
    }
    /**
     * Method to get instructor's fullNames from database and display using SimpleCursorAdapter
     * */
    public void DisplayDbInstructorNameList(){

        //---------get all Instructor profiles(fullNames) from Database----------
        instructorDb.open();
        Cursor cursorDb = instructorDb.getAllProfiles();

        // ----------Alert user to connect network when using for first time-----------
        if(cursorDb.getCount()<=0){ /*This particular block will execute only once,ie.,
         when first time user starts app without connecting to network*/
            InstructorDialogFragment alertDialog = InstructorDialogFragment.newInstance("5");
            alertDialog.show(getFragmentManager(), "DB_ALERT");
        }

        //-----------Display Instructor fullName list----------
        String[] columns = new String[]{Instructor.KEY_FULL_NAME};
        int[] views = new int[]{android.R.id.text1};
        SimpleCursorAdapter cAdapter;
        cAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1, cursorDb,
                columns,views,CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setListAdapter(cAdapter);
        instructorDb.close();
    }

    private void showP_Dialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideP_Dialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onListItemClick( ListView parent, View v, int position, long id){
        /* Starting single detail activity passing intent with value of current ROW_ID
                                 of instructor pressed*/
        Intent intentToPass = new Intent(getApplicationContext(),DetailActivity.class);
        intentToPass.putExtra(Instructor.KEY_INSTRUCTOR_ID, Integer.toString(position+1));
        startActivity(intentToPass);
    }

    @Override
    public void onPause() {

        super.onPause();
        if(httpclient!=null)httpclient.getConnectionManager().shutdown();
    }

    @Override
    public void onDialogPositiveClick(String nothing1, String nothing2){
        DisplayDbInstructorNameList();
    }


    @Override
    public void onDialogNegativeClick() {    
        finish();
    }

}
