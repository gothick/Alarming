package uk.co.mattgibsoncreative.alarming;


public interface ChimePlayer {
    void prepareChime(int chimeResourceId);
    void playChime(int chimeResourceId);
    void playChime(int chimeResourceId, float volume);
    void close();
}
