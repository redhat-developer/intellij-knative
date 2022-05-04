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
import com.intellij.execution.ui.ConsoleView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.intellij.ide.plugins.PluginManagerConfigurable.SEARCH_FIELD_BORDER_COLOR;
import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;
import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;

public class BuildFuncPanel extends ContentImpl {

    private ToolWindow toolWindow;
    private List<BuildFuncHandler> buildFuncHandlers;
    private JPanel terminalPanel;
    private DefaultTreeModel treeModel;
    private Tree tree;

    public BuildFuncPanel(ToolWindow toolWindow) {
        super(null, BUILDFUNC_CONTENT_NAME, true);
        this.toolWindow = toolWindow;
        this.buildFuncHandlers = new ArrayList<>();
        setComponent(createMainPanel());
    }

    public BuildFuncHandler createBuildFuncHandler(Project project, Function function) {
        ensureToolWindowOpened();
        ensureOldHandlerIsDisposed(function);

        final Icon[] nodeIcon = {new AnimatedIcon.FS()};
        final String[] location = {"running ..."};
        BuildFuncHandler buildFuncHandler = initBuildFuncHandler(project, function, nodeIcon, location);
        buildFuncHandlers.add(buildFuncHandler);

        drawBuildFuncHandler(project, buildFuncHandler, nodeIcon, location);

        return buildFuncHandler;
    }

    private void drawBuildFuncHandler(Project project, BuildFuncHandler buildFuncHandler, Icon[] nodeIcon, String[] location) {
        addBuildFuncTreeNode(project, buildFuncHandler, nodeIcon, location);
        updateTerminalPanel(buildFuncHandler.getTerminalExecutionConsole());
    }

    private void addBuildFuncTreeNode(Project project, BuildFuncHandler buildFuncHandler, Icon[] nodeIcon, String[] location) {
        DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                new LabelAndIconDescriptor(project,
                        buildFuncHandler,
                        () -> "Build " + buildFuncHandler.getFuncName() + ":",
                        () -> location[0],
                        () -> nodeIcon[0],
                        null));
        treeModel.insertNodeInto(buildNode, (MutableTreeNode) treeModel.getRoot(), 0);
        tree.invalidate();
        tree.expandPath(new TreePath(((DefaultMutableTreeNode)treeModel.getRoot()).getPath()));
    }

    private BuildFuncHandler initBuildFuncHandler(Project project, Function function, Icon[] nodeicon, String[] location) {
        BuildFuncHandler buildFuncHandler = new BuildFuncHandler(function.getName());
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
                buildFuncHandler.setEndTime();
            }
        };
        buildFuncHandler.setProcessListener(processListener);
        buildFuncHandler.setTerminalExecutionConsole(commonTerminalExecutionConsole);
        return buildFuncHandler;
    }

    private void ensureOldHandlerIsDisposed(Function function) {
        Optional<BuildFuncHandler> buildFuncHandler = buildFuncHandlers.stream()
                .filter(buildFunc -> buildFunc.getFuncName().equals(function.getName()))
                .findFirst();
        if (buildFuncHandler.isPresent()) {
            buildFuncHandler.get().dispose();
            removeNode(function.getName());
            buildFuncHandlers.remove(buildFuncHandler.get());
        }
    }

    private void ensureToolWindowOpened() {
        if (toolWindow.isVisible()
                && toolWindow.isActive()
                && toolWindow.isAvailable()) {
            return;
        }
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAvailable(true, null);
        toolWindow.activate(null);
        toolWindow.show(null);
    }

    private void removeNode(String name) {
        int children = treeModel.getChildCount(treeModel.getRoot());
        for (int i = 0; i<children; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeModel.getChild(treeModel.getRoot(), i);
            String nodeName = ((BuildFuncHandler)((LabelAndIconDescriptor)child.getUserObject()).getElement()).getFuncName();
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

    private void updateTerminalPanel(ConsoleView console) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(console.getComponent(), BorderLayout.CENTER);
        updateTerminalPanel(panel);
    }

    private void updateTerminalPanel(JComponent component) {
        terminalPanel.removeAll();
        terminalPanel.add(component, BorderLayout.CENTER);
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
                BuildFuncHandler buildFuncHandler = ((BuildFuncHandler)((LabelAndIconDescriptor<?>) node).getElement());
                if (selected) {
                    updateTerminalPanel(buildFuncHandler.getTerminalExecutionConsole());
                }

                ((LabelAndIconDescriptor) node).update();
                return createLabel(((LabelAndIconDescriptor) node).getPresentation().getPresentableText(),
                        ((LabelAndIconDescriptor) node).getPresentation().getLocationString(),
                        ((LabelAndIconDescriptor) node).getIcon(),
                        (BuildFuncHandler) ((LabelAndIconDescriptor<?>) node).getElement()
                );
            }
            return null;
        };
    }

    private JComponent createLabel(String name, String location, Icon icon, BuildFuncHandler buildFuncHandler) {
        JPanel buildInfo = new JPanel(new BorderLayout());
        String label = "<html><span style=\"font-weight: bold;\">" + name + "</span> ";
        if (location != null && !location.isEmpty() ) {
            label += location;
        }
        label += "</html> ";
        buildInfo.add(new JLabel(label, icon, SwingConstants.LEFT), BorderLayout.CENTER);

        JLabel lblDuration = new JLabel(getDuration(buildFuncHandler));
        lblDuration.setForeground(JBColor.GRAY);
        buildInfo.add(lblDuration, BorderLayout.EAST);
        return buildInfo;
    }

    public String getDuration(BuildFuncHandler buildFuncHandler) {
        long duration;
        if (buildFuncHandler.getEndTime() != -1) {
            duration = buildFuncHandler.getEndTime() - buildFuncHandler.getStartTime();
        } else {
            duration = System.currentTimeMillis() - buildFuncHandler.getStartTime();
        }

        String durationText = StringUtil.formatDurationApproximate(duration);
        int index = durationText.indexOf("s ");
        if (index != -1) {
            durationText = durationText.substring(0, index + 1);
        }
        return durationText;
    }
}
