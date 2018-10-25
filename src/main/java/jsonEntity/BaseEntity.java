package jsonEntity;

import com.google.gson.Gson;

/**
 * @author: zpf
 **/
public class BaseEntity {

    public String getJson(){
        Gson gson = new Gson();
        String json = gson.toJson(this, this.getClass());
        return json;
    }

}
