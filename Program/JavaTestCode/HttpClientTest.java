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

public class HttpClientTest {

	public static void main(String[] args) {
		//ニューラジオ再生リスト
		//String PLAYLISTID = "PLDCx5WcWNkqpmyT4EGdKe4xTZxiB9m_oU";

		//ライバロリ再生リスト
		String PLAYLISTID = "PLecwb-GfnfWGCOw_WRGlJgUmyMkkpczAT";

		String playlistItems_json_string  = getJSONString(PLAYLISTID, "");

		System.out.println(playlistItems_json_string);
		System.out.println("=======================");

		ArrayList<ArrayList<?>> playlistItems_list = new ArrayList<>();

		for(int itemNum = 0; itemNum < 8; itemNum++) {
			editResult(playlistItems_json_string, itemNum, playlistItems_list);
		}


		for (int i = 0; i < playlistItems_list.size(); i++) {
			System.out.println(playlistItems_list.get(i));
		}

		//ArrayList<String> playlistItems_list_contents = playlistItems_list.get(0);
		//String nextPageToken = playlistItems_list_contents.get(0);

		//System.out.println(nextPageToken);

		//String result  = getJSONString(PLAYLISTID, nextPageToken);
		//System.out.println(result);

	}

	//YoutubeDataAPIを使用して再生リストのJSON文字列を返す
	public static String getJSONString(String PLAYLISTID, String nextPageToken) {
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
	private static String is2String(InputStream is) throws IOException {
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
	 * JSON文字列から必要な情報を抽出してListに格納する
	 *
	 * @param json_string   JSON文字列
	 * @param itemNum       JSON配列のインデックス
	 * @param playlistItems 格納するList
	 */
	public static void editResult(String json_string, int itemNum, ArrayList<ArrayList<?>> playlistItems_list) {
		int totalResults = 0;
		String nextPageToken = "";

        String videoId = "";
        String title = "";

        //JSON文字列から必要な情報を抽出
		try {
			//JSON文字列からJSONObjectオブジェクトを生成
			//これをルートJSONオブジェクトとする
			JSONObject rootJSON = new JSONObject(json_string);

			//ルートJSON直下の「pageInfo」JSONオブジェクトを取得
			JSONObject pageInfoJSON = rootJSON.getJSONObject("pageInfo");
			//「totalResults」数値を取得
			totalResults = pageInfoJSON.getInt("totalResults");
			//totalResults = totalResultsInt.toString();

			//ルートJSON直下の「nextPageToken」文字列を取得
			//nextPageToken = rootJSON.getString("nextPageToken");


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

		playlistItem.add(totalResults);
		//System.out.println("test");
		//System.out.println(totalResults);

		//playlistItem.add(nextPageToken);

		playlistItem.add(videoId);
		playlistItem.add(title);
		playlistItems_list.add(playlistItem);

	}

}