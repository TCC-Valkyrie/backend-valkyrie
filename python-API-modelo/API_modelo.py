import base64
from io import BytesIO
from pathlib import Path

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from fastapi import FastAPI, HTTPException, File, UploadFile, Form
from PIL import Image
from pydantic import BaseModel
from ultralytics import YOLO

BUCKET_NAME = "bkt-valkyrie-modelos-455549248523"
MODEL_KEY = "best.pt"
LOCAL_MODEL_PATH = Path("/tmp/best.pt")
CONF_THRESHOLD = 0.25

path = ""

if LOCAL_MODEL_PATH.exists():
    path = LOCAL_MODEL_PATH

s3 = boto3.client("s3")
try:
    s3.download_file(BUCKET_NAME, MODEL_KEY, str(LOCAL_MODEL_PATH))
except (BotoCoreError, ClientError) as e:
    raise RuntimeError(f"Falha ao baixar modelo do S3: {e}")
path = LOCAL_MODEL_PATH

# BASE_DIR = Path(__file__).resolve().parent
# MODEL_PATH = path / "best.pt"

model = YOLO(str(path))

app = FastAPI()

@app.post("/predict/")
async def predict_crime(
    nomeArquivo: str = Form(...)
):
    try:
        image_bytes = await imagem.read()
        image = Image.open(BytesIO(image_bytes)).convert("RGB")
    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Arquivo de imagem inválido"
        )

    results = model(image)

    detected_objects = []

    for result in results:
        for box in result.boxes:
            detected_objects.append({
                "class": model.names[int(box.cls[0])],
                "confidence": round(float(box.conf[0]), 3),
                "bbox": box.xyxy[0].tolist()
            })

    return {
        "nomeArquivo": nomeArquivo,
        "predictions": detected_objects
    }

@app.get("/health")
def health():
    return {"status": "ok"}