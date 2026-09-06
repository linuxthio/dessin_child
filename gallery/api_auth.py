from rest_framework.authentication import BaseAuthentication, get_authorization_header
from rest_framework.exceptions import AuthenticationFailed
from rest_framework.permissions import BasePermission

from .models import EnfantToken


class EnfantTokenAuthentication(BaseAuthentication):
    """Authentifie un enfant via l'en-tête `Authorization: Enfant-Token <clé>`.

    En cas de succès, `request.user` reste anonyme et `request.auth` contient
    l'objet EnfantToken (donc `request.auth.enfant` donne l'enfant connecté).
    """

    keyword = "Enfant-Token"

    def authenticate(self, request):
        auth = get_authorization_header(request).split()

        if not auth or auth[0].decode().lower() != self.keyword.lower():
            return None

        if len(auth) != 2:
            raise AuthenticationFailed("En-tête d'authentification invalide.")

        try:
            key = auth[1].decode()
        except UnicodeError:
            raise AuthenticationFailed("En-tête d'authentification invalide.")

        try:
            token = EnfantToken.objects.select_related("enfant").get(key=key)
        except EnfantToken.DoesNotExist:
            raise AuthenticationFailed("Jeton enfant invalide ou expiré.")

        return (None, token)

    def authenticate_header(self, request):
        return self.keyword


class IsEnfantAuthenticated(BasePermission):
    def has_permission(self, request, view):
        return bool(request.auth) and hasattr(request.auth, "enfant")
