package com.tar.wvtemplate.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.github.ybq.parallaxviewpager.ParallaxViewPager;
import com.tar.wvtemplate.R;
import com.tar.wvtemplate.controllers.ActivityController;
import com.tar.wvtemplate.utils.Assets;
import com.tar.wvtemplate.utils.Banner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    ArrayList<Drawable> items = new ArrayList<>();
    boolean enableAutoScroll = true;

    @ViewById(R.id.banner_frame)
    FrameLayout banner_frame;


    @ViewById(R.id.viewpager)
    ParallaxViewPager viewpager;

    @ViewById(R.id.webview_frame)
    FrameLayout webview_frame;


    @ViewById(R.id.webview)
    WebView webview;


    public static final int REQUEST_CODE_LOLIPOP = 1;
    private final static int RESULT_CODE_ICE_CREAM = 2;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;


    /**
     * Метод, вызываемый после загрузки
     */
    @AfterViews
    public void afterView() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        //getSupportActionBar().hide();
        banner_frame.setVisibility(View.GONE);
        webview_frame.setVisibility(View.GONE);

        ActivityController.setBaseActivity(this);

        //Запоняем ArrayList изображениями
        for (String file : Assets.listAssetFiles("")) {
            items.add(Assets.getImageFromAssets(file));
        }

        //Инициализируем ViewPager
        initViewPager();
        //Запускаем поток для автосмены картинок
        doInUiThread();

         initWebView();
         new Banner.ParseTask().execute();

    }

    /**
     * Метод смены картинок
     */
    @UiThread
    void changeViewPagerImage() {
        viewpager.setCurrentItem((viewpager.getCurrentItem() + 1) % items.size());
    }

    /**
     * Поток с циклом в котором вызывается метод смены картинок
     */
    @Background(delay=5000)
    void doInUiThread() {
        while (enableAutoScroll && !ActivityController.getWebViewIsShown()) {
            changeViewPagerImage();
            SystemClock.sleep(5000);
        }
    }

    /**
     * Метод инициализации ViewPager
     */
    private void initViewPager() {

        PagerAdapter adapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object obj) {
                container.removeView((View) obj);

            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = View.inflate(container.getContext(), R.layout.item_pager, null);
                Drawable image = items.get(position);
                final ImageView imageView = view.findViewById(R.id.item_img);
                imageView.setImageDrawable(image);
                container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                view.setTag(position);


                //Отмена смены картинок при нажатии на картинку
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        enableAutoScroll = false;
                        return false;
                    }
                });
                return view;
            }

            @Override
            public int getCount() {
                return items.size();
            }
        };
        viewpager.setAdapter(adapter);
    }


    /**
     * Обработка нажатия кнопки "Назад"
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (ActivityController.getWebViewIsShown()) {
                            if (webview.canGoBack()) {
                                webview.goBack();
                            } else {
                                finish();
                            }
                        }
                        if (ActivityController.getBannerIsShown()) {
                            ActivityController.setBannerIsShown(false);
                            getSupportActionBar().show();
                            banner_frame.setVisibility(View.GONE);
                            viewpager.setVisibility(View.VISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        }
                        return true;
                }

        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Контекстное меню сохранения изображения
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu,View v,ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);

        // Get the web view hit test result
        final WebView.HitTestResult result = webview.getHitTestResult();


        // If user long press on an image
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

            // Set the title for context menu
            menu.setHeaderTitle(R.string.context_menu_title);

            // Add an item to the menu
            menu.add(0, 1, 0, R.string.menu_save_image)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Get the image url
                            String imgUrl = result.getExtra();

                            // If this is an image url then download it
                            if(URLUtil.isValidUrl(imgUrl)){
                                // Initialize a new download request
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgUrl));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);

                                Toast.makeText(ActivityController.getBaseActivity(), R.string.image_saved,Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ActivityController.getBaseActivity(), R.string.invalid_image_url,Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    });
        }
    }


    private void initWebView() {
        webview.setWebChromeClient(new WebChromeClient() {

            //The undocumented magic method override
            //Eclipse will swear at you if you try to put @Override here
            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {

                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                ActivityController.getBaseActivity().startActivityForResult(Intent.createChooser(i, "File Chooser"),
                        RESULT_CODE_ICE_CREAM);

            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                ActivityController.getBaseActivity().startActivityForResult(Intent.createChooser(i, "File Browser"),
                        RESULT_CODE_ICE_CREAM);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                ActivityController.getBaseActivity().startActivityForResult(Intent.createChooser(i, "File Chooser"),
                        RESULT_CODE_ICE_CREAM);

            }

            //For Android5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(ActivityController.getBaseActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e("img", "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");

                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                ActivityController.getBaseActivity().startActivityForResult(chooserIntent, REQUEST_CODE_LOLIPOP);

                return true;
            }

            private File createImageFile() throws IOException {
                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File imageFile = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                return imageFile;
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_CODE_ICE_CREAM:
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                mUploadMessage.onReceiveValue(uri);
                mUploadMessage = null;
                break;
            case REQUEST_CODE_LOLIPOP:
                Uri[] results = null;
                // Check that the response is a good one
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        // If there is not data, then we may have taken a photo
                        if (mCameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        }
                    } else {
                        String dataString = data.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(results);
                    mFilePathCallback = null;
                }
                break;
        }
    }
}
