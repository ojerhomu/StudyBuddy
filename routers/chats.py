from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

import models
import schemas
from database import get_db
from auth import get_current_user

router = APIRouter(prefix="/chats", tags=["Chats"])


@router.get("/", response_model=List[schemas.ChatSessionSummary])
def list_chats(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return (
        db.query(models.ChatSession)
        .filter(models.ChatSession.user_id == current_user.id)
        .order_by(models.ChatSession.created_at.desc())
        .all()
    )


@router.get("/{chat_id}", response_model=schemas.ChatSessionOut)
def get_chat(
    chat_id: int,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    chat = (
        db.query(models.ChatSession)
        .filter(
            models.ChatSession.id == chat_id,
            models.ChatSession.user_id == current_user.id,
        )
        .first()
    )
    if not chat:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Chat not found")
    return chat


@router.post("/", response_model=schemas.ChatSessionOut, status_code=status.HTTP_201_CREATED)
def create_chat(
    chat: schemas.ChatSessionCreate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    new_chat = models.ChatSession(
        title=chat.title,
        messages=chat.messages,
        user_id=current_user.id,
    )
    db.add(new_chat)
    db.commit()
    db.refresh(new_chat)
    return new_chat


@router.put("/{chat_id}", response_model=schemas.ChatSessionOut)
@router.patch("/{chat_id}", response_model=schemas.ChatSessionOut)
def update_chat(
    chat_id: int,
    chat: schemas.ChatSessionUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    existing = (
        db.query(models.ChatSession)
        .filter(
            models.ChatSession.id == chat_id,
            models.ChatSession.user_id == current_user.id,
        )
        .first()
    )
    if not existing:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Chat not found")
    if chat.title is not None:
        existing.title = chat.title
    existing.messages = chat.messages
    db.add(existing)
    db.commit()
    db.refresh(existing)
    return existing
