package com.cardmovil.cardmovil;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sa.tonisa.tonisa.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn;
    private UserLoginTask mAuthTask = null;
    private EditText mUsuarioView, mPasswordView;
    public String RPTA_msg = "", RPTA_id, RPTA_pais, RPTA_distrito, RPTA_nobmre, RPTA_correo;
    public JSONObject RPTA_usuario = null;
    public String url_login = "", nom, apellido, correo, id;
    private View mProgressView;
    private View mLoginFormView;
    String protocolo = "http://", url;
    TextView tvRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        tvRegistrar = (TextView) findViewById(R.id.tvRegistrar);
        tvRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Registrar.class);
                startActivity(i);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mUsuarioView = (EditText) findViewById(R.id.etUsuario);
        mPasswordView = (EditText) findViewById(R.id.etPassword);

        btn = (Button) findViewById(R.id.btnLogin);
        btn.setOnClickListener(this);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String protocolo = "http://";
//                String ip = getResources().getString(R.string.ipweb);
//                String puerto = getResources().getString(R.string.puertoweb);
//                puerto = puerto.equals("") ? "" : ":" + puerto;
//
//                url = protocolo + ip + puerto + "/pagador/login";
//                new JSONTask().execute(url);
//
//                Intent i = new Intent(MainActivity.this, categorias.class);
//                startActivity(i);
//            }
//        });

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheckLoc = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionLlamada = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheckLoc != PackageManager.PERMISSION_GRANTED || permissionLlamada != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE}, 10);
        }
    }

    @Override
    public void onClick(View v) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsuarioView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsuarioView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("La contraseña es muy corta");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsuarioView.setError("Este campo es Obligatorio");
            focusView = mUsuarioView;
            cancel = true;
        } else if (!isPasswordValid(email)) {
            mUsuarioView.setError("Este campo es Obligatorio");
            focusView = mUsuarioView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);


            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
//
//            // TODO: register the new account here.
//            return true;
            Boolean rpta = false;
            try {
                final String resultado = enviarDatosGET(mEmail, mPassword);
                rpta = obtDatosJSON(resultado) == 1 ? true : false;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("no se puede conectar: ");
                RPTA_msg = "No se puede acceder al Servidor";
            }

            return rpta;
        }

        public String enviarDatosGET(String usu, String pas) {
            String ip = getResources().getString(R.string.ipweb);
            String puerto = getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;

            String ruta = protocolo + ip + puerto + "/login/login_check";

            URL url = null;
            String linea = "";
            int respuesta = 0;
            StringBuilder result = null;

            try {
                url = new URL(ruta);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("username", usu);
                postDataParams.put("password", pas);

                HttpURLConnection conection = (HttpURLConnection) url.openConnection();
                conection.setReadTimeout(15000 /* milliseconds */);
                conection.setConnectTimeout(15000 /* milliseconds */);
                conection.setRequestMethod("POST");
                conection.setDoInput(true);
                conection.setDoOutput(true);

                OutputStream os = conection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                respuesta = conection.getResponseCode();

                result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {
                    InputStream in = new BufferedInputStream(conection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    while ((linea = reader.readLine()) != null) {
                        result.append(linea);
                    }
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("URL mal formateada");
            } catch (ConnectException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "No se puede tener Acceso al Servidor " + ip + puerto, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        public int obtDatosJSON(String response) {
            int res = 0;
            try {
//            JSONArray json = new JSONArray(response);
//            if (json.length() > 0 ){
//                res = 1;
//            }
                JSONObject jsonRpta = new JSONObject(response);
                Boolean ok = jsonRpta.getBoolean("status");
                RPTA_msg = jsonRpta.getString("menssage");
                RPTA_id = jsonRpta.getString("usuario");
                RPTA_pais = jsonRpta.getString("pais");
                RPTA_distrito = jsonRpta.getString("distrito");
                RPTA_nobmre = jsonRpta.getString("nombre");
                RPTA_correo = jsonRpta.getString("correo");

                return ok ? 1 : 0;
            } catch (Exception e) {
                System.out.println("error al Parsear JSON");
            }
            return res;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
                try {
                    Intent i = new Intent(MainActivity.this, categorias.class);
                    Bundle bolsa = new Bundle();
                    bolsa.putString("usuario", RPTA_id);
                    bolsa.putString("pais", RPTA_pais);
                    bolsa.putString("distrito", RPTA_distrito);
                    bolsa.putString("nombre", RPTA_nobmre);
                    bolsa.putString("correo", RPTA_correo);
                    i.putExtras(bolsa);
                    startActivity(i);

                } catch (Exception e) {
                    System.out.println("Error al leer el JSON RPTA_usuario");
                }

            } else {
                mPasswordView.setError(RPTA_msg);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
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

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
