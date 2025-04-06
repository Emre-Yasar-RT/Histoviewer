import {useState, useEffect} from "react";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Checkbox from "@mui/material/Checkbox";
import Tooltip from '@mui/material/Tooltip';
import IconButton from '@mui/material/IconButton';
import ClearIcon from '@mui/icons-material/Clear';
import CallMergeIcon from '@mui/icons-material/CallMerge';
import { useTranslation } from 'react-i18next';
import { fetchWithAuth } from './fetchWithAuth';

function TagManager(apiUrl) {
      const [tagData, setTagData] = useState([]);
      const [selected, setSelected] = useState([]);
      const [rerender, setRerender] = useState(false);

        // Variable for translation function
        const { t } = useTranslation();

    // gets a list of tags from the api
      useEffect(() => {
        const fetchData = async () => {
            try {
              const result = await fetchWithAuth(`${apiUrl.apiUrl}/allTags`);
              console.log("tag ressource fetched successfully");
              const resultjson = await result.json();
              setTagData(resultjson);
    
            } catch (error) {
              console.error('Error fetching data:', error);
                }
            }
        fetchData();
        console.log("tags updated")
        }, [rerender])

function mergeTagPrompt() {
  let tag1;
  let tag2;
  // checks if 2 rows are selected, else just one tag will be renamed
  if (selected.length === 2){
    tag1 = tagData.find(x => x.id === selected[0]).name;
    tag2 = tagData.find(x => x.id === selected[1]).name;
  }
  else {
    tag1 = tagData.find(x => x.id === selected[0]).name;
    tag2 = tagData.find(x => x.id === selected[0]).name;
  }

  const resultTag = prompt(t("tags.mergePrompt"));
  if (window.confirm(t("tags.mergeNotif", {tagName1: tag1, tagName2: tag2, resultTag: resultTag}))) {
    mergeTag(tag1, tag2, resultTag);
  } else {
    console.log('Merge has been cancelled');

  }
}

function mergeTag(tag1, tag2, resultTag) {
  fetchWithAuth(`/api/replaceTags?tag1=${tag1}&tag2=${tag2}&newTag=${resultTag}`, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    },
  })
  console.log("merged");
  setSelected([]);

  // triggers useeffect -> rerender
  setRerender(!rerender);
}

function deleteTag(tag) {
  // Displays a warning that all the tags will be deleted
  if (window.confirm(t("tags.deleteNotif", {variable: tag.name}))) {
    fetchWithAuth(`/api/deleteTagFromAllDicomData?tagID=${tag.id}`, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
    })

    // triggers useeffect -> rerender
    setRerender(!rerender);

  } else {
    console.log('Delete has been cancelled');
  }
}

function handleCheckbox(event, id) {
  console.log(id)
  console.log(selected)
  let newSelected = selected.slice();
  if (selected.includes(id)) {
    const index = newSelected.indexOf(id);
    newSelected.splice(index, 1);
  }
  else {
    newSelected.push(id);
  }
  //setSelect needs to be called so it rerenders
  setSelected(newSelected)
  
}

    return (
        <div className="tagTable">

<TableContainer component={Paper}>
      <Table sx={{ minWidth: 650 }} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell></TableCell>
            <TableCell>Tag ID</TableCell>
            <TableCell>Tag Name</TableCell>
            <TableCell>{<Tooltip title={t("tooltips.mergeTags")}><IconButton disabled={selected.length === 0}><CallMergeIcon id="mergeTagPromptIcon" onClick={() => {mergeTagPrompt()}}/></IconButton></Tooltip>}</TableCell>
            <TableCell></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tagData.map((row) => {
            // disables the remaining checkboxes when 2 are selected
            const isSelected = selected.includes(row.id);
            const isDisabled = (selected.length === 2 && isSelected === false)
            return (
            <TableRow
              key={row.id}
              sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
            >
               <Checkbox
            color="primary"
            onClick={(event) => handleCheckbox(event, row.id)}
            checked={isSelected}
            disabled={isDisabled}
          />
              <TableCell component="th" scope="row">
                {row.id}
              </TableCell>
              <TableCell>{row.name}</TableCell>
              <TableCell></TableCell>
              <TableCell>{<ClearIcon id="deleteTagIcon" onClick={() => {deleteTag(row)}}></ClearIcon>}</TableCell>

            </TableRow>
          )})}
        </TableBody>
      </Table>
    </TableContainer>
        </div>
        
    )
}

export default TagManager