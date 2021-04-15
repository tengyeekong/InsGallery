package com.luck.picture.lib.model;

import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linkedin.android.litr.utils.CodecUtils;

public class TargetTrack {
    public int sourceTrackIndex;
    public boolean shouldInclude;
    public boolean shouldTranscode;
    public MediaTrackFormat format;

    public TargetTrack(int sourceTrackIndex, boolean shouldInclude, boolean shouldTranscode, @NonNull MediaTrackFormat format) {
        this.sourceTrackIndex = sourceTrackIndex;
        this.shouldInclude = shouldInclude;
        this.shouldTranscode = shouldTranscode;
        this.format = format;
    }

    @Nullable
    public MediaFormat createMediaFormat(@Nullable TargetTrack targetTrack) {
        MediaFormat mediaFormat = null;
        if (targetTrack != null && targetTrack.format != null) {
            mediaFormat = new MediaFormat();
            if (targetTrack.format.mimeType.startsWith("video")) {
                VideoTrackFormat trackFormat = (VideoTrackFormat) targetTrack.format;
                String mimeType = CodecUtils.MIME_TYPE_VIDEO_AVC;
                mediaFormat.setString(MediaFormat.KEY_MIME, mimeType);
                mediaFormat.setInteger(MediaFormat.KEY_WIDTH, trackFormat.width);
                mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, trackFormat.height);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, trackFormat.bitrate);
                mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, trackFormat.keyFrameInterval);
                mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, trackFormat.frameRate);
            } else if (targetTrack.format.mimeType.startsWith("audio")) {
                AudioTrackFormat trackFormat = (AudioTrackFormat) targetTrack.format;
                mediaFormat.setString(MediaFormat.KEY_MIME, trackFormat.mimeType);
                mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, trackFormat.channelCount);
                mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, trackFormat.samplingRate);
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, trackFormat.bitrate);
            }
        }

        return mediaFormat;
    }
}
