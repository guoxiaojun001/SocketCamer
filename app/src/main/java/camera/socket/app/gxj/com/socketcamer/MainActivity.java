package camera.socket.app.gxj.com.socketcamer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CameraPreview mPreview;
    private CameraManager mCameraManager;
    private boolean mIsOn = true;
    private SocketClient mThread;
    private Button mButton;
    private String mIP;
    private int mPort = 8888;

    Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button_capture);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                if (mIsOn) {
                    if (mIP == null) {
                        mThread = new SocketClient(mPreview);
                    } else {
                        mThread = new SocketClient(mPreview, mIP, mPort);
                    }

                    mIsOn = false;
                    mButton.setText(R.string.stop);
                } else {
                    closeSocketClient();
                    reset();
                }
            }
        });
        mCameraManager = new CameraManager(this);
        // Create our Preview view and set it as the content of our activity.
        camera = mCameraManager.getCamera();
        mPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(camera != null){
            camera.release();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ipcamera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                setting();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setting() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.server_setting,
                null);
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.setting_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                EditText ipEdit = (EditText) textEntryView
                                        .findViewById(R.id.ip_edit);
                                EditText portEdit = (EditText) textEntryView
                                        .findViewById(R.id.port_edit);
                                mIP = ipEdit.getText().toString();
                                mPort = Integer.parseInt(portEdit.getText()
                                        .toString());

                                Toast.makeText(MainActivity.this,
                                        "New address: " + mIP + ":" + mPort,
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

								/* User clicked cancel so do some stuff */
                            }
                        }).create();
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeSocketClient();
        mPreview.onPause();
        mCameraManager.onPause(); // release the camera immediately on pause
        // event
        reset();
    }

    private void reset() {
        mButton.setText(R.string.start);
        mIsOn = true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mCameraManager.onResume();
        mPreview.setCamera(mCameraManager.getCamera());
    }

    private void closeSocketClient() {
        if (mThread == null)
            return;

        mThread.interrupt();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mThread = null;
    }
}
