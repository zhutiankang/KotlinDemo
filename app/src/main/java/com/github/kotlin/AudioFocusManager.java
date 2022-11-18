package com.github.kotlin;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

public class AudioFocusManager {
    private static final String TAG = AudioFocusManager.class.getSimpleName();

    /**
     * audio focus for call
     */
    private static final int AUDIO_FOCUS_CALL = 0;

    /**
     * audio focus type
     */
    public enum FocusType {
        Unknown,
        Call,
        Media,
        Navigation,
        VR,
        HUI,
        Radar,
    }

    /**
     * audio focus callback
     */
    public interface AudioFocusIndicator {
        void startPlayOnFocusGain();
        void stopPlayOnFocusLoss();
        void pausePlayOnFocusLossTransient();
        void mayPausePlayOnDuck();
    }

    /**
     * instance of AudioFocusManager
     */
    private static volatile AudioFocusManager sInstance;

    /**
     * initialize flag
     */
    private boolean setup;

    /**
     * audio manager
     */
    private AudioManager mAudioManager;

    /**
     * focus type
     */
    private FocusType mFocusType = FocusType.Unknown;

    /**
     * audio usage
     */
    private int mAudioUsage = AudioAttributes.USAGE_UNKNOWN;

    /**
     * audio attributes
     */
    private AudioAttributes mAttributes;

    /**
     * audio focus request
     */
    private AudioFocusRequest mAudioFocusRequest;

    /**
     * audio focus indicator
     */
    private AudioFocusIndicator mIndicator;

    /**
     * current request type
     */
    private int mCurrentRequestType;

    /**
     * current focus status
     */
    private int mCurrentFocusStatus = AudioManager.AUDIOFOCUS_LOSS;

    /**
     * build mode
     */
    private boolean mBuildMode;

    /**
     * accepts delayed flag
     */
    private boolean mAcceptsDelayed;

    private final AudioManager.OnAudioFocusChangeListener mListenerWrapper = new AFListener();

    /**
     * get instance of AudioFocusManager
     *
     * @return instance of AudioFocusManager
     */
    public static AudioFocusManager getInstance() {
        if (sInstance == null) {
            synchronized (AudioFocusManager.class) {
                if (sInstance == null) {
                    sInstance = new AudioFocusManager();
                }
            }
        }
        return sInstance;
    }

    public AudioFocusManager() {
    }

    /**
     * init audio focus manager
     *
     * @param context context
     * @param indicator audio focus callback
     */
    public synchronized void init(Context context, AudioFocusIndicator indicator) {
        if (mIndicator != null) {
            return;
        }

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager == null) {
            debug("get audio manager failed!");
            return;
        }

        mIndicator = indicator;
        setup = true;
    }

    /**
     * deinit audio focus manager
     */
    public synchronized void release() {
        abandonAudioFocus();
        mIndicator = null;
        setup = false;
    }

    private synchronized boolean isAudioFocusLossTransient() {
        return mCurrentFocusStatus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                mCurrentFocusStatus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
    }

    /**
     * request audio focus
     *
     * @param type focus type
     * @return request result
     */
    public synchronized int requestAudioFocus(FocusType type) {
        if (!setup) {
            return AudioManager.AUDIOFOCUS_LOSS;
        }

        if (mFocusType != FocusType.Unknown && mFocusType != type) {
            abandonAudioFocus();
        }

        if (isAudioFocusLossTransient()) {
            // already has focus, but loss it transient.
            // focus will come back late, so there is not need to do request.
            return AudioManager.AUDIOFOCUS_REQUEST_DELAYED;
        }

        if (mCurrentFocusStatus != AudioManager.AUDIOFOCUS_LOSS) {
            // already has focus, and not loss transient
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        mFocusType = type;
        switch (mFocusType) {
        case Call:
            //For requestAudioFocusForCall()
            mAudioUsage = AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
            mCurrentRequestType = AUDIO_FOCUS_CALL;
            mAcceptsDelayed = false;
            mBuildMode = false;
            //For BluetoothCall
            //mAudioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
            //mCurrentRequestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
            //mAcceptsDelayed = false;
            //mBuildMode = true;
            break;

        case Navigation:
            mAudioUsage = AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;
            mCurrentRequestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
            mAcceptsDelayed = false;
            mBuildMode = true;
            break;

        case VR:
            mAudioUsage = AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY;
            mCurrentRequestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
            mAcceptsDelayed = false;
            mBuildMode = true;
            break;

        case HUI:
            mAudioUsage = AudioAttributes.USAGE_NOTIFICATION;
            mCurrentRequestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
            mAcceptsDelayed = true;
            mBuildMode = true;
            break;

        case Radar:
            mAudioUsage = AudioAttributes.USAGE_ALARM;
            mCurrentRequestType = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
            mAcceptsDelayed = true;
            mBuildMode = true;
            break;

        case Media:
        default:
            mAudioUsage = AudioAttributes.USAGE_MEDIA;
            mCurrentRequestType = AudioManager.AUDIOFOCUS_GAIN;
            mAcceptsDelayed = false;
            mBuildMode = true;
            break;
        }

        mAttributes = new AudioAttributes.Builder()
                .setUsage(mAudioUsage)
                .build();

        int ret;
        switch (mCurrentRequestType) {
        case AUDIO_FOCUS_CALL:
            requestAudioFocusForCall();
            ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            //ret = doRequestGain(mCurrentRequestType);
            break;
        case AudioManager.AUDIOFOCUS_GAIN:
        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            ret = doRequestGain(mCurrentRequestType);
            break;
        default:
            ret = doRequestGain(AudioManager.AUDIOFOCUS_GAIN);
            break;
        }

        return ret;
    }

    /**
     * abandon audio focus
     */
    public synchronized void abandonAudioFocus() {
//        debug("abandonAudioFocus, request usage " + AudioUtils.usageToString(mAudioUsage) +
//                "(" + mCurrentRequestType + ")" +
//                ", current focus status (" + AudioUtils.focusToString(mCurrentFocusStatus) +
//                ") -> (LOSS)");

        if (mCurrentFocusStatus == AudioManager.AUDIOFOCUS_LOSS) {
            // noting to do
            return;
        }

        if (mCurrentRequestType == AUDIO_FOCUS_CALL) {
//            mAudioManager.abandonAudioFocusForCall();
        } else {
            if (mAudioFocusRequest != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
                }
            } else {
                mAudioManager.abandonAudioFocus(mListenerWrapper);
            }
        }

        mCurrentFocusStatus = AudioManager.AUDIOFOCUS_LOSS;
    }

    /**
     * request gain
     *
     * @param gain gain
     * @return request result
     */
    private int doRequestGain(int gain) {
        int ret = -1;
        boolean build = mAudioUsage == AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY
                || mBuildMode || mAcceptsDelayed;

        if (build) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioFocusRequest = new AudioFocusRequest.Builder(gain)
                        .setOnAudioFocusChangeListener(mListenerWrapper)
                        .setAudioAttributes(mAttributes)
                        .setAcceptsDelayedFocusGain(mAcceptsDelayed)
                        .build();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ret = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            }
        }

//        debug("requestAudioFocus, " + AudioUtils.focusToString(gain) +
//                (build ? ", use builder" : "") +
//                ", accepts delayed " + mAcceptsDelayed +
//                ", result " + AudioUtils.focusResultToString(ret));

        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentFocusStatus = gain;
        } else if (ret == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
            mCurrentFocusStatus = -gain;
        } else {
            mCurrentFocusStatus = AudioManager.AUDIOFOCUS_LOSS;
        }
        return ret;
    }

    /**
     * request audio focus for call
     */
    private void requestAudioFocusForCall() {
        debug("requestAudioFocusForCall");

//        mAudioManager.requestAudioFocusForCall(mAudioUsage, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mCurrentFocusStatus = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        startPlayOnFocusGain();
    }

    private class AFListener implements AudioManager.OnAudioFocusChangeListener {
        @Override
        public void onAudioFocusChange(int focusChange) {
            int oldStatus = mCurrentFocusStatus;
            mCurrentFocusStatus = focusChange;
            switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
//                debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                        ") -> (GAIN), can start play");
                startPlayOnFocusGain();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
//                debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                        ") -> (GAIN_TRANSIENT_MAY_DUCK), can start play");
                startPlayOnFocusGain();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
//                debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                        ") -> (GAIN_TRANSIENT_EXCLUSIVE), can start play");
                startPlayOnFocusGain();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
//                debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                        ") -> (LOSS), should stop play");
                stopPlayOnFocusLoss();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                        ") -> (LOSS_TRANSIENT), should pause play");
                pausePlayOnFocusLossTransient();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mFocusType == FocusType.Media) {
//                    debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                            ") -> (LOSS_TRANSIENT_CAN_DUCK), do nothing...");
                } else {
//                    debug("onAudioFocusChange (" + AudioUtils.focusToString(oldStatus) +
//                            ") -> (LOSS_TRANSIENT_CAN_DUCK), may pause play on duck...");
                    mayPausePlayOnDuck();
                }
                break;
            }
        }
    }

    private void startPlayOnFocusGain() {
        if (mIndicator != null) {
            mIndicator.startPlayOnFocusGain();
        }
    }

    private void stopPlayOnFocusLoss() {
        if (mIndicator != null) {
            mIndicator.stopPlayOnFocusLoss();
        }
    }

    private void pausePlayOnFocusLossTransient() {
        if (mIndicator != null) {
            mIndicator.pausePlayOnFocusLossTransient();
        }
    }

    private void mayPausePlayOnDuck() {
        if (mIndicator != null) {
            mIndicator.mayPausePlayOnDuck();
        }
    }

    private static void debug(String msg) {
        Log.e(TAG, msg);
    }
}

