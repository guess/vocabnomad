package ca.taglab.vocabnomad.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ExecuteRequest extends IntentService {

    int responseCode;
    int method;
    String message, response, entity;
    ArrayList <ParcelableNameValuePair> params;
    ArrayList <ParcelableNameValuePair> headers;
    HttpRequestBase request;
    ResultReceiver receiver;

    private String url;


    public ExecuteRequest() {
        super("executeRestRequest");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        params = intent.getParcelableArrayListExtra("params");
        headers = intent.getParcelableArrayListExtra("headers");
        url = intent.getStringExtra("url");
        receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        method = (int) intent.getIntExtra("method", 1);
        entity = intent.getStringExtra("entity");
        try {
            execute(method);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void executeDelete(String deletePath) throws Exception
    {
        request = new HttpDelete(url + deletePath);

        //add headers
        for(NameValuePair h : headers)
        {
            request.addHeader(h.getName(), h.getValue());
        }
        commit();
    }

    public void execute(int method) throws Exception
    {
        String combinedParams = "";
        if(!params.isEmpty()){
            for(NameValuePair p : params)
            {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
                if(combinedParams.length() > 1)
                {
                    combinedParams  +=  ";" + paramString;
                }
                else
                {
                    combinedParams += paramString;
                }
            }
        }

        Log.i("RestService", url + combinedParams);

        switch(method) {
            case RestService.GET:
            {
                request = new HttpGet(url + combinedParams);

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }

                commit();
                break;
            }
            case RestService.POST:
            {

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }

                Log.i("RestService", url + combinedParams);
                request = new HttpPost(url + combinedParams);

                if (entity != null && !TextUtils.isEmpty(entity)) {
                    StringEntity se = new StringEntity(entity);
                    ((HttpPost) request).setEntity(se);
                }
                commit();
                break;
            }
            case RestService.PUT:
            {

                request = new HttpPut(url + combinedParams);

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }

                if (entity != null && !TextUtils.isEmpty(entity)) {
                    StringEntity se = new StringEntity(entity);
                    ((HttpPut) request).setEntity(se);
                }

                commit();
                break;
            }
            case RestService.DELETE:
            {
                request = new HttpDelete(url);

                //add headers
                for(NameValuePair h : headers)
                {
                    request.addHeader(h.getName(), h.getValue());
                }
                commit();
            }

        }
    }

    private void commit(){
        // Changed to AndroidHttpClient because it just works for some reason
        //HttpClient client = new DefaultHttpClient();

        AndroidHttpClient client = AndroidHttpClient.newInstance("");

        HttpResponse httpResponse;

        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {

                InputStream instream = entity.getContent();
                String response = convertStreamToString(instream);
                Bundle responseBundle = new Bundle();
                responseBundle.putString("result", response);
                receiver.send(method, responseBundle);
                // Closing the input stream will trigger connection release
                instream.close();

            }

        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            client.close();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            client.close();
            e.printStackTrace();
        }

        client.close();
    }


    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}