package com.cardmovil.cardmovil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.Modules.DirectionFinder;
import com.cardmovil.cardmovil.Modules.DirectionFinderListener;
import com.cardmovil.cardmovil.Modules.Route;
import com.cardmovil.cardmovil.ayudas.mapa.Constants;
import com.cardmovil.cardmovil.ayudas.mapa.DetectedActivitiesIntentService;
import com.cardmovil.cardmovil.ayudas.mapa.MapWrapperLayout;
import com.cardmovil.cardmovil.ayudas.mapa.OnInfoWindowElemTouchListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sa.tonisa.tonisa.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class mapa extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, OnMapReadyCallback, DirectionFinderListener, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final String TAG = mapa.class.getSimpleName();

    private static final String LOCATION_KEY = "location-key";
    private static final String ACTIVITY_KEY = "activity-key";

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Location API
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mLastLocation;

    @DrawableRes
    private int mImageResource = R.drawable.ic_bloquear;

    // UI
    private ImageView mDectectedActivityIcon;

    // Códigos de petición
    public static final int REQUEST_LOCATION = 1;

    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    Bundle bolsa, tempBundle;
    String destino = "", origen = "", modeMapa, id_usuario, desde, latitud, longitud, id_categoria, razon_social, direccion, telefono, logo, latitud_tienda, longitud_tiend;
    private ShowcaseView showcaseView;

    LatLng latLng;
    Marker currLocationMarker;

    private View popup=null;
    private OnInfoWindowElemTouchListener infoButtonListener, infoButtonListenerRuta;

    Button btnLlamarMapa, btnRutaMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        modeMapa = "walking";

        tempBundle = savedInstanceState;

        // Establecer punto de entrada para la API de ubicación
//        buildGoogleApiClient();

        // Crear configuración de peticiones
        createLocationRequest();

        // Crear opciones de peticiones
        buildLocationSettingsRequest();

        updateValuesFromBundle(savedInstanceState);

//        showcaseView = new ShowcaseView.Builder(this)
//                .setTarget(viewTarget)
//                .setContentTitle(R.string.movilidad)
//                .setContentText(R.string.movilidad_ayuda)
//                .setStyle(R.style.CustomShowcaseTheme2)
//                .singleShot(4)
//                .build();
//        showcaseView.setButtonText(getString(R.string.close));

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            id_usuario = bolsa.getString("usuario");
            desde = bolsa.getString("desde");
            id_categoria = bolsa.getString("id_categoria");
            latitud = bolsa.getString("latitud");
            longitud = bolsa.getString("longitud");

            final String protocolo = "http://";
            String ip = getResources().getString(R.string.ipweb);
            String puerto = getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";

            if(desde.equals("categoria")){
                url = protocolo + ip + puerto +  "/negocios/get_negocios_5km";
                new JSONTaskNegociosMapa().execute(url, latitud, longitud, id_categoria);
            }else if(desde.equals("negocios")){
                url = protocolo + ip + puerto +  "/negocios/get_negocios_5km_by_categoria";
                new JSONTaskNegociosMapa().execute(url, latitud, longitud, id_categoria);
            }else if(desde.equals("negocio")){
                razon_social = bolsa.getString("razon_social");
                direccion = bolsa.getString("direccion");
                telefono = bolsa.getString("telefono");
                logo = bolsa.getString("logo");
                latitud_tienda = bolsa.getString("latitud");
                longitud_tiend = bolsa.getString("longitud");
            }

        }
    }

    private void startActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.ACTIVITY_RECOGNITION_INTERVAL,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    private void processLastLocation() {
        getLastLocation();
        if (mLastLocation != null) {
            updateLocationUI();
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);

                updateLocationUI();
            }

            if (savedInstanceState.containsKey(ACTIVITY_KEY)) {
                mImageResource = savedInstanceState.getInt(ACTIVITY_KEY);

                updateRecognitionUI();
            }
        }
    }

    private void updateRecognitionUI() {
        mDectectedActivityIcon.setImageResource(mImageResource);
    }

    private void updateLocationUI() {
        origen = String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude());
        latitud = String.valueOf(mLastLocation.getLatitude());
        longitud = String.valueOf(mLastLocation.getLongitude());
        sendRequest();
    }

    private void manageDeniedPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Aquí muestras confirmación explicativa al usuario
            // por si rechazó los permisos anteriormente
            Toast.makeText(this, "Conceder permisos y reinicar aplicación", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);

        }
    }


    private boolean isLocationPermissionGranted() {
        int permission = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest)
                .setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(Constants.UPDATE_INTERVAL)
                .setFastestInterval(Constants.UPDATE_FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Toast.makeText(this,"onConnected",Toast.LENGTH_SHORT).show();
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
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                //place marker at current position
                //mGoogleMap.clear();

                origen = String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude());
                latitud = String.valueOf(mLastLocation.getLatitude());
                longitud = String.valueOf(mLastLocation.getLongitude());
                sendRequest();

//            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(latLng);
//            markerOptions.title("Current Position");
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//            currLocationMarker = mMap.addMarker(markerOptions);
            }

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            manageDeniedPermission();
        }



//
//        // Obtenemos la última ubicación al ser la primera vez
//        processLastLocation();
//        // Y también las de reconocimiento de actividad
//        startActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Conexión suspendida");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(
                this,
                "Error de conexión con el código:" + connectionResult.getErrorCode(),
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {

        //place marker at current position
        //mGoogleMap.clear();

        if (origen.equals("")) {
            origen = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
            latitud = String.valueOf(location.getLatitude());
            longitud = String.valueOf(location.getLongitude());
            sendRequest();
        }

//        if (currLocationMarker != null) {
//            currLocationMarker.remove();
//        }
//        latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        currLocationMarker = mMap.addMarker(markerOptions);
//
//        Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();
//
//        //zoom to current position:
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

        //If you only need one location, unregister the listener
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

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

    public PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void getLastLocation() {
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
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        } else {
            manageDeniedPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1: {
                recreate();
                if (permissions.length == 1 &&
                        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                }
            }
        }
    }

    private void sendRequest() {
        String origin = origen;
        String destination = destino;
        String mode = modeMapa;

        Log.d("ubicacion", destino);


        if (origin.isEmpty()) {
//            Toast.makeText(this, "Ruta no encontrada", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
//            Toast.makeText(this, "Ruta no encontrada", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            new DirectionFinder(this, origin, destination, mode).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        @SuppressLint("WrongViewCast")
        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        if(popup == null){
            popup = getLayoutInflater().inflate(R.layout.popup_maps, null);
        }
        btnLlamarMapa = (Button) popup.findViewById(R.id.btnLlamarMapa);
        infoButtonListener = new OnInfoWindowElemTouchListener(btnLlamarMapa) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + v.getTag().toString()));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        };
        btnLlamarMapa.setOnTouchListener(infoButtonListener);

        btnRutaMapa = (Button) popup.findViewById(R.id.btnRutaMapa);
        infoButtonListenerRuta = new OnInfoWindowElemTouchListener(btnRutaMapa) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                destino = v.getTag().toString();
                sendRequest();
            }
        };
        btnRutaMapa.setOnTouchListener(infoButtonListenerRuta);


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if(popup == null){
                    popup = getLayoutInflater().inflate(R.layout.popup_maps, null);
                }

                TextView tvNombreNegocioMaoa = (TextView)popup.findViewById(R.id.tvNombreNegocioMaoa);
                TextView tvDireccionNegocioMapa = (TextView)popup.findViewById(R.id.tvDireccionNegocioMapa);
                TextView tvEstadoNegocioMapa = (TextView)popup.findViewById(R.id.tvEstadoNegocioMapa);
                ImageView ivLogoNegocioMapa = (ImageView) popup.findViewById(R.id.ivLogoNegocioMapa);
                final ProgressBar progressBarNegocioMapa = (ProgressBar) popup.findViewById(R.id.progressBarNegocioMapa);
                tvNombreNegocioMaoa.setText(marker.getTitle());
                String[] separated = marker.getSnippet().split("&");
                tvDireccionNegocioMapa.setText(separated[0]);
                btnLlamarMapa.setTag(separated[1]);
                infoButtonListener.setMarker(marker);
                infoButtonListenerRuta.setMarker(marker);
                btnRutaMapa.setTag(String.valueOf(marker.getPosition().latitude)+","+String.valueOf(marker.getPosition().longitude));

                final String protocolo = "http://";
                String ip = getResources().getString(R.string.ipweb);
                String puerto = getResources().getString(R.string.puertoweb);
                puerto = puerto.equals("") ? "" : ":" + puerto;
                String url = protocolo + ip + puerto + "/";

                ImageLoader.getInstance().displayImage(url + separated[2], ivLogoNegocioMapa, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        progressBarNegocioMapa.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        progressBarNegocioMapa.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBarNegocioMapa.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        progressBarNegocioMapa.setVisibility(View.GONE);
                    }
                });

                mapWrapperLayout.setMarkerWithInfoWindow(marker, popup);
                return (popup);
            }
        });

        buildGoogleApiClient();
        mGoogleApiClient.connect();

        LatLng hcmus = new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 13));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if(desde.equals("negocio")) {
            LatLng ohcmuss = new LatLng(Float.parseFloat(latitud_tienda), Float.parseFloat(longitud_tiend));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(razon_social)
                    .snippet(direccion + "&" + telefono + "&" + logo)
                    .position(ohcmuss)));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        } else {
            settingsrequest();
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Espere por favor",
                "¡Buscando dirección!", true);

//        if (originMarkers != null) {
//            for (Marker marker : originMarkers) {
//                marker.remove();
//            }
//        }
//
//        if (destinationMarkers != null) {
//            for (Marker marker : destinationMarkers) {
//                marker.remove();
//            }
//        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        //onPause();
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 13));

//            originMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.un_negocio))
//                    .title(route.startAddress)
//                    .snippet("Suc_direccion&Suc_telefijo&Cli_logo")
//                    .position(route.startLocation)));
//            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.varios_negocios))
//                    .title(route.endAddress)
//                    .position(route.endLocation)));
            PolylineOptions polylineOptions = null;
            if (modeMapa.equals("driving")) {
                polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(10);
            } else if (modeMapa.equals("walking")) {
                polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.RED).
                        width(10);
            } else if (modeMapa.equals("bicycling")) {
                polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.GREEN).
                        width(10);
            }

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

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
                            status.startResolutionForResult(mapa.this, REQUEST_CHECK_SETTINGS);
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

    public class JSONTaskNegociosMapa extends AsyncTask<String, String, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            int respuesta = 0;
            String respuesta_horario = "";

            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("latitud", params[1]);
                postDataParams.put("longitud", params[2]);
                postDataParams.put("categoria", params[3]);

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                respuesta = connection.getResponseCode();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    String finalJson = buffer.toString();

                    JSONObject parentObject = new JSONObject(finalJson);
                    JSONArray parentArray = parentObject.getJSONArray("negocios");

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    JSONObject finalObject = null;
                    for (int i = 0; i < parentArray.length(); i++) {
                        finalObject = parentArray.getJSONObject(i);
                        respuesta_horario = finalObject.toString();
                    }
                    return parentArray;
                }else{
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            if (result != null) {
                JSONObject finalObject = null;
                for (int i = 0; i < result.length(); i++) {
                    try {
                        finalObject = result.getJSONObject(i);
                        LatLng ohcmuss = new LatLng(Float.parseFloat(finalObject.getString("Suc_x")), Float.parseFloat(finalObject.getString("Suc_y")));
                        originMarkers.add(mMap.addMarker(new MarkerOptions()
                                .title(finalObject.getString("Cli_razonsocial"))
                                .snippet(finalObject.getString("Suc_direccion")+"&"+finalObject.getString("Suc_telefijo")+"&"+finalObject.getString("Cli_logo"))
                                .position(ohcmuss)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "Sin conección a internet", Toast.LENGTH_SHORT).show();
            }
        }

        public String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while (itr.hasNext()) {

                String key = itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void miUbicacion(View v) {
        if(mMap.getMyLocation() != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
            LatLng hcmus = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 13));
//            mMap.setCenterCoordinate(new LatLngZoom(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude(), 20), true);
        }
    }
}