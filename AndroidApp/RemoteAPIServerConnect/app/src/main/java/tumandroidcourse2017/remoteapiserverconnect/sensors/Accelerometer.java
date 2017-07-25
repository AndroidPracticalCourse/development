package tumandroidcourse2017.remoteapiserverconnect.sensors;

import android.hardware.SensorEvent;

public class Accelerometer {

    // Parameter to determine how often the accelerometer sensor state is to be updated (in milliseconds)
    private static final int UPDATE_FREQ = 100;

    private SensorEvent event;
    private long lastUpdateTime;

    int tiltLeftRight = 0;
    int tiltUpDown = 0;

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

            tiltLeftRight = (int) Math.round(Math.toDegrees(Math.atan2(g[0], g[2]))) * -1;
            tiltUpDown = (int) (Math.round(Math.toDegrees(Math.atan2(g[1], g[2]))));
            //System.out.println("interval = " + interval + ", tiltLeftRight = " + tiltLeftRight + ", tiltUpDown = " + tiltUpDown);
        }
    }

    /*
    public float calculateVelocity() {
        long currentTime = System.currentTimeMillis();
        long interval = currentTime - lastUpdateTime;

        if (interval > UPDATE_FREQ) {
            lastUpdateTime = currentTime;
            double acceleration = event.values[0] + event.values[1] + event.values[2] - lastX - lastY - lastZ;

            /* To measure the real acceleration of the device, the contribution of the force of gravity needs to be removed
            // A high-pass filter is used here
            final float alpha = Float.valueOf("0.8");

            // Isolate the force of gravity
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution
            linearAcceleration[0] = event.values[0] - gravity[0];
            linearAcceleration[1] = event.values[1] - gravity[1];
            linearAcceleration[2] = event.values[2] - gravity[2];
            double acceleration = linearAcceleration[0] + linearAcceleration[1];

            // Motion at constant acceleration: v(t) = v0 + at
            velocity = v0 + (acceleration * (interval / (double) 1000));
            //v0 = velocity;
            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];
        }

        DecimalFormat df = new DecimalFormat("#");
        return Float.valueOf(df.format(velocity));
    }
    */

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
