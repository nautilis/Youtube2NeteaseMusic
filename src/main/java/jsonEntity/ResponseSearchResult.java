package jsonEntity;

import java.util.List;

/**
 * @author: zpf
 **/
public class ResponseSearchResult {

    private List<ResponseSong> songs;

    public List<ResponseSong> getSongs() {
        return songs;
    }

    public void setSongs(List<ResponseSong> songs) {
        this.songs = songs;
    }
}
