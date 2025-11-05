from fastapi import FastAPI
from pydantic import BaseModel
from typing import List


app = FastAPI()


# Pydantic models
class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    token: str | None = None


class Task(BaseModel):
    id: int
    title: str
    due_date: str


class ScheduleRequest(BaseModel):
    title: str
    datetime: str


# In-memory stores for prototype
TASKS = [Task(id=1, title="Math HW 3", due_date="2025-10-15")]


@app.post("/auth/login", response_model=LoginResponse)
async def login(req: LoginRequest):
# TODO: validate credentials properly
    if req.username:
        return LoginResponse(token="fake-jwt-token")
        return LoginResponse(token=None)


@app.get("/tasks", response_model=List[Task])
async def get_tasks():
    return TASKS


@app.post("/schedule")
async def create_schedule(req: ScheduleRequest):
# placeholder: store schedule or call model
    return {"status": "ok"}


@app.get("/recommend")
async def recommend():
# placeholder for AI-driven recommendations
    return ["Review for your Calculus 2 test", "This is a test message and this project stinks"]