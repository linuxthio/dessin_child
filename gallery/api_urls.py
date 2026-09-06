from django.urls import path

from . import api

app_name = "api"

urlpatterns = [
    # Auth parent
    path("auth/register/", api.RegisterParentAPIView.as_view(), name="register"),
    path("auth/login/", api.LoginParentAPIView.as_view(), name="login"),
    path("auth/logout/", api.LogoutParentAPIView.as_view(), name="logout"),
    path("auth/me/", api.MeAPIView.as_view(), name="me"),
    # Enfants (parent)
    path("enfants/", api.EnfantListCreateAPIView.as_view(), name="enfant_list"),
    path(
        "enfants/public/",
        api.EnfantPublicListAPIView.as_view(),
        name="enfant_public_list",
    ),
    path("enfants/<int:pk>/", api.EnfantDetailAPIView.as_view(), name="enfant_detail"),
    # Dessins (parent)
    path("dessins/", api.DessinListCreateAPIView.as_view(), name="dessin_list"),
    path("dessins/top/", api.DessinTopListAPIView.as_view(), name="dessin_top_list"),
    path("dessins/<int:pk>/", api.DessinDetailAPIView.as_view(), name="dessin_detail"),
    # Auth enfant
    path("enfant/login/", api.EnfantLoginAPIView.as_view(), name="enfant_login"),
    path("enfant/logout/", api.EnfantLogoutAPIView.as_view(), name="enfant_logout"),
    path("enfant/me/", api.EnfantMeAPIView.as_view(), name="enfant_me"),
    # Dessins (enfant)
    path(
        "enfant/dessins/",
        api.EnfantDessinListCreateAPIView.as_view(),
        name="enfant_dessin_list",
    ),
    path(
        "enfant/dessins/<int:pk>/",
        api.EnfantDessinDetailAPIView.as_view(),
        name="enfant_dessin_detail",
    ),
]
