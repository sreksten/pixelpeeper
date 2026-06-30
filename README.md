# PixelPeeper

A desktop application for photographers to compare multiple images side-by-side with full EXIF metadata display, image filtering, and annotation support.

---

## Table of Contents

- [Features](#features)
- [Building and Running](#building-and-running)
- [Loading Images](#loading-images)
- [Image Display](#image-display)
- [EXIF Tags](#exif-tags)
- [Filters](#filters)
- [Doodling / Annotations](#doodling--annotations)
- [Grid Overlay](#grid-overlay)
- [Cursor / Big Pointer](#cursor--big-pointer)
- [Preferences](#preferences)
- [Keyboard Shortcuts](#keyboard-shortcuts)

---

## Features

- Side-by-side comparison of multiple images in vertical, horizontal, or grid layout
- Zoom from 10% to 800% (13 steps)
- Display of 28+ EXIF tags per image, with configurable per-tag visibility
- Edge detection and retro palette filters (Canny, Sobel, ZX Spectrum, C64, Windows 3.11)
- Free-hand annotation (doodles) with colour, size, and transparency control; saved as JSON sidecar files
- Configurable grid overlay
- Large custom cursor for precise pixel inspection
- Automatic image rotation from EXIF orientation
- Crop-factor and focal-length normalisation for equivalent-zoom comparisons
- Image grouping by any EXIF tag for structured comparisons
- Drag-and-drop image loading
- Session persistence (last loaded files, zoom, position, etc.)

---

## Building and Running

**Requirements:** Java 8+, Apache Maven 3.x

```bash
# Build a fat JAR with all dependencies
mvn clean package

# Run
java -jar target/pixelpeeper-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

Main class: `com.threeamigos.pixelpeeper.Main`

**Key dependencies:**

| Library | Version | Purpose |
|---------|---------|---------|
| Gson | 2.10.1 | JSON preference & doodle persistence |
| metadata-extractor (Drew Noakes) | 2.18.0 | EXIF reading |
| Apache Commons Imaging | 1.0-alpha3 | Image decoding |
| three-amigos common-utils | 1.0.8 | Internal utilities |

---

## Loading Images

| Method | How |
|--------|-----|
| Open files | Menu → File → Open (`O`) |
| Browse directory | Menu → File → Browse (`B`) — recursively discovers images |
| Drag and drop | Menu → File → Drag & Drop (`D`) opens a floating drop target |

Images can be **grouped by any EXIF tag** (e.g. camera model, lens, focal length). Use the **Previous / Next** buttons in the controls panel to navigate between groups.

---

## Image Display

### Layout dispositions
- **Vertical** — slices stacked vertically
- **Horizontal** — slices side by side
- **Grid** — rectangular grid

Change via *Image Handling* menu.

### Zoom
- Mouse wheel anywhere on the canvas
- Zoom slider in the controls panel
- Levels: 10 %, 20 %, 30 %, 40 %, 50 %, 60 %, 70 %, 80 %, 90 %, 100 %, 200 %, 400 %, 800 %

### Panning
- Click and drag on any image slice
- By default all slices move together; press `I` to toggle per-slice movement
- Hold `Ctrl` while dragging to temporarily invert the "all vs. single" behaviour
- Press `M` to toggle between fixed-pixel and relative (percentage) movement

### Normalisation
- `C` — **Crop factor normalisation**: adjusts zoom so the field of view is equivalent regardless of sensor size
- `L` — **Focal length normalisation**: adjusts zoom relative to the lens focal length
- `R` — Toggle automatic EXIF orientation correction
- `S` — Toggle the position miniature (small overlay showing which part of the full image is visible)

---

## EXIF Tags

28+ tags are read and displayed on each image slice:

| Category | Tags |
|----------|------|
| Camera | Manufacturer, Model, Firmware |
| Lens | Manufacturer, Model, Maximum Aperture, Firmware |
| Image | Orientation, Width, Height, Date/Time |
| Exposure | Focal Length, F-stop, ISO, Exposure Time, Exposure Program, Exposure Mode |
| Metering | Mode, White Balance, White Balance Mode, Color Temperature |
| Advanced | Focus Mode, Flash, Color Space, Digital Zoom Ratio, Gain Control, Contrast, Saturation, Sharpness, HDR, Subject Distance |

### Tag visibility

Each tag has three modes, configurable from the *EXIF Tags* menu:

| Mode | Behaviour |
|------|-----------|
| **YES** | Always shown |
| **ONLY_IF_DIFFERENT** | Shown only when the value differs between loaded images |
| **NO** | Never shown |

- `T` — toggle all tags on/off
- `V` — override individual tag visibility (show all regardless of per-tag settings)

### Rendering style

Tags are rendered as text with an optional shadow border. Four shadow sizes are available: Line, Thin, Medium, Large (configurable in the *EXIF Tags* menu). Text is clipped to the image slice so it never bleeds into an adjacent slice.

---

## Filters

Access via the *Filters* menu. Toggle display with `F`; adjust transparency with the filter transparency slider.

| Filter | Description |
|--------|-------------|
| **Canny Edge Detector** | Classic gradient-based edge detection. Parameters: low/high threshold, Gaussian kernel radius & width, contrast normalisation |
| **Sobel Edge Detector** | Gradient magnitude edge detection |
| **ZX Spectrum Palette** | Maps image colours to the Sinclair ZX Spectrum 8-colour palette |
| **Commodore 64 Palette** | Maps image colours to the C64 16-colour palette |
| **Windows 3.11 Palette** | Maps image colours to the Win 3.11 system palette |

- `P` — open the parameter window for the current filter
- Filter transparency (0–100 %) blends the filtered result over the original image
- Calculations run in a background thread and are aborted automatically when zoom or images change

---

## Doodling / Annotations

Hold **Shift** and drag on an image slice to draw free-hand annotations.

| Control | Description |
|---------|-------------|
| Shift + drag | Draw a stroke |
| `U` | Undo the last stroke |
| `Delete` | Clear all strokes on the active slice |
| Color buttons (controls panel) | Red, Yellow, Green, Orange |
| Brush size slider | 5–50 px (default 20) |
| Transparency slider | 0–100 % (default 50 %) |

**Persistence:** doodles are saved automatically as JSON sidecar files (same directory and base name as the image, with a `.json` extension) and reloaded the next time the image is opened.

---

## Grid Overlay

- `G` — toggle grid visibility
- `Numpad +` / `Numpad -` — increase / decrease grid spacing (range 25–200 px, step 25; default 50 px)

---

## Cursor / Big Pointer

A large custom cursor can be enabled for precise pixel-level inspection.

- `Numpad 5` — toggle big pointer on/off
- `Numpad 1/2/3/4/6/7/8/9` — rotate / change pointer orientation (eight directions)

Size is configurable in the *Cursor* menu (default 100 px).

---

## Preferences

All preferences are stored as JSON files in the application configuration directory and persist across sessions.

| Category | Notable settings |
|----------|-----------------|
| Window | Position, dimensions |
| Image handling | Disposition, zoom, autorotation, crop/focal normalisation, movement mode, position miniature, image reader library, EXIF reader library |
| Grid | Visibility, spacing |
| Cursor | Visibility, size, rotation |
| Filters | Active filter, show results, transparency, per-filter parameters |
| EXIF tags | Global visibility, override mode, per-tag visibility, border/shadow style |
| Doodles | Brush colour, size, transparency |
| Session | Last path, last files, grouping tag, ordering tag, current group index |

---

## Keyboard Shortcuts

### File

| Key | Action |
|-----|--------|
| `O` | Open image files |
| `B` | Browse directory |
| `D` | Toggle drag-and-drop window |
| `Q` | Quit |

### Image handling

| Key | Action |
|-----|--------|
| Mouse wheel | Zoom in / out |
| Drag | Pan image(s) |
| `R` | Toggle autorotation |
| `C` | Toggle crop-factor normalisation |
| `L` | Toggle focal-length normalisation |
| `I` | Toggle move-all vs. move-single |
| `M` | Toggle fixed vs. relative movement |
| `S` | Toggle position miniature |
| `Ctrl` (held) | Temporarily invert move-all/single |

### EXIF tags

| Key | Action |
|-----|--------|
| `T` | Toggle tag display |
| `V` | Toggle visibility override |

### Filters

| Key | Action |
|-----|--------|
| `F` | Toggle filter results |
| `P` | Open filter parameters |

### Grid

| Key | Action |
|-----|--------|
| `G` | Toggle grid |
| `Numpad +` | Increase grid spacing |
| `Numpad -` | Decrease grid spacing |

### Cursor

| Key | Action |
|-----|--------|
| `Numpad 5` | Toggle big pointer |
| `Numpad 1–4, 6–9` | Change big pointer direction |

### Doodles

| Key | Action |
|-----|--------|
| `Shift` + drag | Draw annotation stroke |
| `U` | Undo last stroke |
| `Delete` | Clear all strokes on active slice |

### Help

| Key | Action |
|-----|--------|
| `H` | Show hints |
| `A` | Show about |
