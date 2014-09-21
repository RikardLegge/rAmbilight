package com.rambilight.plugins.Music;

import java.awt.MenuItem;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import com.rambilight.core.serial.LightHandler;
import com.rambilight.core.ui.TrayController.CustomCreator;
import com.rambilight.plugins.Module;

public class Music extends Module {

    Recorder     recorder;
    LightHandler lightHandler;
    long         lastRec        = 0;
    boolean      notImplemented = true;

    OutputStream audioListener;

    public Music() throws Exception {
        audioListener = new AudioListener();
    }

    public void dispose() {
        recorder.stopRecording();
    }

    public void resume() {
        if (recorder != null)
            try {
                recorder.stopRecording();
            } catch (Exception e) {}
        recorder = new Recorder(audioListener);
        recorder.startRecording();
    }

    public void loaded() {
        recorder = new Recorder(audioListener);
        recorder.startRecording();
    }

    public void suspend() {
        recorder.stopRecording();
    }

    class AudioListener extends OutputStream {

        @Override public void write(int b) throws IOException {
            //System.out.println(b);
            // lightHandler.addToUpdateBuffer(30, (byte)255, (byte)255, (byte)255);
        }
    }

    public CustomCreator getTrayCreator() {
        return () -> {
            return new MenuItem[0];
        };
    }

}

class Recorder extends Thread {

    private TargetDataLine       line;
    private AudioFileFormat.Type targetType  = AudioFileFormat.Type.AU;
    private AudioInputStream     inputStream;
    private OutputStream         audioListener;

    private static AudioFormat   audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,    // Encoding technique
                                                     41000.0F,                            // Sample Rate
                                                     16,                                 // Number of bits in each channel
                                                     2,                                  // Number of channels (2=stereo)
                                                     4,                                  // Number of bytes in each frame
                                                     41000.0F,                            // Number of frames per second
                                                     false);                                         ;

    private static DataLine.Info info        = new DataLine.Info(TargetDataLine.class, audioFormat);

    public Recorder(OutputStream audioListener) {
        this.audioListener = audioListener;

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            inputStream = new AudioInputStream(line);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRecording() {
        line.start();
        start();
    }

    public void stopRecording() {
        line.stop();
        line.close();
    }

    public void run() {
        try {
            AudioSystem.write(this.inputStream, this.targetType, audioListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}