import { React, useState, useEffect } from 'react'
import "./imageContainer.css"
import Tooltip from '@mui/material/Tooltip';
import { useImageBlobURL } from './useImageBlobURL';
import { fetchWithAuth } from './fetchWithAuth';

const apiUrl = "/api";
// const apiUrl = "http://localhost:8080/api";  // Now Nginx handles the API calls

function BlobImage({ uid, description, active, onClick }) {
  const blobUrl = useImageBlobURL(`${apiUrl}/previewImage?imageUid=${uid}`); // Neu
  return (
    <Tooltip title={description} arrow slotProps={{
      popper: {
        modifiers: [{
          name: 'offset',
          options: { offset: [0, -14] },
        }],
      },
    }}>
      <img
        className="imageInstance"
        id={active ? "active" : undefined}
        src={blobUrl}
        onClick={onClick}
      />
    </Tooltip>
  );
}

function ImageContainer({searchInputText, handleImageClick, activeImg, imgPrevSize, user}) {

  const [imageData, setImageData] = useState([]);

  // sizes the image width based on the slider value (updates every time the slider moves or imagedata is modified)
  useEffect(() => {
    const imageInstance = document.querySelectorAll(".imageInstance");
    for (var i=0; i < imageInstance.length; i++) {
      imageInstance[i].setAttribute("style", `width:${imgPrevSize}px`);
    };
  }, [imgPrevSize, imageData])

  // gets the image entities from the api
  useEffect(() => {
    const fetchData = async () => {
      if (searchInputText === ""){
        try {
          const result = await fetchWithAuth(apiUrl + '/allImageEntities');
          console.log("ressource fetched successfully");
          const resultjson = await result.json();
          setImageData(resultjson);

        } catch (error) {
          console.error('Error fetching data:', error);
        }
      }
      else {
        try {
          // handles search with searchInputText as JSON
          console.log(searchInputText)
          const result = await fetchWithAuth(`${apiUrl}/search?username=${user}`, {
            method: 'POST',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              "descriptionSearchTerm": `${searchInputText.description}`,
              "tagSearchTerm": `${searchInputText.tag}`,
              "commentsSearchTerm": `${searchInputText.comment}`,
              "colorSearchTerm": `${searchInputText.colorSearchTerm}`
            })
          });
          console.log("ressource fetched successfully");
          // checks if the api response has no content
          if (result.status === 204) {
            setImageData([])
          }
          else {
            const resultjson = await result.json();
            setImageData(resultjson);
          }


        } catch (error) {
          console.error('Error fetching data:', error);
        }
      } 
    };

    fetchData();
  }, [searchInputText]);


  return (
    <div className="allImages">
      {imageData.map((image) => {
        const isActive = activeImg === image.uid;
        return (
          <Tooltip title={image.description} arrow slotProps={{
            popper: {
              modifiers: [{ name: 'offset', options: { offset: [0, -14] } }],
            },
          }}>
            <img
              className={`imageInstance${isActive ? ' active' : ''}`}
              src={`${apiUrl}/previewImage?imageUid=${image.uid}`}
              onClick={handleImageClick}
              id={image.src}
            />
          </Tooltip>
        );
      })}
    </div>
    )}

export default ImageContainer


