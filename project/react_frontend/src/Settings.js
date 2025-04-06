import React from "react";
import { useState, useEffect } from "react";
import "./settings.css";
import { useTranslation } from 'react-i18next';

import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';

import { fetchWithAuth } from './fetchWithAuth';



function Settings({isToggled, user, setIsToggled, defaultMode, defaultLanguage, defaultSliderValue}) {
  // Variable for translation function
  const { t } = useTranslation();

    const [darkModePref, setDarkModePref] = useState("light")
    const [languagePref, setLanguagePref] = useState("en")
    const [imgWidthPref, setImgWidthPref] = useState(50)


// sets the default values of the settings every time the menu is opened (otherwhise will not display correctly)
    useEffect(() => {
        setDarkModePref(defaultMode)
        setLanguagePref(defaultLanguage)
        setImgWidthPref(defaultSliderValue)
      }, [isToggled])

const handleDarkSubmit = (e) => {
    setDarkModePref(e.target.value)
}
const handleLangSubmit = (e) => {
    setLanguagePref(e.target.value)
}

const handleNumSubmit = (e) => {
    setImgWidthPref(e.target.value)
}

// sends updated user config to api (database)
const saveSettings = async () => {
  await fetchWithAuth(`api/user?username=${user}`, 
   {
       method: 'PATCH',
       headers: {
         'Accept': 'application/json',
         'Content-Type': 'application/json',
       },
       body: JSON.stringify({
           "defaultSliderValue": imgWidthPref,
           "defaultLanguage": `${languagePref}`,
           "defaultMode": `${darkModePref}`
         })  
   })
     console.log("user config updated")
     window.location.reload();
 }

return (
<div>
<Dialog onClose={() => setIsToggled(false)} open={isToggled}>
      <DialogTitle>Settings</DialogTitle>
    <DialogContent>
            <p>{t("settings.loggedInAs")} {user}</p>

            <Box
            noValidate
            component="form"
            sx={{
              display: 'flex',
              flexDirection: 'column',
              m: 'auto',
              width: 'fit-content',
            }}
          >
            <FormControl id="preferences" sx={{ mt: 2, minWidth: 120 }}>
              <InputLabel></InputLabel>
              <div className="preferenceChoice">
                <p>{t("settings.darkDesc")}</p>
              <Select
                value={darkModePref}
                onChange={handleDarkSubmit}
                defaultValue={darkModePref}
            
              >
                <MenuItem value="light">Light</MenuItem>
                <MenuItem value="dark">Dark</MenuItem>
                
              </Select>
              </div>
              <hr></hr>
              <div className="preferenceChoice">
              <p>{t("settings.langDesc")}</p>
              <Select
                value={languagePref}
                onChange={handleLangSubmit}
                defaultValue={languagePref}
              >
                <MenuItem value="en">English</MenuItem>
                <MenuItem value="de">Deutsch</MenuItem>
                <MenuItem value="fr">Français</MenuItem>
                
              </Select>
              </div>
              <hr></hr>
              <div className="preferenceChoice">
              <p>{t("settings.imgWidthDesc")}</p>
              <TextField
                id="standard-number"
                type="number"
                variant="standard"
                defaultValue={imgWidthPref}
                onChange={handleNumSubmit}
                slotProps={{
                    inputLabel: {
                    shrink: true,
                    inputProps: { min: 25, max: 500 }
                    },
                }}
                />
                </div>
            </FormControl>
            
          </Box>

    </DialogContent>
    <DialogActions>
          <Button autoFocus onClick={saveSettings}>
            {t("settings.save")}
          </Button>
        </DialogActions>
    </Dialog>
    </div>)
}


export default Settings;