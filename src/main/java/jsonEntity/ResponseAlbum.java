package jsonEntity;

/**
 * @author: zpf
 **/
public class ResponseAlbum {

    private Integer id;
    private String name;
    private Long publishTime;
    private ResponseArtists artist;

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

    public Long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }

    public ResponseArtists getArtist() {
        return artist;
    }

    public void setArtist(ResponseArtists artist) {
        this.artist = artist;
    }
}
