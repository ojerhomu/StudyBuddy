# main.py
#AAAAAAAAAAAAAAAH I HATE THIS
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from fastapi.middleware.cors import CORSMiddleware
from database import engine, SessionLocal, Base
import models, schemas
from auth import hash_password, verify_password, create_access_token, oauth2_scheme, get_current_user
from fastapi.security import OAuth2PasswordBearer
from auth import get_current_user
import models, schemas
from fastapi import Depends, HTTPException, status
from routers import tasks

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="login")

Base.metadata.create_all(bind=engine)

app = FastAPI()

app.include_router(tasks.router)

#this is CORS middleware for Android/Frontend access
origins = [
    "http://localhost:3000",  # incase of React
    "http://127.0.0.1:3000",
    "http://localhost:5173",  # incase of Vite
    "http://127.0.0.1:5173",
    "http://localhost:8080",  # Vue / Android WebView
    "*",  # allow all during development
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

#database dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# authentication routes
@app.post("/register", response_model=schemas.UserOut)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    existing_user = db.query(models.User).filter(models.User.email == user.email).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")

    hashed_pw = hash_password(user.password)
    new_user = models.User(email=user.email, hashed_password=hashed_pw)
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user


@app.post("/login", response_model=schemas.Token)
def login(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if not db_user or not verify_password(user.password, db_user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    token = create_access_token({"sub": user.email})
    return {"access_token": token, "token_type": "bearer"}


# protected route
@app.get("/profile", response_model=schemas.UserOut)
def read_profile(current_user: models.User = Depends(get_current_user)):
    return current_user


# temp logout
@app.post("/logout")
def logout(token: str = Depends(oauth2_scheme)):
    return {
        "message": "You have successfully logged out. (Remove your token on the client side — will secure later)"
    }

#token-protected routes, tied to the authenticated user

@app.get("/tasks", response_model=list[schemas.TaskOut])
def get_tasks(current_user: models.User = Depends(get_current_user), db: Session = Depends(get_db)):
    return db.query(models.Task).filter(models.Task.owner_id == current_user.id).all()


@app.post("/tasks", response_model=schemas.TaskOut)
def create_task(
    task: schemas.TaskCreate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    new_task = models.Task(
        title=task.title,
        description=task.description,
        due_date=task.due_date,
        owner_id=current_user.id
    )
    db.add(new_task)
    db.commit()
    db.refresh(new_task)
    return new_task



@app.delete("/tasks/{task_id}")
def delete_task(task_id: int, current_user: models.User = Depends(get_current_user), db: Session = Depends(get_db)):
    task = db.query(models.Task).filter(models.Task.id == task_id, models.Task.owner_id == current_user.id).first()
    if not task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")
    db.delete(task)
    db.commit()
    return {"message": "Task deleted successfully"}
