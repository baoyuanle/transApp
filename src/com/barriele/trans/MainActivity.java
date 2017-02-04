package com.barriele.trans;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements
		SwipeRefreshLayout.OnRefreshListener {

	private SwipeRefreshLayout mSwipeLayout;
	private TextView mText;
	public static String lastTextIn = "";
	//public static String lastTextOut = "";

	private static EditText _textIn;
	private static TextView _textOut;
	private static View _viewLine;
	private static Context _context;
	private static ImageView mFloat;

	private static boolean lastTransFail = false;
	
	private int[] myColor = { -11497739, -12145841};//-11749718 , -6763520
	private int colorIdx = 0;

	// 消息处理（自动打开键盘）
	public static Handler inputHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			View view = (View) msg.obj;
			openKeybord(view, _context, true);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_context = MainActivity.this;
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		mText = (TextView) findViewById(R.id.Text1);
		_textIn = (EditText) this.findViewById(R.id.EditTextIn);
		_viewLine = (View) this.findViewById(R.id.TextViewLine);
		_textOut = (TextView) this.findViewById(R.id.TextViewOut);
		mFloat = (ImageView) findViewById(R.id.img_float);

		_textIn.setOnEditorActionListener(transListener);
		// _textOut.setOnClickListener(new TextViewListener());

		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorSchemeResources(android.R.color.holo_green_light);
		// mSwipeLayout.setDistanceToTriggerSync(300);// 设置手指在屏幕下拉多少距离会触发下拉刷新
		mSwipeLayout.setSize(SwipeRefreshLayout.LARGE); // 设置圆圈的大小

		//_textIn.setText(Color.rgb(0x46, 0xab, 0x4f)+"k");颜色转换
		// //打开键盘
		Message msg = inputHandler.obtainMessage();
		msg.obj = _textIn;// 输入焦点位置
		inputHandler.sendMessageDelayed(msg, 200);
		// //

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);//按返回键后台
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/*@Override  
    protected void onRestart() {  
        super.onRestart();  
        
        _textIn.setText(lastTextIn);
        _textOut.setText(lastTextOut);
    } 
	
	@Override
	protected void onStart() {
		super.onStart();
		lastTextIn = "";
		// //打开键盘
		Message msg = inputHandler.obtainMessage();
		msg.obj = _textIn;// 输入焦点位置
		inputHandler.sendMessageDelayed(msg, 50);
		// //
	}*/

	private TextView.OnEditorActionListener transListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				openKeybord(_textIn, _context, false);
				translate();
			}
			return true;
		}
	};

	public void onimgClick(View view) {

		// Toast.makeText(this, "345665", Toast.LENGTH_SHORT).show();
		// _textOut.setText("454");
		// _textIn.setFocusable(true);
		// _textIn.setFocusableInTouchMode(true);
		// _textIn.requestFocus();
		// _textIn.requestFocusFromTouch();

		translate();
	}

	public static void openKeybord(View mEditText, Context mContext,
			boolean open) {
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (open) {
			mEditText.requestFocus();
			imm.showSoftInput(mEditText, 0);
		} else
			imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}

	public void onRefresh() {
		mText.setBackgroundColor(myColor[colorIdx]);
		mSwipeLayout.setColorSchemeColors(myColor[colorIdx]);
		_viewLine.setBackgroundColor(myColor[colorIdx]);
		colorIdx++;
		if (colorIdx > 1) {
			colorIdx = 0;
			mFloat.setImageResource(R.drawable.icgreen);
		}

		else if (colorIdx == 1) {
			mFloat.setImageResource(R.drawable.icblue);
		} else {
			//mFloat.setImageResource(R.drawable.icbg);
		}

		ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		if (c.hasPrimaryClip()) {
			_textIn.setText(c.getPrimaryClip().getItemAt(0).getText());
			translate();
		}
		// Toast.makeText(this, "已改变", Toast.LENGTH_SHORT).show();
		mSwipeLayout.setRefreshing(false);
	}

	public static void translate() {
		String str = _textIn.getText().toString();
		str = str.trim();
		if(lastTransFail)
			lastTransFail=false;
		else if (str.contentEquals(lastTextIn) || str.contentEquals("")) {
			_textIn.setText("");
			openKeybord(_textIn, _context, true);
			return;
		}
		lastTextIn = str;
		
		// Toast.makeText(_context, lastTextIn, Toast.LENGTH_SHORT).show();
		_textOut.setText("让我想想....");
		new Thread(_threadTrans).start();
	}

	private static Runnable _threadTrans = new Runnable() {
		@Override
		public void run() {
			getRequest(lastTextIn);
		}
	};

	// 1.有道翻译
	public static void getRequest(String str)  {
		String result = null;
		String url = "http://fanyi.youdao.com/openapi.do?keyfrom=fanyi-gq&key=830666132&type=data&doctype=json&version=1.1&q=";// 请求接口地址

		
		//要翻译的文本
		try {
			str=URLEncoder.encode(str,"UTF-8");
			result = net(url, str);
			orgnizeResult(result);
		} catch (Exception e) {
			lastTransFail=true;
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("text", "とAPI汚いですPY交易失敗\n悪い番号：え\n想了2秒仍无结果\n确认已打开网络？\n点下面按钮重试！");
			message.setData(bundle);//します
			_handlerOutput.sendMessage(message);
		}
	}

	private static void orgnizeResult(String str) throws Exception {
		Message message = new Message();
		Bundle bundle = new Bundle();

		JSONObject object = new JSONObject(str);
		if (object.getInt("errorCode") == 0) {
			String temp, strOut = "";
			JSONArray jsonArray;
			if (object.optJSONObject("basic") == null) {
				jsonArray = object.getJSONArray("translation");
			} else {
				object = object.getJSONObject("basic");
				jsonArray = object.getJSONArray("explains");
			}
			for (int i = 0; i < jsonArray.length(); i++) {
				strOut += jsonArray.get(i) + "\n";
			}
			temp = object.optString("phonetic", "123");
			if (!temp.contentEquals("123")) {
				strOut += "音：[" + temp + "]";
			}
			bundle.putString("text", strOut);
		} else {
			lastTransFail=true;
			bundle.putString("text", "とAPI服务汚いですPY交易失敗します\n悪い番号："+object.get("errorCode")  );
		}

		//bundle.putString("text", str);
		message.setData(bundle);
		_handlerOutput.sendMessage(message);
	}

	static Handler _handlerOutput = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			_textOut.setText(msg.getData().getString("text"));
		}
	};

	/**
	 *
	 * @param strUrl
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @return 网络请求字符串
	 * @throws Exception
	 */
	public static String net(String strUrl, String params) throws Exception {
		HttpURLConnection conn = null;
		BufferedReader reader = null;
		String rs = null;
		try {
			StringBuffer sb = new StringBuffer();
			strUrl = strUrl + params;

			URL url = new URL(strUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			conn.setConnectTimeout(700);//3s
			conn.setReadTimeout(1300);//3s
			conn.connect();
			InputStream is = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sb.append(strRead);
			}
			rs = sb.toString();
		} 
		finally {
			if (reader != null) {
				reader.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return rs;
	}
}
