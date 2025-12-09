from fastapi import APIRouter, Depends, HTTPException, status, Body
from sqlalchemy.orm import Session
from typing import List, Any
from datetime import datetime

import models
import schemas
from database import get_db
from auth import get_current_user

router = APIRouter(prefix="/users/me", tags=["Users"])
public_router = APIRouter(tags=["Users"])


def _apply_profile_update(profile: schemas.UserProfileUpdate, current_user: models.User, db: Session):
    db_user = db.query(models.User).filter(models.User.id == current_user.id).first()
    if not db_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    db_user.first_name = profile.first_name
    db_user.last_name = profile.last_name
    if profile.education_level is not None:
        db_user.education_level = profile.education_level
    if profile.pomodoro_study_minutes is not None:
        db_user.pomodoro_study_minutes = profile.pomodoro_study_minutes
    if profile.pomodoro_short_break_minutes is not None:
        db_user.pomodoro_short_break_minutes = profile.pomodoro_short_break_minutes
    if profile.pomodoro_long_break_minutes is not None:
        db_user.pomodoro_long_break_minutes = profile.pomodoro_long_break_minutes
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


@router.patch("/profile", response_model=schemas.UserOut)
def update_profile(
    profile: schemas.UserProfileUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return _apply_profile_update(profile, current_user, db)


@public_router.patch("/profile", response_model=schemas.UserOut)
def update_profile_no_prefix(
    profile: schemas.UserProfileUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return _apply_profile_update(profile, current_user, db)


def _apply_pomodoro_preferences(pref: schemas.PomodoroPreferencesUpdate, current_user: models.User, db: Session):
    db_user = db.query(models.User).filter(models.User.id == current_user.id).first()
    if not db_user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    db_user.pomodoro_study_minutes = pref.pomodoro_study_minutes
    db_user.pomodoro_short_break_minutes = pref.pomodoro_short_break_minutes
    db_user.pomodoro_long_break_minutes = pref.pomodoro_long_break_minutes
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


@public_router.patch("/profile/preferences", response_model=schemas.UserOut)
def update_preferences_no_prefix(
    preferences: schemas.PomodoroPreferencesUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return _apply_pomodoro_preferences(preferences, current_user, db)


@router.patch("/preferences", response_model=schemas.UserOut)
def update_preferences(
    preferences: schemas.PomodoroPreferencesUpdate,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return _apply_pomodoro_preferences(preferences, current_user, db)


@router.post("/courses", response_model=List[schemas.UserCourseOut], status_code=status.HTTP_201_CREATED)
def set_courses(
    payload: schemas.UserCoursesCreateRequest,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if len(payload.courses) > 7:
        raise HTTPException(status_code=400, detail="You can only store up to 7 courses")

    existing_courses = db.query(models.UserCourse).filter(models.UserCourse.user_id == current_user.id).all()
    for course in existing_courses:
        db.delete(course)
    db.flush()

    new_courses: List[models.UserCourse] = []
    for course_in in payload.courses:
        new_course = models.UserCourse(
            course_name=course_in.course_name,
            subject_category=course_in.subject_category,
            color=course_in.color,
            user_id=current_user.id,
        )
        db.add(new_course)
        new_courses.append(new_course)

    db.commit()
    for course in new_courses:
        db.refresh(course)
    return new_courses


@router.post("/schedule", response_model=List[schemas.ClassScheduleOut], status_code=status.HTTP_201_CREATED)
def set_schedule(
    payload: schemas.ScheduleCreateRequest,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    items = [i.model_dump() for i in payload.schedules]
    return _save_schedule(items, current_user, db)


def _save_schedule(items: List[dict[str, Any]], current_user: models.User, db: Session):
    if not items:
        raise HTTPException(status_code=400, detail="No schedule entries provided")

    user_courses = db.query(models.UserCourse).filter(models.UserCourse.user_id == current_user.id).all()
    courses_by_id = {c.id: c for c in user_courses}
    courses_by_name = {c.course_name: c for c in user_courses}
    courses_by_norm = {c.course_name.strip().lower(): c for c in user_courses}

    target_course_ids = set()
    normalized_items = []

    for item in items:
        # reslove or create course
        course = None
        color = item.get("color")
        skip_insert = item.get("skip_insert", False)
        course_id_val = item.get("course_id")
        course_name_val = item.get("course_name")
        if course_id_val:
            course = courses_by_id.get(course_id_val)
            if not course:
                raise HTTPException(status_code=404, detail="Course not found for this user")
        else:
            normalized_name = (course_name_val or "").strip().lower()
            course = courses_by_norm.get(normalized_name)
            if not course:
                course = models.UserCourse(
                    course_name=course_name_val,
                    subject_category=None,
                    color=color,
                    user_id=current_user.id,
                )
                db.add(course)
                db.flush()
                courses_by_id[course.id] = course
                courses_by_name[course.course_name] = course
                courses_by_norm[normalized_name] = course

        if color is not None and course.color != color:
            course.color = color
            db.add(course)

        target_course_ids.add(course.id)
        normalized_items.append(
            {
                "course_id": course.id,
                "day_of_week": item.get("day_of_week"),
                "start_time": item.get("start_time"),
                "end_time": item.get("end_time"),
                "is_asynchronous": item.get("is_asynchronous", False),
                "skip_insert": skip_insert,
            }
        )

    if target_course_ids:
        db.query(models.ClassSchedule).filter(models.ClassSchedule.course_id.in_(target_course_ids)).delete(synchronize_session=False)
        db.flush()

    new_schedules: List[models.ClassSchedule] = []
    seen = set()
    for data in normalized_items:
        skip = data.pop("skip_insert", False)
        if skip:
            continue
        dedupe_key = (
            data["course_id"],
            data["day_of_week"],
            data["start_time"],
            data["end_time"],
            data["is_asynchronous"],
        )
        if dedupe_key in seen:
            continue
        seen.add(dedupe_key)
        sched = models.ClassSchedule(**data)
        db.add(sched)
        new_schedules.append(sched)

    db.commit()
    for sched in new_schedules:
        db.refresh(sched)
    return new_schedules


@router.get("/courses", response_model=List[schemas.UserCourseOut])
def list_courses(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return (
        db.query(models.UserCourse)
        .filter(models.UserCourse.user_id == current_user.id)
        .all()
    )


@router.get("/schedule", response_model=List[schemas.ClassScheduleOut])
def list_schedule(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return (
        db.query(models.ClassSchedule)
        .join(models.UserCourse, models.ClassSchedule.course_id == models.UserCourse.id)
        .filter(models.UserCourse.user_id == current_user.id)
        .all()
    )


@public_router.get("/courses", response_model=List[schemas.UserCourseOut])
def list_courses_no_prefix(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return (
        db.query(models.UserCourse)
        .filter(models.UserCourse.user_id == current_user.id)
        .all()
    )


@public_router.get("/schedule", response_model=List[schemas.ClassScheduleOut])
def list_schedule_no_prefix(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return (
        db.query(models.ClassSchedule)
        .join(models.UserCourse, models.ClassSchedule.course_id == models.UserCourse.id)
        .filter(models.UserCourse.user_id == current_user.id)
        .all()
    )


def _color_for_category(category: str | None) -> str:
    palette = {
        "Math": "#4F46E5",
        "Science": "#0EA5E9",
        "English": "#22C55E",
        "Social Studies": "#F97316",
        "Other": "#A855F7",
    }
    return palette.get(category or "", "#3B82F6")


@public_router.get("/profile/schedule")
def get_profile_schedule(
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    courses = db.query(models.UserCourse).filter(models.UserCourse.user_id == current_user.id).all()
    course_ids = [c.id for c in courses]
    schedules = (
        db.query(models.ClassSchedule)
        .filter(models.ClassSchedule.course_id.in_(course_ids) if course_ids else False)
        .all()
    )

    schedules_by_course: dict[int, list[models.ClassSchedule]] = {}
    for sched in schedules:
        schedules_by_course.setdefault(sched.course_id, []).append(sched)

    resp = {"schedule": {}}
    for course in courses:
        entries = []
        for sched in schedules_by_course.get(course.id, []):
            if sched.is_asynchronous:
                entries.append({"day": "ASYNCHRONOUS", "startTime": None, "endTime": None})
            else:
                day = (sched.day_of_week or "").upper()
                start = sched.start_time.strftime("%H:%M") if sched.start_time else None
                end = sched.end_time.strftime("%H:%M") if sched.end_time else None
                entries.append({"day": day, "startTime": start, "endTime": end})
        resp["schedule"][course.course_name] = {
            "schedule": entries,
            "color": course.color or _color_for_category(course.subject_category),
        }

    return resp


@public_router.post("/profile/schedule", response_model=List[schemas.ClassScheduleOut], status_code=status.HTTP_201_CREATED)
def save_profile_schedule(
    payload: dict = Body(...),
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # make sure to support both the original list format and the new map format
    if "schedules" in payload:
        model = schemas.ScheduleCreateRequest.model_validate(payload)
        items = [i.model_dump() for i in model.schedules]
        return _save_schedule(items, current_user, db)

    if "schedule" in payload:
        items: List[dict[str, Any]] = []
        for course_name, subject in payload["schedule"].items():
            color = subject.get("color")
            entries = subject.get("schedule", [])
            if not entries:
                # clear existing schedules for this course, but don't insert new rows; still update color
                items.append(
                    {
                        "course_name": course_name,
                        "is_asynchronous": True,
                        "day_of_week": None,
                        "start_time": None,
                        "end_time": None,
                        "color": color,
                        "skip_insert": True,
                    }
                )
                continue

            for entry in entries:
                day = entry.get("day")
                start = entry.get("startTime")
                end = entry.get("endTime")
                start_time_obj = datetime.strptime(start, "%H:%M").time() if start else None
                end_time_obj = datetime.strptime(end, "%H:%M").time() if end else None
                items.append(
                    {
                        "course_name": course_name,
                        "day_of_week": day,
                        "start_time": start_time_obj,
                        "end_time": end_time_obj,
                        "is_asynchronous": False,
                        "color": color,
                    }
                )
        if not items:
            raise HTTPException(status_code=400, detail="No schedule entries provided")
        return _save_schedule(items, current_user, db)

    raise HTTPException(status_code=400, detail="Invalid schedule payload")
