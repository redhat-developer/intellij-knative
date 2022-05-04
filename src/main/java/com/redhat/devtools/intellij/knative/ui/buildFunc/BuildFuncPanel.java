/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.ui.buildFunc;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.kn.Function;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.ide.plugins.PluginManagerConfigurable.SEARCH_FIELD_BORDER_COLOR;
import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;

public class BuildFuncPanel extends ContentImpl {

    private ToolWindow toolWindow;
    private Map<String, TerminalExecutionConsole> funcXTerminal;
    private JPanel terminalPanel;
    private DefaultTreeModel treeModel;
    private Tree tree;

    public BuildFuncPanel(ToolWindow toolWindow) {
        super(null, "Build Output", true);
        this.toolWindow = toolWindow;
        this.funcXTerminal = new HashMap<>();
        setComponent(createMainPanel());
    }

    public BuildFuncExec createViewBuildFunc(Project project, Function function) {
        if (funcXTerminal.containsKey(function.getName())) {
            funcXTerminal.get(function.getName()).dispose();
            removeNode(function.getName());
        }
        final Icon[] nodeicon = {new AnimatedIcon.FS()};
        final String[] location = {"running ..."};
        BuildFuncExec buildFuncExec = new BuildFuncExec(function.getName());
        DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                new LabelAndIconDescriptor(project,
                        buildFuncExec,
                        () -> "Build " + function.getName() + ":",
                        () -> location[0],
                        () -> nodeicon[0],
                        null));
        treeModel.insertNodeInto(buildNode, (MutableTreeNode) treeModel.getRoot(), 0);

        TerminalExecutionConsole commonTerminalExecutionConsole = new TerminalExecutionConsole(project, null);
        ProcessListener processListener = new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                if (event.getExitCode() == 0) {
                    nodeicon[0] = AllIcons.RunConfigurations.TestPassed;
                    location[0] =  "successful" +
                            (function.getImage().isEmpty()
                                    ? ""
                                    : " <span style=\"color: gray;\">" + function.getImage() + "</span>");
                } else {
                    nodeicon[0] = AllIcons.General.BalloonError;
                    location[0] = "failed";
                }
                buildFuncExec.setEndTime();
            }
        };
        funcXTerminal.put(function.getName(), commonTerminalExecutionConsole);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(commonTerminalExecutionConsole.getComponent(), BorderLayout.CENTER);
        updateTerminalPanel(panel);


        tree.invalidate();
        tree.expandPath(new TreePath(((DefaultMutableTreeNode)treeModel.getRoot()).getPath()));
        buildFuncExec.setProcessListener(processListener);
        buildFuncExec.setTerminalExecutionConsole(commonTerminalExecutionConsole);
        return buildFuncExec;
    }

    private void removeNode(String name) {
        int children = treeModel.getChildCount(treeModel.getRoot());
        for (int i = 0; i<children; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeModel.getChild(treeModel.getRoot(), i);
            String nodeName = ((BuildFuncExec)((LabelAndIconDescriptor)child.getUserObject()).getElement()).getFuncName();
            if (nodeName.equals(name)) {
                treeModel.removeNodeFromParent(child);
                break;
            }
        }
    }

    private JComponent createMainPanel() {
        OnePixelSplitter tabPanel = new OnePixelSplitter(false, 0.37F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
        tabPanel.setFirstComponent(buildBuildsTree());
        tabPanel.setSecondComponent(buildTerminalPanel());
        return tabPanel;
    }

    private JComponent buildTerminalPanel() {
        terminalPanel = new JPanel(new BorderLayout());
        fillTerminalPanelWithMessage();
        return terminalPanel;
    }

    private void fillTerminalPanelWithMessage() {
        JLabel infoMessage = new JLabel("Nothing to show");
        infoMessage.setEnabled(false);
        infoMessage.setHorizontalAlignment(JLabel.CENTER);
        updateTerminalPanel(infoMessage);
    }

    private void updateTerminalPanel(JComponent component) {
        terminalPanel.removeAll();
        terminalPanel.add(component, BorderLayout.CENTER);
        terminalPanel.revalidate();
        terminalPanel.repaint();
    }

    private void updateTerminalPanel(TerminalExecutionConsole console) {
        terminalPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(console.getComponent(), BorderLayout.CENTER);
        terminalPanel.add(console.getComponent(), BorderLayout.CENTER);
        terminalPanel.revalidate();
        terminalPanel.repaint();
    }

    private JComponent buildBuildsTree() {
        DefaultTreeModel treeModel = getTreeModel();
        tree = new Tree(treeModel);
        UIUtil.putClientProperty(tree, ANIMATION_IN_RENDERER_ALLOWED, true);
        tree.setCellRenderer(getTreeCellRenderer());
        tree.setVisible(true);
        tree.setRootVisible(false);
        return new JBScrollPane(tree);
    }

    private DefaultTreeModel getTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(root);
        return treeModel;
    }

    private TreeCellRenderer getTreeCellRenderer() {
        return (tree1, value, selected, expanded, leaf, row, hasFocus) -> {
            Object node = TreeUtil.getUserObject(value);

            if (node instanceof LabelAndIconDescriptor) {
                String funcName = ((BuildFuncExec)((LabelAndIconDescriptor<?>) node).getElement()).getFuncName();
                if (selected && funcXTerminal.containsKey(funcName)) {
                    updateTerminalPanel(funcXTerminal.get(funcName));
                }

                ((LabelAndIconDescriptor) node).update();
                return createLabel(((LabelAndIconDescriptor) node).getPresentation().getPresentableText(),
                        ((LabelAndIconDescriptor) node).getPresentation().getLocationString(),
                        ((LabelAndIconDescriptor) node).getIcon(),
                        (BuildFuncExec) ((LabelAndIconDescriptor<?>) node).getElement()
                );
            }
            return null;
        };
    }

    private JComponent createLabel(String name, String location, Icon icon, BuildFuncExec buildFuncExec) {
        JPanel buildInfo = new JPanel(new BorderLayout());
        String label = "<html><span style=\"font-weight: bold;\">" + name + "</span> ";
        if (location != null && !location.isEmpty() ) {
            label += location;
        }
        label += "</html>";
        buildInfo.add(new JLabel(label, icon, SwingConstants.LEFT), BorderLayout.CENTER);

        JLabel lblDuration = new JLabel(getDuration(buildFuncExec));
        lblDuration.setForeground(JBColor.GRAY);
        buildInfo.add(lblDuration, BorderLayout.EAST);
        return buildInfo;
    }

    public String getDuration(BuildFuncExec buildFuncExec) {
        long duration;
        if (buildFuncExec.getEndTime() != -1) {
            duration = buildFuncExec.getEndTime() - buildFuncExec.getStartTime();
        } else {
            duration = System.currentTimeMillis() - buildFuncExec.getStartTime();
        }

        String durationText = StringUtil.formatDurationApproximate(duration);
        int index = durationText.indexOf("s ");
        if (index != -1) {
            durationText = durationText.substring(0, index + 1);
        }
        return durationText;
    }
}
