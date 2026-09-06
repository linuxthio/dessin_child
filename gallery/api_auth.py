from typing import Annotated, Optional

from fastapi import Depends, Header, HTTPException, status

from .models import Enfant, EnfantToken, Parent, ParentToken


def get_parent_token(
    authorization: Annotated[Optional[str], Header()] = None,
) -> ParentToken:
    """Authentifie un parent via l'en-tête `Authorization: Token <clé>`."""
    parts = (authorization or "").split()
    if len(parts) != 2 or parts[0].lower() != "token":
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Authentification requise.")
    try:
        return ParentToken.objects.select_related("parent").get(key=parts[1])
    except ParentToken.DoesNotExist:
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Jeton invalide.")


def get_current_parent(token: Annotated[ParentToken, Depends(get_parent_token)]) -> Parent:
    return token.parent


def get_enfant_token(
    authorization: Annotated[Optional[str], Header()] = None,
) -> EnfantToken:
    """Authentifie un enfant via l'en-tête `Authorization: Enfant-Token <clé>`."""
    parts = (authorization or "").split()
    if len(parts) != 2 or parts[0].lower() != "enfant-token":
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Authentification requise.")
    try:
        return EnfantToken.objects.select_related("enfant").get(key=parts[1])
    except EnfantToken.DoesNotExist:
        raise HTTPException(
            status.HTTP_401_UNAUTHORIZED, "Jeton enfant invalide ou expiré."
        )


def get_current_enfant(token: Annotated[EnfantToken, Depends(get_enfant_token)]) -> Enfant:
    return token.enfant
