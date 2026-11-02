# Smishing Detector

Android app that detects SMS phishing (smishing) on the phone and explains
why a message was flagged. University of Queensland Capstone Project prototype.

SMS → Python preprocessing (Chaquopy) → TFLite model (LSTM text + CNN URL)
→ on-device LLM (Qwen3-1.7B, llama.cpp) → explanation in the UI.
Everything runs offline on the device.

<img width="323" height="709" alt="image14" src="https://github.com/user-attachments/assets/84f2a9d1-d092-4f06-b842-396f329d8500" />


## Layout

- `data/`: dataset notes (raw data not committed)
- `preprocessing/`: data prep, training, TFLite conversion
- `SmishingDetection/`: Android app (Kotlin, Compose, WebView UI)

Read in this order: `data/` → `preprocessing/` → `SmishingDetection/`

## Not in this repo

- The LLM file (over 1 GB). Push it to the phone as described in `SmishingDetection/README.md`.
- Raw datasets. See `data/README.md`.

The small TFLite classifier and tokenizer are included, so classification
works after cloning. Only explanations need the LLM.

## Results

Hybrid model: accuracy 93.48%, F1 90.68% (test set of 75,562 messages)

## Lessons learned

- Convert with `TFLiteConverter.from_keras_model`. Converting from a
  SavedModel caused NaN output on the device when trying to convert a model with training signatures.
- Preprocessing in training and in the app (Chaquopy) must be identical.
- Fit the tokeniser on the training split only.

## Limitations

- TODO: dataset size and language
- LLM explanations can be vague or wrong
- No testing conducted on physical phone

## Credits

Classifier branches adapted from:

- https://github.com/Ronitt272/NLP-LSTM-Text-Classification
- https://github.com/Vikaymaker/Phishing-URL-Detection-using-CNN

- LLM: Qwen3-1.7B (Apache 2.0) via llama.cpp.

© 2026 Mayosma. All rights reserved.
