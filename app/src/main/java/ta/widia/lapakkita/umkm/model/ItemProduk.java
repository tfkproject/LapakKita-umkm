package ta.widia.lapakkita.umkm.model;

/**
 * Created by taufik on 21/05/18.
 */

public class ItemProduk {
    String id, url_gambar, nama_pdk, berat, harga;

    public ItemProduk(String id, String url_gambar, String nama_pdk, String berat, String harga){
        this.id = id;
        this.url_gambar = url_gambar;
        this.nama_pdk = nama_pdk;
        this.berat = berat;
        this.harga = harga;
    }

    public String getId() {
        return id;
    }

    public String getUrl_gambar() {
        return url_gambar;
    }

    public String getNama_pdk() {
        return nama_pdk;
    }

    public String getBerat() {
        return berat;
    }

    public String getHarga() {
        return harga;
    }
}
