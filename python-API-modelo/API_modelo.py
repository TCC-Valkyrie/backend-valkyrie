from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from ultralytics import YOLO
from PIL import Image
from io import BytesIO
import base64

model = YOLO("best.pt")

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