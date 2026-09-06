from datetime import date, datetime
from typing import Optional

from pydantic import BaseModel, EmailStr, Field, field_validator


class ParentOut(BaseModel):
    id: int
    username: str
    email: str


class ParentRegisterIn(BaseModel):
    username: str
    email: EmailStr
    password: str


class LoginIn(BaseModel):
    email: str
    password: str


class TokenOut(BaseModel):
    token: str
    parent: ParentOut


class EnfantOut(BaseModel):
    id: int
    prenom: str
    nom: str
    date_naissance: Optional[date] = None
    age: Optional[int] = None
    avatar: Optional[str] = None
    pin_code: str
    created_at: datetime


class EnfantPublicOut(BaseModel):
    id: int
    prenom: str
    avatar: Optional[str] = None


class EnfantLoginIn(BaseModel):
    enfant_id: int
    pin_code: str

    @field_validator("pin_code", mode="before")
    @classmethod
    def _coerce_pin(cls, value):
        return str(value)


class EnfantTokenOut(BaseModel):
    enfant_token: str
    enfant: EnfantOut


class DessinOut(BaseModel):
    id: int
    titre: str
    image: Optional[str] = None
    date_creation: date
    description: str
    enfant: int
    enfant_prenom: str
    ajoute_par: str
    note: Optional[int] = None
    aime: bool
    created_at: datetime


class DessinEnfantOut(BaseModel):
    id: int
    titre: str
    image: Optional[str] = None
    date_creation: date
    description: str
    ajoute_par: str
    note: Optional[int] = None
    aime: bool
    created_at: datetime


class DessinPublicOut(BaseModel):
    id: int
    titre: str
    image: Optional[str] = None
    date_creation: date
    enfant_prenom: str
    note: Optional[int] = None
    aime: bool
    created_at: datetime


class DessinUpdateIn(BaseModel):
    """Mise à jour partielle (JSON) : ne permet pas de changer l'image."""

    titre: Optional[str] = None
    date_creation: Optional[date] = None
    description: Optional[str] = None
    enfant: Optional[int] = None
    note: Optional[int] = Field(default=None, ge=1, le=5)
    aime: Optional[bool] = None
