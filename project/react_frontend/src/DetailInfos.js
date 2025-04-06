import { React, useState, useEffect, useCallback} from 'react';
import { Comments } from './Comments.tsx';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import TextField from "@mui/material/TextField";
import ClearIcon from '@mui/icons-material/Clear';
import { Tooltip } from '@mui/material';
import "./detailInfos.css";
import { useTranslation, Trans } from 'react-i18next';
import { fetchWithAuth } from './fetchWithAuth';
import { useImageBlobURL } from './useImageBlobURL';

function DetailInfos({apiUrl, imgSrc, user}) {
  const [imgDesc, setImgDesc] = useState("");
  const [magnification, setMagnification] = useState("");
  const [imgTags, setImgTags] = useState([]);
  const [imgComments, setImgComments] = useState([]);
  const [isTagInput, setIsTagInput] = useState(false);
  const [reload, setReload] = useState(false);

  // Variable for translation function
  const { t } = useTranslation();

  const detailImageUrl = useImageBlobURL(`${apiUrl}/detailViewImage?imageUid=${imgSrc}&username=${user}`);

  const fetchDetails = useCallback(async () => {
    try {
      const response = await fetchWithAuth(`${apiUrl}/imageEntityByUid/${imgSrc}`);
      console.log("description ressource fetched successfully");
      const resultjson = await response.json();
      const resultjsonStringed = JSON.stringify(resultjson);
      const obj = JSON.parse(resultjsonStringed);
      setImgDesc(obj.description);
      setImgTags(obj.tags);
      setImgComments(obj.comments);
      setMagnification(obj.magnification);
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  }, [imgSrc]);

    useEffect(() => {
          fetchDetails();
            setReload(false);
        }, [fetchDetails, reload]);

    const addTag = () => {
      setIsTagInput(true);
    }
    
    function handleTagSubmit(e) {
      // Prevent the browser from reloading the page
      e.preventDefault();
        
      const formData = new FormData(e.target);
      var object = {};
      formData.forEach(function(value, key){
        object[key] = value;
      });
                   
      fetchWithAuth(`${apiUrl}/imageEntityByUid/${imgSrc}?username=${user}`, {
        method: 'PATCH',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          },
          body: JSON.stringify({
          "tags": [object]
            })
          })

          setReload(true);
          setIsTagInput(false);
        }

    function deleteTag(tag) {      
      fetchWithAuth(`/api/removeTagFromDicomData?dicomDataUID=${imgSrc}&tagID=${tag.id}`, {
        method: 'DELETE',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          },
        })

      setReload(true);
    }

    function handleCommentAdd(bool) {
        setReload(bool);
    }

    return (
        <div id="detailInfos">

        <div id="tagList">
        <div className="allTags">
    {/* TAG LIST */}
            {imgTags.map((tag) => {
                return <div className="tagInstance">
                    <ClearIcon id="deleteTagIcon" onClick={() => {deleteTag(tag)}}></ClearIcon>
                    <p id="tagName">{tag.name}</p>
                </div>
                })}
            </div>
            <Tooltip title={t("tags.add")}><AddCircleOutlineIcon id="addTagIcon" onClick={addTag}/></Tooltip>
          </div>
          {isTagInput?(
            <form id="tagAdder" method="post" onSubmit={handleTagSubmit}>
            <TextField name="name"
            accept='String' />
            <button id="addTagButton" type="submit">Add</button>
          </form>
          ):(
          //placeholder div, otherwhise syntax error
          <div></div>)}
          <hr></hr>

          {/* DESCRIPTION */}
          <div id="textInfos">
          <p id="desc">{imgDesc}</p>
          <p className="minorInfos">{t("details.magnification")}: {magnification}</p>
          </div>
          <hr></hr>

          {/* COMMENTS */}
          <Comments imgSrc={imgSrc}
          comments={imgComments}
            handleCommentAdd={handleCommentAdd}
            user={user}>
          </Comments>
        </div>
    );


}

export default DetailInfos