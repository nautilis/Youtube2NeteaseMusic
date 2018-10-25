package jsonEntity;

import java.util.Map;

/**
 * @author: zpf
 **/
public class ResponseYoutubeItems {

    private ResponseYoutubeSnippet snippet;
    private Map<String, String> resourceId;

    @Override
    public String toString() {
        return "ResponseYoutubeItems{" +
                "snippet=" + snippet +
                ", resourceId=" + resourceId +
                '}';
    }

    public ResponseYoutubeSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(ResponseYoutubeSnippet snippet) {
        this.snippet = snippet;
    }

    public Map<String, String> getResourceId() {
        return resourceId;
    }

    public void setResourceId(Map<String, String> resourceId) {
        this.resourceId = resourceId;
    }
}
