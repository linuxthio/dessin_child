from fastapi import FastAPI

from .api import router

app = FastAPI(title="Galerie API")
app.include_router(router)
