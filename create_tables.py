# create_tables.py, generate tables directly from the Python models
from database import engine, Base
import models  # ensure all models are registered on Base.metadata

print("I'm creating database tables...")
Base.metadata.create_all(bind=engine)
print("Tables created successfully!!")
