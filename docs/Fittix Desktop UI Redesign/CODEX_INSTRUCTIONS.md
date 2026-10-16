# Fittix UI Redesign — Implementation Brief for Codex

Reference design: `Fittix Redesign.dc.html` (open in a browser; sections 01–05).
Target: the existing JavaFX app (AB3-style: `MainWindow`, `CommandBox`, `ResultDisplay`, `PersonListPanel`, `PersonCard`, `StatusBarFooter`).

## Ground rules
- **Do not change the model, storage, parser, or command logic.** UI layer only (FXML, CSS, controller/view classes).
- Keep every existing command and its exact syntax. The command words in the mockup are illustrative.
- All derived values (day grouping, "next session", "today", macro %) are computed in the UI from existing fields.
- Keep index numbers visible for clients (list order) and exercises (plan order) — commands depend on them.
- No new features (accounts, sync, drag-and-drop, analytics, etc.).

## 1. Fonts
Bundle in `src/main/resources/fonts/`: Manrope (400/500/600/700/800) and JetBrains Mono (500/700), both from Google Fonts (OFL).
Load at startup before showing the stage:
```java
Font.loadFont(getClass().getResourceAsStream("/fonts/Manrope-Variable.ttf"), 14);
Font.loadFont(getClass().getResourceAsStream("/fonts/JetBrainsMono-Medium.ttf"), 14);
```
Use mono for anything a user types back: indexes, commands, phone numbers, sets/reps/weights, dates/times.

## 2. Design tokens
| Token | Hex | Use |
|---|---|---|
| canvas | #121213 | window background |
| surface | #1c1c1e | cards |
| raised | #242427 | exercise tiles, notes, secondary buttons |
| line | #2a2a2d | borders, dividers (dashed between rows) |
| ink | #f4f4f2 | primary text |
| muted | #a3a3a8 | secondary text |
| subtle | #85858b | labels (uppercase 10.5px/800, letter-spacing ~1px) |
| lime | #d4f25a | primary action, selection, success, "today" (text on it: #141414) |
| violet | #5d4fd8 fill / #a99ffb text | scheduled sessions, find-filter, help/info |
| amber | #f0a54b | diet |
| coral | #ff6b5e (text #ff8a7f) | errors |
| yellow | #f3d34a | warnings |

Radii: 8 chips · 10 badges · 14 tiles · 22–24 cards · 28 window · 999 pills.
Spacing (4px base): 4, 8, 12, 16 (column gap), 24 (window padding).
Type: Display 26/700 · Metric 34/700 · Title 17/700 · Body 13.5–14/500 · Label 10.5/800 caps · Mono 15/500 (input) and 12.5/500 (data).
No gradients, no drop shadows — elevation is surface steps only.

## 3. Layout (MainWindow.fxml)
```
BorderPane .window                      (min 1280x800, pref 1440x980)
├─ top     HBox .app-header             spacing 14, padding 0 24, height 72, alignment CENTER_LEFT
│   ├─ HBox brand (lime 30x30 rounded "F" + Label "Fittix" 20/700)
│   ├─ MenuBar .menu-pills  (File, Help — keep existing menu items)
│   ├─ Region  HBox.hgrow=ALWAYS
│   ├─ Label .filter-pill   "All clients  6 of 6"  |  "find: alex  1 of 6"
│   └─ Label .date-pill     today's date
├─ center  GridPane .body               hgap 16, padding 0 24 14 24
│   │  ColumnConstraints: 300 fixed | hgrow ALWAYS | 340 fixed
│   ├─ col 0: VBox .card  ClientListPanel
│   │     header HBox (Label "Clients", count badge, Label "list · find")
│   │     ListView<Person> (VBox.vgrow ALWAYS) → ClientCard cell
│   ├─ col 1: VBox spacing 14  ClientDetailPanel
│   │     ProfileCard   (VBox .card)
│   │     WorkoutPlanCard (VBox .card, VBox.vgrow ALWAYS) → ScrollPane → VBox of DayRow x7
│   └─ col 2: ScrollPane (fitToWidth) → VBox spacing 14
│         DietCard · SessionsCard · HistoryCard
└─ bottom  VBox .command-zone           spacing 10, padding 0 24 12 24
    ├─ ListView<CommandResult> .result-list   prefHeight 118
    ├─ HBox .command-bar  height 58, padding 0 7 0 22, spacing 12
    │     Label "›" · TextField #commandTextField (hgrow) · Label "↑ ↓ history" · Button .btn-primary "Run ↵"
    ├─ FlowPane .hint-row  hgap 8  (usage hints)
    └─ HBox .status-bar   (existing StatusBarFooter: data file path left, "Saved · N clients" right)
```

## 4. Components

**ClientCard (ListCell)** — GridPane: 32x32 index badge | VBox(name 14.5/700, phone mono 12 muted, FlowPane of first 2 tags, meta line 11.5 subtle: "10 exercises · Next Mon 19 Oct" or "No plan · No session booked").
Selected: cell background #26262a, border #34343a, radius 16, **badge turns lime with #141414 text**. Hover: border #3a3a3e.

**ProfileCard** — Row 1: lime index badge (40px, mono 18/700) · name (Display) · tag pills · spacer · mono hint chip "edit N … · delete N".
Row 2 (dashed top border): 4 columns separated by 1px line: PHONE (mono) · EMAIL · ADDRESS (wraps) · GOAL (label in lime, value 600).

**WorkoutPlanCard** — Header: "Workout plan" + summary "10 exercises · 4 training days" + mono chips `addex editex delex clearplan`.
Body: 7 DayRows (MON→SUN), each an HBox: left VBox width 104 (day code mono 13/700 + "Monday · 3"; today's day in lime + "TODAY" lime pill) | right TilePane/GridPane with 3 columns of ExerciseTile. Rest days show "Rest — no exercises assigned" in #6f6f76. Rows separated by dashed top border.
**Exercise index = position in the plan list (1-based), displayed sorted by weekday.** If the existing model doesn't keep the plan sorted by day, show the model's real index — never renumber in the UI only.
ExerciseTile: VBox, bg raised, radius 14, padding 10 12. Top: index chip (#34343a, mono 11.5/700) + name 13.5/700 (ellipsis). Bottom: "3 × 10" muted mono left, "60 kg" (or "BW" for 0) ink mono right. Today's tiles border #3d4420.
Empty: dashed box with "No workout plan yet", one-line explanation, and lime mono example `addex N n/… s/… r/… w/… d/MON`.

**DietCard** — Header: amber 10px rounded square + "DIET" amber label + "Targets & notes" + mono hint `diet N`.
Body: kcal (Metric) + "kcal / day"; 3 macro columns (Protein / Carbs / Fat) each with value and a 6px amber ProgressBar = grams×kcal-per-gram ÷ total kcal; notes in raised box radius 14. Show only fields the model stores.
Empty: dashed box "No diet targets recorded." + amber mono example.

**SessionsCard** — Header: violet dot + "SCHEDULE" (#a99ffb) + "Sessions". Rows (GridPane 48 | grow | auto, radius 16): date block (DOW 10/800, day 20/700, MON 10/700) · note + status ("Next session" / "Upcoming" / "Past") · time mono. **First upcoming session filled #5d4fd8 with white text**; others raised; past sessions opacity 0.55. Sort by date+time.
Empty: dashed box + violet mono example.

**HistoryCard** — Header: lime ring + "HISTORY" (muted) + "Completed workouts". Entries newest first, dashed separators: 22px lime circle with "✓" | date (700) + time (mono muted) on one line, then one mono 12 line per exercise.
Empty: dashed box + lime mono example.

**Empty app state** (no clients): center card "No client selected" + explanation + lime `add …` example; right column shows a dashed hint box. If `find` matches nothing, list shows "No clients match" + `list`.

## 5. Command zone behaviour
- **ResultDisplay → ListView<CommandResult>**, where `record CommandResult(String command, Kind kind, String message, String usage)` and `Kind { SUCCESS, ERROR, INFO, WARNING }`. Append on every execution; `scrollTo(size-1)`; cap at ~50 items. Newest at the bottom.
  Cell: 20px status circle (lime ✓ / coral ! / violet ? / yellow –) | VBox( "› command" mono 12 subtle, message 13.5/600 (coral text for errors), usage line mono 12 muted if present ).
  `help` → INFO with the existing MESSAGE_USAGE strings as detail lines. Help menu does the same plus opens the existing help window.
- **Command bar states** (PseudoClass on the HBox):
  - default: border line
  - `:focused-field` (bind to `textField.focusedProperty()`): border lime
  - `:error`: border coral, "›" and text coral. Keep the failed command text in the field (existing AB3 behaviour); clear the state on next keystroke.
- **History:** Up/Down in the TextField cycles previously entered commands.
- **Hint row:** if the field is empty, show clickable example commands; otherwise show `MESSAGE_USAGE` of commands whose word starts with the typed first word (max 4), or "No matching command". Clicking a chip fills the field with the command word + space and focuses it.
- **Filter pill:** "All clients" (lime dot) after `list`; "find: KEYWORDS" (violet dot) after `find`, plus "shown of total". Bind to the filtered list's size.
- After any command that targets a client index, select that client in the ListView.
- Focus the command TextField on launch.

## 6. CSS (FittixTheme.css — replace DarkTheme.css)
```css
.root {
  canvas:#121213; surface:#1c1c1e; raised:#242427; line:#2a2a2d;
  ink:#f4f4f2; muted:#a3a3a8; subtle:#85858b;
  lime:#d4f25a; violet:#5d4fd8; violet-text:#a99ffb; amber:#f0a54b; coral:#ff6b5e;
  -fx-font-family:"Manrope"; -fx-font-size:14px; -fx-background-color:canvas;
}
.label { -fx-text-fill:ink; }
.mono  { -fx-font-family:"JetBrains Mono"; }
.muted { -fx-text-fill:muted; }
.caps-label { -fx-font-size:10.5px; -fx-font-weight:800; -fx-text-fill:subtle; }

.card { -fx-background-color:surface; -fx-background-radius:22; -fx-border-color:line; -fx-border-radius:22; -fx-padding:18 22; }
.tile { -fx-background-color:raised; -fx-background-radius:14; -fx-border-color:#2e2e32; -fx-border-radius:14; -fx-padding:10 12; }
.tile.today { -fx-border-color:#3d4420; }
.empty-box { -fx-border-color:#2f2f33; -fx-border-style:segments(6,4); -fx-border-width:1.5; -fx-border-radius:14; -fx-padding:14; }
.day-row { -fx-border-color:#2c2c30 transparent transparent transparent; -fx-border-style:segments(4,4); -fx-padding:10 0; }

.pill, .filter-pill, .date-pill { -fx-background-color:surface; -fx-background-radius:999; -fx-border-color:#26262a; -fx-border-radius:999; -fx-padding:8 16; }
.tag { -fx-background-color:#26262a; -fx-background-radius:999; -fx-padding:2 8; -fx-font-size:11px; -fx-font-weight:600; }

.index-badge { -fx-font-family:"JetBrains Mono"; -fx-font-weight:bold; -fx-background-color:#2a2a2d; -fx-background-radius:10; -fx-alignment:center; -fx-min-width:32; -fx-min-height:32; }
.index-badge.selected, .profile-index { -fx-background-color:lime; -fx-text-fill:#141414; }

.list-view { -fx-background-color:transparent; -fx-padding:4 10 12 10; }
.list-cell { -fx-background-color:transparent; -fx-padding:2 0; }
.client-cell { -fx-background-radius:16; -fx-border-radius:16; -fx-border-color:transparent; -fx-padding:12 10; }
.client-cell:hover { -fx-border-color:#3a3a3e; }
.list-cell:selected .client-cell { -fx-background-color:#26262a; -fx-border-color:#34343a; }
.list-cell:selected .index-badge { -fx-background-color:lime; -fx-text-fill:#141414; }

.session-row { -fx-background-color:raised; -fx-background-radius:16; -fx-padding:10 12; }
.session-row.next { -fx-background-color:violet; }
.session-row.past { -fx-opacity:0.55; }
.macro-bar .track { -fx-background-color:#2a2a2d; -fx-background-radius:3; }
.macro-bar .bar   { -fx-background-color:amber; -fx-background-radius:3; -fx-background-insets:0; }

.result-list { -fx-background-color:#161618; -fx-background-radius:18; -fx-border-color:#26262a; -fx-border-radius:18; }
.result-icon.success { -fx-background-color:lime; }
.result-icon.error   { -fx-background-color:coral; }
.result-icon.info    { -fx-background-color:violet-text; }
.result-icon.warning { -fx-background-color:#f3d34a; }
.result-message.error { -fx-text-fill:#ff8a7f; }

.command-bar { -fx-background-color:surface; -fx-background-radius:999; -fx-border-color:line; -fx-border-radius:999; -fx-border-width:1.5; }
.command-bar:focused-field { -fx-border-color:lime; }
.command-bar:error { -fx-border-color:coral; }
.command-bar .text-field { -fx-background-color:transparent; -fx-text-fill:ink; -fx-font-family:"JetBrains Mono"; -fx-font-size:15px; -fx-prompt-text-fill:#6f6f76; }
.command-bar:error .text-field, .command-bar:error .prompt { -fx-text-fill:#ff8a7f; }

.btn-primary { -fx-background-color:lime; -fx-text-fill:#141414; -fx-background-radius:999; -fx-font-weight:bold; -fx-pref-height:44; -fx-padding:0 20; }
.btn-primary:hover { -fx-background-color:#e3ff7a; }
.btn-secondary { -fx-background-color:raised; -fx-text-fill:ink; -fx-background-radius:999; -fx-border-color:line; -fx-border-radius:999; }
.hint-chip { -fx-font-family:"JetBrains Mono"; -fx-font-size:11.5px; -fx-text-fill:#c4c4c8; -fx-background-color:surface; -fx-border-color:line; -fx-background-radius:999; -fx-border-radius:999; -fx-padding:4 10; -fx-cursor:hand; }
.hint-chip:hover { -fx-border-color:lime; -fx-text-fill:ink; }

.scroll-pane, .scroll-pane > .viewport { -fx-background-color:transparent; }
.scroll-bar { -fx-background-color:transparent; }
.scroll-bar .thumb { -fx-background-color:#333337; -fx-background-radius:8; }
.scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-padding:0; }
```
PseudoClass wiring:
```java
private static final PseudoClass FOCUSED = PseudoClass.getPseudoClass("focused-field");
private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");
commandTextField.focusedProperty().addListener((o, a, f) -> commandBar.pseudoClassStateChanged(FOCUSED, f));
commandTextField.textProperty().addListener((o, a, b) -> commandBar.pseudoClassStateChanged(ERROR, false));
// in the existing CommandException catch: commandBar.pseudoClassStateChanged(ERROR, true);
```

## 7. Feature → UI checklist (all must work)
- add / edit / delete / list / clear / find → client list, profile card, filter pill, result log
- name, phone, email, address, goal, tags → profile card (+ name/phone/tags on list card)
- add/view sessions → SessionsCard (next one filled violet) + list card meta
- diet targets & notes → DietCard
- add exercise (name, sets, reps, weight, day) → tile in the right DayRow with its index
- edit exercise day → tile moves rows · remove exercise → tile removed · clear plan → empty state
- record completed workout (date, time, exercises) → HistoryCard
- success / error / help → result log + command bar state + hint row
- data file path → status bar

## 8. Acceptance
- Matches section 01 of the reference at 1440×980; reflows to 1280×800 (center column shrinks, side columns fixed, all panes scroll).
- Text contrast ≥ 4.5:1 (don't put muted text on raised for important values).
- Keyboard-only use: launch → type → Enter → read result, no mouse needed.
- Existing tests pass; UI tests updated for renamed/added nodes only.
