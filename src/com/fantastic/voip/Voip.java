package com.fantastic.voip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fantastic.makcal.R;

public class Voip extends Activity {

	private Button endCall;
	private TextView contact;
	public byte[] buffer;
	String ipAddress; // = "192.168.137.12"; //
	int PORT = 8001;
	static final int frequency[] = { 8000, 11025, 16000, 22050, 24000, 32000,
			44100, 48000 };
	private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	int i = 1;
	// public static final int PORT = 8008;
	private int playBufSize;
	private AudioTrack audioTrack;
	private int recBufSize;
	private AudioRecord audioRecord;
	ProgressDialog dialog;
	Socket Receive;
	private Socket Transmit;
	private ServerSocket ssocket;
	protected DataInputStream dIn;
	BufferedOutputStream bos;
	BufferedInputStream bis;
	DataOutputStream dOut;
	boolean Status = true;
	int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voip);
		contact = (TextView) findViewById(R.id.callerID);
		contact.setText("Testing Client");
		endCall = (Button) findViewById(R.id.stop_button);

		Bundle CallerID = getIntent().getExtras();
		contact.setText(CallerID.getString("callerID"));
		ipAddress = CallerID.getString("ipAddress");
		int callType = CallerID.getInt("callType");

		dialog = ProgressDialog.show(this, "Dailing", "Please wait..", true);
		switch (callType) {
		case 1: // caller
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Receive = new Socket(ipAddress, PORT);
					} catch (Exception e) {
						e.printStackTrace();
					}
					playBufSize = AudioTrack.getMinBufferSize(frequency[i],
							channelConfiguration, audioEncoding);
					audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
							frequency[i], channelConfiguration, audioEncoding,
							playBufSize, AudioTrack.MODE_STREAM);

					Playback(playBufSize, audioTrack, Receive);
					Log.e("Playback", "Playback initialised");
				}

			}).start();
		case 2: // receiver
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						ssocket = new ServerSocket(PORT);
						Transmit = ssocket.accept();
						Log.d("Socket", "Socket Created");

					} catch (Exception e) {
						e.printStackTrace();
					}

					recBufSize = AudioRecord.getMinBufferSize(frequency[i],
							channelConfiguration, audioEncoding);
					// recBufSize = 4096;
					audioRecord = new AudioRecord(
							MediaRecorder.AudioSource.MIC, frequency[i],
							channelConfiguration, audioEncoding, recBufSize);
					Log.e("Recorder", "Recorder Initialized");
					Record(recBufSize, audioRecord, Transmit);
					Log.e("Record", "Recorder initilized");
				}

			}).start();
			break;
		default:
			Log.e("Voip Client", "Error in feedback");
			finish();

		}
		// start VoiceCient to send voice

		endCall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopCall();

				Log.d("VS", "Recorder released");
				finish();
			}

		});
	}

	public void showToast(final String value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}

	void Playback(final int playBufSize, final AudioTrack audioTrack,
			final Socket socket) {
		// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		buffer = new byte[playBufSize];
		audioTrack.play();
		Log.e("Speaker", "Speaker started");
		try {
			// bis = new BufferedInputStream(Receive.getInputStream());
			dIn = new DataInputStream(socket.getInputStream());

			// ObjectInputStream(socket.getInputStream());
			Log.d("Speaker", "DataInput");

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int readSize = 0;// not needed
		while (Status) {

			try {
				Log.d("Speaker", "Waiting for buffer read...");
				readSize = dIn.read(buffer, 0, buffer.length);

				if (dialog.isShowing())
					dialog.dismiss();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			Log.e("Speaker", "Received Packet of Size " + readSize);

			audioTrack.write(buffer, 0, readSize);
			Log.e("Speaker", "writing to speaker");
		}
		audioTrack.stop();

	}

	void Record(final int recBufSize, final AudioRecord audioRecord,
			final Socket socket) {
		Log.e("Recorder", "Recorder Initiallized");
		new Thread(new Runnable() {

			byte[] buffer = new byte[recBufSize];

			@Override
			public void run() {
				// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				try {
					dOut = new DataOutputStream(socket.getOutputStream());
					if (dialog.isShowing())
						dialog.dismiss();
					Log.e("Recorder", "Rec Buffer size is " + recBufSize);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("HTTP Output",
							"Couldn't create a bufferedOutput stream");
					return;
				}

				audioRecord.startRecording();
				Log.i("Recorder", "Starting Recorder");

				while (Status) {
					int writeSize = audioRecord.read(buffer, 0, recBufSize);
					Log.e("Recorder", "Read size is " + recBufSize);
					try {
						dOut.write(buffer, 0, writeSize);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("HTTP Output", "Couldnt write to output stream");
					}
				}
				audioRecord.stop();

			}

		}).start();
	}

	protected void stopCall() {
		// TODO Auto-generated method stub
		audioTrack.stop();
		audioRecord.stop();
		Status = false;

		try {
			dOut.close();
			Receive.close();
			bis.close();
			bos.close();
			ssocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			finish();
		}

	}
}
