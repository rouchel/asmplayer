package com.maxwit;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

class MediaFile {
	URL url;
	File file;
	RandomAccessFile rAccessFile;
	Boolean isUsed;
	HttpDownload download;
	Socket socket;
	DataInputStream in;
	PrintWriter out;
	HashMap<String, String> head;

	private void parseHeadString(String headString) {
		String[] hash;

		if (headString.matches(".*:.*")) {
			hash = headString.split(": +");
			head.put(hash[0], hash[1]);
		}
	}

	private void getHead() {
		try {
			String headString;
			while ((headString = in.readLine()) != null) {
				if (headString.length() == 0) {
					break;
				}

				parseHeadString(headString);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MediaFile(String downUrl, String path) throws UnknownHostException,
			IOException {
		url = new URL(downUrl);
		file = new File(path);
		file.createNewFile();
		file.setReadable(true, false);
		rAccessFile = new RandomAccessFile(file, "rw");
		isUsed = true;
		download = new HttpDownload();

		socket = new Socket(url.getHost(), 80);
		in = new DataInputStream(socket.getInputStream());
		out = new PrintWriter(socket.getOutputStream(), true);

		head = new HashMap<String, String>();

		out.print("GET " + url.getPath() + " HTTP/1.0 \r\n\r\n");
		out.flush();
		getHead();
	}

	class HttpDownload extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] buffer = new byte[512];
			int length;

			super.run();
			try {
				while ((length = in.read(buffer, 0, 512)) > 0) {
					rAccessFile.write(buffer, 0, length);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void getFile() {
		download.start();
	}

	public void destroy() {
		download.destroy();
	}
}

class SyncSeekbar extends Thread {
	MediaPlayer mediaPlayer;
	SeekBar seekBar;
	int max;
	int old;
	boolean status;
	EditText time;

	public SyncSeekbar(MediaPlayer mp, SeekBar bar, EditText timeView) {
		mediaPlayer = mp;
		seekBar = bar;
		max = 0;
		old = 0;
		status = true;
		time = timeView;
	}

	public void setStatus(boolean s) {
		status = s;
	}
	@Override
	public void run() {
		int cur;
		// TODO Auto-generated method stub
		super.run();
		// TODO:
		while (true) {
			if (mediaPlayer.isPlaying() && status) {
				if (max != mediaPlayer.getDuration()) {
					max = mediaPlayer.getDuration();
					seekBar.setMax(max);
				}

				cur = mediaPlayer.getCurrentPosition();
				if (old != cur) {
					seekBar.setProgress(cur);
					old = cur;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

public class AsmplayerActivity extends Activity implements Callback, OnSeekBarChangeListener {
	/** Called when the activity is first created. */
	MediaPlayer mediaPlayer;
	SurfaceView surface;
	SurfaceHolder holder;
	SeekBar seekBar;
	EditText time;
	Button playButton;
	EditText urlEdit;
	String url;
	String path;
	MediaFile mediaFile;
	SyncSeekbar syncSeekbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		surface = (SurfaceView) findViewById(R.id.surfaceView);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		urlEdit = (EditText) findViewById(R.id.urlEdit);
		playButton = (Button) findViewById(R.id.playButton);
		time = (EditText) findViewById(R.id.textView);

		holder = surface.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDisplay(holder);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		time.setVisibility(1);
		syncSeekbar = new SyncSeekbar(mediaPlayer, seekBar, time);
		syncSeekbar.start();

		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				url = urlEdit.getText().toString();
				path = "/data/tmp/__mediafile.mp4";
				playStart();
			}
		});

		seekBar.setOnSeekBarChangeListener(this);
	}

	private void playStart() {
		// TODO:
		if (mediaFile != null) {
			mediaFile.destroy();
			mediaPlayer.reset();
		}

		try {
			mediaFile = new MediaFile(url, path);
			mediaFile.getFile();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			holder.setFixedSize(mediaPlayer.getVideoWidth(),
//					mediaPlayer.getVideoHeight());
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			mediaPlayer.start();
		}catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mediaPlayer.stop();

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		time.setText(String.valueOf(mediaPlayer.getCurrentPosition() / 60000) + ":" + String.valueOf(mediaPlayer.getCurrentPosition() / 1000 % 60));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		syncSeekbar.setStatus(false);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		syncSeekbar.setStatus(true);
		mediaPlayer.seekTo(seekBar.getProgress());
	}
}
