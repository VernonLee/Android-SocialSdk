package com.nodlee.wx.wxapi;

import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.nodlee.wx.Constants;
import com.nodlee.wx.HttpManager;
import com.nodlee.wx.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;

public class WXEntryActivity extends Activity {
	private static final String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	private static final String GET_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(getIntent());
	}
	
	private void handleIntent(Intent intent) {
		if (getIntent() == null) return;
		
		SendAuth.Resp response = new SendAuth.Resp(intent.getExtras());
		if (response.errCode == BaseResp.ErrCode.ERR_OK) {
			// 同意登录
			String code = response.code;
			String state = response.state;
			// 如果不是微信登录
			if (!state.equals("wechat_login")) {
				finish();
			}
			
			String tokenUrl = String.format(GET_ACCESS_TOKEN_URL, Constants.WECHAT_APP_ID,
					Constants.WECHAT_APP_SECRET, code);
			getAccessTokenAndOpenId(tokenUrl);
		} else {
			finish();
		}
	}
	
	private void getAccessTokenAndOpenId(String url) {
		new WxTask() {
			protected void onPostExecute(String result) {
				if(result == null) {
					Toast.makeText(WXEntryActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
					return;
				}
				
				try {
					JSONObject json = new JSONObject(result);
					String accessToken = json.getString("access_token");
					// String refreshToken = json.get("refresh_token").getAsString();
					String openId = json.getString("openid");
					// String unionId = json.get("unionid").getAsString();
					
					String urlSpec = String.format(GET_USER_INFO_URL, accessToken, openId);
					getUserInfo(urlSpec);
				} catch (JSONException e) {
					Log.e("XXXX", "解析返回数据失败:" + e);
				}
			};
		}.execute(url);
	}
	
	// 根据token和openid获取用户信息
	private void getUserInfo(String url) {
		new WxTask() {
			protected void onPostExecute(String result) {
				if(result == null) {
					Toast.makeText(WXEntryActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
					return;
				}
				
				try {
					JSONObject json = new JSONObject(result);
					String nickname = json.getString("nickname");
					String openId  = json.getString("openid");
				
					Intent intent = new Intent("wechat");
					intent.putExtra("nickname", nickname);
					intent.putExtra("openid", openId);
					sendBroadcast(intent);
				} catch (JSONException e) {
					Log.e("XXXX", "解析返回数据失败:" + e);
				}
				finish();
			};
		}.execute(url);
	}
	
	private class WxTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... params) {
			try {
				URL url = new URL(params[0]);
				return new HttpManager().executeGet(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null; 
		}
	}
}
