# create_tables.py, generate tables directly from the Python models
from database import engine, Base
from models import User

print("I'm creating database tables...")
Base.metadata.create_all(bind=engine)
print("Tables created successfully!")
