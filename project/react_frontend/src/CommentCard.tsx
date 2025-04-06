import { Card, CardContent, IconButton, Typography} from '@mui/material'
import DeleteIcon from "@mui/icons-material/Delete"
import React from 'react'

interface Props {
  text: any
  id: number
  user: any
  deleteComment: (commentToDelete: any) => void
}

export const CommentCard = ({ text, id, user, deleteComment}: Props) => (
  <Card>
    <CardContent sx={{ p:2, '&:last-child': { pb: 0 }}}>
       <Typography align={'right'}>{user}</Typography>
      <Typography component={'div'}>
      <script>
        </script>
        {text.split('\n').map((text: string, key: number) => {
          return <div key={key}>{text}</div>
        })}
      </Typography>
      <Typography align="left">
        <IconButton aria-label="delete" sx={{padding: 0, pb: 1, pt: 4}} onClick={() => {deleteComment(id)}}>
          <DeleteIcon />
        </IconButton>
      </Typography>
    </CardContent>
  </Card>
)
