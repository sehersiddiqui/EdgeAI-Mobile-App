package com.edgeai.reader;

/**
 * Pure Java rule engine for calibrated, normalized point-of-regard samples.
 * It is intentionally decoupled from camera detection: do not feed face-center/head-pose
 * proxies as if they were true gaze coordinates.
 */
public final class ReadingTelemetryEngine {
    public static final long FIXATION_THRESHOLD_MS = 600;
    public static final int REGRESSION_THRESHOLD = 3;
    public static final float SAME_WORD_RADIUS = 0.035f;
    public static final float LEFTWARD_JUMP = 0.12f;
    public static final float SAME_LINE_Y_DELTA = 0.08f;

    public static final class Sample {
        public final float x, y;
        public final long timestampMs;
        public Sample(float x, float y, long timestampMs) {
            this.x = clamp(x); this.y = clamp(y); this.timestampMs = timestampMs;
        }
        private static float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
    }
    public static final class Event {
        public final boolean fixationAlert, regressionAlert;
        public final long fixationDurationMs;
        public final int regressionCount;
        public final String evidence;
        Event(boolean f, boolean r, long d, int c, String e) {
            fixationAlert=f; regressionAlert=r; fixationDurationMs=d; regressionCount=c; evidence=e;
        }
    }
    private Sample prior;
    private long fixationStart;
    private int regressionsInWindow;
    private long windowStart;

    public synchronized Event accept(Sample s) {
        if (prior == null) { prior=s; fixationStart=s.timestampMs; windowStart=s.timestampMs;
            return new Event(false,false,0,0,"Calibration/sample stream started"); }
        long duration=0; boolean fixation=false, regression=false;
        float dx=s.x-prior.x, dy=Math.abs(s.y-prior.y);
        if(Math.hypot(dx,dy)<=SAME_WORD_RADIUS) {
            duration=Math.max(0,s.timestampMs-fixationStart);
            fixation=duration>FIXATION_THRESHOLD_MS;
        } else {
            if(dx < -LEFTWARD_JUMP && dy < SAME_LINE_Y_DELTA) {
                if(s.timestampMs-windowStart>120_000){regressionsInWindow=0;windowStart=s.timestampMs;}
                regressionsInWindow++; regression=regressionsInWindow>REGRESSION_THRESHOLD;
            }
            fixationStart=s.timestampMs;
        }
        prior=s;
        String evidence=fixation ? "Stable point for "+duration+" ms (>600 ms prototype threshold)"
                : regression ? regressionsInWindow+" leftward same-line jumps in a 120-second window"
                : "No rule threshold crossed";
        return new Event(fixation,regression,duration,regressionsInWindow,evidence);
    }
    public synchronized void reset(){prior=null;fixationStart=0;regressionsInWindow=0;windowStart=0;}
}
