package liza.com.rateinstructor;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;


public class DetailActivity extends ListActivity implements InstructorDialogFragment.InstructorDialogListener{

    private static String TAG_DEBUG = DetailActivity.class.getSimpleName();
    private static final String KEY_RATING = "rating";
    private static final String KEY_COMMENT_TEXT = "text" ;
    private static final String KEY_COMMENT_DATE= "date";

    private String idPassed;//ID of instructor passed from mainActivity
    private ProgressDialog pDialog;//for showing progress while data load from network
    private String newComment;//comment to post for single instructor
    public int mPostUrlID;//1=rating url//2=comment url

    public int getPostUrlID() {
        return mPostUrlID;
    }

    public void setPostUrlID(int postUrlID) {
        mPostUrlID = postUrlID;
    }

    String urlJsonObject1;
    String urlJsonArray2;

    ArrayList<HashMap<String, String>> detailList;
    HashMap<String, String> details;

    DBAdapter instructorDb;
    HttpClient httpclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if(actionBar!=null)actionBar.setSubtitle(R.string.subTitle_details);

        //--------Deriving GET method Urls from main Activity--------------//
        idPassed = getIntent().getStringExtra(Instructor.KEY_INSTRUCTOR_ID);
        urlJsonObject1 = "http://bismarck.sdsu.edu/rateme/instructor/" + idPassed;
        urlJsonArray2 = "http://bismarck.sdsu.edu/rateme/comments/" + idPassed;

        // ------Showing progress dialog-------
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        //---setting up table view(list View) essentials--//
        detailList = new ArrayList<>();
        details = new HashMap<>();
        instructorDb = new DBAdapter(this);
        getInstructorDetails();

    }

    /**-- Method to load request from cache/new network call if
     * network connected otherwise read from Database-----*/

    public  void getInstructorDetails(){

        NetworkInfo networkInfo = checkNetworkConnection();

        if (networkInfo != null && networkInfo.isConnected()) {//Network connected

            //-----------Loading request from cache/new network call--------------//
            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry cachedData_JO = cache.get(urlJsonObject1);
            Cache.Entry cachedData_JA = cache.get(urlJsonArray2);

            if ((cachedData_JO != null)&& (cachedData_JA != null)) {//data present in cache
                try {

                    JSONObject jO_Again = new JSONObject(new String(cachedData_JO.data, "UTF-8"));
                    JSONArray jA_Again = new JSONArray(new String(cachedData_JA.data, "UTF-8"));

                    extractJsonO_SaveB_Details(jO_Again);
                    extractJsonA_ShowDetails(jA_Again);

                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {/* Cached response doesn't exists. Make network call
                     making urlJsonrequest */
                makeJsonRequest();
            }
        } else {
            readDbInstructorDetails();
        }
    }

    /**
     * Method to make urlJsonObject1 request where jO_Response starts with {
     * and to start urlJsonArray2 request inorder to make synchronised detail list
     * */

    private void makeJsonRequest() {

        //---- network call making urlJsonObject1 request------//
        showP_Dialog();
        JsonObjectRequest req_JO1 = new JsonObjectRequest(Request.Method.GET,
                urlJsonObject1,null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jO_Response) {

                //--Extract JSON String from JSON Object
                // and store List(Table view) of basic details (details
                // except comments)----//
                try {
                    extractJsonO_SaveB_Details(jO_Response);
                    //Make urlJsonArray2 request after first response
                    //inorder to get complete instructor detail list and display
                    makeJsonArrayRequest();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
        // Adding urlJsonObject1 request to request queue
        AppController.getInstance().addToRequestQueue(req_JO1);
    }

    /**
     * Method to extract JSON Strings from JSON Object to get basic details and store to arrayList
     * */
    private void extractJsonO_SaveB_Details(JSONObject jO_bDetails) throws JSONException {


        long id = Long.valueOf(jO_bDetails.getString(Instructor.KEY_INSTRUCTOR_ID));

        String firstName = jO_bDetails.getString(Instructor.KEY_FIRST_NAME);
        String lastName = jO_bDetails.getString(Instructor.KEY_LAST_NAME);
        String office = jO_bDetails.getString(Instructor.KEY_OFFICE);
        String email = jO_bDetails.getString(Instructor.KEY_EMAIL);
        String phone = jO_bDetails.getString(Instructor.KEY_PHONE);

        JSONObject ratingJS = jO_bDetails.getJSONObject(KEY_RATING);
        String avgRating = ratingJS.getString(Instructor.KEY_AVG_RATING);
        String totalRatings = ratingJS.getString(Instructor.KEY_TOTAL_RATING);

        //---------Creating Hash Map ---------//
        details.put(Instructor.KEY_FIRST_NAME,firstName);
        details.put(Instructor.KEY_LAST_NAME,lastName);
        details.put(Instructor.KEY_OFFICE,office);
        details.put(Instructor.KEY_PHONE,phone);
        details.put(Instructor.KEY_EMAIL,email);
        details.put(Instructor.KEY_AVG_RATING,avgRating);
        details.put(Instructor.KEY_TOTAL_RATING, totalRatings);

        //-----update basic details of instructor in database -----
        instructorDb.open();
        instructorDb.updateBasicDetails(new Instructor(id, firstName, lastName, office, phone, email,
                avgRating, totalRatings));
        instructorDb.close();
    }

    /**
     * Method to make urlJsonArray2 request where jA_Response starts with [
     * */
    private void makeJsonArrayRequest() {
        //------- network call making urlJsonArray2 request---------//
        JsonArrayRequest req_JA2 = new JsonArrayRequest(urlJsonArray2,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jA_Response) {

                        //--Extract JSON String from JSON
                        // Array and display Table of comments and basic details--//
                        try {
                            extractJsonA_ShowDetails(jA_Response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG_DEBUG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Adding urlJsonArray2 request to request queue
        AppController.getInstance().addToRequestQueue(req_JA2);
    }
    /**
     * Method to extract JSON Strings from JSON Array,to get comments and store to arrayList
     * and finally display all details using simple list adapter
     * */
    private void extractJsonA_ShowDetails(JSONArray jA_response) throws JSONException {

        String commentList = "";
        for (int i = 0; i < jA_response.length(); i++) {
            commentList += (i + 1) + ". " +"< " +
                    jA_response.getJSONObject(i).getString(KEY_COMMENT_DATE) + "> "+
                    jA_response.getJSONObject(i).getString(KEY_COMMENT_TEXT) + "\n";
        }
        //-----update comments of an instructor in database -----
        instructorDb.open();
        instructorDb.updateComments(new Instructor(Long.valueOf(idPassed), commentList));
        instructorDb.close();

        //----adding current instructor's comments to arrayList to display---
        details.put(Instructor.KEY_COMMENT_LIST,commentList);
        //--show all details stored in arrayList--//
        showAllDetails();
    }
    /**
     * Method to display all details of an instructor
     * */
    private void showAllDetails() {
        detailList.add(details);
        String[] from = new String[]{Instructor.KEY_FIRST_NAME,
                Instructor.KEY_LAST_NAME, Instructor.KEY_OFFICE,
                Instructor.KEY_PHONE,Instructor.KEY_EMAIL,
                Instructor.KEY_AVG_RATING,Instructor.KEY_TOTAL_RATING,Instructor.KEY_COMMENT_LIST};
        int[] to = new int[]{R.id.firstName, R.id.lastName, R.id.office,
                R.id.phone, R.id.email,R.id.average, R.id.totalRatings,R.id.comments_list};
        ListAdapter adapter = new SimpleAdapter(
                DetailActivity.this, detailList,
                R.layout.activity_detail, from, to);
        setListAdapter(adapter);
    }

    /**
     * Method to read instructor details from Database and display*
     * */
    private void readDbInstructorDetails() {

        //---------get single instructor details from Database----------
        instructorDb.open();
        long rowId = Long.valueOf(idPassed);
        Cursor c = instructorDb.getProfile(rowId);

        // ----------Alert user that the details are not updated; to see connect network-----------
        //----------Checking valid first name is present;if not finish current activity------------
        if(c.getString(2)== null){ //checking column=2(first name) is null;if first name is null
            //assume details are not updated yet
            InstructorDialogFragment alertDialog = InstructorDialogFragment.newInstance("4");
            alertDialog.show(getFragmentManager(), "DB_NO_UPDATE");
        }else {

            //-----------Display Instructor fullName list from database----------
            String[] columns = new String[]{Instructor.KEY_FIRST_NAME, Instructor.KEY_LAST_NAME,
                    Instructor.KEY_OFFICE, Instructor.KEY_PHONE,
                    Instructor.KEY_EMAIL, Instructor.KEY_AVG_RATING,
                    Instructor.KEY_TOTAL_RATING, Instructor.KEY_COMMENT_LIST};
            int[] views = new int[]{R.id.firstName, R.id.lastName, R.id.office, R.id.phone, R.id.email,
                    R.id.average, R.id.totalRatings, R.id.comments_list};
            SimpleCursorAdapter adapter;
            adapter = new SimpleCursorAdapter(this, R.layout.activity_detail, c, columns,
                    views, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            setListAdapter(adapter);
            instructorDb.close();
        }
    }


    /*------------AsyncTask to do POST Request in background---------------*/
    private class HttpClientDetailTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            // Showing progress dialog
            pDialog = new ProgressDialog(DetailActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(String... urls) {

            //---------post comment/rate-----------
            HttpPost postMethod = new HttpPost(urls[0]);
            if(getPostUrlID()==1) {//Rating Url
                try {
                    httpclient.execute(postMethod);

                } catch (Throwable t) {
                    Log.i(TAG_DEBUG, t.toString());
                }
            }else if(getPostUrlID()==2){//Comment Url
                StringEntity postComment;
                try {
                    postComment = new StringEntity(newComment, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    Log.i(TAG_DEBUG, e.toString());
                    return null;
                }
                postMethod.setHeader("Content-Type", "application/json;charset=UTF-8");
                postMethod.setEntity(postComment);
                try {
                    httpclient.execute(postMethod);
                } catch (Throwable t) {
                    Log.i(TAG_DEBUG, t.toString());
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(Void nothing) {

            /*reloading after posting ratings/comments to get
               updated display of instructor details  */
            detailList = new ArrayList<>();
            details = new HashMap<>();
            makeJsonRequest();
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        if(httpclient!=null)httpclient.getConnectionManager().shutdown();
    }

    /**
     * Execute async task for post ratings/comments if network connected or
     * read from database *
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback
     **/
    @Override
    public void onDialogPositiveClick(String dialogMode, String data){

        HttpClientDetailTask postTask;
        NetworkInfo networkInfo = checkNetworkConnection();
        if (networkInfo != null && networkInfo.isConnected()) {//network connected

            if(!data.isEmpty()){//valid data to post
                httpclient = new DefaultHttpClient();
                postTask = new HttpClientDetailTask();
                if(dialogMode.equals("1")) {/*Execute Post request for rating*/
                    Integer newRating = Integer.valueOf(data);
                    if(newRating >0){
                        String urlPostRating = "http://bismarck.sdsu.edu/rateme/rating/"
                                +idPassed + "/" + newRating;
                        setPostUrlID(1);
                        postTask.execute(urlPostRating);
                    }
                }else if(dialogMode.equals("2")){/*Execute Post request for comment*/

                    String urlPostComment = "http://bismarck.sdsu.edu/rateme/comment/"+idPassed;
                    newComment = data;
                    setPostUrlID(2);
                    postTask.execute(urlPostComment);
                }
            }
        } else {//no network connection

            // Create an instance of the dialog fragment and alert user to connect to
            // network and try again
            InstructorDialogFragment alertDialog = InstructorDialogFragment.newInstance("0");
            alertDialog.show(getFragmentManager(),"NO_NETWORK_ALERT");
        }

    }
    // -----show the progress dialog----
    private void showP_Dialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    // -----Dismiss the progress dialog----
    private void hideP_Dialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick() {
        finish();
    }

    public NetworkInfo checkNetworkConnection() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }

    public void addRating(View v) {
        // Create an instance of the dialog fragment and show rating bar
        InstructorDialogFragment rateMeDialog = InstructorDialogFragment.newInstance("1");
        rateMeDialog.show(getFragmentManager(),"RATE");

    }
    public void addComment(View v) {
        // Create an instance of the dialog fragment and show add comment
        InstructorDialogFragment rateMeDialog = InstructorDialogFragment.newInstance("2");
        rateMeDialog.show(getFragmentManager(),"COMMENT");
    }

    /**
    * Creating menu for refresh action;on click make new network request
    * */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                NetworkInfo networkInfo = checkNetworkConnection();

                if (networkInfo != null && networkInfo.isConnected()) {//Network connected

                    detailList = new ArrayList<>();
                    details = new HashMap<>();
                    makeJsonRequest();

                }else{
                    // Create an instance of the dialog fragment and alert user to connect to
                    // network and try again
                    InstructorDialogFragment alertDialog = InstructorDialogFragment.newInstance("0");
                    alertDialog.show(getFragmentManager(),"NO_NETWORK_ALERT");
                }
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
