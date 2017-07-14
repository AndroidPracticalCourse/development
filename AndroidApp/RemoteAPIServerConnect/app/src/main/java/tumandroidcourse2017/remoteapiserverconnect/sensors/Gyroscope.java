package tumandroidcourse2017.remoteapiserverconnect.sensors;

import android.hardware.SensorEvent;

/**
 * Gyroscope measures the rate of rotation around the device's local X-,Y- and Z-axis.
 * Rotation is positive in the counter-clockwise direction.
 *
 * Used to control the pivot of the robotic arm.
 */

public class Gyroscope {

    private SensorEvent event;
    private long lastUpdateTime;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float rollAngle = 0;
    private float pitchAngle = 0;
    private float previousRollAngle = 0;
    private float previousPitchAngle = 0;

    public Gyroscope(SensorEvent event) {
        this.event = event;
    }

    public float[] update() {
        long timeInterval = event.timestamp - lastUpdateTime;
        lastUpdateTime = event.timestamp;

        float pitch = event.values[0]; // around x-axis
        float roll = event.values[1]; // around y-axis
        if (roll < 0.005 && roll > -0.005) {
            roll = 0;
        }
        if (pitch < 0.005 & pitch > -0.005) {
            pitch = 0;
        }

        rollAngle = rollAngle + (((roll + previousRollAngle) / 2.0f) * timeInterval * NS2S);
        pitchAngle = pitchAngle + (((pitch + previousPitchAngle) / 2.0f) * timeInterval * NS2S);

        previousRollAngle = roll;
        previousPitchAngle = pitch;

        System.out.println("Current roll value: " + (rollAngle * 360 / 2 / Math.PI)); // debug statement

        return new float[]{ rollAngle, pitchAngle };
    }

}
