from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Enum, Boolean, Time, JSON
from sqlalchemy.orm import relationship
from datetime import datetime
from enum import Enum as PyEnum
from database import Base


class EducationLevel(str, PyEnum):
    MIDDLE_SCHOOL = "Middle School"
    HIGH_SCHOOL = "High School"
    COLLEGE = "College"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True)
    hashed_password = Column(String)
    first_name = Column(String, nullable=True)
    last_name = Column(String, nullable=True)
    education_level = Column(Enum(EducationLevel, native_enum=False), nullable=True)
    tasks = relationship("Task", back_populates="owner")
    events = relationship("Event", back_populates="owner", cascade="all, delete-orphan")
    courses = relationship("UserCourse", back_populates="user", cascade="all, delete-orphan")
    chat_sessions = relationship("ChatSession", back_populates="user", cascade="all, delete-orphan")

class Task(Base): #task model linked to the User, defines a one-to-many relationship between user and task i think
    __tablename__ = "tasks"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, nullable=False)
    description = Column(String)
    due_date = Column(DateTime)
    created_at = Column(DateTime, default=datetime.utcnow)
    owner_id = Column(Integer, ForeignKey("users.id"))
    owner = relationship("User", back_populates="tasks")

class Event(Base): #calendar stuff: creates new events table for calendar, each even belongs to a user using owner_id, supports differnt even types, tracks start and end times
    __tablename__ = "events"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, nullable=False)
    description = Column(String)
    start_time = Column(DateTime, nullable=False)
    end_time = Column(DateTime, nullable=False)
    event_type = Column(String, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    owner_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    owner = relationship("User", back_populates="events")


class UserCourse(Base):
    __tablename__ = "user_courses"

    id = Column(Integer, primary_key=True, index=True)
    course_name = Column(String, nullable=False)
    subject_category = Column(String, nullable=True)
    color = Column(String, nullable=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    user = relationship("User", back_populates="courses")
    schedules = relationship("ClassSchedule", back_populates="course", cascade="all, delete-orphan")


class ClassSchedule(Base):
    __tablename__ = "class_schedules"

    id = Column(Integer, primary_key=True, index=True)
    day_of_week = Column(String, nullable=True)
    start_time = Column(Time, nullable=True)
    end_time = Column(Time, nullable=True)
    is_asynchronous = Column(Boolean, default=False)
    course_id = Column(Integer, ForeignKey("user_courses.id"), nullable=False)
    course = relationship("UserCourse", back_populates="schedules")


class ChatSession(Base):
    __tablename__ = "chat_sessions"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, nullable=False)
    messages = Column(JSON, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    user = relationship("User", back_populates="chat_sessions")
