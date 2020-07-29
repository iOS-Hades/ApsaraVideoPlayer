package com.aliyun.sdk.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class HttpClientHelper {
    private static final int CONNECTION_TIMEOUT = 10000;

    private static ExecutorService sThreadCachePool = Executors.newCachedThreadPool();

    private URLConnection urlConnection = null;


    public HttpClientHelper(String serverUrl) {
        if (serverUrl.startsWith("https://")) {
            urlConnection = getHttpsUrlConnection(serverUrl);
        } else if (serverUrl.startsWith("http://")) {
            urlConnection = getHttpUrlConnection(serverUrl);
        } else {
            urlConnection = null;
        }
    }

    public String doGet() {

        if (urlConnection == null) {
            return null;
        }

        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {

            StringBuilder response = null;

            int responseCode = getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                in = urlConnection.getInputStream();
                //下面对获取到的输入流进行读取
                inputStreamReader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(inputStreamReader);
                response = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            } else {

                in = getErrorStream();

                //下面对获取到的输入流进行读取
                inputStreamReader = new InputStreamReader(in);
                bufferedReader = new BufferedReader(inputStreamReader);
                response = new StringBuilder();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("StatusCode", responseCode);
                jsonObject.put("ResponseStr", response.toString());

                return jsonObject.toString();

            }
        } catch (Exception e) {
            VcPlayerLog.d("HttpClientUtil", e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }

                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {

            }
            if (urlConnection != null) {
                if (urlConnection instanceof HttpURLConnection) {
                    ((HttpURLConnection) urlConnection).disconnect();
                } else if (urlConnection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) urlConnection).disconnect();
                }
            }
        }
        return null;
    }

    private InputStream getErrorStream() {
        InputStream in = null;
        if (urlConnection instanceof HttpsURLConnection) {
            in = ((HttpsURLConnection) urlConnection).getErrorStream();
        } else if (urlConnection instanceof HttpURLConnection) {
            in = ((HttpURLConnection) urlConnection).getErrorStream();
        }
        return in;
    }

    private int getResponseCode() throws IOException {
        int responseCode = 0;
        if (urlConnection instanceof HttpsURLConnection) {
            responseCode = ((HttpsURLConnection) urlConnection).getResponseCode();
        } else if (urlConnection instanceof HttpURLConnection) {
            responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
        }
        return responseCode;
    }


    public void stop() {
        VcPlayerLog.e("lfj0417_2", "HttpClientHelper stop().... urlConnection = " + urlConnection);
        if (urlConnection != null) {
            sThreadCachePool.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (urlConnection instanceof HttpsURLConnection) {
                                    VcPlayerLog.e("lfj0417_2", "HttpClientHelper stop().... HttpsURLConnection.disconnect ");
                                    ((HttpsURLConnection) urlConnection).disconnect();
                                } else if (urlConnection instanceof HttpURLConnection) {
                                    VcPlayerLog.e("lfj0417_2", "HttpClientHelper stop().... HttpURLConnection.disconnect ");
                                    ((HttpURLConnection) urlConnection).disconnect();
                                }
                            } catch (Exception e) {
                                VcPlayerLog.e("lfj0417_2", e.getMessage());
                            }

                        }
                    });
        }
    }

    private URLConnection getHttpUrlConnection(String serverUrl) {
        URLConnection urlConnection = null;

        URL url = null;
        try {
            url = new URL(serverUrl);
            urlConnection = url.openConnection();
            if (!(urlConnection instanceof HttpURLConnection)) {
                return null;
            }

            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return urlConnection;
    }

    private URLConnection getHttpsUrlConnection(String serverUrl) {
        URLConnection urlConnection = null;

        URL url = null;
        try {
            url = new URL(serverUrl);
            urlConnection = url.openConnection();
            if (!(urlConnection instanceof HttpsURLConnection)) {
                return null;
            }

            HttpsURLConnection connection = (HttpsURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return urlConnection;
    }

}
