package com.ibm.bluelist;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ibm.mobile.services.cloudcode.IBMCloudCode;
import com.ibm.mobile.services.core.http.IBMHttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by root on 7/6/15.
 */
public class sendTweet extends Activity{
    BlueListApplication blApplication;
    ArrayAdapter<Item> lvArrayAdapter;
    ActionMode mActionMode = null;
    int listItemPosition;
    String eatery;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom);
        String eatery = "dsfkms";
        Intent intent = getIntent();
        eatery= intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Random randomGenerator = new Random();
        ArrayList<String> eateries = new ArrayList<String>();
        eateries.add("enjoying a meal at"+eatery+" #yummy");
        eateries.add("food at "+eatery+" #tasty");
        eateries.add("#"+eatery+" food is delicious");
        eateries.add("currrently at "+eatery+" eatery. #enjoyingFood");

        int randomInt = randomGenerator.nextInt(eateries.size());
        System.out.println(eateries.get(randomInt));
        TextView tv = (TextView)findViewById(R.id.text);
        tv.setText(eateries.get(randomInt));

//
//        ArrayList<String> mall = new ArrayList<String>();
//        mall.add("having fun at "+mall);
//        mall.add("currently at "+mall+" #masti");
//        mall.add("enjoying my day at "+mall);
//
//        int randomInt2 = randomGenerator.nextInt(eateries.size());
//        System.out.println(eateries.get(randomInt2));


}


    public void customTweet(View view)
    {
        TextView tv = (TextView)findViewById(R.id.edit);
        String tweet = tv.getText().toString();
        System.out.println("Tweet" + tweet);
        try {
            tweet = URLEncoder.encode(tweet, "utf-8");
        }
        catch(UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
        }
        blApplication = (BlueListApplication) getApplication();

        IBMCloudCode.initializeService();
        IBMCloudCode myCloudCodeService = IBMCloudCode.getService();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("key1", "value1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

/*
* Call the node.js application hosted in the IBM Cloud Code service
* with a POST call, passing in a non-essential JSONObject.
* The URI is relative to/appended to the BlueMix context root.
*/
        myCloudCodeService.get("/sendTweet?tweet="+tweet).continueWith(new Continuation<IBMHttpResponse, Void>() {
            @Override
            public Void then(Task<IBMHttpResponse> task) throws Exception {
                if (task.isCancelled()) {
                    // Log.e(CLASS_NAME, "Exception : Task" + task.isCancelled() + "was cancelled.");
                } else if (task.isFaulted()) {
                    //  Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                } else {
                    InputStream is = task.getResult().getInputStream();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
                        String responseString = "";
                        String myString = "";
                        while ((myString = in.readLine()) != null)
                            responseString += myString;


                        //responseString.split("pName");

                        Log.d("response string: ", responseString);
//                        JSONArray jsonArray =  new JSONArray(responseString);
//                        //places = new String[jsonArray.length()];
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject explrObject = jsonArray.getJSONObject(i);
//                            //Item item1 = new Item();
//                            //item1.setName(explrObject.getString("pName"));
//                            //itemList.add(item1);
//                            places.add(explrObject.getString("pName").toString());
//                            Log.d("size is "," "+ explrObject.getString("pName"));
//                        }

                        in.close();
                        //System.out.println( "Response Body: " + responseString);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //  Log.i(CLASS_NAME, "Response Status from notifyOtherDevices: " + task.getResult().getHttpResponseCode());
                }

                return null;
            }

        });

    }



    public void appGenTweet(View view){

        String eatery = "dsfkms";
        Intent intent = getIntent();
        eatery= intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        Random randomGenerator = new Random();
        ArrayList<String> eateries = new ArrayList<String>();
        eateries.add("enjoying a meal at"+eatery+" #yummy");
        eateries.add("food at "+eatery+" #tasty");
        eateries.add("#"+eatery+" food is delicious");
        eateries.add("currrently at "+eatery+" eatery. #enjoyingFood");

        int randomInt = randomGenerator.nextInt(eateries.size());
        System.out.println(eateries.get(randomInt));


        blApplication = (BlueListApplication) getApplication();

        IBMCloudCode.initializeService();
        IBMCloudCode myCloudCodeService = IBMCloudCode.getService();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("key1", "value1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

/*
* Call the node.js application hosted in the IBM Cloud Code service
* with a POST call, passing in a non-essential JSONObject.
* The URI is relative to/appended to the BlueMix context root.
*/
        myCloudCodeService.get("/sendTweet?tweet="+eateries.get(randomInt)).continueWith(new Continuation<IBMHttpResponse, Void>() {
            @Override
            public Void then(Task<IBMHttpResponse> task) throws Exception {
                if (task.isCancelled()) {
                    // Log.e(CLASS_NAME, "Exception : Task" + task.isCancelled() + "was cancelled.");
                } else if (task.isFaulted()) {
                    //  Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                } else {
                    InputStream is = task.getResult().getInputStream();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
                        String responseString = "";
                        String myString = "";
                        while ((myString = in.readLine()) != null)
                            responseString += myString;


                        //responseString.split("pName");

                        Log.d("response string: ", responseString);
//                        JSONArray jsonArray =  new JSONArray(responseString);
//                        //places = new String[jsonArray.length()];
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject explrObject = jsonArray.getJSONObject(i);
//                            //Item item1 = new Item();
//                            //item1.setName(explrObject.getString("pName"));
//                            //itemList.add(item1);
//                            places.add(explrObject.getString("pName").toString());
//                            Log.d("size is "," "+ explrObject.getString("pName"));
//                        }

                        in.close();
                        //System.out.println( "Response Body: " + responseString);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //  Log.i(CLASS_NAME, "Response Status from notifyOtherDevices: " + task.getResult().getHttpResponseCode());
                }

                return null;
            }

        });



    }

}
