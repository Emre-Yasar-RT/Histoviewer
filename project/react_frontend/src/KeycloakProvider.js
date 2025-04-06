import React, { useState, useEffect, createContext } from 'react';
import keycloak from './keycloak';

export const KeycloakContext = createContext();

const KeycloakProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false
    }).then(auth => {
      if (auth) {
        setIsAuthenticated(true);
        setUser(keycloak.tokenParsed);
        console.log("Userdaten:", keycloak.tokenParsed);
      } else {
        console.warn('Nicht authentifiziert');
      }
    }).catch(console.error);
  }, []);

  if (!isAuthenticated) {
    return <div>Lade Authentifizierung...</div>;
  }

  return (
    <KeycloakContext.Provider value={{ keycloak, user }}>
      {children}
    </KeycloakContext.Provider>
  );
};

export default KeycloakProvider;
