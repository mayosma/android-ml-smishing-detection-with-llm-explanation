# Android app

The classifier (`_model.tflite`, `word_index.json`) is already in
`../SmishingDetection/app/src/main/assets` and `../SmishingDetection/app/src/main/python`. The LLM is not in the repo because of its size, so
you push it to the phone yourself.

## Setup

1.  Download `Qwen_Qwen3-1.7B-Q4_K_M.gguf` from https://huggingface.co/bartowski/Qwen_Qwen3-1.7B-GGUF?show_file_info=Qwen_Qwen3-1.7B-Q4_K_M.gguf (Hugging Face link)
    into a local `models/` folder.
2.  Open the project in Android Studio, sync Gradle, and run it once so the
    app is installed.
3.  Push the model to the app's storage:

        adb shell mkdir -p /sdcard/Android/data/com.example.smishingdetection/files/models
        adb push ./models/Qwen_Qwen3-1.7B-Q4_K_M.gguf /sdcard/Android/data/com.example.smishingdetection/files/models/model.gguf

    The file must be renamed to `model.gguf`, because that is the name the app looks for.

## Notes

- Uninstalling the app deletes this folder, so push the model again after a reinstall.
- Without the LLM file, classification works but explanations won't.
- Needs Android Studio and the `RECEIVE_SMS` permission. Nothing is
  stored or uploaded.
