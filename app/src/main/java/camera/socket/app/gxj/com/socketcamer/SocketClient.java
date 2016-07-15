package camera.socket.app.gxj.com.socketcamer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * 要确保手机和client端和server端 在同一网络环境下
 */
public class SocketClient extends Thread {
	private Socket mSocket;
	private CameraPreview mCameraPreview;
	private static final String TAG = "socket";
	private String mIP = "10.2.9.210";//自己电脑的ip
	private int mPort = 8888;//设置端口，要与server端保持一致

	public SocketClient(CameraPreview preview, String ip, int port) {
		mCameraPreview = preview;
		mIP = ip;
		mPort = port;
		start();
	}

	public SocketClient(CameraPreview preview) {
		mCameraPreview = preview;
		start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		try {
			//第一种方式
			mSocket = new Socket(mIP, mPort);

			//第二种方式 采用 InetSocketAddress
//			mSocket = new Socket();
//			mSocket.connect(new InetSocketAddress(mIP, mPort), 10000); // hard-code

			BufferedOutputStream outputStream = new BufferedOutputStream(
					mSocket.getOutputStream());
			BufferedInputStream inputStream = new BufferedInputStream(
					mSocket.getInputStream());

			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("type", "data");
			jsonObj.addProperty("length", mCameraPreview.getPreviewLength());
			jsonObj.addProperty("width", mCameraPreview.getPreviewWidth());
			jsonObj.addProperty("height", mCameraPreview.getPreviewHeight());

			byte[] buff = new byte[1024];
			int len = 0;
			String msg = null;
			outputStream.write(jsonObj.toString().getBytes());
			outputStream.flush();

			while ((len = inputStream.read(buff)) != -1) {
				msg = new String(buff, 0, len);

				// JSON analysis
				JsonParser parser = new JsonParser();
				boolean isJSON = true;
				JsonElement element = null;
				try {
					element = parser.parse(msg);
				} catch (JsonParseException e) {
					Log.e(TAG, "exception: " + e);
					isJSON = false;
				}
				if (isJSON && element != null) {
					JsonObject obj = element.getAsJsonObject();
					element = obj.get("state");
					if (element != null && element.getAsString().equals("ok")) {
						// send data
						while (true) {
							outputStream.write(mCameraPreview.getImageBuffer());
							outputStream.flush();

							if (Thread.currentThread().isInterrupted())
								break;
						}

						break;
					}
				} else {
					break;
				}
			}

			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			// e.printStackTrace();
			Log.e(TAG, e.toString());
		} finally {
			try {
				mSocket.close();
				mSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
