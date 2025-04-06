import './App.css';
import TextField from "@mui/material/TextField";
import Slider from "@mui/material/Slider";
import Splitpane from "react-split-pane";
import {createContext, useContext, useState, useEffect, useRef} from "react";
import ImageContainer from './ImageContainer';
import { DarkModeSwitch } from 'react-toggle-dark-mode';
import CloseIcon from '@mui/icons-material/Close';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import TagManager from './TagManager.js';
import DetailInfos from './DetailInfos.js';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import FormControl from '@mui/material/FormControl';
import RestoreIcon from '@mui/icons-material/Restore';
import SettingsIcon from '@mui/icons-material/Settings';
import FileDownloadOutlinedIcon from '@mui/icons-material/FileDownloadOutlined';
import Settings from './Settings.js';
import Tooltip from '@mui/material/Tooltip';
import { useTranslation } from 'react-i18next';
import { KeycloakContext } from './KeycloakProvider';
import { fetchWithAuth } from './fetchWithAuth';
import { useImageBlobURL } from './useImageBlobURL';


export const ThemeContext = createContext(null);
const apiUrl = "/api";
// const apiUrl = "http://localhost:8080/api";  // Now Nginx handles the API calls

function App() {
  // Variable for translation function
  const { t, i18n } = useTranslation();

  const [searchInputText, setSearchInputText] = useState("");

  const formRef = useRef();

  const [isOverview, setIsOverview] = useState(true);

  // imgSrc is the UID
  const [imgSrc, setImgSrc] = useState("");
  const [imgPrevSize, setImgPrevSize] = useState(50);
  const [isFullscreen, setIsFullScreen] = useState(false);
  const [isTagManager, setIsTagManager] = useState(false);
  const [activeImg, setActiveImg] = useState("");
  const [existingTags, setExistingTags] = useState([]);
  const [colorSearch, setColorSearch] = useState("");
  const [settingsVisible, setSettingsVisible] = useState(false);
  // keycloak username
  const { user } = useContext(KeycloakContext);
  console.log(user.preferred_username);
  const [username, setUsername] = useState(user.preferred_username);
  const [defaultMode, setDefaultMode] = useState("");
  const [defaultLanguage, setDefaultLanguage] = useState("");
  const [defaultSliderValue, setDefaultSliderValue] = useState();
  
  // Dark mode theme variable
  const [theme, setTheme] = useState("light");

  const BlobImage = ({ uid, username, className, onClick }) => {
    const blobUrl = useImageBlobURL(`/api/detailViewImage?imageUid=${uid}&username=${username}`);
    if (!blobUrl) return <div>Lade Bild...</div>;
    return <img src={blobUrl} className={className} onClick={onClick} />;
  };

  const toggleTheme = () => {
    setTheme((curr) => (curr === "light" ? "dark" : "light"));
    if (theme === "light") { 
    document.body.style.backgroundColor = "#282c34";
    }
    else {
      document.body.style.backgroundColor = "white"
    }
  };
  
  // loads the user config
  useEffect(() => {
    const getConfig = async () => {
      const config = await fetchWithAuth(`api/user?username=${username}`);
      const configJson = await config.json();

      setImgPrevSize(Number(configJson.defaultSliderValue))
      console.log(configJson.defaultSliderValue)
      setTheme(configJson.defaultMode)
      i18n.changeLanguage(configJson.defaultLanguage)

      // set background color according to theme
      if (configJson.defaultMode === "dark") { 
        document.body.style.backgroundColor = "#282c34";
        }
        else {
          document.body.style.backgroundColor = "white"
        }

      setDefaultMode(configJson.defaultMode)
      setDefaultLanguage(configJson.defaultLanguage)
      setDefaultSliderValue(configJson.defaultSliderValue)
  };
getConfig()
}, [])

  // Lets the state variable to be changed from child element
  function handleSettingsToggle(bool) {
    setSettingsVisible(bool);
  }

  // activates detailview when image is clicked
  const handleImageClick = (e) => {
          const src = (e.target.getAttribute("src").split("imageUid=")[1]);
          setImgSrc(src);
          setActiveImg(src);
          setIsOverview(false);   
      }
  
  // gets all the tags for the tag suggestions
  useEffect(() => {
    const fetchTags = async () => {
      try {
        const result = await fetchWithAuth(`${apiUrl}/allTags`);
        console.log("tag ressource fetched successfully");
        const resultjson = await result.json();
        const tempTagDataJson = []
        Object.keys(resultjson).forEach(function(key) {
          tempTagDataJson.push(resultjson[key]);
        });
        const tempExistingTags = []
        // adds the tag names to the temp array, then is set to the actual array so it can properly rerender
        tempTagDataJson.map(item => {tempExistingTags.push(item.name)})
        setExistingTags(tempExistingTags);

      } catch (error) {
        console.error('Error fetching data:', error);
          }
      }
  fetchTags();
  }, []);


  function handleSearch(e) {
    // Prevent the browser from reloading the page
    e.preventDefault();
    const descriptionInput = document.getElementById("searchDesc");
    const tagInput = document.getElementById("searchTag");
    const commentInput = document.getElementById("searchComm");
    const color = colorSearch

    const formData = {
      description: descriptionInput.value,
      tag: tagInput.value,
      comment: commentInput.value,
      colorSearchTerm: color
    };

    setSearchInputText(formData);
  }

  const handleSearchRestore = async () => {
    const search = await fetchWithAuth(`${apiUrl}/lastSearch?username=${username}`);
    const searchjson = await search.json();
    console.log(searchjson)
    const formData = {
      description: searchjson.descriptionSearchTerm,
      tag: searchjson.tagSearchTerm,
      comment: searchjson.commentsSearchTerm,
      colorSearchTerm: searchjson.colorSearchTerm
    };
    console.log(formData);
    setSearchInputText(formData);

    // removes the label of search input so there are no overlaps (it doesn't disappear on its own)
    if (formData.valueOf().description !== "") {
      document.getElementById("searchDesc-label").innerHTML="" 
    }
    if (formData.valueOf().tag !== "") {
      document.getElementById("searchTag-label").innerHTML="" 
    }
    if (formData.valueOf().comment !== "") {
      document.getElementById("searchComm-label").innerHTML="" 
    }

  }

  const handlePrevSize = (event, newValue) => {
    setImgPrevSize(newValue);
  };

  const filterByColor = (event) => {
    console.log(event.target.value);
    setColorSearch(event.target.value);
  }

      // Function to show suggestions for the tag input
      function showTagSuggestions(input) {
        const searchTag = document.getElementById("searchTag");
        const tagSuggestions = document.getElementById("tagSuggestions");
        // Clear the suggestions div so there are no duplicates
        tagSuggestions.innerHTML = "";

        // Filter tags based on the input
        const filteredTags = existingTags.filter(tag => tag.toLowerCase().includes(input.toLowerCase()));

            // show suggestions in a dropdown
            filteredTags.forEach(tag => {
                const suggestionItem = document.createElement("div");
                suggestionItem.classList.add("suggestion-item");
                suggestionItem.textContent = tag;
                suggestionItem.addEventListener("click", function () {
                    searchTag.value = tag;
                    tagSuggestions.innerHTML = ""; // Clear suggestions after selection
                });
                tagSuggestions.appendChild(suggestionItem);
            });
        }

    function handleTagSuggestions(event) {
      // refreshes the tag suggestions after every character input
      showTagSuggestions(event.target.value);
    }

    function handleFocusLoss() {
      // removes tag suggestions when clicking out of textfield
      document.getElementById("tagSuggestions").innerHTML = ""
    }

  const onDownload = () => {
    // creates an anchor element that gets automatically clicked to download the image
    const imageElement = document.getElementsByClassName("active")[0];
    const link = document.createElement("a");
    link.download = imageElement.id;
    link.href = imageElement.src;
    link.click();
  };


  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
    <div className="App"id={theme}>
      {/* HEADER */}
      <header className="App-header">
        <div className="emptySpace"></div>
        <Slider 
        id="previewSlider"
        aria-label="preview size" 
        defaultValue={50}
        value={imgPrevSize} 
        min={25}
        max={500}
        onChange={handlePrevSize} />
        <Tooltip title={t("tooltips.restoreSearch")}><RestoreIcon id="RestoreButton" onClick={handleSearchRestore}></RestoreIcon></Tooltip>
        <div id="search">
        <form id="searchBar" method="post" onSubmit={handleSearch} ref={formRef}>
          <TextField
            id="searchDesc"
            variant="outlined"
            label={t("search.desc")}
            defaultValue={searchInputText.description}
            ></TextField>
            <div className="tagSearchbar">
          <TextField
            id="searchTag"
            variant="outlined"
            label={t("search.tag")}
            defaultValue={searchInputText.tag}
            onChange={handleTagSuggestions}
            onBlur={handleFocusLoss}
            ></TextField>
            <div id="tagSuggestions" className="suggestions"></div>
            </div>
          <TextField
            id="searchComm"
            variant="outlined"
            label={t("search.comment")}
            defaultValue={searchInputText.comment}
            ></TextField>
           
           {/* invisible submit button so it can be accessed via formRef */}
          <button type="submit" style={{ display: "none" }}>Submit</button>
          </form>
        </div>
        <Tooltip title={t("tags.tagmanager")}><LocalOfferIcon id="tagManagerIcon" onClick={() => {setIsTagManager(!isTagManager)}}></LocalOfferIcon></Tooltip>
        <DarkModeSwitch
        checked={theme}
        onChange={toggleTheme}
        size={40}
        ></DarkModeSwitch>
        <SettingsIcon id="settingsButton" onClick={() => {setSettingsVisible(!settingsVisible)}} style={{ marginRight: '2rem' , fontSize: "2rem", cursor: "pointer"}}></SettingsIcon>
      </header>
      <div>
        <Settings
        isToggled={settingsVisible}
        user={username}
        setIsToggled={handleSettingsToggle}
        defaultLanguage={defaultLanguage}
        defaultSliderValue={defaultSliderValue}
        defaultMode={defaultMode}
        ></Settings>
        {isTagManager? (<div>
            <TagManager apiUrl={apiUrl}></TagManager>
        </div>):(<div>
      {/* CONTROL ELEMENT FOR COLOR FILTERING */}
      <div className='colorFilterRow' id={theme}>
      <FormControl>
        <RadioGroup
        row
          aria-labelledby="radio-buttons-group-label"
          defaultValue=""
          name="radio-buttons-group"
          onChange={filterByColor}
        >
          <FormControlLabel value="" control={<Radio size="small"/>} label={t("search.noColor")} />
          
          <FormControlLabel value="white" control={<Radio size="small"/>} label={<ins style={{ "background": "#ffffff" }} className="color-box"></ins>} />
          <FormControlLabel value="pink" control={<Radio size="small"/>} label={<ins style={{ "background": "#f5bcc6" }} className="color-box"></ins>} />
          <FormControlLabel value="blue" control={<Radio size="small"/>} label={<ins style={{ "background": "#1111d4" }} className="color-box"></ins>} />
          <FormControlLabel value="green" control={<Radio size="small"/>} label={<ins style={{ "background": "#47eb47" }} className="color-box"></ins>} />
          <FormControlLabel value="brown" control={<Radio size="small"/>} label={<ins style={{ "background": "#e0801f" }} className="color-box"></ins>} />
        
        </RadioGroup>
      </FormControl>
      <button className="searchSubmitButton" id={theme} form='searchBar' type="submit">{t("search.submit")}</button>
      </div>
      
      {
      /* OVERVIEW */
      isOverview ? (
        <div className="Overview" id={theme}>
        <ImageContainer
        searchInputText={searchInputText}
        handleImageClick={handleImageClick}
        activeImg={activeImg}
        imgPrevSize={imgPrevSize}
        user={username}
        ></ImageContainer>
      </div>
      ):
      (
        /* FULLSCREEN */
      isFullscreen? (<div className='fullScreen'>
      <div className="fullScreenInfos" id={theme}>
      <DetailInfos imgSrc={imgSrc} apiUrl={apiUrl} user={username}></DetailInfos>
        </div>
      <div className="imgFullViewContainer">
        <BlobImage uid={imgSrc} username={username} className="imageFullView" onClick={() => { setIsFullScreen(false) }} />
      </div>
      </div>
      ):(
        /* DETAIL VIEW */
      <Splitpane split="vertical" minSize={200} defaultSize={700}>
      <div className="Overview" id={theme}>
        <ImageContainer
        searchInputText={searchInputText}
        handleImageClick= {handleImageClick}
        activeImg={activeImg}
        imgPrevSize={imgPrevSize}
        user={username}
        ></ImageContainer>
      </div>
      <div className='detailView' id={theme}>
        <div className='exitRow'>
          <CloseIcon id="exitButton" onClick={() => {setIsOverview(true); setActiveImg("")}}></CloseIcon>
          <p id="fullscreenInfo">{t("details.fullscreen")}</p>
          <FileDownloadOutlinedIcon id="downloadButton" onClick={onDownload}></FileDownloadOutlinedIcon>
        </div>
        <BlobImage uid={imgSrc} username={username} className="imageDetailView" onClick={() => { setIsFullScreen(true) }} />
        <div className="detailInfos">
          <DetailInfos imgSrc={imgSrc} apiUrl={apiUrl} user={username}></DetailInfos>
        </div>
  
        
      </div>
      </Splitpane>
      )
    )}
  
  </div>)}


    </div>
    </div>
    </ThemeContext.Provider>
  );
}

export default App;
