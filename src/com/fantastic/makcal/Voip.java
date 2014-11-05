package com.fantastic.makcal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Voip extends Activity {

	private Button startButton,stopButton;
	private TextView contact;
	private String ipAddress;
	public byte[] buffer;
	public DatagramSocket socket;
	AudioRecord recorder;
	AudioTrack track;

	private int sampleRate = 44100;   //make it 14400
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;    
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       
	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
	//int minBufSize = 250;
	private boolean status = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voip);
		contact = (TextView)findViewById(R.id.callerID);
		startButton = (Button) findViewById (R.id.start_button);
		stopButton = (Button) findViewById (R.id.stop_button);
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		Bundle CallerID = getIntent().getExtras();
		
		contact.setText(CallerID.getString("callerID"));
		ipAddress = CallerID.getString("ipAddress");
		
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				// TODO Auto-generated method stub
					status = true;
					if(checkWifi()){
					startStreamingIn();
					startStreamingOut(ipAddress); 
					}
				}
			});
		
		stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	
            	startButton.setEnabled(true);
				stopButton.setEnabled(false);
                status = false;
                recorder.release();
                
                Log.d("VS", "Recorder released");

            }

        });
		//minBufSize += 2048;
		//minBufSize = minBufSize*10;
		minBufSize = 2048;
		String value = ("minBufSize: " + minBufSize);
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}
	
	public void showToast(final String value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}
	
	public boolean checkWifi() {

		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (wifi.isAvailable()) {
			// showToast("Wifi Connected");
			status = true;

		} else {
			// dialog.dismiss();
			showToast("Wifi Not Detected");
			status = false;

		}

		return status;
	}

	public void startStreamingOut(final String ipAddress)
	{
		Thread streamThread = new Thread(new Runnable(){
			@Override
			public void run()
			{
				try{

					DatagramSocket socket = new DatagramSocket();
					Log.d("VS", "Socket Created");

					byte[] buffer = new byte[minBufSize];

					Log.d("VS","Buffer created of size " + minBufSize);


					Log.d("VS", "Address retrieved");
					recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);
					Log.d("VS", "Recorder initialized");


					recorder.startRecording();
					

					InetAddress destination = InetAddress.getByName(ipAddress); //server address
					//byte[] sendData = new byte[1024];
					//byte[] receiveData = new byte[1024];


					while (status == true)
					{

						/*DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 50005);
						socket.send(sendPacket);*/
						 // reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        // putting buffer in the packet
                        DatagramPacket packet;
                        packet = new DatagramPacket(buffer, buffer.length,
                                destination, 5000);

                        socket.send(packet);
                        //System.out.println("MinBufferSize: " + minBufSize);
					}

				} catch(UnknownHostException e) {
					Log.e("VS", "UnknownHostException");
				} catch (IOException e) {
					Log.e("VS", "IOException");
					e.printStackTrace();
				} 


			}

		});
		streamThread.start();
	}
	
public void startStreamingIn() {
	final int sampleRate = 44100;   //make it 14400
	final int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;    
	final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       
	

        Thread receiveThread = new Thread (new Runnable() {

            @Override
            public void run() {

                try {

                    DatagramSocket socket = new DatagramSocket(5000);
                    Log.d("VR", "Socket Created");
                    byte[] buffer = new byte[1024]; //minBufSize
                    
                    socket.setReceiveBufferSize(buffer.length);

                    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    
                    track = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize,AudioTrack.MODE_STREAM);
                    track.play();
                    
                    while(status == true) {
                        try {


                            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                            socket.receive(packet);
                            Log.d("VR", "Packet Received");

                            //reading content from packet
                            buffer=packet.getData();
                            Log.d("VR", "Packet data read into buffer");

                            //sending data to the Audiotrack obj i.e. speaker
                            track.write(buffer, 0, packet.getLength()); //buffer.length

                            Log.d("VR", String.valueOf(buffer));

                            track.play();

                        } catch(IOException e) {
                            Log.e("VR","IOException");
                        }
                    }


                } catch (SocketException e) {
                    Log.e("VR", "SocketException");
                }


            }

        });
        receiveThread.start();
    }

}