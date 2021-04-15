package com.luck.picture.lib.model;

public class TargetAudioTrack extends TargetTrack {

    public TargetAudioTrack(int sourceTrackIndex,
                            boolean shouldInclude,
                            boolean shouldTranscode,
                            AudioTrackFormat format) {
        super(sourceTrackIndex, shouldInclude, shouldTranscode, format);
    }

    public AudioTrackFormat getTrackFormat() {
        return (AudioTrackFormat) format;
    }
}
