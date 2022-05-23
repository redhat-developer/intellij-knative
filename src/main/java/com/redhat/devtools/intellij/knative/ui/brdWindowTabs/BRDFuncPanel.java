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
package com.redhat.devtools.intellij.knative.ui.brdWindowTabs;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.ide.plugins.PluginManagerConfigurable.SEARCH_FIELD_BORDER_COLOR;
import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;

public abstract class BRDFuncPanel extends ContentImpl implements ActionFuncHandlerListener {

    protected final ToolWindow toolWindow;
    protected final Map<String, List<ActionFuncHandler>> funcPerHandlers;
    protected JPanel terminalPanel;
    protected DefaultTreeModel buildTreeModel;
    protected Tree buildTree;
    protected boolean showHistory;

    public BRDFuncPanel(ToolWindow toolWindow, String displayName) {
        super(null, displayName, true);
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
                    drawActionFuncHandlers(entry.getValue());
                });
    }

    public abstract ActionFuncHandler createActionFuncHandler(Project project, Function function, List<String> steps);

    protected ActionFuncHandler createActionFuncHandler(String name, Project project, Function function, List<String> steps) {
        ensureToolWindowOpened();
        removeNodeOrAll(function.getName());

        ActionFuncHandler actionFuncHandler = new ActionFuncHandler(name, project, function, steps);
        actionFuncHandler.addStepChangeListener(element -> {
            try {
                Object pathComponent = buildTree.getLastSelectedPathComponent();
                Object node = TreeUtil.getUserObject(pathComponent);

                if (node instanceof LabelAndIconDescriptor) {
                    if (((LabelAndIconDescriptor<?>) node).getElement() instanceof  ActionFuncHandler) {
                        ActionFuncHandler buildFuncHandler = ((ActionFuncHandler)((LabelAndIconDescriptor<?>) node).getElement());
                        if (buildFuncHandler.equals(element)) {
                            updateTerminalPanel(element.getRunningStep().getTerminalExecutionConsole());
                        }
                    }
                }
            } catch (Exception ignored) {}
        });
        List<ActionFuncHandler> actionFuncHandlers = funcPerHandlers.getOrDefault(function.getName(), new ArrayList<>());
        actionFuncHandlers.add(0, actionFuncHandler);
        if (actionFuncHandlers.size() > 10) {
            actionFuncHandlers.remove(10);
        }
        funcPerHandlers.put(function.getName(), actionFuncHandlers);

        drawActionFuncHandlers(actionFuncHandlers);

        return actionFuncHandler;
    }

    private void drawActionFuncHandlers(List<ActionFuncHandler> actionFuncHandlers) {
        DefaultMutableTreeNode node = createActionFuncTreeNode(actionFuncHandlers);
        addFuncHandlerTreeNode(node);
        updateTerminalPanel(actionFuncHandlers.get(0).getFirstStep().getTerminalExecutionConsole());
    }

    protected String createReadableHistoryLocation(ActionFuncHandler actionFuncHandler) {
        if (actionFuncHandler.isFinished()) {
            return (!actionFuncHandler.isSuccessfullyCompleted() ?
                    actionFuncHandler.getState() :
                    actionFuncHandler.getFunction().getImage()) +
                    " <span style=\"color: gray;\">At " + actionFuncHandler.getStartingDate() + "</span>";
        }
        return actionFuncHandler.getState();
    }

    protected abstract DefaultMutableTreeNode createActionFuncTreeNode(List<ActionFuncHandler> actionFuncHandlers);

   // protected abstract void addChildrenToBuildFuncTreeNode(DefaultMutableTreeNode parent, List<ActionFuncHandler> actionFuncHandlers);

    private void addFuncHandlerTreeNode(DefaultMutableTreeNode node) {
        buildTreeModel.insertNodeInto(node, (MutableTreeNode) buildTreeModel.getRoot(), 0);
        buildTree.invalidate();
        buildTree.expandPath(new TreePath(((DefaultMutableTreeNode) buildTreeModel.getRoot()).getPath()));
        buildTree.setSelectionRow(0);
    }

  /*  private void addChildrenToBuildFuncTreeNode(DefaultMutableTreeNode parent, List<ActionFuncHandler> actionFuncHandlers) {
        for (ActionFuncHandler buildFuncHandler: actionFuncHandlers) {
            DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                    new LabelAndIconDescriptor(buildFuncHandler.getProject(),
                            buildFuncHandler,
                            () -> "Build " + buildFuncHandler.getFuncName(),
                            () -> createReadableHistoryLocation(buildFuncHandler),
                            buildFuncHandler::getStateIcon,
                            null));
            parent.add(buildNode);
        }
    }*/

    protected String createReadableBuildLocation(ActionFuncHandler actionFuncHandler) {
        if (actionFuncHandler.isFinished()) {
            return !actionFuncHandler.isSuccessfullyCompleted() ?
                    actionFuncHandler.getState() :
                    actionFuncHandler.getState() + (
                            actionFuncHandler.getFunction().getImage().isEmpty() ?
                                    "" :
                                    " <span style=\"color: gray;\">" + actionFuncHandler.getFunction().getImage() + "</span>"
                    );
        }
        return actionFuncHandler.getState();
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
                String nodeName = ((ActionFuncHandler)((LabelAndIconDescriptor)child.getUserObject()).getElement()).getFuncName();
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
        DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        for (AnAction action: getToolbarActions()) {
            toolbarGroup.add(action);
        }
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, toolbarGroup, false);
    }

    protected List<AnAction> getToolbarActions() {
        return Collections.singletonList(
                new ShowBuildHistoryAction(this)
        );
    }

    protected JComponent createMainPanel() {
        OnePixelSplitter mainSplitter = createSplitter();
        mainSplitter.setFirstComponent(buildBuildsTree());
        mainSplitter.setSecondComponent(buildTerminalPanel());
        return mainSplitter;
    }

    protected OnePixelSplitter createSplitter() {
        return new OnePixelSplitter(false, (float) 0.40) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(SEARCH_FIELD_BORDER_COLOR);
                return divider;
            }
        };
    }

    protected JComponent buildTerminalPanel() {
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
                    if (((LabelAndIconDescriptor<?>) node).getElement() instanceof  ActionFuncHandler) {
                        ActionFuncHandler buildFuncHandler = ((ActionFuncHandler)((LabelAndIconDescriptor<?>) node).getElement());
                        updateTerminalPanel(buildFuncHandler.getRunningStep().getTerminalExecutionConsole());
                    } else if (((LabelAndIconDescriptor<?>) node).getElement() instanceof ActionFuncStepHandler) {
                        ActionFuncStepHandler stepHandler = ((ActionFuncStepHandler)((LabelAndIconDescriptor<?>) node).getElement());
                        updateTerminalPanel(stepHandler.getTerminalExecutionConsole());
                    }
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
                        (ActionFuncHandler) ((LabelAndIconDescriptor<?>) node).getElement()
                );
            }
            return null;
        };
    }

    private JComponent createLabel(String name, String location, Icon icon, ActionFuncHandler actionFuncHandler) {
        JPanel buildInfo = new JPanel(new BorderLayout());
        String label = "<html><span style=\"font-weight: bold;\">" + name + "</span> ";
        if (location != null && !location.isEmpty() ) {
            label += location;
        }
        label += "</html> ";
        buildInfo.add(new JLabel(label, icon, SwingConstants.LEFT), BorderLayout.CENTER);

        JLabel lblDuration = new JLabel(getDuration(actionFuncHandler));
        lblDuration.setForeground(JBColor.GRAY);
        buildInfo.add(lblDuration, BorderLayout.EAST);
        return buildInfo;
    }

    public String getDuration(ActionFuncHandler actionFuncHandler) {
        long duration;
        if (actionFuncHandler.getEndTime() != -1) {
            duration = actionFuncHandler.getEndTime() - actionFuncHandler.getStartTime();
        } else {
            duration = System.currentTimeMillis() - actionFuncHandler.getStartTime();
        }

        String durationText = StringUtil.formatDuration(duration);
        int index = durationText.indexOf("s ");
        if (index != -1) {
            durationText = durationText.substring(0, index + 1);
        }
        return durationText;
    }

    @Override
    public void fireModified(ActionFuncHandler element) {

    }
}
