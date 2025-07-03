package androidmancala;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import androidmancala.animation.MarbleCollectionSound;
import androidmancala.menu.R;

import static android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE;

/**
 * Created by Dennis on 2018-01-16.
 */

public class MancalaSoundPool {

    private SoundPool soundPool;
    private SoundPool.Builder soundPoolBuilder;

    private AudioAttributes audioAttributes;
    private AudioAttributes.Builder audioAttributesBuilder;
    //The sound of the first marble might not be finished when we drop the second
    private static final int maxStreams=5;

    /*private int soundID_Marble1;
    private int soundID_Marble2;
    private int soundID_Marble3;
    private int soundID_Marble4;
    private int soundID_Marble5;
    private int soundID_Marble10;*/

    private Map<String,Integer> soundIDs;

    public MancalaSoundPool(Context context){
        //Setup sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributesBuilder = new AudioAttributes.Builder();
            audioAttributesBuilder.setUsage(AudioAttributes.USAGE_GAME);
            audioAttributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);

            audioAttributes = audioAttributesBuilder.build();
            soundPoolBuilder = new SoundPool.Builder();
            soundPoolBuilder.setAudioAttributes(audioAttributes);
            soundPool = soundPoolBuilder.setMaxStreams(maxStreams).build();
        }else
            soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC,0);

        soundIDs= new HashMap<>();
        soundIDs.put(getSoundIDKey(1,0),soundPool.load(context, R.raw.marble1,1));
        soundIDs.put(getSoundIDKey(2,0),soundPool.load(context, R.raw.marble2,1));
        soundIDs.put(getSoundIDKey(3,0),soundPool.load(context, R.raw.marble3,1));
        soundIDs.put(getSoundIDKey(4,0),soundPool.load(context, R.raw.marble4,1));
        soundIDs.put(getSoundIDKey(5,0),soundPool.load(context, R.raw.marble5,1));
        soundIDs.put(getSoundIDKey(10,0),soundPool.load(context, R.raw.marble10,1));
        /*AudioManager am = context.getSystemService(context.AUDIO_SERVICE);
        his.AudioManager.getProperty(PROPERTY_OUTPUT_SAMPLE_RATE);*/
;    }

    /*We choose sound based on on many marbles we drop into a pit and how many marbles the pit already contains
    //though right now we only implement 1,0
    //We could also change the sound (left,right) depending on drop location*/
    public void play(int drop,int pitMarbles,float leftVolume,float rightVolume) {
            int soundID=getSoundID(drop,pitMarbles);
            soundPool.play(soundID, leftVolume, rightVolume, 0, 0, 1);
    }

    public void play(MarbleCollectionSound sound) {
        int soundID=getSoundID(sound.getMarblesToDrop(),sound.getMarblesInPit());
        soundPool.play(soundID, sound.getLeft(), sound.getRight(), 0, 0, 1);
    }

    private String getSoundIDKey(int drop,int pitMarbles){
        return drop+","+pitMarbles;
    }

    private int getSoundID(int drop,int pitMarbles){
        if (drop<=5)
            return soundIDs.get(getSoundIDKey(drop,pitMarbles));
        if (drop<=8)
            return soundIDs.get(getSoundIDKey(5,pitMarbles));
        else
            return soundIDs.get(getSoundIDKey(10,pitMarbles));
    }

    public void realease(){
        if (soundPool!=null) {
            soundPool.release();
            soundPool=null;
        }
    }
}
