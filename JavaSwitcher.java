import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class JavaSwitcher {
    private List<JavaVersion> versions;
    private JPanel listPanel;
    private ButtonGroup group;
    private JLabel statusLabel;

    private record JavaVersion(String name, String home, String version, boolean isCurrent) {}

    private static final String[] SCAN_DIRS = {
        System.getProperty("user.home") + "/.sdkman/candidates/java",
        "/usr/lib/jvm",
        "/usr/lib/jdk",
        "/usr/java",
        "/opt/java",
        "/opt/jdk"
    };

    public JavaSwitcher() {
        versions = scan();
        buildUI();
    }

    private List<JavaVersion> scan() {
        List<JavaVersion> list = new ArrayList<>();
        String currentHome = resolveCurrent();

        for (String dir : SCAN_DIRS) {
            File d = new File(dir);
            if (!d.isDirectory()) continue;
            File[] subs = d.listFiles(File::isDirectory);
            if (subs == null) continue;
            for (File sub : subs) {
                File binJava = new File(sub, "bin/java");
                if (!binJava.canExecute()) continue;
                String ver = readVersion(binJava);
                String name = sub.getName();
                boolean cur = sub.getAbsolutePath().equals(currentHome);
                list.add(new JavaVersion(name, sub.getAbsolutePath(), ver, cur));
            }
        }
        list.sort(Comparator.comparing(JavaVersion::isCurrent).reversed()
            .thenComparing(JavaVersion::name));
        return list;
    }

    private String resolveCurrent() {
        String home = System.getenv("JAVA_HOME");
        if (home != null && new File(home).isDirectory()) return new File(home).getAbsolutePath();

        File sdkmanCur = new File(System.getProperty("user.home"), ".sdkman/candidates/java/current");
        if (sdkmanCur.isDirectory()) return sdkmanCur.getAbsolutePath();

        String path = System.getenv("PATH");
        if (path != null) {
            for (String p : path.split(File.pathSeparator)) {
                File j = new File(p, "java");
                if (j.canExecute()) {
                    return j.getParentFile().getParentFile().getAbsolutePath();
                }
            }
        }
        return System.getProperty("java.home");
    }

    private String readVersion(File javaBin) {
        try {
            Process p = new ProcessBuilder(javaBin.getAbsolutePath(), "-version")
                .redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes());
            p.waitFor();
            return out.lines().findFirst().orElse("unknown");
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    private void buildUI() {
        JFrame f = new JFrame("Java 版本切换器");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLayout(new BorderLayout(10, 10));
        f.setMinimumSize(new Dimension(520, 300));

        JLabel title = new JLabel("选择 Java 版本");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(12, 16, 0, 16));
        f.add(title, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        group = new ButtonGroup();

        if (versions.isEmpty()) {
            listPanel.add(new JLabel("未扫描到 Java 版本"));
        } else {
            for (JavaVersion v : versions) {
                listPanel.add(createRow(v));
            }
        }

        JScrollPane sp = new JScrollPane(listPanel);
        sp.setBorder(null);
        f.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        bottom.add(statusLabel, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancel = new JButton("取消");
        cancel.addActionListener(e -> f.dispose());
        JButton apply = new JButton("应用");
        apply.addActionListener(e -> applySelection(f));
        btns.add(cancel);
        btns.add(apply);
        bottom.add(btns, BorderLayout.EAST);

        f.add(bottom, BorderLayout.SOUTH);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private JPanel createRow(JavaVersion v) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JRadioButton rb = new JRadioButton(v.name);
        rb.setSelected(v.isCurrent);
        rb.addActionListener(e -> updatePreview(v));
        group.add(rb);

        JLabel verLabel = new JLabel(v.version);
        verLabel.setForeground(Color.GRAY);
        verLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JLabel curTag = new JLabel(v.isCurrent ? "● 当前" : "");
        curTag.setForeground(new Color(46, 125, 50));
        curTag.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.add(verLabel);
        right.add(curTag);

        row.add(rb, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JavaVersion selected() {
        for (JavaVersion v : versions) {
            if (v.isCurrent) return v;
        }
        return versions.isEmpty() ? null : versions.get(0);
    }

    private void updatePreview(JavaVersion v) {
        statusLabel.setText("选中: " + v.home + "  (" + v.version + ")");
    }

    private void applySelection(JFrame frame) {
        JavaVersion sel = findSelected();
        if (sel == null) {
            JOptionPane.showMessageDialog(frame, "请选择一个版本", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sel.isCurrent) {
            statusLabel.setText("已是当前版本，无需切换");
            return;
        }

        try {
            int r = JOptionPane.showConfirmDialog(frame,
                "将 Java 切换为:\n" + sel.name + " (" + sel.version + ")\n路径: " + sel.home,
                "确认切换", JOptionPane.OK_CANCEL_OPTION);
            if (r != JOptionPane.OK_OPTION) return;

            applyToProfile(sel.home);
            linkSdkman(sel.home);

            statusLabel.setText("已切换，新终端生效");
            JOptionPane.showMessageDialog(frame, "切换完成！\n请重新打开终端以生效。", "完成", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "切换失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JavaVersion findSelected() {
        Enumeration<AbstractButton> en = group.getElements();
        int i = 0;
        while (en.hasMoreElements()) {
            if (en.nextElement().isSelected() && i < versions.size()) {
                return versions.get(i);
            }
            i++;
        }
        return null;
    }

    private void applyToProfile(String javaHome) throws Exception {
        File profile = new File(System.getProperty("user.home"), ".profile");
        String content = Files.readString(profile.toPath());
        String marker = "export JAVA_HOME=";
        String newLine = marker + "\"" + javaHome + "\"";
        if (content.contains(marker)) {
            content = content.replaceAll("(?m)^export JAVA_HOME=.*$", newLine);
        } else {
            if (content.contains("_JAVA_OPTIONS")) {
                content = content.replaceAll("(_JAVA_OPTIONS[^\n]*\n)", "$1" + newLine + "\n");
            } else {
                content += "\n" + newLine + "\n";
            }
        }
        Files.writeString(profile.toPath(), content);
    }

    private void linkSdkman(String javaHome) {
        File sdkman = new File(System.getProperty("user.home"), ".sdkman/bin/sdkman-init.sh");
        if (!sdkman.exists()) return;
        Path javaPath = Path.of(javaHome);
        Path candidates = Path.of(System.getProperty("user.home"), ".sdkman/candidates/java");
        if (javaPath.startsWith(candidates)) {
            try {
                Path current = candidates.resolve("current");
                Files.deleteIfExists(current);
                Files.createSymbolicLink(current, javaPath);
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(JavaSwitcher::new);
    }
}
