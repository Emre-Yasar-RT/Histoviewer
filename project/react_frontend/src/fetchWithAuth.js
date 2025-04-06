import keycloak from './keycloak';

export async function fetchWithAuth(url, options = {}) {
  try {
    await keycloak.updateToken(30);
  } catch (err) {
    console.warn("Token konnte nicht erneuert werden", err);
  }

  const token = keycloak.token;

  const headers = {
    ...options.headers,
    Authorization: `Bearer ${token}`,
  };

  return fetch(url, {
    ...options,
    headers,
  });
}
