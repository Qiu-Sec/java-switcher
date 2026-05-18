#!/bin/bash
set -e
unset _JAVA_OPTIONS
shopt -s nullglob

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

JDK_HOME=""
for ver in 24 23 22 21 17; do
    for d in "$HOME/.sdkman/candidates/java/${ver}."*; do
        if [ -x "$d/bin/javac" ] && [ -x "$d/bin/java" ]; then
            JDK_HOME="$d"
            break 2
        fi
    done
done

if [ -z "$JDK_HOME" ] && [ -x "$HOME/.sdkman/candidates/java/current/bin/javac" ]; then
    JDK_HOME="$HOME/.sdkman/candidates/java/current"
fi

if [ -z "$JDK_HOME" ]; then
    JAVAC_PATH=$(command -v javac 2>/dev/null) || true
    if [ -n "$JAVAC_PATH" ]; then
        JDK_HOME=$(dirname "$(dirname "$JAVAC_PATH")")
    fi
fi

if [ -z "$JDK_HOME" ] || [ ! -x "$JDK_HOME/bin/javac" ]; then
    echo "错误: 未找到 JDK 17+，请先安装。" >&2
    exit 1
fi

JAVAC="$JDK_HOME/bin/javac"
JAVA="$JDK_HOME/bin/java"

if [ ! -f "$SCRIPT_DIR/JavaSwitcher.class" ] || \
   [ "$SCRIPT_DIR/JavaSwitcher.java" -nt "$SCRIPT_DIR/JavaSwitcher.class" ]; then
    echo "==> Compiling..."
    "$JAVAC" -d "$SCRIPT_DIR" "$SCRIPT_DIR/JavaSwitcher.java"
    echo "==> Done."
fi

exec "$JAVA" -cp "$SCRIPT_DIR" JavaSwitcher
