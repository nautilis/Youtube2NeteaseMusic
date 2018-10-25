package jsonEntity;
import java.util.List;
import java.util.Map;

/**
 * @author: zpf
 **/
public class ResponseYoutubePlaylist extends BaseEntity {

    private Map<String,Integer> pageInfo;
    private String nextPageToken;
    private List<ResponseYoutubeItems> items;

    public Map<String, Integer> getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(Map<String, Integer> pageInfo) {
        this.pageInfo = pageInfo;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<ResponseYoutubeItems> getItems() {
        return items;
    }

    public void setItems(List<ResponseYoutubeItems> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ResponseYoutubePlaylist{" +
                "pageInfo=" + pageInfo +
                ", nextPageToken='" + nextPageToken + '\'' +
                ", items=" + items +
                '}';
    }
}
