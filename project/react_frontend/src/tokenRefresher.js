import keycloak from './keycloak';

export function startTokenRefresh(intervalSeconds = 30) {
  setInterval(() => {
    keycloak.updateToken(30)
      .then(refreshed => {
        if (refreshed) {
          console.log("Token wurde automatisch erneuert");
        }
      })
      .catch(() => {
        console.warn("Token konnte nicht automatisch erneuert werden");
      });
  }, intervalSeconds * 1000);
}
