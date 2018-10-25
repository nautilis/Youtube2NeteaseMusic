package jsonEntity;

import java.util.List;

/**
 * @author: zpf
 **/
public class ResponseUserPlayLists {

    private List<ResponsePlaylist> playlist;

    public List<ResponsePlaylist> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<ResponsePlaylist> playlist) {
        this.playlist = playlist;
    }


}
