from io import BytesIO
from pathlib import Path

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from fastapi import FastAPI, HTTPException, File, UploadFile, Form
from PIL import Image
from pydantic import BaseModel
from ultralytics import YOLO
import os

import zipfile
import glob

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

class PredictionRequest(BaseModel):
    objectId: str

def predict_image(image: Image.Image):
    results = model(image)
    detected_objects = []

    for result in results:
        for box in result.boxes:
            detected_objects.append({
                "class": model.names[int(box.cls[0])],
                "confidence": round(float(box.conf[0]), 3),
                "bbox": box.xyxy[0].tolist()
            })

    return detected_objects

@app.post("/predict/")
async def predict_crime(
    request: PredictionRequest
):
    objectId = request.objectId

    obj_path = os.path.join("/tmp", objectId)
    os.makedirs(os.path.dirname(obj_path), exist_ok=True)

    try:
        s3.download_file(BUCKET_CLIENT_NAME, objectId, obj_path)
    except (BotoCoreError, ClientError) as e:
        raise HTTPException(status_code=500, detail=f"Falha ao baixar arquivo do S3: {e}")

    detected_objects = []

    if "/videos/" in objectId:
        with zipfile.ZipFile(obj_path, 'r') as zip_ref:
            temp_dir = obj_path + "/extracted"
            os.makedirs(temp_dir, exist_ok=True)
            zip_ref.extractall(temp_dir)

        image_paths = glob.glob(os.path.join(temp_dir, "*.jpg"))

        if not image_paths:
            raise HTTPException(status_code=400, detail="Nenhuma imagem encontrada no arquivo zip.")

        for image_path in image_paths:
            image = Image.open(image_path).convert("L")
            detected_objects.extend(predict_image(image))
        
    elif "/images/" in objectId:
        image = Image.open(obj_path).convert("L")
        detected_objects = predict_image(image)

    else:
        raise HTTPException(status_code=400, detail="Formato de arquivo não suportado. Use imagens ou vídeos.")
    
    return {
        "nomeArquivo": objectId,
        "predictions": detected_objects
    }

@app.get("/health")
def health():
    return {"status": "ok"}