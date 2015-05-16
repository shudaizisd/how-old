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
 * ����Ц��ʶ�����
 *
 */
public class FaceppDetect {
	/**
	 * @author sdf
	 * ʵ�ֽӿڣ��ɹ���ʱ�򷵻ص���json���ݣ���ʧ�ܵ�ʱ�򷵻ش���
	 *
	 */
	public interface CallBack {
		void success(JSONObject result);

		void error(FaceppParseException exception);

	}

	/**
	 * @param bm
	 * @param callBack
	 * ʶ����
	 */
	public static void detect(final Bitmap bm, final CallBack callBack) {
		// ��ʱ������ʹ���߳�
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					// ��������
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
