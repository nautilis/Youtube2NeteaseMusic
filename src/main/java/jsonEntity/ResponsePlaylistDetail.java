package jsonEntity;

import java.util.List;
import java.util.Map;

/**
 * @author: zpf
 **/
public class ResponsePlaylistDetail {

    private List<Map<String, Integer>> trackIds;

    public List<Map<String, Integer>> getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(List<Map<String, Integer>> trackIds) {
        this.trackIds = trackIds;
    }
}
