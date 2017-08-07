package tumandroidcourse2017.remoteapiserverconnect.sensors;

import android.hardware.SensorEvent;

public class Accelerometer {

    // Parameter to determine how often the accelerometer sensor state is to be updated (in milliseconds)
    private static final int UPDATE_FREQ = 100;

    private SensorEvent event;
    private long lastUpdateTime;

    private int tiltLeftRight = 0;
    private int tiltUpDown = 0;

    public Accelerometer(SensorEvent event, long lastUpdateTime) {
        this.event = event;
        this.lastUpdateTime = lastUpdateTime;
    }

    public void calculateRotation() {
        long currentTime = System.currentTimeMillis();
        long interval = currentTime - lastUpdateTime;

        if (interval > UPDATE_FREQ) {
            lastUpdateTime = currentTime;
            float[] g = event.values.clone();

            double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]);

            // Normalise the accelerometer vector
            g[0] = (float) (g[0] / norm_Of_g);
            g[1] = (float) (g[1] / norm_Of_g);
            g[2] = (float) (g[2] / norm_Of_g);

            tiltLeftRight = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[2])));
            tiltUpDown = (int) (Math.round(Math.toDegrees(Math.atan2(g[1], g[2]))));
            //System.out.println("interval = " + interval + ", tiltLeftRight = " + tiltLeftRight + ", tiltUpDown = " + tiltUpDown);
        }
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getTiltLeftRight() {
        return tiltLeftRight;
    }

    public int getTiltUpDown() {
        return tiltUpDown;
    }

}
