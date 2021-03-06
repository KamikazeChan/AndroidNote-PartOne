package cn.bingoogol.zxing.ui.activity;

import java.util.Random;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.bingoogol.zxing.R;
import cn.bingoogol.zxing.util.Logger;

import com.zxing.encoding.EncodingHandler;

public class MainActivity extends BaseActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private TextView mResultTv;
	private ImageView mResultIv;
	private EditText mContentEt;

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		mResultTv = (TextView) findViewById(R.id.tv_main_result);
		mResultIv = (ImageView) findViewById(R.id.iv_main_result);
		mContentEt = (EditText) findViewById(R.id.et_main_content);
	}

	@Override
	protected void setListener() {
		findViewById(R.id.btn_main_scan).setOnClickListener(this);
		findViewById(R.id.btn_main_qrcode).setOnClickListener(this);
	}

	@Override
	protected void afterViews(Bundle savedInstanceState) {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_main_scan:
			Intent scanIntent = new Intent(mApp, ScanActivity.class);
			startActivityForResult(scanIntent, 0);
			break;
		case R.id.btn_main_qrcode:
			try {
				long startTime = System.currentTimeMillis();
				//String content = mContentEt.getText().toString().trim();
				Random random = new Random();
				String content = "http://www.baidu.com/" + random.nextLong() + "?wd=runonuithread&rsv_spt=1&issp=1&rsv_bp=0&ie=utf-8&tn=baiduhome_pg&rsv_sug3=7&rsv_sug4=270&rsv_sug1=7&oq=runonui&rsv_sug2=0&f=3&rsp=0&inputT=2961";
				mContentEt.setText(content);
				Bitmap image = null;
				if (content != null && !"".equals(content)) {
					image = EncodingHandler.createQRCode(content, 400);
					if (image != null) {
						mResultIv.setImageBitmap(image);
					}
				}
				Logger.i(TAG, "耗时:" + (System.currentTimeMillis() - startTime));
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			mResultTv.setText(scanResult);
		}
	}
}