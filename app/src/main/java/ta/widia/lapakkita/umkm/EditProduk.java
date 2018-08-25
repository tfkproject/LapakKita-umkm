package ta.widia.lapakkita.umkm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ta.widia.lapakkita.R;
import ta.widia.lapakkita.umkm.model.ItemKategori;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;
import ta.widia.lapakkita.umkm.util.SessionManager;

public class EditProduk extends AppCompatActivity {

    EditText edtNama, edtHarga, edtBerat, edtStok, edtDesk;
    Button btnGambar1, btnGambar2, btnGambar3, btnGambar4, btnSubmit;
    TextView txtLokasi;
    ImageView imgProduk;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"produk_edit.php";
    SessionManager session;
    int PLACE_PICKER_REQUEST    =   2;
    public String timestamp, id_kategori;

    Uri image1, image2, image3, image4;

    int RESULT_SELECT_IMAGE1 = 1;
    /*int RESULT_SELECT_IMAGE2 = 2;
    int RESULT_SELECT_IMAGE3 = 3;
    int RESULT_SELECT_IMAGE4 = 4;*/

    private Spinner listKat;
    private SpinAdapter spinAdapter;

    List<ItemKategori> items;

    private static String url_kat = Config.HOST+"kategori.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_produk);

        getSupportActionBar().setTitle("Edit Produk Anda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //cek permission di android M
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        final String id_umkm = user.get(SessionManager.KEY_ID_UMKM);

        final String id_produk = getIntent().getStringExtra("key_id_produk");
        String nama = getIntent().getStringExtra("key_nama");
        String harga = getIntent().getStringExtra("key_harga");
        String stok = getIntent().getStringExtra("key_stok");
        String berat = getIntent().getStringExtra("key_berat");
        String desk = getIntent().getStringExtra("key_desk");
        String link_gambar = getIntent().getStringExtra("key_gambar");

        imgProduk = (ImageView) findViewById(R.id.img_produk);
        Glide.with(EditProduk.this).load(link_gambar).into(imgProduk);

        listKat = (Spinner) findViewById(R.id.list_kat);
        listKat.setPrompt("Kategori");

        items = new ArrayList<>();

        new listKategori().execute();

        spinAdapter = new SpinAdapter(EditProduk.this,
                android.R.layout.simple_spinner_item,
                items);
        listKat.setAdapter(spinAdapter);
        spinAdapter.notifyDataSetChanged();

        edtNama = (EditText) findViewById(R.id.edt_nama);
        edtHarga = (EditText) findViewById(R.id.edt_harga);
        edtStok = (EditText) findViewById(R.id.edt_stok);
        edtBerat = (EditText) findViewById(R.id.edt_berat);
        edtDesk = (EditText) findViewById(R.id.edt_desk);

        edtNama.setText(nama);
        edtHarga.setText(harga);
        edtStok.setText(stok);
        edtBerat.setText(berat);
        edtDesk.setText(Html.fromHtml(desk));

        btnGambar1 = (Button) findViewById(R.id.btn_gmbr1);
        /*btnGambar2 = (Button) findViewById(R.id.btn_gmbr2);
        btnGambar3 = (Button) findViewById(R.id.btn_gmbr3);
        btnGambar4 = (Button) findViewById(R.id.btn_gmbr4);*/

        btnGambar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pilihGambar1();
            }
        });

        /*btnGambar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pilihGambar2();
            }
        });

        btnGambar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pilihGambar3();
            }
        });

        btnGambar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pilihGambar4();
            }
        });*/

        btnSubmit = (Button) findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String nama = edtNama.getText().toString();
                String harga = edtHarga.getText().toString();
                String berat = edtBerat.getText().toString();
                String stok = edtStok.getText().toString();
                String desk = edtDesk.getText().toString();

                new editProduk(id_produk, id_umkm, id_kategori, nama, desk, berat, harga, stok).execute();

            }
        });
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

    private void pilihGambar1() {
        //open album untuk pilih image
        Intent gallaryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallaryIntent, RESULT_SELECT_IMAGE1);
    }

    /*private void pilihGambar2() {
        //open album untuk pilih image
        Intent gallaryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallaryIntent, RESULT_SELECT_IMAGE2);
    }

    private void pilihGambar3() {
        //open album untuk pilih image
        Intent gallaryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallaryIntent, RESULT_SELECT_IMAGE3);
    }

    private void pilihGambar4() {
        //open album untuk pilih image
        Intent gallaryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallaryIntent, RESULT_SELECT_IMAGE4);
    }*/

    private class listKategori extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String id_kategori, kategori_produk, psn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url_kat, dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        JSONArray products = ob.getJSONArray("field");

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            id_kategori = c.getString("id_kategori");
                            kategori_produk = c.getString("kategori_produk");

                            items.add(new ItemKategori(id_kategori, kategori_produk));
                        }
                    } else {
                        // no data found
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
            //pDialog.dismiss();
        }

    }

    private class editProduk extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String psn;

        String id_produk, id_umkm, id_kategori, nama_produk, deskripsi_produk, berat_produk, harga_produk, stok_produk;

        public editProduk(
                String id_produk,
                String id_umkm,
                String id_kategori,
                String nama_produk,
                String deskripsi_produk,
                String berat_produk,
                String harga_produk,
                String stok_produk){
            this.id_produk = id_produk;
            this.id_umkm = id_umkm;
            this.id_kategori = id_kategori;
            this.nama_produk = nama_produk;
            this.deskripsi_produk = deskripsi_produk;
            this.berat_produk = berat_produk;
            this.harga_produk = harga_produk;
            this.stok_produk = stok_produk;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditProduk.this);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                detail.put("id_produk", id_produk);
                detail.put("id_umkm", id_umkm);
                detail.put("id_kategori", id_kategori);
                detail.put("nama_produk", nama_produk);
                detail.put("deskripsi_produk", deskripsi_produk);
                detail.put("berat_produk", berat_produk);
                detail.put("harga_produk", harga_produk);
                detail.put("stok_produk", stok_produk);

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
                Toast.makeText(EditProduk.this, ""+psn, Toast.LENGTH_SHORT).show();
            }else{
                //tutup activity ini
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

    public class SpinAdapter extends ArrayAdapter<ItemKategori>{

        // Your sent context
        private Context context;
        // Your custom values for the spinner (User)
        private List<ItemKategori> values;

        public SpinAdapter(Context context, int textViewResourceId,
                           List<ItemKategori> values) {
            super(context, textViewResourceId, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public int getCount(){
            return values.size();
        }

        @Override
        public ItemKategori getItem(int position){
            return values.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }


        // And the "magic" goes here
        // This is for the "passive" state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
            TextView label = (TextView) super.getView(position, convertView, parent);

            // Then you can get the current item using the values array (Users array) and the current position
            // You can NOW reference each method you has created in your bean object (User class)
            label.setText(values.get(position).getId_kategori());

            // And finally return your dynamic (or custom) view for each spinner item
            id_kategori = values.get(position).getId_kategori();
            return label;
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);

            label.setText(values.get(position).getNama_kategori());

            return label;
        }
    }

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
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
}
