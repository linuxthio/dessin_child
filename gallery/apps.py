from django.apps import AppConfig


class GalleryConfig(AppConfig):
    name = 'gallery'

    def ready(self):
        # Permet à Pillow de lire/valider les images HEIC/HEIF (photos par
        # défaut sur iPhone), qu'il ne sait pas décoder nativement.
        import pillow_heif

        pillow_heif.register_heif_opener()
