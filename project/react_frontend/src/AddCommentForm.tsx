import { ChangeEvent, useState } from 'react'
import { Comment } from './model.ts'
import { Button, FormGroup, TextField } from '@mui/material'
import React from 'react'
import { useTranslation } from 'react-i18next';


const sxForm = {
  display: 'grid',
  gridTemplateRows: '1fr auto',
  gap: 1,
}

const sxButton = {
  justifySelf: 'end',
}

interface Props {
  onAddComment: (comment: Comment) => void
}

export const AddCommentForm = ({ onAddComment }: Props) => {
  const [comment, setComment] = useState<string>('')

  const { t } = useTranslation();

  const handleTextChange = (e: ChangeEvent<HTMLInputElement>) => {
    const text = e.target.value
    setComment(text)
  }

  const handleButtonClick = () => {
    onAddComment({ timestamp: Date.now(), text: comment })
    setComment('')
  }

  const isButtonDisabled = comment.length === 0

  return (
    <FormGroup sx={sxForm}>
      <TextField
        label={t('comment.textEntry')}
        multiline={true}
        variant={'outlined'}
        rows={5}
        value={comment}
        onChange={handleTextChange}
      />
      <Button
        sx={sxButton}
        variant={'contained'}
        color={'primary'}
        disabled={isButtonDisabled}
        onClick={handleButtonClick}
      >
        {t("comment.add")}
      </Button>
    </FormGroup>
  )
}
