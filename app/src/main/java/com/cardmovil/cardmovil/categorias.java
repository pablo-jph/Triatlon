package com.cardmovil.cardmovil;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.ayudas.mapa.Constants;
import com.cardmovil.cardmovil.json.post_preferencia;
import com.cardmovil.cardmovil.json.row_tarjetas;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.sa.tonisa.tonisa.R;
import com.cardmovil.cardmovil.json.row_categorias;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class categorias extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, LocationListener, NavigationView.OnNavigationItemSelectedListener {

    private static Context mContext;
    public LayoutInflater inflater;
    private TabHost tabs;
    private ProgressDialog dialog;
    public TextView nameNavigation, email, tvAddTarjeta;

    public ProgressDialog getDialog() {
        return dialog;
    }

    public ListView lvCategorias, lvTarjetas;
    public String url, usuario_id, pais_id, distrito_id, categoria_id, item_selecateg = "0", estado = "-1", urlJson, url_tarjetas;
    Bundle bolsa;
    MaterialSearchView searchView;
    Menu menu_s = null;
    public Integer num_preferencias = 0;
    String origen = "";

    // Location API
    private static final String TAG = categorias.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mLastLocation;
    public static final int REQUEST_LOCATION = 1;
    public String latitud = "", longitud = "";

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final String LOCATION_KEY = "location-key";
    private static final String ACTIVITY_KEY = "activity-key";

    @DrawableRes
    private int mImageResource = R.drawable.ic_bloquear;

    // UI
    private ImageView mDectectedActivityIcon;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.srlContainerConteniedo);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
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
                url = protocolo + ip + puerto + "/categorias/get_all";

                row_categorias rwcategorias = new row_categorias();

                rwcategorias.execute(url, usuario_id, pais_id, distrito_id, latitud, longitud, newText);
                return true;
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Cargando...");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        this.mContext = this;
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        lvCategorias = (ListView) findViewById(R.id.lvCategorias);

        lvCategorias.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvCategorias == null || lvCategorias.getChildCount() == 0) ? 0 : lvCategorias.getChildAt(0).getTop();
                swipeContainer.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String protocolo = "http://";
                String ip = getResources().getString(R.string.ipweb);
                String puerto = getResources().getString(R.string.puertoweb);
                puerto = puerto.equals("") ? "" : ":" + puerto;
                url = protocolo + ip + puerto + "/categorias/get_all";

                row_categorias rwcategorias = new row_categorias();
                rwcategorias.execute(url, usuario_id, pais_id, distrito_id, latitud, longitud, "");

                url_tarjetas = protocolo + ip + puerto + "/tarjetas/get_tarjetas_pagador";
                row_tarjetas rwtarjetas = new row_tarjetas();
                rwtarjetas.execute(url_tarjetas, usuario_id);
                swipeContainer.setRefreshing(false);
            }
        });

        bolsa = getIntent().getExtras();
        usuario_id = bolsa.getString("usuario");
        pais_id = bolsa.getString("pais");
        distrito_id = bolsa.getString("distrito");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        nameNavigation = (TextView) header.findViewById(R.id.tvProfileName);
        email = (TextView) header.findViewById(R.id.tvProfileEmail);
        tvAddTarjeta = (TextView) header.findViewById(R.id.tvAddTarjeta);
        lvTarjetas = (ListView) header.findViewById(R.id.lvTarjetas);
        nameNavigation.setText(bolsa.getString("nombre"));
        email.setText(bolsa.getString("correo"));

        tvAddTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), add_tarjeta.class);
                Bundle bolsa = new Bundle();
                bolsa.putString("usuario", usuario_id);
                bolsa.putString("pais", pais_id);
                bolsa.putString("distrito", distrito_id);
                i.putExtras(bolsa);
                startActivity(i);
            }
        });

        tabs = (TabHost) findViewById(R.id.thBancos);
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
        spec.setContent(R.id.tabSearch);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabPay");

        View tabIndicatorP = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView titleP = (TextView) tabIndicatorP.findViewById(R.id.title);
        titleP.setText("Pagar");
        ImageView iconP = (ImageView) tabIndicatorP.findViewById(R.id.icon);
        iconP.setImageResource(R.drawable.ic_pay);
        tabIndicatorP.setTag("tabPay");

        spec.setIndicator(tabIndicatorP);
        spec.setContent(R.id.tabPay);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabMap");

        View tabIndicatorM = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView titleM = (TextView) tabIndicatorM.findViewById(R.id.title);
        titleM.setText("Mapa");
        ImageView iconM = (ImageView) tabIndicatorM.findViewById(R.id.icon);
        iconM.setImageResource(R.drawable.varios_negocios);
        tabIndicatorM.setTag("tabMap");

        spec.setIndicator(tabIndicatorM);
        spec.setContent(R.id.tabMap);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabSetting");

        View tabIndicatorS = LayoutInflater.from(this).inflate(R.layout.indicator, tabs.getTabWidget(), false);
        TextView titleS = (TextView) tabIndicatorS.findViewById(R.id.title);
        titleS.setText("Setting");
        ImageView iconS = (ImageView) tabIndicatorS.findViewById(R.id.icon);
        iconS.setImageResource(R.drawable.ic_settings);
        tabIndicatorS.setTag("tabSetting");

        spec.setIndicator(tabIndicatorS);
        spec.setContent(R.id.tabSetting);
        tabs.addTab(spec);

        //click on seleccted tab
        int numberOfTabs = tabs.getTabWidget().getChildCount();
        for (int t = 0; t < numberOfTabs; t++) {
            tabs.getTabWidget().getChildAt(t).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String currentSelectedTag = tabs.getCurrentTabTag();
                        String currentTag = (String) v.getTag();
                        Log.d(this.getClass().getSimpleName(), "currentSelectedTag: " + currentSelectedTag + " currentTag: " + currentTag);
                        if (currentTag.equalsIgnoreCase("tabSearch")) {
                            searchView.setHint("Buscar");
                            searchView.showSearch();
                            return true;
                        }else if (currentTag.equalsIgnoreCase("tabSetting")) {
                            Intent intent = new Intent(getApplicationContext(), configuracion.class);
                            Bundle bolsa = new Bundle();
                            bolsa.putString("usuario", usuario_id);
                            intent.putExtras(bolsa);
                            startActivity(intent);
                            return true;
                        }else if (currentTag.equalsIgnoreCase("tabMap")) {
                            Intent intent = new Intent(getApplicationContext(), mapa.class);
                            Bundle bolsa = new Bundle();
                            bolsa.putString("usuario", usuario_id);
                            bolsa.putString("desde", "categoria");
                            bolsa.putString("id_categoria", "0");
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

        final String protocolo = "http://";
        String ip = getResources().getString(R.string.ipweb);
        String puerto = getResources().getString(R.string.puertoweb);
        puerto = puerto.equals("") ? "" : ":" + puerto;
        url = protocolo + ip + puerto + "/categorias/get_all";

        row_categorias rwcategorias = new row_categorias();
        rwcategorias.execute(url, usuario_id, pais_id, distrito_id, latitud, longitud, "");

        url_tarjetas = protocolo + ip + puerto + "/tarjetas/get_tarjetas_pagador";
        row_tarjetas rwtarjetas = new row_tarjetas();
        rwtarjetas.execute(url_tarjetas, usuario_id);

        // Crear configuración de peticiones
        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();
        mGoogleApiClient.connect();
    }

    public static Context getContext() {
        return mContext;
    }

    public void setTabIcon(TabHost tabHost, int tabIndex, int iconResource) {
        ImageView tabImageView = (ImageView) tabHost.getTabWidget().getChildTabViewAt(tabIndex).findViewById(android.R.id.icon);
        tabImageView.setVisibility(View.VISIBLE);
        tabImageView.setImageResource(iconResource);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu_s = menu;
        getMenuInflater().inflate(R.menu.categorias, menu);
        MenuItem item = menu.findItem(R.id.action_settings);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String getItem_selecateg() {
        return item_selecateg;
    }

    public void setItem_selecateg(String item_selecateg) {
        this.item_selecateg = item_selecateg;
    }

    public void registrar_preferencia(View v) {
        String[] separatedPreferencia = v.getTag().toString().split(",");
        Log.d("probando", separatedPreferencia[0] + "-" + separatedPreferencia[1] + "-" + separatedPreferencia[2]);
        Log.d("probando", num_preferencias.toString());
        categoria_id = separatedPreferencia[0];
//            if (getItem_selecateg().equals("0")) {
        if (separatedPreferencia[1] == null) {
            item_selecateg = "0";
        } else {
            item_selecateg = separatedPreferencia[1];
        }
//            } else {
//                item_selecateg = getItem_selecateg();
//            }
        if (estado.equals("-1")) {
            if (separatedPreferencia[2] == null || separatedPreferencia[2].equals("0")) {
                estado = "1";
            } else {
                estado = "0";
            }
        } else if (estado.equals("0")) {
            estado = "1";
        } else {
            estado = "0";
        }
        if (estado.equals("0")) {
            num_preferencias--;
            if (num_preferencias < 0)
                num_preferencias = 0;
            v.setBackgroundResource(R.drawable.ic_star);
        } else {
            num_preferencias++;
            if (num_preferencias <= 5)
                v.setBackgroundResource(R.drawable.ic_star_seleccionado);
            else
                Toast.makeText(getApplicationContext(), "Máximo solo 5 preferencias", Toast.LENGTH_SHORT).show();
        }

        if (num_preferencias <= 5) {
            post_preferencia jsonPost = new post_preferencia();
            final String protocolo = "http://";
            String ip = getResources().getString(R.string.ipweb);
            String puerto = getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            urlJson = protocolo + ip + puerto + "/categorias/set_preferencia";
            jsonPost.execute(urlJson, categoria_id, usuario_id, item_selecateg, estado);
            v.setTag(categoria_id+","+item_selecateg+","+estado);
            estado = "-1";
        } else
            num_preferencias = 5;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (isLocationPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }else{
                settingsrequest();
            }
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                latitud = String.valueOf(mLastLocation.getLatitude());
                longitud = String.valueOf(mLastLocation.getLongitude());
                setLocation(latitud, longitud);
            }

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            manageDeniedPermission();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    private boolean isLocationPermissionGranted() {
        int permission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void manageDeniedPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Aquí muestras confirmación explicativa al usuario
            // por si rechazó los permisos anteriormente
            Toast.makeText(this, "Necesita otorgar permisos a la aplicación", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Detección de actividad iniciada");

        } else {
            Log.e(TAG, "Error al iniciar/remover la detección de actividad: "
                    + status.getStatusMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitud = String.valueOf(location.getLatitude());
        longitud = String.valueOf(location.getLongitude());
        setLocation(latitud, longitud);
    }

    public void setLocation(String lat, String lon) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud

        Log.d("direccion", lat + "," + lon);
        if (Double.parseDouble(lat) != 0.0 && Double.parseDouble(lon) != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    Log.d("direccion", "Mi direccion es: \n"
                            + DirCalle.getAddressLine(0) + DirCalle.getAddressLine(1) + DirCalle.getAddressLine(2));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //SOLICITAR ACTIVAR UBICACION
    public void settingsrequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(categorias.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    //RESULTADO DE LA SOLICITUD DE ACTIVACION DE UBICACION
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        settingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }
}