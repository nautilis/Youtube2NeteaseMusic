package jsonEntity;

/**
 * @author: zpf
 **/
public class RequestSearch extends BaseEntity{

    //{s:"red",type:1,limit:30,offset:0}
    private String s;//搜索关键字
    private Integer type;
    private Integer limit;
    private Integer offset;

    public RequestSearch(String s, Integer type, Integer limit, Integer offset) {
        this.s = s;
        this.type = type;
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "RequestSearch{" +
                "s='" + s + '\'' +
                ", type=" + type +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
