# main.py
#AAAAAAAAAAAAAAAH I HATE THIS SO MUCH
from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy.orm import Session
from fastapi.middleware.cors import CORSMiddleware
from database import engine, SessionLocal, Base
import models, schemas
from auth import hash_password, verify_password, create_access_token, get_current_user
from fastapi.security import OAuth2PasswordRequestForm
from routers import tasks, calendar, users, chats
from fastapi.openapi.utils import get_openapi

Base.metadata.create_all(bind=engine)

app = FastAPI()
app.router.redirect_slashes = False  # avoid redirect dropping token

def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema
    openapi_schema = get_openapi(
        title="StudyBuddy API",
        version="1.0.0",
        description="Backend API for StudyBuddy (auth + tasks + calendar)",
        routes=app.routes,
    )
    openapi_schema["components"]["securitySchemes"] = {
        "BearerAuth": {
            "type": "http",
            "scheme": "bearer",
            "bearerFormat": "JWT",
        }
    }
    openapi_schema["security"] = [{"BearerAuth": []}]
    app.openapi_schema = openapi_schema
    return app.openapi_schema

app.openapi = custom_openapi

#routers
app.include_router(tasks.router)
app.include_router(calendar.router)
app.include_router(users.router)
app.include_router(users.public_router)
app.include_router(chats.router)

# cors
origins = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "http://localhost:8080",
]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# database dependency 
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
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == form_data.username).first()
    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    token = create_access_token({"sub": user.email})
    return {"access_token": token, "token_type": "bearer"}


@app.get("/profile", response_model=schemas.UserOut)
def read_profile(current_user: models.User = Depends(get_current_user)):
    return current_user

# temp logout
@app.post("/logout")
def logout():
    return {"message": "Logged out (remove token client-side)"}
