package pl.androiddev.musicplayer.permissions;

public interface PermissionCallback {
    void permissionGranted();

    void permissionRefused();
}