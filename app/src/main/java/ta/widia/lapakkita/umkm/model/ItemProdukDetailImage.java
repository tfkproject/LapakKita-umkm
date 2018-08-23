package ta.widia.lapakkita.umkm.model;

/**
 * Created by taufik on 21/05/18.
 */

public class ItemProdukDetailImage {

    String url_gambar, gambar_ke;

    public ItemProdukDetailImage(String url_gambar, String gambar_ke){
        this.url_gambar = url_gambar;
        this.gambar_ke = gambar_ke;
    }

    public String getUrl_gambar() {
        return url_gambar;
    }

    public String getGambar_ke() {
        return gambar_ke;
    }
}
