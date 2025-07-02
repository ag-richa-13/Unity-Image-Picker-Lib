# UnityImagePickerLib
This library provides an easy way to pick images from the gallery or camera in Unity Android projects using a native Android plugin.

## How to Use UnityImagePickerLib in Your Unity Project
### Step 1: Build the AAR Library
1. Open the Android library project located in ImagePicker_lib using Android Studio.
2. Build the project to generate the .aar file:
   - Go to Build > Make Project or use the Gradle task assembleRelease .
   - The generated .aar file will be located in ImagePicker_lib/build/outputs/aar/ImagePicker_lib-release.aar .
### Step 2: Import the AAR into Unity
1. Copy the generated .aar file ( ImagePicker_lib-release.aar ) into your Unity project under the folder:
   ```
   Assets/Plugins/Android/
   ```
2. In Unity Editor, select the .aar file and ensure the following settings in the Inspector:
   - Select platforms : Android
   - Any Platform : Unchecked
   - Android : Checked
### Step 3: Add Required Permissions
Make sure your Unity AndroidManifest.xml includes the following permissions:

```
<uses-permission 
android:name="android.
permission.CAMERA" />
<uses-permission 
android:name="android.
permission.
READ_MEDIA_IMAGES" />
```
You can add these permissions by creating or modifying the Plugins/Android/AndroidManifest.xml file in your Unity project.

### Step 4: Using the Image Picker in Unity C# Scripts
You can call the image picker methods from your Unity scripts using AndroidJavaClass to interact with the native plugin.

Example usage:

```
using UnityEngine;

public class 
ImagePickerExample : 
MonoBehaviour
{
    private const string 
    PluginClass = "com.
    myteam11.imagepicker_lib.
    ImagePicker";
    private const string 
    GameObjectName = 
    "YourGameObjectName"; // 
    Replace with your 
    GameObject name

    public void 
    PickImageFromGallery()
    {
        using 
        (AndroidJavaClass 
        imagePicker = new 
        AndroidJavaClass
        (PluginClass))
        {
            imagePicker.
            CallStatic
            ("pickImageFromGal
            lery", 
            GameObjectName, 
            "OnImagePicked", 
            "OnImagePickFailed
            ");
        }
    }

    public void 
    PickImageFromCamera()
    {
        using 
        (AndroidJavaClass 
        imagePicker = new 
        AndroidJavaClass
        (PluginClass))
        {
            imagePicker.
            CallStatic
            ("pickImageFromCam
            era", 
            GameObjectName, 
            "OnImagePicked", 
            "OnImagePickFailed
            ");
        }
    }

    // Callback when image is 
    picked successfully
    private void OnImagePicked
    (string imagePath)
    {
        Debug.Log("Image 
        picked: " + 
        imagePath);
        // Load or use the 
        image path as needed
    }

    // Callback when image 
    picking fails
    private void 
    OnImagePickFailed(string 
    error)
    {
        Debug.LogError("Image 
        pick failed: " + 
        error);
    }
}
```
### Step 5: Handling the Cropper
The plugin automatically launches a cropper activity after picking or capturing an image. The cropped image path is returned in the success callback.

## Summary
- Build the .aar from the Android library project.
- Import the .aar into Unity under Assets/Plugins/Android/ .
- Add required permissions in Unity's AndroidManifest.
- Use the provided C# example to call the image picker.
- Handle success and failure callbacks in your Unity scripts.
This setup allows seamless image picking and cropping functionality in your Unity Android projects using this native plugin.
