# Fittix UI v2 — Change Brief for Codex

**Read this together with `CODEX_INSTRUCTIONS.md` (the v1 brief).** Everything in v1 still applies **unless this file overrides it**.
Reference: `Fittix Redesign v2.dc.html`. Open it in a browser; `support.js` must be in the same folder. Section 01 is interactive.

## Ground rules (unchanged, with one exception)
- UI layer only: FXML, CSS, controller/view classes. Model, storage and other commands stay unchanged.
- **Exception:** the diet command gets the extra validation described in §9. Put it in the existing diet parser (`DietCommandParser` or equivalent). If the parser already rejects these inputs, keep its messages and skip that part.
- Keep the existing command syntax. The mockup's command words are illustrative.

---

## 1. Header: simplify
Remove:
- `MenuBar` with **File → Exit**. Closing the window is enough.
- The **"All clients · N of N" filter pill** in its normal state.
- The **date pill** background.

New header (`HBox .app-header`, height 64, padding `0 24`, spacing 16, CENTER_LEFT):
```
[dumbbell logo] Fittix   [Help]   [filter pill — only during find]   <spacer>   [date / clock]
```
- **Logo:** a lime (#d4f25a) dumbbell icon, 30×30, rotated −45°, with **no tile behind it**. Build it as an `SVGPath` or a `Group` of `Rectangle`s. Geometry (24×24 box, rotate −45° about the centre): handle 9.2×2.6 at (7.4,10.7); inner plates 3.4×10.8 at x=4.4 and x=16.2, y=6.6, arc 1; outer plates 2×6.8 at x=1.8 and x=20.2, y=8.6, arc 0.8. The same icon replaces the "F" anywhere else it appears (window icon, help window).
- **Help:** a single `Button .pill`. On click it runs the existing help command, which opens the terminal (§3) and prints the usage. It also opens the existing help window if the app has one.
- **Filter pill:** visible and managed **only while a `find` is active**. Violet dot, "find: KEYWORDS", "shown of total", "×". Clicking it runs `list`.
- **Date / clock** (`HBox .date-toggle`, radius 999, padding `7 14`):
  - Collapsed (default): `Sat, 17 Oct 2026` (pattern `EEE, d MMM yyyy`), subtle text #85858b, 13/500.
  - Hover: background surface #1c1c1e, text ink. Cursor: hand.
  - Click toggles expanded: `Saturday, 17 October 2026` (`EEEE, d MMMM yyyy`, ink 600) + **live time to the second** `HH:mm:ss` (24h, mono 600, lime, tabular figures).
  - Tick it with a `Timeline` (`KeyFrame` of 1 s, `INDEFINITE`) that runs **only while expanded**. Stop it when collapsed.
  - Tooltip: "Click for full date and live time" or "Click to collapse".

## 2. Main content: everything in the GUI, scrollable
Replace the v1 `center GridPane` and `bottom .command-zone` with:
```
BorderPane .window
├─ top     HBox .app-header                  (§1)
├─ center  SplitPane .workspace              orientation VERTICAL
│   ├─ ScrollPane .content                   fitToWidth=true, vbar AS_NEEDED, hbar NEVER
│   │   └─ GridPane .body                    hgap 16, padding 12 24 24 24
│   │        cols: 300 fixed | hgrow ALWAYS | 340 fixed     (no fixed row heights)
│   │        ├─ VBox .card ClientListPanel   (VBox of ClientCard — see note)
│   │        ├─ VBox spacing 16              ProfileCard, WorkoutPlanCard
│   │        └─ VBox spacing 16              DietCard, SessionsCard, HistoryCard
│   └─ VBox .terminal                        in SplitPane items ONLY while open (§3)
└─ bottom  VBox
    ├─ HBox .terminal-closed-bar             visible+managed ONLY while closed (§3)
    └─ HBox .status-bar
```
- Cards take their **natural height**. Nothing inside scrolls separately; the whole content area scrolls as one.
- When the terminal opens, the content area gets shorter but **cards keep their size**, and the ScrollPane scrolls. This is how the VS Code editor sits above its terminal.
- When the terminal is closed, the content fills the window. If the content is shorter than the window, let the plan card grow (`VBox.vgrow`) so the columns end at the same line.
- **ClientListPanel:** inside one outer ScrollPane, a `ListView` would add a second scrollbar. Use a `VBox` rebuilt from the filtered list with a `ListChangeListener` instead, keeping the selection logic. Alternatively, keep the `ListView` and set its pref height to item count × cell height. The point is that only one scrollbar appears.
- **Every record is visible in the GUI.** In particular, the training schedule must appear in SessionsCard and never only as terminal output.
- Spacing is the same everywhere: window margin 24, gap between cards and columns 16, card padding **20** on all sides (v1 used 18/22).

## 3. Terminal panel (replaces the v1 result list + command bar)
### Closed (default on launch)
`HBox .terminal-closed-bar`: height 44, padding `0 16 0 24`, background #161618, 1 px top border line, cursor TEXT.
- Left: lime "›" (mono 15/700) + "Start typing to run a command" (13/500 muted).
- Right: `Button .terminal-btn` labelled "Terminal ▲" (12/600, transparent, radius 8, hover background #2a2a2d). Click it to open.
- Clicking anywhere on the bar also opens the terminal.

### Opening
1. **Any printable key typed while no text field has focus** opens the terminal, focuses the TextField and inserts that character. Use a Scene-level `KEY_TYPED` event filter: skip it when the focus owner is a `TextInputControl` or when Ctrl/Meta/Alt is held; skip empty or control characters; consume the event and append the character with the caret at the end.
2. Clicking the closed bar or its Terminal button.
3. Help (button or command).
4. Any executed command (results always land in the open terminal, so errors show automatically).
- **No keyboard shortcut** for toggling. Ctrl+` was removed on purpose.

### Open
`VBox .terminal` (background #161618, 1 px top border line):
```
├─ HBox .terminal-header    height 44, padding 0 16 0 24, bottom border #222225
│   ├─ Label "TERMINAL"      11/800 caps, ink, 2 px lime bottom border
│   ├─ Region hgrow
│   ├─ Label "Esc to close"  mono 11.5, #6f6f76
│   └─ Button .icon-btn "×"  32×32, radius 10, transparent; hover #2a2a2d. Closes.
├─ ListView<CommandResult> .result-list   vgrow ALWAYS, padding 14 24, cell gap 14
├─ HBox .command-bar        (see below)
└─ FlowPane .hint-row       visible+managed ONLY while the field has text
```
- **Result cells:** larger than v1 so they are easy to read. Status circle 22 px; "› command" mono 13 subtle; message **15/600** (coral #ff8a7f for errors); detail and usage lines mono 13, line height 1.6, muted. Everything wraps (`wrapText`), with no ellipsis.
- The terminal **starts with an empty log**. Because it is closed until first use, an empty panel is never shown on launch.
- **Closing:** Esc (when the TextField or nothing has focus), or the × button. On close, remove the terminal VBox from the SplitPane items; the TextField loses focus.
- **Resizing:** the SplitPane divider is the drag handle. No text or grip graphic. Divider has a 4 px hit area; hover colour lime at about 35% opacity; cursor V_RESIZE. Clamp the terminal height to **170–640 px**. Save the last height in `GuiSettings` (or `UserPrefs`) and restore it each time the terminal opens. Default height: 320.

### Command bar: minimal (no pill, no Run button)
```
HBox .command-bar   height 44, spacing 12, no background, 1 px TOP border only
  Label "›"  (mono 17/700, lime; coral on error)
  TextField #commandTextField  hgrow, transparent, mono 15/500
  Label "↵ run · ↑↓ history"   mono 11.5, #6f6f76
```
- Delete `.btn-primary` "Run ↵". Enter runs the command.
- Delete the `:focused-field` state; the bar no longer changes on focus.
- `:error`: top border, "›" and text turn coral; the failed text stays in the field; the state clears on the next keystroke (as in v1).
- **Hint row:** the v1 "Try …" example chips are **removed**. While typing, show the usage of matching commands (up to 4) as **plain mono text** (11.5, muted, hover lime, click to fill). Show nothing when the field is empty.

## 4. Text is never truncated
- Remove **every ellipsis** (`textOverrun`) used for user data. Set `wrapText=true` and bind `maxWidth` to the parent width (or use `minWidth(0)` plus `maxWidth(Double.MAX_VALUE)`).
- **ProfileCard:** the 4-column strip becomes a **2×2 GridPane** (two equal columns, hgap 24, vgap 16): PHONE | EMAIL, ADDRESS | GOAL. Values wrap, and long emails may break mid-word. To do that, use a `Text`/`TextFlow`, or insert zero-width spaces after `@` and `.` before displaying.
- Wrap these: client names (list and profile), exercise names in tiles, session notes, history lines, and terminal output.
- The profile header row wraps (FlowPane or HBox with wrapping): badge, name, tags.

## 5. No command hints in cards
Remove all of these v1 elements:
- ProfileCard hint chip "edit N … · delete N"
- WorkoutPlanCard header chips `addex editex delex clearplan`
- Card header hints `diet N`, `session N`, `log N`
- Client list header "list · find"
- Status bar right side "Saved · N clients" (it is now just "Saved")

## 6. Empty states: blank, not instructional
Every v1 dashed empty box, with its explanation and example command, is **removed**:
- An empty card **keeps its header** (dot, caps label, title) and shows a **blank `Region`**: min height 48 for Diet, Sessions and History; 160 for the Workout plan.
- The workout plan summary label is empty when there are no exercises (no "Empty").
- Rest days show just "Rest" (#6f6f76). v1 said "Rest — no exercises assigned".
- With no clients: the list card shows only its header; the centre column shows one blank card (min height 320); the right column is empty.
- `find` with no matches: the list card shows only its header plus the filter pill in the app header.
- Diet notes: if empty, **hide the notes box completely**. Never show "No notes."
- Remove the CSS class `.empty-box`.

## 7. Diet card: calorie split bar (replaces the three progress bars)
The v1 bars looked like progress toward a goal, but they weren't. Replace them with **one stacked bar** that shows each macro's share of that client's daily calorie target.
```
Label "SPLIT OF DAILY CALORIES"   caps 10.5/700, subtle
HBox .macro-split   height 8, spacing 2, background #2a2a2d, radius 4, clip to rounded rect
  Region protein (#f0a54b) · Region carbs (#f7d3a0) · Region fat (#a8692a)
GridPane 3 equal cols: [8×8 swatch + label 11.5 muted] / grams mono 14/600 / "NN% of kcal" 11 subtle
```
Calculation (UI layer, recomputed when the selected client changes):
```java
int pct(int grams, int kcalPerGram, int calTarget) {
    return grams == 0 ? 0 : (int) Math.round(grams * kcalPerGram * 100.0 / calTarget);
}
// protein 4, carbs 4, fat 9 kcal/g
// region pref width = min(pct,100)% of the bar width (bind to bar.widthProperty())
```
- A missing macro (0 or not set) → no region, grams shown as "—", share text empty.
- The parts don't have to add up to 100%. Any leftover stays as the grey track. That is deliberate: it shows where the gram targets don't cover the calorie target.
- Example (Alex, 2,100 kcal, 160/210/65 g) → 30% / 40% / 28%.

## 8. Design system deltas
- Card padding token: **20** (was 18–22).
- Spacing: window margin 24 on all sides; card and column gap 16.
- Fonts unchanged (Manrope + JetBrains Mono). A Space Grotesk + Space Mono variant exists as a separate mockup; ignore it unless told otherwise.
- CSS to **add**:
```css
.terminal { -fx-background-color:#161618; -fx-border-color:line transparent transparent transparent; }
.terminal-header { -fx-border-color:transparent transparent #222225 transparent; -fx-padding:0 16 0 24; }
.terminal-tab { -fx-font-size:11px; -fx-font-weight:800; -fx-border-color:transparent transparent lime transparent; -fx-border-width:0 0 2 0; -fx-padding:13 0 11 0; }
.workspace > .split-pane-divider { -fx-padding:0 0 4 0; -fx-background-color:line; }
.workspace > .split-pane-divider:hover { -fx-background-color:rgba(212,242,90,0.35); }
.terminal-closed-bar { -fx-background-color:#161618; -fx-border-color:line transparent transparent transparent; -fx-padding:0 16 0 24; -fx-cursor:text; }
.terminal-btn, .icon-btn { -fx-background-color:transparent; -fx-text-fill:muted; -fx-background-radius:8; }
.terminal-btn:hover, .icon-btn:hover { -fx-background-color:#2a2a2d; -fx-text-fill:ink; }
.date-toggle { -fx-background-radius:999; -fx-padding:7 14; -fx-cursor:hand; }
.date-toggle:hover { -fx-background-color:surface; }
.date-toggle .clock { -fx-font-family:"JetBrains Mono"; -fx-font-weight:600; -fx-text-fill:lime; }
.macro-split { -fx-background-color:#2a2a2d; -fx-background-radius:4; -fx-spacing:2; }
.macro-protein { -fx-background-color:#f0a54b; }
.macro-carbs   { -fx-background-color:#f7d3a0; }
.macro-fat     { -fx-background-color:#a8692a; }
```
- CSS to **replace**:
```css
.card { ... -fx-padding:20; }
.command-bar { -fx-background-color:transparent; -fx-border-color:line transparent transparent transparent; -fx-border-width:1 0 0 0; -fx-spacing:12; }
.command-bar .text-field { -fx-background-color:transparent; -fx-padding:0; -fx-text-fill:ink; -fx-font-family:"JetBrains Mono"; -fx-font-size:15px; }
.command-bar:error { -fx-border-color:coral transparent transparent transparent; }
.command-bar:error .text-field, .command-bar:error .prompt { -fx-text-fill:#ff8a7f; }
.hint-chip { -fx-font-family:"JetBrains Mono"; -fx-font-size:11.5px; -fx-text-fill:muted; -fx-background-color:transparent; -fx-cursor:hand; }
.hint-chip:hover { -fx-text-fill:lime; }
```
- CSS to **delete**: `.empty-box`, `.date-pill`, `.menu-pills`, `.command-bar:focused-field`, `.btn-primary` (if nothing else uses it), `.macro-bar .track/.bar`, `.result-list` rounded border (the terminal has no bubble).

## 9. Diet command validation (the only logic change)
Format: `diet INDEX cal/KCAL [pro/G] [carb/G] [fat/G] [n/NOTES]`. Prefixes can come in any order; a repeated prefix takes its last value; values are trimmed.

| Input | Expected |
|---|---|
| `diet 2 cal/2400 pro/170 carb/250 fat/70 n/Lean bulk` | ✓ all fields; bar ≈ 28/42/26 % |
| `diet 2 cal/2400` | ✓ kcal only; bar empty; macros "—"; no notes box |
| `diet 2 cal/2400 pro/170` | ✓ protein only filled |
| `diet 2 n/Low sugar pro/170 cal/2400` | ✓ order doesn't matter |
| `diet   2   cal/2400    pro/170` | ✓ extra whitespace OK |
| `diet 2 pro/170` | ✗ `Calorie target must be a whole number (cal/2000).` |
| `diet 2 cal/2400.5` / `cal/abc` / `cal/` / `cal/2 400` | ✗ same calorie message |
| `diet 2 cal/2000 pro/abc` (or carb/fat, decimals) | ✗ `Macros must be whole numbers of grams.` |
| `diet 2 cal/2000 pro/` | ✓ empty macro = not set |
| `diet 99 cal/2000` | ✗ `The client index provided is invalid.` |
| `diet cal/2000` | ✗ `Invalid command format!` + usage |
| `diet 2 cal /2000` | ✗ `Invalid command format!` (a space before the slash is not a prefix) |

Order of checks: index format → index range → calorie → macros. Re-running `diet` replaces the targets; macros left out are cleared; notes are kept if `n/` is omitted.
Add parser unit tests for every row above.

## 10. Updated acceptance checklist
- [ ] Launch: terminal closed, content fills the window, header has no File menu and no count pill.
- [ ] Typing any letter with nothing focused opens the terminal with that letter in the field.
- [ ] Esc and × close it; the Terminal button and a click on the closed bar open it; there is no Ctrl+` shortcut.
- [ ] Dragging the divider resizes it (170–640 px); the height persists across open/close and app restarts.
- [ ] With the terminal open, the content scrolls; cards keep their size.
- [ ] A long email or address (try a 40+ character email) shows in full in the profile.
- [ ] Empty plan, diet, sessions and history show a header only with a blank body; no command text anywhere in cards.
- [ ] The date toggles to the full date plus a seconds clock; the timer stops when collapsed.
- [ ] The diet split bar matches the formula; the leftover stays grey; no notes box when notes are empty.
- [ ] All diet test cases in §9 pass.
- [ ] Keyboard-only flow still works: type → Enter → read result → Esc.
