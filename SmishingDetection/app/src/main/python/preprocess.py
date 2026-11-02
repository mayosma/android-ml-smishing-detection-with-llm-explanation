import json
import os
import re
# Code was inspired from the LSTM model github repository
VOCAB        = "abcdefghijklmnopqrstuvwxyz0123456789/:.?&=%-_"
CHAR_TO_INT  = {ch: idx + 1 for idx, ch in enumerate(VOCAB)}
URL_MAX_LEN  = 200
TEXT_MAX_LEN = 150
NUM_WORDS    = 1000

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
WORD_INDEX_PATH = os.path.join(SCRIPT_DIR, "word_index.json")

with open(WORD_INDEX_PATH, 'r') as f:
    word_index = json.load(f)


def clean_text(text):
    text = text.lower()

    # Remove URLs
    text = re.sub(
        r"http\S+|www\.\S+",
        "",
        text,
        flags=re.IGNORECASE
    )

    # Remove email addresses
    text = re.sub(
        r"\b[\w\.-]+@[\w\.-]+\.\w+\b",
        "",
        text
    )

    # Remove phone numbers
    text = re.sub(
        r"\+?\d[\d\s\-]{7,}\d",
        "",
        text
    )

    # Remove punctuation and normalise whitespace
    text = re.sub(r'[!"#$%&()*+,\-./:;<=>?@\[\]^_`{|}~\t\n]', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()

    return text

def text_to_sequence(text):
    text = clean_text(text)
    words = text.split()
    seq = []
    for w in words:
        idx = word_index.get(w)
        if idx is not None and idx < NUM_WORDS:
            seq.append(idx)
    return seq

def pad_sequence_manual(seq, maxlen, padding='post', truncating='post'):
    if len(seq) > maxlen:
        seq = seq[:maxlen] if truncating == 'post' else seq[-maxlen:]
    pad_len = maxlen - len(seq)
    if pad_len > 0:
        seq = seq + [0] * pad_len if padding == 'post' else [0] * pad_len + seq
    return seq

def encode_url(url: str):
    url = url.lower().strip()
    return [CHAR_TO_INT.get(ch, 0) for ch in url]

# encodes text and URL and adds padding
def preprocess(text, url):
    lstm_seq = text_to_sequence(text)
    lstm_padded = pad_sequence_manual(lstm_seq, TEXT_MAX_LEN)

    if url:
        cnn_encoded = pad_sequence_manual(encode_url(url), URL_MAX_LEN)
    else:
        cnn_encoded = [0] * URL_MAX_LEN

    return json.dumps({
        "textArray": [lstm_padded],
        "urlArray": [cnn_encoded]
    })