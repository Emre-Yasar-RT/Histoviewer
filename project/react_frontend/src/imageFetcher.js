import { fetchWithAuth } from './fetchWithAuth';

const imageCache = new Map();

export async function fetchImageBlobURL(endpoint) {
  if (imageCache.has(endpoint)) {
    return imageCache.get(endpoint);
  }

  try {
    const response = await fetchWithAuth(endpoint);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);

    imageCache.set(endpoint, url);

    return url;
  } catch (error) {
    console.error("Fehler beim Laden des Bildes:", error);
    return "";
  }
}
