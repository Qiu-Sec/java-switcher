# Java Switcher

GUI tool to scan and switch between local Java versions on Linux.

## Usage

```bash
chmod +x java-switcher.sh
./java-switcher.sh
```

One-click: auto-compiles (if needed) and launches the GUI. Select a version and click "Apply".

## What it does

- Scans `/usr/lib/jvm`, sdkman candidates, and other common Java paths
- Shows all found versions with radio buttons
- Marks the current version in green
- Updates `JAVA_HOME` in `~/.profile` and sdkman `current` symlink

## Requirements

- Any JDK 17+ to run the GUI (auto-detected from sdkman or PATH)
- Linux with Swing/GUI environment
