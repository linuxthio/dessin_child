from typing import Annotated, Optional, Type

from django.contrib.auth import authenticate
from django.contrib.auth.password_validation import validate_password
from django.core.exceptions import ValidationError as DjangoValidationError
from django.core.files.base import ContentFile
from django.db import models as django_models
from django.db.models import F
from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status

from .api_auth import get_current_enfant, get_current_parent, get_enfant_token, get_parent_token
from .models import Dessin, Enfant, EnfantToken, Parent, ParentToken
from .schemas import (
    DessinEnfantOut,
    DessinOut,
    DessinPublicOut,
    DessinUpdateIn,
    EnfantLoginIn,
    EnfantOut,
    EnfantPublicOut,
    EnfantTokenOut,
    LoginIn,
    ParentOut,
    ParentRegisterIn,
    TokenOut,
)

router = APIRouter()


def _get_or_404(model: Type[django_models.Model], **kwargs) -> django_models.Model:
    try:
        return model.objects.get(**kwargs)
    except model.DoesNotExist:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Introuvable.")


def _file_url(file_field) -> Optional[str]:
    return file_field.url if file_field else None


def _parent_out(parent: Parent) -> ParentOut:
    return ParentOut(id=parent.id, username=parent.username, email=parent.email)


def _enfant_out(enfant: Enfant) -> EnfantOut:
    return EnfantOut(
        id=enfant.id,
        prenom=enfant.prenom,
        nom=enfant.nom,
        date_naissance=enfant.date_naissance,
        age=enfant.age,
        avatar=_file_url(enfant.avatar),
        pin_code=enfant.pin_code,
        created_at=enfant.created_at,
    )


def _enfant_public_out(enfant: Enfant) -> EnfantPublicOut:
    return EnfantPublicOut(id=enfant.id, prenom=enfant.prenom, avatar=_file_url(enfant.avatar))


def _dessin_out(dessin: Dessin) -> DessinOut:
    return DessinOut(
        id=dessin.id,
        titre=dessin.titre,
        image=_file_url(dessin.image),
        date_creation=dessin.date_creation,
        description=dessin.description,
        enfant=dessin.enfant_id,
        enfant_prenom=dessin.enfant.prenom,
        ajoute_par=dessin.ajoute_par,
        note=dessin.note,
        aime=dessin.aime,
        created_at=dessin.created_at,
    )


def _dessin_enfant_out(dessin: Dessin) -> DessinEnfantOut:
    return DessinEnfantOut(
        id=dessin.id,
        titre=dessin.titre,
        image=_file_url(dessin.image),
        date_creation=dessin.date_creation,
        description=dessin.description,
        ajoute_par=dessin.ajoute_par,
        note=dessin.note,
        aime=dessin.aime,
        created_at=dessin.created_at,
    )


def _dessin_public_out(dessin: Dessin) -> DessinPublicOut:
    return DessinPublicOut(
        id=dessin.id,
        titre=dessin.titre,
        image=_file_url(dessin.image),
        date_creation=dessin.date_creation,
        enfant_prenom=dessin.enfant.prenom,
        note=dessin.note,
        aime=dessin.aime,
        created_at=dessin.created_at,
    )


# ---------------------------------------------------------------------------
# Authentification Parent
# ---------------------------------------------------------------------------

@router.post("/auth/register/", response_model=TokenOut, status_code=status.HTTP_201_CREATED)
def register_parent(payload: ParentRegisterIn):
    if Parent.objects.filter(email__iexact=payload.email).exists():
        raise HTTPException(422, "Cette adresse email est déjà utilisée.")
    try:
        validate_password(payload.password)
    except DjangoValidationError as exc:
        raise HTTPException(422, "; ".join(exc.messages))

    parent = Parent.objects.create_user(
        username=payload.username, email=payload.email, password=payload.password
    )
    token, _ = ParentToken.objects.get_or_create(parent=parent)
    return TokenOut(token=token.key, parent=_parent_out(parent))


@router.post("/auth/login/", response_model=TokenOut)
def login_parent(payload: LoginIn):
    parent = authenticate(username=payload.email, password=payload.password)
    if parent is None:
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Email ou mot de passe incorrect.")
    token, _ = ParentToken.objects.get_or_create(parent=parent)
    return TokenOut(token=token.key, parent=_parent_out(parent))


@router.post("/auth/logout/", status_code=status.HTTP_204_NO_CONTENT)
def logout_parent(token: Annotated[ParentToken, Depends(get_parent_token)]):
    token.delete()


@router.get("/auth/me/", response_model=ParentOut)
def me(parent: Annotated[Parent, Depends(get_current_parent)]):
    return _parent_out(parent)


# ---------------------------------------------------------------------------
# Enfants (gérés par le parent)
# ---------------------------------------------------------------------------

@router.get("/enfants/", response_model=list[EnfantOut])
def list_enfants(parent: Annotated[Parent, Depends(get_current_parent)]):
    return [_enfant_out(e) for e in Enfant.objects.filter(parent=parent)]


@router.post("/enfants/", response_model=EnfantOut, status_code=status.HTTP_201_CREATED)
def create_enfant(
    parent: Annotated[Parent, Depends(get_current_parent)],
    prenom: str = Form(...),
    nom: str = Form(""),
    date_naissance: Optional[str] = Form(None),
    avatar: Optional[UploadFile] = File(None),
):
    enfant = Enfant(
        prenom=prenom, nom=nom, date_naissance=date_naissance or None, parent=parent
    )
    if avatar is not None:
        enfant.avatar.save(avatar.filename, ContentFile(avatar.file.read()), save=False)
    enfant.save()
    return _enfant_out(enfant)


@router.get("/enfants/public/", response_model=list[EnfantPublicOut])
def list_enfants_public():
    return [_enfant_public_out(e) for e in Enfant.objects.all()]


@router.get("/enfants/{enfant_id}/", response_model=EnfantOut)
def get_enfant(enfant_id: int, parent: Annotated[Parent, Depends(get_current_parent)]):
    enfant = _get_or_404(Enfant, pk=enfant_id, parent=parent)
    return _enfant_out(enfant)


@router.patch("/enfants/{enfant_id}/", response_model=EnfantOut)
def update_enfant(
    enfant_id: int,
    parent: Annotated[Parent, Depends(get_current_parent)],
    prenom: Optional[str] = Form(None),
    nom: Optional[str] = Form(None),
    date_naissance: Optional[str] = Form(None),
    avatar: Optional[UploadFile] = File(None),
):
    enfant = _get_or_404(Enfant, pk=enfant_id, parent=parent)
    if prenom is not None:
        enfant.prenom = prenom
    if nom is not None:
        enfant.nom = nom
    if date_naissance is not None:
        enfant.date_naissance = date_naissance or None
    if avatar is not None:
        enfant.avatar.save(avatar.filename, ContentFile(avatar.file.read()), save=False)
    enfant.save()
    return _enfant_out(enfant)


@router.delete("/enfants/{enfant_id}/", status_code=status.HTTP_204_NO_CONTENT)
def delete_enfant(enfant_id: int, parent: Annotated[Parent, Depends(get_current_parent)]):
    enfant = _get_or_404(Enfant, pk=enfant_id, parent=parent)
    enfant.delete()


# ---------------------------------------------------------------------------
# Authentification Enfant (PIN)
# ---------------------------------------------------------------------------

@router.post("/enfant/login/", response_model=EnfantTokenOut)
def enfant_login(payload: EnfantLoginIn):
    enfant = _get_or_404(Enfant, pk=payload.enfant_id)
    if enfant.pin_code != payload.pin_code:
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "PIN incorrect.")

    EnfantToken.objects.filter(enfant=enfant).delete()
    token = EnfantToken.objects.create(enfant=enfant)
    return EnfantTokenOut(enfant_token=token.key, enfant=_enfant_out(enfant))


@router.post("/enfant/logout/", status_code=status.HTTP_204_NO_CONTENT)
def enfant_logout(token: Annotated[EnfantToken, Depends(get_enfant_token)]):
    token.delete()


@router.get("/enfant/me/", response_model=EnfantOut)
def enfant_me(enfant: Annotated[Enfant, Depends(get_current_enfant)]):
    return _enfant_out(enfant)


# ---------------------------------------------------------------------------
# Dessins côté Parent
# ---------------------------------------------------------------------------

@router.get("/dessins/", response_model=list[DessinOut])
def list_dessins(
    parent: Annotated[Parent, Depends(get_current_parent)], enfant: Optional[int] = None
):
    qs = Dessin.objects.filter(enfant__parent=parent).select_related("enfant")
    if enfant:
        qs = qs.filter(enfant_id=enfant)
    return [_dessin_out(d) for d in qs]


@router.post("/dessins/", response_model=DessinOut, status_code=status.HTTP_201_CREATED)
def create_dessin(
    parent: Annotated[Parent, Depends(get_current_parent)],
    titre: str = Form(...),
    image: UploadFile = File(...),
    date_creation: str = Form(...),
    description: str = Form(""),
    enfant: int = Form(...),
    note: Optional[int] = Form(None),
    aime: bool = Form(False),
):
    if note is not None and not (1 <= note <= 5):
        raise HTTPException(422, "La note doit être comprise entre 1 et 5.")
    enfant_obj = _get_or_404(Enfant, pk=enfant)
    if enfant_obj.parent_id != parent.id:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Cet enfant ne vous appartient pas.")

    dessin = Dessin(
        titre=titre,
        date_creation=date_creation,
        description=description,
        enfant=enfant_obj,
        ajoute_par="PARENT",
        note=note,
        aime=aime,
    )
    dessin.image.save(image.filename, ContentFile(image.file.read()), save=False)
    dessin.save()
    return _dessin_out(dessin)


@router.get("/dessins/top/", response_model=list[DessinPublicOut])
def dessins_top():
    qs = Dessin.objects.select_related("enfant").order_by(
        F("note").desc(nulls_last=True), "-aime", "-created_at"
    )[:5]
    return [_dessin_public_out(d) for d in qs]


@router.get("/dessins/{dessin_id}/", response_model=DessinOut)
def get_dessin(dessin_id: int, parent: Annotated[Parent, Depends(get_current_parent)]):
    dessin = _get_or_404(Dessin, pk=dessin_id, enfant__parent=parent)
    return _dessin_out(dessin)


@router.patch("/dessins/{dessin_id}/", response_model=DessinOut)
def update_dessin(
    dessin_id: int,
    payload: DessinUpdateIn,
    parent: Annotated[Parent, Depends(get_current_parent)],
):
    dessin = _get_or_404(Dessin, pk=dessin_id, enfant__parent=parent)
    data = payload.model_dump(exclude_unset=True)
    if "enfant" in data:
        enfant_obj = _get_or_404(Enfant, pk=data.pop("enfant"), parent=parent)
        dessin.enfant = enfant_obj
    for field, value in data.items():
        setattr(dessin, field, value)
    dessin.save()
    return _dessin_out(dessin)


@router.delete("/dessins/{dessin_id}/", status_code=status.HTTP_204_NO_CONTENT)
def delete_dessin(dessin_id: int, parent: Annotated[Parent, Depends(get_current_parent)]):
    dessin = _get_or_404(Dessin, pk=dessin_id, enfant__parent=parent)
    dessin.delete()


# ---------------------------------------------------------------------------
# Dessins côté Enfant
# ---------------------------------------------------------------------------

@router.get("/enfant/dessins/", response_model=list[DessinEnfantOut])
def list_enfant_dessins(enfant: Annotated[Enfant, Depends(get_current_enfant)]):
    return [_dessin_enfant_out(d) for d in Dessin.objects.filter(enfant=enfant)]


@router.post(
    "/enfant/dessins/", response_model=DessinEnfantOut, status_code=status.HTTP_201_CREATED
)
def create_enfant_dessin(
    enfant: Annotated[Enfant, Depends(get_current_enfant)],
    titre: str = Form(...),
    image: UploadFile = File(...),
    date_creation: str = Form(...),
    description: str = Form(""),
    note: Optional[int] = Form(None),
    aime: bool = Form(False),
):
    if note is not None and not (1 <= note <= 5):
        raise HTTPException(422, "La note doit être comprise entre 1 et 5.")
    dessin = Dessin(
        titre=titre,
        date_creation=date_creation,
        description=description,
        enfant=enfant,
        ajoute_par="ENFANT",
        note=note,
        aime=aime,
    )
    dessin.image.save(image.filename, ContentFile(image.file.read()), save=False)
    dessin.save()
    return _dessin_enfant_out(dessin)


@router.get("/enfant/dessins/{dessin_id}/", response_model=DessinEnfantOut)
def get_enfant_dessin(dessin_id: int, enfant: Annotated[Enfant, Depends(get_current_enfant)]):
    dessin = _get_or_404(Dessin, pk=dessin_id, enfant=enfant)
    return _dessin_enfant_out(dessin)


@router.patch("/enfant/dessins/{dessin_id}/", response_model=DessinEnfantOut)
def update_enfant_dessin(
    dessin_id: int,
    payload: DessinUpdateIn,
    enfant: Annotated[Enfant, Depends(get_current_enfant)],
):
    dessin = _get_or_404(Dessin, pk=dessin_id, enfant=enfant)
    data = payload.model_dump(exclude_unset=True, exclude={"enfant"})
    for field, value in data.items():
        setattr(dessin, field, value)
    dessin.save()
    return _dessin_enfant_out(dessin)


@router.delete("/enfant/dessins/{dessin_id}/", status_code=status.HTTP_204_NO_CONTENT)
def delete_enfant_dessin(dessin_id: int, enfant: Annotated[Enfant, Depends(get_current_enfant)]):
    dessin = _get_or_404(Dessin, pk=dessin_id, enfant=enfant)
    dessin.delete()
