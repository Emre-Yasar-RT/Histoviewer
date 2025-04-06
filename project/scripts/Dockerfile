FROM python:3.9-slim

# Installiere curl
RUN apt-get update && apt-get install -y curl

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere das Skript & die Abhängigkeiten
COPY convert_and_upload.py .
COPY requirements.txt .

# Installiere die benötigten Python-Pakete
RUN pip install --no-cache-dir -r requirements.txt
