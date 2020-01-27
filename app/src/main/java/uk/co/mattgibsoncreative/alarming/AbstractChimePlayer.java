package uk.co.mattgibsoncreative.alarming;

import android.content.Context;

public abstract class AbstractChimePlayer implements ChimePlayer {
    protected Context mContext;
    public AbstractChimePlayer(Context c) {
        mContext = c;
    }
    abstract public void close();
    abstract public void playChime(int resourceId);
    abstract public void prepareChime(int resourceId);
}
