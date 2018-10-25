package jsonEntity;

import java.util.List;
import java.util.Map;

/**
 * @author: zpf
 **/
public class ResponseCheck extends BaseEntity{

    private List<Map<String, String>> data;
    private Integer code;

    public List<Map<String, String>> getData() {
        return data;
    }

    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "JsonEntity{" +
                "data=" + data +
                ", code=" + code +
                '}';
    }
}
