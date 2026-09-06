from django.urls import path

from . import views

app_name = "gallery"

urlpatterns = [
    path("", views.accueil, name="accueil"),
    # Parent
    path("parent/inscription/", views.register_parent, name="register_parent"),
    path("parent/connexion/", views.login_parent, name="login_parent"),
    path("parent/deconnexion/", views.logout_parent, name="logout_parent"),
    path("parent/tableau-de-bord/", views.dashboard_parent, name="dashboard_parent"),
    path("parent/enfants/nouveau/", views.enfant_creer, name="enfant_creer"),
    path(
        "parent/enfants/<int:pk>/modifier/",
        views.enfant_modifier,
        name="enfant_modifier",
    ),
    path(
        "parent/enfants/<int:pk>/supprimer/",
        views.enfant_supprimer,
        name="enfant_supprimer",
    ),
    path("parent/dessins/nouveau/", views.dessin_upload_parent, name="dessin_upload_parent"),
    path(
        "parent/dessins/<int:pk>/supprimer/",
        views.dessin_supprimer,
        name="dessin_supprimer",
    ),
    # Enfant
    path("enfant/connexion/", views.login_enfant_select, name="login_enfant"),
    path("enfant/connexion/pin/", views.login_enfant_pin, name="login_enfant_pin"),
    path("enfant/deconnexion/", views.logout_enfant, name="logout_enfant"),
    path("enfant/tableau-de-bord/", views.dashboard_enfant, name="dashboard_enfant"),
    path("enfant/dessins/nouveau/", views.dessin_upload_enfant, name="dessin_upload_enfant"),
]
