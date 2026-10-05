import requests
import base64
from io import BytesIO
from PIL import Image, ImageDraw

image_path = "test_image.jpg"
with open(image_path, "rb") as img_file:
    b64_string = base64.b64encode(img_file.read()).decode('utf-8')

print("Sending request to API...")
response = requests.post(
    "http://127.0.0.1:8000/predict/",
    json={"image_base64": b64_string}
)
result = response.json()

print(f"Detected {len(result.get('predictions', []))} objects.")

returned_b64 = result["original_image_base64"]
image_bytes = base64.b64decode(returned_b64)
image = Image.open(BytesIO(image_bytes))

draw = ImageDraw.Draw(image)

for pred in result.get("predictions", []):
    box = pred["bbox"]
    label = f"{pred['class']} ({pred['confidence']})"
    
    draw.rectangle(box, outline="red", width=3)
    
    draw.text((box[0], box[1] - 15), label, fill="red")

image.show()