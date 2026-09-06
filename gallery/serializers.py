from django.contrib.auth.password_validation import validate_password
from rest_framework import serializers

from .models import Dessin, Enfant, Parent


class ParentSerializer(serializers.ModelSerializer):
    class Meta:
        model = Parent
        fields = ["id", "username", "email"]


class ParentRegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, validators=[validate_password])

    class Meta:
        model = Parent
        fields = ["id", "username", "email", "password"]

    def validate_email(self, value):
        if Parent.objects.filter(email__iexact=value).exists():
            raise serializers.ValidationError("Cette adresse email est déjà utilisée.")
        return value

    def create(self, validated_data):
        return Parent.objects.create_user(
            username=validated_data["username"],
            email=validated_data["email"],
            password=validated_data["password"],
        )


class EnfantSerializer(serializers.ModelSerializer):
    age = serializers.IntegerField(read_only=True)

    class Meta:
        model = Enfant
        fields = [
            "id",
            "prenom",
            "nom",
            "date_naissance",
            "age",
            "avatar",
            "pin_code",
            "created_at",
        ]
        read_only_fields = ["pin_code", "created_at"]


class EnfantPublicSerializer(serializers.ModelSerializer):
    """Vue allégée sans PIN, utilisée pour l'écran de sélection enfant."""

    class Meta:
        model = Enfant
        fields = ["id", "prenom", "avatar"]


class DessinSerializer(serializers.ModelSerializer):
    """Utilisé côté parent : le champ `enfant` est fourni par le client."""

    enfant_prenom = serializers.CharField(source="enfant.prenom", read_only=True)

    class Meta:
        model = Dessin
        fields = [
            "id",
            "titre",
            "image",
            "date_creation",
            "description",
            "enfant",
            "enfant_prenom",
            "ajoute_par",
            "note",
            "aime",
            "created_at",
        ]
        read_only_fields = ["ajoute_par", "created_at"]

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        request = self.context.get("request")
        if request is not None and "enfant" in self.fields:
            self.fields["enfant"].queryset = Enfant.objects.filter(
                parent=request.user
            )


class DessinEnfantSerializer(serializers.ModelSerializer):
    """Utilisé côté enfant : l'enfant est déduit du jeton, pas du payload."""

    class Meta:
        model = Dessin
        fields = [
            "id",
            "titre",
            "image",
            "date_creation",
            "description",
            "ajoute_par",
            "note",
            "aime",
            "created_at",
        ]
        read_only_fields = ["ajoute_par", "created_at"]


class DessinPublicSerializer(serializers.ModelSerializer):
    """Vue publique en lecture seule, utilisée pour le top des dessins
    affiché sur l'écran d'accueil (avant toute connexion)."""

    enfant_prenom = serializers.CharField(source="enfant.prenom", read_only=True)

    class Meta:
        model = Dessin
        fields = [
            "id",
            "titre",
            "image",
            "date_creation",
            "enfant_prenom",
            "note",
            "aime",
            "created_at",
        ]
        read_only_fields = fields
