import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/js/bootstrap.js';
import KeycloakProvider from './KeycloakProvider';
import './i18n';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <KeycloakProvider>
    <App />
  </KeycloakProvider>
);

reportWebVitals();
