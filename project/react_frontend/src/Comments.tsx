import { useEffect, useState } from 'react'
import { Comment } from './model.ts'
import { AddCommentForm } from './AddCommentForm.tsx'
import { CommentCard } from './CommentCard.tsx'
import { Box } from '@mui/material'
import { fetchWithAuth } from './fetchWithAuth';

import React from 'react'

// const apiUrl = "http://localhost:8080/api";  // Now Nginx handles the API calls
const apiUrl = "/api";


const sxRoot = {
  display: 'grid',
  width: '100%',
  height: '100%',
  gridTemplateRows: 'auto 1fr',
  gap: 2,
}

const sxComment = {
  display: 'grid',
  alignSelf: 'start',
  gap: 1,
}

export const Comments = (imgSrc, comments, handleCommentAdd, user) => {
  const [commentList, setCommentList] = useState<any[]>([])

  useEffect(() => {
    setCommentList(imgSrc.comments);
  }, [imgSrc])

  const handleAddComment = (comment: Comment) => {
    var object = {text: comment.text};
    console.log(commentList);

    fetchWithAuth(`${apiUrl}/imageEntityByUid/${imgSrc.imgSrc}?username=${imgSrc.user}`, {
    method: 'PATCH',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      "comments": [object]
    })
  })
// sets variable in parent component "reload" to true so list of comments updates and it rerenders
  imgSrc.handleCommentAdd(true);
  }

const handleDeleteComment = (commentToDelete: number) => {
  console.log(`comment to delete: ${commentToDelete}`)
    fetchWithAuth(`${apiUrl}/commentById?commentID=${commentToDelete}`, {
    method: 'DELETE',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    },
  })
  // Rerender after comment ist deleted (only visual, variable of parent is not overwritten)
  setCommentList(commentList.filter((comment) => comment.id != commentToDelete));
  } 

  return (
    
    <Box sx={sxRoot}>
      <AddCommentForm onAddComment={handleAddComment} />
      <Box sx={sxComment}>
        {commentList
                  .map((comment: any) => (
                    <CommentCard key={comment.id} id={comment.id} text={comment.text} user={imgSrc.user} deleteComment={handleDeleteComment}
                    />
                  ))}
      </Box>
    </Box>
  )
}
