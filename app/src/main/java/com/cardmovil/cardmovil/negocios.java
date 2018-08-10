package com.cardmovil.cardmovil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.json.row_negocios;
import com.cardmovil.cardmovil.models.list_negocios;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sa.tonisa.tonisa.R;
import com.cardmovil.cardmovil.models.list_categorias;

public class negocios extends AppCompatActivity {

    private static Context mContext;
    public LayoutInflater inflater;
    private TabHost tabs;
    Bundle bolsa;
    TextView tvCabeceraNegocios;
    ImageView ivCabeceraNegocios;
    ProgressBar progressBarImgNegocio;
    String url, categoriaid, distrito, latitud, longitud, id_usuario;
    public ListView lvNegocios;
    private ProgressDialog dialog;
    MaterialSearchView searchView;

    public ProgressDialog getDialog() {
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negocios);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarN);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchView = (MaterialSearchView) findViewById(R.id.search_viewN);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final String protocolo = "http://";
                String ip = getResources().getString(R.string.ipweb);
                String puerto = getResources().getString(R.string.puertoweb);
                puerto = puerto.equals("") ? "" : ":" + puerto;
                url = protocolo + ip + puerto + "/";

                row_negocios rwnegocios = new row_negocios();
                rwnegocios.execute(url + "/negocios/get_all", categoriaid, distrito, newText);
                return true;
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Cargando...");

        this.mContext = this;
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        ivCabeceraNegocios = (ImageView) findViewById(R.id.ivCabeceraNegocios);
        tvCabeceraNegocios = (TextView) findViewById(R.id.tvCabeceraNegocios);
        progressBarImgNegocio = (ProgressBar) findViewById(R.id.progressBarImgNegocio);

        lvNegocios = (ListView) findViewById(R.id.lvNegocios);

        final String protocolo = "http://";
        String ip = getResources().getString(R.string.ipweb);
        String puerto = getResources().getString(R.string.puertoweb);
        puerto = puerto.equals("") ? "" : ":" + puerto;
        url = protocolo + ip + puerto + "/";

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            String json = bolsa.getString("l_categoriaModel");
            distrito = bolsa.getString("distrito");
            latitud =  bolsa.getString("latitud");
            longitud = bolsa.getString("longitid");
            id_usuario = bolsa.getString("usuario");
            list_categorias list_categoriasModel = new Gson().fromJson(json, list_categorias.class);

            categoriaid = list_categoriasModel.getCat_Codigo();

            ImageLoader.getInstance().displayImage(url + list_categoriasModel.getCat_Imagen(), ivCabeceraNegocios, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBarImgNegocio.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBarImgNegocio.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBarImgNegocio.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBarImgNegocio.setVisibility(View.GONE);
                }
            });

            tvCabeceraNegocios.setText(list_categoriasModel.getCat_Desc());
            getSupportActionBar().setTitle(list_categoriasModel.getCat_Desc());

        }

        tabs = (TabHost) findViewById(R.id.thNegocios);
        tabs.setVisibility(View.VISIBLE);
        tabs.setup();
        TabHost.TabSpec spec = tabs.newTabSpec("tabSearch");

        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView title = (TextView) tabIndicator.findViewById(R.id.title);
        title.setText("Buscar");
        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.ic_search);
        tabIndicator.setTag("tabSearch");

        spec.setIndicator(tabIndicator);
        spec.setContent(R.id.tabSearchNegocios);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabPay");

        View tabIndicatorP = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView titleP = (TextView) tabIndicatorP.findViewById(R.id.title);
        titleP.setText("Pagar");
        ImageView iconP = (ImageView) tabIndicatorP.findViewById(R.id.icon);
        iconP.setImageResource(R.drawable.ic_pay);
        tabIndicatorP.setTag("tabPay");

        spec.setIndicator(tabIndicatorP);
        spec.setContent(R.id.tabPayNegocios);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabMap");

        View tabIndicatorM = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView titleM = (TextView) tabIndicatorM.findViewById(R.id.title);
        titleM.setText("Mapa");
        ImageView iconM = (ImageView) tabIndicatorM.findViewById(R.id.icon);
        iconM.setImageResource(R.drawable.varios_negocios);
        tabIndicatorM.setTag("tabMap");

        spec.setIndicator(tabIndicatorM);
        spec.setContent(R.id.tabMapNegocios);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabSetting");

        View tabIndicatorS = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView titleS = (TextView) tabIndicatorS.findViewById(R.id.title);
        titleS.setText("Setting");
        ImageView iconS = (ImageView) tabIndicatorS.findViewById(R.id.icon);
        iconS.setImageResource(R.drawable.ic_settings);
        tabIndicatorS.setTag("tabSetting");

        spec.setIndicator(tabIndicatorS);
        spec.setContent(R.id.tabSettingNegocios);
        tabs.addTab(spec);

        //click on seleccted tab
        int numberOfTabs = tabs.getTabWidget().getChildCount();
        for (int t = 0; t < numberOfTabs; t++) {
            tabs.getTabWidget().getChildAt(t).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String currentTag = (String) v.getTag();
                        if (currentTag.equalsIgnoreCase("tabSearch")) {
                            searchView.setHint("Buscar");
                            searchView.showSearch();
                            return true;
                        }else if (currentTag.equalsIgnoreCase("tabSetting")) {
                            Intent intent = new Intent(getApplicationContext(), configuracion.class);
                            Bundle bolsa = new Bundle();
                            bolsa.putString("usuario", id_usuario);
                            intent.putExtras(bolsa);
                            startActivity(intent);
                            return true;
                        }else if (currentTag.equalsIgnoreCase("tabMap")) {
                            Intent intent = new Intent(getApplicationContext(), mapa.class);
                            Bundle bolsa = new Bundle();
                            bolsa.putString("usuario", id_usuario);
                            bolsa.putString("desde", "negocios");
                            bolsa.putString("id_categoria", categoriaid);
                            bolsa.putString("latitud", latitud);
                            bolsa.putString("longitud", longitud);
                            intent.putExtras(bolsa);
                            startActivity(intent);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        row_negocios rwnegocios = new row_negocios();
        rwnegocios.execute(url + "/negocios/get_all", categoriaid, distrito, latitud, longitud, "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.negocios, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void llamar(View v) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + v.getTag().toString()));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

}
