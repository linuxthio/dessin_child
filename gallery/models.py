import random
import secrets

from django.contrib.auth.models import AbstractUser
from django.core.validators import MaxValueValidator, MinValueValidator
from django.db import models
from django.utils import timezone


class Parent(AbstractUser):
    """Utilisateur parent : authentification par email + mot de passe."""

    email = models.EmailField("adresse email", unique=True)

    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["username"]

    def __str__(self):
        return self.email


def generer_pin_unique():
    """Génère un code PIN à 4 chiffres, unique parmi les enfants existants."""
    while True:
        pin = f"{random.randint(0, 9999):04d}"
        if not Enfant.objects.filter(pin_code=pin).exists():
            return pin


class Enfant(models.Model):
    prenom = models.CharField("prénom", max_length=100)
    nom = models.CharField("nom", max_length=100, blank=True)
    parent = models.ForeignKey(
        Parent, on_delete=models.CASCADE, related_name="enfants"
    )
    pin_code = models.CharField(
        "code PIN", max_length=4, unique=True, blank=True, editable=False
    )
    date_naissance = models.DateField("date de naissance", null=True, blank=True)
    avatar = models.ImageField(
        "avatar", upload_to="avatars/", null=True, blank=True
    )
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "enfant"
        verbose_name_plural = "enfants"
        ordering = ["prenom"]

    def __str__(self):
        return self.prenom

    def save(self, *args, **kwargs):
        if not self.pin_code:
            self.pin_code = generer_pin_unique()
        super().save(*args, **kwargs)

    @property
    def age(self):
        if not self.date_naissance:
            return None
        today = timezone.now().date()
        return (
            today.year
            - self.date_naissance.year
            - (
                (today.month, today.day)
                < (self.date_naissance.month, self.date_naissance.day)
            )
        )


class Dessin(models.Model):
    AJOUTE_PAR_CHOICES = [
        ("PARENT", "Parent"),
        ("ENFANT", "Enfant"),
    ]

    titre = models.CharField("titre", max_length=150)
    image = models.ImageField("image", upload_to="dessins/%Y/%m/")
    date_creation = models.DateField("date du dessin", default=timezone.localdate)
    description = models.TextField("description", blank=True)
    enfant = models.ForeignKey(
        Enfant, on_delete=models.CASCADE, related_name="dessins"
    )
    ajoute_par = models.CharField(
        "ajouté par", max_length=10, choices=AJOUTE_PAR_CHOICES, default="PARENT"
    )
    note = models.PositiveSmallIntegerField(
        "note",
        null=True,
        blank=True,
        validators=[MinValueValidator(1), MaxValueValidator(5)],
    )
    aime = models.BooleanField("coup de cœur", default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "dessin"
        verbose_name_plural = "dessins"
        ordering = ["-date_creation", "-created_at"]

    def __str__(self):
        return f"{self.titre} ({self.enfant.prenom})"


class EnfantToken(models.Model):
    """Jeton d'authentification pour l'espace enfant côté API (mobile)."""

    enfant = models.OneToOneField(
        Enfant, on_delete=models.CASCADE, related_name="token"
    )
    key = models.CharField(max_length=40, unique=True, editable=False)
    created = models.DateTimeField(auto_now_add=True)

    def save(self, *args, **kwargs):
        if not self.key:
            self.key = secrets.token_hex(20)
        super().save(*args, **kwargs)

    def __str__(self):
        return f"Token de {self.enfant.prenom}"
