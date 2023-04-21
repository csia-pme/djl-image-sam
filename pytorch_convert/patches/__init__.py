import contextlib
import os
import importlib


def apply_patches():
    module_names = [
        "patches." + fn.replace(".py", "")
        for fn in os.listdir(os.path.dirname(__file__))
        if fn.endswith(".py") and fn != "__init__.py"
    ]

    patches = tuple()
    for module_name in module_names:
        module = importlib.import_module(module_name)
        with contextlib.suppress(AttributeError):
            patches += module.patches

    for patch in patches:
        patch.__enter__()
