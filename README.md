# UnityImagePickerLib

This library provides an easy way to pick images from the gallery or camera in Unity using Android native code.

## How to Import and Use in Unity

1. Import the Library

- Copy the ImagePicker_lib folder into your Unity project's Assets/Plugins/Android directory.
- Make sure your Unity project is set up to build for Android.

2. Permissions

- The library requires the following permissions, which Unity should request automatically if you use the Android manifest from the library:
  ```
  <uses-permission 
  android:name="android.
  permission.CAMERA" />
  <uses-permission 
  android:name="android.
  permission.
  READ_MEDIA_IMAGES" />
  ```

3. Usage in Unity C# Script
   You can call the image picker from your Unity scripts using AndroidJavaObject or a wrapper class. Here's an example:

```
using UnityEngine;

public class 
ImagePickerExample : 
MonoBehaviour
{
    private AndroidJavaClass 
    imagePickerClass;
    private string 
    gameObjectName = 
    "YourGameObjectName";

    void Start()
    {
        imagePickerClass = 
        new AndroidJavaClass
        ("com.myteam11.
        imagepicker_lib.
        ImagePicker");
    }

    public void 
    PickImageFromGallery()
    {
        imagePickerClass.
        CallStatic
        ("pickImageFromGallery
        ", gameObjectName, 
        "OnImagePicked", 
        "OnImagePickFailed");
    }

    public void 
    PickImageFromCamera()
    {
        imagePickerClass.
        CallStatic
        ("pickImageFromCamera"
        , gameObjectName, 
        "OnImagePicked", 
        "OnImagePickFailed");
    }

    // Callback when image is 
    picked successfully
    void OnImagePicked(string 
    imagePath)
    {
        Debug.Log("Image 
        picked: " + 
        imagePath);
        // Load or use the 
        image path as needed
    }

    // Callback when image 
    picking fails
    void OnImagePickFailed
    (string error)
    {
        Debug.LogError("Image 
        pick failed: " + 
        error);
    }
}
```

4. How It Works

- The library launches native Android activities to pick images from gallery or camera.
- After picking, it optionally allows cropping the image.
- The final image path is sent back to Unity via callbacks.

5. Build and Run

- Build your Unity project for Android.
- Make sure the AndroidManifest.xml from the library is merged correctly.
- Run on an Android device.
