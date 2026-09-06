"""
ASGI config for galerie project.

It exposes the ASGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/6.1/howto/deployment/asgi/
"""

import os

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'galerie.settings')

from django.core.asgi import get_asgi_application

django_asgi_app = get_asgi_application()

from starlette.applications import Starlette
from starlette.routing import Mount

from gallery.fastapi_app import app as fastapi_app

application = Starlette(
    routes=[
        Mount('/api/v1', app=fastapi_app),
        Mount('/', app=django_asgi_app),
    ]
)
