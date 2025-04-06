import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'https://v000557.fhnw.ch/', // Keycloak-Server
  realm: 'FHNW-LST-MI',        // Ersetze mit deinem tatsächlichen Realm
  clientId: 'g3-app',              // Dein Keycloak-Client
});

export default keycloak;
