package com.cardmovil.cardmovil;

import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sa.tonisa.tonisa.R;

public class zoomImagen extends FragmentActivity {

    ImageView imgZoom;
    private ProgressBar progressZoomImagen;
    Bundle bolsa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_imagen);

        imgZoom = (ImageView)findViewById(R.id.imgZoom);
        progressZoomImagen = (ProgressBar) findViewById(R.id.progressZoomImagen);

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            ImageLoader.getInstance().displayImage(bolsa.getString("urlImagen"), imgZoom, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressZoomImagen.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressZoomImagen.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressZoomImagen.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressZoomImagen.setVisibility(View.GONE);
                }
            });
        }
    }
}
