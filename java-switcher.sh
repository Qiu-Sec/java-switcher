#!/bin/bash
set -e
unset _JAVA_OPTIONS

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# auto-compile if needed
if [ ! -f "$SCRIPT_DIR/JavaSwitcher.class" ] || \
   [ "$SCRIPT_DIR/JavaSwitcher.java" -nt "$SCRIPT_DIR/JavaSwitcher.class" ]; then
    echo "==> Compiling..."
    javac -d "$SCRIPT_DIR" "$SCRIPT_DIR/JavaSwitcher.java"
    echo "==> Compile done."
fi

# prefer higher JDK versions via sdkman
JDK_HOME=""
for ver in 24 23 22 21 17; do
    for d in "$HOME/.sdkman/candidates/java/${ver}."* 2>/dev/null; do
        if [ -x "$d/bin/java" ]; then
            JDK_HOME="$d"
            break 2
        fi
    done
done

if [ -n "$JDK_HOME" ]; then
    JAVA_BIN="$JDK_HOME/bin/java"
elif [ -d "$HOME/.sdkman/candidates/java/current" ]; then
    JAVA_BIN="$HOME/.sdkman/candidates/java/current/bin/java"
else
    JAVA_BIN="java"
fi

exec "$JAVA_BIN" -cp "$SCRIPT_DIR" JavaSwitcher
