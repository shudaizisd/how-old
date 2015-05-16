package com.sdf.how_old;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

/**
 * @author sdf
 * 进行笑脸识别的类
 *
 */
public class FaceppDetect {
	/**
	 * @author sdf
	 * 实现接口，成功的时候返回的是json数据，而失败的时候返回错误
	 *
	 */
	public interface CallBack {
		void success(JSONObject result);

		void error(FaceppParseException exception);

	}

	/**
	 * @param bm
	 * @param callBack
	 * 识别类
	 */
	public static void detect(final Bitmap bm, final CallBack callBack) {
		// 耗时操作，使用线程
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					// 发送请求
					HttpRequests httpRequests = new HttpRequests(
							Constant.API_KEY, Constant.API_SECRET, true, true);
					Bitmap bmSmall = Bitmap.createBitmap(bm, 0, 0,
							bm.getWidth(), bm.getHeight());

					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bmSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);

					byte[] arrays = stream.toByteArray();

					PostParameters params = new PostParameters();
					params.setImg(arrays);
					JSONObject jsonObject = httpRequests
							.detectionDetect(params);
					Log.e("TAG", jsonObject.toString());
					if (callBack != null) {
						callBack.success(jsonObject);

					}
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if (callBack != null) {
						callBack.error(e);

					}

				}

			}

		}

		).start();

	}
}
