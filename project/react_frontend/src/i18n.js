import i18n from 'i18next';

import { initReactI18next } from 'react-i18next';

import LanguageDetector from 'i18next-browser-languagedetector';


i18n

  // detect user language

  // learn more: https://github.com/i18next/i18next-browser-languageDetector

  .use(LanguageDetector)

  // pass the i18n instance to react-i18next.

  .use(initReactI18next)

  // init i18next

  // for all options read: https://www.i18next.com/overview/configuration-options

  .init({

    debug: true,

    fallbackLng: 'en',

    interpolation: {

      escapeValue: false, // not needed for react as it escapes by default

    },

    resources: {
// ENGLISCH
      en: {
        translation: {
            search: {
                desc: "Search by Description",
                tag: "Search by Tag",
                comment: "Search by Comment",
                submit: "Submit Search",
                noColor: "No Selection"
            },
            settings: {
                loggedInAs: "Logged in as",
                save: "Save Changes",
                darkDesc: "Standardtheme",
                langDesc: "Default Language",
                imgWidthDesc: "Preview Image Default Size"
            },
            details: {
                magnification: "Magnification",
                fullscreen: "Click image for fullscreen"
            }, 
            tooltips: {
                restoreSearch: "Restore last search",
                mergeTags: "Merge selected tags",
            },
            tags: {
                mergePrompt: "Enter merged Tag name",
                mergeNotif: `The tags "{{tagName1}}" and "{{tagName2}}" will be merged into "{{resultTag}}", continue?`,
                deleteNotif: "The tag {{variable}} will be deleted on ALL the corresponding images, continue?",
                tagmanager: "Tag Manager",
                add: "Add tag"
            },
            comment: {
                textEntry: "Enter Comment",
                add: "Add Comment"
            }

        }

      }
,
// GERMAN
      de: {
        translation: {
            search: {
                desc: "Beschreibung durchsuchen",
                tag: "Tags durchsuchen",
                comment: "Kommentare durchsuchen",
                submit: "Suchen",
                noColor: "Keine Auswahl"
            },
            settings: {
                loggedInAs: "Eingeloggt als",
                save: "Änderungen speichern",
                darkDesc: "Standardtheme",
                langDesc: "Standardsprache",
                imgWidthDesc: "Standardgrösse Vorschaubilder"
            },
            details: {
                magnification: "Vergrösserung",
                fullscreen: "Auf Bild klicken für Vollbild"
            }, 
            tooltips: {
                restoreSearch: "Letzte Suche wiederherstellen",
                mergeTags: "Ausgewählte Tags zusammenfügen"
                
            },
            tags: {
                mergePrompt: "Bitte Name für zusammengefügten Tag setzen",
                mergeNotif: `Die Tags "{{tagName1}}" und "{{tagName2}}" werden zusammengefügt zu "{{resultTag}}", weiter?`,
                deleteNotif: "Der Tag {{variable}} wird bei ALLEN Bildern entfernt, weiter?",
                tagmanager: "Tagverwaltung",
                add: "Tag hinzufügen"
            },
            comment: {
                textEntry: "Kommentar eingeben",
                add: "Hinzufügen"
            }

        }

      }
      ,

// FRENCH
      fr: {
        translation: {
            search: {
                desc: "Rechercher par description",
                tag: "Rechercher par étiquette",
                comment: "Rechercher par commentaire",
                submit: "Chercher",
                noColor: "Aucune Sélection"
            },
            settings: {
                loggedInAs: "Vous êtes connecté comme",
                save: "Enregistrer les modifications",
                darkDesc: "Mode d'apparence standard",
                langDesc: "Langue standard",
                imgWidthDesc: "Taille standard image d'aperçu"
            },
            details: {
                magnification: "Élargissement",
                fullscreen: "Cliquez l'image pour plein écran"
            }, 
            tooltips: {
                restoreSearch: "Restaurer la dernière recherche",
                mergeTags: "Fusionner tags choisi"
                
            },
            tags: {
                mergePrompt: "Nom pour étiquette fusionné",
                mergeNotif: `Les étiquettes "{{tagName1}}" et "{{tagName2}}" seront fusionnées dans "{{resultTag}}", continuez?`,
                deleteNotif: "L'étiquette {{variable}} sera supprimée sur TOUTES les images correspondantes, continuez?",
                tagmanager: "Administration des étiquettes",
                add: "Ajouter une étiquette"
            },
            comment: {
                textEntry: "Ajouter un commentaire",
                add: "Ajouter"
            }

        }

      }
    }

  });


export default i18n;