package com.sdf.how_old;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;

public class MainActivity extends Activity {

	private ImageView mImageView;
	private Button mDetect;
	private Button mGetImage;
	private TextView mTip;
	private View mWaiting;
	// 当前图片路径
	private String mCurrentImageStr;
	private Bitmap mBitmap;
	// 画笔
	private Paint mPaint;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Constant.MSG_SUCCES:
				mWaiting.setVisibility(View.GONE);
				JSONObject rs = (JSONObject) msg.obj;
				prepareRsBitmap(rs);
				mImageView.setImageBitmap(mBitmap);

				break;
			case Constant.MSG_ERROR:
				mWaiting.setVisibility(View.GONE);
				String errorMessage = (String) msg.obj;
				if (TextUtils.isEmpty(errorMessage)) {

					mTip.setText("Error!");

				} else {

					mTip.setText(errorMessage);
				}

				break;
			default:
				break;
			}

		};

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		initEvents();
		mPaint = new Paint();
	}

	public void initEvents() {
		mDetect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mWaiting.setVisibility(View.VISIBLE);
				if (mCurrentImageStr != null
						&& !mCurrentImageStr.trim().equals("")) {

					resizeImage();

				} else {
					mBitmap = BitmapFactory.decodeResource(getResources(),
							R.drawable.p2);

				}
				FaceppDetect.detect(mBitmap, new FaceppDetect.CallBack() {

					@Override
					public void success(JSONObject result) {
						// TODO Auto-generated method stub
						Message message = Message.obtain();
						message.what = Constant.MSG_SUCCES;
						message.obj = result;
						mHandler.sendMessage(message);
					}

					@Override
					public void error(FaceppParseException exception) {
						// TODO Auto-generated method stub
						Message message = Message.obtain();
						message.what = Constant.MSG_ERROR;
						message.obj = exception.getErrorMessage();
						mHandler.sendMessage(message);
					}

				});
			}
		});
		mGetImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent, Constant.PICK_CODE);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub

		if (requestCode == Constant.PICK_CODE) {
			if (intent != null) {
				Uri imageUri = intent.getData();
				Cursor cursor = getContentResolver().query(imageUri, null,
						null, null, null);
				cursor.moveToFirst();
				int id = cursor
						.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				mCurrentImageStr = cursor.getString(id);
				cursor.close();

				resizeImage();
				mImageView.setImageBitmap(mBitmap);
				mTip.setText("点击识别 ====>");
			}

		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	public void prepareRsBitmap(JSONObject rs) {

		Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(),
				mBitmap.getHeight(), mBitmap.getConfig());
		// 创建画布
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(mBitmap, 0, 0, null);

		try {
			JSONArray faces = rs.getJSONArray("face");
			int faceCount = faces.length();
			mTip.setText("找到" + faceCount + "张脸");
			for (int i = 0; i < faceCount; i++) {
				JSONObject face = faces.getJSONObject(i);
				JSONObject position = face.getJSONObject("position");
				float x = (float) position.getJSONObject("center").getDouble(
						"x");
				float y = (float) position.getJSONObject("center").getDouble(
						"y");

				float w = (float) position.getDouble("width");
				float h = (float) position.getDouble("height");

				x = x / 100 * bitmap.getWidth();
				y = y / 100 * bitmap.getHeight();

				w = w / 100 * bitmap.getWidth();
				h = h / 100 * bitmap.getHeight();
				mPaint.setColor(0xffffffff);
				// 设置3个像素
				mPaint.setStrokeWidth(5);
				// 画框
				canvas.drawLine(x - w / 2, y - h / 2, x - w / 2, y + h / 2,
						mPaint);
				canvas.drawLine(x - w / 2, y - h / 2, x + w / 2, y - h / 2,
						mPaint);
				canvas.drawLine(x + w / 2, y - h / 2, x + w / 2, y + h / 2,
						mPaint);
				canvas.drawLine(x - w / 2, y + h / 2, x + w / 2, y + h / 2,
						mPaint);

				int age = face.getJSONObject("attribute").getJSONObject("age")
						.getInt("value");
				String gender = face.getJSONObject("attribute")
						.getJSONObject("gender").getString("value");
				Bitmap ageBitmap = buildeAgeBitmap(age, "Male".equals("gender"));

				int ageWidth = ageBitmap.getWidth();
				int ageHeight = ageBitmap.getHeight();
				if (bitmap.getWidth() < mImageView.getWidth()
						&& bitmap.getHeight() < mImageView.getHeight()) {

					float ratio = Math.max(bitmap.getWidth() * 1.0f
							/ mImageView.getWidth(), bitmap.getHeight() * 1.0f
							/ mImageView.getHeight());
					ageBitmap = Bitmap.createScaledBitmap(ageBitmap,
							(int) (ageWidth * ratio),
							(int) (ageHeight * ratio), false);
				}
				canvas.drawBitmap(ageBitmap, x - ageBitmap.getWidth() / 2, y
						- h / 2 - ageBitmap.getHeight(), null);
				mBitmap = bitmap;

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Bitmap buildeAgeBitmap(int age, boolean isMale) {
		// TODO Auto-generated method stub
		TextView tv = (TextView) findViewById(R.id.age_gender);
		tv.setText(age + "");
		if (isMale) {
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(R.drawable.male), null, null, null);

		} else {
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources()
					.getDrawable(R.drawable.female), null, null, null);
		}
		tv.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
		tv.destroyDrawingCache();

		return bitmap;
	}

	/**
	 * 压缩图片，应该是目前最常用的图片压缩方式了
	 */
	public void resizeImage() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只读取图片的大小不显示
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(mCurrentImageStr, options);
		// 图片缩放的倍数
		double ratio = Math.max(options.outWidth * 1.0d / 1024f,
				options.outHeight * 1.0d / 1024f);

		options.inSampleSize = (int) Math.ceil(ratio);

		options.inJustDecodeBounds = false;
		mBitmap = BitmapFactory.decodeFile(mCurrentImageStr, options);

	}

	public void initViews() {
		mImageView = (ImageView) findViewById(R.id.image);
		mDetect = (Button) findViewById(R.id.detect);
		mGetImage = (Button) findViewById(R.id.getimage);
		mTip = (TextView) findViewById(R.id.tip);
		mWaiting = findViewById(R.id.waiting);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
