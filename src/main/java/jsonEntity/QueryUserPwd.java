package jsonEntity;

import com.google.gson.Gson;

/**
 * @author: zpf
 **/
public class QueryUserPwd extends BaseEntity{

    private String username;
    private String password;
    private String rememberLogin;

    public QueryUserPwd(String username, String password, String rememberLogin) {
        this.username = username;
        this.password = password;
        this.rememberLogin = rememberLogin;
    }

    public static void main(String[] args) {
        QueryUserPwd query = new QueryUserPwd("bevanzpf@gmail.com", "123456", "true");
        String json = query.getJson();
        System.out.println(json);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRememberLogin() {
        return rememberLogin;
    }

    public void setRememberLogin(String rememberLogin) {
        this.rememberLogin = rememberLogin;
    }
}
