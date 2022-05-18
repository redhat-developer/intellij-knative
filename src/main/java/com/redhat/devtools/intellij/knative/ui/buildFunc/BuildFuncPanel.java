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

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.actions.toolbar.ShowBuildHistoryAction;
import com.redhat.devtools.intellij.knative.kn.Function;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.ide.plugins.PluginManagerConfigurable.SEARCH_FIELD_BORDER_COLOR;
import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;
import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;

public class BuildFuncPanel extends ContentImpl {

    private final ToolWindow toolWindow;
    private final Map<String, List<BuildFuncHandler>> funcPerHandlers;
    private JPanel terminalPanel;
    private DefaultTreeModel buildTreeModel;
    private Tree buildTree;
    private boolean showHistory;
    private ShowBuildHistoryAction showBuildHistoryAction;

    public BuildFuncPanel(ToolWindow toolWindow) {
        super(null, BUILDFUNC_CONTENT_NAME, true);
        this.toolWindow = toolWindow;
        this.funcPerHandlers = new HashMap<>();
        this.showHistory = false;
        setComponent(createMainComponent());
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public void switchShowHistoryMode() {
        showHistory = !showHistory;
        refreshBuildTree();
    }

    private void refreshBuildTree() {
        removeNodeOrAll(null);
        funcPerHandlers.entrySet().stream()
                .sorted(Comparator.comparing(p -> p.getValue().get(0).getStartingDate())).forEachOrdered((entry) -> {
                    drawBuildFuncHandler(entry.getValue());
        });
    }

    public BuildFuncHandler createBuildFuncHandler(Project project, Function function) {
        ensureToolWindowOpened();
        removeNodeOrAll(function.getName());

        BuildFuncHandler buildFuncHandler = new BuildFuncHandler(project, function);
        List<BuildFuncHandler> buildFuncHandlers = funcPerHandlers.getOrDefault(function.getName(), new ArrayList<>());
        buildFuncHandlers.add(0, buildFuncHandler);
        if (buildFuncHandlers.size() > 10) {
            buildFuncHandlers.remove(10);
        }
        funcPerHandlers.put(function.getName(), buildFuncHandlers);

        drawBuildFuncHandler(buildFuncHandlers);

        return buildFuncHandler;
    }

    private void drawBuildFuncHandler(List<BuildFuncHandler> buildFuncHandlers) {
        addBuildFuncTreeNode(buildFuncHandlers);
        updateTerminalPanel(buildFuncHandlers.get(0).getTerminalExecutionConsole());
    }

    private String createReadableHistoryLocation(BuildFuncHandler buildFuncHandler) {
        if (buildFuncHandler.isFinished()) {
            return (!buildFuncHandler.isSuccessfullyCompleted() ?
                    buildFuncHandler.getState() :
                    buildFuncHandler.getFunction().getImage()) +
                        " <span style=\"color: gray;\">At " + buildFuncHandler.getStartingDate() + "</span>";
        }
        return buildFuncHandler.getState();
    }

    private void addBuildFuncTreeNode(List<BuildFuncHandler> buildFuncHandlers) {
        BuildFuncHandler runningBuild = buildFuncHandlers.get(0);
        DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                new LabelAndIconDescriptor(runningBuild.getProject(),
                        runningBuild,
                        () -> showHistory ? runningBuild.getFuncName() + " [latest-build]:" : "Build " + runningBuild.getFuncName() + " [latest]:",
                        () -> createReadableBuildLocation(runningBuild),
                        runningBuild::getStateIcon,
                        null));
        buildTreeModel.insertNodeInto(buildNode, (MutableTreeNode) buildTreeModel.getRoot(), 0);

        if(showHistory) {
            addChildrenToBuildFuncTreeNode(buildNode, buildFuncHandlers);
        }

        buildTree.invalidate();
        buildTree.expandPath(new TreePath(((DefaultMutableTreeNode) buildTreeModel.getRoot()).getPath()));
        buildTree.setSelectionRow(0);
    }

    private void addChildrenToBuildFuncTreeNode(DefaultMutableTreeNode parent, List<BuildFuncHandler> buildFuncHandlers) {
        for (BuildFuncHandler buildFuncHandler: buildFuncHandlers) {
            DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                    new LabelAndIconDescriptor(buildFuncHandler.getProject(),
                            buildFuncHandler,
                            () -> "Build " + buildFuncHandler.getFuncName(),
                            () -> createReadableHistoryLocation(buildFuncHandler),
                            buildFuncHandler::getStateIcon,
                            null));
            parent.add(buildNode);
        }
    }

    private String createReadableBuildLocation(BuildFuncHandler buildFuncHandler) {
        if (buildFuncHandler.isFinished()) {
            return !buildFuncHandler.isSuccessfullyCompleted() ?
                    buildFuncHandler.getState() :
                    buildFuncHandler.getState() + (
                            buildFuncHandler.getFunction().getImage().isEmpty() ?
                                    "" :
                                    " <span style=\"color: gray;\">" + buildFuncHandler.getFunction().getImage() + "</span>"
                            );
        }
        return buildFuncHandler.getState();
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

    private void removeNodeOrAll(String name) {
        int children = buildTreeModel.getChildCount(buildTreeModel.getRoot());
        for (int i = children - 1; i>=0; i--) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) buildTreeModel.getChild(buildTreeModel.getRoot(), i);
            if (name == null) {
                buildTreeModel.removeNodeFromParent(child);
            } else {
                String nodeName = ((BuildFuncHandler)((LabelAndIconDescriptor)child.getUserObject()).getElement()).getFuncName();
                if (nodeName.equals(name)) {
                    buildTreeModel.removeNodeFromParent(child);
                    break;
                }
            }
        }
    }

    private JComponent createMainComponent() {
        JComponent mainPanel = createMainPanel();
        ActionToolbar actionToolbar = createActionsColumn();
        actionToolbar.setTargetComponent(mainPanel);

        SimpleToolWindowPanel wrapper = new SimpleToolWindowPanel(false, true);
        wrapper.setContent(mainPanel);
        wrapper.setToolbar(actionToolbar.getComponent());
        wrapper.revalidate();
        return wrapper;
    }

    private ActionToolbar createActionsColumn() {
        ensureInitActions();

        DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        toolbarGroup.add(showBuildHistoryAction);

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, toolbarGroup, false);
    }

    private void ensureInitActions() {
        showBuildHistoryAction = new ShowBuildHistoryAction(this);
    }

    private JComponent createMainPanel() {
        OnePixelSplitter mainSplitter = createSplitter();
        mainSplitter.setFirstComponent(buildBuildsTree());
        mainSplitter.setSecondComponent(buildTerminalPanel());
        return mainSplitter;
    }

    private OnePixelSplitter createSplitter() {
        return new OnePixelSplitter(false, (float) 0.40) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
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
        buildTreeModel = createTreeModel();
        buildTree = buildTree("build", buildTreeModel);
        buildTree.addTreeSelectionListener(e -> {
            try {
                Object pathComponent = e.getNewLeadSelectionPath().getLastPathComponent();
                Object node = TreeUtil.getUserObject(pathComponent);

                if (node instanceof LabelAndIconDescriptor) {
                    BuildFuncHandler buildFuncHandler = ((BuildFuncHandler)((LabelAndIconDescriptor<?>) node).getElement());
                    updateTerminalPanel(buildFuncHandler.getTerminalExecutionConsole());
                }
            } catch (Exception ignored) {}
        });
        return new JBScrollPane(buildTree);
    }

    private DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        return new DefaultTreeModel(root);
    }

    private Tree buildTree(String name, TreeModel treeModel) {
        Tree tree = new Tree(treeModel);
        UIUtil.putClientProperty(tree, ANIMATION_IN_RENDERER_ALLOWED, true);
        tree.setCellRenderer(getTreeCellRenderer());
        tree.setVisible(true);
        tree.setRootVisible(false);
        tree.setName(name);
        return tree;
    }

    private TreeCellRenderer getTreeCellRenderer() {
        return (tree1, value, selected, expanded, leaf, row, hasFocus) -> {
            Object node = TreeUtil.getUserObject(value);

            if (node instanceof LabelAndIconDescriptor) {
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

        String durationText = StringUtil.formatDuration(duration);
        int index = durationText.indexOf("s ");
        if (index != -1) {
            durationText = durationText.substring(0, index + 1);
        }
        return durationText;
    }
}
