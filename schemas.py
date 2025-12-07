# schemas.py, defines how data is sent and received in API requests/responses defaults are EmailStr for email and str for password
from pydantic import BaseModel, EmailStr, model_validator, field_validator, ConfigDict
from datetime import datetime, time
from typing import Optional, List, Any
from models import EducationLevel

# User Schemas
class UserCreate(BaseModel):
    email: EmailStr
    password: str


class UserOut(BaseModel):
    id: int
    email: EmailStr
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    education_level: Optional[EducationLevel] = None

    model_config = ConfigDict(from_attributes=True)

class UserBase(BaseModel): #will this fix this cursed code???
    email: EmailStr

class UserProfileUpdate(BaseModel):
    first_name: str
    last_name: str
    education_level: Optional[EducationLevel] = None

    @field_validator("education_level", mode="before")
    def normalize_education_level(cls, v):
        if v is None or isinstance(v, EducationLevel):
            return v
        text = str(v).strip().lower().replace("_", " ")
        mapping = {
            "middle school": EducationLevel.MIDDLE_SCHOOL,
            "middleschool": EducationLevel.MIDDLE_SCHOOL,
            "high school": EducationLevel.HIGH_SCHOOL,
            "highschool": EducationLevel.HIGH_SCHOOL,
            "college": EducationLevel.COLLEGE,
        }
        if text in mapping:
            return mapping[text]
        raise ValueError("education_level must be one of: Middle School, High School, College")

# Task Schemas
class TaskBase(BaseModel):
    title: str
    description: Optional[str] = None
    due_date: Optional[datetime] = None


class TaskCreate(TaskBase):
    pass

class TaskOut(TaskBase):
    id: int
    owner_id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class Token(BaseModel):
    access_token: str
    token_type: str

    # base Event schema (shared properties) (calendar stuff belowh here)
class EventBase(BaseModel):
    title: str
    description: Optional[str] = None
    start_time: datetime
    end_time: datetime
    event_type: str  #'assignment', 'quiz', 'test', 'study'

#  for creating a new event
class EventCreate(EventBase):
    pass

#  for returning event data (includes database fields)
class EventOut(EventBase):
    id: int
    owner_id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

# Course Schemas
class UserCourseBase(BaseModel):
    course_name: str
    subject_category: Optional[str] = None
    color: Optional[str] = None


class UserCourseCreate(UserCourseBase):
    pass


class UserCourseOut(UserCourseBase):
    id: int

    model_config = ConfigDict(from_attributes=True)


class UserCoursesCreateRequest(BaseModel):
    courses: List[UserCourseCreate]

    @field_validator("courses")
    def validate_course_limit(cls, v):
        if len(v) > 7:
            raise ValueError("You can only store up to 7 courses")
        return v

# Schedule Schemas
class ClassScheduleBase(BaseModel):
    course_id: Optional[int] = None
    course_name: Optional[str] = None
    day_of_week: Optional[str] = None
    start_time: Optional[time] = None
    end_time: Optional[time] = None
    is_asynchronous: bool = False

    @model_validator(mode="after")
    def validate_schedule(self):
        if self.course_id is None and not self.course_name:
            raise ValueError("Either course_id or course_name is required")

        if self.is_asynchronous:
            self.day_of_week = None
            self.start_time = None
            self.end_time = None
            return self

        if not self.day_of_week:
            raise ValueError("day_of_week is required for scheduled classes")

        # normalize day input (accepts Monday, MONDAY, mon, mon-day)
        day_map = {
            "monday": "Monday",
            "mon": "Monday",
            "tuesday": "Tuesday",
            "tue": "Tuesday",
            "tues": "Tuesday",
            "wednesday": "Wednesday",
            "wed": "Wednesday",
            "thursday": "Thursday",
            "thu": "Thursday",
            "thur": "Thursday",
            "thurs": "Thursday",
            "friday": "Friday",
            "fri": "Friday",
            "saturday": "Saturday",
            "sat": "Saturday",
            "sunday": "Sunday",
            "sun": "Sunday",
        }
        normalized = day_map.get(str(self.day_of_week).lower().replace("-", "").strip())
        if not normalized:
            raise ValueError("day_of_week must be a valid weekday name (e.g., Monday)")
        self.day_of_week = normalized

        if not self.start_time:
            raise ValueError("start_time is required for scheduled classes")
        if self.end_time and self.end_time <= self.start_time:
            raise ValueError("end_time must be after start_time")
        return self

class ClassScheduleCreate(ClassScheduleBase):
    pass


class ClassScheduleOut(ClassScheduleBase):
    id: int

    model_config = ConfigDict(from_attributes=True)


class ScheduleCreateRequest(BaseModel):
    schedules: List[ClassScheduleCreate]


# Chat Schemas
class ChatSessionCreate(BaseModel):
    title: str
    messages: List[Any]


class ChatSessionSummary(BaseModel):
    id: int
    title: str
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class ChatSessionOut(ChatSessionSummary):
    messages: List[Any]

    model_config = ConfigDict(from_attributes=True)
