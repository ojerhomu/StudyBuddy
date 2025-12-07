from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

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
    return _save_schedule(payload, current_user, db)


def _save_schedule(payload: schemas.ScheduleCreateRequest, current_user: models.User, db: Session):
    if not payload.schedules:
        raise HTTPException(status_code=400, detail="No schedule entries provided")

    user_courses = db.query(models.UserCourse).filter(models.UserCourse.user_id == current_user.id).all()
    courses_by_id = {c.id: c for c in user_courses}
    courses_by_name = {c.course_name: c for c in user_courses}

    target_course_ids = set()
    normalized_items = []

    for item in payload.schedules:
        # Resolve or create course
        course = None
        if item.course_id:
            course = courses_by_id.get(item.course_id)
            if not course:
                raise HTTPException(status_code=404, detail="Course not found for this user")
        else:
            course = courses_by_name.get(item.course_name)
            if not course:
                course = models.UserCourse(
                    course_name=item.course_name,
                    subject_category=None,
                    color=None,
                    user_id=current_user.id,
                )
                db.add(course)
                db.flush()
                courses_by_id[course.id] = course
                courses_by_name[course.course_name] = course

        target_course_ids.add(course.id)
        normalized_items.append(
            {
                "course_id": course.id,
                "day_of_week": item.day_of_week,
                "start_time": item.start_time,
                "end_time": item.end_time,
                "is_asynchronous": item.is_asynchronous,
            }
        )

    if target_course_ids:
        db.query(models.ClassSchedule).filter(models.ClassSchedule.course_id.in_(target_course_ids)).delete(synchronize_session=False)
        db.flush()

    new_schedules: List[models.ClassSchedule] = []
    for data in normalized_items:
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
    payload: schemas.ScheduleCreateRequest,
    current_user: models.User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    return _save_schedule(payload, current_user, db)
