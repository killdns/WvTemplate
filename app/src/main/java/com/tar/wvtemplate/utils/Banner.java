package com.tar.wvtemplate.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.tar.wvtemplate.R;
import com.tar.wvtemplate.controllers.ActivityController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Кирилл Даньшин on 06.02.2018.
 */

/**
 * Класс работы с баннером и открыванием ссылок
 */
public final class Banner {

    /**
     * Получение JSON по API и занесение данных из него
     */
    public static class ParseTask extends AsyncTask<Void, Void, String> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... params) {
            // получаем данные с внешнего ресурса
            try {
                URL url = new URL(ActivityController.getBaseActivity().getString(R.string.api_url));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            // выводим целиком полученную json-строку
            Log.e("JSON", strJson);
            JSONObject dataJsonObj;
            try {
                dataJsonObj = new JSONObject(strJson);
            }
            catch (JSONException e) {
                Log.e("JSON", "ERROR");
                return;
            }


            try {
                Log.e("bannerimgUrl", dataJsonObj.getJSONObject("banner").getString("imgUrl"));
                Log.e("bannerimgurlTo", dataJsonObj.getJSONObject("banner").getString("urlTo"));

                ActivityController.setBannerImageUrl(dataJsonObj.getJSONObject("banner").getString("imgUrl"));
                ActivityController.setBannerUrlTo(dataJsonObj.getJSONObject("banner").getString("urlTo"));

                ActivityController.setBannerOpenInDefaultBrowser(dataJsonObj.getJSONObject("settings").getBoolean("openInDefaultBrowser"));
                ActivityController.setBannerIstaOpen(dataJsonObj.getJSONObject("settings").getBoolean("InstaOpen"));

                if (ActivityController.getBannerIstaOpen()) {
                    openUrl();
                }
                else
                    new DownloadImageTask((ImageView) ActivityController.getBaseActivity().findViewById(R.id.banner))
                            .execute();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Скачивание картинки баннера
     */
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = ActivityController.getBannerImageUrl();
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);

            ActionBar actionBar = ActivityController.getBaseActivity().getSupportActionBar();
            actionBar.hide();
            ActivityController.getBaseActivity().findViewById(R.id.viewpager).setVisibility(View.GONE);
            ActivityController.getBaseActivity().findViewById(R.id.banner_frame).setVisibility(View.VISIBLE);

            ActivityController.getBaseActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ActivityController.setBannerIsShown(true);
            bmImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityController.setBannerIsShown(false);
                    ActivityController.getBaseActivity().findViewById(R.id.banner_frame).setVisibility(View.GONE);
                    ActivityController.getBaseActivity().findViewById(R.id.viewpager).setVisibility(View.VISIBLE);
                    ActivityController.getBaseActivity().getSupportActionBar().show();
                    openUrl();
                }
            });
        }
    }

    /**
     * Открытие ссылки
     */
    private static void openUrl()
    {
        if (ActivityController.getBannerOpenInDefaultBrowser())
        {
            Uri uri = Uri.parse(ActivityController.getBannerUrlTo());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            ActivityController.getBaseActivity().startActivity(intent);
        } else {

            WebView wv = ActivityController.getBaseActivity().findViewById(R.id.webview);

            ActivityController.getBaseActivity().registerForContextMenu(wv);

            wv.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                    Toast.makeText(ActivityController.getBaseActivity(), description, Toast.LENGTH_SHORT).show();
                }

                public void onPageFinished(WebView view, String url) {
                    CookieSyncManager.getInstance().sync();
                    if (ActivityController.getBaseActivity().findViewById(R.id.webview_frame).getVisibility() == View.GONE)
                    {
                        ActivityController.getBaseActivity().findViewById(R.id.viewpager).setVisibility(View.GONE);
                        ActivityController.getBaseActivity().findViewById(R.id.banner_frame).setVisibility(View.GONE);
                        ActivityController.getBaseActivity().findViewById(R.id.webview_frame).setVisibility(View.VISIBLE);
                        ActivityController.getBaseActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                        ActivityController.getBaseActivity().getSupportActionBar().hide();

                        ActivityController.getBaseActivity().findViewById(R.id.webview_frame).setVisibility(View.VISIBLE);
                    }
                }

            });



            wv.getSettings().setJavaScriptEnabled(true);
            wv.getSettings().setDomStorageEnabled(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
            } else {
                CookieManager.getInstance().setAcceptCookie(true);
            }
            wv.loadUrl( ActivityController.getBannerUrlTo());

            ActivityController.setWebViewIsShown(true);
        }
    }

}
