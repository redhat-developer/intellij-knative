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
package com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import com.redhat.devtools.intellij.knative.actions.toolbar.ShowFunctionTaskHistoryAction;
import com.redhat.devtools.intellij.knative.actions.toolbar.StopFunctionTaskAction;
import com.redhat.devtools.intellij.knative.utils.UIUtils;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.intellij.ui.AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED;

public abstract class BuildRunDeployFuncPanel extends ContentImpl {

    protected final ToolWindow toolWindow;
    private final String displayName;
    protected final Map<String, List<IFuncAction>> funcPerActionHandlers;
    protected JPanel terminalPanel;
    protected DefaultTreeModel buildTreeModel;
    protected Tree buildTree;
    protected boolean showHistory;

    public BuildRunDeployFuncPanel(ToolWindow toolWindow, String displayName) {
        super(null, displayName, true);
        this.toolWindow = toolWindow;
        this.displayName = displayName;
        this.funcPerActionHandlers = new HashMap<>();
        this.showHistory = false;
        setComponent(createMainComponent());
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
        return Arrays.asList(
                new ShowFunctionTaskHistoryAction(this),
                new StopFunctionTaskAction(this)
        );
    }

    protected JComponent createMainPanel() {
        OnePixelSplitter mainSplitter = UIUtils.createSplitter(false, (float) 0.40);
        mainSplitter.setFirstComponent(buildBuildsTree());
        mainSplitter.setSecondComponent(buildTerminalPanel());
        return mainSplitter;
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
        buildTree = buildTree(displayName, buildTreeModel);
        buildTree.addTreeSelectionListener(e -> {
            try {
                Object pathComponent = e.getNewLeadSelectionPath().getLastPathComponent();
                updateTerminalBySelectedPath(pathComponent);
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

            if (node instanceof FuncActionNodeDescriptor) {
                ((FuncActionNodeDescriptor) node).update();
                return createLabel(((FuncActionNodeDescriptor) node).getPresentation().getPresentableText(),
                        ((FuncActionNodeDescriptor) node).getPresentation().getLocationString(),
                        ((FuncActionNodeDescriptor) node).getIcon(),
                        ((FuncActionNodeDescriptor) node).getElement()
                );
            }
            return null;
        };
    }

    private JComponent createLabel(String name, String location, Icon icon, IFuncAction actionFuncHandler) {
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

    public String getDuration(IFuncAction actionFuncHandler) {
        long duration;
        if (actionFuncHandler.getStartTime() == -1) {
            return "0";
        } else if (actionFuncHandler.getEndTime() != -1) {
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

    public void setSelectionDefault() {
        if (getChildrenCount() > 0) {
            buildTree.setSelectionRow(0);
            Object pathComponent = buildTree.getLastSelectedPathComponent();
            updateTerminalBySelectedPath(pathComponent);
        }
    }

    private int getChildrenCount() {
        return buildTreeModel.getChildCount(buildTreeModel.getRoot());
    }

    private void updateTerminalBySelectedPath(Object path) {
        try {
            Object node = TreeUtil.getUserObject(path);

            if (node instanceof FuncActionNodeDescriptor) {
                IFuncAction actionNode = ((FuncActionNodeDescriptor) node).getElement();
                if (actionNode instanceof FuncActionPipeline) {
                    updateTerminalPanel(((FuncActionPipeline)actionNode).getRunningStep().getTerminalExecutionConsole());
                } else if (actionNode instanceof FuncActionTask) {
                    updateTerminalPanel(((FuncActionTask)actionNode).getTerminalExecutionConsole());
                }
            }
        } catch (Exception ignored) {}
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public void switchHistoryMode() {
        showHistory = !showHistory;
        refreshBuildTree();
    }

    private void refreshBuildTree() {
        removeAllNodes();
        drawAllNodes();
    }

    private void drawAllNodes() {
        funcPerActionHandlers.entrySet().stream()
                .sorted(Comparator.comparing(p -> p.getValue().get(0).getStartingDate())).forEachOrdered((entry) -> {
                    drawFuncActionHandlers(entry.getValue(), true);
                });
    }

    private void removeAllNodes() {
        removeNodeOrAll(null);
    }

    private void removeNodeOrAll(String name) {
        int childrenCount = getChildrenCount();
        for (int i = childrenCount - 1; i>=0; i--) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) buildTreeModel.getChild(buildTreeModel.getRoot(), i);
            if (name == null) {
                buildTreeModel.removeNodeFromParent(child);
            } else {
                String nodeName = ((FuncActionNodeDescriptor)child.getUserObject()).getElement().getFuncName();
                if (nodeName.equals(name)) {
                    buildTreeModel.removeNodeFromParent(child);
                    break;
                }
            }
        }
    }

    public void drawFuncAction(IFuncAction funcAction) {
        boolean isPipeline = funcAction instanceof FuncActionPipeline;
        removeNode(funcAction.getFuncName());

        if (isPipeline) {
            ensureToolWindowOpened();
            ((FuncActionPipeline)funcAction).addStepChangeListener(element -> {
                try {
                    Object pathComponent = buildTree.getLastSelectedPathComponent();
                    updateTerminalBySelectedPath(pathComponent);
                } catch (Exception ignored) {}
            });
        }

        List<IFuncAction> funcActionHandlers = funcPerActionHandlers.getOrDefault(funcAction.getFuncName(), new ArrayList<>());
        funcActionHandlers.add(0, funcAction);
        if (funcActionHandlers.size() > 10) {
            funcActionHandlers.remove(10);
        }
        funcPerActionHandlers.put(funcAction.getFuncName(), funcActionHandlers);

        drawFuncActionHandlers(funcActionHandlers, isPipeline);
    }

    private void removeNode(String name) {
        removeNodeOrAll(name);
    }

    private void drawFuncActionHandlers(List<IFuncAction> funcActionHandlers, boolean updateTerminal) {
        DefaultMutableTreeNode node = createFuncActionTreeNode(funcActionHandlers);
        addFuncActionTreeNode(node);
        if (updateTerminal) {
            updateTerminalPanel(getDefaultTerminal(funcActionHandlers.get(0)));
        }
    }

    protected abstract DefaultMutableTreeNode createFuncActionTreeNode(List<IFuncAction> actionFuncHandlers);

    private void addFuncActionTreeNode(DefaultMutableTreeNode node) {
        buildTreeModel.insertNodeInto(node, (MutableTreeNode) buildTreeModel.getRoot(), 0);
        buildTree.invalidate();
        buildTree.expandPath(new TreePath(((DefaultMutableTreeNode) buildTreeModel.getRoot()).getPath()));
        buildTree.setSelectionRow(0);
    }

    private TerminalExecutionConsole getDefaultTerminal(IFuncAction funcActionHandlers) {
        if (funcActionHandlers instanceof FuncActionPipeline) {
            return ((FuncActionPipeline)funcActionHandlers).getRunningStep().getTerminalExecutionConsole();
        } else {
            return ((FuncActionTask)funcActionHandlers).getTerminalExecutionConsole();
        }
    }

    private void ensureToolWindowOpened() {
        if (toolWindow.isVisible()
                && toolWindow.isActive()
                && toolWindow.isAvailable()) {
            ensureContentIsSelected();
            return;
        }
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAvailable(true, null);
        toolWindow.activate(null);
        toolWindow.show(null);
        ensureContentIsSelected();
    }

    private void ensureContentIsSelected() {
        toolWindow.getContentManager().setSelectedContent(this, true);
    }

    protected DefaultMutableTreeNode createTreeNode(IFuncAction funcAction, Supplier<String> label, Supplier<String> location) {
        return new DefaultMutableTreeNode(
                new FuncActionNodeDescriptor(
                        funcAction.getProject(),
                        funcAction,
                        label,
                        location,
                        funcAction::getStateIcon,
                        null)
        );
    }

    protected String getBuildLocation(IFuncAction funcAction) {
        return getBuildLocation(funcAction, "");
    }

    protected String getBuildLocation(IFuncAction funcAction, String defaultState) {
        String state = funcAction.getState() == null || funcAction.getState().isEmpty() ? defaultState : funcAction.getState();
        return !funcAction.isSuccessfullyCompleted() ?
                state :
                state + (
                        funcAction.getFunction().getImage().isEmpty() ?
                                "" :
                                " <span style=\"color: gray;\">" + funcAction.getFunction().getImage() + "</span>"
                );
    }

    protected String getNodeLocation(IFuncAction funcAction) {
        return !funcAction.isSuccessfullyCompleted() ?
                funcAction.getState() :
                funcAction.getState() + " <span style=\"color: gray;\">At " + funcAction.getStartingDate() + "</span>";
    }

    public IFuncAction getSelectedFuncActionNode() {
        Object pathComponent = buildTree.getLastSelectedPathComponent();
        Object node = TreeUtil.getUserObject(pathComponent);
        if (node instanceof FuncActionNodeDescriptor) {
            return ((FuncActionNodeDescriptor) node).getElement();
        }
        return null;
    }
}
