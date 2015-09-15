package com.nodlee.wx;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private BroadcastReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 只要设置当前Activity透明即可，一种方式是使用style
		// 另一种是设置没有ActionBar并且布局背景为透明
		setContentView(R.layout.activity_main);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login_wx:
			wxLogin();
			break;
		}
	}
	
	private void wxLogin() {
		IWXAPI api = WXAPIFactory.createWXAPI(this, Constants.WECHAT_APP_ID, true);
		api.registerApp(Constants.WECHAT_APP_ID);
		
		if (!api.isWXAppInstalled()) {
			Toast.makeText(this, "当前手机没有安装微信", Toast.LENGTH_SHORT).show();;
			return;
		}
		// 唤起微信登录授权
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo"; 
		req.state = "taotie_wx_login";
		api.sendReq(req);
		// 注册一个广播，监听微信的获取openid返回（类：WXEntryActivity中）
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("wechat");
		receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (intent != null && intent.getAction().equals("wechat")) {
					String nickname = intent.getStringExtra("nickname");
					String openId = intent.getStringExtra("openid");
					Log.i("XXXX", "微信名称:" + nickname + "微信opendId:" + openId);
					if (receiver != null) {
						unregisterReceiver(receiver);
					}
				}
			}
		};
		registerReceiver(receiver, intentFilter);
	}
}
