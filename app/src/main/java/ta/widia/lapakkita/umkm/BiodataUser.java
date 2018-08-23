package ta.widia.lapakkita.umkm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ta.widia.lapakkita.R;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;
import ta.widia.lapakkita.umkm.util.SessionManager;

public class BiodataUser extends AppCompatActivity {

    ImageView logo;
    TextView txtNama, txtNoHp, txtAlamat, txtEmail;
    Button btnLogout;
    SessionManager session;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"biodata_umkm.php";
    String link_foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //cek permission di android M
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        getSupportActionBar().setTitle("Biodata Anda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HashMap<String, String> user = session.getUserDetails();
        String id_pelanggan = user.get(SessionManager.KEY_ID_UMKM);
        //final String foto_pelanggan = user.get(SessionManager.KEY_FOTO_UMKM);
        /*String nm_pelanggan = user.get(SessionManager.KEY_NM_UMKM);
        String email_pelanggan = user.get(SessionManager.KEY_MAIL_UMKM);
        String nohp_pelanggan = user.get(SessionManager.KEY_NOHP_UMKM);
        String alamat_pelanggan = user.get(SessionManager.KEY_ALAMAT_UMKM);
        String foto_pelanggan = user.get(SessionManager.KEY_FOTO_UMKM);*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_edit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(BiodataUser.this, BiodataUserEdit.class);
                intent.putExtra("key_nama", txtNama.getText().toString());
                intent.putExtra("key_nohp", txtNoHp.getText().toString());
                intent.putExtra("key_alamat", txtAlamat.getText().toString());
                intent.putExtra("key_email", txtEmail.getText().toString());
                intent.putExtra("key_foto", link_foto);
                startActivity(intent);
            }
        });

        logo = (ImageView) findViewById(R.id.profil_user);

        txtNama = (TextView) findViewById(R.id.nama_user);
        //txtNama.setText(nm_pelanggan);
        txtNoHp = (TextView) findViewById(R.id.hp_user);
        //txtNoHp.setText(nohp_pelanggan);
        txtAlamat = (TextView) findViewById(R.id.alamat_user);
        //txtAlamat.setText(alamat_pelanggan);
        txtEmail = (TextView) findViewById(R.id.email_user);
        //txtEmail.setText(email_pelanggan);

        btnLogout = (Button) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BiodataUser.this);
                builder.setTitle("Anda akan logout");
                builder.setMessage("Yakin ingin logout dari aplikasi?");
                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        session.logoutUser();
                        Intent intent = new Intent(BiodataUser.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);

                        //terus tutup activity ini
                        finish();
                    }
                });

                builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setCancelable(false);
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
        });

        //new ambilData(id_pelanggan).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, String> user = session.getUserDetails();
        String id_pelanggan = user.get(SessionManager.KEY_ID_UMKM);
        new ambilData(id_pelanggan).execute();
    }

    private class ambilData extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String psn;

        String id_pelanggan;
        String id, nama, alamat, email, no_hp, foto;

        public ambilData(String id_pelanggan){
            this.id_pelanggan = id_pelanggan;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BiodataUser.this);
            pDialog.setMessage("Memuat data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                //detail.put("id_pelanggan", id_pelanggan);

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url+"?id_pelanggan="+id_pelanggan,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        JSONArray products = ob.getJSONArray("field");

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            id = c.getString("id_pelanggan");
                            nama = c.getString("nama_pelanggan");
                            alamat = c.getString("alamat_pelanggan");
                            email = c.getString("email_pelanggan");
                            no_hp = c.getString("no_hp_pelanggan");
                            foto  = c.getString("foto");
                            link_foto = foto;

                        }
                    } else {
                        // no data found
                        psn = ob.getString("message");

                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();

            if(scs == 0){
                Toast.makeText(BiodataUser.this, ""+psn, Toast.LENGTH_SHORT).show();
            }else{
                //set
                txtNama.setText(nama);
                txtNoHp.setText(no_hp);
                txtAlamat.setText(alamat);
                txtEmail.setText(email);
                //Glide.with(BiodataUser.this).load(foto).into(logo);
            }
        }

    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * Permission
    * */

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                        //do task
                        //...
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    /*End permission*/
}
