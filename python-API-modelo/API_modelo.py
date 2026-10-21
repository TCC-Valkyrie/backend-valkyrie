import base64
from io import BytesIO
from pathlib import Path

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from fastapi import FastAPI, HTTPException, File, UploadFile, Form
from PIL import Image
from pydantic import BaseModel
from ultralytics import YOLO

BUCKET_MODEL_NAME = "bkt-valkyrie-modelos-455549248520"
BUCKET_CLIENT_NAME = "bkt-valkyrie-client-455549248520"
MODEL_KEY = "best.pt"
LOCAL_MODEL_PATH = Path("/tmp/best.pt")
CONF_THRESHOLD = 0.25

model_path = ""

if LOCAL_MODEL_PATH.exists():
   model_path = LOCAL_MODEL_PATH

s3 = boto3.client("s3")
try:
    s3.download_file(BUCKET_MODEL_NAME, MODEL_KEY, str(LOCAL_MODEL_PATH))
except (BotoCoreError, ClientError) as e:
    raise RuntimeError(f"Falha ao baixar modelo do S3: {e}")
model_path = LOCAL_MODEL_PATH

model = YOLO(str(model_path))

app = FastAPI()

# Lê uma única imagem da pasta imagens da S3
@app.post("/predict/")
async def predict_crime(
    objectId: str = Form(...)
):

    image_path = f"/tmp/images/image-{objectId}.jpg"

    try:
        s3.download_file(BUCKET_CLIENT_NAME, f"images/{objectId}/image.jpg", image_path)
    except (BotoCoreError, ClientError) as e:
        raise HTTPException(status_code=500, detail=f"Falha ao baixar imagem do S3: {e}")

    image = Image.open(image_path).convert("RGB")
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
        "nomeArquivo": objectId,
        "predictions": detected_objects
    }

@app.post("/predict-video/")
async def predict_crime(
    nomeArquivo: str = Form(...)
):

    image_path = f"/tmp/{nomeArquivo}"

    try:
        s3.download_file(BUCKET_CLIENT_NAME, nomeArquivo, image_path)
    except (BotoCoreError, ClientError) as e:
        raise HTTPException(status_code=500, detail=f"Falha ao baixar imagem do S3: {e}")

    image = Image.open(image_path).convert("RGB")
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