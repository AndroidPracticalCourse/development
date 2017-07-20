package tumandroidcourse2017.remoteapiserverconnect.sensors;

import android.hardware.SensorEvent;

import java.text.DecimalFormat;

/*
 * Acceleration sensor measures the acceleration applied to the device.
 * Force of gravity (9.81m/s^2) is always influencing the measured acceleration.
 */

public class Accelerometer {

    // Parameter to determine how often the accelerometer sensor state is to be updated (in milliseconds)
    private static final int UPDATE_FREQ = 1000;

    // Previous accelerometer values
    private double lastX;
    private double lastY;
    private double lastZ;

    // Velocity data
    private double v0;
    private double velocity;

    private SensorEvent event;
    private long lastUpdateTime;

    public Accelerometer(SensorEvent event, long lastUpdateTime, double[] lastAccelerometerValues, double startingVelocity) {
        this.event = event;
        this.lastUpdateTime = lastUpdateTime;
        this.lastX = lastAccelerometerValues[0];
        this.lastY = lastAccelerometerValues[1];
        this.lastZ = lastAccelerometerValues[2];
        this.v0 = startingVelocity;
    }

    public float calculateVelocity() {
        long currentTime = System.currentTimeMillis();
        long interval = currentTime - lastUpdateTime;

        if (interval > UPDATE_FREQ) {
            System.out.println("interval = " + interval);
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
            */

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

    /*
    public float[] update() {
        float[] gravity = new float[3];
        float[] linearAcceleration = new float[3];

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdateTime) > UPDATE_FREQ) {
            lastUpdateTime = currentTime;

            // To measure the real acceleration of the device, the contribution of the force of gravity needs to be removed
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

            if (linearAcceleration[0] > noiseX || linearAcceleration[1] > noiseY || linearAcceleration[2] > noiseZ) {
                System.out.println("Linear acceleration: [" + linearAcceleration[0] + ", " +
                        linearAcceleration[1] + ", " + linearAcceleration[2] + "]");
                return linearAcceleration;
            }
        }

        return new float[]{};
    }
    */

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public double[] getLastAccelerometerValues() {
        return new double[] {lastX, lastY, lastZ};
    }

    public double getStartingVelocity() {
        return v0;
    }

}
