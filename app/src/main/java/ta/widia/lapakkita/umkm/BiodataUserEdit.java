package ta.widia.lapakkita.umkm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class BiodataUserEdit extends AppCompatActivity {

    ImageView logo;
    EditText edtNama, edtNoHp, edtAlamat, edtEmail;
    Button btnSimpan;
    SessionManager session;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"biodata_pelanggan_update.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        //cek permission di android M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        getSupportActionBar().setTitle("Biodata Anda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HashMap<String, String> user = session.getUserDetails();
        final String id_pelanggan = user.get(SessionManager.KEY_ID_UMKM);

        String nm_pelanggan = getIntent().getStringExtra("key_nama");
        String email_pelanggan = getIntent().getStringExtra("key_email");
        String nohp_pelanggan = getIntent().getStringExtra("key_nohp");
        String alamat_pelanggan = getIntent().getStringExtra("key_alamat");
        final String foto_pelanggan = getIntent().getStringExtra("key_foto");

        logo = (ImageView) findViewById(R.id.edt_profil_user);
        //Glide.with(BiodataUserEdit.this).load(foto_pelanggan).into(logo);

        edtNama = (EditText) findViewById(R.id.edt_nama_user);
        edtNama.setText(nm_pelanggan);
        edtNoHp = (EditText) findViewById(R.id.edt_hp_user);
        edtNoHp.setText(nohp_pelanggan);
        edtAlamat = (EditText) findViewById(R.id.edt_alamat_user);
        edtAlamat.setText(alamat_pelanggan);
        edtEmail = (EditText) findViewById(R.id.edt_email_user);
        edtEmail.setText(email_pelanggan);

        btnSimpan = (Button) findViewById(R.id.btn_simpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BiodataUserEdit.this);
                builder.setTitle("Perhatian!");
                builder.setMessage("Yakin ingin menyimpan data?");
                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nama = edtNama.getText().toString();
                        String nohp = edtNoHp.getText().toString();
                        String alamat = edtAlamat.getText().toString();
                        String email = edtEmail.getText().toString();

                        new submitData(id_pelanggan, nama, alamat, email, nohp, foto_pelanggan).execute();
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
    }

    private class submitData extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String psn;

        String id_pelanggan;
        String id, nama, alamat, email, no_hp, foto;

        public submitData(String id_pelanggan, String nama, String alamat, String email, String no_hp, String foto){
            this.id_pelanggan = id_pelanggan;
            this.nama = nama;
            this.alamat = alamat;
            this.email = email;
            this.no_hp = no_hp;
            this.foto = foto;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BiodataUserEdit.this);
            pDialog.setMessage("Submit data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                detail.put("id_pelanggan", id_pelanggan);
                detail.put("nama", nama);
                detail.put("nohp", no_hp);
                detail.put("alamat", alamat);
                detail.put("email", email);
                detail.put("foto", foto);

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        psn = ob.getString("message");
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
                Toast.makeText(BiodataUserEdit.this, ""+psn, Toast.LENGTH_SHORT).show();
            }else{
                finish();
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
