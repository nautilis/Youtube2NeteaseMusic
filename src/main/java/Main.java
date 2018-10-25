import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jsonEntity.*;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import tools.Encrypt;
import tools.MConstants;
import tools.MRequest;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author: zpf
 **/
public class Main {

    private static final String STATIC_DIR = System.getProperty("user.dir") + "/";
    private static final Logger logger = Logger.getLogger("Main.class");
    private static final String YOUTUBE_KEY = "AIzaSyD7UqT5d_iZzz7B4K8oYgWPzVc5h0FZazw";
    private static final String YOUTUBE_LIST = "PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth";
    private static final String MUSIC_LIST = "youtube this week";
    private static final Gson gson = new Gson();

    /**
     * GetCookie: 访问文件获取cookie文本
     */
    public String getFileContent(String path, String fileStr) {

        Path file = Paths.get(path + fileStr);
        Charset charset = Charset.forName("UTF-8");
        StringBuffer sb = new StringBuffer();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
            return "";
        }
        return new String(sb);

    }

    /**
     * SaveCookie: 生成路径，保存cookie
     */
    public void save2file(String path, String file, String content) {

        Path p = Paths.get(path + file);
        byte[] data = content.getBytes();

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }

    }

    /**
     * 判断是否存在文件目录是否存在
     */
    public boolean createDirIfNotExisted(String path) {
        File file = new File(path);
        boolean existed = false;
        if (file.exists()) {
            existed = true;
            System.out.println("file existed");
        } else {
            int index = path.lastIndexOf("/");
            File dir = new File(path.substring(0, index));
            dir.mkdirs();
        }
        return existed;
    }


    /**
     * Login: 先查看是否存在cookie文件，如果存在直接返回cookie字符串，不存在则登录并保存cookie
     */
    public boolean login(String username, String password) {

        try {
            QueryUserPwd userPwd = new QueryUserPwd(username, password, "true");
            String json = userPwd.getJson();
            Map<String, String> paramAndEncSecKey = Encrypt.getParamAndEncSecKey(json);
            UrlEncodedFormEntity urlEncodedFormEntity = MRequest.getUrlEncodedFormEntity(paramAndEncSecKey);
            String url = "http://music.163.com/weapi/login";
            HttpResponse response = MRequest.query("POST", url, new HashMap<>(), urlEncodedFormEntity, 2);
            String accountJson = EntityUtils.toString(response.getEntity());
            System.out.println(accountJson);
            Gson gson = new Gson();
            Type type = new TypeToken<ResponseUserInfo>() {
            }.getType();
            ResponseUserInfo userInfo = gson.fromJson(accountJson, type);
            ResponseAccount account = userInfo.getAccount();
            Integer id = account.getId();
            save2file(STATIC_DIR, MConstants.USER_INFO_FILE_NAME, String.valueOf(id));

            Header[] headers = response.getHeaders("Set-Cookie");
            StringBuffer sb = new StringBuffer();
            for (Header header : headers) {
                String cookie = header.toString().replaceFirst("Set-Cookie: ", "");
                sb.append(";" + cookie);
            }
            String cookieStr = sb.toString();
            String[] cookieList = cookieStr.split(";");
            List<String> usefulCookie = new ArrayList<>();
            for (String cookie : cookieList) {
                String[] kv = cookie.split("=");
                String[] noneed = {"Domain", "Expires", "Path"};
                Set noneedSet = new HashSet(Arrays.asList(noneed));
                if (kv.length != 2 || noneedSet.contains(kv[0].trim())) {
                    continue;
                }
                usefulCookie.add(cookie);
            }
            String cookie = String.join(";", usefulCookie);
            save2file(STATIC_DIR, MConstants.COOKIE_FILE_NAME, cookie);

        } catch (Exception ex) {
            logger.info("登录失败");
            ex.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * Youtubuapi： 获取获取指定playlist的数据，返回ResponseXXX对象
     */
    public ResponseYoutubePlaylist getYoutubePlayList(String playlistId, String pageToken) {
        String baseUrl = "https://www.googleapis.com/youtube/v3/playlistItems?playlistId=%s&maxResults=20&part=snippet,id&key=%s" ;
        if(pageToken != null){
            baseUrl += "&pageToken=" + pageToken;
        }
        String url = String.format(baseUrl, playlistId, YOUTUBE_KEY);
        ResponseYoutubePlaylist playlist = null;
        try {
            HttpResponse response= MRequest.query("GET", url, new HashMap<>(), null, 2);
            String json = EntityUtils.toString(response.getEntity());
            //StringBuffer json = new StringBuffer("{\"kind\":\"youtube#playlistItemListResponse\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/Eg7dtyoCXRB8gnH5kTbluFhY_tk\\\"\",\"nextPageToken\":\"CBQQAA\",\"pageInfo\":{\"totalResults\":99,\"resultsPerPage\":20},\"items\":[{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/Rm4DnAhNEkohowP6T0zRLxoloig\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC5GMDM3NjU4MzYyNjlBNzcw\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Ariana Grande - everytime (Audio)\",\"description\":\"Music video by Ariana Grande performing everytime (Audio). © 2018 Republic Records, a Division of UMG Recordings, Inc.\\n\\nhttp://vevo.ly/GIRyzp\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/MYvjW3zdCws/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/MYvjW3zdCws/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/MYvjW3zdCws/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/MYvjW3zdCws/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/MYvjW3zdCws/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":0,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"MYvjW3zdCws\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/QcIhh5UxSathDiDX2d1MtE3gTDc\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC4xMkUzNDdCRjI5Mjk0MkVG\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Major Lazer - Blow That Smoke (Feat. Tove Lo) (Official Music Video)\",\"description\":\"Official Music Video | Major Lazer - Blow That Smoke (Feat. Tove Lo)\\n\\nLyrics: \\nBlow that smoke\\nDarkness bring out our emotions\\nMix em up with night life potions\\nI loose my head for you\\nCountless times we catch the sunrise\\nFix em scars we share with white lies\\nI loose my head for you\\n\\nI got the keys to heaven now\\nBaes all around and they got my mind spinning\\nSuddenly doors just open wide\\n\\nBlow that smoke and let me love that fire \\nI don't need no memories \\nBed is broken now I'm floating higher \\nThis madness so good for me\\n\\nSo good so good for me\\n\\nPretty pictures keep me dreaming\\n(I loose my head for you)\\nLivin big but still deceiving \\n(I loose my head for you)\\nI’m in deep and I don’t mind\\nI want that moment one of a kind \\n\\n\\nI got the keys to heaven now\\nBaes all around and they got my mind spinning\\nSuddenly doors just open wide\\n\\nBlow that smoke and let me love that fire \\nI don't need no memories \\nBed is broken now I'm floating higher \\nThis madness so good for me\\n\\nSo good so good for me\\n\\nLove that fire\\nMemories\\nFloatin higher\\nGood for me\\n\\nBlow that smoke and let me love that fire \\nI don't need no memories \\nBed is broken now I'm floating higher \\nThis madness so good for me\\n\\nSo good so good for me\\n\\nDirected by: dad®\\n \\nChoreographer: Calvit Hodge\\nLazer Gyals - Sara Bivens, Andranita Smith Shannon, Jaylene Mendoza, Helen Gedlu\\n\\n#MajorLazer\\n#ToveLo\\n#BlowThatSmoke\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/b63IPrrdPOU/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/b63IPrrdPOU/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/b63IPrrdPOU/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/b63IPrrdPOU/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/b63IPrrdPOU/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":1,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"b63IPrrdPOU\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/32rPpviubqRxGPELZw_pEC6XQiw\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC40NjFDMDA4QjYyM0NENzMx\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Cardi B - Money (Official Audio)\",\"description\":\"Cardi B - \\\"Money\\\" (Official Audio)\\nStream/Download: https://CardiB.lnk.to/moneyID\\n\\nSubscribe for more official content from Cardi B: https://CardiB.lnk.to/Subscribe\\n\\nFollow Cardi B\\nhttp://cardibofficial.com\\nhttp://Twitter.com/IAmCardiB\\nhttp://Facebook.com/IAmCardiB\\nhttp://Instagram.com/IAmCardiB\\nhttp://Soundcloud.com/IAmCardiB\\n\\nExclusive Bardi Gang merchandise available here: http://smarturl.it/BardiGangMerchYT\\n\\nThe official YouTube channel of Atlantic Records artist Cardi B. Subscribe for the latest music videos, performances, and more.\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/Zj2cK8wymIA/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/Zj2cK8wymIA/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/Zj2cK8wymIA/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/Zj2cK8wymIA/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/Zj2cK8wymIA/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":2,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"Zj2cK8wymIA\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/i6eYM7LRPbFn76uWhsEUWfLNSA0\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC4xQzk5OEVBNzAzQzg4OTI0\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"P!nk - A Million Dreams [from The Greatest Showman: Reimagined]\",\"description\":\"Download/Stream Now: https://Atlantic.lnk.to/TGS_ReimaginedID\\nP!nk - A Million Dreams [from The Greatest Showman: Reimagined]\\n\\nPre-Order The Greatest Showman: Reimagined now: https://Atlantic.lnk.to/TGS_ReimaginedID\\n\\n'The Greatest Showman Soundtrack' available now:\\nDownload/Stream - https://atlantic.lnk.to/TheGreatestShowmanID\\n \\nSubscribe for more official content from Atlantic Records:\\nhttps://Atlantic.lnk.to/subscribe\\n\\nFollow The Greatest Showman\\nhttps://twitter.com/GreatestShowman\\nhttps://facebook.com/GreatestShowman\\nhttps://instagram.com/greatestshowman\\n\\nFollow Atlantic Records\\nhttps://facebook.com/atlanticrecords\\nhttps://instagram.com/atlanticrecords\\nhttps://twitter.com/AtlanticRecords\\nhttp://atlanticrecords.com \\n\\nThe official Atlantic Records YouTube Channel is home to the hottest in hip-hop, rock, pop, R&B, indie, musicals and soundtracks. With over 70 years of global recorded music history, Atlantic Records’ legacy and passion for artistry continues with Top 40 hitmakers like Wiz Khalifa, Sean Paul, Trey Songz, Bruno Mars, Charlie Puth, Janelle Monáe, and B.o.B. \\n\\nAtlantic Records prides itself for working on Motion Picture Soundtracks, such as “The Greatest Showman”, “Suicide Squad” and “The Fate of the Furious.” As well as Musical Soundtracks including “Dear Evan Hansen,” and Grammy and Tony Award Winning “Hamilton”.\\n \\nIt is home to world-renowned record labels representing music from every genre, including Asylum, Big Beat, Canvasback, Elektra, Fueled By Ramen, Rhino, Roadrunner, and Sire.\\n \\nSubscribe for the latest official music videos, official audio videos, performances, bts and more from our artists and projects.\\nhttps://Atlantic.lnk.to/subscribe\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/TJ9GYswlzsI/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/TJ9GYswlzsI/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/TJ9GYswlzsI/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/TJ9GYswlzsI/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/TJ9GYswlzsI/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":3,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"TJ9GYswlzsI\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/JnJnK5n2zf1rIzQNZAcrUYfI7LA\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC45NTMyMkNFM0Q2QTAwRDlF\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Imagine Dragons - Zero (From the Original Motion Picture \\\"Ralph Breaks The Internet\\\")\",\"description\":\"Listen to \\\"\\\"Zero (From the Original Motion Picture \\\"\\\"Ralph Breaks The Internet\\\"\\\"),\\\"\\\" out now: http://smarturl.it/IDZero\\n\\nPre-order the upcoming album \\\"\\\"Origins,\\\"\\\" out 11/9: http://smarturl.it/OriginsID\\n\\nShop Imagine Dragons: http://smarturl.it/ImagineDragonsShop\\nSign up for email updates: http://smarturl.it/ID_Email\\nListen to Imagine Dragons on Spotify: http://smarturl.it/ID_Discography\\nCatch Imagine Dragons on tour: http://imaginedragonsmusic.com/tour\\n\\nFollow Imagine Dragons:\\nFacebook: https://www.facebook.com/ImagineDragons\\nTwitter: https://twitter.com/Imaginedragons\\nInstagram: https://www.instagram.com/imaginedragons\\n\\nLYRICS\\n\\nI find it hard to say the things I want to say the most\\nFind a little bit of steady as I get close\\nFind a balance in the middle of the chaos\\nSend me low, send me high, send me never demigod\\nI remember walking in the cold of November\\nHoping that I make it to the end of December\\n27 years and the end on my mind\\nBut holding to the thought of another time\\nBut looking to the ways of the ones before me\\nLooking for the path of the young and lonely\\nI don't want to hear about what to do\\nI don't want to do it just to do it for you\\n\\nHello, hello\\nLet me tell you what it's like to be a zero, zero\\nLet me show you what it's like to always feel, feel\\nLike I'm empty and there's nothing really real, real\\nI'm looking for a way out\\n\\nHello, hello\\nLet me tell you what it's like to be a zero, zero\\nLet me show you what it's like to never feel, feel\\nLike I'm good enough for anything that's real, real\\nI'm looking for a way out\\n\\nI find it hard to tell you how I want to run away\\nI understand it always makes you feel a certain way\\nI find a balance in the middle of the chaos\\nSend me up, send me down\\nSend me never demigod\\n\\nI remember walking in the heat of the summer\\nWide eyed one with a mind full of wonder\\n27 years and I've nothing to show\\nFalling from the dove to the dark of the crow\\nLooking to the ways of the ones before me\\nLooking for a path of the young and lonely\\nI don't want to hear about what to do, no\\nI don't want to do it just to do it for you\\n\\nHello, hello\\nLet me tell you what it's like to be a zero, zero\\nLet me show you what it's like to always feel, feel\\nLike I'm empty and there's nothing really real, real\\nI'm looking for a way out\\n\\nHello, hello\\nLet me tell you what it's like to be a zero, zero\\nLet me show you what it's like to never feel, feel\\nLike I'm good enough for anything that's real, real\\nI'm looking for a way out\\n\\nLet me tell you bout it\\nLet me tell you bout it\\nMaybe you're the same as me\\n\\nLet me tell you bout it\\nLet me tell you bout it\\nThey say the truth will set you free\\n\\nHello, hello\\nLet me tell you what it's like to be a zero, zero\\nLet me show you what it's like to always feel, feel\\nLike I'm empty and there's nothing really real, real\\nI'm looking for a way out\\n\\nHello, hello\\nLet me tell you what it's like to be a zero, zero\\nLet me show you what it's like to never feel, feel\\nLike I'm good enough for anything that's real, real\\nI'm looking for a way out\\n\\nMusic video by Imagine Dragons performing Zero (Vertical Video/From the Original Motion Picture \\\"Ralph Breaks The Internet\\\"). © 2018 KIDinaKORNER/Interscope Records/Disney Enterprises, Inc.\\n\\nhttp://vevo.ly/iBUo9Z\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/j60ClcNYWu4/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/j60ClcNYWu4/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/j60ClcNYWu4/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/j60ClcNYWu4/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/j60ClcNYWu4/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":4,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"j60ClcNYWu4\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/SW0ramoVBF9VYqbswPGcA2GGPS8\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC5BRUYzMTlENkQzRUZDN0ZG\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Justine Skye - Build ft. Arin Ray\",\"description\":\"Music video by Justine Skye performing Build. © 2018 Roc Nation, LLC.\\n\\nhttp://vevo.ly/pBSm4e\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/khEd4Rt0nGQ/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/khEd4Rt0nGQ/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/khEd4Rt0nGQ/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/khEd4Rt0nGQ/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/khEd4Rt0nGQ/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":5,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"khEd4Rt0nGQ\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/NmjA82yJxAy4qJOaOo6F4gGr9iI\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC5DOTBGNERCMTJCNjFBNkY0\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Jason Derulo x David Guetta - Goodbye (feat. Nicki Minaj & Willy William) [OFFICIAL MUSIC VIDEO]\",\"description\":\"\\u202aJason Derulo\\u202a x David Guetta feat. Nicki Minaj & Willy William “Goodbye” Official Music Video.\\u202c\\nDirected by : Jason Derulo, David Strbik and Jeremy Strong.\\u202c\\n\\u202cProduced by Derulo and David Strbik \\nEdited by David Strbik\\n\\n\\u202aListen Now: https://jderulo.co/goodbye\\u202c\\n\\u202a \\u202c\\n\\u202aCONNECT WITH JASON DERULO:\\u202c\\n\\u202aInstagram - https://www.instagram.com/jasonderulo/\\u202c\\n\\u202aFacebook - https://www.facebook.com/jasonderulo\\u202c\\n\\u202aTwitter - https://twitter.com/jasonderulo\\u202c\\n\\u202aOfficial Website - http://www.jasonderulo.com/\\u202c\\n\\n#jasonderulo #nickiminaj #davidguetta #willywilliam\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/kUjKxtJd21E/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/kUjKxtJd21E/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/kUjKxtJd21E/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/kUjKxtJd21E/sddefault.jpg\",\"width\":640,\"height\":480}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":6,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"kUjKxtJd21E\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/blrYuHFCBjs5shTwZ7jqk-yL7k4\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC44QUM1MUY1RDM4M0Q4ODc5\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Bring Me The Horizon - wonderful life (Lyric Video) ft. Dani Filth\",\"description\":\"Wonderful Life: http://bmthorizon.co/wl\\n\\nDirected by Theo Watkins. \\n\\naмo - тнe вrand new alвυм\\noυт jan 25тн\\npre-order: http://bmthorizon.co/amo\\n\\nLYRICS:\\n\\nI read a fun fact about the brain and how it starts to deteriorate when we get to 27 or there abouts. It got me thinking about my head and what I can do to help stimulate it. \\nMakes me sad but.. \\nI’ve forgotten what I’m on about. \\nLooked on the brightside, got keratitis. \\n& You can’t sit there unless you’re righteous. I wear a happy face like I’m Ed Gein. \\nI feel all numb now, is that a feeling? \\nlike a plastic boxed orange with no peel on- I wanna waste I wanna waste I wanna waste away. \\nAlone getting high on a Saturday night \\nI’m on the edge of a knife \\nNobody cares if I’m dead or alive \\nOh what a wonderful life \\nOi Debbie downer what’s your problem? \\nDon’t wanna be here, still call shotgun \\nYou got the fomo coursing through my veins.\\nThis is not a drill no, this is the real world.\\nDomesticated still a little feral.\\nWell don’t you know to chew with your mouth closed? \\nIt’s all gone wrong!\\nI got a Type 2 kinda thirstiness \\nA far out other worldliness \\n& one day this might hurt me less\\nBut everyone knows I’m still down \\nDon’t tell me what the butcher does \\nThere’s no need for the obvious \\nSo ugly still it’s kinda lush \\nBut everybody knows i made vows \\nLeft feet on the podium \\nCan’t think of an alternate \\nAnd hell yeah I’m the awkwardest \\nBut everybody knows I got bounce\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/4hoDwVy6IQ4/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/4hoDwVy6IQ4/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/4hoDwVy6IQ4/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/4hoDwVy6IQ4/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/4hoDwVy6IQ4/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":7,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"4hoDwVy6IQ4\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/nMBTiTkPNQyqyZ5TupPaww6kAnI\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC4wREYxNjI3NjIwMjg3MjhC\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Zara Larsson - Ruin My Life\",\"description\":\"This video means so much to me!! I’m so excited to finally be able to ruin your life! ✨✨✨\\n\\\"Ruin My Life\\\" Available at iTunes: http://smarturl.it/RuinMyLife/itunes\\nApple Music: http://smarturl.it/RuinMyLife/applemusic\\nSpotify: http://smarturl.it/RuinMyLife/spotify\\nGoogle Play: http://smarturl.it/RuinMyLife/googleplay\\nAmazon Digital: http://smarturl.it/RuinMyLife/az\\nDeezer: http://smarturl.it/RuinMyLife/deezer\\nSoundcloud: http://smarturl.it/RuinMyLife/soundcloud\\nTidal: http://smarturl.it/RuinMyLife/tidal\\n\\n\\nJoin Zara Larsson online: \\nhttp://www.instagram.com/zaralarsson \\nhttps://www.facebook.com/ZaraLarssonOfficial\\nhttps://twitter.com/zaralarsson\\nhttp://zaralarsson.se\\nhttp://www.zaralarssonofficial.com/\\n\\nhttp://ten.se\\nhttp://www.facebook.com/TENmusicgroup\\n\\n(C) 2018 Record Company TEN, exclusively licensed by Epic Records, a division of Sony Music Entertainment\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/3OTjFqWcDQY/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/3OTjFqWcDQY/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/3OTjFqWcDQY/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/3OTjFqWcDQY/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/3OTjFqWcDQY/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":8,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"3OTjFqWcDQY\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/cNL46yAl09csBbgFCi1y0b4jgkg\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC5FNzExNkY5Q0YyQzFCMjIx\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Paloma Faith - Loyal (Official Video)\",\"description\":\"Loyal is taken from the album 'The Architect: Zeitgeist Edition', available Nov 16: http://smarturl.it/TAzeitgeiststore\\nListen to Loyal\\nSpotify: http://smarturl.it/PalomaLoyal/Spotify\\nApple Music: http://smarturl.it/PalomaLoyal/AppleMusic\\niTunes: http://smarturl.it/PalomaLoyal/itunes\\nAmazon Music: http://smarturl.it/PalomaLoyal/amazonunlimited\\n \\nDirected by Jamie Travis\\nChoreography by Jasmin Vardimon\\n \\nFollow Paloma Faith\\nWebsite: http://www.palomafaith.com/\\nFacebook: https://www.facebook.com/palomafaith\\nTwitter: https://twitter.com/Palomafaith\\nInstagram: https://www.instagram.com/palomafaith/\\nSpotify: http://smarturl.it/palomacatsp\\n \\nLyrics:\\n\\nWandering eyes that always seem to search out other faces in the room\\nI know we try, pretend that this will work out\\nBut lately acting’s just no good\\n \\nMaybe we’re loyal\\nMaybe we’re not\\nYeah we got our secrets\\nBut where does it stop\\nCos you don’t remember\\nAnd I think I forgot\\nThat we made a choice to stay no matter what\\n \\nIf we’re always searching for a love that’s perfect\\nHow we ever gonna see just what we got?\\nTreading water til we know it’s worth it\\nAfraid to be the first to open up\\nMaybe we’re loyal?\\nMaybe we’re not?\\nYeah we got our secrets\\nBut where does it stop?\\nIf we’re always searching for a love that’s perfect\\nHow we ever gonna know just what we got?\\n \\nI don’t remember the last time that we made love\\nLost in a blind reality\\nSearching for someone who’s standing right before me\\nQuestioning my morality\\n \\nMaybe we’re loyal\\nMaybe we’re not\\nI wish I could tell you\\nI want it to stop\\nCos you don’t remember\\nAnd I think I forgot\\nThat we made a choice to stay no matter what\\n \\nIf we’re always searching for a love that’s perfect\\nHow we ever gonna see just what we got?\\nTreading water til we know it’s worth it\\nAfraid to be the first to open up\\nMaybe we’re loyal?\\nMaybe we’re not?\\nYeah we got our secrets\\nBut where does it stop?\\nIf we’re always searching for a love that’s perfect\\nHow we ever gonna know just what we got?\\n \\nWhy can’t we be loyal?\\nWhy fight what we got when we got it so good?\\nWhy can’t we be loyal?\\nWhy fight what we got cos?\\n \\nIf we’re always searching for a love that’s perfect\\nHow we ever gonna see just what we got?\\nTreading water til we know it’s worth it\\nAfraid to be the first to open up\\nMaybe we’re loyal?\\nMaybe we’re not?\\nYeah we got our secrets\\nBut where does it stop?\\nIf we’re always searching for a love that’s perfect\\nHow we ever gonna know just what we got?\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/BQxLJGeXjM8/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/BQxLJGeXjM8/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/BQxLJGeXjM8/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/BQxLJGeXjM8/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/BQxLJGeXjM8/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":9,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"BQxLJGeXjM8\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/3gN3U_mMocFA9C5uwJfBkb5y6ck\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC5FODIwNTlBNjlGRDExMzAz\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Dua Lipa & BLACKPINK - Kiss and Make Up (Official Audio)\",\"description\":\"The Complete Edition is here!! https://dualipa.co/completeedition\\n\\nFollow me online:\\nhttps://wbr.ec/website_dualipa\\nhttps://wbr.ec/facebook_dualipa\\nhttps://wbr.ec/twitter_dualipa\\nhttps://wbr.ec/instagram_dualipa\\n\\n#dualipa #blackpink #kissandmakeup\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/AX3Bsiq-13k/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/AX3Bsiq-13k/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/AX3Bsiq-13k/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/AX3Bsiq-13k/sddefault.jpg\",\"width\":640,\"height\":480}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":10,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"AX3Bsiq-13k\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/MIakJdVVNUiRcW9wex-Rs-6q2iw\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC5FMkVCNDg2QTc4QThGNjMx\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"I Wonder As I Wander - Lindsey Stirling\",\"description\":\"Pick up a copy of the Deluxe Edition of Lindsey's Christmas Album \\\"Warmer in the Winter\\\" at the following places:\\nTarget: https://found.ee/LS_WITWDeluxeTarget\\nAmazon: https://found.ee/LS_WITWDeluxeAMZ\\niTunes: https://found.ee/LS_WITWDeluxeiTunes\\nBarnes & Noble https://found.ee/LS_WITWDeluxeBN\\nSpotify: https://found.ee/LS_WITWDeluxeSpotify\\nApple Music: https://found.ee/LS_WITWDeluxeApple\\nPledge: https://found.ee/LS_WITWpledge\\n\\nHead here for tour dates, tickets, and VIP upgrades: http://www.lindseystirling.com/\\n\\nSheet Music Here: https://lindseystirlingsheetmusic.com\\n\\nFollow me here:\\nhttps://www.facebook.com/lindseystirlingmusic\\nhttps://twitter.com/LindseyStirling\\nhttp://www.instagram.com/LindseyStirling\\n\\nSign up for my super-cool newsletter here:\\nhttp://lindseystirling.fanbridge.com\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/4rR8jc6EPQM/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/4rR8jc6EPQM/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/4rR8jc6EPQM/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/4rR8jc6EPQM/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/4rR8jc6EPQM/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":11,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"4rR8jc6EPQM\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/ihhIXrKxvh5XlLR_jH5aNJHwLlE\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC4zMjM5MUJDQTBBOUNENUQ2\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Travis Scott - SICKO MODE ft. Drake\",\"description\":\"ASTROWORLD OUT NOW   http://travisscott.com\\n\\nDirectors: Dave Meyers and Travis Scott\\nProducers: Nathan Scherrer for Freenjoy, inc, Sam Lecca\\n\\nTravis Scott online:\\nhttps://twitter.com/trvisXX\\nhttps://www.instagram.com/travisscott/\\nhttps://soundcloud.com/travisscott-2\\nhttps://www.facebook.com/travisscottlaflame\\nhttps://travisscott.com/\\n\\nEpic / Cactus Jack\\n(c) 2018 Epic Records, a division of Sony Music Entertainment. With Cactus Jack and Grand Hustle.\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/6ONRf7h3Mdk/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/6ONRf7h3Mdk/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/6ONRf7h3Mdk/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/6ONRf7h3Mdk/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/6ONRf7h3Mdk/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":12,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"6ONRf7h3Mdk\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/leMfINBCZeD8NufnenTK47PEqwc\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC4yNDA4NTZBRTYxRDEyNjg4\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Kane Brown - Good as You\",\"description\":\"Kane Brown’s new album, Experiment, is available 11.9. Pre-order and connect to get “Good As You” + four more songs including Kane’s hit “Lose It” right now!\\n\\nApple Music: http://smarturl.it/kbexperiment/applemusic?IQid=yt \\niTunes: http://smarturl.it/kbexperiment/itunes?IQid=yt \\nSpotify: http://smarturl.it/kbexperiment/spotify?IQid=yt \\nAmazon Music: http://smarturl.it/kbexperiment/az?IQid=yt \\nGoogle Play: http://smarturl.it/kbexperiment/googleplay?IQid=yt \\nYou Tube Music:  http://smarturl.it/kbexperiment/youtubemusic?IQid=yt \\n  \\nFollow Kane Brown: \\nOfficial Website: http://kanebrownmusic.com \\nFacebook: https://www.facebook.com/kaneallenbrown \\nInstagram: https://www.instagram.com/kanebrown_music \\nTwitter: https://twitter.com/kanebrown\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/5PaJ-BHmWiE/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/5PaJ-BHmWiE/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/5PaJ-BHmWiE/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/5PaJ-BHmWiE/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/5PaJ-BHmWiE/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":13,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"5PaJ-BHmWiE\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/rgnobJREY7CEaWUjIG5EUd6_Rgc\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC42QkVCREE5NDQwMTVBQ0JE\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Ariana Grande - better off (Audio)\",\"description\":\"Music video by Ariana Grande performing better off (Audio). © 2018 Republic Records, a Division of UMG Recordings, Inc.\\n\\nhttp://vevo.ly/ikYY5g\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/-mNEr0OGusU/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/-mNEr0OGusU/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/-mNEr0OGusU/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/-mNEr0OGusU/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/-mNEr0OGusU/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":14,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"-mNEr0OGusU\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/iSrGrE1KbrZvridKdSw1GkLbm6M\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC42NUI1MDU0OEU0MDA3QjhC\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Lil Peep - Cry Alone (Official Video)\",\"description\":\"‘Come Over When You’re Sober, Pt. 2’ out Nov. 9: \\nhttp://smarturl.it/COWYS2 \\n \\n“Cry Alone” out now:\\n http://smarturl.it/CRYALONE \\n \\nProduced by Smokeasac and IIVI\\n\\nDIRECTOR’S NOTES:\\n\\nPeep and I shot Cry Alone back in May 2017.  He originally thought it was going to be a track included on COWYS1, but it ended up fitting better on COWYS2 which inevitably postponed the videos release... Here's the story: \\n \\nIt was around 1AM when I saw Peep on Twitter asking if someone could bring him McDonalds in Berkeley. I texted him asking what he was doing in town (I was living in the Bay Area at the time) and he told me he'd just finished his show in SF, but that I should come to his hotel and maybe take some photos if I was down. Ever since I met him, we talked about shooting various videos, but nothing ever came into fruition so I suggested we shoot a music vid that night. He said it was perfect timing cause he'd just recorded a bunch of new music and that we'd pick a song when I got there. After listening to a few different songs and discussing ideas we chose Cry Alone. “It just feels right”, he said. “It's the only song that fits the energy of the night”.\\n \\nDirected & Edited by @maxdotbam\\nVideo produced by @getmezzy\\nShot in Berkeley, CA in May 2017\\n \\n/////\\n \\nhttp://www.lilpeep.party/ \\nhttp://twitter.com/lilpeep \\nhttp://instagram.com/lilpeep \\nhttp://soundcloud.com/lil_peep \\nhttp://facebook.com/lilpeepmusic\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/fzV_QZODisQ/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/fzV_QZODisQ/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/fzV_QZODisQ/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/fzV_QZODisQ/sddefault.jpg\",\"width\":640,\"height\":480}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":15,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"fzV_QZODisQ\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/u4qozywj0YkxbsBWcJ1SKVPXjzU\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC45OEUyN0YwREQ3QUY0QzYz\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Brandi Carlile - Party Of One feat. Sam Smith (Official Video)\",\"description\":\"The official video for Brandi Carlile - Party Of One (feat. Sam Smith) - available now. https://ElektraRecords.lnk.to/PartyOfOne\\n\\nA portion of the profits from this recording will be donated to Children in Conflict via Brandi Carlile’s Looking Out Foundation as part of its ongoing campaign to raise $1 million for children impacted by war.\\n\\nmore information can be found at http://www.childreninconflict.org  &  http://www.lookingoutfoundation.org\\n\\nFrom the album \\\"By The Way, I Forgive You\\\" available now. http://elektrar.ec/BrandiCarlileBTWIFY \\n\\nDirector: Tom Kirk\\nProducer: Rowan Glenn\\nDirector of Photography: Matt Hayslett\\nProduction Company: Hidden Road Studios\\nFilmed at Capitol Studio B\\n\\nConnect with Brandi: \\nhttp://brandicarlile.com \\nhttp://facebook.com/brandicarlile \\nhttp://twitter.com/brandicarlile \\nhttp://instagram.com/brandicarlile\\n\\n#BrandiCarlile #SamSmith #PartyOfOne\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/Ll_QeA-1LZM/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/Ll_QeA-1LZM/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/Ll_QeA-1LZM/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/Ll_QeA-1LZM/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/Ll_QeA-1LZM/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":16,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"Ll_QeA-1LZM\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/krmsGI7rFZPod7tEi3A_FtpXEng\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC43N0M1NTVEQ0E4QkExNjE4\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"ZAYN - Fingers (Audio)\",\"description\":\"Get ZAYN’s new song “Fingers” now:\\niTunes - http://smarturl.it/Zfing3rZ/itunes?iQid=yt\\nApple Music - http://smarturl.it/Zfing3rZ/applemusic?iQid=yt\\nSpotify - http://smarturl.it/Zfing3rZ/spotify?iQid=yt\\nAmazon - http://smarturl.it/Zfing3rZ/az?iQid=yt\\nGoogle Play - http://smarturl.it/Zfing3rZ/googleplay?iQid=yt\\n\\nGet ZAYN’s song “Too Much” feat. Timbaland: http://smarturl.it/Zt00muchZt?iQid=yt\\nWatch the animated video: http://smarturl.it/Zt00muchZt/youtube?iQid=yt\\n \\nGet ZAYN’s song “Let Me”: http://smarturl.it/ZlZmZ?IQid=yt\\nWatch the official video: http://smarturl.it/ZlZmZv?IQid=yt\\n \\nGet ZAYN’s song “Entertainer”: http://smarturl.it/Z3ntrt4inerZ?iQid=yt\\nWatch the official video: http://smarturl.it/Z3ntrt4inerZv?iQid=yt\\n \\nGet ZAYN’s song “Sour Diesel”: http://smarturl.it/zS0urzDi3s3l?iQid=yt\\nWatch the official video for “Sour Diesel” exclusively on Apple Music: http://smarturl.it/zS0urvDi3s3l?iQid=yt \\n \\nFollow ZAYN: \\nhttp://www.facebook.com/ZAYN \\nhttps://twitter.com/zaynmalik \\nhttp://www.instagram.com/ZAYN \\nSnapchat: @ZAYN \\nhttp://smarturl.it/ZAYNSpotify \\nhttp://www.inZAYN.com \\n \\nBest of ZAYN: \\nhttp://smarturl.it/BoZPlaylist\\n\\n\\\"Fingers\\\" Official Lyrics:\\n\\nFucked and I want ya\\nLooked and I loved ya\\nStuck now I need ya\\nHoping I see ya\\n\\nTouch wanna feel her\\nMuch can’t conceal her no\\nHiding all your features\\nSliding on the filters \\nShould be you that’s in the middle\\nDon’t be hiding what you thinking I been\\n \\nFucked and I want ya\\nI can’t even text ya\\nCuz my fingers ain’t working but my heart is\\nIf you wanna let me know where you are b \\nI can come and love ya\\n \\nWhat did I tell ya\\nTypo said I loved ya\\nDidn’t mean what I was saying\\nNo I wasn’t playing just confused\\nTrying to play it smooth\\nStood spinning in this room \\nIn this state I been consumed\\nStood spinning in this room\\n \\nCuz I’m Fucked and I want ya\\nI can’t even text ya\\nCuz my fingers ain’t working but my heart is\\nIf you wanna let me know where you are b\\nI can come and love ya\\n \\nKnow it’s taking all your strength to keep restrained \\nCuz you got different things replacing \\nOld feelings that you felt\\nNow you seen it for yourself \\nWhere’s the shame?\\nNo shame in what you need\\n \\nFucked and I want ya\\nI can’t even text ya\\nCuz my fingers ain’t working but my heart is\\nIf you wanna let me know where you are b \\nI can come and love ya\\n \\n#ZAYN\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/9cmPcH38wjc/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/9cmPcH38wjc/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/9cmPcH38wjc/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/9cmPcH38wjc/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/9cmPcH38wjc/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":17,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"9cmPcH38wjc\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/gmp1i7sKCFU3iFQbAaj7DfhZGIs\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC41NTA4REJFNTcwQzA3RDNG\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Post Malone, Swae Lee - Sunflower (Spider-Man: Into the Spider-Verse)\",\"description\":\"\\\"Sunflower\\\" is the first single from the official soundtrack album Spider-Man™: Into the Spider-Verse (available December 14, 2018). The film opens in theaters nationwide the same day. Get the song here: https://postmalone.lnk.to/sunflowerYD\\n\\nThe single re-teams Post Malone & Swae Lee after the two joined forces for “Spoil My Night” from the year’s biggest multi-platinum album, beerbongs & bentleys.\\n\\nFor more info on Post Malone:\\nhttp://www.postmalone.com\\nhttps://instagram.com/postmalone\\nhttps://twitter.com/postmalone\\nhttps://facebook.com/postmalone\\n\\nFor more info on Swae Lee:\\nhttps://instagram.com/swaelee\\nhttps://twitter.com/goSwaeLee\\nhttps://facebook.com/swaelee\\n\\nFollow Spider-Man™: Into the Spider-Verse:\\nhttps://www.facebook.com/SpiderVerseMovie\\nhttps://www.twitter.com/SpiderVerse\\nhttps://instagram.com/SpiderVerseMovie\\n\\nPhil Lord and Christopher Miller, the creative minds behind The Lego Movie and 21 Jump Street, bring their unique talents to a fresh vision of a different Spider-Man Universe, with a groundbreaking visual style that’s the first of its kind. Spider-Man™: Into the Spider-Verse introduces Brooklyn teen Miles Morales, and the limitless possibilities of the Spider-Verse, where more than one can wear the mask.\\n\\nCast: \\nShameik Moore\\nHailee Steinfeld\\nMahershala Ali\\nJake Johnson\\nLiev Schreiber\\nBrian Tyree Henry\\nLuna Lauren Velez\\nLily Tomlin\\nNicolas Cage\\nJohn Mulaney\\nKimiko Glenn\\n\\n©2018 Columbia Pictures Industries, Inc. All Rights Reserved. MARVEL and all related character names\\n\\n© & ™ 2018 MARVEL\\n\\n#PostMalone #SwaeLee #IntoTheSpiderVerse #SpiderMan #Marvel #MilesMorales\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/ApXoWvfEYVU/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/ApXoWvfEYVU/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/ApXoWvfEYVU/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/ApXoWvfEYVU/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/ApXoWvfEYVU/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":18,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"ApXoWvfEYVU\"}}},{\"kind\":\"youtube#playlistItem\",\"etag\":\"\\\"XI7nbFXulYBIpL0ayR_gDh3eu1k/VnDhPypOvcmg7Yb4Im_T5a23dRU\\\"\",\"id\":\"UExGZ3F1TG5MNTlhbFczeG1ZaVdSYW96MG9NM0gxN0x0aC4wOEIxNEMyQTFEMzc5MjNC\",\"snippet\":{\"publishedAt\":\"2018-10-24T15:09:58.000Z\",\"channelId\":\"UC-9-kyTW8ZkZNDHQJ6FgpwQ\",\"title\":\"Rae Morris - Dancing With Character [Official Video]\",\"description\":\"Rae's new album SOMEONE OUT THERE, is out now: http://atlanti.cr/someoneoutthere\\n\\nDirected by Noel Paul\\n\\nSubscribe to Rae's channel: http://goo.gl/n745dd\\nLike on Facebook: http://on.fb.me/SqvAnR\\nFollow on Twitter: http://bit.ly/QqtGnb\\nFollow on Spotify: http://smarturl.it/rae.spotify\\nFollow on Instagram: @rae_morris\\nhttp://raemorris.co.uk\",\"thumbnails\":{\"default\":{\"url\":\"https://i.ytimg.com/vi/Jhb1iRqGBHk/default.jpg\",\"width\":120,\"height\":90},\"medium\":{\"url\":\"https://i.ytimg.com/vi/Jhb1iRqGBHk/mqdefault.jpg\",\"width\":320,\"height\":180},\"high\":{\"url\":\"https://i.ytimg.com/vi/Jhb1iRqGBHk/hqdefault.jpg\",\"width\":480,\"height\":360},\"standard\":{\"url\":\"https://i.ytimg.com/vi/Jhb1iRqGBHk/sddefault.jpg\",\"width\":640,\"height\":480},\"maxres\":{\"url\":\"https://i.ytimg.com/vi/Jhb1iRqGBHk/maxresdefault.jpg\",\"width\":1280,\"height\":720}},\"channelTitle\":\"Music\",\"playlistId\":\"PLFgquLnL59alW3xmYiWRaoz0oM3H17Lth\",\"position\":19,\"resourceId\":{\"kind\":\"youtube#video\",\"videoId\":\"Jhb1iRqGBHk\"}}}]}");
            Type type = new TypeToken<ResponseYoutubePlaylist>() {
            }.getType();
            Gson gson = new Gson();
            playlist = gson.fromJson(json.toString(), type);


        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取YouTubeapi失败#=>" + url);
        }
        return playlist;
    }

    public Map<String, String> getAllYoutubePlayList(String playlistId) {

        List<ResponseYoutubePlaylist> playlists = new ArrayList<>();
        String nextPageToken = null;
        String oldPageToken = null;
        Map<String, String> songs = new HashMap<>();

        do {
            ResponseYoutubePlaylist playlist = getYoutubePlayList(playlistId, nextPageToken);
            oldPageToken = nextPageToken;
            nextPageToken = playlist.getNextPageToken();
            playlists.add(playlist);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (nextPageToken != null && !nextPageToken.equals(oldPageToken));

        for (ResponseYoutubePlaylist playlist : playlists) {
            List<ResponseYoutubeItems> items = playlist.getItems();
            for (ResponseYoutubeItems item : items) {
                ResponseYoutubeSnippet snippet = item.getSnippet();
                String title = snippet.getTitle();
                String[] authorAndName = title.split("-");
                if (authorAndName.length != 2) {
                    continue;
                }
                String author = authorAndName[0];
                String song = authorAndName[1];
                int brachIndex = song.indexOf("(");
                if (brachIndex >= 0) {
                    song = song.substring(0, brachIndex);
                }

                int index = song.indexOf("[");
                if (index >= 0) {
                    song = song.substring(0, index);
                }

                songs.put(author.trim().toLowerCase(), song.trim().toLowerCase());
            }
        }
        return songs;


    }

    /**
     * 网易云搜索接口，获取匹配的音乐数据
     */
    public Integer searchMusic(String name, String author) {

        name = name.toLowerCase();
        author = author.toLowerCase();

        RequestSearch search = new RequestSearch(name, 1, 30, 0);
        String json = search.getJson();
        try {

            Map<String, String> paramAndEncSecKey = Encrypt.getParamAndEncSecKey(json);
            UrlEncodedFormEntity urlEncodedFormEntity = MRequest.getUrlEncodedFormEntity(paramAndEncSecKey);
            Map<String, String> headers = new HashMap<>();
            String cookie = getFileContent(STATIC_DIR, MConstants.COOKIE_FILE_NAME);
            headers.put("Cookie", cookie);
            String url = "http://music.163.com/weapi/search/get";
            HttpResponse response = MRequest.query("POST", url, headers, urlEncodedFormEntity, 2);
            String result = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            Type type = new TypeToken<ResponseSearch>() {
            }.getType();
            ResponseSearch searchResult = gson.fromJson(result, type);
            ResponseSearchResult result1 = searchResult.getResult();
            List<ResponseSong> songs = result1.getSongs();
            for (ResponseSong song : songs) {
                List<ResponseArtists> artists = song.getArtists();
                Set<String> artistNames = new HashSet<>();
                for (ResponseArtists artist : artists) {
                    artistNames.add(artist.getName().trim().toLowerCase());
                }
                if (artistNames.contains(author.trim().toLowerCase())) {
                    System.out.println(song.getName() + "<====>" + song.getId() + "<====>" + artistNames);
                    return song.getId();
                }
            }
        } catch (Exception ex) {
            logger.info("搜索失敗");
            ex.printStackTrace();
        }
        return null;
    }

    public Long getPlaylistId(String uid, String targetPlayList) {

        String url = "http://music.163.com/weapi/user/playlist";
        try {
            QueryUserPlayLists playlistQuery = new QueryUserPlayLists(uid, 30, 0);
            String json = playlistQuery.getJson();
            Map<String, String> paramAndEncSecKey = Encrypt.getParamAndEncSecKey(json);
            UrlEncodedFormEntity urlEncodedFormEntity = MRequest.getUrlEncodedFormEntity(paramAndEncSecKey);
            Map<String, String> headers = new HashMap<>();
            String cookie = getFileContent(STATIC_DIR, MConstants.COOKIE_FILE_NAME);
            headers.put("Cookie", cookie);
            HttpResponse response = MRequest.query("POST", url, headers, urlEncodedFormEntity, 2);
            String result = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            Type type = new TypeToken<ResponseUserPlayLists>() {
            }.getType();
            ResponseUserPlayLists playlists = gson.fromJson(result, type);
            for (ResponsePlaylist playlist : playlists.getPlaylist()) {
                if (playlist.getName().equalsIgnoreCase(targetPlayList)) {
                    return playlist.getId();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("播放列表搜索失敗");
        }
        return null;

    }

    public void changePlaylist(String playlistId, List<Integer> trackIds, String op) {

        Map<Object, Object> query = new HashMap<>();
        //{ op: 'add', pid: '2479931171', trackIds: '[347231]' }
        query.put("op", op);
        query.put("pid", playlistId);
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        List<String> trackIdsStr = new ArrayList<>();
        for (Integer trackId : trackIds) {
            trackIdsStr.add(String.valueOf(trackId));
        }
        String trackStr = String.join(",", trackIdsStr);
        sb.append(trackStr);
        sb.append("]");
        query.put("trackIds", new String(sb));
        Gson gson = new Gson();
        String json = gson.toJson(query);

        String cookie = getFileContent(STATIC_DIR, MConstants.COOKIE_FILE_NAME);
        Map<String, String> header = new HashMap<>();
        header.put("Cookie", cookie);
        Map<String, String> paramAndEncSecKey = Encrypt.getParamAndEncSecKey(json);
        UrlEncodedFormEntity urlEncodedFormEntity = MRequest.getUrlEncodedFormEntity(paramAndEncSecKey);
        String url = "http://music.163.com/weapi/playlist/manipulate/tracks";
        MRequest.query("POST", url, header, urlEncodedFormEntity, 2);

    }

    public List<Integer> getPlaylistTrackIds(String playlistId) {
        Map<String, Object> query = new HashMap<>();
        query.put("id", playlistId);
        query.put("n", 100000);
        query.put("s", 8);

        List<Integer> ids = new ArrayList<>();
        String json = gson.toJson(query);
        try {

            String url = "http://music.163.com/weapi/v3/playlist/detail";
            String cookie = getFileContent(STATIC_DIR, MConstants.COOKIE_FILE_NAME);
            Map<String, String> header = new HashMap<>();
            header.put("Cookie", cookie);
            Map<String, String> paramAndEncSecKey = Encrypt.getParamAndEncSecKey(json);
            UrlEncodedFormEntity urlEncodedFormEntity = MRequest.getUrlEncodedFormEntity(paramAndEncSecKey);
            HttpResponse response = MRequest.query("POST", url, header, urlEncodedFormEntity, 2);

            Type type = new TypeToken<ResponsePlaylistDeatilResult>() {
            }.getType();
            String responseJson = EntityUtils.toString(response.getEntity());
            ResponsePlaylistDeatilResult result = gson.fromJson(responseJson, type);
            ResponsePlaylistDetail playlist = result.getPlaylist();
            for (Map<String, Integer> trackId : playlist.getTrackIds()) {
                ids.add((Integer) trackId.get("id"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("获取列表详情失败");
        }

        return ids;

    }

    /**
     * /playlist/tracks?op=add&pid=24381616&tracks=347231 歌单操作
     * 获取自己歌单 /user/playlist?uid=32953014
     * 获取歌单 所有歌曲 [trackIds] /playlist/detail?id=24381616
     * 搜索 [trackIds] http://music.163.com/weapi/search/get
     */
    /*
     * 网易云音乐列表数据获取, 特定列表数据
     */

    /**
     * 更新指定列表数据，如果最近数据不存在
     */

    public static void main(String[] args) throws IOException {

        Main test = new Main();

        boolean existed = test.createDirIfNotExisted(STATIC_DIR + MConstants.COOKIE_FILE_NAME);
        if (args.length < 2){
            System.out.println("请输入账号【空格】密码");
            System.exit(0);
        }

        if (!existed) { //登录账号
            logger.info("重新登录账号");
            test.login(args[0], Encrypt.getMd5(args[1]));
        }

        //获取Youtube表单信息  author#=>song
        Map<String, String> youtubeMusic = test.getAllYoutubePlayList(YOUTUBE_LIST);
        List<Integer> foundTrackList = new ArrayList<>();
        for (Map.Entry<String, String> authorSong : youtubeMusic.entrySet()) {
            String author = authorSong.getKey();
            String song = authorSong.getValue();
            Integer trackId = test.searchMusic(song, author);
            if (trackId != null) {
                foundTrackList.add(trackId);
            }
        }

        String idStr = test.getFileContent(STATIC_DIR, MConstants.USER_INFO_FILE_NAME);
        Long playlistId = test.getPlaylistId(idStr, MUSIC_LIST);
        List<Integer> hadTrackIds = test.getPlaylistTrackIds(String.valueOf(playlistId));

        List<Integer> shouldRemove = new ArrayList<>();
        List<Integer> shouldAdd = new ArrayList<>();
        for(Integer id : hadTrackIds){
            if(!foundTrackList.contains(id)){
                shouldRemove.add(id);
            }
        }
        for(Integer id : foundTrackList){
            if(!hadTrackIds.contains(id)){
                shouldAdd.add(id);
            }
        }

        if(shouldRemove.size() > 0) {
            test.changePlaylist(String.valueOf(playlistId), shouldRemove, "del");
        }
        if(shouldAdd.size() > 0 ) {
            test.changePlaylist(String.valueOf(playlistId), shouldAdd, "add");
        }
        logger.info("remove #=>" + shouldRemove + "\r\n" + "add#=>" + shouldAdd);

    }
}
