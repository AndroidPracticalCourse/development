package tumandroidcourse2017.remoteapiserverconnect.sensors;

import android.hardware.SensorEvent;

/**
 * Acceleration sensor measures the acceleration applied to the device.
 * Force of gravity (9.81m/s^2) is always influencing the measured acceleration.
 */

public class Accelerometer {

    // Parameter to determine how often the accelerometer sensor state is to be updated (in milliseconds)
    private static final int UPDATE_FREQ = 200;

    private SensorEvent event;
    private long lastUpdateTime;

    // Adjustable threshold to configure the sensitivity
    private double noiseX = 0.2;
    private double noiseY = 0.2;
    private double noiseZ = 8.0;

    public Accelerometer(SensorEvent event) {
        this.event = event;
    }

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
}
