import os
import xml.etree.ElementTree as ET
from datetime import datetime
import logging
import numpy as np
import requests
from pydicom.uid import generate_uid, ExplicitVRLittleEndian
from PIL import Image
import pydicom
from pydicom.dataset import Dataset, FileMetaDataset


# Orthanc PACS-Server Konfiguration
ORTHANC_URL = "http://orthanc:8042/instances"


def get_existing_sop_instance_uids():
    """Abrufen der bereits vorhandenen SOPInstanceUIDs aus Orthanc."""
    response = requests.get(ORTHANC_URL)
    existing_uids = set()

    if response.status_code == 200:
        instance_ids = response.json()
        for instance_id in instance_ids:
            instance_response = requests.get(f"{ORTHANC_URL}/{instance_id}/tags")
            if instance_response.status_code == 200:
                tags = instance_response.json()
                if "00080018" in tags and "Value" in tags["00080018"]:
                    existing_uids.add(tags["00080018"]["Value"])
    return existing_uids


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "../data")
DICOM_DIR = os.path.join(DATA_DIR, "allDICOMImages")

if not os.path.exists(DICOM_DIR):
    os.makedirs(DICOM_DIR, exist_ok=True)

XML_FILE = os.path.join(DATA_DIR, "allImageMetadata.xml")
IMAGE_DIR = os.path.join(DATA_DIR, "allJPEGImages")

# Logger einrichten
logger = logging.getLogger("dicom_uploader")
logger.setLevel(logging.INFO)
file_handler = logging.FileHandler("warnings.log")
file_handler.setLevel(logging.WARNING)
console_handler = logging.StreamHandler()
console_handler.setLevel(logging.INFO)
logger.addHandler(file_handler)
logger.addHandler(console_handler)


def parse_xml_metadata(xml_file_path):
    """Parst die Metadaten aus der XML-Datei und gibt eine Liste mit Bildinformationen zurück."""
    if not os.path.exists(xml_file_path):
        raise FileNotFoundError(f"XML-Datei nicht gefunden: {xml_file_path}")

    tree = ET.parse(xml_file_path)
    root = tree.getroot()

    images_metadata = []
    for image in root.findall("image"):
        metadata = {
            "src": image.get("src"),
            "description": image.find("description").text.strip(),
            "originalURL": image.find("originalURL").text.strip(),
            "magnification": image.find("magnification").text.strip(),
            "uid": image.find("uid").text.strip(),
        }
        images_metadata.append(metadata)
    
    return images_metadata


def create_dicom(data):
    """Erstellt eine DICOM-Datei aus einem JPEG und fügt DICOM-Metadaten hinzu."""
    dicom_filename = os.path.join(DICOM_DIR, data["src"].replace(".jpg", "") + ".dcm")
    sop_instance_uid = "1.2.826.0.1.3680043." + data["uid"].lstrip("0")

    if os.path.exists(dicom_filename):
        ds = pydicom.dcmread(dicom_filename)
        if ds.SOPInstanceUID == sop_instance_uid:
            return dicom_filename

    ds = Dataset()
    ds.file_meta = FileMetaDataset()
    ds.file_meta.TransferSyntaxUID = ExplicitVRLittleEndian

    ds.PatientName = "Unknown^Patient"
    ds.PatientID = "000000"
    ds.StudyInstanceUID = generate_uid()
    ds.SeriesInstanceUID = generate_uid()
    ds.SOPInstanceUID = sop_instance_uid
    ds.SOPClassUID = pydicom.uid.SecondaryCaptureImageStorage
    ds.Modality = "OT"

    image_path = os.path.join(IMAGE_DIR, data["src"])
    if not os.path.exists(image_path):
        raise FileNotFoundError(f"Bilddatei nicht gefunden: {image_path}")

    img = Image.open(image_path).convert("RGB")
    img_array = np.array(img)
    ds.PixelData = img_array.tobytes()
    ds.Rows, ds.Columns, ds.SamplesPerPixel = img_array.shape
    ds.PhotometricInterpretation = "RGB"
    ds.BitsStored = 8
    ds.BitsAllocated = 8
    ds.HighBit = 7
    ds.PixelRepresentation = 0

    ds.save_as(dicom_filename, write_like_original=False)
    
    return dicom_filename


def send_to_pacs():
    """Sendet die erstellten DICOM-Dateien an Orthanc PACS."""
    existing_sop_uids = get_existing_sop_instance_uids()

    for file in os.listdir(DICOM_DIR):
        dicom_path = os.path.join(DICOM_DIR, file)
        if file == ".gitignore":
            continue

        ds = pydicom.dcmread(dicom_path)

        if ds.SOPInstanceUID in existing_sop_uids:
            continue

        with open(dicom_path, "rb") as f:
            headers = {"Content-Type": "application/dicom"}
            response = requests.post(ORTHANC_URL, data=f, headers=headers)

            if response.status_code != 200:
                print(f"Fehler beim Hochladen {file}: {response.status_code}, {response.text}")


try:
    metadata = parse_xml_metadata(XML_FILE)
    for data in metadata:
        create_dicom(data)
    send_to_pacs()
except FileNotFoundError as e:
    logger.error(e)
    print(e)
