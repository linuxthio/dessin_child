from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.shortcuts import get_object_or_404, redirect, render
from django.views.decorators.http import require_http_methods

from .forms import (
    DessinEnfantForm,
    DessinParentForm,
    EnfantForm,
    EnfantSelectPinForm,
    ParentLoginForm,
    ParentRegisterForm,
)
from .models import Dessin, Enfant

SESSION_ENFANT_KEY = "enfant_id"


# ---------------------------------------------------------------------------
# Aides d'accès (espace enfant, basé sur la session, pas sur auth Django)
# ---------------------------------------------------------------------------

def enfant_connecte(request):
    """Renvoie l'Enfant actuellement connecté via la session, ou None."""
    enfant_id = request.session.get(SESSION_ENFANT_KEY)
    if not enfant_id:
        return None
    return Enfant.objects.filter(pk=enfant_id).first()


def enfant_required(view_func):
    def wrapper(request, *args, **kwargs):
        enfant = enfant_connecte(request)
        if enfant is None:
            messages.info(request, "Connecte-toi d'abord avec ton PIN !")
            return redirect("gallery:login_enfant")
        request.enfant = enfant
        return view_func(request, *args, **kwargs)

    return wrapper


# ---------------------------------------------------------------------------
# Accueil
# ---------------------------------------------------------------------------

def accueil(request):
    return render(request, "gallery/accueil.html")


# ---------------------------------------------------------------------------
# Espace Parent
# ---------------------------------------------------------------------------

def register_parent(request):
    if request.user.is_authenticated:
        return redirect("gallery:dashboard_parent")
    if request.method == "POST":
        form = ParentRegisterForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            messages.success(request, "Bienvenue ! Votre compte a été créé.")
            return redirect("gallery:dashboard_parent")
    else:
        form = ParentRegisterForm()
    return render(request, "registration/register_parent.html", {"form": form})


def login_parent(request):
    if request.user.is_authenticated:
        return redirect("gallery:dashboard_parent")
    if request.method == "POST":
        form = ParentLoginForm(request.POST)
        if form.is_valid():
            user = authenticate(
                request,
                username=form.cleaned_data["email"],
                password=form.cleaned_data["password"],
            )
            if user is not None:
                login(request, user)
                return redirect("gallery:dashboard_parent")
            form.add_error(None, "Email ou mot de passe incorrect.")
    else:
        form = ParentLoginForm()
    return render(request, "registration/login_parent.html", {"form": form})


def logout_parent(request):
    logout(request)
    return redirect("gallery:accueil")


@login_required(login_url="gallery:login_parent")
def dashboard_parent(request):
    enfants = request.user.enfants.all()
    dessins = Dessin.objects.filter(enfant__parent=request.user).select_related(
        "enfant"
    )

    enfant_filtre = request.GET.get("enfant")
    if enfant_filtre:
        dessins = dessins.filter(enfant_id=enfant_filtre)

    return render(
        request,
        "gallery/dashboard_parent.html",
        {
            "enfants": enfants,
            "dessins": dessins,
            "enfant_filtre": int(enfant_filtre) if enfant_filtre else None,
        },
    )


@login_required(login_url="gallery:login_parent")
def enfant_creer(request):
    if request.method == "POST":
        form = EnfantForm(request.POST, request.FILES)
        if form.is_valid():
            enfant = form.save(commit=False)
            enfant.parent = request.user
            enfant.save()
            messages.success(
                request,
                f"{enfant.prenom} a été ajouté(e) ! Son code PIN est {enfant.pin_code}.",
            )
            return redirect("gallery:dashboard_parent")
    else:
        form = EnfantForm()
    return render(
        request, "gallery/enfant_form.html", {"form": form, "mode": "creer"}
    )


@login_required(login_url="gallery:login_parent")
def enfant_modifier(request, pk):
    enfant = get_object_or_404(Enfant, pk=pk, parent=request.user)
    if request.method == "POST":
        form = EnfantForm(request.POST, request.FILES, instance=enfant)
        if form.is_valid():
            form.save()
            messages.success(request, f"Profil de {enfant.prenom} mis à jour.")
            return redirect("gallery:dashboard_parent")
    else:
        form = EnfantForm(instance=enfant)
    return render(
        request,
        "gallery/enfant_form.html",
        {"form": form, "mode": "modifier", "enfant": enfant},
    )


@login_required(login_url="gallery:login_parent")
@require_http_methods(["POST"])
def enfant_supprimer(request, pk):
    enfant = get_object_or_404(Enfant, pk=pk, parent=request.user)
    prenom = enfant.prenom
    enfant.delete()
    messages.success(request, f"Le profil de {prenom} a été supprimé.")
    return redirect("gallery:dashboard_parent")


@login_required(login_url="gallery:login_parent")
def dessin_upload_parent(request):
    if request.method == "POST":
        form = DessinParentForm(request.POST, request.FILES, parent=request.user)
        if form.is_valid():
            dessin = form.save(commit=False)
            dessin.ajoute_par = "PARENT"
            dessin.save()
            messages.success(request, "Le dessin a été ajouté à la galerie.")
            return redirect("gallery:dashboard_parent")
    else:
        form = DessinParentForm(parent=request.user)
    return render(request, "gallery/dessin_form_parent.html", {"form": form})


@login_required(login_url="gallery:login_parent")
@require_http_methods(["POST"])
def dessin_supprimer(request, pk):
    dessin = get_object_or_404(Dessin, pk=pk, enfant__parent=request.user)
    dessin.delete()
    messages.success(request, "Le dessin a été supprimé.")
    return redirect("gallery:dashboard_parent")


# ---------------------------------------------------------------------------
# Espace Enfant
# ---------------------------------------------------------------------------

def login_enfant_select(request):
    """Étape 1 : l'enfant choisit son prénom / avatar."""
    if request.method == "POST":
        enfant_id = request.POST.get("enfant_id")
        enfant = get_object_or_404(Enfant, pk=enfant_id)
        form = EnfantSelectPinForm(initial={"enfant_id": enfant.pk})
        return render(
            request, "gallery/login_enfant_pin.html", {"form": form, "enfant": enfant}
        )

    enfants = Enfant.objects.all()
    return render(request, "gallery/login_enfant_select.html", {"enfants": enfants})


@require_http_methods(["POST"])
def login_enfant_pin(request):
    """Étape 2 : vérification du PIN."""
    form = EnfantSelectPinForm(request.POST)
    enfant = get_object_or_404(Enfant, pk=request.POST.get("enfant_id"))
    if form.is_valid() and form.cleaned_data["pin_code"] == enfant.pin_code:
        request.session[SESSION_ENFANT_KEY] = enfant.pk
        return redirect("gallery:dashboard_enfant")

    messages.error(request, "PIN incorrect, réessaie !")
    form = EnfantSelectPinForm(initial={"enfant_id": enfant.pk})
    return render(
        request, "gallery/login_enfant_pin.html", {"form": form, "enfant": enfant}
    )


def logout_enfant(request):
    request.session.pop(SESSION_ENFANT_KEY, None)
    return redirect("gallery:accueil")


@enfant_required
def dashboard_enfant(request):
    dessins = request.enfant.dessins.all()
    return render(
        request,
        "gallery/dashboard_enfant.html",
        {"enfant": request.enfant, "dessins": dessins},
    )


@enfant_required
def dessin_upload_enfant(request):
    if request.method == "POST":
        form = DessinEnfantForm(request.POST, request.FILES)
        if form.is_valid():
            dessin = form.save(commit=False)
            dessin.enfant = request.enfant
            dessin.ajoute_par = "ENFANT"
            dessin.save()
            messages.success(request, "Bravo, ton dessin a été ajouté !")
            return redirect("gallery:dashboard_enfant")
    else:
        form = DessinEnfantForm()
    return render(
        request,
        "gallery/dessin_form_enfant.html",
        {"form": form, "enfant": request.enfant},
    )
