# 📸 UnityImagePickerLib

**UnityImagePickerLib** is a Unity-native Android plugin that allows seamless image selection from the **gallery or camera**, with **built-in cropping support**, permission handling, and image import directly into Unity’s `RawImage` UI component.

---

## ✅ Features

* 📷 Pick image from **Camera**
* 🖼️ Pick image from **Gallery**
* ✂️ Automatic **Image Cropping** (via Android native cropper)
* 🔐 Permission handling (camera, media access)
* 🧠 Easy Unity C# integration using `AndroidJavaClass`
* 🧩 Dynamically updates Unity’s **RawImage** and metadata (`name`, `size`)

---

## 📁 Folder Structure

```
UnityImagePickerLib/
│
├── ImagePicker_lib/        # Android native plugin source (open in Android Studio)
├── UnityProject/           # Your Unity project
│   └── Assets/
│       └── Plugins/
│           └── Android/
│               └── ImagePicker_lib-release.aar
```

---

## 🔧 Setup Instructions

### Step 1: Build the AAR Library

1. Open `ImagePicker_lib` folder in **Android Studio**.
2. Build the `.aar` file:

   * Go to **Build > Make Project** or run `./gradlew assembleRelease`.
3. Locate the generated AAR:

   ```
   ImagePicker_lib/build/outputs/aar/ImagePicker_lib-release.aar
   ```

---

### Step 2: Import AAR into Unity

1. Copy the `.aar` into your Unity project under:

   ```
   Assets/Plugins/Android/
   ```
2. In Unity Editor:

   * Select the `.aar` file
   * **Inspector Settings:**

     * ✅ `Android`: checked
     * ❌ `Any Platform`: unchecked

---

### Step 3: Add Required Android Permissions

In your Unity AndroidManifest (`Assets/Plugins/Android/AndroidManifest.xml`), add:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

> ✅ The plugin also automatically opens the cropper activity and returns the **cropped image path** to Unity.

---

### Step 4: Unity C# Usage – Integration Example

Here's how to trigger the image picker and load the result into a `RawImage` UI component:

```csharp
using UnityEngine;

public class ImagePickerExample : MonoBehaviour
{
    private const string PluginClass = "com.myteam11.imagepicker_lib.ImagePicker";
    private const string GameObjectName = "ImagePickerReceiver"; // Must match the GameObject in scene

    public void PickImageFromGallery()
    {
        using (AndroidJavaClass imagePicker = new AndroidJavaClass(PluginClass))
        {
            imagePicker.CallStatic("pickImageFromGallery", GameObjectName, "OnImagePickedSuccess", "OnImagePickedFailure");
        }
    }

    public void PickImageFromCamera()
    {
        using (AndroidJavaClass imagePicker = new AndroidJavaClass(PluginClass))
        {
            imagePicker.CallStatic("pickImageFromCamera", GameObjectName, "OnImagePickedSuccess", "OnImagePickedFailure");
        }
    }
}
```

---

### Step 5: Handle Result in Unity

Your GameObject (e.g., `ImagePickerReceiver`) should implement the following callbacks:

```csharp
public void OnImagePickedSuccess(string path)
{
    // Load image from path and display in RawImage
    byte[] imageBytes = File.ReadAllBytes(path);
    Texture2D tex = new Texture2D(2, 2);
    tex.LoadImage(imageBytes);

    rawImage.texture = tex;
    rawImage.SetNativeSize();

    Debug.Log("Image loaded: " + path);
}

public void OnImagePickedFailure(string error)
{
    Debug.LogError("Image picking failed: " + error);
}
```

---

### ✅ Included FilePickerReceiver.cs (Advanced Example)

For a complete, production-ready setup with UI panels, permission handling, and file renaming, use the provided script `FilePickerReceiver.cs`. Key features include:

* Modular image picker setup
* Automatically updates:

  * ✅ `RawImage`
  * ✅ Image name and size (`TMP_Text`)
* Cropped image automatically handled (Android plugin)

To initialize the script from Unity UI:

```csharp
filePickerReceiver.Initialize(rawImage, beforeUploadPanel, afterUploadPanel, imageNameText, imageSizeText);
filePickerReceiver.ShowFilePicker();
```

---

## 🧠 How Cropping Works

The Android plugin uses a built-in **cropper** (e.g., `uCrop`) after image capture or selection. Once the image is cropped, its path is returned to Unity via:

```java
UnityPlayer.UnitySendMessage(gameObjectName, "OnImagePickedSuccess", croppedImagePath);
```

✅ No need for additional cropping logic in Unity.

---

## 📦 Summary

| Feature                       | Status |
| ----------------------------- | ------ |
| Camera Picker                 | ✅ Yes  |
| Gallery Picker                | ✅ Yes  |
| Cropping (Android Native)     | ✅ Yes  |
| RawImage Support              | ✅ Yes  |
| Permission Handling           | ✅ Yes  |
| File Rename + Metadata Update | ✅ Yes  |
| Unity Integration             | ✅ Easy |

---

## 🛠️ Requirements

* Unity **2021.3+**
* Android Plugin built using **Android Studio**
* Android SDK **API 30+**

---

## 📞 Need Help?

If you’d like to:

* Customize the cropper (aspect ratio, circle, skip cropping)
* Add support for multiple images
* Handle runtime permissions with `Unity Permissions Plugin`

Just ask, and I’ll guide you step-by-step!

---

Let me know if you want this as a `.md` file download or want to integrate this into your Unity documentation.
