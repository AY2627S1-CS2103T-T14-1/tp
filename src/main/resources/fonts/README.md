# Bundled fonts

Fittix bundles static Manrope faces at weights 400, 500, 600, 700 and 800,
and static JetBrains Mono faces at weights 500, 600 and 700. The static faces
are instantiated from the variable fonts from the Google Fonts repository so
JavaFX can resolve every CSS font weight reliably.

The non-Regular/non-Bold faces retain distinct legacy family names (for
example, `Manrope SemiBold`). FittixTheme.css pairs those family names with
numeric `-fx-font-weight` declarations because JavaFX does not reliably group
all loaded static faces under one family on every platform.

Both families are distributed under the SIL Open Font License 1.1; their
license texts are included alongside the font files.
