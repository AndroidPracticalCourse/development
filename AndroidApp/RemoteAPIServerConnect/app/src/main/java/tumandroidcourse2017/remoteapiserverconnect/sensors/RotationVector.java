package tumandroidcourse2017.remoteapiserverconnect.sensors;

import android.hardware.SensorEvent;

/**
 * Rotation vector represents the orientation of the device as a combination of an angle and an axis.
 * The three elements of the rotation vector are <x*sin(θ/2), y*sin(θ/2), z*sin(θ/2)>
 */

public class RotationVector {

    private SensorEvent event;

    public RotationVector(SensorEvent event) {
        this.event = event;
    }

    public float[] update() {
        float orientationX = event.values[0];
        float orientationY = event.values[1];
        float orientationZ = event.values[2];

        return new float[]{ orientationX, orientationY, orientationZ };
    }
}
