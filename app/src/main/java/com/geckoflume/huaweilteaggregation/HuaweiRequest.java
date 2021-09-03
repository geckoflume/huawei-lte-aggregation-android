package com.geckoflume.huaweilteaggregation;

import android.graphics.Color;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HuaweiRequest {
    public static final String TAG = "AsyncHuaweiRequest";
    private static final String RESPONSE_OK = "<response>OK</response>";
    private final String pwd;
    private CookieManager cookieManager;
    private String csrf1;
    private String csrf2;
    private String sessionId;
    private final String ip;
    private final StatusCallback statusCallback;

    public HuaweiRequest(StatusCallback statusCallback, String pwd, String ip) {
        this.statusCallback = statusCallback;
        this.pwd = pwd;
        this.ip = ip;
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine())
            sb.append(line);
        is.close();
        return sb.toString();
    }

    private void extractSessionId() {
        List<HttpCookie> cookies = this.cookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase("SessionID")) {
                this.sessionId = cookie.getValue();
                break;
            }
        }
    }

    public void fetchCsrf() {
        this.cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpURLConnection urlConnection = null;
        String result = null;

        try {
            URL url = new URL("http://" + this.ip + "/");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setUseCaches(false);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        result = result.substring(result.indexOf("meta name=\"csrf_token\" content=\""));
        this.csrf1 = result.substring(32, 64);
        extractSessionId();
    }

    public boolean login() {
        this.cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpURLConnection urlConnection = null;
        String result = null;
        boolean ret;

        try {
            URL url = new URL("http://" + this.ip + "/api/user/login");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Cookie", "SessionID=" + this.sessionId);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.setRequestProperty("__RequestVerificationToken", this.csrf1);
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><request><Username>admin</Username><Password>"
                    + HuaweiPassword.getInstance(this.pwd, this.csrf1).getPwd()
                    + "</Password><password_type>4</password_type></request>");
            out.close();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        ret = result.contains(HuaweiRequest.RESPONSE_OK);
        if (ret) {
            this.csrf2 = urlConnection.getHeaderField("__RequestVerificationTokenone");
            extractSessionId();
        }
        statusCallback.updateLoginTextView(ret, false);
        return ret;
    }

    public void getSesTokInfo() {
        this.cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpURLConnection urlConnection = null;
        String result = null;

        try {
            URL url = new URL("http://" + this.ip + "/api/webserver/SesTokInfo");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Cookie", "SessionID=" + this.sessionId);
            urlConnection.setRequestProperty("__RequestVerificationToken", this.csrf2);
            urlConnection.setUseCaches(false);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        result = result.substring(result.indexOf("<TokInfo>"));
        this.csrf1 = result.substring(9, 41);
    }


    public boolean changeBand(String band, int bandNumber) {
        this.cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        HttpURLConnection urlConnection = null;
        String result = null;
        boolean ret;

        try {
            URL url = new URL("http://" + this.ip + "/api/net/net-mode");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Cookie", "SessionID=" + this.sessionId);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.setRequestProperty("__RequestVerificationToken", this.csrf1);
            urlConnection.setRequestProperty("Connection", "keep-alive");
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><request><NetworkMode>03</NetworkMode><NetworkBand>3FFFFFFF</NetworkBand><LTEBand>"
                    + band
                    + "</LTEBand></request>");
            out.close();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        ret = result.contains(HuaweiRequest.RESPONSE_OK);
        statusCallback.updateBandTextView(band, bandNumber, ret, false);
        return ret;
    }
}
