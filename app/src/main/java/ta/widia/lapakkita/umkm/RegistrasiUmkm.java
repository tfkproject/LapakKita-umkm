package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

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

public class RegistrasiUmkm extends AppCompatActivity {

    EditText edtNamaPemilik, edtNamaUmkm, edtNoktp, edtPass, edtAlmt, edtNohp, edtEmail;
    Button btnLokasi, btnReg;
    TextView txtLokasi;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"regis_umkm.php";
    SessionManager session;
    int PLACE_PICKER_REQUEST    =   2;
    public String lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);

        getSupportActionBar().setTitle("Register Akun");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(getApplicationContext());

        ////
        edtNamaPemilik = (EditText) findViewById(R.id.edt_nama_pmlk);
        edtNamaUmkm = (EditText) findViewById(R.id.edt_nama_umkm);
        edtNoktp = (EditText) findViewById(R.id.edt_noktp);
        edtAlmt= (EditText) findViewById(R.id.edt_almt);
        edtNohp = (EditText) findViewById(R.id.edt_nohp);
        edtEmail= (EditText) findViewById(R.id.edt_email);
        edtPass= (EditText) findViewById(R.id.edt_pass);
        ////


        txtLokasi = (TextView) findViewById(R.id.txt_lokasi);

        btnLokasi = (Button) findViewById(R.id.btn_lokasi);
        btnLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlacePicker.IntentBuilder builder   =   new PlacePicker.IntentBuilder();
                Intent intent;
                try {
                    intent  =   builder.build(RegistrasiUmkm.this);
                    startActivityForResult(intent,PLACE_PICKER_REQUEST );
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btnReg = (Button) findViewById(R.id.btn_reg);
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nama_pemilik = edtNamaPemilik.getText().toString();
                String nama_umkm = edtNamaUmkm.getText().toString();
                String noktp = edtNoktp.getText().toString();
                String alamat = edtAlmt.getText().toString();
                String nohp = edtNohp.getText().toString();
                String email = edtEmail.getText().toString();
                String pass = edtPass.getText().toString();

                new prosesDaftar(nama_pemilik, nama_umkm, noktp, alamat, nohp, email, pass, lat, lon).execute();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == PLACE_PICKER_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                Place place =   PlacePicker.getPlace(data,RegistrasiUmkm.this);
                Double latitude = place.getLatLng().latitude;
                Double longitude = place.getLatLng().longitude;
                lat = String.valueOf(latitude);
                lon = String.valueOf(longitude);
                String address = "Lat: "+String.valueOf(latitude)+"\nLon: "+String.valueOf(longitude);
                txtLokasi.setText(address);
            }
        }
    }

    private class prosesDaftar extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String psn;

        String nama_pemilik, nama_umkm, no_ktp, alamat, no_hp, email, password,lat, lon;

        public prosesDaftar(
                String nama_pemilik,
                String nama_umkm,
                String no_ktp,
                String alamat,
                String no_hp,
                String email,
                String password,
                String lat,
                String lon){
            this.nama_pemilik = nama_pemilik;
            this.nama_umkm = nama_umkm;
            this.no_ktp = no_ktp;
            this.alamat = alamat;
            this.no_hp = no_hp;
            this.email = email;
            this.password = password;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegistrasiUmkm.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                detail.put("nama_pemilik", nama_pemilik);
                detail.put("nama_umkm", nama_umkm);
                detail.put("no_ktp", no_ktp);
                detail.put("alamat", alamat);
                detail.put("no_hp", no_hp);
                detail.put("email", email);
                detail.put("password", password);
                detail.put("lat", lat);
                detail.put("lon", lon);

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject c = new JSONObject(response);
                    scs = c.getInt("success");

                    if (scs == 1) {
                        psn = c.getString("message");

                        // Storing each json item in variable
                        String id_umkm = c.getString("id_umkm");
                        String nm_pmlk = c.getString("nama_pemilik");
                        String nm_umkm = c.getString("nama_umkm");
                        String noktp = c.getString("no_ktp");
                        String alamat = c.getString("alamat_umkm");
                        String nohp = c.getString("no_hp");
                        String email = c.getString("email");
                        String desk = c.getString("deskripsi");
                        String logo = c.getString("logo");
                        String lat = c.getString("lat");
                        String lon = c.getString("lon");

                        //buat sesi login
                        session.createLoginSession(
                                id_umkm,
                                nm_pmlk,
                                nm_umkm,
                                noktp,
                                alamat,
                                nohp,
                                email,
                                desk,
                                logo,
                                lat,
                                lon);
                    } else {
                        // no data found
                        psn = c.getString("message");
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
                Toast.makeText(RegistrasiUmkm.this, ""+psn, Toast.LENGTH_SHORT).show();
            }else{
                //tutup activity ini
                finish();

                Intent intent = new Intent(RegistrasiUmkm.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
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

}
