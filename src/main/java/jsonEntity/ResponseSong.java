package jsonEntity;

import java.util.List;

/**
 * @author: zpf
 **/
public class ResponseSong {

    private Integer id;
    private String name;
    private List<ResponseArtists> artists;
    private ResponseAlbum album;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResponseArtists> getArtists() {
        return artists;
    }

    public void setArtists(List<ResponseArtists> artists) {
        this.artists = artists;
    }

    public ResponseAlbum getAlbum() {
        return album;
    }

    public void setAlbum(ResponseAlbum album) {
        this.album = album;
    }
}

