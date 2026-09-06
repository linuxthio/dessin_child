from django.contrib import admin
from django.contrib.auth.admin import UserAdmin

from .models import Dessin, Enfant, Parent


@admin.register(Parent)
class ParentAdmin(UserAdmin):
    model = Parent
    list_display = ("email", "username", "is_staff")
    ordering = ("email",)


@admin.register(Enfant)
class EnfantAdmin(admin.ModelAdmin):
    list_display = ("prenom", "nom", "parent", "pin_code", "created_at")
    readonly_fields = ("pin_code",)
    search_fields = ("prenom", "nom", "parent__email")


@admin.register(Dessin)
class DessinAdmin(admin.ModelAdmin):
    list_display = ("titre", "enfant", "date_creation", "ajoute_par", "created_at")
    list_filter = ("ajoute_par", "date_creation")
    search_fields = ("titre", "enfant__prenom")
