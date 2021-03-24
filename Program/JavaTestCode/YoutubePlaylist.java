package httpClientTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YoutubePlaylist {
	//PLAYLISTID
	private final String PLAYLISTID;
	//今何個目の動画か
	private int count = 0;
	//後何個動画があるか
    private int remaining;
    //再生リストの中の動画の数
    int totalResults = 0;
    //一度のgetJSONStringの実行で取得できる動画の数
    int resultsPerPage = 0;
    //playlistItems_json_string
    String playlistItems_json_string;

    String nextPageToken = "";

    public YoutubePlaylist(String PLAYLISTID) {
    	this.PLAYLISTID = PLAYLISTID;
    }

	//プレイリストの中の動画の情報を使いやすい形に合わせて取得する
	public void getPlaylistItems() {
        //extractPlaylistItemsInformationを効率よく回すために必要なデータを保存しておくリスト
        ArrayList playlist_information = new ArrayList();
        //再生リストの中の動画情報を保存しておくList
        ArrayList<ArrayList> playlistItems_list = new ArrayList();

        //YoutubeDataAPIを使用して再生リストのJSON文字列を返す
        playlistItems_json_string  = getJSONString(nextPageToken);

        //extractPlaylistItemsInformationを効率よく回すために必要なデータを再生リストから抽出
        extractPlaylistInformation(playlistItems_json_string, playlist_information);

        totalResults = (int) playlist_information.get(0);
        resultsPerPage = (int) playlist_information.get(1);

        remaining = totalResults;

        while(remaining > 0) {
        	test(playlist_information, playlistItems_list);
        	//System.out.println(nextPageToken);
        }

        for (int i = 0; i < playlistItems_list.size(); i++) {
            System.out.println(playlistItems_list.get(i));
        }
    }

    //テスト用再起呼び出しによる再生リスト内の動画取得
    private void test(ArrayList playlist_information, ArrayList<ArrayList> playlistItems_list) {

    	String playlistItems_json_string  = getJSONString(nextPageToken);

    	extractPlaylistInformation(playlistItems_json_string, playlist_information);

    	nextPageToken = getNextPageToken(playlistItems_json_string, nextPageToken);

        //System.out.println(playlistItems_json_string);

        if(remaining < resultsPerPage) {
        	resultsPerPage = remaining;
        }
        for(int itemNum = 0; itemNum < resultsPerPage; itemNum++) {
            extractPlaylistItemsInformation(playlistItems_json_string, itemNum, playlistItems_list);
            count++;
            remaining--;
        }
    }


    //YoutubeDataAPIを使用して再生リストのJSON文字列を返す
    private String getJSONString(String nextPageToken) {
        //Googleで取得したAPIKEY
        String APIKEY = "AIzaSyDEfFT1M0tKb8dgyDyT54KRMWG5Lb_Xh0Q";
        //playlistItemsのJSONデータを返すAPI
        String url_str = "https://www.googleapis.com/youtube/v3/playlistItems?part=id,snippet,contentDetails";
        url_str += "&playlistId=" + PLAYLISTID;
        url_str += "&maxResults=50";
        url_str += "&key=" + APIKEY;
        if(nextPageToken != "") {
            url_str += "&pageToken=" + nextPageToken;
        }
        //戻り値となるJSON文字列
        String json_string = "";
        //HTTP接続を行うHttpURLConnectionオブジェクトを宣言
        //finallyで確実に解放するためにtry外で宣言
        HttpURLConnection con = null;
        //HTTP接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言
        //finallyで確実に解放するためにtry外で宣言
        InputStream is = null;
        try {
            //URLオブジェクトを設定
            URL url = new URL(url_str);
            //URLオブジェクトからHttpURLConnectionオブジェクトを取得
            con = (HttpURLConnection) url.openConnection();
            //HTTP接続メソッドを設定
            con.setRequestMethod("GET");
            //接続
            con.connect();
            //HttpURLConnectionオブジェクトからレスポンスデータを取得
            is = con.getInputStream();
            //レスポンスデータであるInputStreamオブジェクトを文字列に変換する
            json_string = is2String(is);
        } catch(MalformedURLException ex) {
        } catch(IOException ex) {
        } finally {
            if (con != null) {
                con.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch(IOException ex) {
                }
            }
        }
        return json_string;
    }
    //InputStreamオブジェクトを文字列に変換する
    private String is2String(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while(0 <= (line = reader.read(b))) {
            sb.append(b, 0, line);
        }
        return sb.toString();
    }
    /**
     * JSON文字列から必要な情報を抽出してArrayListに格納する
     * playlistItemsの中身
     * 再生リストの中の動画を抽出
     *
     * @param json_string   JSON文字列
     * @param itemNum       JSON配列のインデックス
     * @param playlistItems 格納するArrayList
     */
    private void extractPlaylistItemsInformation(String json_string, int itemNum, ArrayList<ArrayList> playlistItems_list) {
        String videoId = "";
        String title = "";
        //JSON文字列から必要な情報を抽出
        try {
            //JSON文字列からJSONObjectオブジェクトを生成
            //これをルートJSONオブジェクトとする
            JSONObject rootJSON = new JSONObject(json_string);

            //ルートJSON直下の「items」JSON配列を取得
            JSONArray items = rootJSON.getJSONArray("items");
            //「items」JSON配列のインデックス(itemNum)のJSONオブジェクトを取得
            JSONObject itemsNow = items.getJSONObject(itemNum);
            //itemsNowから「snippet」JSONオブジェクトを取得
            JSONObject snippetJSON = itemsNow.getJSONObject("snippet");
            //snippetJSONから「title」文字列(動画タイトル)を取得
            title = snippetJSON.getString("title");
            //itemsNowから「contentDetails」JSONオブジェクトを取得
            JSONObject contentDetailsJSON = itemsNow.getJSONObject("contentDetails");
            //contentDetailsJSONから「videoId」文字列を取得
            videoId = contentDetailsJSON.getString("videoId");
        } catch (JSONException ex) {
        }
        ArrayList playlistItem = new ArrayList();
        playlistItem.add(videoId);
        playlistItem.add(title);
        playlistItems_list.add(playlistItem);
    }
    /**
     * JSON文字列から必要な情報を抽出してArrayListに格納する
     * playlistの情報
     * 再生リストの中の動画を抽出
     *
     * @param json_string          JSON文字列
     * @param playlist_information 格納するArrayList
     */
    private void extractPlaylistInformation(String json_string, ArrayList playlist_information) {
        int   totalResults = 0;
        int   resultsPerPage = 0;
        //JSON文字列から必要な情報を抽出
        try {
            //JSON文字列からJSONObjectオブジェクトを生成
            //これをルートJSONオブジェクトとする
            JSONObject rootJSON = new JSONObject(json_string);
            //ルートJSON直下の「pageInfo」JSONオブジェクトを取得
            JSONObject pageInfoJSON = rootJSON.getJSONObject("pageInfo");
            //「totalResults」数値を取得
            totalResults = pageInfoJSON.getInt("totalResults");
            //「resultsPerPage」数値を取得
            resultsPerPage = pageInfoJSON.getInt("resultsPerPage");
        } catch (JSONException ex) {
        }

        playlist_information.add(totalResults);
        playlist_information.add(resultsPerPage);
    }

    private String getNextPageToken(String json_string, String nextPageToken) {
    	//JSON文字列から必要な情報を抽出
        try {
            //JSON文字列からJSONObjectオブジェクトを生成
            //これをルートJSONオブジェクトとする
            JSONObject rootJSON = new JSONObject(json_string);
            //ルートJSON直下の「pageInfo」JSONオブジェクトを取得
            JSONObject pageInfoJSON = rootJSON.getJSONObject("pageInfo");
            //ルートJSON直下の「nextPageToken」文字列を取得
            nextPageToken = rootJSON.getString("nextPageToken");
        } catch (JSONException ex) {
        }

        return nextPageToken;
    }

}
