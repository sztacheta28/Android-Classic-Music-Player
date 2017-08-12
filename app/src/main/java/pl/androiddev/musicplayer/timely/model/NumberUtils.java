package pl.androiddev.musicplayer.timely.model;

import java.security.InvalidParameterException;

import pl.androiddev.musicplayer.timely.model.number.Eight;
import pl.androiddev.musicplayer.timely.model.number.Five;
import pl.androiddev.musicplayer.timely.model.number.Four;
import pl.androiddev.musicplayer.timely.model.number.Nine;
import pl.androiddev.musicplayer.timely.model.number.Null;
import pl.androiddev.musicplayer.timely.model.number.One;
import pl.androiddev.musicplayer.timely.model.number.Seven;
import pl.androiddev.musicplayer.timely.model.number.Six;
import pl.androiddev.musicplayer.timely.model.number.Three;
import pl.androiddev.musicplayer.timely.model.number.Two;
import pl.androiddev.musicplayer.timely.model.number.Zero;

public class NumberUtils {

    public static float[][] getControlPointsFor(int start) {
        switch (start) {
            case (-1):
                return Null.getInstance().getControlPoints();
            case 0:
                return Zero.getInstance().getControlPoints();
            case 1:
                return One.getInstance().getControlPoints();
            case 2:
                return Two.getInstance().getControlPoints();
            case 3:
                return Three.getInstance().getControlPoints();
            case 4:
                return Four.getInstance().getControlPoints();
            case 5:
                return Five.getInstance().getControlPoints();
            case 6:
                return Six.getInstance().getControlPoints();
            case 7:
                return Seven.getInstance().getControlPoints();
            case 8:
                return Eight.getInstance().getControlPoints();
            case 9:
                return Nine.getInstance().getControlPoints();
            default:
                throw new InvalidParameterException("Unsupported number requested");
        }
    }
}
