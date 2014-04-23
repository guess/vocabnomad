package ca.taglab.vocabnomad.details;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.*;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class AudioRecorderFragment extends Fragment {
    public static final String TAG = "AudioRecorderFragment";

    private static final int SAMPLE_RATE = 16000;

    private static final String AUDIO_FOLDER = "/VocabNomad/audio";

    private AudioRecord mRecorder;
    private File mRecording;
    private short[] mBuffer;
    private boolean mIsRecording;


    @Override
    public void onResume() {
        Log.d(TAG, "Recorder was initialized");
        initRecorder();
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Recorder was released");
        if (mIsRecording) {
            mRecorder.stop();
        }
        mRecorder.release();
        super.onStop();
    }

    /**
     * Start a recording
     * @return  True if the recording started, False if there is already a recording in progress_bar
     */
    protected boolean startRecording() {
        if (!mIsRecording) {
            mIsRecording = true;
            mRecorder.startRecording();
            mRecording = getFile("raw");
            startBufferedWrite(mRecording);
            return true;
        }
        return false;
    }

    /**
     * Stop the recording in progress_bar
     * @return  The file path of the recording, Null if one does not exist
     */
    protected String stopRecording() {
        if (mIsRecording) {
            mIsRecording = false;
            mRecorder.stop();
            File waveFile = getFile("wav");
            try {
                rawToWave(mRecording, waveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return waveFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Handle the current amplitude measured during the recording
     * @param amplitude The maximum extent of a vibration or oscillation
     */
    protected abstract void onAmplitudeChanged(int amplitude);

    protected abstract long entryId();


    private void initRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (mIsRecording) {
                        double sum = 0;
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                            sum += mBuffer[i] * mBuffer[i];
                        }
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                            onAmplitudeChanged((int) Math.sqrt(amplitude));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    onAmplitudeChanged(0);
                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /*
    private File getFile(final String suffix) {
        Time time = new Time();
        time.setToNow();
        return new File(Environment.getExternalStorageDirectory(), time.format("%Y%m%d%H%M%S") + "." + suffix);
    } */


    private File getFile(final String suffix) {
        // The audio file will be saved in external storage
        File folder = new File(Environment.getExternalStorageDirectory() + AUDIO_FOLDER);
        boolean isFolderCreated = true;
        if (!folder.exists()) {
            Log.d(TAG, "Creating audio folder: " + AUDIO_FOLDER);
            isFolderCreated = folder.mkdirs();
        }

        if (isFolderCreated) {
            return new File(folder, entryId() + "-" + System.currentTimeMillis() + "." + suffix);
        } else {
            Log.e(TAG, "Error: Audio folder was not found or created");
            return new File(Environment.getExternalStorageDirectory(), "temp." + suffix);
        }
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}
