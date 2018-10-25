package jsonEntity;

/**
 * @author: zpf
 **/
public class QueryUserPlayLists extends BaseEntity {

    //{ uid: '92591332', limit: 30, offset: 0 }
    private String uid;
    private Integer limit;
    private Integer offset;

    public QueryUserPlayLists(String uid, Integer limit, Integer offset) {
        this.uid = uid;
        this.limit = limit;
        this.offset = offset;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
