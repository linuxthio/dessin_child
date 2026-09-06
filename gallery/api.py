from django.contrib.auth import authenticate
from django.db.models import F
from django.shortcuts import get_object_or_404
from rest_framework import generics, status
from rest_framework.authentication import TokenAuthentication
from rest_framework.authtoken.models import Token
from rest_framework.exceptions import PermissionDenied
from rest_framework.parsers import FormParser, MultiPartParser
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .api_auth import EnfantTokenAuthentication, IsEnfantAuthenticated
from .models import Dessin, Enfant, EnfantToken, Parent
from .serializers import (
    DessinEnfantSerializer,
    DessinPublicSerializer,
    DessinSerializer,
    EnfantPublicSerializer,
    EnfantSerializer,
    ParentRegisterSerializer,
    ParentSerializer,
)


# ---------------------------------------------------------------------------
# Authentification Parent
# ---------------------------------------------------------------------------

class RegisterParentAPIView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = ParentRegisterSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        parent = serializer.save()
        token, _ = Token.objects.get_or_create(user=parent)
        return Response(
            {"token": token.key, "parent": ParentSerializer(parent).data},
            status=status.HTTP_201_CREATED,
        )


class LoginParentAPIView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        email = request.data.get("email", "")
        password = request.data.get("password", "")
        parent = authenticate(request, username=email, password=password)
        if parent is None:
            return Response(
                {"detail": "Email ou mot de passe incorrect."},
                status=status.HTTP_401_UNAUTHORIZED,
            )
        token, _ = Token.objects.get_or_create(user=parent)
        return Response({"token": token.key, "parent": ParentSerializer(parent).data})


class LogoutParentAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request):
        request.user.auth_token.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class MeAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        return Response(ParentSerializer(request.user).data)


# ---------------------------------------------------------------------------
# Enfants (gérés par le parent)
# ---------------------------------------------------------------------------

class EnfantListCreateAPIView(generics.ListCreateAPIView):
    serializer_class = EnfantSerializer
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return Enfant.objects.filter(parent=self.request.user)

    def perform_create(self, serializer):
        serializer.save(parent=self.request.user)


class EnfantDetailAPIView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = EnfantSerializer
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return Enfant.objects.filter(parent=self.request.user)


class EnfantPublicListAPIView(generics.ListAPIView):
    """Liste publique (prénom + avatar) pour l'écran « Qui es-tu ? »."""

    queryset = Enfant.objects.all()
    serializer_class = EnfantPublicSerializer
    permission_classes = [AllowAny]


# ---------------------------------------------------------------------------
# Authentification Enfant (PIN)
# ---------------------------------------------------------------------------

class EnfantLoginAPIView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        enfant_id = request.data.get("enfant_id")
        pin_code = str(request.data.get("pin_code", ""))
        enfant = get_object_or_404(Enfant, pk=enfant_id)

        if enfant.pin_code != pin_code:
            return Response(
                {"detail": "PIN incorrect."}, status=status.HTTP_401_UNAUTHORIZED
            )

        EnfantToken.objects.filter(enfant=enfant).delete()
        token = EnfantToken.objects.create(enfant=enfant)
        return Response(
            {
                "enfant_token": token.key,
                "enfant": EnfantSerializer(enfant).data,
            }
        )


class EnfantLogoutAPIView(APIView):
    authentication_classes = [EnfantTokenAuthentication]
    permission_classes = [IsEnfantAuthenticated]

    def post(self, request):
        request.auth.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class EnfantMeAPIView(APIView):
    authentication_classes = [EnfantTokenAuthentication]
    permission_classes = [IsEnfantAuthenticated]

    def get(self, request):
        return Response(EnfantSerializer(request.auth.enfant).data)


# ---------------------------------------------------------------------------
# Dessins côté Parent
# ---------------------------------------------------------------------------

class DessinListCreateAPIView(generics.ListCreateAPIView):
    serializer_class = DessinSerializer
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser]

    def get_queryset(self):
        qs = Dessin.objects.filter(enfant__parent=self.request.user).select_related(
            "enfant"
        )
        enfant_id = self.request.query_params.get("enfant")
        if enfant_id:
            qs = qs.filter(enfant_id=enfant_id)
        return qs

    def perform_create(self, serializer):
        enfant = serializer.validated_data["enfant"]
        if enfant.parent_id != self.request.user.id:
            raise PermissionDenied("Cet enfant ne vous appartient pas.")
        serializer.save(ajoute_par="PARENT")


class DessinDetailAPIView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = DessinSerializer
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        return Dessin.objects.filter(enfant__parent=self.request.user)


class DessinTopListAPIView(generics.ListAPIView):
    """Top 5 des dessins (les mieux notés, puis les coups de cœur, puis les
    plus récents), affiché sur l'écran d'accueil avant toute connexion."""

    serializer_class = DessinPublicSerializer
    permission_classes = [AllowAny]

    def get_queryset(self):
        return Dessin.objects.select_related("enfant").order_by(
            F("note").desc(nulls_last=True), "-aime", "-created_at"
        )[:5]


# ---------------------------------------------------------------------------
# Dessins côté Enfant
# ---------------------------------------------------------------------------

class EnfantDessinListCreateAPIView(generics.ListCreateAPIView):
    serializer_class = DessinEnfantSerializer
    authentication_classes = [EnfantTokenAuthentication]
    permission_classes = [IsEnfantAuthenticated]
    parser_classes = [MultiPartParser, FormParser]

    def get_queryset(self):
        return Dessin.objects.filter(enfant=self.request.auth.enfant)

    def perform_create(self, serializer):
        serializer.save(enfant=self.request.auth.enfant, ajoute_par="ENFANT")


class EnfantDessinDetailAPIView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = DessinEnfantSerializer
    authentication_classes = [EnfantTokenAuthentication]
    permission_classes = [IsEnfantAuthenticated]

    def get_queryset(self):
        return Dessin.objects.filter(enfant=self.request.auth.enfant)
