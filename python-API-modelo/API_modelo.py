import base64
from io import BytesIO

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from fastapi import FastAPI, HTTPException
from PIL import Image
from pydantic import BaseModel
from ultralytics import YOLO

BUCKET_NAME = "bkt-valkyrie-modelos-455549248520"
MODEL_KEY = "best.pt"
LOCAL_MODEL_PATH = "/tmp/best.pt"
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

class ImageRequest(BaseModel):
    image_base64: str

@app.post("/predict/")
async def predict_crime(request: ImageRequest):
    try:
        b64_data = request.image_base64
        if "," in b64_data:
            b64_data = b64_data.split(",")[1]
        
        image_bytes = base64.b64decode(b64_data)
        image = Image.open(BytesIO(image_bytes))
    except Exception as e:
        raise HTTPException(status_code=400, detail="Invalid Base64 string")

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
        "predictions": detected_objects,
        "original_image_base64": request.image_base64
    }