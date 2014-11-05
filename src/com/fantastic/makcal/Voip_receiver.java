package com.fantastic.makcal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Voip_receiver extends Activity {
	
	private AudioTrack speaker;

	private Button startButton,stopButton;

	public byte[] buffer;
	public DatagramSocket socket;
	
	
	private int sampleRate = 11025;   //make it 14400
	private int channelConfig = AudioFormat.CHANNEL_IN_MONO;    
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;   
	//new bread just
	/*static final int AUDIO_PORT = 5000;
    static final int SAMPLE_RATE = 8000;
    static final String LOG_TAG = "UdpStream";
    static final int SAMPLE_INTERVAL = 20; // milliseconds
    static final int SAMPLE_SIZE = 2; // bytes per sample
	
	static final int BUF_SIZE = SAMPLE_INTERVAL*SAMPLE_INTERVAL*SAMPLE_SIZE*2;*/
	//end of new bread just
	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
	//int minBufSize = 250;
	private boolean status = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voip);

		startButton = (Button) findViewById (R.id.start_button);
		stopButton = (Button) findViewById (R.id.stop_button);

		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
					status = true;
					startReceiving();           
				}
			});
		
		stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                status = false;
                speaker.release();
                //recorder.release();
                Log.d("VS", "Recorder released");

            }

        });
		//minBufSize += 2048;
		//minBufSize = minBufSize*10;
		String value = ("minBufSize: " + minBufSize);
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	} /*
    public void startReceiving()
    {
        Thread thrd = new Thread(new Runnable() {
                        @Override
            public void run() 
            {
                Log.e(LOG_TAG, "start recv thread, thread id: "
                    + Thread.currentThread().getId());
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 
                        sampleRate, AudioFormat.CHANNEL_IN_MONO, 
                        AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, 
                        AudioTrack.MODE_STREAM);
                track.play();
                try
                {
                    DatagramSocket sock = new DatagramSocket(AUDIO_PORT);
                    byte[] buf = new byte[BUF_SIZE];

                    while(true)
                    {
                        DatagramPacket pack = new DatagramPacket(buf, BUF_SIZE);
                        sock.receive(pack);
                        Log.d(LOG_TAG, "recv pack: " + pack.getLength());
                        track.write(pack.getData(), 0, pack.getLength());
                    }
                }
                catch (SocketException se)
                {
                    Log.e(LOG_TAG, "SocketException: " + se.toString());
                }
                catch (IOException ie)
                {
                    Log.e(LOG_TAG, "IOException" + ie.toString());
                }
            } // end run
        });
        thrd.start();
    } */

public void startReceiving() {

        Thread receiveThread = new Thread (new Runnable() {

            @Override
            public void run() {

                try {

                    DatagramSocket socket = new DatagramSocket(5000);
                    Log.d("VR", "Socket Created");


                    byte[] buffer = new byte[11025];
                    speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize,AudioTrack.MODE_STREAM);



                    while(status == true) {
                        try {


                            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                            socket.receive(packet);
                            Log.d("VR", "Packet Received");

                            //reading content from packet
                            buffer=packet.getData();
                            Log.d("VR", "Packet data read into buffer");

                            //sending data to the Audiotrack obj i.e. speaker
                            speaker.write(buffer, 0, minBufSize); //buffer.length

                            Log.d("VR", String.valueOf(buffer));

                            speaker.play();

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