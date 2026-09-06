from django import forms
from django.contrib.auth.forms import UserCreationForm

from .models import Dessin, Enfant, Parent


class ParentRegisterForm(UserCreationForm):
    email = forms.EmailField(label="Adresse email", required=True)

    class Meta:
        model = Parent
        fields = ("username", "email", "password1", "password2")

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        for field in self.fields.values():
            field.widget.attrs["class"] = TAILWIND_INPUT

    def save(self, commit=True):
        user = super().save(commit=False)
        user.email = self.cleaned_data["email"]
        if commit:
            user.save()
        return user


TAILWIND_INPUT = (
    "w-full rounded-xl border border-gray-300 px-4 py-2.5 "
    "focus:outline-none focus:ring-2 focus:ring-indigo-400"
)


class ParentLoginForm(forms.Form):
    email = forms.EmailField(
        label="Adresse email", widget=forms.EmailInput(attrs={"class": TAILWIND_INPUT})
    )
    password = forms.CharField(
        label="Mot de passe",
        widget=forms.PasswordInput(attrs={"class": TAILWIND_INPUT}),
    )


class EnfantForm(forms.ModelForm):
    class Meta:
        model = Enfant
        fields = ["prenom", "nom", "date_naissance", "avatar"]
        widgets = {
            "prenom": forms.TextInput(attrs={"class": TAILWIND_INPUT}),
            "nom": forms.TextInput(attrs={"class": TAILWIND_INPUT}),
            "date_naissance": forms.DateInput(
                attrs={"class": TAILWIND_INPUT, "type": "date"}
            ),
            "avatar": forms.ClearableFileInput(
                attrs={
                    "class": "block w-full text-sm text-gray-600 "
                    "file:mr-4 file:rounded-full file:border-0 "
                    "file:bg-indigo-100 file:px-4 file:py-2 file:text-indigo-700"
                }
            ),
        }


class DessinParentForm(forms.ModelForm):
    """Upload d'un dessin par un parent : sélection de l'enfant possible."""

    class Meta:
        model = Dessin
        fields = ["enfant", "titre", "image", "date_creation", "description"]
        widgets = {
            "enfant": forms.Select(attrs={"class": TAILWIND_INPUT}),
            "titre": forms.TextInput(attrs={"class": TAILWIND_INPUT}),
            "date_creation": forms.DateInput(
                attrs={"class": TAILWIND_INPUT, "type": "date"}
            ),
            "description": forms.Textarea(
                attrs={"class": TAILWIND_INPUT, "rows": 3}
            ),
            "image": forms.ClearableFileInput(
                attrs={
                    "class": "block w-full text-sm text-gray-600 "
                    "file:mr-4 file:rounded-full file:border-0 "
                    "file:bg-indigo-100 file:px-4 file:py-2 file:text-indigo-700"
                }
            ),
        }

    def __init__(self, *args, parent=None, **kwargs):
        super().__init__(*args, **kwargs)
        if parent is not None:
            self.fields["enfant"].queryset = Enfant.objects.filter(parent=parent)


class DessinEnfantForm(forms.ModelForm):
    """Formulaire simplifié pour l'upload par l'enfant : pas de champ enfant."""

    class Meta:
        model = Dessin
        fields = ["titre", "image"]
        widgets = {
            "titre": forms.TextInput(
                attrs={
                    "class": "w-full rounded-2xl border-4 border-yellow-300 "
                    "px-5 py-4 text-xl text-center focus:outline-none "
                    "focus:ring-4 focus:ring-yellow-200",
                    "placeholder": "Donne un titre à ton dessin !",
                }
            ),
            "image": forms.ClearableFileInput(
                attrs={
                    "class": "block w-full text-lg",
                    "capture": "environment",
                }
            ),
        }


class EnfantSelectPinForm(forms.Form):
    """Saisie du PIN une fois l'enfant sélectionné (via son id caché)."""

    enfant_id = forms.IntegerField(widget=forms.HiddenInput)
    pin_code = forms.CharField(
        label="Code PIN",
        max_length=4,
        min_length=4,
        widget=forms.PasswordInput(
            attrs={
                "class": "w-full rounded-2xl border-4 border-indigo-300 "
                "px-5 py-4 text-3xl tracking-[1em] text-center "
                "focus:outline-none focus:ring-4 focus:ring-indigo-200",
                "inputmode": "numeric",
                "maxlength": "4",
                "autocomplete": "off",
            }
        ),
    )
