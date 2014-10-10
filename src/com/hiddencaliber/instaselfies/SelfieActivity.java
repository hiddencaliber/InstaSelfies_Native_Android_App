package com.hiddencaliber.instaselfies;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class SelfieActivity extends Activity {
	Bitmap bitmap= null;
    String[] selfieImgUrls = null;
    String instaURL = "https://api.instagram.com/v1/tags/selfie/media/recent?callback=?&amp;client_id=23b24e7b0a7c45a59aaa4b9375c80460";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie);
        new RequestTask().execute(instaURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.selfie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    class RequestTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
            	 TextView respTxt = (TextView) findViewById(R.id.response_msg);
                 respTxt.setText("Getting Selfies. Please wait...");
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView respTxt = (TextView) findViewById(R.id.response_msg);
            respTxt.setText("Pulling Selfies. Please wait...");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
           parseAndpopulateSelfies(result);
        }
        }
    private void parseAndpopulateSelfies(String responseFromInstaGramServer){
    	try{
    	JSONObject respObj= new JSONObject(responseFromInstaGramServer);
    	JSONArray dataArray= (JSONArray)respObj.getJSONArray("data");
    	selfieImgUrls= new String[dataArray.length()];
    	for(int dataNum=0; dataNum < dataArray.length(); dataNum++){
    		//kk.data[0].images.thumbnail.url
    		JSONObject dataObj=  (JSONObject) dataArray.get(dataNum);
    		JSONObject imageObj= (JSONObject)dataObj.get("images");
    		
    		JSONObject thumbnailObj= ((dataNum %  3) > 0) ? (JSONObject)imageObj.get("thumbnail") : (JSONObject)imageObj.get("low_resolution");
    		//JSONObject thumbnailObj= (JSONObject)imageObj.get("low_resolution");
    		//JSONObject thumbnailObj= (JSONObject)imageObj.get("high_resolution");
    		String imageUrl= thumbnailObj.getString("url");
    		selfieImgUrls[dataNum]= imageUrl.toString();
    	}
    	}
    	catch(JSONException jexp){
    		System.out.println("Some parser exception occured");
    	}
    	catch(Exception exp){
    		
    	}
    	 TextView respTxt = (TextView) findViewById(R.id.response_msg);  
    	 String allUrls= "";
    	 for(int strCnt=0; strCnt< selfieImgUrls.length; strCnt++ ){
    		 new DownloadImageTask().execute(selfieImgUrls[strCnt]);
    	 }
    }
    
    public Bitmap loadImageFromURL(String fileUrl){
    	Bitmap bitmap= null;
    		  try {  		 
    		    URL myFileUrl = new URL (fileUrl);
    		    HttpURLConnection conn =
    		      (HttpURLConnection) myFileUrl.openConnection();
    		    conn.setDoInput(true);
    		    conn.connect();    		 
    		    InputStream is = conn.getInputStream();
    		    //iv.setImageBitmap(BitmapFactory.decodeStream(is));
    		    bitmap= BitmapFactory.decodeStream(is);
    		    //return true;   		 
    		  } catch (MalformedURLException e) {
    		    e.printStackTrace();
    		  } catch (Exception e) {
    		    e.printStackTrace();
    		  }
    		 return bitmap;
    		 // return false;
    		}
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        /** The system calls this to perform work in a worker thread and
          * delivers it the parameters given to AsyncTask.execute() */
        protected Bitmap doInBackground(String... urls) {
            return loadImageFromURL(urls[0]);
        }
        
        /** The system calls this to perform work in the UI thread and delivers
          * the result from doInBackground() */
        protected void onPostExecute(Bitmap result) {
            ImageView imageView= new ImageView(getApplicationContext());
            imageView.setImageBitmap(result);
            imageView.setOnTouchListener(new View.OnTouchListener() {              
				@Override
				public boolean onTouch(View v, MotionEvent event) {					
					int eventaction = event.getAction();
				    switch (eventaction) {
				        case MotionEvent.ACTION_DOWN: 
				            // finger touches the screen
				            break;

				        case MotionEvent.ACTION_MOVE:
				            // finger moves on the screen
				            break;

				        case MotionEvent.ACTION_UP:   
				        	Toast.makeText(SelfieActivity.this, "Touch click performed... :)"+v.getScaleX() , Toast.LENGTH_LONG).show();
				        	if(v.getScaleX() >1){
					        	v.setScaleX(1);
					        	v.setScaleY(1);				        	
				        	}else{
					        	v.setScaleX(1.5f);
					        	v.setScaleY(1.5f);
					        	
				        	}
				        	break;
				    }

				    // tell the system that we handled the event and no further processing is required
				    return true; 
				}
            });    
            // imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
      		LinearLayout linearlayout = (LinearLayout) findViewById(R.id.selfiesWrapper);  		
      		linearlayout.addView(imageView); 
      		TextView respTxt = (TextView) findViewById(R.id.response_msg);
            respTxt.setText("Latest images in Instagram with #selfie are :");
        }
    }
    
    
    }



